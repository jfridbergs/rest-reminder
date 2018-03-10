package com.colormindapps.rest_reminder_alarm.shared;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;


public abstract class OnAlarmReceiver extends WakefulBroadcastReceiver {
	private String debug = "ON_ALARM_RECEIVER";

	public abstract Intent getAlarmServiceIntent(Context context);

	@Override
	public void onReceive(Context context, Intent intent){
		int type = intent.getExtras().getInt(RReminder.PERIOD_TYPE);
		int extendCount = intent.getExtras().getInt(RReminder.EXTEND_COUNT);
		long periodEndTimeValue = intent.getExtras().getLong(RReminder.PERIOD_END_TIME);
		Intent i = getAlarmServiceIntent(context);
		i.putExtra(RReminder.PERIOD_TYPE, type);
		i.putExtra(RReminder.EXTEND_COUNT, extendCount);
		i.putExtra(RReminder. PERIOD_END_TIME, periodEndTimeValue);
		startWakefulService(context,i);

	}

}
