package com.colormindapps.rest_reminder_alarm;


import android.content.Context;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;



public class MyCountDownTimer extends CountDownTimer {
	TextView hour1, minute1, second1, hour2, minute2, second2, colon, point;
	Context context;
	boolean initiated = false;
	public boolean isRunning = false;
	int timeInSeconds,seconds, minutes, hours, timeInMinutes;
	int colorBlack, colorInactive;
	String secondsText1 = "0";
	String minutesText1 = "0";
	String hoursText1 = "0";
	String secondsText2, minutesText2, hoursText2;
	public MyCountDownTimer(Context context, long range, long tick, TextView hour1, TextView hour2, TextView colon,  TextView minute1, TextView minute2, TextView point, TextView second1, TextView second2){
		super(range,tick);
		this.hour1 = hour1;
		this.minute1 = minute1;
		this.second1 = second1;
		this.hour2 = hour2;
		this.minute2 = minute2;
		this.second2 = second2;
		this.colon = colon;
		this.point = point;
		this.context = context;
		this.colorBlack = ContextCompat.getColor(context, R.color.black);
		this.colorInactive = ContextCompat.getColor(context, R.color.inactive_digit);
		
		this.hour1.setTextColor(colorBlack);
		this.hour2.setTextColor(colorBlack);
		this.minute1.setTextColor(colorBlack);
		this.minute2.setTextColor(colorBlack);
		this.colon.setTextColor(colorBlack);
		this.point.setTextColor(colorBlack);
	}
	
	@Override
	public void onTick(long millisUntilFinished){
			isRunning = true;
	    	 timeInSeconds =  Math.round(millisUntilFinished / 1000);
	    	 seconds = timeInSeconds % 60;
	    	 timeInMinutes  = timeInSeconds / 60;
	    	 minutes = timeInMinutes % 60;
	    	 hours = timeInMinutes / 60;
	    	 
	    	 if (seconds > 8){	    	 
	    		 secondsText1 = ""+(seconds/10);	    	 
	    	 }
	    	 
	    		 secondsText2 = ""+(seconds % 10);
	    	
	    	 
	    	 if(!initiated || (minutes == 59 & seconds == 59)){		    		 
		    	 if(hours == 0){
		    		 hour1.setTextColor(colorInactive);
		    		 hour2.setTextColor(colorInactive);
		    		 
		    	 }
		    	 
		    	 if (hours > 8){
		    		 hoursText1 = ""+(hours/10);
		    	 } 
		    	 
		    	 hoursText2 = ""+(hours % 10);
		    	 
		    	 hour1.setText(hoursText1);
		    	 hour2.setText(hoursText2); 
	    	 }

	    	 if(!initiated || seconds == 59){
		    	 if(hours == 0 && minutes == 0){
		    		 minute1.setTextColor(colorInactive);
		    		 minute2.setTextColor(colorInactive);
		    		 colon.setTextColor(colorInactive);
		    	 }
		    	 
		    	 if (minutes > 8){
		    		 minutesText1 = ""+(minutes/10);
		    	 } 
		    	 
		    	 minutesText2 = ""+(minutes % 10);
		    	 
	    		 minute1.setText(minutesText1);
	    		 minute2.setText(minutesText2);
	    	 }
	    	 second1.setText(secondsText1);
	    	 second2.setText(secondsText2);
	    	 initiated = true;

		
	}
	
	@Override
    public void onFinish() {
		isRunning = false;
   	 hour1.setText("0");
   	 hour2.setText("0");
   	 minute1.setText("0");
   	 minute2.setText("0");
   	 second1.setText("0");
   	 second2.setText("0");
   	 initiated = false;


    }



}
