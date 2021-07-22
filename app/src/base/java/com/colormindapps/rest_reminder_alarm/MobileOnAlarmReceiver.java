package com.colormindapps.rest_reminder_alarm;

import android.content.Context;
import android.content.Intent;

import com.colormindapps.rest_reminder_alarm.shared.OnAlarmReceiver;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;


public class MobileOnAlarmReceiver extends OnAlarmReceiver {

	public MobileOnAlarmReceiver(){

	}


	@Override
	public void enqueueWork(Context context, Intent intent){
		MobilePeriodService.enqueueWork(context, intent);
	}

	@Override
	public void playSound(Context context, int type, long currentTime){
		Intent playIntent = new Intent(context, PlaySoundService.class);
		playIntent.putExtra(RReminder.PERIOD_TYPE,type);
		playIntent.putExtra(RReminder.PERIOD_END_TIME,currentTime);
		context.startService(playIntent);
	}

	@Override
	public void startNextPeriod(Context context, int nextType, long calendar){
		new MobilePeriodManager(context.getApplicationContext()).setPeriod(nextType, calendar, 0);
		RReminderMobile.startCounterService(context.getApplicationContext(), nextType, 0, calendar, true, true);
	}

}
