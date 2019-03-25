package com.colormindapps.rest_reminder_alarm;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;
import com.colormindapps.rest_reminder_alarm.shared.ReminderStatus;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Calendar;


public class MobilePeriodService extends JobIntentService implements
		GoogleApiClient.ConnectionCallbacks,
		DataApi.DataListener,
		CapabilityApi.CapabilityListener,
		GoogleApiClient.OnConnectionFailedListener {

	long mCalendar;
	int type;
	int extendCount;
	int typeForNotification;
	long periodEndedTime;
	boolean noReminderStatus = false;
	Intent periodIntent;
	String notificationMessage;
	NotificationManagerCompat mgr;
	ReminderStatus statusData;

	static final int JOB_ID = 1000;

	/**
	 * Convenience method for enqueuing work in to this service.
	 */
	static void enqueueWork(Context context, Intent work) {
		enqueueWork(context, MobilePeriodService.class, JOB_ID, work);
	}

	String debug  = "MOBILE_PERIOD_SERVICE";

	private GoogleApiClient mGoogleApiClient;
	private Node connectedNode;



	@Override
	public void onCreate() {
		Log.d(debug, "onCreate");
		super.onCreate();

	}

	@Override
	public void onDestroy() {
		Log.d(debug, "onDestroy");
		if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected()) {
			Wearable.DataApi.removeListener(mGoogleApiClient, this);
			Wearable.CapabilityApi.removeListener(mGoogleApiClient, this);
			mGoogleApiClient.disconnect();
		}
		super.onDestroy();

	}

	@Override
	protected void onHandleWork(Intent intent) {
		Log.d(debug, "onHandleWork");
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(Wearable.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
		mGoogleApiClient.connect();
		periodIntent = intent;
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d(debug, "onConnected(): Successfully connected to Google API client");
		Wearable.DataApi.addListener(mGoogleApiClient, this);
		Wearable.CapabilityApi.addListener(
				mGoogleApiClient, this, Uri.parse("wear://"), CapabilityApi.FILTER_REACHABLE);
		//it is required to connect to Google API before doing any work
		getConnectedNode();
	}

	@Override
	public void onConnectionSuspended(int cause) {
		Log.d(debug, "onConnectionSuspended(): Connection to Google API client was suspended");
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.e(debug, "onConnectionFailed(): Failed to connect, with result: " + result);
		noReminderStatus = true;
		doServiceWork();
	}

	@Override
	public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
		Log.d(debug, "onCapabilityChanged: " + capabilityInfo);
	}

	@Override
	public void onDataChanged(DataEventBuffer dataEvents) {
		Log.d(debug, "onDataChanged(): " + dataEvents);

		for (DataEvent event : dataEvents) {
			if (event.getType() == DataEvent.TYPE_CHANGED) {

			}
		}
	}

	public void updateReminderStatus(int type, long periodEndTimeValue, int extendCount, boolean mobileOn, boolean wearOn){
		PendingResult<DataApi.DataItemResult> pendingResult =
				Wearable.DataApi.putDataItem(mGoogleApiClient,RReminder.createStatusData(RReminder.DATA_API_SOURCE_MOBILE,type,periodEndTimeValue,extendCount, mobileOn, wearOn));
	}

	private void getData(final String pathToContent) {
		Log.d(debug, "attempt to get data from DATA API");

		Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
			@Override
			public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {

				String nodeID;
				if (connectedNode != null){
					nodeID = connectedNode.getId();
				} else {
					nodeID = getLocalNodeResult.getNode().getId();
				}

				Uri uri = new Uri.Builder()
						.scheme(PutDataRequest.WEAR_URI_SCHEME)
						.path(pathToContent)
						.authority(nodeID)
						.build();

				Wearable.DataApi.getDataItem(mGoogleApiClient, uri)
						.setResultCallback(
								new ResultCallback<DataApi.DataItemResult>() {
									@Override
									public void onResult(DataApi.DataItemResult dataItemResult) {

										if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
											DataMap data = DataMap.fromByteArray(dataItemResult.getDataItem().getData());
											statusData = setReminderData(data.getInt(RReminder.PERIOD_TYPE), data.getLong(RReminder.PERIOD_END_TIME), data.getInt(RReminder.EXTEND_COUNT), data.getInt(RReminder.DATA_API_SOURCE),data.getBoolean(RReminder.DATA_API_MOBILE_ON));

										}
										//only after connecting to Google API and checking whether or not it has reminderStatus stored in Data API, we can continue with the rest of MobilePeriodService function
										doServiceWork();
									}
								}
						);
			}
		});
	}

	private void getConnectedNode()
	{
		Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
			@Override
			public void onResult(NodeApi.GetConnectedNodesResult nodes) {
				for (Node node : nodes.getNodes()) {
					connectedNode = node;
				}
				getData(RReminder.DATA_API_REMINDER_STATUS_PATH);
			}
		});
	}

	public ReminderStatus setReminderData(int periodType, long periodEndTime, int extendCount, int commandSource, boolean mobileOn){
		Log.d(debug, "obtaining reminder status data from data api");
		return new ReminderStatus(periodType, periodEndTime, extendCount, commandSource, mobileOn, true);

	}


	public void doServiceWork(){
		if(periodIntent.getExtras()!=null){
			type = periodIntent.getExtras().getInt(RReminder.PERIOD_TYPE);
			extendCount = periodIntent.getExtras().getInt(RReminder.EXTEND_COUNT);
			periodEndedTime = periodIntent.getExtras().getLong(RReminder.PERIOD_END_TIME);
		}

		Log.d(debug, "periodEndTimeValue: "+ periodIntent.getExtras().getLong(RReminder.PERIOD_END_TIME));

			//manage period end
			RReminder.addDismissDialogFlag(this);


			typeForNotification = type;

			type = RReminder.getNextPeriodType(type);
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
				launchNotification();
			}

			Log.d(debug, "checking, if app is running on wear");
			if(!noReminderStatus){
				if(!statusData.isWearOn()){
					updateReminderStatus(type, mCalendar,0, true, false);
				}

			}

	}


	public void gotoMainActivity(){
						/*
				Intent playIntent = new Intent(context, PlaySoundService.class);
				playIntent.putExtra(Scheduler.PERIOD_TYPE,typeForNotification);
				context.startService(playIntent);
				*/

		//Set the next period end alarms and start service, if any automatical mode is selected
		if(RReminder.getMode(this) != 1){
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


	public void launchNotification(){
		//building android wear-only action for extending previously ended period
		//the action intent

		AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);

		Intent extendIntent = new Intent(this,MainActivity.class);
		extendIntent.setAction(RReminder.ACTION_WEAR_NOTIFICATION_EXTEND);
		extendIntent.putExtra(RReminder.PERIOD_TYPE,type);
		extendIntent.putExtra(RReminder.EXTENDED_PERIOD_TYPE, typeForNotification);
		extendIntent.putExtra(RReminder.PERIOD_END_TIME,mCalendar);
		extendIntent.putExtra(RReminder.EXTEND_COUNT,extendCount);
		PendingIntent extendPendingIntent = PendingIntent.getActivity(this,5,extendIntent,PendingIntent.FLAG_ONE_SHOT);

		//action itself
		NotificationCompat.Action extendAction = new NotificationCompat.Action.Builder(R.drawable.ic_notify_wear_extend, getString(R.string.notify_extend),extendPendingIntent)
				.build();

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, RReminder.CHANNEL_PERIOD_END_ID);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			if(RReminder.getMode(this)==1){
				Intent startNextPeriod = new Intent (this, MainActivity.class);
				startNextPeriod.setAction(RReminder.ACTION_MANUAL_START_NEXT_PERIOD);
				startNextPeriod.putExtra(RReminder.MANUAL_MODE_NEXT_PERIOD_TYPE, type);
				PendingIntent pIntentStartNextPeriod = PendingIntent.getActivity(this, 0, startNextPeriod, PendingIntent.FLAG_ONE_SHOT);
				builder.addAction(android.R.drawable.stat_notify_sync , getString(R.string.start_next_period), pIntentStartNextPeriod);
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
