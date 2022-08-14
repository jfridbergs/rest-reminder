package com.colormindapps.rest_reminder_alarm;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.colormindapps.rest_reminder_alarm.shared.PeriodManager;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.Calendar;


public class MobilePeriodManager extends PeriodManager {
	int buildNumber;
	private final Context mContext;
	private final AlarmManager mAlarmManager;

	public MobilePeriodManager(Context context){
		mContext = context;
		mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	}
	@TargetApi(Build.VERSION_CODES.M)
	public void setPeriod(int type, long when, int extendCount){
		buildNumber = Build.VERSION.SDK_INT;

		PendingIntent pi;

		Intent i = new Intent(mContext, MobileOnAlarmReceiver.class);
		i.putExtra(RReminder.PERIOD_TYPE, type);
		i.putExtra(RReminder.EXTEND_COUNT, extendCount);
		i.putExtra(RReminder.PERIOD_END_TIME, when);
		i.putExtra(RReminder.IS_SHORT_PERIOD, false);
		i.setAction(RReminder.ACTION_ALARM_PERIOD_END);
		Calendar endTime = Calendar.getInstance();
		endTime.setTimeInMillis(when);

		if(buildNumber>=Build.VERSION_CODES.M){
			pi = PendingIntent.getBroadcast(mContext, (int)when, i, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
		} else {
			pi = PendingIntent.getBroadcast(mContext, (int)when, i, PendingIntent.FLAG_ONE_SHOT);
		}
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



