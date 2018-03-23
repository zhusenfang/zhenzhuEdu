package com.xboot.stdcall;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DataforHandle {
	
	private static final String TAG = DataforHandle.class.getName();

    String ontime="",offtime="",state="";
    public void setonoff(String[] data){
    	if(data==null){
    		System.out.println("kong");
    	}
    	else{
        	if(data.length==3){
        		ontime=data[0];
        		offtime=data[1];
        		state=data[2];
        		try {
					if(Integer.parseInt(state)==0){
						Log.i(TAG, "DataforHandle --- stop");
						setPowerOnOff((byte)0, (byte)4, (byte)0, (byte)4, (byte)0);
					}else{
						Log.i(TAG, "DataforHandle --- start");
						judge(ontime,offtime,state);
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.i(TAG, "DataforHandle --- State values are not for the digital");
				}
        	}else{
        		Log.i(TAG, "DataforHandle --- Amount of data is wrong");
        	}
    	}	
    }
    
    public void judge(String on_time,String off_time,String _state){
    	if(num(on_time)&&num(off_time)&&num(_state)){
    		settings(
    				Integer.parseInt(nowtime()[0]),
    				Integer.parseInt(nowtime()[1]),
    				Integer.parseInt(on_time.split(":")[0]),
    				Integer.parseInt(on_time.split(":")[1]),
    				Integer.parseInt(off_time.split(":")[0]),
    				Integer.parseInt(off_time.split(":")[1])
    				);
    	}
    	else{
    		Log.i(TAG, "DataforHandle --- Presentation Error ");
    	}
    }
    public String[] nowtime(){
    	String NOW = (new SimpleDateFormat("yyyy-MM-dd kk:mm")).format(Calendar.getInstance().getTime());
    	String nowtime[] =NOW.split(" ")[1].split(":");
    	return nowtime;
    }
    
    public boolean num(String thisnum){
    	String [] num =thisnum.split(":");
    	try {
    		Log.i(TAG,""+Integer.parseInt(num[0]));
    		if(num.length>1){
    			Log.i(TAG, ""+Integer.parseInt(num[1]));
    		}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
    	return true;
    }
    public void settings(int now_hour,int now_min,int on_hour,int on_min,int off_hour,int off_min){
    	now_hour=now_hour==24?0:now_hour;    	

    	 if(off_hour==on_hour&&off_min==on_min){
    		 Log.i(TAG, "DataforHandle --- failed to set datafor ");
    	} 	
    	
    	else{
    		boolean byte_off_m = off_min-now_min<0;
    		boolean byte_on_m=on_min-off_min<0;

    		int off_h=
    				(off_hour-now_hour<0?
    					(byte_off_m?(off_hour-now_hour+24-1):(off_hour-now_hour+24))
    					:		
    					(byte_off_m?(off_hour-now_hour-1):(off_hour-now_hour))
    					);
    		int off_m=
    			  (byte_off_m?(off_min-now_min+60):(off_min-now_min));

    		int on_h=(byte_on_m?(on_hour-off_hour-1):(on_hour-off_hour));
    		int on_m=(byte_on_m?(on_min-off_min+60):(on_min-off_min));
    		off_h=off_h<0?(off_h+24):off_h;
    		on_h=on_h<0?(on_h+24):on_h;
    		String NOW = (new SimpleDateFormat("yyyy-MM-dd kk:mm")).format(Calendar.getInstance().getTime());
    		Log.i(TAG, "---------------------------"+NOW);
    		Log.i(TAG, "For the set of parameters=="+on_h+"==="+on_m+"==="+off_h+"==="+off_m);
    		Log.i(TAG, "--------------------------- ");  
    		

    			if(on_h==0&&on_m<3||off_h==0&&off_m<3){//С��3�������ò��ɹ�
    				Log.i(TAG, "DataforHandle --- stop Time is too short to 3 minutes");
    			}else{
    				setPowerOnOff((byte)on_h, (byte)on_m, (byte)off_h, (byte)off_m, (byte)3);
    			}

    		
    	}
    }
    
    int setPowerOnOff(byte off_h, byte off_m, byte on_h, byte on_m, byte enable) {
   		int fd, ret;
   		// byte buf[] = { 0, 3, 0, 3 };
   		fd = posix.open("/dev/McuCom", posix.O_RDWR, 0666);
   		ret = posix.poweronoff(off_h, off_m, on_h, on_m, enable, fd);
   		posix.close(fd);
   		return 0;
   	}
}
