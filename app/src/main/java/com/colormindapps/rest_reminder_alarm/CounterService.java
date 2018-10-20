package com.colormindapps.rest_reminder_alarm;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.Calendar;



public class CounterService extends Service {
	private Calendar startTime;
	private long periodEndTime;
	private long periodLength;
	private int type;
	private int extendCount;
	SharedPreferences preferences;
	SharedPreferences.Editor editor;

	/* for testing purposes */
	public boolean created = false;
	public boolean started = false;
	public int onStartCommandCount = 0;
	public int onCreateCount = 0;


	private final IBinder mBinder = new CounterBinder();
	public class CounterBinder extends Binder{
		CounterService getService(){
			return CounterService.this;
		}
	}
	
	
	

	@Override
	public void onCreate(){
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		created = true;
		onCreateCount++;
		//when testing service, comment out the foreground service part



	}

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		boolean excludeOngoing = false;
		started = true;
		onStartCommandCount++;
		editor   = preferences.edit();
		editor.putBoolean(RReminder.COUNTERSERVICE_STATUS, true);
		editor.apply();
		startTime = Calendar.getInstance();
		if(intent.getExtras()!=null){
			type = intent.getExtras().getInt(RReminder.PERIOD_TYPE);
			extendCount = intent.getExtras().getInt(RReminder.EXTEND_COUNT);
			periodEndTime = intent.getExtras().getLong(RReminder.PERIOD_END_TIME);
			excludeOngoing = intent.getExtras().getBoolean(RReminder.EXCLUDE_ONGOING);
		}
		periodLength = periodEndTime - startTime.getTimeInMillis();

		if(RReminder.isActiveModeNotificationEnabled(this) && !excludeOngoing){
			NotificationCompat.Builder builder = new NotificationCompat.Builder(this, RReminder.CHANNEL_ONGOING_ID);
			Intent notificationIntent = new Intent(this, MainActivity.class);
			notificationIntent.setAction(RReminder.ACTION_VIEW_MAIN_ACTIVITY);
			notificationIntent.putExtra(RReminder.START_COUNTER, false);
			PendingIntent pi = PendingIntent.getActivity(this, 15, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				Intent turnOffIntent = new Intent (this, MainActivity.class);
				turnOffIntent.setAction(RReminder.ACTION_TURN_OFF);
				turnOffIntent.putExtra(RReminder.TURN_OFF, 1);
				turnOffIntent.putExtra(RReminder.PERIOD_END_TIME,periodEndTime);
				PendingIntent pIntentTurnOff = PendingIntent.getActivity(this, 0, turnOffIntent, PendingIntent.FLAG_ONE_SHOT);
				builder.addAction(R.drawable.ic_notify_turn_off , getString(R.string.notify_turn_off), pIntentTurnOff);
				builder.setPriority(Notification.PRIORITY_MAX);
			}
			//note = new Notification(android.R.drawable.stat_notify_sync, null, System.currentTimeMillis());
			builder.setSmallIcon(R.drawable.ic_notify_work_period);
			builder.setContentTitle(this.getString(R.string.notify_reminder_is_on_title));
			if(type ==RReminder.WORK || type == RReminder.WORK_EXTENDED){
				builder.setContentText(this.getString(R.string.notify_reminder_is_on_work_message));
				builder.setSmallIcon(R.drawable.ic_notify_work_period);
			} else {
				builder.setContentText(this.getString(R.string.notify_reminder_is_on_rest_message));
				builder.setSmallIcon(R.drawable.ic_notify_rest_period);
			}

			//note.setLatestEventInfo(context, context.getString(R.string.notify_scheduler_is_on_title), context.getString(R.string.notify_scheduler_is_on_message), pi);
			//note.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
			builder.setContentIntent(pi);
			builder.setOngoing(true);
			builder.setAutoCancel(false);
			int id = 1;
			startForeground(id,builder.build());
		}

		return START_REDELIVER_INTENT;
	}
	
	@Override
	public IBinder onBind(Intent intent){
		return mBinder;
	}
	
	public long getCurrentMillis(){
		Calendar requestedTime = Calendar.getInstance();
		return (requestedTime.getTimeInMillis() - startTime.getTimeInMillis());		
	}


	
	public long getCounterTimeValue(){
		return periodLength - getCurrentMillis();
	}
	
	public Bundle getData(){
		Bundle data = new Bundle();
		data.putInt(RReminder.PERIOD_TYPE, type);
		data.putInt(RReminder.EXTEND_COUNT, extendCount);
		data.putLong(RReminder.PERIOD_END_TIME, periodEndTime);
		data.putLong(RReminder.COUNTER_TIME_VALUE, getCounterTimeValue());
		return data;
	}
	
	
	
	@Override
	public void onDestroy(){
		editor.putBoolean(RReminder.COUNTERSERVICE_STATUS, false);
		editor.commit();
		Context context = getBaseContext();
		//when testing service, comment out the foreground service part

		 if(RReminder.isActiveModeNotificationEnabled(context)){
			 stopForeground(true);
		 }

		created = false;
		started = false;
	    super.onDestroy(); 
	   
	}
	


}
