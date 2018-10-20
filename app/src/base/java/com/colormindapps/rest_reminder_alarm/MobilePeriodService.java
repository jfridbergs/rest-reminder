package com.colormindapps.rest_reminder_alarm;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.Calendar;


public class MobilePeriodService extends IntentService {
	long mCalendar;
	int type;
	int extendCount;
	int typeForNotification;
	String notificationMessage;
	NotificationManagerCompat mgr;



	public MobilePeriodService() {
		super("MobilePeriodService");
	}
	
	
	
	@Override
	protected void onHandleIntent(Intent intent){
		if(intent.getExtras()!=null){
			type = intent.getExtras().getInt(RReminder.PERIOD_TYPE);
			extendCount = intent.getExtras().getInt(RReminder.EXTEND_COUNT);
		}

		



		
		if (type==99){
			//play approximate alarm sound
			playSound();
		} else {
			//manage period end
			RReminder.addDismissDialogFlag(this);

			
			typeForNotification = type;
		
			type = getNextPeriodType(type);
			playSound();
			mCalendar = RReminder.getNextPeriodEndTime(this, type, Calendar.getInstance().getTimeInMillis(), 1, 0L);

			mgr = NotificationManagerCompat.from(getApplicationContext());
			mgr.cancel(24);
				
				

							
			//PowerManager pm = (PowerManager)
			//getSystemService(Context.POWER_SERVICE);
			//boolean isScreenOn = pm.isScreenOn();
			if(RReminder.isActiveModeNotificationEnabled(this)){
				mgr.notify(1, RReminderMobile.updateOnGoingNotification(this, type, mCalendar, true));
			}
			if(MainActivity.getVisibleState() || NotificationActivity.getVisibleState() || RReminder.getMode(this) == 1){
				gotoMainActivity();
			} else {


				new MobilePeriodManager(getApplicationContext()).setPeriod(type, mCalendar, 0, false);
				RReminderMobile.startCounterService(this, type, 0, mCalendar, true);

				launchNotification();



				
			}
		}
		MobileOnAlarmReceiver.completeWakefulIntent(intent);
	}

	public void gotoMainActivity(){
						/*
				Intent playIntent = new Intent(context, PlaySoundService.class);
				playIntent.putExtra(Scheduler.PERIOD_TYPE,typeForNotification);
				context.startService(playIntent);
				*/

		//Set the next period end alarms and start service, if any automatical mode is selected
		if(RReminder.getMode(this) != 1){
			new MobilePeriodManager(getApplicationContext()).setPeriod(type, mCalendar, 0, false);
			RReminderMobile.startCounterService(this, type, 0, mCalendar, false);
		}

		Intent actionIntent = new Intent(this, NotificationActivity.class);
		actionIntent.putExtra(RReminder.PERIOD_TYPE, typeForNotification);
		actionIntent.putExtra(RReminder.PERIOD_END_TIME, mCalendar);
		actionIntent.putExtra(RReminder.EXTEND_COUNT, extendCount);
		actionIntent.putExtra(RReminder.PLAY_SOUND, true);
		actionIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
		actionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		getApplication().startActivity(actionIntent);
	}

	public void playSound(){
		Intent playIntent = new Intent(this, PlaySoundService.class);
		playIntent.putExtra(RReminder.PERIOD_TYPE,type);
		this.startService(playIntent);
	}

	public int getNextPeriodType( int previousType){
		switch(previousType){
			case RReminder.WORK:case RReminder.WORK_EXTENDED:  return  RReminder.REST;
			case RReminder.REST:case RReminder.REST_EXTENDED:  return  RReminder.WORK;
			default: return RReminder.PERIOD_OFF;
		}
	}

	public void launchNotification(){
		//building android wear-only action for extending previously ended period
		//the action intent

		AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, RReminder.CHANNEL_PERIOD_END_ID);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			if(RReminder.getMode(this)==1){
				Intent startNextPeriod = new Intent (this, MainActivity.class);
				startNextPeriod.setAction(RReminder.ACTION_MANUAL_START_NEXT_PERIOD);
				startNextPeriod.putExtra(RReminder.MANUAL_MODE_NEXT_PERIOD_TYPE, type);
				PendingIntent pIntentStartNextPeriod = PendingIntent.getActivity(this, 0, startNextPeriod, PendingIntent.FLAG_ONE_SHOT);
				builder.addAction(android.R.drawable.stat_notify_sync , getString(R.string.notify_turn_off), pIntentStartNextPeriod);
			}
			Intent turnOffIntent = new Intent (this, MainActivity.class);
			turnOffIntent.setAction(RReminder.ACTION_TURN_OFF);
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
		notificationIntent.setAction(RReminder.ACTION_VIEW_NOTIFICATION_ACTIVITY);
		PendingIntent pi = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
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
				//
				if(am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL && callState == TelephonyManager.CALL_STATE_IDLE){
					builder.setSound(Scheduler.getRingtone(getBaseContext(),typeForNotification));
				}
				*/
		if(am!=null && am.getRingerMode()!=AudioManager.RINGER_MODE_SILENT){

			if(RReminder.isVibrateEnabledSupport(this)){
				// Vibrate for 300 milliseconds
				builder.setVibrate(new long[] {0,300});

			}
		}

		//setting up background color for notification
		Log.d("MOBILE_PERIOD_SERVICE", "is notification colorize enabled: "+RReminder.isNotificationColorizeEnabled(this));
		if(RReminder.isNotificationColorizeEnabled(this)){
			builder.setColorized(true);
			builder.setColor(RReminder.getNotificationBackgroundColorId(this.getApplicationContext(), typeForNotification,extendCount));
			Log.d("MOBILE_PERIOD_SERVICE", "notification color ON");
		} else {
			builder.setColorized(false);
			Log.d("MOBILE_PERIOD_SERVICE", "notification color OFF");
		}


		builder.setContentIntent(pi);
		builder.setAutoCancel(true);

		int id = 1;
		mgr.notify(id,builder.build());
	}

}
