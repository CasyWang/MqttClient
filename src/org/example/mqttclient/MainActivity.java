/*
 *   MQTT app on Android
 *   Author: Oliver
 *   Description:
 *   这是MainActivity,可在AndroidManifest.xml中修改启动的Activity
*/
package org.example.mqttclient;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.internal.MemoryPersistence;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;

import org.example.mqttclient.R;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.content.BroadcastReceiver;

//继承基类BaseLeftFlow,用来生成左侧菜单栏
public class MainActivity extends BaseLeftFlow {

    private static final String STATE_CONTENT_TEXT = "org.example.mqttclient.MainActivity.contentText";
    public static final String SERVICE_CLASSNAME = "org.example.mqttclient.MqttService";
    private String mContentText;
    private TextView mContentTextView;  
         
    public static final String BROKER_URL = "tcp://test.mosquitto.org:1883";
    public static final String TOPIC = "test/led_on_sleep";

    private static MqttClient pubClient = null;
    private static final String mqttClientId = "cp01";
          
    private int cnt = 1;
    
    @Override
    protected void onCreate(Bundle inState) {
    	//解决新版本不能在主线程中使用网络的问题
    	StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
    	.detectDiskReads()
    	.detectDiskWrites()
    	.detectNetwork()     // or .detectAll() for all detectable problems
    	.penaltyLog()
    	.build());
    	StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
    	.detectLeakedSqlLiteObjects()
    	.detectLeakedClosableObjects()
    	.penaltyLog()
    	.penaltyDeath()
    	.build());
    	
        super.onCreate(inState);   
        if (inState != null) {
            mContentText = inState.getString(STATE_CONTENT_TEXT);
        }

        //设置HomePage页面
        mMenuDrawer.setContentView(R.layout.activity_windowsample);
        mMenuDrawer.setTouchMode(MenuDrawer.TOUCH_MODE_FULLSCREEN);
        mMenuDrawer.setSlideDrawable(R.drawable.ic_drawer);
        mMenuDrawer.setDrawerIndicatorEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mContentTextView = (TextView) findViewById(R.id.contentText);                 
        mContentTextView.setText(mContentText);
              
        mMenuDrawer.setOnInterceptMoveEventListener(new MenuDrawer.OnInterceptMoveEventListener() {
            @Override
            public boolean isViewDraggable(View v, int dx, int x, int y) {
                return v instanceof SeekBar;
            }
        });        
        //初始化控件
        InitSetupControl();
        //CreatePubClient();         
        errorHandler();
    }
	@Override
	public boolean onKeyDown(int kCode, KeyEvent kEvent) {
	    switch (kCode) {
	    case KeyEvent.KEYCODE_DPAD_LEFT: {
	        return true;
	    }

	    case KeyEvent.KEYCODE_DPAD_UP: {
	        return true;
	    }

	    case KeyEvent.KEYCODE_DPAD_RIGHT: {
	        return true;
	    }

	    case KeyEvent.KEYCODE_DPAD_DOWN: {
	        return true;
	    }
	    case KeyEvent.KEYCODE_DPAD_CENTER: {
	        return true;
	    }
	    case KeyEvent.KEYCODE_BACK: {
	        return false;
	    }
	    }
	    return super.onKeyDown(kCode, kEvent);
	}
    private void errorHandler(){
    	Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable ex) {
            //TODO
             
             Writer result = new StringWriter();         
             PrintWriter printWriter = new PrintWriter(result);         
             ex.printStackTrace(printWriter);         
             String stacktrace = result.toString(); 
             
             Log.d("TEMP1","_____"+ex.toString()+" "+stacktrace);                          
             finish();
            }
              
           });
    }
       
    private void startMqttService(String strCmd) {
        final Intent intent = new Intent(this, MqttService.class);           
        intent.putExtra("ledCmd", strCmd); 
        startService(intent);
    }

    private void stopMqttService() {
        final Intent intent = new Intent(this, MqttService.class);
        stopService(intent);
    }

    private boolean serviceIsRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SERVICE_CLASSNAME.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
            
    //初始化控件
    private void InitSetupControl(){     	    	    	
    	//Set connect Broker btn
    	final Button btn_connect = (Button)findViewById(R.id.btn_connect);  
    	btn_connect.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v) {  
    			startMqttService("");
    			if(serviceIsRunning()){
    				btn_connect.setTextColor(Color.GREEN);
        			new AlertDialog.Builder(MainActivity.this)
        			.setTitle("Success")
        			.setIcon(android.R.drawable.btn_star_big_on)
        			.setPositiveButton("OK", null)
        			.setMessage("Connected to the broker!")
        			.show();
    			}
    			else{
    				btn_connect.setTextColor(Color.RED);
    				new AlertDialog.Builder(MainActivity.this)
        			.setTitle("Failed")
        			.setIcon(android.R.drawable.btn_star_big_off)
        			.setPositiveButton("OK", null)
        			.setMessage("Fail to connect to the broker!")
        			.show();
    			}
    		}
    	});
 
    	//Set disconnect Broker btn
    	final Button btn_disconnect = (Button)findViewById(R.id.btn_disconnect);  
    	btn_disconnect.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v) {
    		    // do something when the button is clicked   
    			stopMqttService();
    			btn_connect.setTextColor(Color.GRAY);
				new AlertDialog.Builder(MainActivity.this)
    			.setTitle("Disconnected")
    			.setIcon(android.R.drawable.btn_star_big_off)
    			.setPositiveButton("OK", null)
    			.setMessage("Disconnect from the broker!")
    			.show();  
    		    }
    	});
    	    	    	   
    	//set usr name
    	EditText editTxt_usr = (EditText)findViewById(R.id.editTxt_usr);
    	EditText editTxt_pwr = (EditText)findViewById(R.id.editTxt_pwr);
    	EditText editTxt_qos = (EditText)findViewById(R.id.editTxt_qos);
    	EditText editTxt_broker = (EditText)findViewById(R.id.editTxt_broker);
    	
    	if(editTxt_broker.getText().toString().trim().length() !=0 ){
    		 
    	}    	    
    }
            
    private void InitHeartbeatPanel(){
    	GridView gvHeartbeat = (GridView)findViewById(R.id.gvHeartbeat);
    	ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();  
    	for(int i=0;i<10;i++)  
        {  
          HashMap<String, Object> map = new HashMap<String, Object>();  
          map.put("ItemImage", R.drawable.icon); 
          map.put("ItemText", "Node."+String.valueOf(i));  
          lstImageItem.add(map);  
        }  
         
        SimpleAdapter saImageItems = new SimpleAdapter(this,  
                                                   lstImageItem,    
                                                   R.layout.gridcell,                                                                                                                  
                                                   new String[] {"ItemImage","ItemText"},                                                                                                             
                                                   new int[] {R.id.ItemImage,R.id.ItemText});  
        
        gvHeartbeat.setAdapter(saImageItems);            
        gvHeartbeat.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				HashMap<String, Object> item=(HashMap<String, Object>) arg0.getItemAtPosition(arg2);
				
			}
        	
        });  
     }  
    
    
    
    @Override
    protected void onMenuItemClicked(int position, Item item) {
        mContentTextView.setText(item.mTitle);  
        //设置需要切换的子页面
        mMenuDrawer.setContentView(item.layoutRes);
        //此处需要设置完页面后,才能通过findViewById()找到该页面的控件
        if(item.mTitle == "Control"){
        	try{
        		//final应用于对象引用时，不能改变的是他的引用，而对象本身是可以修改的，这个引用已经不能指向别的对象
            	final ToggleButton toggleButton_led1 = (ToggleButton)findViewById(R.id.toggleButton_led1);
            	toggleButton_led1.setOnClickListener(new OnClickListener(){
            		@Override
            		public void onClick(View v){     
            			final String strState;
            			if(toggleButton_led1.isChecked()){
            				strState = "1";    //打开灯
            			}else{
            				strState = "0";    //关闭灯
            			}
            			String strCmd = "dio9" + "/" + strState;
            			startMqttService(strCmd);       
            		}
            	});
        	}
        	catch(Exception e){
        		e.printStackTrace();
        	}
        }
        else if(item.mTitle=="Setup"){
        	InitSetupControl();
        }
        else if(item.mTitle=="Sensor"){
        	getSupportFragmentManager().beginTransaction().replace(R.id.chart, new ChartFragment()).commit();                  
        }
        else if(item.mTitle=="Heartbeat"){
        	InitHeartbeatPanel();
        }
        mMenuDrawer.closeMenu();
    }

 
    
    @Override
    protected int getDragMode() {
        return MenuDrawer.MENU_DRAG_CONTENT;
    }

    @Override
    protected Position getDrawerPosition() {
        return Position.START;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_CONTENT_TEXT, mContentText);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mMenuDrawer.toggleMenu();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        final int drawerState = mMenuDrawer.getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            mMenuDrawer.closeMenu();
            return;
        }

        super.onBackPressed();
    }          
    
    //新添加回收资源函数,在此处需要关闭Mqtt Service以及解注册Receiver
    @Override  
    protected void onDestroy(){
    	 stopMqttService();    	  
    }
}
