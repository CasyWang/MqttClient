package org.example.mqttclient;
 
import java.util.HashMap;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import org.example.mqttclient.R;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class PushCallback implements MqttCallback {

    private ContextWrapper context;
    private Map mMap;

    public PushCallback(ContextWrapper context) {
        this.context = context;
        this.mMap = new HashMap();
        mMap.put("topic_holder", "/config_file_holder/gw_yaml/id_pan2mqtt");        
    }

    @Override
    public void connectionLost(Throwable cause) {
        //We should reconnect here
    }
    
    @Override
    public void messageArrived(MqttTopic topic, MqttMessage message) throws Exception {
    	//if here comes the constant topic message    	 
    	if(topic.getName().equals(mMap.get("topic_holder"))){
    		//parse it, format: topic1@topic2@topic3     		
    		String raw_topic = new String(message.getPayload());
    		String[] arrTopic = raw_topic.split("@");  
    	    
    	    
    	}
    	else{
    		sendMessage(message);
    	}
    	
    	//先向MainActivity发送数据消息包
    	//sendMessage(message);
    	
        final NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        final Notification notification = new Notification(R.drawable.snow,
                "Cloud Message!", System.currentTimeMillis());

        // Hide the notification after its selected
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        final Intent intent = new Intent(context, MainActivity.class);
        final PendingIntent activity = PendingIntent.getActivity(context, 0, intent, 0);
        notification.setLatestEventInfo(context, "MqttBroker Message:", "JN5168 onchip temperature : " +
                new String(message.getPayload()) + " Degree", activity);
        notification.number += 1;
        notificationManager.notify(0, notification);
    }

    @Override
    public void deliveryComplete(MqttDeliveryToken token) {
        //We do not need this because we do not publish
    	return;
    }
    
    private void sendMessage(MqttMessage message) {
    	  Intent intent = new Intent("sensor_event");
    	  // add data
    	  try{
        	  String strAd = new String(message.getPayload());
        	  Double val = Double.parseDouble(strAd);
        	  //int ad_val = Integer.parseInt(strAd);
        	  intent.putExtra("adc", val);
        	  context.sendBroadcast(intent);
    	  }
    	  catch(Exception e){
    		  e.printStackTrace();
    	  }    	  
    	} 
}

