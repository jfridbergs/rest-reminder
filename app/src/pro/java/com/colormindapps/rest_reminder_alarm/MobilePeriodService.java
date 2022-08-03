package com.colormindapps.rest_reminder_alarm;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;



public class MobilePeriodService extends JobIntentService {

	int type;
	int extendCount;
	int typeForNotification;
	long periodEndedTime, nextPeriodEndTime;
	Intent periodIntent;
	String notificationMessage;
	NotificationManagerCompat mgr;

	static final int JOB_ID = 1000;

	/**
	 * Convenience method for enqueuing work in to this service.
	 */
	static void enqueueWork(Context context, Intent work) {
		enqueueWork(context, MobilePeriodService.class, JOB_ID, work);
	}










	@Override
	protected void onHandleWork(@NonNull Intent intent) {
		periodIntent = intent;
		doServiceWork();

	}

	public void doServiceWork(){
		if(periodIntent.getExtras()!=null){
			type = periodIntent.getExtras().getInt(RReminder.PERIOD_TYPE);
			extendCount = periodIntent.getExtras().getInt(RReminder.EXTEND_COUNT);
			periodEndedTime = periodIntent.getExtras().getLong(RReminder.PERIOD_END_TIME);
			nextPeriodEndTime = periodIntent.getExtras().getLong(RReminder.NEXT_PERIOD_END_TIME);
		}
			//manage period end
			RReminder.addDismissDialogFlag(this);
			typeForNotification = type;
			type = RReminder.getNextPeriodType(type);

			mgr = NotificationManagerCompat.from(getApplicationContext());
			mgr.cancel(24);

			if(RReminder.isActiveModeNotificationEnabled(this)){
				mgr.notify(1, RReminderMobile.updateOnGoingNotification(this, type, nextPeriodEndTime, true));
			}
			if(MainActivity.getVisibleState() || NotificationActivity.getVisibleState() || RReminder.getMode(this) == 1){
				gotoNotificationActivity();
			} else {
				launchNotification();
			}
	}


	public void gotoNotificationActivity(){


		//Set the next period end alarms and start service, if any automatic mode is selected

		Intent actionIntent = new Intent(this, NotificationActivity.class);
		actionIntent.putExtra(RReminder.PERIOD_TYPE, typeForNotification);
		actionIntent.putExtra(RReminder.PREVIOUS_PERIOD_END_TIME, periodEndedTime);
		actionIntent.putExtra(RReminder.PERIOD_END_TIME, nextPeriodEndTime);
		actionIntent.putExtra(RReminder.EXTEND_COUNT, extendCount);
		actionIntent.putExtra(RReminder.PLAY_SOUND, true);
		actionIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
		actionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		getApplication().startActivity(actionIntent);
	}


	@SuppressLint("UnspecifiedImmutableFlag")
	public void launchNotification(){
		//building android wear-only action for extending previously ended period
		//the action intent

		AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);

		Intent extendIntent = new Intent(this,MainActivity.class);
		extendIntent.setAction(RReminder.ACTION_WEAR_NOTIFICATION_EXTEND);
		extendIntent.putExtra(RReminder.PERIOD_TYPE,type);
		extendIntent.putExtra(RReminder.EXTENDED_PERIOD_TYPE, typeForNotification);
		extendIntent.putExtra(RReminder.PERIOD_END_TIME,nextPeriodEndTime);
		extendIntent.putExtra(RReminder.EXTEND_COUNT,extendCount);
		PendingIntent extendPendingIntent;
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
			extendPendingIntent = PendingIntent.getActivity(this,5,extendIntent,PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
		} else {
			extendPendingIntent = PendingIntent.getActivity(this,5,extendIntent,PendingIntent.FLAG_ONE_SHOT);
		}


		//action itself
		NotificationCompat.Action extendAction = new NotificationCompat.Action.Builder(R.drawable.ic_notify_wear_extend, getString(R.string.notify_extend),extendPendingIntent)
				.build();

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, RReminder.CHANNEL_PERIOD_END_ID);
		if(RReminder.getMode(this)==1){
			Intent startNextPeriod = new Intent (this, MainActivity.class);
			startNextPeriod.setAction(RReminder.ACTION_MANUAL_START_NEXT_PERIOD);
			startNextPeriod.putExtra(RReminder.MANUAL_MODE_NEXT_PERIOD_TYPE, type);
			PendingIntent pIntentStartNextPeriod;
			if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
				pIntentStartNextPeriod = PendingIntent.getActivity(this, 0, startNextPeriod, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
			} else {
				pIntentStartNextPeriod = PendingIntent.getActivity(this, 0, startNextPeriod, PendingIntent.FLAG_ONE_SHOT);
			}

			builder.addAction(android.R.drawable.stat_notify_sync , getString(R.string.start_next_period), pIntentStartNextPeriod);
		}
		Intent turnOffIntent = new Intent (this, MainActivity.class);
		turnOffIntent.setAction(RReminder.ACTION_TURN_OFF);
		turnOffIntent.putExtra(RReminder.TURN_OFF, 1);
		PendingIntent pIntentTurnOff;
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
			pIntentTurnOff = PendingIntent.getActivity(this, 0, turnOffIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
		} else {
			pIntentTurnOff = PendingIntent.getActivity(this, 0, turnOffIntent, PendingIntent.FLAG_ONE_SHOT);
		}

		builder.addAction(R.drawable.ic_notify_turn_off , getString(R.string.notify_turn_off), pIntentTurnOff);
		Intent notificationIntent = new Intent(this, NotificationActivity.class);
		notificationIntent.putExtra(RReminder.PERIOD_TYPE, typeForNotification);
		notificationIntent.putExtra(RReminder.PERIOD_END_TIME, nextPeriodEndTime);
		notificationIntent.putExtra(RReminder.EXTEND_COUNT, extendCount);
		notificationIntent.putExtra(RReminder.PLAY_SOUND, false);
		notificationIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		notificationIntent.setAction(RReminder.ACTION_VIEW_NOTIFICATION_ACTIVITY);
		PendingIntent pi;
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
			pi = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		} else {
			pi = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		}

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
				builder.setSmallIcon(android.R.drawable.stat_sys_warning);
				builder.setTicker(getString(R.string.notify_work_period_end_ticker_message));
				builder.setContentTitle("PeriodTypeException");
				builder.setContentText("PeriodTypeException");
				break;
		}
		if(am!=null && am.getRingerMode()!=AudioManager.RINGER_MODE_SILENT){

			if(RReminder.isVibrateEnabledSupport(this)){
				// Vibrate for 300 milliseconds
				builder.setVibrate(new long[] {0,300});

			}
		}

		//setting up background color for notification
		if(RReminder.isNotificationColorizeEnabled(this)){
			builder.setColorized(true);
			builder.setColor(RReminder.getNotificationBackgroundColorId(this.getApplicationContext(), typeForNotification,extendCount));
		} else{
			builder.setColorized(false);
		}

		builder.setContentIntent(pi);
		builder.setAutoCancel(true);

		int id = 1;
		mgr.notify(id,builder.build());
	}

}
