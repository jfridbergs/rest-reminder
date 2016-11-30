package com.colormindapps.rest_reminder_alarm;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Calendar;


public class PeriodService extends IntentService {
	long mCalendar;



	public PeriodService() {
		super("PeriodService");
	}
	
	
	
	@Override
	protected void onHandleIntent(Intent intent){
		
		AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
	
		

		
		int type = intent.getExtras().getInt(RReminder.PERIOD_TYPE);
		int extendCount = intent.getExtras().getInt(RReminder.EXTEND_COUNT);
		
		Intent playIntent = new Intent(this, PlaySoundService.class);


		
		if (type==99){
				if(RReminder.isApproxEnabled(getApplicationContext())){
					playIntent.putExtra(RReminder.PERIOD_TYPE,type);
					this.startService(playIntent);
				}
	        
		} else {
			
			RReminder.addDismissDialogFlag(this);
			String notificationMessage;

			
			int typeForNotification = type;
		
			switch (type){
				case 1:  type = 2;  break;
				case 3:  type = 2;  break;
				case 2:  type = 1;  break;
				case 4:  type = 1;  break;
				default: break;
			}
			playIntent.putExtra(RReminder.PERIOD_TYPE, type);
			this.startService(playIntent);
			mCalendar = RReminder.getNextPeriodEndTime(this, type, Calendar.getInstance().getTimeInMillis(), 1, 0L);

			NotificationManagerCompat mgr = NotificationManagerCompat.from(getApplicationContext());
			mgr.cancel(24);
				
				

							
			//PowerManager pm = (PowerManager)
			//getSystemService(Context.POWER_SERVICE);
			//boolean isScreenOn = pm.isScreenOn();
			if(RReminder.isActiveModeNotificationEnabled(this)){
				mgr.notify(1, RReminder.updateOnGoingNotification(this, type, mCalendar, true));
			}
			if(MainActivity.getVisibleState() || NotificationActivity.getVisibleState() || RReminder.getMode(this) == 1){
				/*
				Intent playIntent = new Intent(context, PlaySoundService.class);
				playIntent.putExtra(Scheduler.PERIOD_TYPE,typeForNotification);
				context.startService(playIntent);
				*/

				//Set the next period end alarms and start service, if any automatical mode is selected
				if(RReminder.getMode(this) != 1){
					new PeriodManager(getApplicationContext()).setPeriod(type, mCalendar, 0, false);
					RReminder.startCounterService(this, type, 0, mCalendar, false);
				}

				Intent actionIntent = new Intent(this, NotificationActivity.class);
				actionIntent.putExtra(RReminder.PERIOD_TYPE, typeForNotification);
				actionIntent.putExtra(RReminder.PERIOD_END_TIME, mCalendar);
				actionIntent.putExtra(RReminder.EXTEND_COUNT, extendCount);
				actionIntent.putExtra(RReminder.PLAY_SOUND, true);
				actionIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
				actionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				getApplication().startActivity(actionIntent);
			} else {
				if(RReminder.getMode(this) != 1){
					new PeriodManager(getApplicationContext()).setPeriod(type, mCalendar, 0, false);
					RReminder.startCounterService(this, type, 0, mCalendar, true);
				}

				//building android wear-only action for extending previously ended period
				//the action intent
				Intent extendIntent = new Intent(this,MainActivity.class);
				extendIntent.setAction(RReminder.CUSTOM_INTENT_WEAR_EXTEND_PERIOD);
				extendIntent.putExtra(RReminder.PERIOD_TYPE,type);
				extendIntent.putExtra(RReminder.EXTENDED_PERIOD_TYPE, typeForNotification);
				extendIntent.putExtra(RReminder.PERIOD_END_TIME,mCalendar);
				extendIntent.putExtra(RReminder.EXTEND_COUNT,extendCount);
				PendingIntent extendPendingIntent = PendingIntent.getActivity(this,5,extendIntent,PendingIntent.FLAG_ONE_SHOT);

				//action itself
				NotificationCompat.Action extendAction = new NotificationCompat.Action.Builder(R.drawable.ic_notify_wear_extend, getString(R.string.notify_extend),extendPendingIntent)
						.build();

				NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					if(RReminder.getMode(this)==1){
						Intent startNextPeriod = new Intent (this, MainActivity.class);
						startNextPeriod.setAction(RReminder.CUSTOM_INTENT_MANUAL_START_NEXT_PERIOD);
						startNextPeriod.putExtra(RReminder.MANUAL_MODE_NEXT_PERIOD_TYPE, type);
						PendingIntent pIntentStartNextPeriod = PendingIntent.getActivity(this, 0, startNextPeriod, PendingIntent.FLAG_ONE_SHOT);
						builder.addAction(android.R.drawable.stat_notify_sync , getString(R.string.notify_turn_off), pIntentStartNextPeriod);
					}
					Intent turnOffIntent = new Intent (this, MainActivity.class);
					turnOffIntent.setAction(RReminder.CUSTOM_INTENT_TURN_OFF);
					turnOffIntent.putExtra(RReminder.TURN_OFF, 1);
					PendingIntent pIntentTurnOff = PendingIntent.getActivity(this, 0, turnOffIntent, PendingIntent.FLAG_ONE_SHOT);
					builder.addAction(R.drawable.ic_notify_turn_off , getString(R.string.notify_turn_off), pIntentTurnOff);
				} 
				Intent notificationIntent = new Intent(this, NotificationActivity.class);
				notificationIntent.putExtra(RReminder.PERIOD_TYPE, typeForNotification);
				notificationIntent.putExtra(RReminder.PERIOD_END_TIME, mCalendar);
				notificationIntent.putExtra(RReminder.EXTEND_COUNT, extendCount);
				notificationIntent.putExtra(RReminder.PLAY_SOUND, false);
				notificationIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
				notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				notificationIntent.setAction(RReminder.CUSTOM_INTENT_VIEW_NOTIFICATION_ACTIVITY);
				PendingIntent pi = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
				builder.extend(new NotificationCompat.WearableExtender().addAction(extendAction));
				switch(typeForNotification){
				case 1: case 3:
					//note = new Notification(android.R.drawable.stat_sys_warning, getString(R.string.notify_work_period_end_ticker_message), System.currentTimeMillis());
					notificationMessage = getString(R.string.notify_work_period_end_message_part_one) + " ";
					if (extendCount>0){
						notificationMessage+=String.format(getString(R.string.notify_period_end_message_extend), extendCount);
					}
					notificationMessage+=getString(R.string.notify_work_period_end_message_part_two);
					//note.setLatestEventInfo(this, getString(R.string.notify_work_period_end_title), notificationMessage , pi);
					builder.setSmallIcon(R.drawable.ic_notify_start_rest);
					builder.setTicker(getString(R.string.notify_work_period_end_ticker_message));
					builder.setContentTitle(getString(R.string.notify_work_period_end_ticker_message));
					builder.setContentText(notificationMessage);
					if(RReminder.isLedEnabled(this)){
						builder.setLights(Color.BLUE, 100,900);
					}
					break;
				case 2: case 4:
					//note = new Notification(android.R.drawable.stat_sys_warning, getString(R.string.notify_rest_period_end_ticker_message), System.currentTimeMillis());
					notificationMessage = getString(R.string.notify_rest_period_end_message_part_one) + " ";
					if (extendCount>0){
						notificationMessage+=String.format(getString(R.string.notify_period_end_message_extend), extendCount);
					}
					notificationMessage+=getString(R.string.notify_rest_period_end_message_part_two);
					//note.setLatestEventInfo(this, getString(R.string.notify_rest_period_end_title), notificationMessage , pi);
					builder.setSmallIcon(R.drawable.ic_notify_start_work);
					builder.setTicker(getString(R.string.notify_rest_period_end_ticker_message));
					builder.setContentTitle(getString(R.string.notify_rest_period_end_ticker_message));
					builder.setContentText(notificationMessage);
					if(RReminder.isLedEnabled(this)){
						builder.setLights(Color.YELLOW, 100,900);
					}
					break;
				default: 
					//note = new Notification(android.R.drawable.stat_sys_warning, getString(R.string.notify_work_period_end_ticker_message), System.currentTimeMillis());
					//note.setLatestEventInfo(this, "Type exception title", "Type exception message", pi);
					builder.setSmallIcon(android.R.drawable.stat_sys_warning);
					builder.setTicker(getString(R.string.notify_work_period_end_ticker_message));
					builder.setContentTitle("PeriodTypeException");
					builder.setContentText("PeriodTypeException");
					break;
				}
				/*
				if(am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL && callState == TelephonyManager.CALL_STATE_IDLE){
					builder.setSound(Scheduler.getRingtone(getBaseContext(),typeForNotification));
				}
				*/
				Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
				if(am.getRingerMode()!=AudioManager.RINGER_MODE_SILENT){
	       			if (Build.VERSION.SDK_INT >= 11) {
						if(RReminder.isVibrateEnabled(this, v)){
							// Vibrate for 300 milliseconds
							builder.setVibrate(new long[] {0,300});
						}
	    			} else {
						if(RReminder.isVibrateEnabledSupport(this)){
							// Vibrate for 300 milliseconds
							builder.setVibrate(new long[] {0,300});
						}
	    			}
				}
				
				builder.setContentIntent(pi);
				builder.setAutoCancel(true);
				
				int id = 1;
				mgr.notify(id,builder.build());
				
			}
		}
		OnAlarmReceiver.completeWakefulIntent(intent);
	}

}
