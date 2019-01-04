package com.colormindapps.rest_reminder_alarm;

import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colormindapps.rest_reminder_alarm.shared.MyCountDownTimer;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;
import com.robotium.solo.Solo;

import java.util.ArrayList;

public class PreferencesTest extends ActivityInstrumentationTestCase2<MainActivity> {

    MainActivity mActivity;
    TextView title,extendHint, swipeHint, startHint, stopHint, menuHint, description;
    TextView timerHour1, timerHour2, timerMinute1, timerMinute2, timerSecond1, timerSecond2, timerPoint, timerColon;
    TextView notificationTitle;
    TextView swipeArea;
    Context appContext;
    String expectedOfflineTitle, expectedOnlineWorkTitle, expectedOnlineRestTitle, actualModeSummary;
    String expectedModeSummary, expectedWorkLengthSummary, expectedRestLengthSummary, expectedProximityLengthSummary, expectedWorkAudioSummary, expectedRestAudioSummary, expectedProximityAudioSummary, expectedExtendCountSummary, expectedExtendBaseLengthSummary;
    String actualWorkLengthSummary, actualRestLengthSummary, actualProximityLenghtSummary, actualWorkAudioSummary, actualRestAudioSummary, actualProximityAudioSummary, actualExtendCountSummary, actualExtendLenghtSummary;
    String actualOfflineTitle, actualOnlineTitle;
    String workPeriodLengthKey, restPeriodLengthKey, approxPeriodLengthKey, workPeriodSoundKey, restPeriodSoundKey, approxSoundKey, extendCountKey, extendBaseLengthKey;
    Button hintButton, extendButton;

    Typeface typeFace;
    int expectedTitleSize, actualTitleSize, extendOptionCount, extendBaseLength;
    MyCountDownTimer timer;
    RelativeLayout timerLayout, rootLayout;
    Instrumentation instr;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    float scaledDensity;
    NumberPicker numberPicker1, numberPicker2, numberPicker21, numberPicker22;
    String debug = "PREFERENCE_ACT_TEST";
    IntentFilter filter;
    MyReceiver receiver;

    //for testing purposes only, remove when releasing the app
    boolean onGoingNotificationIsOn = false;


    Uri originalWorkUri, originalRestUri, originalApproxUri;

    private Solo solo;

    public PreferencesTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception{
        setActivityInitialTouchMode(false);
        instr = this.getInstrumentation();
        mActivity = getActivity();
        solo = new Solo(instr, mActivity);


        appContext = mActivity.getApplicationContext();

        preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        editor   = preferences.edit();
        scaledDensity = appContext.getResources().getDisplayMetrics().scaledDensity;


        rootLayout = (RelativeLayout) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.mainActivityLayout);
        title =(TextView) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.period_title);
        description =(TextView) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.description_text);
        timerLayout = (RelativeLayout) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.timer_layout);
        timerHour1 = (TextView)mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.timer_hour1);
        timerHour2 = (TextView)mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.timer_hour2);
        timerMinute1 = (TextView)mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.timer_minute1);
        timerMinute2 = (TextView)mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.timer_minute2);
        timerSecond1 = (TextView)mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.timer_second1);
        timerSecond2 = (TextView)mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.timer_second2);
        timerPoint = (TextView)mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.timer_point);
        timerColon = (TextView)mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.timer_colon);
        hintButton = (Button) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.info_button);
        extendButton = (Button) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.button_period_end_extend);
        swipeArea = (TextView) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.swipe_area_text);
        expectedOfflineTitle = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.reminder_off_title).toUpperCase();
        expectedOnlineWorkTitle = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.on_work_period).toUpperCase();
        expectedOnlineRestTitle = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.on_rest_period).toUpperCase();
        actualOfflineTitle = title.getText().toString().toUpperCase();
        extendOptionCount = preferences.getInt(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key), 1);
        extendBaseLength = preferences.getInt(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key), 5);

        workPeriodLengthKey = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_key);
        restPeriodLengthKey = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key);
        approxPeriodLengthKey = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_approx_notification_length_key);
        workPeriodSoundKey = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_start_sound_key);
        restPeriodSoundKey = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_start_sound_key);
        approxSoundKey = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_approx_time_sound_key);
        extendCountKey = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key);
        extendBaseLengthKey = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key);

        filter = new IntentFilter(RReminder.CUSTOM_INTENT_TEST_PREFERENCES);

        editor.putBoolean(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.putBoolean(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_approx_notification_key), true);
        editor.putBoolean(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_end_period_key), true);
        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_key), "00:05");
        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), "00:05");
        editor.putInt(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key), 1);
        editor.commit();
        receiver = new MyReceiver();
        mActivity.registerReceiver(receiver, filter);
    }

    @Override
    public void tearDown() throws Exception{
        RReminderMobile.cancelCounterAlarm(appContext, mActivity.periodType, mActivity.extendCount, mActivity.periodEndTimeValue, false,0L);
        RReminderMobile.stopCounterService(appContext, mActivity.periodType);
        mActivity.unregisterReceiver(receiver);

        editor.putBoolean(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), false);
        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_key), "0");
        editor.putBoolean(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_end_period_key), true);
        editor.putBoolean(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.putBoolean(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_approx_notification_key), false);
        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), RReminder.DEFAULT_REST_PERIOD_STRING);
        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_key), "00:45");
        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), "00:15");
        editor.putInt(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key), 3);
        editor.putInt(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key), 5);
        editor.commit();
        solo.finishOpenedActivities();
        super.tearDown();
    }

    public void testPreferenceDescriptions(){



        Intent intent = new Intent(appContext,PreferenceActivity.class);
        mActivity.startActivity(intent);

        Instrumentation.ActivityMonitor monitor = instr.addMonitor(PreferenceActivity.class.getName(), null, false);

        PreferenceActivity pActivity = (PreferenceActivity) instr.waitForMonitor(monitor);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);
        solo.sleep(1000);

        solo.goBack();

        instr.waitForIdleSync();

        solo.assertCurrentActivity("main activity should be open", MainActivity.class);
        solo.sleep(3000);



        expectedModeSummary = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_summary_automatic);
        expectedWorkLengthSummary = RReminder.getFormatedValue(mActivity.getApplicationContext(), 0, preferences.getString(workPeriodLengthKey, mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.default_work_length_string)));
        expectedRestLengthSummary = RReminder.getFormatedValue(mActivity.getApplicationContext(), 0, preferences.getString(restPeriodLengthKey, mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.default_rest_length_string)));
        expectedProximityLengthSummary = RReminder.getFormatedValue(mActivity.getApplicationContext(), 1, preferences.getString(approxPeriodLengthKey, mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.default_approx_length_string)));

        String temp = preferences.getString(workPeriodSoundKey, "DEFAULT_RINGTONE_URI");

        if (temp.equals("DEFAULT_RINGTONE_URI")){
            originalWorkUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {
            originalWorkUri = Uri.parse(temp);
        }

        Ringtone ringtone = RingtoneManager.getRingtone(mActivity.getApplicationContext(), originalWorkUri);
        expectedWorkAudioSummary = ringtone.getTitle(mActivity.getApplicationContext());

        temp = preferences.getString(restPeriodSoundKey, "DEFAULT_RINGTONE_URI");

        if (temp.equals("DEFAULT_RINGTONE_URI")){
            originalRestUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {
            originalRestUri = Uri.parse(temp);
        }

        ringtone = RingtoneManager.getRingtone(mActivity.getApplicationContext(), originalRestUri);
        expectedRestAudioSummary = ringtone.getTitle(mActivity.getApplicationContext());

        temp = preferences.getString(approxSoundKey, "DEFAULT_RINGTONE_URI");

        if (temp.equals("DEFAULT_RINGTONE_URI")){
            originalApproxUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {
            originalApproxUri = Uri.parse(temp);
        }

        ringtone = RingtoneManager.getRingtone(mActivity.getApplicationContext(), originalApproxUri);
        expectedProximityAudioSummary = ringtone.getTitle(mActivity.getApplicationContext());

        Integer value = preferences.getInt(extendCountKey, RReminder.DEFAULT_EXTEND_COUNT);
        if(value==1){
            expectedExtendCountSummary=mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_options_single);
        } else {
            expectedExtendCountSummary = String.format(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_options_multiple), value);
        }

        value = preferences.getInt(extendBaseLengthKey, RReminder.DEFAULT_EXTEND_BASE_LENGTH);
        if(value==1){
            expectedExtendBaseLengthSummary=mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_minute_single);
        } else {
            expectedExtendBaseLengthSummary = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_minute_multiple, value);
        }


        assertEquals("the summary of mode preference should say automatic", expectedModeSummary, actualModeSummary);
        assertEquals("the summary of work lenght preference should match", expectedWorkLengthSummary, actualWorkLengthSummary);
        assertEquals("the summary of rest lenght preference should match", expectedRestLengthSummary, actualRestLengthSummary);
        assertEquals("the summary of proximity lenght preference should match", expectedProximityLengthSummary, actualProximityLenghtSummary);
        assertEquals("the summary of work period audio preference should match", expectedWorkAudioSummary, actualWorkAudioSummary);
        assertEquals("the summary of rest period audio preference should match", expectedRestAudioSummary, actualRestAudioSummary);
        assertEquals("the summary of proximity audio preference should match", expectedProximityAudioSummary, actualProximityAudioSummary);
        assertEquals("the summary of extend count prfererence should match", expectedExtendCountSummary, actualExtendCountSummary);
        assertEquals("the summary of extend base length prfererence should match", expectedExtendBaseLengthSummary, actualExtendLenghtSummary);






        /*

        assertTrue("the period setting submenu should be open", solo.searchText("Work period duration"));
        solo.sleep(1000);
        solo.goBack();
        solo.sleep(2000);
        solo.clickOnText("Rest reminder mode");
        solo.sleep(500);
        assertTrue("the first preference title should spell Rest reminder mode", solo.searchText("Rest reminder mode"));

        ArrayList<View> currentModeViews = solo.getCurrentViews();
        for (View v : currentModeViews) {
            Log.d(debug,"current view: "+ v.toString());
        }



*/



    }

    public void testUpdatedWorkPeriodAfterPreferenceChange(){
        solo.clickOnView(timerLayout);
        solo.sleep(3000);
        long periodEndTimeValue = mActivity.periodEndTimeValue;
        long expectedPeriodEndTime1 = periodEndTimeValue + 35*60*1000;
        Intent intent = new Intent(appContext,PreferenceActivity.class);
        mActivity.startActivity(intent);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);
        solo.sleep(1000);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title));
        solo.sleep(1000);

        changePeriodLengthPreference(RReminder.WORK,0,40);


        solo.sleep(200);
        solo.goBack();
        solo.sleep(1000);
        solo.goBack();
        solo.sleep(2000);

        long actualEndTimeValue1 = mActivity.periodEndTimeValue;
        long difference = Math.abs(actualEndTimeValue1-expectedPeriodEndTime1);
        Log.d("PREFERENCE_TEST", "difference: "+difference);
        assertTrue("after changing work length preference, the service should be updated", difference<1000);

        periodEndTimeValue = mActivity.periodEndTimeValue;
        expectedPeriodEndTime1 = periodEndTimeValue - 10*60*1000;
        intent = new Intent(appContext,PreferenceActivity.class);
        mActivity.startActivity(intent);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);
        solo.sleep(1000);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title));
        solo.sleep(1000);

        changePeriodLengthPreference(RReminder.WORK,0,30);


        solo.sleep(200);
        solo.goBack();
        solo.sleep(1000);
        solo.goBack();
        solo.sleep(2000);

        actualEndTimeValue1 = mActivity.periodEndTimeValue;
        difference = Math.abs(actualEndTimeValue1-expectedPeriodEndTime1);
        Log.d("PREFERENCE_TEST", "difference: "+difference);
        assertTrue("after changing work length preference 2nd time, the service should be updated", difference<1000);
    }

    public void swipePeriodEnd(){
        float swipeLenght;
        DisplayMetrics displayMetrics = appContext.getResources().getDisplayMetrics();


        if(RReminder.isPortrait(appContext)){
            swipeLenght = displayMetrics.widthPixels*0.6f;
        } else {
            swipeLenght = displayMetrics.heightPixels*0.6f;
        }
        int location[] = new int[2];
        swipeArea.getLocationOnScreen(location);
        if(RReminder.isPortrait(appContext)){
            solo.drag(50,70+(int)swipeLenght, location[1]+40, location[1]+40,20);
        } else {
            solo.drag(location[0]+40,location[0]+40, location[1]+40, location[1]+40+(int)swipeLenght,20);
        }

    }

    public void testMultiplePeriodChangesInOneSession(){

        //testing work period preferences with 2 changes within one preference session
        solo.clickOnView(timerLayout);
        solo.sleep(3000);

        long periodEndTimeValue = mActivity.periodEndTimeValue;
        //Since we are changing work period length to 8 mins, we need to add 3 mins to the expected time, because the original work period length is set to 5 mins
        long expectedPeriodEndTime1 = periodEndTimeValue + 3*60*1000;

        Intent intent = new Intent(appContext,PreferenceActivity.class);
        mActivity.startActivity(intent);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);
        solo.sleep(1000);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title));
        solo.sleep(1000);

        changePeriodLengthPreference(RReminder.WORK,0,40);

        solo.sleep(4000);

        changePeriodLengthPreference(RReminder.WORK,0,8);

        solo.sleep(200);
        solo.goBack();
        solo.sleep(1000);
        solo.goBack();
        solo.sleep(2000);

        long actualEndTimeValue1 = mActivity.periodEndTimeValue;
        long difference = Math.abs(actualEndTimeValue1-expectedPeriodEndTime1);
        Log.d("PREFERENCE_TEST", "difference: "+difference);
        assertTrue("after changing work length preference 2 times, the service should be updated and match the latest change", difference<1000);

        //testing rest period preferences with 3 changes within 1 preference session
        //switching to rest period
        swipePeriodEnd();
        solo.sleep(2000);

        periodEndTimeValue = mActivity.periodEndTimeValue;
        //Since we are changing final period length to 3 mins, we need to subtrack 2 mins from the expected time, because the original rest period length is set to 5 mins
        expectedPeriodEndTime1 = periodEndTimeValue - 2*60*1000;

        intent = new Intent(appContext,PreferenceActivity.class);
        mActivity.startActivity(intent);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);
        solo.sleep(1000);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title));
        solo.sleep(1000);

        changePeriodLengthPreference(RReminder.REST, 0,10);

        solo.sleep(4000);

        changePeriodLengthPreference(RReminder.REST, 0,15);

        solo.sleep(4000);

        changePeriodLengthPreference(RReminder.REST, 0,3);

        solo.sleep(200);
        solo.goBack();
        solo.sleep(1000);
        solo.goBack();
        solo.sleep(2000);

        actualEndTimeValue1 = mActivity.periodEndTimeValue;
        difference = Math.abs(actualEndTimeValue1-expectedPeriodEndTime1);
        Log.d("PREFERENCE_TEST", "difference: "+difference);
        assertTrue("after changing rest length preference 3 time, the service should be updated and match the latest change", difference<1000);

    }

    public void changeSingleNumberPickerPreference(int value){

        setValueForOneNumberPicker(value);

        solo.clickOnButton("Set");
        solo.sleep(200);

    }

    public void setValueForOneNumberPicker(int value){
        final int newValue = value;
        ArrayList<View> currentViews = solo.getCurrentViews();
        for (View v : currentViews) {
            if (v instanceof NumberPicker) {
                Log.d(debug, "NumberPicker object detected");
                numberPicker1 = (NumberPicker)v;
            }
        }


        mActivity.runOnUiThread(
                new Runnable() {
                    public void run() {
                        numberPicker1.setValue(newValue);
                    }
                });
        solo.sleep(200);
    }

    public void setValuesForTwoNumberPickers(int first, int second){
        final int hours = first;
        final int minutes = second;
        ArrayList<View> currentViews = solo.getCurrentViews();
        boolean nb1Picked = false;
        for (View v : currentViews) {
            if (v instanceof NumberPicker) {
                Log.d(debug, "NumberPicker object detected");
                if(nb1Picked == false){
                    numberPicker1 = (NumberPicker)v;
                    nb1Picked = true;
                    Log.d(debug, "first numberpicker assigned");
                } else {
                    numberPicker2 = (NumberPicker)v;
                    Log.d(debug, "second numberpicker assigned");
                }
            }
        }


        mActivity.runOnUiThread(
                new Runnable() {
                    public void run() {
                        numberPicker1.setValue(hours);
                        numberPicker2.setValue(minutes);
                    }
                });
        solo.sleep(200);
    }

    public void changePeriodLengthPreference(int periodType, int hour, int mins){
        switch (periodType){
            case RReminder.WORK : {
                solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_title));
                break;
            }
            case RReminder.REST : {
                solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_title));
                break;
            }
            case 99 : {
                solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_approx_notification_length_title));
                break;
            }
            default: break;
        }

        solo.sleep(1000);

        setValuesForTwoNumberPickers(hour, mins);

        solo.clickOnButton("Set");
    }

    public void testUpdatedRestPeriodAfterPreferenceChange(){
        solo.clickOnView(timerLayout);
        solo.sleep(3000);
        swipePeriodEnd();
        solo.sleep(2000);
        long periodEndTimeValue = mActivity.periodEndTimeValue;
        //Since we are changing rest period lenght to 40 mins, we need to add 35 mins to the expected time, because the original rest period length is set to 5 mins
        long expectedPeriodEndTime1 = periodEndTimeValue + 35*60*1000;
        Intent intent = new Intent(appContext,PreferenceActivity.class);
        mActivity.startActivity(intent);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);
        solo.sleep(1000);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title));
        solo.sleep(1000);

        changePeriodLengthPreference(RReminder.REST, 0,40);

        solo.sleep(200);
        solo.goBack();
        solo.sleep(1000);
        solo.goBack();
        solo.sleep(2000);

        long actualEndTimeValue1 = mActivity.periodEndTimeValue;
        long difference = Math.abs(actualEndTimeValue1-expectedPeriodEndTime1);
        Log.d("PREFERENCE_TEST", "difference: "+difference);
        assertTrue("after changing rest length preference, the service should be updated", difference<1000);

        periodEndTimeValue = mActivity.periodEndTimeValue;
        expectedPeriodEndTime1 = periodEndTimeValue - 10*60*1000;
        intent = new Intent(appContext,PreferenceActivity.class);
        mActivity.startActivity(intent);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);
        solo.sleep(1000);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title));
        solo.sleep(1000);

       changePeriodLengthPreference(RReminder.REST,0,30);

        solo.sleep(200);
        solo.goBack();
        solo.sleep(1000);
        solo.goBack();
        solo.sleep(2000);

        actualEndTimeValue1 = mActivity.periodEndTimeValue;
        difference = Math.abs(actualEndTimeValue1-expectedPeriodEndTime1);
        Log.d("PREFERENCE_TEST", "difference: "+difference);
        assertTrue("after changing rest length preference 2nd time, the service should be updated", difference<1000);
    }

    public void testUpdatedSummary(){

        Intent intent = new Intent(appContext,PreferenceActivity.class);
        mActivity.startActivity(intent);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);
        solo.sleep(1000);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_title));
        solo.sleep(1000);
        solo.clickOnText("Manual");
        solo.sleep(1000);
        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title));
        solo.sleep(1000);

        changePeriodLengthPreference(RReminder.WORK,1,30);

        solo.sleep(200);

        changePeriodLengthPreference(RReminder.REST, 2,45);

        solo.sleep(200);
        /*
        solo.clickOnText("Work period end sound");
        solo.sleep(1000);

        solo.clickOnText("Antares");


        solo.clickOnButton("OK");

        solo.sleep(200);

        solo.clickOnText("Rest period end duration");
        solo.sleep(1000);

        solo.clickOnText("Arcturus");

        solo.clickOnButton("OK");
*/
        solo.goBack();
        solo.sleep(1000);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_approx_settings_title));
        solo.sleep(200);

        changePeriodLengthPreference(99,0,15);

        solo.sleep(200);

        solo.goBack();
        solo.sleep(1000);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_period_extend_title));
        solo.sleep(200);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_title));
        solo.sleep(200);

        changeSingleNumberPickerPreference(2);


        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_title));
        solo.sleep(200);

        changeSingleNumberPickerPreference(17);



        solo.goBack();
        solo.sleep(1000);

        solo.goBack();
        solo.sleep(1000);

        expectedModeSummary = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_summary_manual);
        expectedWorkLengthSummary = RReminder.getFormatedValue(mActivity.getApplicationContext(), 0, preferences.getString(workPeriodLengthKey, mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.default_work_length_string)));
        expectedRestLengthSummary = RReminder.getFormatedValue(mActivity.getApplicationContext(), 0, preferences.getString(restPeriodLengthKey, mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.default_rest_length_string)));
        expectedProximityLengthSummary = RReminder.getFormatedValue(mActivity.getApplicationContext(), 1, preferences.getString(approxPeriodLengthKey, mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.default_approx_length_string)));

        String temp = preferences.getString(workPeriodSoundKey, "DEFAULT_RINGTONE_URI");

        if (temp.equals("DEFAULT_RINGTONE_URI")){
            originalWorkUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {
            originalWorkUri = Uri.parse(temp);
        }

        Ringtone ringtone = RingtoneManager.getRingtone(mActivity.getApplicationContext(), originalWorkUri);
        expectedWorkAudioSummary = ringtone.getTitle(mActivity.getApplicationContext());

        temp = preferences.getString(restPeriodSoundKey, "DEFAULT_RINGTONE_URI");

        if (temp.equals("DEFAULT_RINGTONE_URI")){
            originalRestUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {
            originalRestUri = Uri.parse(temp);
        }

        ringtone = RingtoneManager.getRingtone(mActivity.getApplicationContext(), originalRestUri);
        expectedRestAudioSummary = ringtone.getTitle(mActivity.getApplicationContext());

        temp = preferences.getString(approxSoundKey, "DEFAULT_RINGTONE_URI");

        if (temp.equals("DEFAULT_RINGTONE_URI")){
            originalApproxUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {
            originalApproxUri = Uri.parse(temp);
        }

        ringtone = RingtoneManager.getRingtone(mActivity.getApplicationContext(), originalApproxUri);
        expectedProximityAudioSummary = ringtone.getTitle(mActivity.getApplicationContext());

        Integer value = preferences.getInt(extendCountKey, RReminder.DEFAULT_EXTEND_COUNT);
        if(value==1){
            expectedExtendCountSummary=mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_options_single);
        } else {
            expectedExtendCountSummary=mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_options_multiple, value);
        }

        value = preferences.getInt(extendBaseLengthKey, RReminder.DEFAULT_EXTEND_BASE_LENGTH);
        if(value==1){
            expectedExtendBaseLengthSummary=mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_minute_single);
        } else {
            expectedExtendBaseLengthSummary = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_minute_multiple, value);
        }


        assertEquals("after updating reminder mode preference the summary should be updated", expectedModeSummary, actualModeSummary);
        assertEquals("after updating work period lenght preference the summary should be updated", expectedWorkLengthSummary, actualWorkLengthSummary);
        assertEquals("after updating rest period lenght preference the summary should be updated", expectedRestLengthSummary, actualRestLengthSummary);
        assertEquals("after updating proximity alarm lenght preference the summary should be updated", expectedProximityLengthSummary, actualProximityLenghtSummary);
        //assertEquals("after updating work period ringtone preference the summary should be updated", expectedWorkAudioSummary, actualWorkAudioSummary);
       //assertEquals("after updating rest period ringtone preference the summary should be updated", expectedRestAudioSummary, actualRestAudioSummary);
        assertEquals("after updating extend options count preference the summary should be updated", expectedExtendCountSummary, actualExtendCountSummary);
        assertEquals("after updating extend lenght preference the summary should be updated", expectedExtendBaseLengthSummary, actualExtendLenghtSummary);


    }

    public void testNumberPickerPreferenceStateAfterRotation(){
        int expectedHour, expectedMinute, actualHour, actualMinute;
        Intent intent = new Intent(appContext,PreferenceActivity.class);
        mActivity.startActivity(intent);
        Instrumentation.ActivityMonitor monitor = instr.addMonitor(PreferenceActivity.class.getName(), null, false);

        PreferenceActivity pActivity = (PreferenceActivity) instr.waitForMonitor(monitor);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);
        solo.sleep(1000);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title));
        solo.sleep(1000);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_title));
        solo.sleep(1000);

        setValuesForTwoNumberPickers(3,47);

        solo.sleep(200);

        expectedHour = 3;
        expectedMinute = 47;

        if(RReminder.isPortrait(appContext)){
            pActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            pActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        monitor = instr.addMonitor(PreferenceActivity.class.getName(), null, false);

        pActivity = (PreferenceActivity) instr.waitForMonitor(monitor);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);
        solo.sleep(3000);
      ArrayList<View>  currentRotateViews = solo.getCurrentViews();
       boolean nb1Picked = false;
        for (View v : currentRotateViews) {
            if (v instanceof NumberPicker) {
                Log.d(debug, "NumberPicker object detected");
                if(nb1Picked == false){
                    numberPicker21 = (NumberPicker)v;
                    nb1Picked = true;
                    Log.d(debug, "first numberpicker assigned");
                } else {
                    numberPicker22 = (NumberPicker)v;
                    Log.d(debug, "second numberpicker assigned");
                }
            }
        }

        actualHour = numberPicker21.getValue();
        actualMinute = numberPicker22.getValue();
        solo.sleep(1000);

        assertEquals("after rotating the device the work preference should keep the previous set hour value", expectedHour, actualHour);
        assertEquals("after rotating the device the work preference should keep the previous set minute value", expectedMinute, actualMinute);
        instr.waitForIdleSync();


        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);

        if(RReminder.isPortrait(appContext)){
            pActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            pActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        solo.goBack();
        solo.sleep(1000);
        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_title));
        solo.sleep(1000);

        setValuesForTwoNumberPickers(2,29);
        solo.sleep(200);

        expectedHour = 2;
        expectedMinute = 29;

        if(RReminder.isPortrait(appContext)){
            pActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            pActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        monitor = instr.addMonitor(PreferenceActivity.class.getName(), null, false);

        pActivity = (PreferenceActivity) instr.waitForMonitor(monitor);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);
        solo.sleep(3000);
        currentRotateViews = solo.getCurrentViews();
        nb1Picked = false;
        for (View v : currentRotateViews) {
            if (v instanceof NumberPicker) {
                Log.d(debug, "NumberPicker object detected");
                if(nb1Picked == false){
                    numberPicker21 = (NumberPicker)v;
                    nb1Picked = true;
                    Log.d(debug, "first numberpicker assigned");
                } else {
                    numberPicker22 = (NumberPicker)v;
                    Log.d(debug, "second numberpicker assigned");
                }
            }
        }

        actualHour = numberPicker21.getValue();
        actualMinute = numberPicker22.getValue();
        solo.sleep(1000);

        assertEquals("after rotating the device the rest preference should keep the previous set hour value", expectedHour, actualHour);
        assertEquals("after rotating the device the rest preference should keep the previous set minute value", expectedMinute, actualMinute);

        solo.goBack();

        if(RReminder.isPortrait(appContext)){
            pActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            pActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        monitor = instr.addMonitor(PreferenceActivity.class.getName(), null, false);

        pActivity = (PreferenceActivity) instr.waitForMonitor(monitor);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);

        solo.goBack();

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_approx_settings_title));
        solo.sleep(1000);
        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_approx_notification_length_title));
        solo.sleep(1000);

        setValuesForTwoNumberPickers(0,19);

        expectedHour = 0;
        expectedMinute = 19;
        solo.sleep(1000);
        if(RReminder.isPortrait(appContext)){
            pActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            pActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }


        monitor = instr.addMonitor(PreferenceActivity.class.getName(), null, false);

        pActivity = (PreferenceActivity) instr.waitForMonitor(monitor);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);
        solo.sleep(3000);
        currentRotateViews = solo.getCurrentViews();
        nb1Picked = false;
        for (View v : currentRotateViews) {
            if (v instanceof NumberPicker) {
                Log.d(debug, "NumberPicker object detected");
                if(nb1Picked == false){
                    numberPicker21 = (NumberPicker)v;
                    nb1Picked = true;
                    Log.d(debug, "first numberpicker assigned");
                } else {
                    numberPicker22 = (NumberPicker)v;
                    Log.d(debug, "second numberpicker assigned");
                }
            }
        }

        actualHour = numberPicker21.getValue();
        actualMinute = numberPicker22.getValue();
        solo.sleep(1000);

        assertEquals("after rotating the device the proximity preference should keep the previous set hour value", expectedHour, actualHour);
        assertEquals("after rotating the device the proximity preference should keep the previous set minute value", expectedMinute, actualMinute);

        solo.clickOnButton("Cancel");

        if(RReminder.isPortrait(appContext)){
            pActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            pActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        monitor = instr.addMonitor(PreferenceActivity.class.getName(), null, false);

        pActivity = (PreferenceActivity) instr.waitForMonitor(monitor);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);

        solo.goBack();
        solo.sleep(1000);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_period_extend_title));
        solo.sleep(1000);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_title));
        solo.sleep(1000);

        setValueForOneNumberPicker(3);

        int expectedCount = 3;

        if(RReminder.isPortrait(appContext)){
            pActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            pActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        monitor = instr.addMonitor(PreferenceActivity.class.getName(), null, false);

        pActivity = (PreferenceActivity) instr.waitForMonitor(monitor);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);
        solo.sleep(3000);

        currentRotateViews = solo.getCurrentViews();
        for (View v : currentRotateViews) {
            if (v instanceof NumberPicker) {
                Log.d(debug, "NumberPicker object detected");
                    numberPicker21 = (NumberPicker)v;

            }
        }

        int actualCount = numberPicker21.getValue();
        solo.sleep(1000);

        assertEquals("after rotating the device the extend count preference should keep the previous set hour value", expectedCount, actualCount);

        solo.clickOnButton("Cancel");

        if(RReminder.isPortrait(appContext)){
            pActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            pActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        monitor = instr.addMonitor(PreferenceActivity.class.getName(), null, false);

        pActivity = (PreferenceActivity) instr.waitForMonitor(monitor);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_title));
        solo.sleep(1000);

        setValueForOneNumberPicker(57);
        solo.sleep(200);

        int expectedBaseDuration = 57;

        if(RReminder.isPortrait(appContext)){
            pActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            pActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        monitor = instr.addMonitor(PreferenceActivity.class.getName(), null, false);

        pActivity = (PreferenceActivity) instr.waitForMonitor(monitor);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);
        solo.sleep(3000);

        currentRotateViews = solo.getCurrentViews();
        for (View v : currentRotateViews) {
            if (v instanceof NumberPicker) {
                Log.d(debug, "NumberPicker object detected");
                numberPicker21 = (NumberPicker)v;

            }
        }

        int actualBaseDuration = numberPicker21.getValue();
        solo.sleep(1000);

        assertEquals("after rotating the device the extend duration preference should keep the previous set hour value", expectedBaseDuration, actualBaseDuration);
    }

    public void testEnablingFunctionality(){
        solo.clickOnView(timerLayout);
        solo.sleep(200);
        assertTrue("after initial launch extend button should be visible", extendButton.getVisibility() == View.VISIBLE);
        assertTrue("after intial launch the swipe area should be visible", swipeArea.getVisibility()==View.VISIBLE);

        Intent intent = new Intent(appContext,PreferenceActivity.class);
        mActivity.startActivity(intent);
        Instrumentation.ActivityMonitor monitor = instr.addMonitor(PreferenceActivity.class.getName(), null, false);

        PreferenceActivity pActivity = (PreferenceActivity) instr.waitForMonitor(monitor);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_end_period_title));
        solo.sleep(100);
        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_title));
        solo.sleep(100);
        solo.goBack();
        instr.waitForIdleSync();

        assertFalse("after changing settings the extend button should not be visible", extendButton.getVisibility() == View.VISIBLE);
        assertFalse("after changing settings the swipe area should not be visible", swipeArea.getVisibility() == View.VISIBLE);

        intent = new Intent(appContext,PreferenceActivity.class);
        mActivity.startActivity(intent);


        monitor = instr.addMonitor(PreferenceActivity.class.getName(), null, false);
        pActivity = (PreferenceActivity) instr.waitForMonitor(monitor);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_end_period_title));
        solo.sleep(100);
        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_title));
        solo.sleep(100);
        solo.goBack();
        instr.waitForIdleSync();

        assertTrue("after restoring settings the extend button should be visible", extendButton.getVisibility() == View.VISIBLE);
        assertTrue("after restoring settings the swipe area should be visible", swipeArea.getVisibility() == View.VISIBLE);

    }

    public void testExtendOptionsChanged(){
        editor.putBoolean(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_key), "00:05");
        editor.commit();

        solo.clickOnView(timerLayout);
        instr.waitForIdleSync();


        String extendButtonOneOption,optionOneButton, optionTwoButton, optionThreeButton;
        extendButtonOneOption = String.format(mActivity.getResources().getString(R.string.extend_period_one_option),extendBaseLength);
        optionOneButton =String.format(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength);
        optionTwoButton = String.format(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength*2);
        optionThreeButton =String.format(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength*3);
        assertTrue("with extend count 1 the extend button direclty calls first option",solo.searchText(extendButtonOneOption, true));


        Intent intent = new Intent(appContext,PreferenceActivity.class);
        mActivity.startActivity(intent);
        Instrumentation.ActivityMonitor monitor = instr.addMonitor(PreferenceActivity.class.getName(), null, false);

        PreferenceActivity pActivity = (PreferenceActivity) instr.waitForMonitor(monitor);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_period_extend_title));
        solo.sleep(200);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_title));
        solo.sleep(1000);

        changeSingleNumberPickerPreference(3);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_title));
        solo.sleep(1000);

        changeSingleNumberPickerPreference(15);


        solo.goBack();
        solo.sleep(1000);

        solo.goBack();
        solo.sleep(1000);

        extendBaseLength = preferences.getInt(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key), 5);
        optionOneButton = String.format(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength);
        optionTwoButton = String.format(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength*2);
        optionThreeButton = String.format(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength*3);


        solo.clickOnView(extendButton);
        instr.waitForIdleSync();

        assertTrue(" after changing settings the first extend option button should be visible",solo.searchText(optionOneButton, true));
        assertTrue("after chaning settings the second extend option button should be visible", solo.searchText(optionTwoButton, true));
        assertTrue("after chaning settings the third extend option button should be visible", solo.searchText(optionThreeButton, true));

    }


    public void testLedPreference(){
        boolean ledActiveStatus = preferences.getBoolean(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_show_led_key), true);
        assertTrue("default value of LED preference should be true", ledActiveStatus);

        Intent intent = new Intent(appContext,PreferenceActivity.class);
        mActivity.startActivity(intent);
        Instrumentation.ActivityMonitor monitor = instr.addMonitor(PreferenceActivity.class.getName(), null, false);

        PreferenceActivity pActivity = (PreferenceActivity) instr.waitForMonitor(monitor);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);
        solo.sleep(1000);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_show_led_title));
        solo.sleep(1000);
        solo.goBack();

        ledActiveStatus = preferences.getBoolean(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_show_led_key), true);

        assertFalse("the changed LED preference should be stored correctly", ledActiveStatus);

        intent = new Intent(appContext,PreferenceActivity.class);
        mActivity.startActivity(intent);
        monitor = instr.addMonitor(PreferenceActivity.class.getName(), null, false);

        pActivity = (PreferenceActivity) instr.waitForMonitor(monitor);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);
        solo.sleep(1000);

        solo.clickOnText(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_show_led_title));
        solo.sleep(1000);
        solo.goBack();

        ledActiveStatus = preferences.getBoolean(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_show_led_key), true);

        assertTrue("the changed LED preference should be restored correctly", ledActiveStatus);



    }


    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(debug, "intent with summaries received");
            actualModeSummary = intent.getExtras().getString(RReminder.PREFERENCE_MODE_SUMMARY);
            actualWorkLengthSummary = intent.getExtras().getString(RReminder.PREFERENCE_WORK_LENGTH_SUMMARY);
            actualRestLengthSummary = intent.getExtras().getString(RReminder.PREFERENCE_REST_LENGTH_SUMMARY);
            actualProximityLenghtSummary = intent.getExtras().getString(RReminder.PREFERENCE_PROXIMITY_LENGTH_SUMMARY);
            actualWorkAudioSummary = intent.getExtras().getString(RReminder.PREFERENCE_WORK_AUDIO_SUMMARY);
            actualRestAudioSummary = intent.getExtras().getString(RReminder.PREFERENCE_REST_AUDIO_SUMMARY);
            actualProximityAudioSummary = intent.getExtras().getString(RReminder.PREFERENCE_PROXIMITY_AUDIO_SUMMARY);
            actualExtendCountSummary = intent.getExtras().getString(RReminder.PREFERENCE_EXTEND_COUNT_SUMMARY);
            actualExtendLenghtSummary = intent.getExtras().getString(RReminder.PREFERENCE_EXTEND_LENGTH_SUMMARY);
            Log.d(debug, "mode preference summary: "+ actualModeSummary);
        }
    }


    //for testing purposes only. remove when releasing the app
    public class NotificationReceiver extends BroadcastReceiver{

            @Override
            public void onReceive(Context context, Intent intent) {
                String temp = intent.getStringExtra("notification_event");
                Log.d(debug, temp);
            }

    }

    public boolean isOnGoingNotificationOn(){
        return onGoingNotificationIsOn;
    }





}