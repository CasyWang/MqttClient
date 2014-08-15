package org.example.mqttclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.view.KeyEvent;
public class LoginActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginpage);
        
        Button button_login = (Button) findViewById(R.id.button_login);
        button_login.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);	
        	}
        	                               
        });
        
    }

}
