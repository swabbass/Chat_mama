package com.example.mama;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class DialogConverActivity extends Activity implements OnClickListener{

	EditText ed;
	Button add,cancel;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dialog_conver);
		ed=(EditText)findViewById(R.id.ContactAdd);
		add=(Button)findViewById(R.id.addok);
		cancel=(Button)findViewById(R.id.Cancel);
		add.setOnClickListener(this);
		cancel.setOnClickListener(this);
		
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.addok:
			
			if(!ed.getText().toString().isEmpty()){
				Intent returnIntent=new Intent();
				returnIntent.putExtra("contactname",ed.getText().toString() );
				setResult(RESULT_OK	, returnIntent);
				finish();
			}
			
			break;

		case R.id.Cancel:
			Intent returnIntent=new Intent();
			setResult(RESULT_CANCELED	, returnIntent);
			finish();
			break;
		}
		
	}

	

}
