package com.example.mama;

import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Logger extends ActionBarActivity {
	ListView convsList;
	List<Conversation> Conversations;
	List<Conversation> Trash;
	List<String> contacts;

	Context cxt;
	String myname;
	DBManager db_mngr;
	consAdapter adapter;
	JSONParser jParser;
	final String CHECKURL = "http://wabbass.byethost9.com/android/check_user.php";
	final String GET_USERS_URL = "http://wabbass.byethost9.com/android/get_ids.php";
	int ischanged = -1;// if changes to not 0 then the value will be the index
	private logRec receiver = null;
	boolean toAdd=false;

	@Override
	protected void onStart() {

		Log.e("Act", " Logger Activity on start");
		ActivityManager.getInstance().UpdateState(ActivityManager.LOGGER,
				ActivityManager.ACTIVE, null);
		ActivityManager.getInstance().getStatus();
		loadData();
		ActivityManager.getInstance().updateLogger(Conversations);
		receiver = new logRec();

		registerReceiver(receiver, new IntentFilter(
				Utilties.DISPLAY_CONVERSATION_ACTION));
		this.setTitle("History of : " + myname);
		super.onStart();
	}

	@Override
	protected void onStop() {

		Log.e("Act", " Logger Activity on stop");
		ActivityManager.getInstance().UpdateState(ActivityManager.LOGGER,
				ActivityManager.STOPPED, null);
		ActivityManager.getInstance().getStatus();
		saveData();
		Conversations.clear();
		if (receiver != null)
			unregisterReceiver(receiver);
		super.onStop();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logger);
		Log.e("Act", " Logger Activity on create");
		jParser = new JSONParser();
		db_mngr = new DBManager(getApplicationContext());
		cxt = getApplicationContext();
		convsList = (ListView) findViewById(R.id.listConvs);
		Conversations = new ArrayList<Conversation>();
		Trash = new ArrayList<Conversation>();
		myname = getSharedPreferences("data", Context.MODE_PRIVATE).getString(
				"myname", "NULL");
		adapter = new consAdapter();
		if (!checkConnection())
			Toast.makeText(this,
					"You have no Internet Connection , Offline Mode  ",
					Toast.LENGTH_SHORT).show();
		convsList.setAdapter(adapter);
		convsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				String n = Conversations.get(arg2).getSender().getName();
				String ID = Conversations.get(arg2).getSender().getID();
				Intent i = new Intent(cxt, ConvActivity.class);
				i.putExtra("myname", myname);
				i.putExtra("contactName", n);
				i.putExtra("regID", ID);
				startActivity(i);

			}
		});
		convsList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				showDialog(Conversations.get(arg2));
				return false;
			}
		});
	}

	private void showDialog(final Conversation c) {
		CharSequence colors[] = new CharSequence[] { "Delete" };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose Action");

		builder.setItems(colors, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// the user clicked on colors[which]
				switch (which) {
				case 0:
					deleteConversation(c);
					break;

				case 1:

					break;
				}
			}
		});
		builder.show();
	}

	private void contactsDialog() {

		if (contacts != null) {

			final CharSequence cons[] = contacts
					.toArray(new CharSequence[contacts.size()]);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Choose Contact");

			builder.setItems(cons, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// the user clicked on colors[which]
					addCoversation(cons[which].toString());
				}
			});
			builder.show();
		} else
			Toast.makeText(this, "No Contacts Avalible !", Toast.LENGTH_LONG)
					.show();
	}

	private void showAskDialog(final Contact contact,final Message m) {
		
		final Conversation c=new Conversation(contact, null);
		c.addMsg(m);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(contact.getName()+"Want to talk to you ... \n Says : "+m.getText());
		builder.setPositiveButton("OK", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				Conversations.add(c);
				ischanged = Conversations.size() - 1;
				db_mngr.addMsg(contact, m);
				adapter.notifyDataSetChanged();
				
			}
		});
		builder.setNegativeButton("Refuse", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//reSend refuse to the sender 
				 Utilties.sendMsg("I cannot talk to you right now ,Sorry! ",contact.getName(),myname,Utilties.sendUrl);
				
			}
		});

		builder.show();

	}

	protected void deleteConversation(Conversation c) {
		// TODO Auto-generated method stub

		if (Conversations.remove(c)) {
			Trash.add(c);
			adapter.notifyDataSetChanged();
			ActivityManager.getInstance().updateLogger(Conversations);
		}
	}

	private void updateDB() throws SQLDataException {
		if (!Trash.isEmpty()) {
			for (Conversation c : Trash) {
				Cursor data = db_mngr.getMsgsByContact(c.getSender());
				while (data.moveToNext()) {
					String txt = data.getString(1);
					String date = data.getString(2);
					String type = data.getString(3);
					Message tmp = new Message(txt, type, date);
					if (!db_mngr.deleteMsgItem(tmp))
						throw new SQLDataException(
								"Go Fuck Ur Self Cannot Delete This Shit");
				}
				db_mngr.deleteConvItem(c);
			}
			Trash.clear();
		}
	}

	private void addDialog() {
		LayoutInflater li = LayoutInflater.from(this);
		View promptsView = li.inflate(R.layout.activity_dialog_conver, null);
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setView(promptsView);

		final EditText conName = (EditText) promptsView
				.findViewById(R.id.editAddDialog);
		alertBuilder.setCancelable(false)
				.setPositiveButton("ADD", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String con = conName.getText().toString();
						if (!con.isEmpty())
							addCoversation(con);

					}
				}).setNegativeButton("Cancel", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						dialog.cancel();

					}
				});

		AlertDialog aD = alertBuilder.create();
		aD.show();
	}

	private void loadData() {

		Cursor data = db_mngr.getCursorALL();
		while (data.moveToNext()) {
			String id = data.getString(0);
			String contact = data.getString(1);
			String phone = data.getString(2);
			String lastmsg = data.getString(3);
			Conversation con = new Conversation(new Contact(id, contact, phone,
					null), null);
			con.addMsg(new Message(lastmsg, "", null));
			Conversations.add(con);
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onPause() {
		Log.e("Act", " Logger Activity on pause");
		super.onPause();

	}

	@Override
	protected void onDestroy() {

		// saveData();
		Log.e("Act", " Logger Activity on destroy");
		super.onDestroy();
	}

	private void saveData() {
		if (ischanged != -1) {// start pushing from the index
			for (int i = ischanged; i < Conversations.size(); i++) {
				db_mngr.addConversation(Conversations.get(i));
				ischanged = -1;
			}

		}
		try {
			updateDB();
		} catch (SQLDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addCoversation(String contact) {
		Contact con = new Contact(contact + "_ID", contact, "", "");
		String result = null;

		try {
			result = new checkUser().execute(contact).get();
		} catch (InterruptedException e) {

			e.printStackTrace();
		} catch (ExecutionException e) {

			e.printStackTrace();
		}

		if (result == null) {
			Toast.makeText(cxt, contact + "not avilble on MAMA",
					Toast.LENGTH_LONG).show();

		} else if (result.equals("")) {
			Toast.makeText(cxt, contact + "not avilble on MAMA",
					Toast.LENGTH_LONG).show();
		} else {
			Message empty = new Message("", "E", "");
			Conversation c = new Conversation(con, null);
			if (!Conversations.contains(c)) {
				c.addMsg(empty);
				Conversations.add(c);
				if (ischanged == -1) {
					ischanged = Conversations.size() - 1;
				}
				ActivityManager.getInstance().updateLogger(Conversations);
				// saveData();
				Intent i = new Intent(cxt, ConvActivity.class);
				i.putExtra("myname", myname);
				i.putExtra("contactName", contact);
				i.putExtra("regID", c.getSender().getID());
				startActivity(i);
			} else {
				Toast.makeText(cxt, contact + "Conversation Already Exists",
						Toast.LENGTH_LONG).show();
			}
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				String conName = data.getExtras().getString("contactname");
				addCoversation(conName);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.logger, menu);
		ActionBar bar = getSupportActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#87684c")));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.addConv:
			if (checkConnection())
				addDialog();
			else
				Toast.makeText(this, "You have no Internet Connection ",
						Toast.LENGTH_SHORT).show();
			break;
		case R.id.Contacts:
			if (checkConnection())
				new getAllusers().execute();
			else
				Toast.makeText(this, "You have no Internet Connection ",
						Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	class consAdapter extends BaseAdapter {
		LayoutInflater inflater;
		Random rnd = new Random();

		public consAdapter() {
			inflater = (LayoutInflater) cxt
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {

			return Conversations.size();
		}

		@Override
		public Object getItem(int arg0) {

			return Conversations.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {

			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {

			TextView name = null, lastmsg = null;
			if (arg1 == null) {
				arg1 = inflater.inflate(R.layout.listitem, arg2, false);

			}
			name = (TextView) arg1.findViewById(R.id.CntkName);
			name.setText(Conversations.get(arg0).getSender().getName());
			lastmsg = (TextView) arg1.findViewById(R.id.lastmsg);
			lastmsg.setText(Conversations.get(arg0).getLastSaid());
			lastmsg.setTextColor(Color.parseColor("#000000"));
			name.setTextColor(Color.parseColor("#000000"));
			return arg1;
		}

	}

	class getAllusers extends AsyncTask<String, String, String> {
		int success = 0;

		@Override
		protected String doInBackground(String... args) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			JSONObject json = jParser.makeHttpRequest(GET_USERS_URL, "GET",
					params);
			try {
				success = json.getInt("success");
				if (success == 1) {
					JSONArray jsonArr = json.getJSONArray("IDs");
					contacts = new ArrayList<String>();
					for (int i = 0; i < jsonArr.length(); ++i) {
						JSONObject obj = jsonArr.getJSONObject(i);
						contacts.add(obj.getString("name"));

					}
				}
			} catch (JSONException e) {

				e.printStackTrace();
			}

			return "";
			// TODO Auto-generated method stub
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					contactsDialog();
				}
			});
			super.onPostExecute(result);
		}

	}

	class checkUser extends AsyncTask<String, String, String> {
		int success = 0;

		@Override
		protected String doInBackground(String... arg0) {

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user", arg0[0]));
			JSONObject json = jParser.makeHttpRequest(CHECKURL, "GET", params);
			try {
				success = json.getInt("success");
			} catch (JSONException e) {

				e.printStackTrace();
			}
			if (success == 1)
				return "success";

			return "";
		}

		@Override
		protected void onPostExecute(String result) {

			super.onPostExecute(result);
		}

	}

	@Override
	protected void onRestart() {

		super.onRestart();

	}

	@Override
	protected void onResume() {

		super.onResume();
		Log.e("Act", " Logger Activity on Resume");
	}

	class logRec extends BroadcastReceiver {

		@Override
		public void onReceive(Context cxt, Intent intent) {

			String action = intent.getAction();
			if (action.equals(Utilties.DISPLAY_CONVERSATION_ACTION)) {
				String msg = intent.getExtras().getString(
						Utilties.EXTRA_MESSAGE);
				String date = intent.getExtras().getString(Utilties.EXTRA_Date);
				Message m = new Message(msg, "R", date);
				Contact contact = new Contact("", intent.getExtras().getString(
						Utilties.CONTACT), "", "");
				Conversation c = new Conversation(contact, null);
				if (!Conversations.contains(c)) {
					showAskDialog(contact,m);
					
				} else {
					int index = Conversations.indexOf(c);
					Conversations.get(index).addMsg(m);
					db_mngr.addMsg(contact, m);
					ActivityManager.getInstance().updateLogger(Conversations);
					adapter.notifyDataSetChanged();
				}
				
			

			} else {

			}

		}

	}

	private boolean checkConnection() {
		ConnectionDetector cd = new ConnectionDetector(this);
		return cd.isConnectingToInternet();
	}

}