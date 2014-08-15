package org.example.mqttclient;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.internal.MemoryPersistence;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class MqttService extends Service {
	//public variables
	public static final String BROKER_URL = "tcp://test.mosquitto.org:1883";
    public static final String clientId = "android-client";
    public static final String CONST_TOPIC = "/const_topic/gw_yaml";    //this is the constant topic
    public static final String GwListeningTopic ="/const_topic/listening";
    public MqttClient mqttClient;
    //private variables
    private Map tpMap;
    private String DefaultLedTopic = "/zigbee/dio/dio9";
    private String OfflineAlertTopic = "/zigbee/offline/id_pan2mqtt";
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onCreate(){
    	StartClientThread();
    }
    
    private void StartClientThread(){
    	tpMap = new HashMap(); 
    	tpMap.put("topic_pool", CONST_TOPIC);
    	//will be execute only once
    	//service in android is running on main thread,so creating a new 
    	//thread is the best way to avoid blocking 
    	new Thread(new Runnable() {  
            @Override  
            public void run() {  
                //background task
            	try{
            		mqttClient = new MqttClient(BROKER_URL, clientId, new MemoryPersistence());
            		mqttClient.setCallback(new MqttCallback(){
            			@Override
            			public void messageArrived(MqttTopic topic, MqttMessage message) throws Exception{
            				
            				String strTopic = topic.getName();
            				 
            				//if here comes the constant topic message
            				if(strTopic.equals(tpMap.get("topic_pool").toString())){
            					//parse it, format: topic1@topic2@topic3 
            					String raw_topic = new String(message.getPayload());
            					String[] arrTopic = raw_topic.split("@");
            					for(int i = 0; i < arrTopic.length; i++){
            						if(arrTopic[i].indexOf("dio9") > 0){
            							tpMap.put("dio9", arrTopic[i]);
            						}
            						else if(arrTopic[i].indexOf("a3") > 0){
            							tpMap.put("a3", arrTopic[i]);            							
            						}
            						mqttClient.subscribe(arrTopic[i]);            						
            					}
            					Alert("gateway is starting");
            				}
            				else{
            					//other topic
            					MqttMessageHandler(strTopic, message); 
            				}
            			}
            			@Override
            			public void connectionLost(Throwable cause){
            				
            			}
            			@Override
            			public void deliveryComplete(MqttDeliveryToken token){
            				
            			}
            		});
            		//connect to broker and subscribe topic_pool
            		mqttClient.connect();       //blocking method
            		mqttClient.subscribe(tpMap.get("topic_pool").toString());
            		mqttClient.subscribe(OfflineAlertTopic);
            		//query topic list from gateway
            		MqttTopic mqttTopic = mqttClient.getTopic(GwListeningTopic); 
            		final MqttMessage mqttMessage = new MqttMessage(tpMap.get("topic_pool").toString().getBytes());
            		mqttTopic.publish(mqttMessage);
            	}
            	catch(Exception e){
            		Toast.makeText(getApplicationContext(), "Something went wrong!" 
            	                  + e.getMessage(), Toast.LENGTH_LONG).show();
            		e.printStackTrace();
            	}
            }  
        }).start();
    }
    
    //will be called every time when service is starting
    @Override 
    public int onStartCommand(Intent intent, int flags, int startId){	
    	
		//get extra data and do some extra job
		String strCmd = intent.getStringExtra("ledCmd");
		SendMqttCmd(strCmd);  	    		
    	   	
    	super.onStartCommand(intent, flags, startId);
    	return START_REDELIVER_INTENT;  //when this service is killed, it will restart automatically
    }
    
    //first call this function
    @Override    
    public void onStart(Intent intent, int startId) {
        System.out.print("null");
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        try {
            mqttClient.disconnect(0);
        } catch (MqttException e) {
            Toast.makeText(getApplicationContext(), "Something went wrong!" + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private void SendMqttCmd(String strCmd){
    	try{
    		//verify at first
    		if(strCmd.isEmpty()) return;
    		
    		String[] cmdIns = strCmd.split("/");
    		if(cmdIns.length<2) return;
    		
    		String strTopic = DefaultLedTopic;
    		if(tpMap.get(cmdIns[0]) != null){
    			strTopic = tpMap.get(cmdIns[0]).toString();    //update dynamic topic
    		}
    		MqttTopic mqttTopic = mqttClient.getTopic(strTopic);
    		final MqttMessage mqttMessage = new MqttMessage(cmdIns[1].getBytes());
            try{
            	mqttTopic.publish(mqttMessage);
            } catch (MqttPersistenceException e){
            	e.printStackTrace();
            } catch (MqttException e) {
            	e.printStackTrace();
            }    			     	     		   	
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    private void MqttMessageHandler(String topic, MqttMessage message) throws MqttException{
    	if(topic.equals(tpMap.get("a3"))){
        	Intent intent = new Intent("sensor_event");
      	    // add data
      	    try{
          	    String strAd = new String(message.getPayload());
          	    Double val = Double.parseDouble(strAd);      	  
          	    intent.putExtra("adc", val);
          	    this.sendBroadcast(intent);
      	    }
      	    catch(Exception e){
      		    e.printStackTrace();
      	    } 
    	}
    	else if(topic.equals(OfflineAlertTopic)){
    		//alert node offline
    		String strMac = new String(message.getPayload());    		
            Alert("Node: " + strMac + " is offline");
    	}
    }
    
    private void Alert(String message){
        final NotificationManager notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        final Notification notification = new Notification(R.drawable.snow,
                "Cloud Message!", System.currentTimeMillis());

        // Hide the notification after its selected
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        final Intent intent = new Intent(this, MainActivity.class);
        final PendingIntent activity = PendingIntent.getActivity(this, 0, intent, 0);
        notification.setLatestEventInfo(this, "MqttBroker Message:", message, activity);
        notification.number += 1;
        notificationManager.notify(0, notification);
    }
}
