package com.example.mama;

import java.sql.SQLDataException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class Utilties {

	static final String sendUrl = "http://wabbass.byethost9.com/android/send_from_to.php";

	static final String DISPLAY_MESSAGE_ACTION = "com.example.mama.DISPLAY_MESSAGE";
	static final String ADD_CONVERSATION_ACTION = "com.example.mama.ADD_CONVERSATION";
	static final String DISPLAY_CONVERSATION_ACTION = "com.example.mama.DISPLAY_CONVERSATION";
	static final String DISMISS_NOTIFICATION_ACTION = "com.example.mama.DISMISS_NOTIFICATION";
	static final String EXTRA_MESSAGE = "message";
	static final String EXTRA_Date = "date";
	static final String EXTRA_NOTIFICATION = "NOTIFICATION";
	private  static SimpleDateFormat sDf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");
	static final String CONTACT = "contact";
	static final int notfyId = 717;
	private static String msg = "";

	/*
	 * sends msgs fort covActivity 
	 */
	static void displayMessage(Context context, String message, String time,
			String username, String Action) {
		Intent intent = new Intent(Action);
		intent.putExtra(EXTRA_MESSAGE, message);
		intent.putExtra(EXTRA_Date, time);
		intent.putExtra(CONTACT, username);
		context.sendBroadcast(intent);
	}
	/*
	 * sends messages for logger BR 
	 */
	static void displayMessage(Context context, String message,
			String username, String Action) {
		Intent intent = new Intent(Action);
		intent.putExtra(EXTRA_MESSAGE, message);
		intent.putExtra(CONTACT, username);

		context.sendBroadcast(intent);
	}
	/*
	 * show notification  when message arrived when the application is closed 
	 * when notfication clicked open the conversation 
	 */
	static void showNotification(Context context, String title, String text) {
		msg = text;
		// Notification notification=new Notification(R.drawable.success,
		// tickerText, when)
		NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.success)
				.setContentTitle(title).setContentText(msg).setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_ALL)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
		// PendingMsgs.add(msg);
		Intent i = new Intent(context, ConvActivity.class);
		String myname = context.getSharedPreferences("data",
				Context.MODE_PRIVATE).getString("myname", "NULL");
		i.putExtra("Newadding", false);
		i.putExtra("myname", myname);
		i.putExtra(EXTRA_MESSAGE, "text");
		i.putExtra("contactName", title);
		// inorder to return to home if back pressed
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(Logger.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(i);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		nBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(notfyId, nBuilder.build());
	}

	/*
	 * get the exact time by the format given
	 */
	public static String getexactTime() {

		Date cal = Calendar.getInstance().getTime();
		return sDf.format(cal);
	}

	/*
	 * set the date format
	 */
	public static void setsDf(SimpleDateFormat sDf) {
		Utilties.sDf = sDf;
	}

	/*
	 * TODO add buttons that dissmes and deal with the events showing ask
	 * notification for the foriegn users that want to talk the this user and
	 * implemments button to refuse (dissmes) and accept
	 */
	static void showAskNotification(Context context, Contact c,Message m) {
		msg = m.getText();

		// Notification notification=new Notification(R.drawable.success,
		// tickerText, when)
		NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.vcard).setContentTitle(m.getText())
				.setContentText(msg).setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_ALL)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
		// PendingMsgs.add(msg);
		
		Intent i = new Intent(context, ConvActivity.class);
	//	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		String myname = context.getSharedPreferences("data",
				Context.MODE_PRIVATE).getString("myname", "NULL");
		i.putExtra("Newadding", true);
		i.putExtra("myname", myname);
		i.putExtra(EXTRA_MESSAGE, m.getText());
		i.putExtra(EXTRA_Date, m.getDate());
		i.putExtra("regID", c.getID());
		i.putExtra("contactName", c.getName());
		i.putExtra(EXTRA_NOTIFICATION, notfyId);
		// inorder to return to home if back pressed
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(ConvActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack

		stackBuilder.addNextIntent(i);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		Intent dis=new Intent(context, MyReceiver.class);
		dis.setAction(Utilties.DISMISS_NOTIFICATION_ACTION);
		dis.putExtras(i.getExtras());
		PendingIntent dismiss=PendingIntent.getBroadcast(context, 0, dis, 0);
	//	nBuilder.setContentIntent(resultPendingIntent);
		// nBuilder.addAction(R.drawable.success, title, resultPendingIntent);
		nBuilder.addAction(R.drawable.success, "Add", resultPendingIntent);
	
		
		nBuilder.addAction(R.drawable.fail, "Refuse", dismiss);
		nBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		
		mNotificationManager.notify(notfyId, nBuilder.build());
	}

	
	/*
	 * used in send message to send data to url using GET method with the values
	 * simplified by names and tags by nameValuePair
	 */
	private static void sendData(String msg, String receiver,
			String senderName, String URL) throws JSONException {
		JSONParser jParser = new JSONParser();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("receiver", receiver));
		params.add(new BasicNameValuePair("sender", senderName));
		params.add(new BasicNameValuePair("message", msg));
		JSONObject json = jParser.makeHttpRequest(URL, "GET", params);
		int success = json.getInt("success");
		if (success != 1) {
			Log.d("server", "error sending ");
		} else {
			Log.d("server", "success sending ");
			;
		}

	}

	/*
	 * Asyncktask that sends the message to destination
	 */
	private static class sendmsgTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... args) {

			try {
				// msg,reciever ,sendername,URl
				sendData(args[0], args[1], args[2], args[3]);
			} catch (JSONException e) {

				e.printStackTrace();
			}

			return null;
		}

	}

	/*
	 * send the message by asyncktask to distenation
	 */
	public static void sendMsg(String... args) {
		new sendmsgTask().execute(args[0], args[1], args[2], args[3]);
	}

	/*
	 * load the messages from db to list return it
	 */
	public static List<Message> loadMsgsFromDB(String name, DBManager db_mngr) {
		List<Message> msgs = new ArrayList<Message>();
		Cursor data = db_mngr.getMsgsByContact(new Contact("", name, "", ""));
		while (data.moveToNext()) {

			String txt = data.getString(1);
			String date = data.getString(2);
			String type = data.getString(3);
			Message message = new Message(txt, type, date);
			msgs.add(message);
		}
		return msgs;
	}

	/*
	 * if there are deleted msg on stop or on pause will make changes on db and
	 * delete what supposed to be deleted
	 */
	private static void updateDB(List<Message> Trash, DBManager db_mngr)
			throws SQLDataException {
		if (!Trash.isEmpty()) {
			for (Message tmp : Trash) {
				if (!db_mngr.deleteMsgItem(tmp))
					throw new SQLDataException(
							"Go Fuck Ur Self Cannot Delete This Shit");
			}
		}
		Trash.clear();
	}

	/*
	 * save messages to data base and update the db for changes by list (trash)
	 * and flag if there any changes
	 */
	public static void saveMsgs(int isChanged, List<Message> msgs,
			List<Message> trash, String name, DBManager db_mngr) {
		if (isChanged != -1) {
			// start pushing from the index
			for (int i = isChanged; i < msgs.size(); i++) {
				Contact c = new Contact("", name, "", "");
				db_mngr.addMsg(c, msgs.get(i));

			}
			isChanged = -1;
		}

		try {
			updateDB(trash, db_mngr);
		} catch (SQLDataException e) {

			e.printStackTrace();
		}
	}

}
