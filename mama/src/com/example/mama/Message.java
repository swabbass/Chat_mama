package com.example.mama;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class Message {
	private String text;
	private String date;
	private String type;
	private int _UID;
	SimpleDateFormat sDf;

	@Override
	public boolean equals(Object o) {
		//
		Message m = (Message) o;
		return this.date.equals(m.getDate()) && this.type.equals(m.getType());
	}

	public Message(String txt, String type, String date) {
		setDate(date);
		setText(txt);
		setType(type);
		set_UID(-1);
		this.sDf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	}

	public Message(String txt, String type, String date, int _UID) {
		setDate(date);
		setText(txt);
		setType(type);
		set_UID(_UID);
		this.sDf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	}

	public String getDate() {
		return date;
	}

	public String getSQLDate() {
		char c = 34;
		Character ch = Character.valueOf(c);

		Log.d("rcvr", (ch.toString() + date + ch.toString()));
		return (ch.toString() + date + ch.toString());
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int get_UID() {
		return _UID;
	}

	public String getTime() {
		Date d = null;
		try {
			d = sDf.parse(getDate());
		} catch (ParseException e) {

			e.printStackTrace();
		}
		return new String(d.getHours() + ":" + d.getMinutes() + ":"
				+ d.getSeconds());
	}

	public void set_UID(int _UID) {
		if (_UID < 0)
			this._UID = -1;
		else
			this._UID = _UID;
	}

}
