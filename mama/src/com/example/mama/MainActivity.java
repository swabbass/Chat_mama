package com.example.mama;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends Activity{

	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	static final String SENDER_ID = "537857651912";
	static final String TAG = "GCMDemo";
	GoogleCloudMessaging gcm;
	SharedPreferences shP;
	SharedPreferences.Editor ed = null;
	Context context;
	String regID = null;
	boolean isReg = false;
	Button btn;
	EditText name, phone;
	TextView status, log;
	String tname, tphone, temail;
	private EditText email;

	@Override
	protected void onStart() {
		Log.d("Act"," Main Activity on start");
		super.onStart();
		checkSharedPref();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initlizeGUI();
		Log.d("Act"," Main Activity on create");
		// checkSharedPref();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	void initlizeGUI() {
		btn = (Button) findViewById(R.id.regsiter);
		name = (EditText) findViewById(R.id.name);
		phone = (EditText) findViewById(R.id.phone);
		email = (EditText) findViewById(R.id.email);
		status = (TextView) findViewById(R.id.status);
		this.context = getApplicationContext();
		gcm = GoogleCloudMessaging.getInstance(this);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				tname = name.getText().toString();
				tphone = phone.getText().toString();
				temail = email.getText().toString();
				if(tname.isEmpty() || tphone.isEmpty() || temail.isEmpty() )
				{
					Toast.makeText(getApplicationContext(), "Fill All Fields Please", Toast.LENGTH_SHORT).show();
				}
				else  {
					if (!isReg)
						new task().execute(tname, tphone, temail);
					Log.i("test", regID);
				}
			}
		});

	}
	@Override
	protected void onDestroy() {		
		Log.d("Act"," Main Activity on destroy");
		super.onDestroy();
		
	}
	@Override
	protected void onResume() {
		Log.d("Act"," Main Activity on resume");
		super.onResume();
	}
	@Override
	protected void onStop() {
		Log.d("Act"," Main Activity on stop");
		super.onStop();
	}
	@Override
	protected void onPause() {
		Log.d("Act"," Main Activity on pause");
		super.onPause();
	}
	private void checkSharedPref() {
		shP = getSharedPreferences("data", Context.MODE_PRIVATE);
		if (shP != null) {
			regID = shP.getString("regID", "NULL");
			if (regID.compareTo("NULL") != 0) {
				Intent i = new Intent(context, Logger.class);				
				i.putExtra("name", shP.getString("myname", "NULL"));
				startActivity(i);
				finish();
			}

		}

		ed = shP.edit();
	}

	

	class task extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			String st = null;
			try {
				st = gcm.register(SENDER_ID);
			} catch (IOException e) {
				e.printStackTrace();
				Log.e("test", e.getMessage());
			}
			if (!st.isEmpty() && st != "null") {
				regID = st;
				Log.d("test", "success :" + st);
				Map<String, String> params1 = new HashMap<String, String>();
				params1.put("ID", st);
				params1.put("name", params[0]);
				params1.put("email", params[2]);
				params1.put("phone", params[1]);
				try {
					post("http://wabbass.byethost9.com/android/register.php",
							params1);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (!isReg) {

				if (regID.compareToIgnoreCase("NULL") != 0) {
					ed.putString("regID", regID);
					ed.putString("myname", tname);
					ed.commit();

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							
							Intent i = new Intent(context, Logger.class);				
							i.putExtra("name", shP.getString("myname", "NULL"));
							startActivity(i);
							finish();
						}
					});

				}
			}
		}

	}

	private void post(String endpoint, Map<String, String> params)
			throws IOException {

		URL url;
		try {
			url = new URL(endpoint);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid url: " + endpoint);
		}
		StringBuilder bodyBuilder = new StringBuilder();
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		// constructs the POST body using the parameters
		while (iterator.hasNext()) {
			Entry<String, String> param = iterator.next();
			bodyBuilder.append(param.getKey()).append('=')
					.append(param.getValue());
			if (iterator.hasNext()) {
				bodyBuilder.append('&');
			}
		}
		String body = bodyBuilder.toString();
		Log.v("test", "Posting '" + body + "' to " + url);
		byte[] bytes = body.getBytes();
		HttpURLConnection conn = null;
		try {
			Log.e("URL", "> " + url);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setFixedLengthStreamingMode(bytes.length);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded;charset=UTF-8");
			// post the request
			OutputStream out = conn.getOutputStream();
			out.write(bytes);
			out.close();
			// handle the response
			int status = conn.getResponseCode();
			if (status != 200) {
				throw new IOException("Post failed with error code " + status);
			}
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
}
