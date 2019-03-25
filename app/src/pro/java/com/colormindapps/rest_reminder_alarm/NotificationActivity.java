package com.colormindapps.rest_reminder_alarm;

import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Calendar;



public class NotificationActivity extends FragmentActivity implements
		DataApi.DataListener,
		OnDialogCloseListener,
		MessageApi.MessageListener,
		CapabilityApi.CapabilityListener,
		GoogleApiClient.OnConnectionFailedListener,
		GoogleApiClient.ConnectionCallbacks {
	private long mCalendar;
	private int type, extendCount;
	public static boolean isOnVisible;
	public boolean hasntRestored = true;
	public boolean redirectScreenOff;
	int restoredType;
	int screenOrientation;
	public boolean playSound;
	CharSequence titleSequence;
	Resources resources;
	Typeface titleFont, descriptionFont, buttonFont;
	NotificationManagerCompat mgr;
	String work,rest;
	boolean wearOn = false;

	private GoogleApiClient mGoogleApiClient;
	private Node connectedNode;
	private boolean mResolvingError = false;

	private String debug = "NOTIFICATION_ACTIVITY";

	//Request code for launching the Intent to resolve Google Play services errors.
	private static final int REQUEST_RESOLVE_ERROR = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);
		resources = getResources();
		screenOrientation = resources.getConfiguration().orientation;
		titleFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-ThCn.otf");
		descriptionFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Lt.otf");
		buttonFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Roman.otf");

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(Wearable.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();

		if(RReminder.getMode(this)==1){
	        final Window win = getWindow();
	        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
	                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	        // Turn on the screen unless we are being launched from the AlarmAlert
	        // subclass.
	            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

			//Initiate a countdown to turn off screen after certain ammount of time in order to avoid burning out battery
			new CountDownTimer(15000, 1000) {

				public void onTick(long millisUntilFinished) {
				}

				public void onFinish() {
					win.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
				}
			}.start();
	        
		}



        if(savedInstanceState!=null){
            hasntRestored = savedInstanceState.getBoolean("hasntRestored");
            restoredType = savedInstanceState.getInt("restoredType");
        }
		if(getIntent().getExtras()!=null) {
			type = getIntent().getExtras().getInt(RReminder.PERIOD_TYPE);
			extendCount = getIntent().getExtras().getInt(RReminder.EXTEND_COUNT);
			playSound = getIntent().getExtras().getBoolean(RReminder.PLAY_SOUND);
			mCalendar = getIntent().getExtras().getLong(RReminder.PERIOD_END_TIME);
			redirectScreenOff = getIntent().getExtras().getBoolean(RReminder.REDIRECT_SCREEN_OFF);
		}
		work = getString(R.string.work);
		rest = getString(R.string.rest);
		mgr = NotificationManagerCompat.from(getApplicationContext());
		if(RReminder.isActiveModeNotificationEnabled(this)){
			mgr.notify(1, RReminderMobile.updateOnGoingNotification(this, RReminder.getNextType(type),mCalendar, false));
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
	
	@Override
	protected void onStart(){
		super.onStart();

		if (!mResolvingError) {
			mGoogleApiClient.connect();
		}

		RelativeLayout rootLayout;
        TextView notificationTitle = findViewById(R.id.notification_title);
		TextView extendDescription = findViewById(R.id.notification_extend_description);
        notificationTitle.setTypeface(titleFont);
        ImageView image =findViewById(R.id.notification_image);
        TextView notificationDescription =findViewById(R.id.notification_description);
        notificationDescription.setTypeface(descriptionFont);
        Button notificationButton = findViewById(R.id.notification_button);
		Button extendPeriodEnd =  findViewById(R.id.button_notification_period_end_extend);
        notificationButton.setTypeface(buttonFont);
        rootLayout =  findViewById(R.id.root_layout);


        switch(type){
            case RReminder.WORK:
                notificationTitle.setText(getString(R.string.notification_work_end_title));
                rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.rest));
                image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.img_coffee_mug));
                break;
            case RReminder.REST:
                notificationTitle.setText(getString(R.string.notification_rest_end_title));
                rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.work));
                if(screenOrientation == Configuration.ORIENTATION_LANDSCAPE){
                    image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.img_gears_land));
                } else {
                    image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.img_gears));
                }
                break;
            case RReminder.WORK_EXTENDED:
                rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.rest));
                image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.img_coffee_mug));
				notificationTitle.setText(getString(R.string.notification_work_end_title));
                if(extendCount<=1){
					extendDescription.setText(String.format(getString(R.string.notification_end_extend_description_once),work));
                } else {
                    String titleText = String.format(getString(R.string.notification_end_extend_description_multiple),work, extendCount);
					extendDescription.setText(titleText);
                }
				extendDescription.setVisibility(View.VISIBLE);
                break;
            case RReminder.REST_EXTENDED:
				notificationTitle.setText(getString(R.string.notification_rest_end_title));
                rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.work));
                if(screenOrientation == Configuration.ORIENTATION_LANDSCAPE){
                    image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.img_gears_land));
                } else {
                    image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.img_gears));

                }
                if(extendCount<=1){
					extendDescription.setText(String.format(getString(R.string.notification_end_extend_description_once),rest));
                } else {
                    String titleText = String.format(getString(R.string.notification_end_extend_description_multiple), rest, extendCount);
					extendDescription.setText(titleText);
                }
				extendDescription.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
        titleSequence = notificationTitle.getText();
        notificationTitle.setTextSize(RReminder.adjustTitleSize(this, titleSequence.length(), false));




        if(RReminder.getMode(this)!= 1){

            if(RReminder.isExtendEnabled(this)){

                extendPeriodEnd.setTypeface(descriptionFont);
                extendPeriodEnd.setVisibility(View.VISIBLE);
                extendPeriodEnd.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        showExtendDialog();

                    }
                });
            }


            notificationButton.setText(getString(R.string.close_notification));

            switch(type){
                case RReminder.WORK: case RReminder.WORK_EXTENDED:
                    notificationDescription.setText(String.format(getString(R.string.next_period_end_description), rest,RReminder.getTimeString(this, mCalendar)));

                    break;
                case RReminder.REST: case RReminder.REST_EXTENDED:

                    notificationDescription.setText(String.format(getString(R.string.next_period_end_description), work, RReminder.getTimeString(this, mCalendar)));
                    break;
                default:
                    break;
            }

        } else {
            notificationButton.setText(getString(R.string.start_next_period));
			extendPeriodEnd.setVisibility(View.INVISIBLE);

            switch(type){
                case RReminder.WORK:
                    notificationDescription.setText(String.format(getString(R.string.notification_end_manual_title), rest));
                    break;
                case RReminder.REST:
					notificationDescription.setText(String.format(getString(R.string.notification_end_manual_title), work));

                    break;
                default:
                    break;
            }

        }




    }
	
	@Override
	protected void onResume(){
		super.onResume();
		setVisibleState(true);
		if(RReminder.getMode(this)!=1 && Calendar.getInstance().getTimeInMillis()>mCalendar){
			finish();
		}
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		setVisibleState(false);
	}
	
	public void notificationButtonAction(View v){
		mgr.cancel(24);
		long nextPeriodEnd = mCalendar;
		
		//Remove a flag for redirecting from MainActivity to NotificationActivity
		
		if(RReminder.getMode(this)== 1){

			nextPeriodEnd = RReminder.getNextPeriodEndTime(this, RReminder.getNextType(type), Calendar.getInstance().getTimeInMillis(), 1, 0L);

			
			new MobilePeriodManager(getApplicationContext()).setPeriod(RReminder.getNextType(type), nextPeriodEnd, extendCount);
			RReminderMobile.startCounterService(this, RReminder.getNextType(type), 0, nextPeriodEnd, false);

		}
		
		if(RReminder.isActiveModeNotificationEnabled(this)){
			mgr.notify(1, RReminderMobile.updateOnGoingNotification(this, RReminder.getNextType(type),nextPeriodEnd, true));
		}
		
		
		finish();
	}
	
	public void notificationTurnOff(View v){
		turnoffMobile();

	}

	public void turnoffMobile(){
		if (mGoogleApiClient!=null) {
			mGoogleApiClient.disconnect();
		}
		Intent intent = new Intent(this, MainActivity.class);
		intent.setAction(RReminder.ACTION_TURN_OFF);
		intent.putExtra(RReminder.START_COUNTER, false);
		intent.putExtra(RReminder.TURN_OFF, 1);
		intent.putExtra(RReminder.PERIOD_END_TIME, mCalendar);
		startActivity(intent);
		finish();
	}

	@Override
	public void onDataChanged(DataEventBuffer dataEvents) {
		Log.d(debug, "onDataChanged: " + dataEvents);

		for (DataEvent event : dataEvents) {

			if (event.getType() == DataEvent.TYPE_CHANGED) {
				DataItem item = event.getDataItem();

				if(item.getUri().getPath().compareTo(RReminder.DATA_API_REMINDER_STATUS_PATH)==0){
					DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
						Log.d(debug, "call updateActivityStatus from onDataChanged");
						Log.d(debug, "difference between wear and mobile: "+Math.abs(dataMap.getLong(RReminder.PERIOD_END_TIME)-mCalendar));
						if(wearOn && !dataMap.getBoolean(RReminder.DATA_API_WEAR_ON)){
							Log.d(debug, "turn off reminder from NotificationActivity");
							turnoffMobile();
						} else if(wearOn && dataMap.getLong(RReminder.PERIOD_END_TIME)> mCalendar && Math.abs(dataMap.getLong(RReminder.PERIOD_END_TIME)-mCalendar)>2000){
							Log.d(debug, "close NotificationActivity and return to MainActivity");
							//if reminder status was updated while notificationA was on screen, first cancel currently running mobile period and then set up new mobile period with values from reminder data
							RReminderMobile.cancelCounterAlarm(NotificationActivity.this.getApplicationContext(), type, extendCount,mCalendar);

							int newPeriodType = dataMap.getInt(RReminder.PERIOD_TYPE);
							long newPeriodEndTime = dataMap.getLong(RReminder.PERIOD_END_TIME);
							int newExtendCount = dataMap.getInt(RReminder.EXTEND_COUNT);
							boolean wearOn = dataMap.getBoolean(RReminder.DATA_API_WEAR_ON);

							new MobilePeriodManager(NotificationActivity.this.getApplicationContext()).setPeriod(newPeriodType, newPeriodEndTime, newExtendCount);
							RReminderMobile.startCounterService(NotificationActivity.this.getApplicationContext(), newPeriodType, newExtendCount, newPeriodEndTime, false);
							finish();
						}

				}
			}
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
											Log.d(debug, "googleAPI source: "+data.getInt(RReminder.DATA_API_SOURCE)+ ", type: "+ data.getInt(RReminder.PERIOD_TYPE)+ ", mobileOn: "+ data.getBoolean(RReminder.DATA_API_MOBILE_ON)+
											", wearOn: "+data.getBoolean(RReminder.DATA_API_WEAR_ON));
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
	public void stopCountDownTimerForDialog(){
	}

    @Override
    public void dialogIsClosed(boolean eulaAccepted){
    }

	@Override
	public void exitApplication(){
	}
	
	@Override
	public void cancelNotificationForDialog(long periodEndTime,boolean removeOnGoing){
		if (RReminder.isActiveModeNotificationEnabled(this)){
			mgr.notify(RReminder.NOTIFICATION_ID, RReminderMobile.updateOnGoingNotification(this, type, periodEndTime,true));
		} else {
			
			mgr.cancel(RReminder.NOTIFICATION_ID);
		}
	}
	
	@Override
	public void resumeCounter(boolean positiveDismissal){
	}
	
	@Override
	public void bindFromFragment(long newPeriodEndTimeValue){
		
	}
	
	@Override
	public void startReminder(){
		
	}

	@Override
	public void updateWearStatus(int type, long periodEndTimeValue, int extendCount, boolean mobileOn){
		Log.d(debug, "updateWearStatus(), type: " + type);
		updateReminderStatus(type,periodEndTimeValue,extendCount, mobileOn, wearOn);
	}

	public void updateReminderStatus(int type, long periodEndTimeValue, int extendCount, boolean mobileOn, boolean wearOn){
		PendingResult<DataApi.DataItemResult> pendingResult =
				Wearable.DataApi.putDataItem(mGoogleApiClient,RReminder.createStatusData(RReminder.DATA_API_SOURCE_MOBILE,type,periodEndTimeValue,extendCount, mobileOn, wearOn));
	}

	
	@Override
	public void unbindFromFragment(){
	}
	
	
	public void showExtendDialog(){
		DialogFragment newFragment = ExtendDialog.newInstance(R.string.extend_dialog_title, type, extendCount, mCalendar, 1);
		newFragment.show(getSupportFragmentManager(), "extendDialog");
	}
	
	public void setVisibleState(boolean state){
		isOnVisible = state;
	}
	
	public static boolean getVisibleState(){
		return isOnVisible;
	}

	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  super.onSaveInstanceState(savedInstanceState);
	  // Save UI state changes to the savedInstanceState.
	  // This bundle will be passed to onCreate if the process is
	  // killed and restarted.
	  savedInstanceState.putBoolean("hasntRestored", false);
	  savedInstanceState.putInt("restoredType", type);
	  // etc.
	}
	
    @Override
    public void onBackPressed() {
    	if(RReminder.getMode(this)==0){
    		super.onBackPressed();
    	}
    }
    
	


}
