package com.colormindapps.rest_reminder_alarm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;


public class OnAlarmReceiver extends WakefulBroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent){
		int type = intent.getExtras().getInt(RReminder.PERIOD_TYPE);
		int extendCount = intent.getExtras().getInt(RReminder.EXTEND_COUNT);
		Intent i = new Intent(context, PeriodService.class);
		i.putExtra(RReminder.PERIOD_TYPE, type);
		i.putExtra(RReminder.EXTEND_COUNT, extendCount);
		startWakefulService(context,i);

	}

}
