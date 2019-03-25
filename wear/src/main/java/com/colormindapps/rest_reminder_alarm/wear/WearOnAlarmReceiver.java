package com.colormindapps.rest_reminder_alarm.wear;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.colormindapps.rest_reminder_alarm.shared.OnAlarmReceiver;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;


public class WearOnAlarmReceiver extends OnAlarmReceiver {
	private String debug = "WEAR_ONALARM_RECEIVER";

	public WearOnAlarmReceiver(){

	}

	@Override
	public void enqueueWork(Context context, Intent intent){
		WearPeriodService.enqueueWork(context, intent);
	}

	@Override
	public void playSound(Context context, int type, long currentTime){
	}

	@Override
	public void startNextPeriod(Context context, int nextType, long calendar){
		new WearPeriodManager(context.getApplicationContext()).setPeriod(nextType, calendar, 0);
	}

}
