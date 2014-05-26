package com.example.mama;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {
	DBManager db_mngr;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null) {
			if (intent.getAction() != null) {
				String t = intent.getAction().toString();
				if (!t.equals(Utilties.DISMISS_NOTIFICATION_ACTION)) {
					Log.d("test", t);
					Bundle extras = intent.getExtras();
					db_mngr = new DBManager(context);

					if (t.compareTo("com.google.android.c2dm.intent.RECEIVE") == 0) {

						String nameFrom = extras.getString("sender");
						Message msg = new Message(extras.getString("price"),
								"R", Utilties.getexactTime());

						Contact from = new Contact("", nameFrom, "00", "mailee");
						/*Toast.makeText(
								context,
								extras.getString("price")
										+ Utilties.getexactTime(),
								Toast.LENGTH_LONG).show();*/
						String activeUsr = ActivityManager.getInstance()
								.activeUser();
						// checl of there any contact with this name if not add
						// it
						// to db
						// and
						// notifiy
						Cursor data = db_mngr.getMsgsByContact(from);
						if (ActivityManager.getInstance().getState(
								ActivityManager.CONV) == ActivityManager.ACTIVE) {

							if (activeUsr != null
									&& activeUsr.equals(from.getName())) {
								Utilties.displayMessage(context, msg.getText(),
										msg.getDate(), from.getName(),
										Utilties.DISPLAY_MESSAGE_ACTION);

							} else if (data.getCount() != 0) {
								db_mngr.addMsg(from, msg);
							} else {
								Utilties.displayMessage(context, msg.getText(),
										msg.getDate(), from.getName(),
										Utilties.DISPLAY_MESSAGE_ACTION);
								// showAskDialog(context, from.getName());

							}

						} else if (ActivityManager.getInstance().getState(
								ActivityManager.LOGGER) == ActivityManager.ACTIVE) {

							Utilties.displayMessage(context, msg.getText(),
									msg.getDate(), nameFrom,
									Utilties.DISPLAY_CONVERSATION_ACTION);
						} else {
							// when the app is closed
							if (db_mngr.hasConversation(new Conversation(from,
									null))) {
								Utilties.showNotification(context,
										from.getName(),
										extras.getString("price"));
								if (data.getCount() != 0) {
									// the message is from friend
									db_mngr.addMsg(from, msg);
								} else {
									Conversation con = new Conversation(from,
											null);
									if (!db_mngr.hasConversation(con)) {
										// showAskDialog(context,
										// from.getName());
										con.addMsg(msg);
										db_mngr.addConversation(con);
										db_mngr.addMsg(from, msg);
									} else {
										con = null;

										db_mngr.addMsg(from, msg);
									}
								}
							} else {
								Utilties.showAskNotification(context, from, msg);
							}
						}

					}
					if (t.compareTo("com.google.android.c2dm.intent.REGISTRATION") == 0) {
						String rId = intent.getExtras().getString(
								"registration_id");
						if (rId != null)
							Log.d("test", " registered :" + rId);
					}

				}
				else{
					Log.d("test", "hon 2e3mal dismiss ya 6rsh ");
					Bundle extras = intent.getExtras();
					if(extras!=null)
					{
						
						int n=extras.getInt(Utilties.EXTRA_NOTIFICATION);
						Log.d("test", " notify number "+n);
						 NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					        manager.cancel(n);
					}
				}
			}
		} else {

		}
	}
}
