package com.colormindapps.rest_reminder_alarm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;


public class PreferenceActivity extends FragmentActivity implements
		DataApi.DataListener,
		OnPreferencesOpenListener,
		MessageApi.MessageListener,
		CapabilityApi.CapabilityListener,
		GoogleApiClient.OnConnectionFailedListener,
		GoogleApiClient.ConnectionCallbacks{

	CounterService mService;
	boolean mBound = false;
	CounterService.CounterBinder binder;
	long periodEndTimeValue;
	int periodType, extendCount;

	private boolean mResolvingError = false;

	private GoogleApiClient mGoogleApiClient;

	private String debug = "PREFERENCE_ACTIVITY";

	//Request code for launching the Intent to resolve Google Play services errors.
	private static final int REQUEST_RESOLVE_ERROR = 1000;


	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className,
									   IBinder service) {
			binder = (CounterService.CounterBinder) service;
			mService = binder.getService();
			mBound = true;
			Bundle data = getDataFromService();
			periodType = data.getInt(RReminder.PERIOD_TYPE);
			extendCount = data.getInt(RReminder.EXTEND_COUNT);
			periodEndTimeValue = data.getLong(RReminder.PERIOD_END_TIME);
			long timeRemaining = mService.getCounterTimeValue();
			getFragmentManager().beginTransaction().replace(android.R.id.content, MyPreferenceFragment.newInstance(periodType, extendCount,periodEndTimeValue)).commit();
			unbindFromService();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	       if (savedInstanceState == null)
	        {
				if(RReminder.isCounterServiceRunning(this)){
					Intent intent = new Intent(this, CounterService.class);
					bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
				} else {
					getFragmentManager().beginTransaction().replace(android.R.id.content, MyPreferenceFragment.newInstance(1,0,5000L)).commit();
				}
	            // Display the fragment as the main content.

	        }

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(Wearable.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();

	}

	@Override
	protected void onStart(){
		super.onStart();

		if (!mResolvingError) {
			mGoogleApiClient.connect();
		}
	}

	@Override
	protected void onStop(){
		if (!mResolvingError && (mGoogleApiClient != null) && (mGoogleApiClient.isConnected())) {

			Wearable.DataApi.removeListener(mGoogleApiClient, this);
			Wearable.MessageApi.removeListener(mGoogleApiClient, this);
			Wearable.CapabilityApi.removeListener(mGoogleApiClient, this);

			mGoogleApiClient.disconnect();
		}

		super.onStop();
	}

	public void unbindFromService(){
		if(mBound){
			unbindService(mConnection);
			mBound = false;
		}
	}

	public Bundle getDataFromService() {
		if (mBound) {
			return mService.getData();
		}
		return null;
	}

	@Override
	public void updateWearStatusFromPreference(int type, long periodEndTimeValue, int extendCount){
		updateReminderStatus(type,periodEndTimeValue,extendCount);
	}

	private void updateReminderStatus(int type, long periodEndValue, int extendCount){
		Log.d(debug, "extend counter value in updateReminderStatus: "+extendCount);
		PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/reminder_status");
		putDataMapRequest.getDataMap().putInt(RReminder.PERIOD_TYPE,type);
		putDataMapRequest.getDataMap().putLong(RReminder.PERIOD_END_TIME,periodEndValue);
		putDataMapRequest.getDataMap().putInt(RReminder.EXTEND_COUNT, extendCount);
		PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
		PendingResult<DataApi.DataItemResult> pendingResult =
				Wearable.DataApi.putDataItem(mGoogleApiClient,putDataRequest);

	}

	@Override
	public void onDataChanged(DataEventBuffer dataEvents) {
		Log.d(debug, "onDataChanged: " + dataEvents);

		for (DataEvent event : dataEvents) {
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d(debug, "Google API Client was connected");
		mResolvingError = false;
		Wearable.DataApi.addListener(mGoogleApiClient, this);
		Wearable.MessageApi.addListener(mGoogleApiClient, this);
		Wearable.CapabilityApi.addListener(
				mGoogleApiClient, this, Uri.parse("wear://"), CapabilityApi.FILTER_REACHABLE);
	}

	@Override
	public void onConnectionSuspended(int cause) {
		Log.d(debug, "Connection to Google API client was suspended");
	}

	@Override
	public void onMessageReceived(final MessageEvent messageEvent) {
		Log.d(debug, "onMessageReceived() A message from watch was received:"
				+ messageEvent.getRequestId() + " " + messageEvent.getPath());

		//mDataItemListAdapter.add(new Event("Message from watch", messageEvent.toString()));
	}

	@Override
	public void onCapabilityChanged(final CapabilityInfo capabilityInfo) {
		Log.d(debug, "onCapabilityChanged: " + capabilityInfo);

		//mDataItemListAdapter.add(new Event("onCapabilityChanged", capabilityInfo.toString()));
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!mResolvingError) {

			if (result.hasResolution()) {
				try {
					mResolvingError = true;
					result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
				} catch (IntentSender.SendIntentException e) {
					// There was an error with the resolution intent. Try again.
					mGoogleApiClient.connect();
				}
			} else {
				Log.e(debug, "Connection to Google API client has failed");
				mResolvingError = false;

				Wearable.DataApi.removeListener(mGoogleApiClient, this);
				Wearable.MessageApi.removeListener(mGoogleApiClient, this);
				Wearable.CapabilityApi.removeListener(mGoogleApiClient, this);

			}
		}
	}

}
