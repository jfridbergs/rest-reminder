package com.colormindapps.rest_reminder_alarm;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colormindapps.rest_reminder_alarm.data.Period;
import com.colormindapps.rest_reminder_alarm.data.Session;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.Calendar;


public class NotificationActivity extends FragmentActivity implements OnDialogCloseListener {
	private long mCalendar, previousPeriodEnd;
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
	private PeriodViewModel mPeriodViewModel;

	private long currentPeriodStartTime = 0;
	private TextView periodTypeTotals;

	private LiveData<Session> lastLDSession;
	private Observer<Session> lastSessionObserver;
	private SessionsViewModel mSessionsViewModel;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);
		resources = getResources();
		screenOrientation = resources.getConfiguration().orientation;
		titleFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-ThCn.otf");
		descriptionFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Lt.otf");
		buttonFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Roman.otf");

		mPeriodViewModel = new ViewModelProvider(this).get(PeriodViewModel.class);
		mSessionsViewModel = new ViewModelProvider(this).get(SessionsViewModel.class);

		mPeriodViewModel.getLastPeriod().observe(this, period -> {
			assert period != null;
			currentPeriodStartTime = period.getStartTime();
		});

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
			previousPeriodEnd = getIntent().getExtras().getLong(RReminder.PREVIOUS_PERIOD_END_TIME);
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
		super.onStop();
	}
	
	@Override
	protected void onStart(){
		super.onStart();

		RelativeLayout rootLayout;
        TextView notificationTitle = findViewById(R.id.notification_title);
		TextView extendDescription = findViewById(R.id.notification_extend_description);
        notificationTitle.setTypeface(titleFont);
        ImageView image =findViewById(R.id.notification_image);
        TextView notificationDescription =findViewById(R.id.notification_description);
		TextView sessionStart = findViewById(R.id.session_start);
		periodTypeTotals = findViewById(R.id.periods_total);
        notificationDescription.setTypeface(descriptionFont);
        Button notificationButton = findViewById(R.id.notification_button);
		Button extendPeriodEnd =  findViewById(R.id.button_notification_period_end_extend);
        notificationButton.setTypeface(buttonFont);
        rootLayout =  findViewById(R.id.root_layout);

		lastLDSession = mSessionsViewModel.getCurrentSession();
		lastSessionObserver = session -> {
			if(session!=null){
				String startTime = RReminder.getTimeString(getApplicationContext(),session.getSessionStart()).toString();
				sessionStart.setText(getString(R.string.session_started, startTime));
			}
			assert session != null;
			fillSessionTotals(session.getSessionStart());
			lastLDSession.removeObserver(lastSessionObserver);


		};
		lastLDSession.observe(this, lastSessionObserver);


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
				extendPeriodEnd.setOnClickListener(v -> showExtendDialog());
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

	public void fillSessionTotals(long sessionStart){
		mPeriodViewModel.getPeriodTotals(sessionStart,previousPeriodEnd).observe(this, periodTotals -> {
			String workTotals = RReminder.getShortDurationFromMillis(getApplicationContext(),periodTotals.get(0).getTotalDuration());
			String restTotals = RReminder.getShortDurationFromMillis(getApplicationContext(),periodTotals.get(1).getTotalDuration());
			periodTypeTotals.setText(getString(R.string.session_details, workTotals, restTotals));
		});
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
			long currentTime = Calendar.getInstance().getTimeInMillis();
			int nextType = RReminder.getNextType(type);
			nextPeriodEnd = RReminder.getNextPeriodEndTime(this, nextType, currentTime, 1, 0L);

			new MobilePeriodManager(getApplicationContext()).setPeriod(nextType, nextPeriodEnd, extendCount);
			RReminderMobile.startCounterService(this, nextType, 0, nextPeriodEnd, false);
			Period nextPeriod = new Period(0,nextType,currentTime,nextPeriodEnd-currentTime,0,0,0);
			RReminderRoomDatabase.getDatabase(this)
					.insertPeriod(nextPeriod);

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
		Intent intent = new Intent(this, MainActivity.class);
		intent.setAction(RReminder.ACTION_TURN_OFF);
		intent.putExtra(RReminder.START_COUNTER, false);
		intent.putExtra(RReminder.TURN_OFF, 1);
		intent.putExtra(RReminder.PERIOD_END_TIME, mCalendar);
		startActivity(intent);
		finish();
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
	public void unbindFromFragment(){
	}
	
	
	public void showExtendDialog(){
		if(currentPeriodStartTime!=0){
			DialogFragment newFragment = ExtendDialog.newInstance(R.string.extend_dialog_title, type, extendCount, mCalendar, 1,previousPeriodEnd);
			newFragment.show(getSupportFragmentManager(), "extendDialog");
		}

	}
	
	public void setVisibleState(boolean state){
		isOnVisible = state;
	}
	
	public static boolean getVisibleState(){
		return isOnVisible;
	}

	
	@Override
	public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
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
