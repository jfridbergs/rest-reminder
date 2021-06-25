package com.colormindapps.rest_reminder_alarm.shared;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;


public abstract class OnAlarmReceiver extends BroadcastReceiver {
	private String debug = "ON_ALARM_RECEIVER";
	private long currentTime, mCalendar;
	private int nextType;

	public abstract void enqueueWork(Context context, Intent intent);
	public abstract void playSound(Context context, int type, long currentTime);
	public abstract void startNextPeriod(Context context, int type, long calendar);

	@Override
	public void onReceive(Context context, Intent intent){

		currentTime = Calendar.getInstance().getTimeInMillis();
		int type = intent.getExtras().getInt(RReminder.PERIOD_TYPE);
		playSound(context, type, currentTime);




		nextType = RReminder.getNextPeriodType(type);
		mCalendar = RReminder.getNextPeriodEndTime(context.getApplicationContext(), nextType, currentTime, 1, 0L);
		intent.putExtra(RReminder.NEXT_PERIOD_END_TIME, mCalendar);

		if(RReminder.getMode(context.getApplicationContext()) == 0){
			startNextPeriod(context.getApplicationContext(), nextType, mCalendar);
		}

		enqueueWork(context,intent);

	}


}
