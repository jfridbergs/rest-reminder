package com.colormindapps.rest_reminder_alarm;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;


public class PeriodManager {
	private Context mContext;
	private AlarmManager mAlarmManager;
	int buildNumber;
	public PeriodManager(Context context){
		mContext = context;
		mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	}
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public void setPeriod(int type, long when, int extendCount, boolean approxOnly){
		buildNumber = Build.VERSION.SDK_INT;

		PendingIntent pi,pa;

		if (RReminder.isApproxEnabled(mContext)){
			long approxCalendarInMillis = RReminder.getApproxTime(mContext, when);
			Intent iApprox = new Intent(mContext, OnAlarmReceiver.class);
			iApprox.putExtra(RReminder.PERIOD_TYPE, 99);
			iApprox.putExtra(RReminder.EXTEND_COUNT, 0);
			iApprox.setAction(RReminder.CUSTOM_INTENT_APPROXIMATE_PERIOD_END);
			pa = PendingIntent.getBroadcast(mContext, (int)approxCalendarInMillis, iApprox, PendingIntent.FLAG_ONE_SHOT);

			if(buildNumber >= Build.VERSION_CODES.LOLLIPOP){
				AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(approxCalendarInMillis, pa);
				mAlarmManager.setAlarmClock(alarmClockInfo,pa);
			} else if(buildNumber >= Build.VERSION_CODES.KITKAT){
				mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, approxCalendarInMillis, pa);
			} else {
				mAlarmManager.set(AlarmManager.RTC_WAKEUP, approxCalendarInMillis, pa);
			}
		}

		if(!approxOnly){
			Intent i = new Intent(mContext, OnAlarmReceiver.class);
			i.putExtra(RReminder.PERIOD_TYPE, type);
			i.putExtra(RReminder.EXTEND_COUNT, extendCount);
			i.setAction(RReminder.CUSTOM_INTENT_ALARM_PERIOD_END);
			pi = PendingIntent.getBroadcast(mContext, (int)when, i, PendingIntent.FLAG_ONE_SHOT);

			if(buildNumber >= Build.VERSION_CODES.LOLLIPOP){
				AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(when, pi);
				mAlarmManager.setAlarmClock(alarmClockInfo,pi);
			} else if(buildNumber >= Build.VERSION_CODES.KITKAT){
				mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, when, pi);
			} else {
				mAlarmManager.set(AlarmManager.RTC_WAKEUP, when, pi);
			}
		}



	}



}