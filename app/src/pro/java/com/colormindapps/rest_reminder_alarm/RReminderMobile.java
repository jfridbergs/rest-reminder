package com.colormindapps.rest_reminder_alarm;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

public class RReminderMobile {


	@SuppressLint("UnspecifiedImmutableFlag")
	public static void cancelCounterAlarm(Context context, int type, int extendCount, long endTime){
		AlarmManager mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pi;



		Intent i = new Intent(context, MobileOnAlarmReceiver.class);
		i.putExtra(RReminder.PERIOD_TYPE, type);
		i.putExtra(RReminder.EXTEND_COUNT, extendCount);
		i.setAction(RReminder.ACTION_ALARM_PERIOD_END);
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
			pi = PendingIntent.getBroadcast(context, (int)endTime, i, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
		} else {
			pi = PendingIntent.getBroadcast(context, (int)endTime, i, PendingIntent.FLAG_ONE_SHOT);
		}

		if(mAlarmManager!=null)
			mAlarmManager.cancel(pi);
		pi.cancel();
	}

	public static void stopCounterService(Context context, int type){
		Intent i = new Intent(context, CounterService.class);
		i.putExtra(RReminder.PERIOD_TYPE,type);
		context.stopService(i);
	}

	public static void startCounterService(Context context, int type, int extendCount, long periodEndTime, boolean excludeOngoing ){
		Intent i = new Intent(context, CounterService.class);
		i.putExtra(RReminder.PERIOD_TYPE,type);
		i.putExtra(RReminder.PERIOD_END_TIME, periodEndTime);
		i.putExtra(RReminder.EXTEND_COUNT, extendCount);
		i.putExtra(RReminder.EXCLUDE_ONGOING, excludeOngoing);
		context.startService(i);
	}

	public static boolean isCounterServiceRunning(Context context) {

		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		if(manager != null){
			if(manager.getRunningServices(Integer.MAX_VALUE)!=null){
				for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
					if (CounterService.class.getName().equals(service.service.getClassName())) {
						return true;
					}
				}
			}

		}
		return false;
	}

	@SuppressLint("UnspecifiedImmutableFlag")
	public static Notification updateOnGoingNotification(Context context, int type, long periodEndTime, boolean showTurnOff){
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, RReminder.CHANNEL_ONGOING_ID);
		Intent notificationIntent = new Intent(context, MainActivity.class);
		notificationIntent.setAction(RReminder.ACTION_VIEW_MAIN_ACTIVITY);
		notificationIntent.putExtra(RReminder.START_COUNTER, false);
		notificationIntent.putExtra(RReminder.PERIOD_TYPE, type);
		PendingIntent pi;
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
			pi = PendingIntent.getActivity(context, 15 , notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		} else {
			pi = PendingIntent.getActivity(context, 15 , notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		}



		if (showTurnOff) {
			Intent turnOffIntent = new Intent (context, MainActivity.class);
			turnOffIntent.setAction(RReminder.ACTION_TURN_OFF);
			turnOffIntent.putExtra(RReminder.TURN_OFF, 1);
			turnOffIntent.putExtra(RReminder.PERIOD_END_TIME,periodEndTime);
			PendingIntent pIntentTurnOff;
			if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
				pIntentTurnOff = PendingIntent.getActivity(context, 0, turnOffIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
			} else {
				pIntentTurnOff = PendingIntent.getActivity(context, 0, turnOffIntent, PendingIntent.FLAG_ONE_SHOT);
			}

			builder.addAction(R.drawable.ic_notify_turn_off , context.getString(R.string.notify_turn_off), pIntentTurnOff);
		}
		//note = new Notification(android.R.drawable.stat_notify_sync, null, System.currentTimeMillis());
		builder.setContentTitle(context.getString(R.string.notify_reminder_is_on_title));
		builder.setPriority(Notification.PRIORITY_MAX);
		switch(type){
			case RReminder.WORK: case RReminder.WORK_EXTENDED: {
				builder.setContentText(context.getString(R.string.notify_reminder_is_on_work_message));
				builder.setSmallIcon(R.drawable.ic_notify_work_period);
				break;
			}
			case RReminder.REST: case RReminder.REST_EXTENDED:  {
				builder.setContentText(context.getString(R.string.notify_reminder_is_on_rest_message));
				builder.setSmallIcon(R.drawable.ic_notify_rest_period);
				break;
			}
			default:
				builder.setContentText(context.getString(com.colormindapps.rest_reminder_alarm.shared.R.string.notify_reminder_is_on_work_message));
				builder.setSmallIcon(R.drawable.ic_notify_work_period);
				break;
		}
		builder.setContentIntent(pi);
		builder.setOngoing(true);
		builder.setAutoCancel(false);
		return builder.build();
	}
}
