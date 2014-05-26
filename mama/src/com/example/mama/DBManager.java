package com.example.mama;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBManager {
	public static final String DB_NAME = "db_Conv";// הניתונים מסד שם
	public static final int DB_VER = 5;// הנתונים מס של הגרסה
	public static final String CONVERS_TABLE = "Conversatins";// הטבלה שם	
	private static final String _UID="id";	
	public static final String _ID = "RegKey";// )הראשי המפתח( יחודי מזהה
	public static final String _Contact = "Contact";// המשימה של הטקסט
	public static final String _PHONE = "phone";// הטלפון מספר
	public static final String _LASTMSG = "lastmsg";
	
	public static final String MSGS_TABLE = "msgs";
	public static final String _MSG = "msg";
	public static final String _TYPE = "type";
	public static final String _date= "date";
	private static final String SCRIPT_CREATE_DATABASE = "create table "
			+ CONVERS_TABLE + " ("+_UID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ _ID + " text not null, "	
			+ _Contact+ " text not null , " 
			+ _PHONE + " text not null , " 
			+ _LASTMSG+ " text not null" + ");";

	DB_HELPER dbHelper;
	Context cxt;
	
	static final String CREATE_TABLE="create table "
			+ MSGS_TABLE + " ("+_UID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
			
			+ _Contact+ " text not null , " 
			+ _MSG + " text not null , "
			+ _date + " text not null , " 
			+ _TYPE+ " text not null" + 
			");"
			;

	public DBManager(Context context) {
		this.cxt = context;
		this.dbHelper = new DB_HELPER(context, DB_NAME, null, DB_VER);
	}

	class DB_HELPER extends SQLiteOpenHelper {

		public DB_HELPER(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, null, version);
		
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			String sql,sql2 ;
			sql = String.format("drop table if exists %s;", CONVERS_TABLE);
			sql2 = String.format("drop table if exists %s;", MSGS_TABLE);
			db.execSQL(sql);
			db.execSQL(SCRIPT_CREATE_DATABASE);
			db.execSQL(sql2);
			db.execSQL(CREATE_TABLE);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
			onCreate(db);

		}

	}

	public void addConversation(Conversation con) {
		SQLiteDatabase ConvsDb = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();		
		values.put(_ID, con.getSender().getID());
		values.put(_Contact, con.getSender().getName());
		values.put(_PHONE, con.getSender().getNumber());
		values.put(_LASTMSG, con.getLastSaid());
		con.setId(ConvsDb.insert(CONVERS_TABLE, null, values));
		if(con.getId()<0)
		Log.d("DB", "db insert problem");
		ConvsDb.close();

	}
	public void addMsg(Contact sndr,Message m) {
		SQLiteDatabase ConvsDb = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();		
		values.put(_Contact, sndr.getName());
		values.put(_MSG, m.getText());
		values.put(_date, m.getDate());
		values.put(_TYPE, m.getType());
		Long id=ConvsDb.insert(MSGS_TABLE, null, values);
		if(id<0)
		Log.d("DB", "db insert problem");
		ConvsDb.close();

	}

	public boolean updateConvById(String convId, Conversation con) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(_Contact, con.getSender().getName());
		values.put(_LASTMSG, con.getLastSaid());
		values.put(_PHONE, con.getSender().getNumber());
		boolean b = database.update(DB_NAME, values, _ID + "=" + convId, null) > 0;
		database.close();
		return b;
	}

	public boolean deleteConvItem(Conversation conv) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		//getid returns long maybe change it to String
		
		boolean b = database.delete(CONVERS_TABLE, _Contact + "=" + conv.getSender().getSQLName(), null) > 0;
		database.close();
		return b;
	}
	public boolean deleteMsgItem(Message m) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		boolean b = database.delete(MSGS_TABLE, _date + "=" + m.getSQLDate(), null) > 0;
		database.close();
		return b;
	}

	public Cursor getCursorALL() {
		Cursor cursor;
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		cursor = database.query(CONVERS_TABLE, new String[] { _ID, _Contact, _PHONE,
				_LASTMSG }, null, null, null, null, null);
		return cursor;
	}
	public Cursor getCursorMsgsALL() {
		Cursor cursor;
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		cursor = database.query(MSGS_TABLE, new String[] {  _Contact, _MSG,
				_date,_TYPE }, null, null, null, null, null);
		return cursor;
	}

	public Cursor getMsgsByContact(Contact c)
	{
		Cursor cursor;
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		cursor = database.query(MSGS_TABLE, new String[] {  _Contact, _MSG,
				_date,_TYPE }, _Contact+" = '"+c.getName()+"' ", null, null, null, null);
		return cursor;
		
	}
	public Cursor getCursorTable(String Table) {
		Cursor cursor;
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		cursor = database.query(Table, new String[] {  _Contact, _MSG,
				_date,_TYPE }, null, null, null, null, null);
		return cursor;
	}
	// TODO enhance effectivity is o(n) better be O(1) by sql script
	public Conversation getTaskConvById(String taskId) {
		Cursor cursor;
		Conversation conv = null;
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		cursor = database.query(DB_NAME, new String[] { _ID, _Contact, _PHONE,
				_LASTMSG }, null, null, null, null, null);
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			String id = cursor.getString(0);
			String contact = cursor.getString(1);
			String phone = cursor.getString(2);
			String lastmsg = cursor.getString(3);
			conv = new Conversation(new Contact(id, contact, phone, null), null);
			conv.addMsg(new Message(lastmsg, "", ""));
		}
		database.close();
		return conv;
	}
	
	
	public boolean hasConversation(Conversation c)
	{
		Cursor data=getCursorALL();
		while(data.moveToNext())
		{
			String contact = data.getString(1);
			Conversation tmp=new Conversation(contact,null);
			if(tmp.equals(c))
			{
				return true;
			}
			tmp=null;
		}
		return false;
	}
	public boolean hasMessage(Message m)
	{
		Cursor data=getCursorMsgsALL();
		while(data.moveToNext())
		{
			String txt = data.getString(1);
			String date = data.getString(2);
			String type = data.getString(3);
			if(m.equals(new Message(txt, type, date)))
				return true;
		}
		return false;
	}

}