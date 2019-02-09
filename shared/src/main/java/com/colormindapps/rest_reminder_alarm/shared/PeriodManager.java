package com.colormindapps.rest_reminder_alarm.shared;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;


public class PeriodManager {



	public PendingIntent createPendingIntent(Context context, Intent intent, @RReminder.PeriodType int type, long when, int extendCount){
		PendingIntent pi;

		intent.putExtra(RReminder.PERIOD_TYPE,type);
		intent.putExtra(RReminder.EXTEND_COUNT, extendCount);
		intent.putExtra(RReminder.PERIOD_END_TIME, when);
		intent.setAction(RReminder.ACTION_ALARM_PERIOD_END);

		pi = PendingIntent.getBroadcast(context, (int)when,intent,PendingIntent.FLAG_ONE_SHOT);
		return pi;
	}



}