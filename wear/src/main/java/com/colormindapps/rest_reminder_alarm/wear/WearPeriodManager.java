package com.colormindapps.rest_reminder_alarm.wear;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.colormindapps.rest_reminder_alarm.shared.PeriodManager;

/**
 * Created by ingressus on 13/01/2017.
 */

public class WearPeriodManager extends PeriodManager {
    private Context mContext;
    private AlarmManager mAlarmManager;
    private PendingIntent pi;
    private String debug = "WEAR_PERIOD_MANAGER";
    public WearPeriodManager(Context context){
        mContext = context;
        mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }

    public void setPeriod(int type, long when, int extendCount){
        Log.d(debug,"setting up alarm");
        Intent i = new Intent(mContext, WearOnAlarmReceiver.class);
        pi = super.createPendingIntent(mContext,i,type,when,extendCount);
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(when, pi);
        mAlarmManager.setAlarmClock(alarmClockInfo,pi);

    }
}
