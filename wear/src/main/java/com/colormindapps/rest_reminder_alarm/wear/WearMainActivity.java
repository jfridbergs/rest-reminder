package com.colormindapps.rest_reminder_alarm.wear;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colormindapps.rest_reminder_alarm.R;
import com.colormindapps.rest_reminder_alarm.shared.MyCountDownTimer;
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
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class WearMainActivity extends WearableActivity implements
        GoogleApiClient.ConnectionCallbacks,
        DataApi.DataListener,
        CapabilityApi.CapabilityListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private static final int EXTEND_FINISHED = 0;
    private static final int EXTEND_CURRENT = 1;
    private static final int START_NEXT_FROM_NOTIFICATION = 0;
    private static final int START_NEXT_FROM_COMMANDS = 1;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private RelativeLayout mContainerView;
    private TextView mTitle, mDescription;
    private GoogleApiClient mGoogleApiClient;
    private Node connectedNode;
    Typeface font, titleFont,timerFont;
    private int periodType = 0, extendCount = 0;
    private long periodEndTimeValue = 0;

    private RelativeLayout timerButtonLayout;
    private TextView timerHour1, timerHour2, timerMinute1, timerMinute2, timerSecond1, timerSecond2, colon, point;
    private int colorRest, colorWork, colorBlack, colorRed, colorWhite, colorInactive;
    private MyCountDownTimer countdown;
    private ValueAnimator bgAnimation;
    private Button openCommands;

    private ImageView transparentLayer;

    private boolean isOn = false;
    private boolean mobileOn;

    private WearPeriodManager periodManager;

    private NotificationManagerCompat notificationManager;


    private String debug = "WEAR_MAIN";

    private boolean skipOnResumeIntentSection = false;
    private boolean fromCommandsActivity = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(debug,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        font = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Lt.otf");
        titleFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-ThCn.otf");
        timerFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Lt.otf");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        transparentLayer = (ImageView) findViewById(R.id.transparentImageView);

        mContainerView = (RelativeLayout) findViewById(R.id.container);
        mTitle = (TextView) findViewById(R.id.title);
        mDescription = (TextView) findViewById(R.id.description);


        timerButtonLayout = (RelativeLayout) findViewById(R.id.timer_layout);
        timerHour1 = (TextView) findViewById(R.id.timer_hour1);
        timerMinute1 = (TextView) findViewById(R.id.timer_minute1);
        timerSecond1 = (TextView) findViewById(R.id.timer_second1);
        timerHour2 = (TextView) findViewById(R.id.timer_hour2);
        timerMinute2 = (TextView) findViewById(R.id.timer_minute2);
        timerSecond2 = (TextView) findViewById(R.id.timer_second2);
        colon = (TextView) findViewById(R.id.timer_colon);
        point = (TextView) findViewById(R.id.timer_point);
        openCommands = (Button) findViewById(R.id.open_commands);

        timerHour1.setTypeface(timerFont);
        timerMinute1.setTypeface(timerFont);
        timerSecond1.setTypeface(timerFont);
        timerHour2.setTypeface(timerFont);
        timerMinute2.setTypeface(timerFont);
        timerSecond2.setTypeface(timerFont);

        colorWork = ContextCompat.getColor(WearMainActivity.this,R.color.work);
        colorRest = ContextCompat.getColor(WearMainActivity.this,R.color.rest);
        colorBlack = ContextCompat.getColor(WearMainActivity.this,R.color.black);
        colorRed = ContextCompat.getColor(WearMainActivity.this,R.color.red);
        colorWhite = ContextCompat.getColor(WearMainActivity.this,R.color.white);
        colorInactive = ContextCompat.getColor(WearMainActivity.this,R.color.inactive_digit);

        mContainerView.setBackgroundColor(colorBlack);

        mDescription.setTypeface(font);
        mTitle.setTypeface(titleFont);

        mTitle.setText(getString(R.string.wear_main_title_off));
        mDescription.setText(getString(R.string.extend));

        periodManager = new WearPeriodManager(this.getApplicationContext());

        prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        editor = prefs.edit();

        //setting work and rest durations to 1 min for testing purposes
        //remove this part of code before publishing the app
        editor.putString(RReminder.PREF_WORK_LENGTH_KEY,"00:01");
        editor.putString(RReminder.PREF_REST_LENGTH_KEY, "00:01");
        editor.apply();

        // Get an instance of the NotificationManager service
        notificationManager =
                NotificationManagerCompat.from(this);

        setUpNotificationChannel();

        manageUI(false);

    }

    @Override
    protected void onResume(){
        super.onResume();
        mGoogleApiClient.connect();
        Log.d(debug, "onResume");
        //skip over intent check, if coming from onNewIntent call (received intent from CommandsActivity)


    }

    @Override
    protected void onPause() {
        if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.CapabilityApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }


        super.onPause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        manageUI(isOn);
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        manageUI(isOn);
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        manageUI(isOn);
    }

    private void setUpNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(RReminder.createNotificationChannel(this.getApplicationContext(),RReminder.NOTIFICATION_CHANNEL_PERIOD_END));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String intentAction = intent.getAction();
        Log.d(debug, "onNewIntent action: " + intentAction);
        //when receiving intent from CommandsActivity, the intent check in onResume is to be skipped
        skipOnResumeIntentSection = true;
        if(intentAction!=null && intentAction.length()>0){
            switch (intentAction) {
                case RReminder.ACTION_TURN_OFF: {
                    if(getIntent().getExtras()!=null){
                        actionTurnOff();
                    }
                    break;
                }
                case RReminder.ACTION_WEAR_COMMANDS_EXTEND:{
                    fromCommandsActivity = true;
                    actionExtendCurrent();
                    break;
                }
                case RReminder.ACTION_WEAR_COMMANDS_START_NEXT: {
                    fromCommandsActivity = true;
                    actionEndPeriod(START_NEXT_FROM_COMMANDS);
                }
                default:
                    break;
            }
        }


    }

    public void actionTurnOff(){
        Log.d(debug, "actionTurnOff()");
        int typeOff = getIntent().getExtras().getInt(RReminder.PERIOD_TYPE);
        long nextPeriodEndTimeOff = getIntent().getExtras().getLong(RReminder.PERIOD_END_TIME);
        stopReminder(typeOff, 0, nextPeriodEndTimeOff);
    }

    public void openCommands(View view){
        Intent openCommandsIntent = new Intent(this, CommandsActivity.class);
        openCommandsIntent.putExtra(RReminder.PERIOD_TYPE, periodType);
        startActivity(openCommandsIntent);
    }

    public void actionExtendFinished(){
        Log.d(debug, "actionExtendFinished()");

        // cancel the notification with notification manager.
        notificationManager.cancel(001);
        //check preferences if extending period option is enabled
        if(RReminder.isExtendEnabled(this.getApplicationContext())){
            extendPeriod(EXTEND_FINISHED);
        }


    }

    public void actionExtendCurrent(){
        Log.d(debug, "actionExtendCurrent");
        extendPeriod(EXTEND_CURRENT);
    }

    public void extendPeriod(int extendType){
        int extendedType, extendedCount;
        long extendedPeriodEndTime;



        // 2. calculate new extended period values
        if (extendType == EXTEND_FINISHED){
            // 1. cancel current alarm
            int cancelType = getIntent().getExtras().getInt(RReminder.PERIOD_TYPE);
            //extend count for newly created period is 0
            int cancelExtendCount = 0;
            long cancelPeriodEndTime = getIntent().getExtras().getLong(RReminder.PERIOD_END_TIME);

            WearRReminder.cancelPeriodAlarm(this.getApplicationContext(),cancelType,cancelExtendCount,cancelPeriodEndTime);

            // 2.1. gathering data for previously ended period, that is to be extended
            int previousType = getIntent().getExtras().getInt(RReminder.EXTENDED_PERIOD_TYPE);
            int previousExtendCount = getIntent().getExtras().getInt(RReminder.EXTEND_COUNT);

            // 2.2. calculate new extended period values

            extendedCount = previousExtendCount+1;
            extendedPeriodEndTime =  RReminder.getTimeAfterExtend(WearMainActivity.this.getApplicationContext(), 1, 0L);

            switch(previousType){
                case 1: case 3:  extendedType = 3; break;
                case 2:case 4: extendedType = 4; break;
                default: extendedType = 1;break;
            }
        } else {
            //cancelling currently running period with field values of this activity
            WearRReminder.cancelPeriodAlarm(this.getApplicationContext(),periodType,extendCount,periodEndTimeValue);

            //since we are extending the current period, we use period values stored in activities fields
            //1st step is to calculate how much time was left till the end of initial currently running period
            long timeTillOriginalEnd = periodEndTimeValue - Calendar.getInstance().getTimeInMillis();
            // 2.2. calculate new extended period values
            extendedCount = extendCount +1;
            extendedPeriodEndTime =  RReminder.getTimeAfterExtend(WearMainActivity.this.getApplicationContext(), 1, timeTillOriginalEnd);

            switch(periodType){
                case 1: case 3:  extendedType = 3; break;
                case 2:case 4: extendedType = 4; break;
                default: extendedType = 1;break;
            }
        }


        // 3. update data api
        Log.d(debug, "values for reminder status after extending on wear: "+ extendedType + " "+extendedPeriodEndTime + " " + extendedCount);
        updateReminderStatus(RReminder.DATA_API_SOURCE_WEAR,extendedType,extendedPeriodEndTime,extendedCount, mobileOn, true);

        // 4. set new alarm
        //periodManager.setPeriod(extendedType,extendedPeriodEndTime,extendedCount);
        // 5. update UI
    }

    public void actionEndPeriod( int startNextType){
        int cancelType, cancelExtendCount;
        long cancelPeriodEndTime;
        Log.d(debug, "actionEndPeriod()");
        //check if starting next period option is enabled
        if(RReminder.isEndPeriodEnabled(this.getApplicationContext())){
            if (startNextType == START_NEXT_FROM_NOTIFICATION){
                // cancel the notification with notification manager.
                notificationManager.cancel(001);

                // 1. cancel current alarm
                cancelType = getIntent().getExtras().getInt(RReminder.PERIOD_TYPE);
                //extend count for newly created period is 0
                cancelExtendCount = 0;
                cancelPeriodEndTime = getIntent().getExtras().getLong(RReminder.PERIOD_END_TIME);
            } else {
                cancelType = periodType;
                cancelExtendCount = extendCount;
                cancelPeriodEndTime = periodEndTimeValue;
            }


            WearRReminder.cancelPeriodAlarm(this.getApplicationContext(),cancelType,cancelExtendCount,cancelPeriodEndTime);

            // 2. gather data for new period
            int newPeriodType = RReminder.getNextType(cancelType);
            long newPeriodEndTime = RReminder.getNextPeriodEndTime(this, newPeriodType, Calendar.getInstance().getTimeInMillis(),1,0L);
            Log.d(debug, "start next period. period type: "+newPeriodType);
            Log.d(debug, "start next period. new period length: "+ (newPeriodEndTime - cancelPeriodEndTime));

            // 3. update Data API
            updateReminderStatus(RReminder.DATA_API_SOURCE_WEAR,newPeriodType,newPeriodEndTime,0, mobileOn, true);

            // 4. dispatch new alarm
            periodManager.setPeriod(newPeriodType,newPeriodEndTime,0);
            // 5. update UI
        } else {
            //if action to end current period was made from outdated notification action, remove it
            notificationManager.cancel(001);
        }

    }




    public void startReminder(View v){
        startReminder();
    }

    private void startReminder() {
        periodType = 1;
        periodEndTimeValue = RReminder.getNextPeriodEndTime(WearMainActivity.this, periodType, Calendar.getInstance().getTimeInMillis(), 1, 0L);
        periodManager.setPeriod(periodType,periodEndTimeValue,extendCount);
        isOn = true;
        updateReminderStatus(RReminder.DATA_API_SOURCE_WEAR, periodType,periodEndTimeValue,0, false, true);
        animateColorChange(RReminder.WEAR_ANIMATE_ON);
        manageUI(true);

    }

    public void stopReminder(){
        stopReminder(periodType, extendCount,periodEndTimeValue);
    }

    public void stopReminder(int typeOFF, int extendCountOFF, long perionEndtimeOff){
        stopCountDownTimer();
        Log.d(debug, "stopReminder period type: " + typeOFF);
        WearRReminder.cancelPeriodAlarm(this.getApplicationContext(),typeOFF,extendCountOFF,perionEndtimeOff);



        // cancel the notification with notification manager.
        notificationManager.cancel(001);
        periodType = 0;
        periodEndTimeValue = 0L;
        extendCount = 0;

        //update the Data API
        updateReminderStatus(RReminder.DATA_API_SOURCE_WEAR,0,0L,0, false, false);

        isOn = false;
        switch(periodType){
            case RReminder.WORK: animateColorChange(RReminder.WEAR_ANIMATE_WORK_TO_OFF); break;
            case RReminder.REST: animateColorChange(RReminder.WEAR_ANIMATE_REST_TO_OFF); break;
            case RReminder.WORK_EXTENDED: {
                if(extendCount>3){
                    animateColorChange(RReminder.WEAR_ANIMATE_RED_TO_OFF);
                } else {
                    animateColorChange(RReminder.WEAR_ANIMATE_WORK_TO_OFF);
                }
                break;
            }
            case RReminder.REST_EXTENDED: {
                if(extendCount>3){
                    animateColorChange(RReminder.WEAR_ANIMATE_RED_TO_OFF);
                } else {
                    animateColorChange(RReminder.WEAR_ANIMATE_REST_TO_OFF);
                }
                break;
            }
            default: break;
        }
        manageUI(false);

    }

    public void stopCountDownTimer(){
        if (countdown != null) {
            countdown.cancel();
            countdown.isRunning = false;
        }
    }

    public String getWorkPeriodLengthString() {
        return prefs.getString(RReminder.PREF_WORK_LENGTH_KEY,RReminder.DEFAULT_WORK_PERIOD_STRING)+ ".00";
    }

    public void updateActivity(int type, long endValue, int extendCount, boolean mobileOn){
        updateActivityStatus(type, endValue,extendCount, mobileOn);
    }

    private void updateActivityStatus(int type, long endValue, int extendCount, boolean mobileOn){
        Log.d(debug, "updateActivityStatus()");

        //manage wear period alarm
        //set wear alarm when reminder launched by mobile
        int test = 0;
        if(this.periodType==0 && type!=0 && mobileOn){
            //set new period alarm (based on the values of mobile app)
            periodManager.setPeriod(type,endValue,extendCount);
            test = 1;
        } else if(type!=0 && this.periodType!=0 && mobileOn){
            WearRReminder.cancelPeriodAlarm(this.getApplicationContext(),this.periodType,this.extendCount,this.periodEndTimeValue);
            periodManager.setPeriod(type,endValue,extendCount);
            test = 2;
        } else if (type==0){
            WearRReminder.cancelPeriodAlarm(this.getApplicationContext(),this.periodType,this.extendCount,this.periodEndTimeValue);
            test = 3;
        }

        Log.d(debug, "updateActivityStatus, test: "+test);
        this.periodType = type;
        this.periodEndTimeValue = endValue;
        this.extendCount = extendCount;
        this.mobileOn = mobileOn;
        if(periodType==0){
            manageUI(false);
        } else {
            manageUI(true);
        }
    }

    private void updatePreferences(String reminderMode, String workLength, String restLength, int extendLength, boolean extendEnabled, boolean startNextEnabled){
        Log.d(debug, "updatePreferences");
        Log.d(debug, "reminder mode "+reminderMode);
        Log.d(debug, "work period: "+workLength);
        Log.d(debug, "rest period: "+restLength);
        Log.d(debug, "extend length: "+ extendLength);
        Log.d(debug, "extend enabled: "+ extendEnabled);
        Log.d(debug, "start next enabled: "+ startNextEnabled);
        editor.putString(RReminder.PREF_REMINDER_MODE_KEY, reminderMode);
        editor.putString(RReminder.PREF_WORK_LENGTH_KEY, workLength);
        editor.putString(RReminder.PREF_REST_LENGTH_KEY, restLength);
        editor.putInt(RReminder.PREF_EXTEND_BASE_LENGTH_KEY, extendLength);
        editor.putBoolean(RReminder.PREF_EXTEND_ENABLED_KEY, extendEnabled);
        editor.putBoolean(RReminder.PREF_START_NEXT_ENABLED_KEY, startNextEnabled);
        editor.apply();

        //handle notification action, when wear is connected to mobile and preferences are made up-to-date
        if(!skipOnResumeIntentSection){
            Log.d(debug, " handling intent actions after updating preferences from MOBILE: " + getIntent().getAction());
            handleNotificationIntentActions();
        } else {
            if(periodType==0){
                manageUI(false);
            } else {
                manageUI(true);
            }
        }
        //reset boolean for skipping intent section
        skipOnResumeIntentSection = false;

    }

    private void handleNotificationIntentActions(){

            String intentAction = getIntent().getAction();
            if(intentAction!=null){
                switch(intentAction){
                    case RReminder.ACTION_TURN_OFF: {
                        actionTurnOff();
                        break;
                    }
                    case RReminder.ACTION_WEAR_NOTIFICATION_EXTEND: {
                        actionExtendFinished();
                        break;
                    }
                    case RReminder.ACTION_WEAR_NOTIFICATION_START_NEXT: {
                        actionEndPeriod(START_NEXT_FROM_NOTIFICATION);
                        break;
                    }
                    default: {
                        if(periodType==0){
                            manageUI(false);
                        } else {
                            manageUI(true);
                        }
                        break;
                    }
                }
            }

        //reset boolean for skipping intent section
        skipOnResumeIntentSection = false;
    }

    public void manageUI(Boolean isOn) {
        Log.d(debug, "manageUI. is on:"+ isOn);

        if (isOn) {
            switch (periodType) {
                case RReminder.WORK:
                    mTitle.setText(getString(R.string.on_work_period).toUpperCase(Locale.ENGLISH));
                    mDescription.setText("");
                    manageColors(1,1);
                    break;
                case RReminder.REST:
                    mTitle.setText(getString(R.string.on_rest_period).toUpperCase(Locale.ENGLISH));
                    mDescription.setText("");
                    manageColors(2,1);
                    break;
                case RReminder.WORK_EXTENDED:
                    manageColors(3,extendCount);
                    mTitle.setText(getString(R.string.on_work_period).toUpperCase(Locale.ENGLISH));
                    if (extendCount <= 1) {
                        mDescription.setText(getString(R.string.description_extended_one_time));
                    } else {
                        mDescription.setText(String.format(getString(R.string.description_extended),extendCount));
                    }
                    break;
                case RReminder.REST_EXTENDED:
                    manageColors(4,extendCount);
                    mTitle.setText(getString(R.string.on_rest_period).toUpperCase(Locale.ENGLISH));
                    if (extendCount <= 1) {
                        mDescription.setText(getString(R.string.description_extended_one_time));
                    } else {
                        mDescription.setText(String.format(getString(R.string.description_extended),extendCount));
                    }
                    break;
                default:
                    mTitle.setText("No title".toUpperCase(Locale.ENGLISH));
                    break;

            }
            if(isAmbient()){
                openCommands.setVisibility(View.GONE);
            } else {
                if(RReminder.isExtendEnabled(this.getApplicationContext())|| RReminder.isEndPeriodEnabled(this.getApplicationContext())) {
                    openCommands.setVisibility(View.VISIBLE);
                } else {
                    openCommands.setVisibility(View.GONE);
                }

            }

            manageTimer(true);
        } else {
            manageColors(0,extendCount);
            mTitle.setText(getString(R.string.reminder_off_title));
            mDescription.setText("");
            openCommands.setVisibility(View.GONE);
            manageTimer(false);

        }
    }

    public void manageTimer(Boolean active) {

        long counterTimeValue = periodEndTimeValue - Calendar.getInstance().getTimeInMillis();

        if (active) {
            stopCountDownTimer();
            countdown = new MyCountDownTimer(getApplicationContext(), counterTimeValue, 1000, timerHour1, timerHour2, colon, timerMinute1, timerMinute2, point, timerSecond1, timerSecond2, isAmbient());
            countdown.start();
            timerButtonLayout.setOnClickListener(null);
            timerButtonLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    stopReminder(periodType, extendCount, periodEndTimeValue);
                }
            });

            if(isAmbient()){
                timerButtonLayout.setBackground(null);
                point.setVisibility(View.GONE);
                timerSecond1.setVisibility(View.GONE);
                timerSecond2.setVisibility(View.GONE);
            } else {
                point.setVisibility(View.VISIBLE);
                timerSecond1.setVisibility(View.VISIBLE);
                timerSecond2.setVisibility(View.VISIBLE);
                timerButtonLayout.setBackgroundResource(R.drawable.btn_timer_idle_np);
            }

        } else {

            stopCountDownTimer();
            timerButtonLayout.setOnClickListener(null);
            timerButtonLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startReminder();
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


            if(isAmbient()){
                timerButtonLayout.setBackground(null);
                point.setVisibility(View.GONE);
                timerSecond1.setVisibility(View.GONE);
                timerSecond2.setVisibility(View.GONE);

                if (lengthArray[0] == '0' && lengthArray[1] == '0') {
                    timerHour1.setTextColor(colorBlack);
                    timerHour2.setTextColor(colorBlack);
                } else {
                    if(lengthArray[0]=='0'){
                        timerHour1.setTextColor(colorBlack);
                    } else {
                        timerHour1.setTextColor(colorWhite);
                    }

                    timerHour2.setTextColor(colorWhite);
                }
                colon.setTextColor(colorWhite);
                timerMinute1.setTextColor(colorWhite);
                timerMinute2.setTextColor(colorWhite);


            } else {
                point.setVisibility(View.VISIBLE);
                timerSecond1.setVisibility(View.VISIBLE);
                timerSecond2.setVisibility(View.VISIBLE);
                timerButtonLayout.setBackgroundResource(R.drawable.btn_timer_idle_np);

                if (lengthArray[0] == '0' && lengthArray[1] == '0') {
                    timerHour1.setTextColor(colorInactive);
                    timerHour2.setTextColor(colorInactive);
                } else {
                    timerHour1.setTextColor(colorBlack);
                    timerHour2.setTextColor(colorBlack);
                }

                colon.setTextColor(colorBlack);
                timerMinute1.setTextColor(colorBlack);
                timerMinute2.setTextColor(colorBlack);
                point.setTextColor(colorBlack);
                timerSecond1.setTextColor(colorBlack);
                timerSecond2.setTextColor(colorBlack);
            }

        }

        //timerButton.setText(getWorkPeriodLengthString(getBaseContext()));



    }


    private void manageColors(int periodType, int extendCount) {
        if (isAmbient()) {
            transparentLayer.setVisibility(View.INVISIBLE);
            mContainerView.setBackgroundColor(colorBlack);
            mTitle.setTextColor(colorWhite);
            mDescription.setTextColor(colorWhite);

        } else {
            transparentLayer.setVisibility(View.VISIBLE);
            if(periodType ==0){
                mTitle.setTextColor(colorWhite);
                mDescription.setTextColor(colorWhite);
            } else {
                mTitle.setTextColor(colorBlack);
                mDescription.setTextColor(colorBlack);
            }


            switch (periodType) {
                case RReminder.PERIOD_OFF: {
                    mContainerView.setBackgroundColor(colorBlack);
                    break;
                }
                case RReminder.WORK: {
                    mContainerView.setBackgroundColor(colorWork);
                    break;
                }
                case RReminder.REST: {
                    mContainerView.setBackgroundColor(colorRest);
                    break;
                }
                case RReminder.WORK_EXTENDED: {
                    if (extendCount > 3) {
                        mContainerView.setBackgroundColor(colorRed);
                    } else {
                        mContainerView.setBackgroundColor(colorWork);
                    }
                    break;
                }
                case RReminder.REST_EXTENDED: {
                    if (extendCount > 3) {
                        mContainerView.setBackgroundColor(colorRed);
                    } else {
                        mContainerView.setBackgroundColor(colorRest);
                    }
                    break;
                }
                default: break;
            }
        }
    }




    private void animateColorChange(int type){
        if(bgAnimation != null){
            bgAnimation.removeAllUpdateListeners();
        }
        switch(type){
            case RReminder.WEAR_ANIMATE_ON: bgAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),colorBlack,colorWork); break;
            case RReminder.WEAR_ANIMATE_WORK_TO_OFF: bgAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),colorWork,colorBlack); break;
            case RReminder.WEAR_ANIMATE_REST_TO_OFF: bgAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),colorRest,colorBlack); break;
            case RReminder.WEAR_ANIMATE_RED_TO_OFF: bgAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),colorRed,colorBlack); break;
            default: break;
        }
        bgAnimation.setDuration(200);
        bgAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mContainerView.setBackgroundColor((int) animator.getAnimatedValue());
            }

        });
        bgAnimation.start();


    }



    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(debug, "onConnected(): Successfully connected to Google API client");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.CapabilityApi.addListener(
                mGoogleApiClient, this, Uri.parse("wear://"), CapabilityApi.FILTER_REACHABLE);

        determineSourceAndGetData();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(debug, "onConnectionSuspended(): Connection to Google API client was suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(debug, "onConnectionFailed(): Failed to connect, with result: " + result);


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
                DataItem item = event.getDataItem();

                if(item.getUri().getPath().compareTo(RReminder.DATA_API_REMINDER_STATUS_PATH)==0){
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();


                    if(dataMap.getInt(RReminder.DATA_API_SOURCE)==RReminder.DATA_API_SOURCE_MOBILE || dataMap.getInt(RReminder.DATA_API_SOURCE)==RReminder.DATA_API_SOURCE_WEAR_SERVICE){
                        Log.d(debug, "call updateActivityStatus from onDataChanged");
                        Log.d(debug, "periodType: "+ dataMap.getInt(RReminder.PERIOD_TYPE));
                        int periodType = dataMap.getInt(RReminder.PERIOD_TYPE);
                        long periodEndTime = dataMap.getLong(RReminder.PERIOD_END_TIME);
                        int extendCount = dataMap.getInt(RReminder.EXTEND_COUNT);
                        boolean mobileOn = dataMap.getBoolean(RReminder.DATA_API_MOBILE_ON);
                        updateActivityStatus(periodType, periodEndTime, extendCount, mobileOn);
                        //updating reminder status node, that reminder on wear device is running, except when update from mobile comes about turning reminder off
                        if(periodType!=0){
                            updateReminderStatus(RReminder.DATA_API_SOURCE_LINKED_WEAR_ON,periodType, periodEndTime, extendCount, mobileOn, true);
                        }

                    } else if(dataMap.getInt(RReminder.DATA_API_SOURCE)==RReminder.DATA_API_SOURCE_LINKED_MOBILE_ON){
                         Log.d(debug, "wear app is notified that mobile app is up and running" );
                        mobileOn = dataMap.getBoolean(RReminder.DATA_API_MOBILE_ON);
                    }

                }

                if(item.getUri().getPath().compareTo(RReminder.DATA_API_REMINDER_PREFERENCES_PATH)==0){
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    updatePreferences(dataMap.getString(RReminder.WEAR_PREF_REMINDER_MODE),dataMap.getString(RReminder.WEAR_PREF_WORK_LENGTH), dataMap.getString(RReminder.WEAR_PREF_REST_LENGTH),
                            dataMap.getInt(RReminder.WEAR_PREF_EXTEND_LENGTH), dataMap.getBoolean(RReminder.WEAR_PREF_EXTEND_ENABLED),dataMap.getBoolean(RReminder.WEAR_PREF_START_NEXT_ENABLED));
                }


            }
        }
    }

    public void updateReminderStatus(int updateSource, int periodType, long periodEndTimeValue, int extendCount, boolean mobileOn, boolean wearOn){
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient,RReminder.createStatusData(updateSource,periodType,periodEndTimeValue,extendCount, mobileOn, wearOn));
    }

    private void getLocalData(final String pathToContent) {
        Log.d(debug, "getLocalData");
        Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
            @Override
            public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {


                Uri uri = new Uri.Builder()
                        .scheme(PutDataRequest.WEAR_URI_SCHEME)
                        .path(pathToContent)
                        .authority(getLocalNodeResult.getNode().getId())
                        .build();

                Wearable.DataApi.getDataItem(mGoogleApiClient, uri)
                        .setResultCallback(
                                new ResultCallback<DataApi.DataItemResult>() {
                                    @Override
                                    public void onResult(DataApi.DataItemResult dataItemResult) {

                                        if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
                                            DataMap data = DataMap.fromByteArray(dataItemResult.getDataItem().getData());
                                            if(data.getLong(RReminder.PERIOD_END_TIME)> Calendar.getInstance().getTimeInMillis()){
                                                Log.d(debug, "update UI with google api data upon connecting to google api");
                                                int periodType = data.getInt(RReminder.PERIOD_TYPE);
                                                long periodEndTime = data.getLong(RReminder.PERIOD_END_TIME);
                                                int extendCount = data.getInt(RReminder.EXTEND_COUNT);
                                                boolean mobileOn = data.getBoolean(RReminder.DATA_API_MOBILE_ON);
                                                updateActivityStatus(periodType, periodEndTime, extendCount, mobileOn);
                                            }

                                        }
                                    }
                                }
                        );
            }
        });
    }

    private void getPreferenceData(final String pathToContent) {
        Log.d(debug, "getPreferenceData");
        Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
            @Override
            public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {



                Uri uri = new Uri.Builder()
                        .scheme(PutDataRequest.WEAR_URI_SCHEME)
                        .path(pathToContent)
                        .authority(connectedNode.getId())
                        .build();

                Wearable.DataApi.getDataItem(mGoogleApiClient, uri)
                        .setResultCallback(
                                new ResultCallback<DataApi.DataItemResult>() {
                                    @Override
                                    public void onResult(DataApi.DataItemResult dataItemResult) {

                                        if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
                                            DataMap data = DataMap.fromByteArray(dataItemResult.getDataItem().getData());
                                            updatePreferences(data.getString(RReminder.WEAR_PREF_REMINDER_MODE),data.getString(RReminder.WEAR_PREF_WORK_LENGTH), data.getString(RReminder.WEAR_PREF_REST_LENGTH),
                                                    data.getInt(RReminder.WEAR_PREF_EXTEND_LENGTH), data.getBoolean(RReminder.WEAR_PREF_EXTEND_ENABLED),data.getBoolean(RReminder.WEAR_PREF_START_NEXT_ENABLED));


                                        }
                                    }
                                }
                        );
            }

        });

    }

    private void getConnectedData(final String pathToContent) {
        Log.d(debug, "getConnectedData");
        Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
            @Override
            public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {



                Uri uri = new Uri.Builder()
                        .scheme(PutDataRequest.WEAR_URI_SCHEME)
                        .path(pathToContent)
                        .authority(connectedNode.getId())
                        .build();

                Wearable.DataApi.getDataItem(mGoogleApiClient, uri)
                        .setResultCallback(
                                new ResultCallback<DataApi.DataItemResult>() {
                                    @Override
                                    public void onResult(DataApi.DataItemResult dataItemResult) {

                                        if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
                                            DataMap data = DataMap.fromByteArray(dataItemResult.getDataItem().getData());
                                            Log.d(debug, "getConnectedData, mobileOn: "+data.getBoolean(RReminder.DATA_API_MOBILE_ON));
                                            if(data.getBoolean(RReminder.DATA_API_MOBILE_ON) && data.getLong(RReminder.PERIOD_END_TIME) > Calendar.getInstance().getTimeInMillis()){
                                                //update wear device with google api data upon connecting to google api only if the data stored in google api isnt outdated
                                                Log.d(debug, "update UI with google api data upon connecting to google api");
                                                int periodType = data.getInt(RReminder.PERIOD_TYPE);
                                                long periodEndTime = data.getLong(RReminder.PERIOD_END_TIME);
                                                int extendCount = data.getInt(RReminder.EXTEND_COUNT);
                                                boolean mobileOn = data.getBoolean(RReminder.DATA_API_MOBILE_ON);
                                                Log.d(debug, "getConnectedData mobileOn value from node"+mobileOn);
                                                updateActivityStatus(periodType, periodEndTime, extendCount, mobileOn);
                                                //notify DATA API, that wear is now on
                                                updateReminderStatus(RReminder.DATA_API_SOURCE_LINKED_WEAR_ON,periodType, periodEndTime, extendCount, mobileOn, true);
                                            }

                                        }
                                        if(mobileOn) {
                                            Log.d(debug, "getConnectedData mobile IS connected and IS running");
                                        } else {
                                            Log.d(debug, "getConnectedData mobile IS connected and IS NOT running");
                                            getLocalData(RReminder.DATA_API_REMINDER_STATUS_PATH);
                                        }
                                    }
                                }
                        );
            }
        });

    }



    private void determineSourceAndGetData()
    {

        Context appContext = this.getApplicationContext();
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult nodes) {

                if(fromCommandsActivity){
                    Log.d(debug, "returning from CommandsActivity, getting data from Local node");
                    getLocalData(RReminder.DATA_API_REMINDER_STATUS_PATH);
                    fromCommandsActivity = false;
                    return;
                }

                for (Node node : nodes.getNodes()) {
                    connectedNode = node;
                }

                //if wear is connected to a mobile, we will update wear app with reminder status and preferences
                if (connectedNode != null){
                    getConnectedData(RReminder.DATA_API_REMINDER_STATUS_PATH);
                    getPreferenceData((RReminder.DATA_API_REMINDER_PREFERENCES_PATH));
                    //TO DO: consider enforcing getLocalNode when returning from commands screen
                } else {
                    Log.d(debug, "mobile IS NOT connected and IS NOT running");
                    getLocalData(RReminder.DATA_API_REMINDER_STATUS_PATH);

                    //if no device is connected to wear, check for wether or not execute action call from notification uses local wear preferences
                    if(!skipOnResumeIntentSection) {
                        Log.d(debug, " handling intent actions using WEAR preferences: " + getIntent().getAction());
                        handleNotificationIntentActions();
                    }
                }
            }
        });



    }

}
