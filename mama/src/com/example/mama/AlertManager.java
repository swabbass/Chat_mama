package com.example.mama;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AlertManager {
	public void showAlertDialog(Context context, String title, String message,
			Boolean status) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title)
				.setMessage(message)
				.setCancelable(false)
				.setNegativeButton("Close",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
