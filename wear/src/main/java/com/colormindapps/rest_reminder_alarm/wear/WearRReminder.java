package com.colormindapps.rest_reminder_alarm.wear;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

/**
 * Created by ingressus on 20/01/2017.
 */

public class WearRReminder {

    public static void cancelPeriodAlarm(Context context, int type, int extendCount, long endTime){
        AlarmManager mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi;
        Intent i = new Intent(context, WearOnAlarmReceiver.class);
        i.putExtra(RReminder.PERIOD_TYPE, type);
        i.putExtra(RReminder.EXTEND_COUNT, extendCount);
        i.setAction(RReminder.ACTION_ALARM_PERIOD_END);
        pi = PendingIntent.getBroadcast(context, (int)endTime, i, PendingIntent.FLAG_ONE_SHOT);
        mAlarmManager.cancel(pi);
        pi.cancel();
    }
}
