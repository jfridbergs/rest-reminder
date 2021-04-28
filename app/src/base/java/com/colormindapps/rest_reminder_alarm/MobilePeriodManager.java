package com.colormindapps.rest_reminder_alarm;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MobilePeriodManager {
	private Context mContext;
	private AlarmManager mAlarmManager;

	public MobilePeriodManager(Context context){
		mContext = context;
		mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	}
	@TargetApi(Build.VERSION_CODES.M)
	public void setPeriod(int type, long when, int extendCount){
		int buildNumber = Build.VERSION.SDK_INT;

		PendingIntent pi;

		Intent i = new Intent(mContext, MobileOnAlarmReceiver.class);
		i.putExtra(RReminder.PERIOD_TYPE, type);
		i.putExtra(RReminder.EXTEND_COUNT, extendCount);
		i.putExtra(RReminder.PERIOD_END_TIME, when);
		i.putExtra(RReminder.IS_SHORT_PERIOD, false);
		i.setAction(RReminder.ACTION_ALARM_PERIOD_END);
		Log.d("PERIOD_MANAGER", "New Alarm: type: "+type+", extendCount: "+extendCount+", endTime: "+when);
		SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
		Calendar endTime = Calendar.getInstance();
		endTime.setTimeInMillis(when);

		pi = PendingIntent.getBroadcast(mContext, (int)when, i, PendingIntent.FLAG_ONE_SHOT);
		if(buildNumber>=Build.VERSION_CODES.M){
			mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
		} else if(buildNumber >= Build.VERSION_CODES.LOLLIPOP){
			AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(when, pi);
			mAlarmManager.setAlarmClock(alarmClockInfo,pi);
		} else if(buildNumber >= Build.VERSION_CODES.KITKAT){
			mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, when, pi);
		} else {
			mAlarmManager.set(AlarmManager.RTC_WAKEUP, when, pi);
		}


	}





}