package com.example.Radar;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.Radar.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
  private MapView mMapView = null;
  private BaiduMap mBaiduMap;
  private Button locateBtn;
  private Button friendsBtn;
  private Button addBtn;
  private LocationService locService;
  private Overlay me;
  private HashMap<String,String> firends=new HashMap<String,String>();
  private String firends_string;
  private Context context;
  private  Reciver receiver;
  private double[] nowLocation=null;
  boolean choice[];


  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    receiver = new Reciver();
    IntentFilter filter = new IntentFilter();
    filter.addAction("android.provider.Telephony.SMS_RECEIVED");
    filter.setPriority(800);
    registerReceiver(receiver, filter);
    receiver.ma=this;
    context=this;
    firends_string=load();
    setFriendsList(firends_string);
    mMapView = (MapView) findViewById(R.id.bmapView);
    locateBtn = (Button) findViewById(R.id.search);
    friendsBtn= (Button) findViewById(R.id.friendList);
    addBtn= (Button) findViewById(R.id.addfriends);
    mBaiduMap = mMapView.getMap();
    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
    mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(18));
    locService = ((LocationApplication) getApplication()).locationService;
    LocationClientOption mOption = locService.getDefaultLocationClientOption();
    locService.setLocationOption(mOption);
    locService.registerListener(listener);
    
    locateBtn.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          // TODO Auto-generated method stub
        	if(firends.size()==0){
        		Toast.makeText(context, "朋友列表为空", Toast.LENGTH_LONG).show();
        	}else{
        		locateFriends();
        	}
          if (mBaiduMap != null){
            mBaiduMap.clear();
            markSelf();
          }
        }
      });
    friendsBtn.setOnClickListener(new OnClickListener(){
		public void onClick(View arg0) {
			
			String s[]=firends_string.split(",");
			
			choice=new boolean[s.length];
			for (int i=0; i<choice.length;i++) {
				choice[i]=false;
			}
			if(s==null||s[0].equals(""))
				Toast.makeText(context, "朋友列表为空", Toast.LENGTH_LONG).show();
			else
				new AlertDialog.Builder(context)  
				.setTitle("朋友列表")  
				.setMultiChoiceItems(s, null, new OnMultiChoiceClickListener(){
					public void onClick(DialogInterface arg0, int i, boolean b) {
						choice[i]=b;
					}
				})
				.setNegativeButton("取消", null)    
				.setPositiveButton("删除", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						String s1=null;
						String s[]=firends_string.split(",");
						for (int i=0; i<choice.length;i++) {
							if(choice[i]==false){
								if(s1==null)
									s1=s[i];
								else 
									s1+=","+s[i];
							}
						}
						UpdateFriendsList(s1);
					}
				}).show();  
		}
    });
    addBtn.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          // TODO Auto-generated method stub
          final EditText tv=new EditText(context);
          new AlertDialog.Builder(context)  
          .setTitle("添加好友(名字:号码)")  
          .setIcon(android.R.drawable.ic_dialog_info)  
          .setView(tv)  
          .setPositiveButton("添加", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
              // TODO Auto-generated method stub
	            String s0=tv.getText().toString();
	            if(s0==null||s0.equals("")) return;
	            String s[]=s0.split(":");
	           if(s==null||s.length!=2) 
	        	   Toast.makeText(context, "格式错误", Toast.LENGTH_LONG).show();
	           else{
	        	   if(firends_string==null||firends_string.equals(""))
	        		   firends_string=s0;
	        	   else
	        		   firends_string+=","+s0;
	        	   UpdateFriendsList(firends_string);
	           	}
            }
          })  
          .setNegativeButton("取消", null)  
          .show();  
        }
      });

  }
  @Override
  protected void onStart(){
    super.onStart();
    locService.start();
   
  }
  public void sendLocation(String phone){
	  if(nowLocation!=null){
	      sendms(phone,"返回坐标:"+Double.toString(nowLocation[0])+","+Double.toString(nowLocation[1]));
	  }
    return ;
  }
  public void locateFriends(){
	  for (String key : firends.keySet()) {
		  sendms(key,"请求坐标");
		}
	  
  }
  public Overlay  setTextOverlay( LatLng point,String name) {
    TextOptions textOptions=new TextOptions().bgColor(0xAAFFFFFF).fontSize(30) .fontColor(0xFF000000).text(name).position(point);
    Overlay o=mBaiduMap.addOverlay(textOptions);
    return o;
  }
  public Overlay setIcomOverlay(LatLng point,int id) {
	  BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(id);
	 OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
	 return mBaiduMap.addOverlay(option);
}

  public boolean markFriend(double Latitude,double Longitude,String phone){
		 String s=firends.get(phone);
		 if(s!=null){
			 LatLng point = new LatLng(Latitude,Longitude);
			 setTextOverlay(point,s);
			 setIcomOverlay(point,R.drawable.friend);
			 return true;
		 }
		 return false;
  }
  public void markSelf(){
	  if(nowLocation==null) return;
	  LatLng point = new LatLng(nowLocation[0],nowLocation[1]);
	  if(me!=null)
		  me.remove();
	  me=setIcomOverlay(point,R.drawable.self);
	  mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(point));
  }

  BDLocationListener listener = new BDLocationListener() {
            
    @Override
    public void onReceiveLocation(BDLocation location) {
      // TODO Auto-generated method stub
      if (location != null && ( location.getLocType() != BDLocation.TypeServerError)) {
        if (location != null) {
          if(nowLocation==null)
        	  nowLocation=new double[2];
	          nowLocation[0]=location.getLatitude();
	          nowLocation[1]=location.getLongitude();
	          markSelf();
        }
      }
    }
  };
    private void sendms(String phone, String message){
    	if(phone==null||phone.equals("")) return;
    	if(message==null||message.equals("")) return;
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phone, null, message, null, null);
    }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    locService.unregisterListener(listener);
    locService.stop();
    mMapView.onDestroy();
    unregisterReceiver(receiver);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapView.onPause();
  }
  
private void setFriendsList(String s0){
	  if(s0==null)return ;
	  int i;
	  String friendstrs[]=s0.split(",");
	  String fs;
	  for(i=friendstrs.length-1;i>=0;i--){
		  fs=friendstrs[i];
			if(fs==null||fs.equals("")) continue;
		  	String s[]=fs.split(":");
		  	if(s==null||s.length!=2||s[0]==null||s[0].equals("")||s[1]==null||s[1].equals("")) continue;
			 firends.put(s[1],s[0]);
	  }
}

  private void  UpdateFriendsList(String fs){
	if(fs==null)
		fs="";
    firends_string=fs;
    firends.clear();
    setFriendsList(firends_string);
    save(firends_string);
  }

  public String load()
  {
	 
	  String content = null;  
      try {  
          FileInputStream fis = openFileInput("friendsList");  
          ByteArrayOutputStream baos = new ByteArrayOutputStream();  
          byte[] buffer = new byte[1024];  
          int len = 0;  
          while ((len = fis.read(buffer)) != -1) {  
              baos.write(buffer, 0, len);  
          }  
          content = baos.toString();  
          fis.close();  
          baos.close();  
      } catch (Exception e) {  
          e.printStackTrace();  
      }  
      return content;  
  }  
  
  public  boolean save(String content)
  {
         try {
        	 FileOutputStream outStream=openFileOutput("friendsList", Context.MODE_PRIVATE);
             String string=content;
             outStream.write(string.getBytes());
             outStream.close();
             return true;
         } catch (FileNotFoundException e) {
             return false;
         }catch (IOException e){
             return false;
         } 
  }
}
