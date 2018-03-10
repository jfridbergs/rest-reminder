package com.colormindapps.rest_reminder_alarm.wear;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.colormindapps.rest_reminder_alarm.shared.OnAlarmReceiver;


public class WearOnAlarmReceiver extends OnAlarmReceiver {
	private String debug = "WEAR_ONALARM_RECEIVER";

	public WearOnAlarmReceiver(){

	}

	@Override
	public Intent getAlarmServiceIntent(Context context){
		Log.d(debug, "setting intent destination");
		return new Intent(context,WearPeriodService.class);
	}

}
