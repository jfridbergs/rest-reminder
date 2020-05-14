package com.colormindapps.rest_reminder_alarm.shared;


import android.content.Context;
import android.os.CountDownTimer;
import androidx.core.content.ContextCompat;
import android.widget.TextView;



public class MyCountDownTimer extends CountDownTimer {
	TextView hour1, minute1, second1, hour2, minute2, second2, colon, point;
	Context context;
	boolean initiated = false;
	public boolean isRunning = false;
	private boolean isAmbient;
	int timeInSeconds,seconds, minutes, hours, timeInMinutes;
	int colorBlack, colorInactive, colorWhite;
	int secondsValue1 = 0;
	int minutesValue1 = 0;
	int hoursValue1 = 0;
	int secondsValue2, minutesValue2, hoursValue2;
	public MyCountDownTimer(Context context, long range, long tick, TextView hour1, TextView hour2, TextView colon,  TextView minute1, TextView minute2, TextView point, TextView second1, TextView second2, boolean isAmbient){
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
		this.isAmbient = isAmbient;
		this.colorBlack = ContextCompat.getColor(context, R.color.black);
		this.colorInactive = ContextCompat.getColor(context, R.color.inactive_digit);
		this.colorWhite = ContextCompat.getColor(context,R.color.white);

		if(this.isAmbient){
			this.hour1.setTextColor(colorWhite);
			this.hour2.setTextColor(colorWhite);
			this.minute1.setTextColor(colorWhite);
			this.minute2.setTextColor(colorWhite);
			this.colon.setTextColor(colorWhite);
		} else {
			this.hour1.setTextColor(colorBlack);
			this.hour2.setTextColor(colorBlack);
			this.minute1.setTextColor(colorBlack);
			this.minute2.setTextColor(colorBlack);
			this.colon.setTextColor(colorBlack);
			this.point.setTextColor(colorBlack);
		}

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
				 secondsValue1 = seconds/10;
	    	 }

			secondsValue2 = seconds % 10;
	    	
	    	 
	    	 if(!initiated || (minutes == 59 & seconds == 59)){		    		 
		    	 if(hours == 0){
					 if(this.isAmbient){
						 hour1.setTextColor(colorBlack);
						 hour2.setTextColor(colorBlack);
					 } else {
						 hour1.setTextColor(colorInactive);
						 hour2.setTextColor(colorInactive);
					 }

		    		 
		    	 }

		    	 
		    	 if (hours > 8){
		    		 hoursValue1 = hours/10;
		    	 } 
		    	 
		    	 hoursValue2 = hours % 10;

				 if(hoursValue1==0 && this.isAmbient){
					 hour1.setTextColor(colorBlack);
				 }
		    	 
		    	 hour1.setText(Integer.toString(hoursValue1));
		    	 hour2.setText(Integer.toString(hoursValue2));
	    	 }

	    	 if(!initiated || seconds == 59){
		    	 if(hours == 0 && minutes == 0){
					 if(!this.isAmbient){
						 minute1.setTextColor(colorInactive);
						 minute2.setTextColor(colorInactive);
						 colon.setTextColor(colorInactive);
					 }

		    	 }
		    	 
		    	 if (minutes > 8){
		    		 minutesValue1 = minutes/10;
		    	 } 
		    	 
		    	 minutesValue2 = minutes % 10;
		    	 
	    		 minute1.setText(Integer.toString(minutesValue1));
	    		 minute2.setText(Integer.toString(minutesValue2));
	    	 }
			if(!this.isAmbient){
				second1.setText(Integer.toString(secondsValue1));
				second2.setText(Integer.toString(secondsValue2));
			}

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
