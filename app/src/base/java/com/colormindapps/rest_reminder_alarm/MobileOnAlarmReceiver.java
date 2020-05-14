package com.colormindapps.rest_reminder_alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.Calendar;


public class MobileOnAlarmReceiver extends BroadcastReceiver {
    int type, nextType;
    long mCalendar, currentTime;

	@Override
	public void onReceive(Context context, Intent intent){
	    currentTime = Calendar.getInstance().getTimeInMillis();
		type = intent.getExtras().getInt(RReminder.PERIOD_TYPE);
		Intent playIntent = new Intent(context, PlaySoundService.class);
		playIntent.putExtra(RReminder.PERIOD_TYPE,type);
		playIntent.putExtra(RReminder.PERIOD_END_TIME,currentTime);
		context.startService(playIntent);
		nextType = RReminder.getNextPeriodType(type);
		mCalendar = RReminder.getNextPeriodEndTime(context, nextType, currentTime, 1, 0L);


		if(RReminder.getMode(context) == 0){
			new MobilePeriodManager(context.getApplicationContext()).setPeriod(nextType, mCalendar, 0);
			RReminderMobile.startCounterService(context.getApplicationContext(), nextType, 0, mCalendar, true);
		}

		MobilePeriodService.enqueueWork(context, intent);

	}

}
