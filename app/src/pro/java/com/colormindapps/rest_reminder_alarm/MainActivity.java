package com.colormindapps.rest_reminder_alarm;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.colormindapps.rest_reminder_alarm.CounterService.CounterBinder;
import com.colormindapps.rest_reminder_alarm.shared.MyCountDownTimer;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
		DataApi.DataListener,
		OnDialogCloseListener,
		MessageApi.MessageListener,
		CapabilityApi.CapabilityListener,
		GoogleApiClient.OnConnectionFailedListener,
		GoogleApiClient.ConnectionCallbacks {


	public int periodType = RReminder.PERIOD_OFF;
	public int extendCount;
	private int requiredSwipeDistance;
	private boolean turnOffIntent = false;
	public MyCountDownTimer countdown;
	public long periodEndTimeValue;
	private long counterTimeValue;
	private boolean dialogOnScreen;
	public int restoreAnimateCounter = 0;
	public int swipeRestoreAnimCounter = 0;
	public int setReminderOffCounter = 0;
	public boolean swipeAreaListenerUsed;
	private CounterService mService;
	private boolean mBound = false;
	private int screenOrientation;
	private boolean turnedOff = true;
	private static boolean isOnVisible;
	private boolean smallTitle = false;
	private boolean stopTimerInServiceConnectedAfterPause = false;
	private String swipeWork, swipeRest, swipeWorkLand, swipeRestLand;
	private RelativeLayout rootLayout;
	private Resources resources;
	private TextView activityTitle;
	private TextView description;
	private PowerManager powerManager;
	private CharSequence titleForRestore, titleSequence;
	private int fullSwipeLength;
	private long storedPeriodEndTime = 0;
	private float swipeStartX, swipeStartY;
	private boolean mResolvingError = false;
	private Toolbar toolBar;
	private boolean animateInfo;
	private int buildNumber;
	private int[] colorIds;
	private String[] titleColors, titlePowerColors;
	private int abHeight;
	private int runnableAnimationIndex, titleChangeId;
	private Handler myOffMainThreadHandler;
	private AudioManager am;
	private int lastColorValue = 25;
	private int colorRest, colorWork, colorBlack, colorRed, colorWhite, colorInactive;
	private TextView swipeArea;
	private Button infoButton;
	private Typeface font, titleFont, swipeFont, timerFont;
	private RelativeLayout timerButtonLayout;
	private Button extendPeriodEnd;
	private TextView timerHour1, timerMinute1, timerSecond1, timerHour2, timerSecond2, timerMinute2, colon, point;
	private CounterBinder binder;
	private int turnOffValue=0;
	private boolean turnOffFirstIntent;
	private NotificationManagerCompat mgr;
	private String debug = "MAIN_ACTIVITY";
	private boolean wearOn = false;

	private ValueAnimator bgAnimation;

	private GoogleApiClient mGoogleApiClient;

	private Node connectedNode;


	//Request code for launching the Intent to resolve Google Play services errors.
	private static final int REQUEST_RESOLVE_ERROR = 1000;

	//for testing purposes only. remove before release
	//boolean isOngoingNotificationOn;
	//private NotificationReceiver nReceiver;


	DialogFragment introFragment, extendFragment;

	private static class MyHandler extends Handler {
		private final WeakReference<MainActivity> mActivity;

		public MyHandler(MainActivity activity) {
			mActivity = new WeakReference<MainActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			MainActivity activity = mActivity.get();
			if (activity != null) {
				// ...
			}
		}
	}

	/*
	private OnLongClickListener longClick = new OnLongClickListener(){
		@Override
		public boolean onLongClick (View v){
			mUpdateHandler.removeCallbacks(mUpdateRunnable);
			typeForPowerDown = periodType;
			stopReminder();
			animatePowerDown();
	        if(animateInfo){
	        	mUpdateHandler.postDelayed(mUpdateRunnable, 10000);
	        }
			return true;
		}
	};
	

	
	
    private boolean isDialogOpen(){
    	if (introFragment!=null){
    		if (introFragment.isVisible()){
    			return true;
    		}
    	} else if(extendFragment != null){
    		if(extendFragment.isVisible()){
    			return true;
    		}
    	}
    	return false;
    }

    	*/


	//Binding MainActivity to CounterService
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className,
									   IBinder service) {
			binder = (CounterBinder) service;
			mService = binder.getService();
			mBound = true;
			Bundle data = getDataFromService();
			periodType = data.getInt(RReminder.PERIOD_TYPE);
			extendCount = data.getInt(RReminder.EXTEND_COUNT);
			counterTimeValue = data.getLong(RReminder.COUNTER_TIME_VALUE);
			periodEndTimeValue = data.getLong(RReminder.PERIOD_END_TIME);
			long timeRemaining = mService.getCounterTimeValue();
			//checking for special case where the reminder was turned off from notification bar after the the app was removed from active task list
			Log.d(debug, "onServiceConnected");
			if(getIntent().hasExtra(RReminder.TURN_OFF)){
				if(getIntent().getAction().equals(RReminder.ACTION_TURN_OFF) && periodEndTimeValue == getIntent().getExtras().getLong(RReminder.PERIOD_END_TIME)){
					turnOffFirstIntent = true;
				}

			}
			//depending on intent turnoff value, either stop the active mode and close the activity, or continue with setting up UI or redirceting to notification
			if(turnOffValue==1 || turnOffFirstIntent){
				setReminderOff(periodEndTimeValue);
				finish();
			} else {
					turnOffIntent = false;
				//If the activity was opened while the countdown was already finished(manual mode), jump to NotificationActivity
				if (RReminder.getMode(MainActivity.this) == 1 && timeRemaining < 0 && !getIntent().getAction().equals(RReminder.ACTION_TURN_OFF)) {
					startNotificationActivity(periodType, extendCount, periodEndTimeValue, false);
				} else {
					if(getVisibleState()){
						manageUI(true);
						if (dialogOnScreen || stopTimerInServiceConnectedAfterPause) {
							manageTimer(false);
							if (stopTimerInServiceConnectedAfterPause)
								stopTimerInServiceConnectedAfterPause = false;
						} else {
							manageTimer(true);
						}
					}

				}
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;


	public void startNotificationActivity(@RReminder.PeriodType int type, int extendCount, long periodEndTimeValue, boolean redirectScreenOff) {
		Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
		intent.putExtra(RReminder.PERIOD_TYPE, type);
		intent.putExtra(RReminder.PERIOD_END_TIME, periodEndTimeValue);
		intent.putExtra(RReminder.EXTEND_COUNT, extendCount);
		intent.putExtra(RReminder.PLAY_SOUND, false);
		intent.putExtra(RReminder.REDIRECT_SCREEN_OFF, redirectScreenOff);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.setAction(RReminder.ACTION_VIEW_NOTIFICATION_ACTIVITY);
		startActivity(intent);
	}


	public Bundle getDataFromService() {
		if (mBound) {
			return mService.getData();
		}
		return null;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(debug, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		String intentAction = getIntent().getAction();
		toolBar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolBar);
		buildNumber = Build.VERSION.SDK_INT;
		swipeWork = getString(R.string.swipe_area_work);
		swipeRest = getString(R.string.swipe_area_rest);
		swipeWorkLand = getString(R.string.swipe_area_text_land_work);
		swipeRestLand = getString(R.string.swipe_area_text_land_rest);
		mgr = NotificationManagerCompat.from(getApplicationContext());

		if (savedInstanceState != null) {
			dialogOnScreen = savedInstanceState.getBoolean("dialogOnScreen");

		}

		//for testing purposes only. remove before release
		/*nReceiver = new NotificationReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.colormindapps.rest_reminder_alarm.NOTIFICATION_LISTENER_EXAMPLE");
		registerReceiver(nReceiver, filter);
		*/

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(Wearable.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();

		setUpNotificationChannels();

	}


	@Override
	protected void onStart() {
		super.onStart();


		if (!mResolvingError) {
			mGoogleApiClient.connect();
		}

		//setting the pre-existing (before getting the current value from counterservice) value of periodEndTime
		storedPeriodEndTime = periodEndTimeValue;

		powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		resources = getResources();
		rootLayout = (RelativeLayout) findViewById(R.id.mainActivityLayout);

		colorWork = ContextCompat.getColor(MainActivity.this,R.color.work);
		colorRest = ContextCompat.getColor(MainActivity.this,R.color.rest);
		colorBlack = ContextCompat.getColor(MainActivity.this,R.color.black);
		colorRed = ContextCompat.getColor(MainActivity.this,R.color.red);
		colorWhite = ContextCompat.getColor(MainActivity.this,R.color.white);
		colorInactive = ContextCompat.getColor(MainActivity.this,R.color.inactive_digit);

		abHeight = toolBar.getHeight();

		titleColors = resources.getStringArray(R.array.titleAnimationColors);
		titlePowerColors = resources.getStringArray(R.array.titlePowerUpDownColors);

		screenOrientation = resources.getConfiguration().orientation;

		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		font = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Lt.otf");
		titleFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-ThCn.otf");
		swipeFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-UltLt.otf");
		timerFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Lt.otf");

		activityTitle = (TextView) findViewById(R.id.period_title);

		if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE && buildNumber <= Build.VERSION_CODES.HONEYCOMB) {
			smallTitle = true;
		}

		description = (TextView) findViewById(R.id.description_text);
		extendPeriodEnd = (Button) findViewById(R.id.button_period_end_extend);

		swipeArea = (TextView) findViewById(R.id.swipe_area_text);
		swipeAreaListenerUsed = false;
		infoButton = (Button) findViewById(R.id.info_button);
		
		/*
		if(buildNumber >= Build.VERSION_CODES.HONEYCOMB){
			rootLayout.setPadding(0,actionBar.getHeight(),0,0);
		}
		*/


		extendPeriodEnd.setTypeface(font);
		swipeArea.setTypeface(swipeFont);
		activityTitle.setTypeface(titleFont);
		description.setTypeface(font);


		description.setText("");

		/*
			if (screenOrientation == Configuration.ORIENTATION_PORTRAIT){
				if(isTablet()){
					swipeArea.setBackgroundResource(R.drawable.swipe_idle_tablet);
				} else {
					swipeArea.setBackgroundResource(R.drawable.swipe_idle);
				}
				
			} else if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE){
				if(isTablet()){
					swipeArea.setBackgroundResource(R.drawable.swipe_idle_land_tablet);
				} else {
					swipeArea.setBackgroundResource(R.drawable.swipe_idle_land);
				}
			}
			*/
			
			
			/*
			if (screenOrientation == Configuration.ORIENTATION_PORTRAIT){
					Bitmap swipeBitmap = BitmapFactory.decodeResource(context.getResources(),
	                        R.drawable.swipe_test);
					BitmapDrawable swipeBmDr = new BitmapDrawable(resources, swipeBitmap);
					swipeBmDr.setTileModeX(Shader.TileMode.REPEAT);
					swipeArea.setBackgroundDrawable(swipeBmDr);
				
			} else if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE){
				Bitmap swipeBitmap = BitmapFactory.decodeResource(context.getResources(),
	                    R.drawable.swipe_land_test);
				BitmapDrawable swipeBmDr = new BitmapDrawable(resources, swipeBitmap);
				swipeBmDr.setTileModeY(Shader.TileMode.REPEAT);
				swipeArea.setBackgroundDrawable(swipeBmDr);
			}
		 	*/


		setVisibleState(true);
		//dismissExtendDialog();
		if (RReminderMobile.isCounterServiceRunning(MainActivity.this)) {
			turnedOff = false;
		}


		rootLayout.setBackgroundColor(colorBlack);
		myOffMainThreadHandler = new MyHandler(this);

		manageUiOnStart();

	}

	@Override
	protected void onResume() {
		super.onResume();
		//removing the flag for special case of serviceconnected after pause to resume
		stopTimerInServiceConnectedAfterPause = false;
		//A workaround for screen-off-orientation-change bug to imitate onstart by binding to service
		manageUiOnResume();


		if (RReminder.dismissDialogs(MainActivity.this)) {
			dismissExtendDialog();
			RReminder.removeDismissDialogFlag(MainActivity.this);
		}

		SharedPreferences sharedPref = getSharedPreferences(RReminder.PRIVATE_PREF, Context.MODE_PRIVATE);
		int currentVersionNumber = 0;
		boolean eulaAccepted = sharedPref.getBoolean(RReminder.EULA_ACCEPTED, false);
		int savedVersionNumber = sharedPref.getInt(RReminder.VERSION_KEY, 0);
		animateInfo = sharedPref.getBoolean(RReminder.ANIMATE_INFO, true);
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
			currentVersionNumber = pi.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!eulaAccepted || currentVersionNumber > savedVersionNumber) {
			showIntroductionDialog();
			Editor editor = sharedPref.edit();
			editor.putInt(RReminder.VERSION_KEY, currentVersionNumber);
			editor.putBoolean(RReminder.EULA_ACCEPTED, false);
			editor.apply();
		}

		Calendar current = Calendar.getInstance();
		current.add(Calendar.SECOND, 30);


		/*
		if(mBound){
			counterTimeValue = mService.getCounterTimeValue();
			manageTimer(true);
			manageUI(true);
		} else {
			manageTimer(false);
			manageUI(false);

		}
		*/
	}

	@Override
	protected void onPause() {
		super.onPause();
		//setting up a flag for special case in manual mode, when onPause, onStop is immediately called after onStart, onResume
		stopTimerInServiceConnectedAfterPause = true;
		if (RReminderMobile.isCounterServiceRunning(MainActivity.this)) {
			stopCountDownTimer();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (!mResolvingError && (mGoogleApiClient != null) && (mGoogleApiClient.isConnected())) {

			Wearable.DataApi.removeListener(mGoogleApiClient, this);
			Wearable.MessageApi.removeListener(mGoogleApiClient, this);
			Wearable.CapabilityApi.removeListener(mGoogleApiClient, this);

			mGoogleApiClient.disconnect();
		}

		setVisibleState(false);
		//stopCountDownTimer();
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}

		//releasing UI elements
		rootLayout = null;
		infoButton = null;
		font = null;
		titleFont = null;
		swipeFont = null;
		myOffMainThreadHandler = null;
		activityTitle = null;
		description = null;
		powerManager = null;
		extendPeriodEnd = null;
		if(swipeArea !=null){
			swipeArea.setOnTouchListener(null);
			swipeArea.setBackgroundResource(0);
		}

		if (timerButtonLayout != null) {
			timerButtonLayout.setBackgroundResource(0);
			timerButtonLayout.setOnTouchListener(null);
			timerButtonLayout = null;
			timerHour1 = null;
			timerMinute1 = null;
			timerSecond1 = null;
			timerHour2 = null;
			timerMinute2 = null;
			timerSecond2 = null;
			colon = null;
			point = null;
		}

		titleColors = null;
		titlePowerColors = null;
		resources = null;
		am = null;

	}

	private void setUpNotificationChannels(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			List<NotificationChannel> notifChannels =new ArrayList<>();
			notifChannels.add(RReminder.createNotificationChannel(this.getApplicationContext(),RReminder.NOTIFICATION_CHANNEL_PERIOD_END));
			notifChannels.add(RReminder.createNotificationChannel(this.getApplicationContext(),RReminder.NOTIFICATION_CHANNEL_ONGOING));


			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannels(notifChannels);
		}
	}


	@Override
	public void onUserInteraction() {
		super.onUserInteraction();

	}

	@TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
	private void manageUiOnStart(){
		if(buildNumber>=Build.VERSION_CODES.KITKAT_WATCH){
			if (RReminderMobile.isCounterServiceRunning(MainActivity.this) && powerManager.isInteractive()) {
				Intent intent = new Intent(this, CounterService.class);
				bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			} else {
				manageUI(false);
			}
		} else {
			if (RReminderMobile.isCounterServiceRunning(MainActivity.this) && powerManager.isScreenOn()) {
				Intent intent = new Intent(this, CounterService.class);
				bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			} else {
				manageUI(false);
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
	private void manageUiOnResume(){
		if(buildNumber >= Build.VERSION_CODES.KITKAT_WATCH){
			if (RReminderMobile.isCounterServiceRunning(MainActivity.this) && powerManager.isInteractive()) {
				if(!mBound) {
					Intent intent = new Intent(this, CounterService.class);
					bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
				} else {
					resumeCounter(false);
				}
			}
		} else {
			if (RReminderMobile.isCounterServiceRunning(MainActivity.this) && powerManager.isScreenOn()) {
				if(!mBound){
					Intent intent = new Intent(this, CounterService.class);
					bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
				} else {
					resumeCounter(false);
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


	private void setReminderOn() {
		Log.d(debug, "setReminderOn");
		periodType = 1;
		long mCalendar = RReminder.getNextPeriodEndTime(MainActivity.this, periodType, Calendar.getInstance().getTimeInMillis(), 1, 0L);
		new MobilePeriodManager(getApplicationContext()).setPeriod(periodType, mCalendar, 0, false);
		RReminderMobile.startCounterService(MainActivity.this, 1, 0, mCalendar, false);

		updateReminderStatus(RReminder.DATA_API_SOURCE_MOBILE,periodType,mCalendar,0, true, wearOn);
		updateWearPreferences();
		if(bgAnimation!=null){
			bgAnimation.removeAllUpdateListeners();
		}
		bgAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),colorBlack, colorWork);
		bgAnimation.setDuration(200);
		bgAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				rootLayout.setBackgroundColor((int) animator.getAnimatedValue());
			}

		});
		bgAnimation.start();


		Intent intent = new Intent(this, CounterService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

		turnedOff = false;


	}

	/*
	public void setReminderOffFromOutside() {
		setReminderOff();
	}
	*/

	private void setReminderOff(long periodEndTime) {
		Log.d(debug, "setReminderOff");
		setReminderOffCounter++;
		stopCountDownTimer();
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		Log.d(debug, "SET_REMINDER_OFF period values: type: "+ periodType + ", endtime: "+ periodEndTime);
		RReminderMobile.cancelCounterAlarm(getApplicationContext(), periodType, extendCount, periodEndTime, false,0L);
		cancelNotification(periodEndTimeValue,true);
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		RReminderMobile.stopCounterService(MainActivity.this, periodType);
		if (isOnVisible) {
			manageUI(false);
		}
		turnedOff = true;
		periodType = 0;
		storedPeriodEndTime = 0;
		periodEndTimeValue = 0;

		updateReminderStatus(RReminder.DATA_API_SOURCE_MOBILE,periodType,0L,0, false, false);
	}


	public void setVisibleState(boolean state) {
		isOnVisible = state;
	}

	public static boolean getVisibleState() {
		return isOnVisible;
	}

	@Override
	public void updateWearStatus(int type, long periodEndTimeValue, int extendCount, boolean mobileOn){
		updateReminderStatus(RReminder.DATA_API_SOURCE_MOBILE,type, periodEndTimeValue,extendCount, mobileOn, wearOn);
	}

	private void updateReminderStatus(int updateSource,int type, long periodEndValue, int extendCount, boolean mobileOn, boolean wearOn){
		Log.d(debug, "updating Data API. type: "+type + ", periodEndValue: "+ periodEndValue+ ", extendCount: "+extendCount );
		PendingResult<DataApi.DataItemResult> pendingResult =
				Wearable.DataApi.putDataItem(mGoogleApiClient,RReminder.createStatusData(updateSource,type,periodEndValue,extendCount, mobileOn, wearOn));

	}

	private void updateWearPreferences(){
		Context context  = getApplicationContext();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(RReminder.DATA_API_REMINDER_PREFERENCES_PATH);
		putDataMapRequest.getDataMap().putString(RReminder.WEAR_PREF_WORK_LENGTH,prefs.getString(context.getString(R.string.pref_work_period_length_key),RReminder.DEFAULT_WORK_PERIOD_STRING));
		putDataMapRequest.getDataMap().putString(RReminder.WEAR_PREF_REST_LENGTH,prefs.getString(context.getString(R.string.pref_rest_period_length_key),RReminder.DEFAULT_REST_PERIOD_STRING));
		putDataMapRequest.getDataMap().putInt(RReminder.WEAR_PREF_EXTEND_LENGTH,prefs.getInt(context.getString(R.string.pref_period_extend_length_key),RReminder.DEFAULT_EXTEND_BASE_LENGTH));
		PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
		PendingResult<DataApi.DataItemResult> pendingResult =
				Wearable.DataApi.putDataItem(mGoogleApiClient,putDataRequest);
	}

	@Override
	public void onDataChanged(DataEventBuffer dataEvents) {
		Log.d(debug, "onDataChanged: " + dataEvents);

		for (DataEvent event : dataEvents) {
			if (event.getType() == DataEvent.TYPE_CHANGED) {
				DataItem item = event.getDataItem();

				if(item.getUri().getPath().compareTo(RReminder.DATA_API_REMINDER_STATUS_PATH)==0){
					DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
					wearOn = dataMap.getBoolean(RReminder.DATA_API_WEAR_ON);
					Log.d(debug, "ONDATACHANGED reminder status values, sources ID: " + dataMap.getInt(RReminder.DATA_API_SOURCE) + ", type: "+ dataMap.getInt(RReminder.PERIOD_TYPE)+ ", periodEndTime: "+
					dataMap.getLong(RReminder.PERIOD_END_TIME)+ ", extend count: "+ dataMap.getInt(RReminder.EXTEND_COUNT)+ ", MOBILE_ON: "+ dataMap.getBoolean(RReminder.DATA_API_MOBILE_ON)+ ", WEAR_ON: "+ dataMap.getBoolean(RReminder.DATA_API_WEAR_ON));
					if(dataMap.getInt(RReminder.DATA_API_SOURCE)==RReminder.DATA_API_SOURCE_WEAR){
						int periodType = dataMap.getInt(RReminder.PERIOD_TYPE);
						long periodEndTime = dataMap.getLong(RReminder.PERIOD_END_TIME);
						int extendCount = dataMap.getInt(RReminder.EXTEND_COUNT);
						boolean wearOn = dataMap.getBoolean(RReminder.DATA_API_WEAR_ON);
						updateMobileReminder(periodType, periodEndTime, extendCount, wearOn);
						// updating reminder status with mobile is on, except when update from wear comes about turning off reminder
						if(periodType!=0){
							updateReminderStatus(RReminder.DATA_API_SOURCE_LINKED_MOBILE_ON,periodType, periodEndTime, extendCount, true, wearOn);
						}


					} else if(dataMap.getInt(RReminder.DATA_API_SOURCE)==RReminder.DATA_API_SOURCE_LINKED_WEAR_ON){
						Log.d(debug, "mobile app is notified that wear app is up and running" );
						wearOn = dataMap.getBoolean(RReminder.DATA_API_WEAR_ON);
					}
				}

			}
		}
	}

	public void updateMobileReminder(int updatedType, long updatedPeriodEndTime, int updatedExtendCount, boolean wearOn){

		//TODO:
		//1. cancel current alarm
		//2. update service
		//3. launch new alarm
		//4. update UI

		this.wearOn = wearOn;

		Log.d(debug, "updateMobileReminder");
		Log.d(debug, "extend count value: "+updatedExtendCount);

		//check if countdown wasn 't stopped
		if(updatedType!=0) {
			stopCountDownTimer();
			Log.d(debug, "updatedType: "+ updatedType);
			if (mBound) {
				unbindService(mConnection);
				mBound = false;
			}

			//call to cancel already running period countdown on mobile in order to set up new period with values from wear device
			if(periodType!=0){
				RReminderMobile.cancelCounterAlarm(MainActivity.this.getApplicationContext(), periodType, extendCount,periodEndTimeValue, false,0L);
			}


			new MobilePeriodManager(MainActivity.this.getApplicationContext()).setPeriod(updatedType, updatedPeriodEndTime, updatedExtendCount, false);
			RReminderMobile.startCounterService(MainActivity.this.getApplicationContext(), updatedType, updatedExtendCount, updatedPeriodEndTime, false);

			Intent intent = new Intent(this, CounterService.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		} else {
			setReminderOff(periodEndTimeValue);
		}

	}

	private void getData(final String pathToContent) {
		Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
			@Override
			public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
				String nodeID;
				final String nodeSource;
				if (connectedNode != null){
					nodeID = connectedNode.getId();
					nodeSource = "connected";
				} else {
					nodeID = getLocalNodeResult.getNode().getId();
					nodeSource = "local";
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
											Log.d(debug, "node source: "+ nodeSource);

											//update main activity with google api data only if its not outdated and if countdown on mobile isnt already running
											if(!RReminderMobile.isCounterServiceRunning(MainActivity.this.getApplicationContext()) && data.getInt(RReminder.DATA_API_SOURCE)!= RReminder.DATA_API_SOURCE_MOBILE && data.getLong(RReminder.PERIOD_END_TIME) > Calendar.getInstance().getTimeInMillis()) {
												Log.d(debug, "update UI with google api data upon connecting to google api");
												int periodType = data.getInt(RReminder.PERIOD_TYPE);
												long periodEndTime = data.getLong(RReminder.PERIOD_END_TIME);
												int extendCount = data.getInt(RReminder.EXTEND_COUNT);
												boolean wearOn = data.getBoolean(RReminder.DATA_API_WEAR_ON);
												updateMobileReminder(periodType, periodEndTime, extendCount, wearOn);
												//update DATA API node, that mobile is also on
												updateReminderStatus(RReminder.DATA_API_SOURCE_LINKED_MOBILE_ON,periodType, periodEndTime, extendCount, true, wearOn);
											}
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


	public void manageUI(Boolean isOn) {
		//swipeArea.setOnFocusChangeListener(swipeFocus);

		/*
		endCurrentPeriodSwipeArea.setOnTouchListener(new OnTouchListener() {
	         @Override
	         public boolean onTouch(final View view, final MotionEvent event) {
	            return mDetector.onTouchEvent(event);
	         }
	      });
	      
	      */

		if (isOn) {
			activityTitle.setTextColor(colorBlack);
			toolBar.setTitleTextColor(colorBlack);

			switch (periodType) {
				case 1:
					activityTitle.setText(getString(R.string.on_work_period).toUpperCase(Locale.ENGLISH));
					rootLayout.setBackgroundColor(colorWork);
					infoButton.setTextColor(colorWork);
					description.setText("");
					if(RReminder.isPortrait(this)){
						swipeArea.setText(getString(R.string.swipe_area_text,swipeRest));
					} else {
						swipeArea.setText(swipeRestLand);
					}
					break;
				case 2:
					activityTitle.setText(getString(R.string.on_rest_period).toUpperCase(Locale.ENGLISH));
					rootLayout.setBackgroundColor(colorRest);
					infoButton.setTextColor(colorRest);
					description.setText("");
					if(RReminder.isPortrait(this)){
						swipeArea.setText(getString(R.string.swipe_area_text,swipeWork));
					} else {
						swipeArea.setText(swipeWorkLand);
					}

					break;
				case 3:
					if (extendCount > 3) {
						rootLayout.setBackgroundColor(colorRed);
						infoButton.setTextColor(colorRed);
					} else {
						rootLayout.setBackgroundColor(colorWork);
						infoButton.setTextColor(colorWork);
					}
					activityTitle.setText(getString(R.string.on_work_period).toUpperCase(Locale.ENGLISH));
					if(RReminder.isPortrait(this)){
						swipeArea.setText(getString(R.string.swipe_area_text,swipeRest));
					} else {
						swipeArea.setText(swipeRestLand);
					}
					if (extendCount <= 1) {
						description.setText(getString(R.string.description_extended_one_time));
					} else {
						description.setText(String.format(getString(R.string.description_extended),extendCount));
					}
					break;
				case 4:
					if (extendCount > 3) {
						rootLayout.setBackgroundColor(colorRed);
						infoButton.setTextColor(colorRed);
					} else {
						rootLayout.setBackgroundColor(colorRest);
						infoButton.setTextColor(colorRest);
					}
					activityTitle.setText(getString(R.string.on_rest_period).toUpperCase(Locale.ENGLISH));
					if(RReminder.isPortrait(this)){
						swipeArea.setText(getString(R.string.swipe_area_text,swipeWork));
					} else {
						swipeArea.setText(swipeWorkLand);
					}
					if (extendCount <= 1) {
							description.setText(getString(R.string.description_extended_one_time));
					} else {
							description.setText(String.format(getString(R.string.description_extended),extendCount));
					}
					break;
				default:
					activityTitle.setText("No title".toUpperCase(Locale.ENGLISH));
					rootLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.blue));
					break;

			}

			titleSequence = activityTitle.getText();
			activityTitle.setTextSize(RReminder.adjustTitleSize(MainActivity.this, titleSequence.length(), smallTitle));


			if (RReminder.isExtendEnabled(MainActivity.this)) {
				extendPeriodEnd.setVisibility(View.VISIBLE);
				if(RReminder.getExtendOptionsCount(MainActivity.this)==1){
					int extendBaseLength = RReminder.getExtendBaseLength(MainActivity.this);
					extendPeriodEnd.setText(getString(R.string.extend_period_one_option,extendBaseLength));
				} else {
					extendPeriodEnd.setText(getString(R.string.extend_current_period));
				}
			} else {
				extendPeriodEnd.setVisibility(View.INVISIBLE);
			}

			if (RReminder.isEndPeriodEnabled(MainActivity.this)) {
				if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
					if (isTablet()) {
						swipeArea.setBackgroundResource(R.drawable.swipe_idle_tablet);
					} else {
						swipeArea.setBackgroundResource(R.drawable.swipe_idle);
					}

				} else if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {

					if (isTablet()) {
						swipeArea.setBackgroundResource(R.drawable.swipe_idle_land_tablet);
					} else {
						swipeArea.setBackgroundResource(R.drawable.swipe_idle_land);
					}

				}

				swipeArea.setVisibility(View.VISIBLE);
				/*
				if (buildNumber >= Build.VERSION_CODES.HONEYCOMB) {
					if(screenOrientation == Configuration.ORIENTATION_PORTRAIT){
						requiredSwipeDistance = rootLayout.getMeasuredWidth();
					} else {
						requiredSwipeDistance = rootLayout.getMeasuredHeight()- abHeight;
					}
					fullSwipeLength = requiredSwipeDistance * 6 / 10;
				} else {
					if(screenOrientation == Configuration.ORIENTATION_PORTRAIT){
			            rootLayout.postDelayed(new Runnable() {
			                @Override
			                public void run() {
			                	requiredSwipeDistance = rootLayout.getMeasuredWidth();
			                	fullSwipeLength = requiredSwipeDistance * 6 / 10;
			                }
			            }, 200);						
					} else {
			            rootLayout.postDelayed(new Runnable() {
			                @Override
			                public void run() {
			                	requiredSwipeDistance = rootLayout.getMeasuredHeight();
			                	fullSwipeLength = requiredSwipeDistance * 6 / 10;
			                }
			            }, 200);						
					}
				}*/


				swipeArea.setOnTouchListener(new SwipeTouchListener());

			} else {
				swipeArea.setVisibility(View.GONE);
			}


		} else {
			toolBar.setTitleTextColor(colorWhite);
			activityTitle.setTextColor(colorWhite);
			activityTitle.setText(getString(R.string.reminder_off_title));
			description.setText("");
			titleSequence = activityTitle.getText();
			activityTitle.setTextSize(RReminder.adjustTitleSize(MainActivity.this, titleSequence.length(), smallTitle));
			infoButton.setTextColor(colorWhite);
			extendPeriodEnd.setVisibility(View.INVISIBLE);
			rootLayout.setBackgroundColor(colorBlack);
			swipeArea.setVisibility(View.GONE);
			manageTimer(false);

		}
	}
	
	/*
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
 
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager.isScreenOn()){
            switch(newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                setContentView(R.layout.activity_main_land);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
            	setContentView(R.layout.activity_main);
                break;
        }
        
    		if (RReminder.isCounterServiceRunning(getBaseContext())){
    			manageUI(true);
    		} else {
    			manageUI(false); 
    		}
        
    }
	}
	
	*/

	public void manageTimer(Boolean isOn) {
		timerButtonLayout = (RelativeLayout) findViewById(R.id.timer_layout);
		timerHour1 = (TextView) findViewById(R.id.timer_hour1);
		timerMinute1 = (TextView) findViewById(R.id.timer_minute1);
		timerSecond1 = (TextView) findViewById(R.id.timer_second1);
		timerHour2 = (TextView) findViewById(R.id.timer_hour2);
		timerMinute2 = (TextView) findViewById(R.id.timer_minute2);
		timerSecond2 = (TextView) findViewById(R.id.timer_second2);
		colon = (TextView) findViewById(R.id.timer_colon);
		point = (TextView) findViewById(R.id.timer_point);


		timerHour1.setTypeface(timerFont);
		timerMinute1.setTypeface(timerFont);
		timerSecond1.setTypeface(timerFont);
		timerHour2.setTypeface(timerFont);
		timerMinute2.setTypeface(timerFont);
		timerSecond2.setTypeface(timerFont);


		if (isOn) {
			countdown = new MyCountDownTimer(getApplicationContext(), counterTimeValue, 1000, timerHour1, timerHour2, colon, timerMinute1, timerMinute2, point, timerSecond1, timerSecond2, false);
			countdown.start();
			timerButtonLayout.setOnTouchListener(new TimerButtonListener());
			timerButtonLayout.setBackgroundResource(R.drawable.btn_timer_idle_np);

		} else {
			timerButtonLayout.setBackgroundResource(R.drawable.btn_timer_np);
			timerButtonLayout.setOnTouchListener(null);
			timerButtonLayout.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					callStartReminder(v);
				}
			});


			String offString = getWorkPeriodLengthString();
			char[] lengthArray;
			lengthArray = offString.toCharArray();
			timerHour1.setText(Character.toString(lengthArray[0]));
			timerHour2.setText(Character.toString(lengthArray[1]));
			timerMinute1.setText(Character.toString(lengthArray[3]));
			timerMinute2.setText(Character.toString(lengthArray[4]));
			timerSecond1.setText(Character.toString(lengthArray[6]));
			timerSecond2.setText(Character.toString(lengthArray[7]));
			if (lengthArray[0] == '0' && lengthArray[1] == '0') {
				timerHour1.setTextColor(colorInactive);
				timerHour2.setTextColor(colorInactive);
			} else {
				timerHour1.setTextColor(colorBlack);
				timerHour2.setTextColor(colorBlack);
				colon.setTextColor(colorBlack);
			}

			timerMinute1.setTextColor(colorBlack);
			timerMinute2.setTextColor(colorBlack);
			point.setTextColor(colorBlack);
			timerSecond1.setTextColor(colorBlack);
			timerSecond2.setTextColor(colorBlack);

			//timerButton.setText(getWorkPeriodLengthString(getBaseContext()));
			stopCountDownTimer();


		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.menu_help) {
			Intent ih = new Intent(this, ManualActivity.class);
			startActivity(ih);
			return true;
		} else if (item.getItemId() == R.id.menu_settings) {
				Intent i = new Intent(this, PreferenceActivity.class);
				startActivity(i);
			return true;
		} else if (item.getItemId() == R.id.menu_feedback){
				Intent Email = new Intent(Intent.ACTION_SEND);
				Email.setType("text/email");
				Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "colormindapps@gmail.com" });
				Email.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
				startActivity(Intent.createChooser(Email, "Send Feedback:"));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void exitApplication(){
		finish();
	}

	public String getWorkPeriodLengthString() {
		String result;
		result = RReminder.getPreferencePeriodLength(MainActivity.this, RReminder.WORK) + ".00";
		return result;
	}

	public void showIntroductionDialog() {


		introFragment = IntroductionDialog.newInstance(
				R.string.intro_title);
		introFragment.show(getSupportFragmentManager(), "introductionDialog");
		dialogOnScreen = true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("currentPeriod", periodType);
		outState.putInt("previousScreenOrientation", screenOrientation);
		outState.putBoolean("dialogOnScreen", dialogOnScreen);
		super.onSaveInstanceState(outState);
	}


	public void cancelNotification(long periodEndTime,boolean removeOnGoing) {
		if (removeOnGoing) {
			mgr.cancel(RReminder.NOTIFICATION_ID);
			return;
		}
		if (RReminder.isActiveModeNotificationEnabled(MainActivity.this)) {

			mgr.notify(1, RReminderMobile.updateOnGoingNotification(MainActivity.this, periodType,periodEndTime, true));
		} else {

			mgr.cancel(1);
		}

	}

	public void stopCountDownTimer() {
		if (countdown != null) {
			countdown.cancel();
			countdown.isRunning = false;
		}
	}

	public void showExtendDialog(View v) {
		if(RReminder.getExtendOptionsCount(MainActivity.this)>1){
			stopCountDownTimer();
			extendFragment = ExtendDialog.newInstance(R.string.extend_dialog_title, periodType, extendCount, periodEndTimeValue, 0);
			extendFragment.show(getSupportFragmentManager(), "extendDialog");
			dialogOnScreen = true;
		} else {
			extendPeriod(RReminder.EXTEND_PERIOD_SINGLE_OPTION,periodType);
		}

	}

	public void extendPeriod(int flag,int functionType){
		stopCountDownTimer();
		long timeRemaining;
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		if(flag == RReminder.EXTEND_PERIOD_SINGLE_OPTION){
			timeRemaining = periodEndTimeValue - Calendar.getInstance().getTimeInMillis();
		} else {
			timeRemaining = 0L;
		}


		RReminderMobile.cancelCounterAlarm(MainActivity.this.getApplicationContext(), periodType, extendCount,periodEndTimeValue, false,0L);

		String toastText;
		if(flag==RReminder.EXTEND_PERIOD_SINGLE_OPTION){
			toastText = getString(R.string.toast_period_end_extended);
		} else {
			toastText = getString(R.string.notification_toast_period_extended);
		}

		long functionCalendar;
		extendCount+=1;
		switch(functionType){
			case 1:  functionType = 3; break;
			case 2:  functionType = 4; break;
			default: break;
		}
		functionCalendar = RReminder.getTimeAfterExtend(MainActivity.this.getApplicationContext(), 1, timeRemaining);
		new MobilePeriodManager(MainActivity.this.getApplicationContext()).setPeriod(functionType, functionCalendar, extendCount, false);
		RReminderMobile.startCounterService(MainActivity.this.getApplicationContext(), functionType, extendCount, functionCalendar, false);

		updateReminderStatus(RReminder.DATA_API_SOURCE_MOBILE,functionType,functionCalendar,extendCount, true, wearOn);

		Intent intent = new Intent(this, CounterService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

		Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
	}

	public void showHintDialog(View v) {

		if (animateInfo) {
			SharedPreferences sharedPref = getSharedPreferences(RReminder.PRIVATE_PREF, Context.MODE_PRIVATE);
			Editor editor = sharedPref.edit();
			editor.putBoolean(RReminder.ANIMATE_INFO, false);
			editor.apply();
			animateInfo = false;
		}
		/*
		hintFragment = HintDialog.newInstance(R.string.hint_dialog_title);
		hintFragment.show(getSupportFragmentManager(), "hintDialog");
		*/

		//replacing hint dialog with showcaseview

		if (RReminderMobile.isCounterServiceRunning(MainActivity.this)) {
			if (RReminder.isExtendEnabled(MainActivity.this)) {
				if (RReminder.isEndPeriodEnabled(MainActivity.this)) {
					createShowcaseView(RReminder.SHOWCASEVIEW_EXTEND, RReminder.SHOWCASEVIEW_FORCE_END);
				} else {
					createShowcaseView(RReminder.SHOWCASEVIEW_EXTEND, RReminder.SHOWCASEVIEW_STOP);
				}

			} else if (RReminder.isEndPeriodEnabled(MainActivity.this)) {
				createShowcaseView(RReminder.SHOWCASEVIEW_FORCE_END, RReminder.SHOWCASEVIEW_STOP);
			} else {
				createShowcaseView(RReminder.SHOWCASEVIEW_STOP, RReminder.SHOWCASEVIEW_MENU);
			}

		} else {
			createShowcaseView(RReminder.SHOWCASEVIEW_START, 0);
		}

	}

	public void createShowcaseView(int showcaseType, int nextShowcaseView) {
		int targetId;
		int titleId;
		int descriptionId;
		Target viewTarget;

		switch (showcaseType) {
			case RReminder.SHOWCASEVIEW_START: {
				targetId = R.id.timer_layout;
				titleId = R.string.showcase_start_title;
				descriptionId = R.string.showcase_start_description;
				break;
			}
			case RReminder.SHOWCASEVIEW_STOP: {
				targetId = R.id.timer_layout;
				titleId = R.string.showcase_stop_title;
				descriptionId = R.string.showcase_stop_description;
				break;
			}
			case RReminder.SHOWCASEVIEW_EXTEND: {
				targetId = R.id.button_period_end_extend;
				titleId = R.string.showcase_extend_title;
				descriptionId = R.string.showcase_extend_description;
				break;
			}
			case RReminder.SHOWCASEVIEW_FORCE_END: {
				targetId = R.id.swipe_area_text;
				titleId = R.string.showcase_force_end_title;
				descriptionId = R.string.showcase_force_end_description;
				break;
			}
			case RReminder.SHOWCASEVIEW_MENU: {
				targetId = R.id.menu_help;
				titleId = R.string.showcase_menu_title;
				descriptionId = R.string.showcase_menu_description;
				break;
			}
			default: {
				targetId = R.id.timer_layout;
				titleId = R.string.showcase_start_title;
				descriptionId = R.string.showcase_start_description;
				break;
			}
		}


		if (showcaseType == RReminder.SHOWCASEVIEW_MENU) {
			View menu3dots;
			if (toolBar != null) {
				// 0 - title
				// 1 - options menu
				View menuLayout = toolBar.getChildAt(1);
				if (menuLayout instanceof ViewGroup) {
					// index here depends on actual toolbar layout
					menu3dots = ((ViewGroup) menuLayout).getChildAt(0);
					viewTarget = new ViewTarget(menu3dots);
				} else {
					viewTarget = new ViewTarget(R.id.timer_layout, this);
				}
			} else {
				viewTarget = new ViewTarget(R.id.timer_layout, this);
			}

		} else {
			viewTarget = new ViewTarget(targetId, this);
		}

		Button customButton = (Button) getLayoutInflater().inflate(R.layout.showcase_custom_button,new LinearLayout(MainActivity.this), false);

		if (nextShowcaseView == 0) {
			customButton.setText(getString(R.string.showcase_close));
		} else {
			customButton.setText(getString(R.string.showcase_next));
		}

		new ShowcaseView.Builder(this)
				.setTarget(viewTarget)
				.setContentTitle(titleId)
				.setContentText(descriptionId)
				.withNewStyleShowcase()
				.setShowcaseEventListener(new MyShowcaseViewListener(nextShowcaseView))
				.setStyle(R.style.CustomShowCaseThemeClose)
				.blockAllTouches()
				.replaceEndButton(customButton)
				.build();
	}

	private class MyShowcaseViewListener implements OnShowcaseEventListener {

		private int showcaseType;

		public MyShowcaseViewListener(int showcase) {
			this.showcaseType = showcase;
		}

		@Override
		public void onShowcaseViewHide(ShowcaseView showcaseView) {

		}

		@Override
		public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
			switch (showcaseType) {
				case RReminder.SHOWCASEVIEW_FORCE_END: {
					createShowcaseView(showcaseType, RReminder.SHOWCASEVIEW_STOP);
					break;
				}
				case RReminder.SHOWCASEVIEW_STOP: {
					createShowcaseView(showcaseType, RReminder.SHOWCASEVIEW_MENU);
					break;
				}
				case RReminder.SHOWCASEVIEW_MENU: {
					createShowcaseView(showcaseType, 0);
					break;
				}
				default:
					break;
			}

		}


		@Override
		public void onShowcaseViewShow(ShowcaseView showcaseView) {
		}
	}


	@Override
	public void cancelNotificationForDialog(long periodEndTime,boolean removeOnGoing) {
		cancelNotification(periodEndTime,removeOnGoing);
	}

	@Override
	public void stopCountDownTimerForDialog() {
		stopCountDownTimer();
	}


	@Override
	public void bindFromFragment(long newPeriodEndTimeValue) {
		dialogOnScreen = false;
		storedPeriodEndTime = newPeriodEndTimeValue;
		Intent intent = new Intent(this, CounterService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void unbindFromFragment() {
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	@Override
	public void resumeCounter(boolean positiveDismissal) {
		dialogOnScreen = false;
		if (mBound && !positiveDismissal) {
			counterTimeValue = mService.getCounterTimeValue();
			manageTimer(true);
		}
	}


	public boolean isTablet() {

		DisplayMetrics displayMetrics = MainActivity.this.getResources().getDisplayMetrics();

		float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
		float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
		return (Math.min(dpHeight,dpWidth) > 600.0f);
	}

	public void swipeEndPeriod() {
		description.setText("");
		stopCountDownTimer();
		RReminderMobile.cancelCounterAlarm(getApplicationContext(), periodType, extendCount, periodEndTimeValue, false,0L);
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}

		long functionCalendar;
		switch (periodType) {
			case RReminder.WORK:
			case RReminder.WORK_EXTENDED:
				periodType = RReminder.REST;
				break;
			case RReminder.REST:
			case RReminder.REST_EXTENDED:
				periodType = RReminder.WORK;
				break;
			default:
				break;
		}
		functionCalendar = RReminder.getNextPeriodEndTime(MainActivity.this, periodType, Calendar.getInstance().getTimeInMillis(), 1, 0L);

		extendCount = 0;
		if (RReminder.isActiveModeNotificationEnabled(MainActivity.this)) {
			mgr.notify(1, RReminderMobile.updateOnGoingNotification(MainActivity.this, periodType,functionCalendar, true));
		}

		new MobilePeriodManager(getApplicationContext()).setPeriod(periodType, functionCalendar, extendCount, false);
		updateReminderStatus(RReminder.DATA_API_SOURCE_MOBILE,periodType, functionCalendar,extendCount, true, wearOn);

		RReminderMobile.startCounterService(MainActivity.this, periodType, 0, functionCalendar, false);
		cancelNotification(functionCalendar,false);
		Intent intent = new Intent(this, CounterService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

		String toastText = getString(R.string.toast_period_end_forced);
		Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
	}


	public void callStartReminder(View v) {
		//animatePowerUp();
		startReminder();
	}

	@Override
	public void dialogIsClosed(boolean eulaAccepted) {

		dialogOnScreen = false;
		if(eulaAccepted){
			SharedPreferences sharedPref = getSharedPreferences(RReminder.PRIVATE_PREF, Context.MODE_PRIVATE);
			Editor editor = sharedPref.edit();
			editor.putBoolean(RReminder.EULA_ACCEPTED, eulaAccepted);
			editor.apply();
		}


	}

	@Override
	public void startReminder() {
		if (!RReminderMobile.isCounterServiceRunning(MainActivity.this)) {
			setReminderOn();
		}
	}

	public void stopReminder() {
		setReminderOff(periodEndTimeValue);
	}


	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Bundle data = intent.getExtras();
		String intentAction = intent.getAction();
		Log.d(debug, "onNewIntent action: "+intentAction);
		Log.d(debug, "wear extend action name: "+RReminder.ACTION_WEAR_NOTIFICATION_EXTEND);

	   /* if(introFragment!=null){
	    	introFragment.dismiss();
	    }
	    if(extendFragment!=null){
	    	extendFragment.dismiss();
	    }
	    
	    if(hintFragment!=null){
	    	hintFragment.dismiss();
	    }

		*/
		if (data != null) {
			Log.d(debug,"intent has data");
			switch(intentAction) {
				//intent after turning off countdown from notification activity
				case RReminder.ACTION_TURN_OFF: {

					turnOffValue = data.getInt(RReminder.TURN_OFF);
					long periodEndTime = data.getLong(RReminder.PERIOD_END_TIME);
					if (turnOffValue == 1) {

						if(RReminder.getMode(getApplicationContext())==1  && !RReminderMobile.isCounterServiceRunning(getApplicationContext())){
							finish();
						} else {
							if(mBound){
								Bundle serviceData = getDataFromService();
								periodType = serviceData.getInt(RReminder.PERIOD_TYPE);
								extendCount = serviceData.getInt(RReminder.EXTEND_COUNT);
								counterTimeValue = serviceData.getLong(RReminder.COUNTER_TIME_VALUE);
								periodEndTimeValue = serviceData.getLong(RReminder.PERIOD_END_TIME);
								setReminderOff(periodEndTimeValue);
								finish();
							}
						}

						//manageUI(false);
						//turnOff = false;

					}
					turnOffIntent = true;
					break;
				}
				//intent after calling next period start from notification activity
				case RReminder.ACTION_MANUAL_START_NEXT_PERIOD: {
					@RReminder.PeriodType int type = data.getInt(RReminder.MANUAL_MODE_NEXT_PERIOD_TYPE);
					long nextPeriodEnd = RReminder.getNextPeriodEndTime(MainActivity.this, RReminder.getNextType(type), Calendar.getInstance().getTimeInMillis(), 1, 0L);


					new MobilePeriodManager(getApplicationContext()).setPeriod(RReminder.getNextType(type), nextPeriodEnd, extendCount,false);
					RReminderMobile.startCounterService(MainActivity.this, RReminder.getNextType(type), 0, nextPeriodEnd, false);
					manageUI(true);
					if (!dialogOnScreen) {
						manageTimer(true);
					}
					break;
				}
				case RReminder.ACTION_WEAR_NOTIFICATION_EXTEND: {
					//dismissing notification after selecting to extend last ended period
					//mgr.cancel(1);
					//code for extending previously ended period
					Log.d(debug, "extend wear notification received");
					periodType = data.getInt(RReminder.PERIOD_TYPE);
					periodEndTimeValue = data.getLong(RReminder.PERIOD_END_TIME);
					int periodToExtend = data.getInt(RReminder.EXTENDED_PERIOD_TYPE);
					extendCount = data.getInt(RReminder.EXTEND_COUNT);
					//extendPeriod(RReminder.EXTEND_PERIOD_WEAR, periodToExtend);
					break;
				}
				case RReminder.ACTION_VIEW_MAIN_ACTIVITY:{
					if(RReminder.isActiveModeNotificationEnabled(this)){
						if(mBound){
							Bundle serviceData = getDataFromService();
							periodType = serviceData.getInt(RReminder.PERIOD_TYPE);
							periodEndTimeValue = serviceData.getLong(RReminder.PERIOD_END_TIME);
						} else {
							periodType = data.getInt(RReminder.PERIOD_TYPE);
						}
						mgr.notify(1, RReminderMobile.updateOnGoingNotification(this, periodType,periodEndTimeValue, true));
					}
					break;
				}
				default:
					break;
			}
		}

		// go on with smth else
	}


	float mLastTouchX, mLastTouchY, startX, startY;
	private int mActivePointerId;

	private class TimerButtonListener implements OnTouchListener {

		CountDownTimer timer;
		int[] colorArray = new int[50];
		int powerDownType;
		boolean turnedOff = false;
		int frames;
		CharSequence tempTitle;

		@Override
		public boolean onTouch(View v, MotionEvent ev) {
			final int action = MotionEventCompat.getActionMasked(ev);


			switch (action) {
				case MotionEvent.ACTION_DOWN: {

					timerButtonLayout.setBackgroundResource(R.drawable.btn_timer_pressed_np);
					tempTitle = activityTitle.getText();
					switch (periodType) {
						case 1:
						case 3:
							powerDownType = 1;
							break;
						case 2:
						case 4:
							powerDownType = 2;
							break;
						default:
							powerDownType = 1;
							break;
					}

					if (extendCount > 3) {
						for (int i = 0; i < 50; i++) {
							colorArray[i] = getCurrentColorId(3, i);
						}
					} else {
						for (int i = 0; i < 50; i++) {
							colorArray[i] = getCurrentColorId(powerDownType, i);
						}
					}

					timer = new CountDownTimer(1200, 24) {
						int ticks = 0;

						public void onTick(long millisUntilFinished) {
							rootLayout.setBackgroundColor(colorArray[ticks]);
							if (ticks == 7) {

								activityTitle.setText(getString(R.string.turning_off_text));
								titleSequence = activityTitle.getText();
								activityTitle.setTextSize(RReminder.adjustTitleSize(MainActivity.this, titleSequence.length(), smallTitle));
								activityTitle.setTextColor(colorWhite);
							}
							//activityTitle.setTextColor(Color.parseColor(titlePowerColors[49-ticks]));
							ticks++;
							frames = ticks;
						}

						public void onFinish() {
							stopReminder();
							turnedOff = true;
						}
					}.start();
					break;
				}

				case MotionEvent.ACTION_UP: {
					timerButtonLayout.setBackgroundResource(R.drawable.btn_timer_idle_np);
					timer.cancel();
					if (!turnedOff) {
						activityTitle.setText(tempTitle);
						activityTitle.setTextColor(colorBlack);
						titleSequence = activityTitle.getText();
						activityTitle.setTextSize(RReminder.adjustTitleSize(MainActivity.this, titleSequence.length(), smallTitle));
						animatePowerDownRestore(frames);
					}
					frames = 0;
					break;
				}

				case MotionEvent.ACTION_CANCEL: {
					activityTitle.setText(tempTitle);
					titleSequence = activityTitle.getText();
					activityTitle.setTextSize(RReminder.adjustTitleSize(MainActivity.this, titleSequence.length(), smallTitle));
					activityTitle.setTextColor(colorBlack);
					timerButtonLayout.setBackgroundResource(R.drawable.btn_timer_idle_np);
					timer.cancel();
					frames = 0;

					break;
				}

				default:
					break;
			}

			return true;
		}
	}

	private class SwipeTouchListener implements OnTouchListener {
		Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


		private boolean readyToLaunch = false;
		boolean requirementReached = false;
		CharSequence tempDescription;


		@Override
		public boolean onTouch(View v, MotionEvent ev) {
			swipeAreaListenerUsed = true;
			final int action = MotionEventCompat.getActionMasked(ev);


			switch (action) {
				case MotionEvent.ACTION_DOWN: {


					if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
						if (isTablet()) {
							swipeArea.setBackgroundResource(R.drawable.swipe_pressed_tablet);
						} else {
							swipeArea.setBackgroundResource(R.drawable.swipe_pressed);
						}
						requiredSwipeDistance = rootLayout.getMeasuredWidth();
					} else if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
						if (isTablet()) {
							swipeArea.setBackgroundResource(R.drawable.swipe_pressed_land_tablet);
						} else {
							swipeArea.setBackgroundResource(R.drawable.swipe_pressed_land);
						}
						requiredSwipeDistance = rootLayout.getMeasuredHeight() - abHeight;

					}

					fullSwipeLength = requiredSwipeDistance * 6 / 10;


					tempDescription = description.getText();
					final int pointerIndex = MotionEventCompat.getActionIndex(ev);
					final float x = MotionEventCompat.getX(ev, pointerIndex);
					final float y = MotionEventCompat.getY(ev, pointerIndex);
					mLastTouchX = startX = swipeStartX = x;
					swipeStartX = x;
					mLastTouchY = startY = swipeStartY = y;
					swipeStartY = y;
					titleForRestore = activityTitle.getText();

					mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
					break;
				}

				case MotionEvent.ACTION_MOVE: {

					final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
					final float x = MotionEventCompat.getX(ev, pointerIndex);
					final float y = MotionEventCompat.getY(ev, pointerIndex);
					int currentColorId;

					float colorMultiplierFloat;
					int colorMultiplier;

					final float dx = x - startX;
					final float dy = y - startY;
					final float dDimension;
					final int dxInt = (int)dx;
					final int dyInt = (int)dy;
					if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
						colorMultiplier = dxInt / (fullSwipeLength / 100);
						dDimension = dx;

					} else {
						colorMultiplier = dyInt / (fullSwipeLength / 100);
						dDimension = dy;
					}


					if (colorMultiplier > 99) {
						colorMultiplier = 99;
					}

					if (colorMultiplier < 0) {
						colorMultiplier = 0;
					}

					activityTitle.setTextColor(Color.parseColor(titleColors[colorMultiplier]));

					if (colorMultiplier > 50) {
						//restoreAnimateTitle = activityTitle.getText();
						activityTitle.setText(getSwipeSwapTitle(periodType).toUpperCase(Locale.ENGLISH));
					} else {
						activityTitle.setText(titleForRestore.toString().toUpperCase(Locale.ENGLISH));
					}
					titleSequence = activityTitle.getText();
					activityTitle.setTextSize(RReminder.adjustTitleSize(MainActivity.this, titleSequence.length(), smallTitle));

					switch (periodType) {
						case 1:
						case 3: {
							if (colorMultiplier < 51 && colorMultiplier > 0) {
								if (extendCount < 4) {
									currentColorId = getCurrentColorId(1, colorMultiplier);
									rootLayout.setBackgroundColor(currentColorId);
									infoButton.setTextColor(currentColorId);
								} else {
									currentColorId = getCurrentColorId(3, colorMultiplier);
									rootLayout.setBackgroundColor(currentColorId);
									infoButton.setTextColor(currentColorId);
								}
							} else if (colorMultiplier > 0) {
								currentColorId = getCurrentColorId(2, colorMultiplier);
								rootLayout.setBackgroundColor(currentColorId);
								infoButton.setTextColor(currentColorId);
							}
							break;
						}
						case 2:
						case 4: {

							if (colorMultiplier > 0 && colorMultiplier < 51) {
								if (extendCount < 4) {
									currentColorId = getCurrentColorId(2, colorMultiplier);
									rootLayout.setBackgroundColor(currentColorId);
									infoButton.setTextColor(currentColorId);
								} else {
									currentColorId = getCurrentColorId(3, colorMultiplier);
									rootLayout.setBackgroundColor(currentColorId);
									infoButton.setTextColor(currentColorId);
								}
							} else if (colorMultiplier > 0 && colorMultiplier < 99) {
								currentColorId = getCurrentColorId(1, colorMultiplier);
								rootLayout.setBackgroundColor(currentColorId);
								infoButton.setTextColor(currentColorId);
							}
							break;
						}
						default:
							break;
					}

					if (dDimension > (float) fullSwipeLength) {
						readyToLaunch = true;

						if (am.getRingerMode() != AudioManager.RINGER_MODE_SILENT && !requirementReached) {
							if (RReminder.isVibrateEnabled(MainActivity.this, vib)) {
								// Vibrate for 300 milliseconds
								vib.vibrate(5000);
							}
						}
						requirementReached = true;
						description.setText(getString(R.string.release_text));
					} else {
						description.setText(tempDescription);
						if (requirementReached) {
							vib.cancel();
						}
						requirementReached = false;
						readyToLaunch = false;
					}


					mLastTouchX = x;
					mLastTouchY = y;
					break;
				}

				case MotionEvent.ACTION_UP: {

					if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
						if (isTablet()) {
							swipeArea.setBackgroundResource(R.drawable.swipe_idle_tablet);
						} else {
							swipeArea.setBackgroundResource(R.drawable.swipe_idle);
						}

					} else if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {

						if (isTablet()) {
							swipeArea.setBackgroundResource(R.drawable.swipe_idle_land_tablet);
						} else {
							swipeArea.setBackgroundResource(R.drawable.swipe_idle_land);
						}

					}


					description.setText(tempDescription);
					final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
					final float x = MotionEventCompat.getX(ev, pointerIndex);
					final float y = MotionEventCompat.getY(ev, pointerIndex);
					float z;
					int orientation;
					vib.cancel();
					requirementReached = false;
					mActivePointerId = 0;
					startX = 0;
					startY = 0;
					if (readyToLaunch) {
						swipeEndPeriod();
					} else {
						if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
							z = x;
							orientation = 0;
						} else {
							z = y;
							orientation = 1;
						}

						animateRestore(z, orientation);

						lastColorValue = 25;

						//activityTitle.setText(titleForRestore.toString());
						
						/*
						switch(periodType){
						case 1: case 3:
							if (extendCount < 3){
								rootView.setBackgroundColor(resources.getColor(R.color.work));
								activityTitle.setTextColor(resources.getColor(R.color.work));
							} else {
								rootView.setBackgroundColor(resources.getColor(R.color.red));
								activityTitle.setTextColor(resources.getColor(R.color.red));
							}

							break;
						case 2: case 4:
							if (extendCount < 3){
								rootView.setBackgroundColor(resources.getColor(R.color.rest));
								activityTitle.setTextColor(resources.getColor(R.color.rest));
							} else {
								rootView.setBackgroundColor(resources.getColor(R.color.red));
								activityTitle.setTextColor(resources.getColor(R.color.red));
							}
							break;
						default: break;
						}
						*/
					}
					break;
				}

				case MotionEvent.ACTION_CANCEL: {
					mActivePointerId = 0;
					startX = 0;
					startY = 0;
					readyToLaunch = false;
					break;
				}
			}

			return true;
		}

	}

	public int[] formatColorValue(int[] input) {

		int arrayLength = input.length;

		for (int i = 0; i < arrayLength; i++) {
			if (input[i] < 0)
				input[i] = 0;
			if (input[i] > 255)
				input[i] = 255;
		}


		return input;
	}

	public String getSwipeSwapTitle(@RReminder.PeriodType int type) {
		String output;
		switch (type) {
			case RReminder.WORK:
			case RReminder.WORK_EXTENDED:
				output = getString(R.string.on_rest_period);
				break;
			case RReminder.REST:
			case RReminder.REST_EXTENDED:
				output = getString(R.string.on_work_period);
				break;
			default:
				output = "swap title exception";
				break;
		}
		return output;
	}

	/*
	public void setInfoButtonIdleColors() {
		switch (periodType) {
			case 0: {
				infoButton.setBackgroundColor(ContextCompat.getColor(context,R.color.black));
				infoButton.setTextColor(ContextCompat.getColor(context,R.color.white));
				break;
			}
			case 1: {
				infoButton.setBackgroundColor(ContextCompat.getColor(context,R.color.black));
				infoButton.setTextColor(ContextCompat.getColor(context,R.color.work));
				break;
			}
			case 2: {
				infoButton.setBackgroundColor(ContextCompat.getColor(context,R.color.black));
				infoButton.setTextColor(ContextCompat.getColor(context,R.color.rest));
				break;
			}
			case 3: {
				if (extendCount > 3) {
					infoButton.setBackgroundColor(ContextCompat.getColor(context,R.color.black));
					infoButton.setTextColor(ContextCompat.getColor(context,R.color.red));
				} else {
					infoButton.setBackgroundColor(ContextCompat.getColor(context,R.color.black));
					infoButton.setTextColor(ContextCompat.getColor(context,R.color.work));
				}

				break;
			}
			case 4: {
				if (extendCount > 3) {
					infoButton.setBackgroundColor(ContextCompat.getColor(context,R.color.black));
					infoButton.setTextColor(ContextCompat.getColor(context,R.color.red));
				} else {
					infoButton.setBackgroundColor(ContextCompat.getColor(context,R.color.black));
					infoButton.setTextColor(ContextCompat.getColor(context,R.color.rest));
				}

				break;
			}
			default:
				break;
		}
	}
	*/

	public void animateRestore(float x, int orientation) {
		swipeRestoreAnimCounter++;
		float dX;
		if (orientation == 0) {
			dX = x - swipeStartX;
		} else {
			dX = x - swipeStartY;
		}

		//int dXint = (int)dX;
		if (dX > 0) {
			float timeFraction = dX / fullSwipeLength;
			float multiplierFloat = timeFraction * 100;
			int multiplier = (int) multiplierFloat;
			if (multiplier < 0)
				multiplier = 0;
			if (multiplier > 99) {
				multiplier = 99;
			}
			colorIds = new int[multiplier];
			int j = 0;
			for (int i = multiplier - 1; i >= 0; i--) {
				if (i > 50) {
					switch (periodType) {
						case 1:
						case 3:
							colorIds[j] = getCurrentColorId(2, i);
							break;
						case 2:
						case 4:
							colorIds[j] = getCurrentColorId(1, i);
							break;
						default:
							break;
					}
				} else {
					switch (periodType) {
						case 1:
							colorIds[j] = getCurrentColorId(1, i);
							break;
						case 2:
							colorIds[j] = getCurrentColorId(2, i);
							break;
						case 3:
							if (extendCount > 3) {
								colorIds[j] = getCurrentColorId(3, i);
							} else {
								colorIds[j] = getCurrentColorId(1, i);
							}
							break;
						case 4:
							if (extendCount > 3) {
								colorIds[j] = getCurrentColorId(3, i);
							} else {
								colorIds[j] = getCurrentColorId(2, i);
							}
							break;
						default:
							break;
					}
				}
				j++;
			}

			switch (periodType) {
				case 1: {
					colorIds[multiplier - 1] = colorWork;
					break;
				}
				case 2: {
					colorIds[multiplier - 1] = colorRest;
					break;
				}
				case 3:
				{
					if(extendCount>3){
						colorIds[multiplier - 1] = colorRed;
					} else {
						colorIds[multiplier - 1] = colorWork;
					}
					break;
				}
				case 4: {
					if(extendCount>3){
						colorIds[multiplier - 1] = colorRed;
					} else {
						colorIds[multiplier - 1] = colorRest;
					}
					break;
				}
			}

			swipeArea.setOnTouchListener(null);

			Runnable runnableOffMain = new Runnable() {
				@Override
				public void run() {  // this thread is not on the main
					if (colorIds.length > 50) {
						titleChangeId = colorIds.length - 50;
					}

					final int arrayLength = colorIds.length;
					for (int i = 0; i < arrayLength; i++) {
						final int runnableAnimationIndex = i;
						myOffMainThreadHandler.post(new Runnable() {  // this is on the main thread
							public void run() {

								activityTitle.setTextColor(Color.parseColor(titleColors[colorIds.length - runnableAnimationIndex - 1]));
								rootLayout.setBackgroundColor(colorIds[runnableAnimationIndex]);
								infoButton.setTextColor(colorIds[runnableAnimationIndex]);
								if (runnableAnimationIndex > titleChangeId) {
									activityTitle.setText(titleForRestore.toString().toUpperCase(Locale.ENGLISH));
									titleSequence = activityTitle.getText();
									activityTitle.setTextSize(RReminder.adjustTitleSize(MainActivity.this, titleSequence.length(), smallTitle));
								}


							}
						});

						try {

							Thread.sleep(3);

						} catch (InterruptedException e) {

							e.printStackTrace();

						}


					}

					addSwipeListener();
				}


			};
			new Thread(runnableOffMain).start();
		}
	}

	public void addSwipeListener() {
		swipeArea.setOnTouchListener(new SwipeTouchListener());
	}

	public void animatePowerDownRestore(int x) {
		restoreAnimateCounter++;

		int multiplier = x;
		if (multiplier < 0)
			multiplier = 0;
		colorIds = new int[multiplier];
		int j = 0;
		for (int i = multiplier - 1; i >= 0; i--) {
			if (i > 50) {
				switch (periodType) {
					case 1:
					case 3:
						colorIds[j] = getCurrentColorId(2, i);
						break;
					case 2:
					case 4:
						colorIds[j] = getCurrentColorId(1, i);
						break;
					default:
						break;
				}
			} else {
				switch (periodType) {
					case 1:
						colorIds[j] = getCurrentColorId(1, i);
						break;
					case 2:
						colorIds[j] = getCurrentColorId(2, i);
						break;
					case 3:
						if (extendCount > 3) {
							colorIds[j] = getCurrentColorId(3, i);
						} else {
							colorIds[j] = getCurrentColorId(1, i);
						}
						break;
					case 4:
						if (extendCount > 3) {
							colorIds[j] = getCurrentColorId(3, i);
						} else {
							colorIds[j] = getCurrentColorId(2, i);
						}
						break;
					default:
						break;
				}
			}
			j++;
		}


		Runnable runnableOffMain = new Runnable() {
			@Override
			public void run() {  // this thread is not on the main
				if (colorIds.length > 50) {
					titleChangeId = colorIds.length - 50;
				}
				int arrayLength = colorIds.length;
				for (int i = 0; i < arrayLength; i++) {
					runnableAnimationIndex = i;
					myOffMainThreadHandler.post(new Runnable() {  // this is on the main thread
						public void run() {
							rootLayout.setBackgroundColor(colorIds[runnableAnimationIndex]);
							//activityTitle.setTextColor(Color.parseColor(titlePowerColors[49-colorIds.length-runnableAnimationIndex]));
						}
					});

					try {

						Thread.sleep(3);

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}


		};


		new Thread(runnableOffMain).start();
	}

	public int getCurrentColorId(int type, int multiplier) {
		int red;
		int green;
		int blue;
		int[] colorValues = new int[3];
		String[] colorHex = new String[3];
		String outputColorHex;

		if (multiplier < 51) {
			switch (type) {
				case 1: {
					//from black to work (green)
					red = 204 - 4 * multiplier;
					green = 250 - 5 * multiplier;
					blue = 51 - multiplier;


					colorValues[0] = red;
					colorValues[1] = green;
					colorValues[2] = blue;

					colorValues = formatColorValue(colorValues);

					for (int i = 0; i < colorValues.length; i++) {
						if (colorValues[i] < 16) {
							colorHex[i] = "0" + Integer.toHexString(colorValues[i]);
						} else {
							colorHex[i] = Integer.toHexString(colorValues[i]);
						}
					}
						
						/*
						String redHex = Integer.toHexString(colorValues[0]);
						String greenHex = Integer.toHexString(colorValues[1]);
						String blueHex = Integer.toHexString(colorValues[2]);
						*/
					outputColorHex = "#FF" + colorHex[0] + colorHex[1] + colorHex[2];
					return Color.parseColor(outputColorHex);
				}
				case 2: {
					//from black to rest (blue)
					red = 105 - 2 * multiplier;
					green = 210 - 4 * multiplier;
					blue = 255 - 5 * multiplier;


					colorValues[0] = red;
					colorValues[1] = green;
					colorValues[2] = blue;

					colorValues = formatColorValue(colorValues);

					int arrayLength = colorValues.length;
					for (int i = 0; i < arrayLength; i++) {
						if (colorValues[i] < 16) {
							colorHex[i] = "0" + Integer.toHexString(colorValues[i]);
						} else {
							colorHex[i] = Integer.toHexString(colorValues[i]);
						}
					}


					outputColorHex = "#FF" + colorHex[0] + colorHex[1] + colorHex[2];
					return Color.parseColor(outputColorHex);
				}
				case 3: {

					//from black to red
					red = 255 - 5 * multiplier;
					if (multiplier % 2 == 0) {
						green = 25 - multiplier / 2;
						blue = 25 - multiplier / 2;
						lastColorValue = green;
					} else {
						green = lastColorValue;
						blue = lastColorValue;
					}

					colorValues[0] = red;
					colorValues[1] = green;
					colorValues[2] = blue;

					int arrayLength = colorValues.length;
					for (int i = 0; i < arrayLength; i++) {
						if (colorValues[i] < 16) {
							colorHex[i] = "0" + Integer.toHexString(colorValues[i]);
						} else {
							colorHex[i] = Integer.toHexString(colorValues[i]);
						}
					}

					outputColorHex = "#FF" + colorHex[0] + colorHex[1] + colorHex[2];
					return Color.parseColor(outputColorHex);

				}
				case 4: {

					int color = 255 - 5 * multiplier;

					colorValues[0] = color;
					colorValues[1] = color;
					colorValues[2] = color;

					int arrayLength = colorValues.length;
					for (int i = 0; i < arrayLength; i++) {
						if (colorValues[i] < 16) {
							colorHex[i] = "0" + Integer.toHexString(colorValues[i]);
						} else {
							colorHex[i] = Integer.toHexString(colorValues[i]);
						}
					}

					outputColorHex = "#FF" + colorHex[0] + colorHex[1] + colorHex[2];
					return Color.parseColor(outputColorHex);

				}
				default:
					return 1;
			}

		} else {
			switch (type) {
				case 1: {
					//from black to work(green)
					red = (multiplier - 50) * 4;
					green = (multiplier - 50) * 5;
					blue = (multiplier - 50);

					colorValues[0] = red;
					colorValues[1] = green;
					colorValues[2] = blue;

					colorValues = formatColorValue(colorValues);

					int arrayLength = colorValues.length;
					for (int i = 0; i < arrayLength; i++) {
						if (colorValues[i] < 16) {
							colorHex[i] = "0" + Integer.toHexString(colorValues[i]);
						} else {
							colorHex[i] = Integer.toHexString(colorValues[i]);
						}
					}


					outputColorHex = "#FF" + colorHex[0] + colorHex[1] + colorHex[2];
					return Color.parseColor(outputColorHex);

				}
				case 2: {
					//from black to rest (blue)
					red = (multiplier - 50) * 2;
					green = (multiplier - 50) * 4;
					blue = (multiplier - 50) * 5;

					colorValues[0] = red;
					colorValues[1] = green;
					colorValues[2] = blue;

					colorValues = formatColorValue(colorValues);

					int arrayLength = colorValues.length;
					for (int i = 0; i < arrayLength; i++) {
						if (colorValues[i] < 16) {
							colorHex[i] = "0" + Integer.toHexString(colorValues[i]);
						} else {
							colorHex[i] = Integer.toHexString(colorValues[i]);
						}
					}
					 /*
					String secondRedHex = Integer.toHexString(colorValues[0]);
					String secondGreenHex = Integer.toHexString(colorValues[1]);
					String secondBlueHex = Integer.toHexString(colorValues[2]);
					*/

					outputColorHex = "#FF" + colorHex[0] + colorHex[1] + colorHex[2];
					return Color.parseColor(outputColorHex);
				}
				default:
					return 1;
			}
		}
	}


	@Override
	protected void onDestroy() {

		toolBar = null;
		super.onDestroy();
		//unregisterReceiver(nReceiver);
	}


	public void dismissExtendDialog() {
		Fragment prev = getSupportFragmentManager().findFragmentByTag("extendDialog");
		if (prev != null) {
			ExtendDialog df = (ExtendDialog) prev;
			df.dismiss();
		}
	}



	//for testing purposes only, remove before release
	/*
	class NotificationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent){
			if( intent.hasExtra("notification_status")){
				isOngoingNotificationOn = intent.getBooleanExtra("notification_status", false);
			}
		}
	}

	*/


}

