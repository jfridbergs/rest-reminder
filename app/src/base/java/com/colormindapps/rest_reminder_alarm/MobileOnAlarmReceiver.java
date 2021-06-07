package com.colormindapps.rest_reminder_alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.Calendar;
import java.util.Objects;


public class MobileOnAlarmReceiver extends BroadcastReceiver {
    int type, nextType;
    long nextPeriodEndTime, currentTime;

	@Override
	public void onReceive(Context context, Intent intent){
	    currentTime = Calendar.getInstance().getTimeInMillis();
		type = Objects.requireNonNull(intent.getExtras()).getInt(RReminder.PERIOD_TYPE);
		Intent playIntent = new Intent(context, PlaySoundService.class);
		playIntent.putExtra(RReminder.PERIOD_TYPE,type);
		playIntent.putExtra(RReminder.PERIOD_END_TIME,currentTime);
		nextType = RReminder.getNextPeriodType(type);
		nextPeriodEndTime = RReminder.getNextPeriodEndTime(context, nextType, currentTime, 1, 0L);
		intent.putExtra(RReminder.NEXT_PERIOD_END_TIME, nextPeriodEndTime);
		context.startService(playIntent);


		if(RReminder.getMode(context) == 0){
			new MobilePeriodManager(context.getApplicationContext()).setPeriod(nextType, nextPeriodEndTime, 0);
			RReminderMobile.startCounterService(context.getApplicationContext(), nextType, 0, nextPeriodEndTime, true);
		}

		MobilePeriodService.enqueueWork(context, intent);

	}

}
