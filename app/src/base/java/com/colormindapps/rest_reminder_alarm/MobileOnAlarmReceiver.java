package com.colormindapps.rest_reminder_alarm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;


public class MobileOnAlarmReceiver extends WakefulBroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent){
		int type = 0, extendCount = 0;
		if(intent.getExtras()!=null){
			type = intent.getExtras().getInt(RReminder.PERIOD_TYPE);
			extendCount = intent.getExtras().getInt(RReminder.EXTEND_COUNT);
		}

		Intent i = new Intent(context, MobilePeriodService.class);
		i.putExtra(RReminder.PERIOD_TYPE, type);
		i.putExtra(RReminder.EXTEND_COUNT, extendCount);
		startWakefulService(context,i);

	}

}
