package com.example.Radar;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;



public class Reciver extends BroadcastReceiver {  
	
	public MainActivity ma=null;
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] sM = null;
        Object[] pdus = null;
        if (bundle != null) {
            pdus = (Object[]) bundle.get("pdus");
        }
        if (pdus !=null){
            sM = new SmsMessage[pdus.length];
            String phone = null;
            String message = null;
            String loc_s[];
            String s[];

            for (int i=0; i<pdus.length; i++){
                sM[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                phone = sM[i].getOriginatingAddress();
                message = sM[i].getMessageBody();

                if(message.indexOf("返回坐标:")==0){
                	if(phone.indexOf('+')==0)
                		phone=phone.substring(3);
                	s=message.split(":");
                	if(s!=null&&s.length==2){
	                	loc_s=s[1].split(",");
	                	if(loc_s!=null&&loc_s.length==2){
	                		if(ma!=null)
	                			ma .markFriend(Double.valueOf(loc_s[0]),Double.valueOf(loc_s[1]),phone);
	                	}
                	}
                }else if(message.indexOf("请求坐标")==0){
                	if(ma!=null)
                		ma.sendLocation(phone);
                }
            }
        }
    }
}