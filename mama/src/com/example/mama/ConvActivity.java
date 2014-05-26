package com.example.mama;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ConvActivity extends ActionBarActivity {

	Context cxt;
	List<Message> msgs;
	List<Message> Trash;
	ListView msgsListView;
	EditText textTosend;
	String regIdToSend;
	String name;
	String myname;
	Button send;
	DBManager db_mngr;
	msgAdapter adapter;
	int ischanged = -1;// if changes to not 0 then the value will be the index
	private dataReciever dr;
	final String sendUrl = "http://wabbass.byethost9.com/android/send_from_to.php";
	JSONParser jParser;

	@Override
	protected void onStart() {
		showMsgs(this.name);
		super.onStart();

	}

	@Override
	protected void onStop() {
		ActivityManager.getInstance().UpdateState(ActivityManager.CONV,
				ActivityManager.STOPPED, name);
		ActivityManager.getInstance().getStatus();
		if (dr != null) {
			unregisterReceiver(dr);
			dr = null;
		}
		Utilties.saveMsgs(this.ischanged, this.msgs, this.Trash, this.name,
				this.db_mngr);
		msgs.clear();
		super.onStop();
	}

	@Override
	protected void onPause() {

		Log.i("Act", " Conv Activity on pause");
		super.onPause();
	}

	@Override
	protected void onResume() {

		Log.i("Act", " Conv Activity on resume");
		super.onResume();
	}

	@Override
	protected void onRestart() {

		Log.i("Act", " Conv Activity on restart");
		super.onRestart();
	}

	@Override
	protected void onDestroy() {

		Log.i("Act", " Conv Activity on destroy");

		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conv);
		Log.i("Act", " Conv Activity on create");

		initlization();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.conv, menu);
		ActionBar bar = getSupportActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#79916a")));
		this.setTitleColor(Color.BLACK);
		return true;
	}

	/* initlize database ,UI,listners */
	private void initlization() {
		db_mngr = new DBManager(getApplicationContext());
		cxt = getApplicationContext();
		msgs = new ArrayList<Message>();
		Trash = new ArrayList<Message>();
		jParser = new JSONParser();
		msgsListView = (ListView) findViewById(R.id.listConv);
		send = (Button) findViewById(R.id.Send);
		textTosend = (EditText) findViewById(R.id.TextFiled);
		textTosend.setTextColor(Color.parseColor("#710ffb"));
		send.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (checkConnection()) {
					String t = textTosend.getText().toString();
					if (!t.isEmpty()) {
						Utilties.sendMsg(t, name, myname, Utilties.sendUrl);
						// new sendmsg().execute(t, name);
						msgs.add(new Message(t, "S", Utilties.getexactTime()));
						if (ischanged == -1)
							ischanged = msgs.size() - 1;
						adapter.notifyDataSetChanged();
						msgsListView.setSelection(adapter.getCount() - 1);
						textTosend.setText("");
					}
				} else {
					Toast.makeText(getApplicationContext(),
							"You have no Internet Connection ",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		Bundle ext = getIntent().getExtras();
		if (ext != null) {
			// maybe get the id for security reasons
			this.myname = ext.getString("myname");
			this.name = ext.getString("contactName");
			this.regIdToSend = ext.getString("regID");

		}
		this.setTitle(name);
		msgsListView.setLongClickable(true);
		msgsListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Message m = msgs.get(arg2);
				showDialog(m);
				return false;
			}
		});
		if (ext.getBoolean("Newadding")
				&& !db_mngr.hasConversation(new Conversation(new Contact("",
						name, "", ""), null))) {
			int notfyId = getIntent().getIntExtra(Utilties.EXTRA_NOTIFICATION,
					-1);
			if (notfyId != -1) {
				NotificationManager notMngr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				try {
					notMngr.cancel(Utilties.notfyId);

				} catch (Exception e) {
					e.printStackTrace();
				}

				Message m = new Message(ext.getString(Utilties.EXTRA_MESSAGE),
						"R", ext.getString(Utilties.EXTRA_Date));
				Conversation con = new Conversation(new Contact("", name, "",
						""), null);
				con.addMsg(m);
				db_mngr.addConversation(con);
				db_mngr.addMsg(new Contact("", name, "", ""), m);
			}
		}
	}

	/* show dialog for copy and delete actions */
	private void showDialog(final Message m) {
		CharSequence colors[] = new CharSequence[] { "Delete", "Copy" };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose Action");
		builder.setItems(colors, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// the user clicked on colors[which]
				switch (which) {
				case 0:
					deleteMsg(m);
					break;

				case 1:

					break;
				}
			}
		});
		builder.show();
	}

	/* delte msg selected by user */
	private void deleteMsg(Message m) {

		if (msgs.remove(m)) {
			Trash.add(m);
			adapter.notifyDataSetChanged();
		}
	}

	/*
	 * how ask dialof for if the user wamts to talk to the asker with hint -if
	 * ok then replace the name and the msg adapter and id to contact and so on
	 * -if no send the asker refuse message
	 */
	private void showAskDialog(final Contact contact, final Message m) {

		final Conversation c = new Conversation(contact, null);
		c.addMsg(m);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Would like to talk to :" + contact.getName());

		builder.setPositiveButton("OK", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Conversation tmp = new Conversation(contact, null);
				tmp.addMsg(m);
				db_mngr.addConversation(tmp);

				String n = tmp.getSender().getName();
				String ID = tmp.getSender().getID();
				// exsiting saving current session
				Utilties.saveMsgs(ischanged, msgs, Trash, name, db_mngr);
				msgs.clear();
				ischanged = -1;
				// /load new session
				name = n;
				setTitle(name);
				regIdToSend = ID;
				showMsgs(name);
				msgs.add(m);
				if (ischanged == -1)
					ischanged = msgs.size() - 1;
				adapter.notifyDataSetChanged();
				msgsListView.setSelection(adapter.getCount() - 1);

			}
		});
		builder.setNegativeButton("Refuse", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Utilties.sendMsg("I cannot talk to you right now ,Sorry! ",
						contact.getName(), myname, Utilties.sendUrl);
				// new
				// sendmsg().execute("I cannot talk to you right now ,Sorry! ",
				// contact.getName());

			}
		});

		builder.show();

	}

	/*
	 * msgs adapter to list view
	 */
	class msgAdapter extends BaseAdapter {

		LayoutInflater inflater;

		// innitlizew inflater
		public msgAdapter() {
			inflater = (LayoutInflater) cxt
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {

			return msgs.size();
		}

		@Override
		public Object getItem(int arg0) {

			return msgs.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {

			return arg0;
		}

		// we have two types of messages sent and recieved and diffrent styles
		@Override
		public int getViewTypeCount() {
			return 2;

		}

		// determine the type of the message that will be shown and return 1 for
		// sent 0 for other
		@Override
		public int getItemViewType(int position) {

			// 1 for recieved 0 for sended
			String tmp = msgs.get(position).getType();
			if (tmp.equals("S")) {
				return 1;
			} else
				return 0;

		}

		/*
		 * inflate layout of specifec element in msgs list by type save holder
		 * by tag and restore when recycled
		 */
		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {

			ViewHolder holder = null;
			int type = getItemViewType(arg0);
			Message tmp = msgs.get(arg0);

			TextView msg = null;
			TextView time = null;
			if (arg1 == null) {
				switch (type) {
				// if the view not recycled
				// create holder and load the ids found into it and set it in
				// tag
				case 1:
					arg1 = inflater.inflate(R.layout.sender_conv_listitem,
							arg2, false);
					msg = (TextView) arg1.findViewById(R.id.receivermsg);
					time = (TextView) arg1.findViewById(R.id.dateTimeSender);
					holder = new ViewHolder();
					holder.msg = msg;
					holder.time = time;
					arg1.setTag(holder);
					break;
				case 0:
					arg1 = inflater.inflate(R.layout.recive_conv_listitem,
							arg2, false);
					msg = (TextView) arg1.findViewById(R.id.msgtxt);
					time = (TextView) arg1.findViewById(R.id.dateTimeRec);
					holder = new ViewHolder();
					holder.msg = msg;
					holder.time = time;
					arg1.setTag(holder);
					break;
				}

			} else {
				// if the view recycled get it
				holder = (ViewHolder) arg1.getTag();

			}
			// check for sure but there is no way that holder in this place be
			// null
			// set the data in the view holder and return view
			if (holder != null) {
				msg = holder.msg;
				time = holder.time;
				msg.setText(tmp.getText());
				time.setText(tmp.getTime());
			}
			return arg1;
		}

	}

	/*
	 * viw holder holds list elemnt for example that have many views that is
	 * different from style and have the same goal like msg text and time but
	 * different layouts
	 */
	static class ViewHolder {
		TextView msg;
		TextView time;
	}

	/*
	 * broadcast receiver that handles messages sended from clound and recieved
	 * by background reciver if the sender is not in friend list ask for talk if
	 * the the conversation is with the sender then append the msg and show
	 */
	class dataReciever extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Utilties.DISPLAY_MESSAGE_ACTION)) {
				Cursor data = db_mngr.getMsgsByContact(new Contact("00", intent
						.getExtras().getString(Utilties.CONTACT), "", ""));
				if (data.getCount() == 0) {
					showAskDialog(
							new Contact("", intent.getExtras().getString(
									Utilties.CONTACT), "", ""),
							new Message(intent.getExtras().getString(
									Utilties.EXTRA_MESSAGE), "R", intent
									.getExtras().getString(Utilties.EXTRA_Date)));

				} else {

					String msg = intent.getExtras().getString(
							Utilties.EXTRA_MESSAGE);
					String date = intent.getExtras().getString(
							Utilties.EXTRA_Date);
					msgs.add(new Message(msg, "R", date));
					if (ischanged == -1)
						ischanged = msgs.size() - 1;
					Log.i("rcvr", msg);
					adapter.notifyDataSetChanged();
					msgsListView.setSelection(adapter.getCount() - 1);
				}
			}

		}

	}

	/*
	 * register the BR and load msgs from db and set the list to adapter and set
	 * it to last
	 */
	private void showMsgs(String name) {
		Log.i("Act", " Conv Activity on start");

		ActivityManager.getInstance().UpdateState(ActivityManager.CONV,
				ActivityManager.ACTIVE, name);
		ActivityManager.getInstance().getStatus();
		if (dr == null) {
			dr = new dataReciever();
			registerReceiver(dr, new IntentFilter(
					Utilties.DISPLAY_MESSAGE_ACTION));
		} else {
			if (dr != null) {
				unregisterReceiver(dr);
				dr = null;
			}
		}
		this.msgs = Utilties.loadMsgsFromDB(name, db_mngr);
		adapter = new msgAdapter();
		msgsListView.setAdapter(adapter);
		msgsListView.setSelection(adapter.getCount() - 1);
	}

	/*
	 * checks the if there valid connection
	 */
	private boolean checkConnection() {
		ConnectionDetector cd = new ConnectionDetector(this);
		return cd.isConnectingToInternet();
	}
}
