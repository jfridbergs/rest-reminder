package com.colormindapps.rest_reminder_alarm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;


public class PreferenceXActivity extends AppCompatActivity implements PreferenceActivityLinkedService, PreferenceFragmentCompat.OnPreferenceStartScreenCallback, OnClearDbDialogCloseListener,
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
	private Node connectedNode;
	private boolean wearOn = false;

	private String debug = "PREFERENCE_ACTIVITY";



	//Request code for launching the Intent to resolve Google Play services errors.
	private static final int REQUEST_RESOLVE_ERROR = 1000;


	private final ServiceConnection mConnection = new ServiceConnection() {
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
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.settings, new PreferenceXFragment())
					.commit();

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences_x);
	       if (savedInstanceState == null)
	        {
				if(RReminderMobile.isCounterServiceRunning(this)){
					Intent intent = new Intent(this, CounterService.class);
					bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
				} else {
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.settings, new PreferenceXFragment())
							.commit();
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
	public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragmentCompat,
										   PreferenceScreen preferenceScreen) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		PreferenceXSubScreenFragment fragment = PreferenceXSubScreenFragment.newInstance("Advanced Settings Subscreen");
		Bundle args = new Bundle();
		//Defining the sub screen as new root for the  subscreen
		args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.getKey());
		fragment.setArguments(args);
		ft.replace(R.id.settings, fragment, preferenceScreen.getKey());
		ft.addToBackStack(null);
		ft.commit();
		return true;
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
		unbindFromService();
		super.onStop();
	}

	public void unbindFromService(){
		if(mBound){
			unbindService(mConnection);
			mBound = false;
		}
	}
	@Override
	public Bundle getDataFromService() {
		if (mBound) {
			return mService.getData();
		}
		return null;
	}

	@Override
	public void updateWearStatusFromPreference(int type, long periodEndTimeValue, int extendCount){
		updateReminderStatus(type,periodEndTimeValue,extendCount, true, wearOn);
	}

	private void updateReminderStatus(int type, long periodEndValue, int extendCount, boolean mobileOn, boolean wearOn){
		PendingResult<DataApi.DataItemResult> pendingResult =
				Wearable.DataApi.putDataItem(mGoogleApiClient,RReminder.createStatusData(RReminder.DATA_API_SOURCE_MOBILE, type,periodEndValue,extendCount, mobileOn, wearOn));

	}
	// cant update values in dataevent individually, with every preference update need to resubmit all preference batch
	@Override
	public void updateWearPreferences(String reminderMode, String workLength, String restLength, int extendLength, boolean extendEnabled, boolean startNextEnabled){

		Log.d(debug, "reminder mode: "+reminderMode);
		Log.d(debug, "work period: "+workLength);
		Log.d(debug, "rest period: "+restLength);
		Log.d(debug, "extend length: "+ extendLength);
		Log.d(debug, "extend enabled: "+ extendEnabled);
		Log.d(debug, "start next enabled: "+ startNextEnabled);


		PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/reminder_preferences");
		putDataMapRequest.getDataMap().putString(RReminder.WEAR_PREF_REMINDER_MODE,reminderMode);
		putDataMapRequest.getDataMap().putString(RReminder.WEAR_PREF_WORK_LENGTH,workLength);
		putDataMapRequest.getDataMap().putString(RReminder.WEAR_PREF_REST_LENGTH,restLength);
		putDataMapRequest.getDataMap().putInt(RReminder.WEAR_PREF_EXTEND_LENGTH,extendLength);
		putDataMapRequest.getDataMap().putBoolean(RReminder.WEAR_PREF_EXTEND_ENABLED,extendEnabled);
		putDataMapRequest.getDataMap().putBoolean(RReminder.WEAR_PREF_START_NEXT_ENABLED,startNextEnabled);
		PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
		PendingResult<DataApi.DataItemResult> pendingResult =
				Wearable.DataApi.putDataItem(mGoogleApiClient,putDataRequest);

	}

	@Override
	public void onDataChanged(DataEventBuffer dataEvents) {

		for (DataEvent event : dataEvents) {
			if (event.getType() == DataEvent.TYPE_CHANGED) {
				DataItem item = event.getDataItem();

				if(item.getUri().getPath().compareTo(RReminder.DATA_API_REMINDER_STATUS_PATH)==0){
					DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
					wearOn = dataMap.getBoolean(RReminder.DATA_API_WEAR_ON);
				}
			}
		}
	}

	private void getData(final String pathToContent) {
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
											wearOn = data.getBoolean(RReminder.DATA_API_WEAR_ON);
										}
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

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d(debug, "Google API Client was connected");
		mResolvingError = false;
		Wearable.DataApi.addListener(mGoogleApiClient, this);
		Wearable.MessageApi.addListener(mGoogleApiClient, this);
		Wearable.CapabilityApi.addListener(
				mGoogleApiClient, this, Uri.parse("wear://"), CapabilityApi.FILTER_REACHABLE);
		//obtaining info about the state of wear app
		getConnectedNode();
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

	@Override
	public void deleteDB() {
		SessionsViewModel mSessionsViewModel = new ViewModelProvider(this).get(SessionsViewModel.class);
		mSessionsViewModel.deleteOlder(0);
	}
}
