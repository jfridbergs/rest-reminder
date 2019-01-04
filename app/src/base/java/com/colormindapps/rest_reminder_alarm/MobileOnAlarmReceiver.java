package com.colormindapps.rest_reminder_alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;


public class MobileOnAlarmReceiver extends BroadcastReceiver {
    String debug = "RREMINDER_ON_ALARM_RECEIVER";
	@Override
	public void onReceive(Context context, Intent intent){
	    Log.d(debug, "was called");
		MobilePeriodService.enqueueWork(context, intent);

	}

}
