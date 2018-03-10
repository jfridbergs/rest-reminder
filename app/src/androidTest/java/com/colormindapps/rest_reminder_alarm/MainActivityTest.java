package com.colormindapps.rest_reminder_alarm;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colormindapps.rest_reminder_alarm.shared.MyCountDownTimer;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;
import com.robotium.solo.Solo;

import java.util.Calendar;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    MainActivity mActivity;
    TextView title,extendHint, swipeHint, startHint, stopHint, menuHint, description;
    TextView timerHour1, timerHour2, timerMinute1, timerMinute2, timerSecond1, timerSecond2, timerPoint, timerColon;
    TextView notificationTitle;
    TextView swipeArea;
    Context appContext;
    String expectedOfflineTitle, expectedOnlineWorkTitle, expectedOnlineRestTitle;
    String actualOfflineTitle, actualOnlineTitle;
    Button hintButton, extendButton;
    Typeface typeFace;
    int expectedTitleSize, actualTitleSize, extendOptionCount, extendBaseLength;
    MyCountDownTimer timer;
    RelativeLayout timerLayout, rootLayout;
    Instrumentation instr;
    SharedPreferences preferences, privatePreferences;
    SharedPreferences.Editor editor, privateEditor;
    float scaledDensity;
    String debug = "MAIN_ACTIVITY_TEST";

    private int versionCode;

    private Solo solo;

    public MainActivityTest() {
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
        privatePreferences =    mActivity.getSharedPreferences(RReminder.PRIVATE_PREF, Context.MODE_PRIVATE);
        editor   = preferences.edit();
        privateEditor = privatePreferences.edit();
        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_key), "00:01");
        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), "00:01");
        editor.putBoolean(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.commit();
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
        expectedOfflineTitle = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.reminder_off_title);
        expectedOnlineWorkTitle = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.on_work_period).toUpperCase();
        expectedOnlineRestTitle = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.on_rest_period).toUpperCase();
        actualOfflineTitle = title.getText().toString();
        extendOptionCount = preferences.getInt(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key), 1);
        extendBaseLength = preferences.getInt(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key),5);
        versionCode = preferences.getInt(RReminder.VERSION_KEY, 0);
    }

    @Override
    public void tearDown() throws Exception{
        mActivity = getActivity();
        solo.sleep(2000);
        Log.d(debug, "TEARDOWN");
        solo.finishOpenedActivities();
        RReminderMobile.cancelCounterAlarm(appContext, mActivity.periodType, mActivity.extendCount, mActivity.periodEndTimeValue, false,0L);
        RReminderMobile.stopCounterService(appContext, mActivity.periodType);

        editor.putBoolean(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_key), "0");
        editor.putBoolean(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_end_period_key), true);
        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), RReminder.DEFAULT_REST_PERIOD_STRING);
        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_key), "00:45");
        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), "00:15");
        editor.putInt(RReminder.VERSION_KEY, versionCode);
        editor.commit();
        appContext = null;
        mActivity = null;
        super.tearDown();
    }

    public void testPreConditions(){
        assertTrue(mActivity != null);
        assertTrue(title != null);
        assertTrue(hintButton != null);
        assertEquals("title was expected to be and actual it was", expectedOfflineTitle, actualOfflineTitle);
    }

    public void testIntroductionDialog(){
        privateEditor.putInt(RReminder.VERSION_KEY, 0);
        privateEditor.putBoolean(RReminder.EULA_ACCEPTED, false);
        privateEditor.commit();
        solo.sleep(500);

        instr.callActivityOnPause(mActivity);

        mActivity.runOnUiThread(
                new Runnable() {
                    public void run() {
                        instr.callActivityOnResume(mActivity);
                    }
                });


        instr.waitForIdleSync();
        solo.sleep(2000);

        DialogFragment introDialog = (DialogFragment)mActivity.getSupportFragmentManager().findFragmentByTag("introductionDialog");

        //check if cliking hintbutton opens a hintdialog


        assertTrue("introdialog is an instance of DialogFragment", introDialog != null);
        assertTrue("extenddialog is visible", introDialog.getShowsDialog());
        if(RReminder.isPortrait(appContext)){
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }


        Instrumentation.ActivityMonitor  monitor = instr.addMonitor(MainActivity.class.getName(), null, false);

        String settingsButtonText = appContext.getResources().getString(R.string.eula_reject);

        mActivity = (MainActivity) instr.waitForMonitor(monitor);
        solo.sleep(1500);
        assertTrue("after rotating the device the intro dialog should still be visible", solo.searchButton(settingsButtonText));
        solo.sleep(1000);


        assertFalse(solo.searchText(expectedOfflineTitle));

        RReminderMobile.stopCounterService(getActivity().getApplicationContext(),1);
        RReminderMobile.cancelCounterAlarm(getActivity().getApplicationContext(), 1,0,mActivity.periodEndTimeValue, false,0L);






    }

    public void testIntroductionDialogClose(){
        privateEditor.putInt(RReminder.VERSION_KEY, 0);
        privateEditor.putBoolean(RReminder.EULA_ACCEPTED, false);
        privateEditor.commit();
        solo.sleep(500);
        instr.callActivityOnPause(mActivity);

        mActivity.runOnUiThread(
                new Runnable() {
                    public void run() {
                        instr.callActivityOnResume(mActivity);
                    }
                });


        instr.waitForIdleSync();
        solo.sleep(2000);

        DialogFragment introDialog = (DialogFragment)mActivity.getSupportFragmentManager().findFragmentByTag("introductionDialog");

        //check if cliking hintbutton opens a hintdialog


        assertTrue("introdialog is an instance of DialogFragment", introDialog != null);
        assertTrue("extenddialog is visible", introDialog.getShowsDialog());
        solo.sleep(2000);

        String closeIntroductionDialogText = appContext.getResources().getString(R.string.eula_accept);
        solo.clickOnButton(closeIntroductionDialogText);
        solo.sleep(2000);
        assertFalse("after pressing i accept button the dialog should be dismissed", (solo.searchButton(closeIntroductionDialogText)));

        assertFalse("after closing introduction dialog the countdown service shouldnt be running", RReminderMobile.isCounterServiceRunning(appContext));

        String expectedTitle = expectedOfflineTitle;
        String actualTitle = title.getText().toString();
        assertEquals("after closing introduction dialog offline title should be seen", expectedTitle, actualTitle);

        int expectedBgColor = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.black);
        int actualBgColor = Color.TRANSPARENT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Drawable background = rootLayout.getBackground();
            if (background instanceof ColorDrawable)
                actualBgColor = ((ColorDrawable) background).getColor();
        }

        assertEquals("the bg color should be off", expectedBgColor, actualBgColor);


    }

    public void testNotificationActivityTurnOffButton(){
        solo.sleep(5000);
        int initialSetReminderOffCounter = mActivity.setReminderOffCounter;
        solo.clickOnView(timerLayout);

        //solo.waitForActivity(NotificationActivity.class, 5000);

        Instrumentation.ActivityMonitor monitor = instr.addMonitor(NotificationActivity.class.getName(), null, false);

        Activity nActivity = instr.waitForMonitor(monitor);
        Button turnOffButton = (Button) nActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_turn_off);
        notificationTitle = (TextView) nActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_title);


        solo.assertCurrentActivity("notification activity should be open", NotificationActivity.class);
        assertTrue(turnOffButton!=null);
        solo.sleep(3000);

        int textSize = 50;
        int tabletTextSize = 100;
        int symbolCount = notificationTitle.getText().length();
        int expectedSize = RReminder.adjustTitleSize(appContext, symbolCount, false);
        int actualSize = (int)(notificationTitle.getTextSize()/scaledDensity);

        if(RReminder.isTablet(appContext)){
            assertEquals("the adjustTitleSize on tablet should return size 100 for 12 symbol text string", tabletTextSize, expectedSize);
        } else {
            assertEquals("the adjustTitleSize should return size 50 for 12 symbol text string", textSize, expectedSize);
        }

        assertEquals("the actual text size should match the return value from adjustTitleSize", expectedSize, actualSize);

        int expectedColor = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.black);
        int actualColor = notificationTitle.getCurrentTextColor();

        assertEquals("the notification title color should be black", expectedColor, actualColor);

        solo.clickOnView(turnOffButton);
        int updatedSetReminderOffCounter = mActivity.setReminderOffCounter;
        assertTrue("after pressing turn off button the setReminderOff should be called", (updatedSetReminderOffCounter == initialSetReminderOffCounter));
        instr.waitForIdleSync();
        solo.assertCurrentActivity("the notification activity should be closed and previous activity (MainActivity) should be visible", MainActivity.class);
    }

    public void testLaunchReminder(){
        mActivity.runOnUiThread(
                new Runnable() {
                    public void run(){
                        timerLayout.requestFocus();
                    }
                });
        this.sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        solo.clickOnView(timerLayout);
        solo.sleep(1000);
        expectedOnlineWorkTitle = appContext.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.on_work_period).toUpperCase();
        actualOnlineTitle = title.getText().toString().toUpperCase();
        assertEquals("after launching the reminder the title was supposed to be %s, but actual it was %s", expectedOnlineWorkTitle, actualOnlineTitle);

    }



    public void testDisableCountDownOnPause(){
        assertTrue(mActivity.countdown == null);
        instr.waitForIdleSync();
        solo.clickOnView(timerLayout);
        solo.sleep(2000);
        assertTrue(mActivity.countdown.isRunning);
        instr.callActivityOnPause(mActivity);
        Log.d("TEST", "onPause called");
        assertFalse(mActivity.countdown.isRunning);
        /*
        solo.sleep(2000);
        mActivity.runOnUiThread(
                new Runnable() {
                    public void run() {
                        instr.callActivityOnResume(mActivity);
                    }
                });
        solo.sleep(3000);
        assertTrue(mActivity.countdown.isRunning);
        */
    }

    public void testAdjustTitleSize(){
        int count, expectedSize, actualSize,expectedTabletSize;

        //BEFORE TEST: ADD EXCEPTION TO adjustTitleSize so that it throws exception, when a negative value is passed as argument
        //normal screen
        count = 10;
        expectedSize = 50;
        expectedTabletSize = 100;
        actualSize = RReminder.adjustTitleSize(appContext, count, false);
        if(RReminder.isTablet(appContext)){
            assertEquals("normal screen (tablet) with 10 symbol string should have font size 100", expectedTabletSize, actualSize);
        } else {
            assertEquals("normal screen with 10 symbol string should have font size 50", expectedSize, actualSize);
        }

        count = 20;
        expectedSize = 35;
        expectedTabletSize = 70;
        actualSize = RReminder.adjustTitleSize(appContext, count, false);
        if(RReminder.isTablet(appContext)){
            assertEquals("normal screen (tablet) with 20 symbol string should have font size 70", expectedTabletSize, actualSize);
        } else {
            assertEquals("normal screen with 20 symbol string should have font size 35", expectedSize, actualSize);
        }

        count = 30;
        expectedSize = 28;
        expectedTabletSize = 56;
        actualSize = RReminder.adjustTitleSize(appContext, count, false);
        if(RReminder.isTablet(appContext)){
            assertEquals("normal screen (tablet) with 30 symbol string should have font size 56", expectedTabletSize, actualSize);
        } else {
            assertEquals("normal screen with 30 symbol string should have font size 28", expectedSize, actualSize);
        }

        count = 40;
        expectedSize = 22;
        expectedTabletSize = 44;
        actualSize = RReminder.adjustTitleSize(appContext, count, false);
        if(RReminder.isTablet(appContext)){
            assertEquals("normal screen (tablet) with 40 symbol string should have font size 44", expectedTabletSize, actualSize);
        } else {
            assertEquals("normal screen with 40 symbol string should have font size 22", expectedSize, actualSize);
        }

        //tablet scrren
        //small screen
        count = 10;
        expectedSize = 34;
        actualSize = RReminder.adjustTitleSize(appContext, count, true);
        assertEquals("small screen with 10 symbol string should have font size 34", expectedSize, actualSize);
        count = 20;
        expectedSize = 26;
        actualSize = RReminder.adjustTitleSize(appContext, count, true);
        assertEquals("small screen with 20 symbol string should have font size 26", expectedSize, actualSize);
        count = 30;
        expectedSize = 20;
        actualSize = RReminder.adjustTitleSize(appContext, count, true);
        assertEquals("small screen with 30 symbol string should have font size 20", expectedSize, actualSize);
        count = 40;
        expectedSize = 15;
        actualSize = RReminder.adjustTitleSize(appContext, count, true);
        assertEquals("small screen with 40 symbol string should have font size 15", expectedSize, actualSize);
    }
    /*
    public void testAdjustTitleSizeTablet(){
        int count, expectedSize, actualSize;
        count = 10;
        expectedSize = 100;
        actualSize = Reminder.adjustTitleSize(appContext, count, false);
        assertEquals("tablet screen with 10 symbol string should have font size 100", expectedSize, actualSize);
        count = 20;
        expectedSize = 70;
        actualSize = Reminder.adjustTitleSize(appContext, count, false);
        assertEquals("tablet screen with 20 symbol string should have font size 70", expectedSize, actualSize);
        count = 30;
        expectedSize = 56;
        actualSize = Reminder.adjustTitleSize(appContext, count, false);
        assertEquals("tablet screen with 30 symbol string should have font size 56", expectedSize, actualSize);
        count = 40;
        expectedSize = 44;
        actualSize = Reminder.adjustTitleSize(appContext, count, false);
        assertEquals("tablet screen with 40 symbol string should have font size 44", expectedSize, actualSize);
    }
    */

    public void testTitleContent(){
        int symbolCount, expectedSize, actualSize, expectedColor, actualColor;

        instr.waitForIdleSync();
        expectedColor = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.white);
        actualColor = title.getCurrentTextColor();
        assertEquals("the color of title when offline", expectedColor, actualColor);
        symbolCount = title.getText().length();
        expectedSize = RReminder.adjustTitleSize(appContext, symbolCount, false);
        actualSize = (int)(title.getTextSize()/scaledDensity);
        assertEquals("title was expected to be and actual it was",expectedOfflineTitle,actualOfflineTitle);
        assertEquals("the title size when offline",expectedSize,actualSize);
        solo.clickOnView(timerLayout);
        instr.waitForIdleSync();
        symbolCount = title.getText().length();
        expectedSize = RReminder.adjustTitleSize(appContext, symbolCount, false);
        actualSize = (int)(title.getTextSize()/scaledDensity);
        assertEquals("the title size when active and work period",expectedSize,actualSize);
        actualOnlineTitle = title.getText().toString().toUpperCase();
        assertEquals("after launching reminder the title should be work period",expectedOnlineWorkTitle,actualOnlineTitle);
        expectedColor = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.black);
        actualColor = title.getCurrentTextColor();
        assertEquals("the color of title when offline", expectedColor, actualColor);
        Instrumentation.ActivityMonitor monitor = instr.addMonitor(NotificationActivity.class.getName(), null, false);

        Activity nActivity = instr.waitForMonitor(monitor);
        solo.assertCurrentActivity("notification activity should be open", NotificationActivity.class);
        instr.waitForIdleSync();
        solo.clickOnButton("Close");
        solo.waitForActivity(MainActivity.class,5000);
        solo.assertCurrentActivity("main activity should be open", MainActivity.class);
        instr.waitForIdleSync();
        actualOnlineTitle = title.getText().toString().toUpperCase();
        symbolCount = title.getText().length();
        expectedSize = RReminder.adjustTitleSize(appContext, symbolCount, false);
        actualSize = (int)(title.getTextSize()/scaledDensity);
        assertEquals("the title size when active and rest period",expectedSize,actualSize);
        assertEquals("after work period ended, the title should be rest period",expectedOnlineRestTitle,actualOnlineTitle);
    }

    public void testTimerButtonOfflineValue(){
        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_key), "00:45");
        editor.commit();
        solo.sleep(1000);

        String offlaneStringFromPreference =  RReminder.getPreferencePeriodLength(appContext, 1) + ".00";
        String expectedString = "00:45.00";
        assertEquals("the string from preference", expectedString,offlaneStringFromPreference );
        String actualString = timerHour1.getText().toString() + timerHour2.getText().toString()+ ":" + timerMinute1.getText().toString()+ timerMinute2.getText().toString()+ "."+ timerSecond1.getText().toString() + timerSecond2.getText().toString();
        assertEquals("the string from timer button should match the string stored in preferences", offlaneStringFromPreference,actualString);
    }

    public void testExtendButton(){
        int expectedButtonTextSize;
        SharedPreferences.Editor editor   = preferences.edit();
        editor.putBoolean(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.commit();
        solo.clickOnView(timerLayout);
        instr.waitForIdleSync();
        solo.sleep(1000);
        Bundle data =  mActivity.getDataFromService();
        long initialPeriodEndTime = data.getLong(RReminder.PERIOD_END_TIME);
        int initialExtendCount = data.getInt(RReminder.EXTEND_COUNT);
        String expectedButtonText = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_current_period);
        String actualButtonText = extendButton.getText().toString();
        assertEquals("the actual extend button text matches the string saved in resources", expectedButtonText, actualButtonText);
        expectedButtonTextSize = (RReminder.isTablet(appContext) ? 28:20);
        int actualButtonTextSize = (int)(extendButton.getTextSize()/scaledDensity);
        assertEquals("the actual extend button text size matches the value saved in dimensions", expectedButtonTextSize, actualButtonTextSize);
        int expectedColor = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.black);
        int actualColor = extendButton.getCurrentTextColor();
        assertEquals("the extend button text color is black", expectedColor, actualColor);
        solo.clickOnView(extendButton);
        instr.waitForIdleSync();

        DialogFragment extendDialog = (DialogFragment)mActivity.getSupportFragmentManager().findFragmentByTag("extendDialog");

        //check if cliking hintbutton opens a hintdialog


        assertTrue("extenddialog is an instance of DialogFragment", extendDialog!=null);
        assertTrue("extenddialog is visible", extendDialog.getShowsDialog());

        String optionOneButton, optionTwoButton, optionThreeButton;
        optionOneButton = String.format(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength);
        optionTwoButton =String.format(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength*2);
        optionThreeButton = String.format(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength*3);
        assertTrue("the first extend option button should be visible",solo.searchText(optionOneButton, true));
        switch(extendOptionCount){
            case 1:{
                assertFalse(" when option count is one, the second extend option button should not be visible",solo.searchText(optionTwoButton, true));
                assertFalse(" when option count is one, the third extend option button should not be visible",solo.searchText(optionThreeButton, true));
                break;
            }
            case 2:{
                assertTrue(" when option count is two, the second extend option button should be visible",solo.searchText(optionTwoButton, true));
                assertFalse(" when option count is two, the third extend option button should not be visible",solo.searchText(optionThreeButton, true));
                break;
            }
            case 3:{
                assertTrue(" when option count is three, the second extend option button should be visible",solo.searchText(optionTwoButton, true));
                assertTrue(" when option count is three, the third extend option button should be visible",solo.searchText(optionThreeButton, true));
                break;
            }
            default: break;
        }

        solo.clickOnButton(optionOneButton);
        instr.waitForIdleSync();
        solo.sleep(1000);

        data = mActivity.getDataFromService();
        long updatedPeriodEndTime = data.getLong(RReminder.PERIOD_END_TIME);
        int updatedExtendCount = data.getInt(RReminder.EXTEND_COUNT);
        long expectedUpdatedPeriodEndTime = initialPeriodEndTime + (long)extendBaseLength * 60000L;
        int expectedExtendCount = initialExtendCount + 1;
        long delta = Math.abs(expectedUpdatedPeriodEndTime - updatedPeriodEndTime);
        assertTrue("after extending period the service value period end time should be updated and be equal previous end time plus extend time", delta < 100 );
        assertEquals("after extending period the service value extend count should be updated", expectedExtendCount, updatedExtendCount);

        solo.clickOnView(extendButton);
        instr.waitForIdleSync();
        solo.clickOnButton(optionOneButton);
        instr.waitForIdleSync();
        solo.sleep(1000);

        solo.clickOnView(extendButton);
        instr.waitForIdleSync();
        solo.clickOnButton(optionOneButton);
        instr.waitForIdleSync();
        solo.sleep(1000);

        solo.clickOnView(extendButton);
        instr.waitForIdleSync();
        solo.clickOnButton(optionOneButton);
        instr.waitForIdleSync();
        solo.sleep(1000);

        int expectedBgColor = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.red);
        int actualBgColor = Color.TRANSPARENT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Drawable background = rootLayout.getBackground();
            if (background instanceof ColorDrawable)
                actualBgColor = ((ColorDrawable) background).getColor();
        }

        assertEquals("after 4th extend the background color should change to red", expectedBgColor, actualBgColor);



    }

    public void testExtendDialogClosedAfterPeriodEnd(){
        solo.sleep(3000);
        SharedPreferences.Editor editor   = preferences.edit();
        editor.putBoolean(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.commit();
        solo.clickOnView(timerLayout);
        instr.waitForIdleSync();
        solo.clickOnView(extendButton);
        instr.waitForIdleSync();
        ExtendDialog extendDialog = (ExtendDialog)mActivity.getSupportFragmentManager().findFragmentByTag("extendDialog");

        assertTrue("extenddialog is an instance of DialogFragment", extendDialog instanceof ExtendDialog);
        assertTrue("extenddialog is visible", extendDialog.dialogIsOpen);
        Instrumentation.ActivityMonitor monitor = instr.addMonitor(NotificationActivity.class.getName(), null, false);

        Activity nActivity = instr.waitForMonitor(monitor);
        solo.assertCurrentActivity("notification activity should be open", NotificationActivity.class);
        solo.clickOnButton("Close");
        solo.waitForActivity(MainActivity.class, 5000);
        solo.assertCurrentActivity("main activity should be open", MainActivity.class);
        solo.sleep(2000);
        assertFalse("extenddialog is dismissed", extendDialog.dialogIsOpen);
        solo.sleep(8000);
    }

    public void testExtendDialogOrientationChange(){


        SharedPreferences.Editor editor   = preferences.edit();
        editor.putBoolean(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.commit();
        solo.clickOnView(timerLayout);
        instr.waitForIdleSync();
        solo.clickOnView(extendButton);
        instr.waitForIdleSync();



        ExtendDialog extendDialog = (ExtendDialog)mActivity.getSupportFragmentManager().findFragmentByTag("extendDialog");

        assertTrue("extenddialog is an instance of DialogFragment", extendDialog instanceof ExtendDialog);
        assertTrue("extenddialog is visible", extendDialog.dialogIsOpen);
        solo.sleep(1000);

        //rotate screen
        if(RReminder.isPortrait(appContext)){
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }


        instr.waitForIdleSync();
        assertTrue("after orientation change extenddialog is visible", extendDialog.getShowsDialog());

        //close dialog with press of a button
        solo.clickOnButton(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_close_dialog_text));
        instr.waitForIdleSync();
        solo.sleep(5000);
        Log.d("TEST", "getShowsDialog value: " + extendDialog.getShowsDialog());
        assertFalse("after pressing close button dialog should be gone", extendDialog.dialogIsOpen);



    }

    public void testTimerButtonLongPress(){
        solo.clickOnView(timerLayout);
        int restoreFunctionCount = mActivity.restoreAnimateCounter;
        instr.waitForIdleSync();
        assertTrue("the service is running", RReminderMobile.isCounterServiceRunning(appContext));
        Intent intent = new Intent (appContext, MobileOnAlarmReceiver.class);
        intent.putExtra(RReminder.PERIOD_TYPE, 1);
        intent.putExtra(RReminder.EXTEND_COUNT, 0);
        intent.setAction(RReminder.ACTION_ALARM_PERIOD_END);
        boolean alarmUp = (PendingIntent.getBroadcast(appContext, (int)mActivity.periodEndTimeValue, intent, PendingIntent.FLAG_ONE_SHOT) != null);
        assertTrue("the alarm manager is running", alarmUp);
        solo.clickLongOnView(timerLayout, 700);
        instr.waitForIdleSync();
        int actualFunctionCount = mActivity.restoreAnimateCounter;
        int diff = actualFunctionCount - restoreFunctionCount;
        assertEquals("the restore animation function counter should have been increased", 1, diff);
        instr.waitForIdleSync();
        solo.clickLongOnView(timerLayout, 1500);
        instr.waitForIdleSync();
        solo.sleep(3000);
        assertFalse("the service is not running", RReminderMobile.isCounterServiceRunning(appContext));
        alarmUp = (PendingIntent.getBroadcast(appContext, (int)mActivity.periodEndTimeValue, intent,PendingIntent.FLAG_ONE_SHOT) != null);
        //assertFalse("the alarm manager was not running anymore", alarmUp);
        boolean periodService = false;
        boolean playSoundService = false;
        ActivityManager manager = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MobilePeriodService.class.getName().equals(service.service.getClassName())) {
                periodService = true;
            }
            if (PlaySoundService.class.getName().equals(service.service.getClassName())) {
                playSoundService = true;
            }

        }

        actualOnlineTitle = title.getText().toString();
        assertFalse("PeriodService should not be running when the app is turned off", periodService);
        assertFalse("PlaySoundService should not be running when the app is turned off", playSoundService);
        assertFalse("the countdown timer is cancelled after long pressing button", mActivity.countdown.isRunning);
        assertEquals("the title should display offline command", expectedOfflineTitle, actualOnlineTitle);

    }

    public void testSwipeListenerActive(){
        assertFalse("at the start of activity the boolean for swipe area should be false", mActivity.swipeAreaListenerUsed);
        solo.clickOnView(timerLayout);
        instr.waitForIdleSync();
        solo.clickOnView(swipeArea);
        assertTrue("the SwipeAreaListener activity was registered",mActivity.swipeAreaListenerUsed);
    }

    public void testSwipeBackgroundChange(){

        solo.clickOnView(timerLayout);
        instr.waitForIdleSync();
        solo.sleep(1000);
        DisplayMetrics displayMetrics = appContext.getResources().getDisplayMetrics();

        float swipeLength = displayMetrics.widthPixels*0.6f;
        int location[] = new int[2];
        float x = swipeArea.getLeft();
        float y = swipeArea.getTop();
        swipeArea.getLocationOnScreen(location);
        Log.d("TEST", "x value: "+ location[0]);
        Log.d("TEST", "y value: "+ location[1]);
        int startY = location[1] - 40;
        int endY = location[1] - 40;
        Log.d("TEST", "startY value: "+startY);
        Log.d("TEST", "endY value: "+endY);
        Log.d("TEST", "endX value: "+(int)swipeLength);
        solo.drag(50, 70+(int)swipeLength, location[1] + 40,location[1] + 40, 20);
    }

    public void testSwipeAreaText(){
        String work,rest, workLand, restLand, expectedText, actualText;
        work = mActivity.getResources().getString(R.string.swipe_area_work);
        rest = mActivity.getResources().getString(R.string.swipe_area_rest);
        workLand = mActivity.getResources().getString(R.string.swipe_area_text_land_work);
        restLand = mActivity.getResources().getString(R.string.swipe_area_text_land_rest);
        int width = rootLayout.getMeasuredWidth();
        int height = rootLayout.getMeasuredHeight();
        int swipeLength = (RReminder.isPortrait(appContext) ? width/10*6:height/10*6);
        solo.clickOnView(timerLayout);
        instr.waitForIdleSync();
        solo.sleep(1000);
        int location[] = new int[2];
        swipeArea.getLocationOnScreen(location);
        Log.d("TEST", "x value: "+ location[0]);
        Log.d("TEST", "y value: "+ location[1]);
        expectedText =(RReminder.isPortrait(appContext)? mActivity.getResources().getString(R.string.swipe_area_text,rest) :  mActivity.getResources().getString(R.string.swipe_area_text_land,restLand));
        actualText = swipeArea.getText().toString();
        if(RReminder.isPortrait(appContext)){
            assertEquals("swipe area text in work period portrait",expectedText, actualText);
            solo.drag(50,swipeLength+100,location[1]+40, location[1]+40,20);
            solo.sleep(2000);
        } else {
            assertEquals("swipe area text in work period landscape",expectedText, actualText);
            solo.drag(location[0]+50,location[0]+50,location[1]+50, swipeLength+location[1]+100,20);
            solo.sleep(2000);
        }

        expectedText = (RReminder.isPortrait(appContext)? mActivity.getResources().getString(R.string.swipe_area_text,work) :  mActivity.getResources().getString(R.string.swipe_area_text_land,workLand));

        actualText = swipeArea.getText().toString();
        if(RReminder.isPortrait(appContext)){
            assertEquals("swipe area text in rest period portrait",expectedText, actualText);
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            solo.sleep(2000);
        } else {
            assertEquals("swipe area text in rest period landscape",expectedText, actualText);
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            solo.sleep(2000);
        }



        mActivity = (MainActivity)solo.getCurrentActivity();
        width = rootLayout.getMeasuredWidth();
        height = rootLayout.getMeasuredHeight();
        swipeLength = (RReminder.isPortrait(appContext) ? width/10*6:height/10*6);
        swipeArea = (TextView) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.swipe_area_text);
        expectedText =(RReminder.isPortrait(appContext)? mActivity.getResources().getString(R.string.swipe_area_text,work) : workLand);
        actualText = swipeArea.getText().toString();
        swipeArea.getLocationOnScreen(location);
        Log.d("TEST", "x value: "+ location[0]);
        Log.d("TEST", "y value: "+ location[1]);
        if(RReminder.isPortrait(appContext)){
            assertEquals("swipe area text in rest period portrait",expectedText, actualText);
            solo.drag(50,swipeLength+100,location[1]+40, location[1]+40,20);
            solo.sleep(2000);
        } else {
            assertEquals("swipe area text in rest period landscape",expectedText, actualText);
            solo.drag(location[0]+50,location[0]+50,location[1]+50, swipeLength+location[1]+100,20);
            solo.sleep(2000);
        }

        expectedText = (RReminder.isPortrait(appContext)? mActivity.getResources().getString(R.string.swipe_area_text,rest) :  restLand);

        actualText = swipeArea.getText().toString();
        if(RReminder.isPortrait(appContext)){
            assertEquals("swipe area text in work period portrait",expectedText, actualText);
        } else {
            assertEquals("swipe area text in work period landscape",expectedText, actualText);
        }

    }

    public void testSwipeAreaBehaviour() {
        //disable hardware keyboard option on emulator
        Log.d("TEST", "width: "+ rootLayout.getMeasuredWidth());
        int width = rootLayout.getMeasuredWidth();
        int height = rootLayout.getMeasuredHeight();
        int initialSwipeRestoreAnimCount = mActivity.swipeRestoreAnimCounter;
        int swipeLength;


        Log.d(debug, "height variable value: "+ height);
        Log.d("TEST", "width variable value: "+ width);
        solo.clickOnView(timerLayout);
        instr.waitForIdleSync();
        solo.sleep(1000);
        int initialColorId = Color.TRANSPARENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Drawable recentBackground = rootLayout.getBackground();
            if (recentBackground instanceof ColorDrawable)
                initialColorId = ((ColorDrawable) recentBackground).getColor();
        }
        solo.sleep(1000);
        String actualTitle = title.getText().toString().toUpperCase();
        assertEquals("initially the title should spell work period", expectedOnlineWorkTitle, actualTitle);
        String expectedDescription = "";
        String actualDescription = description.getText().toString();
        String initialDescription = actualDescription;
        assertEquals("the description should be empty", expectedDescription, actualDescription);
        expectedDescription = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.release_text);

        long downTime = SystemClock.uptimeMillis();
        // event time MUST be retrieved only by this way!
        long eventTime = SystemClock.uptimeMillis();
        float xStart, yStart;
        int xStartInt, yStartInt;

        // Just init your variables, or create your own coords logic :)

        //different event coordinates depending on orientation
        if(RReminder.isPortrait(appContext)){
            swipeLength = width * 6 / 10;
            xStart = 50;
            xStartInt = (int)xStart;
            yStart = (float)height - 50.0f;
            yStartInt = (int)yStart;

            //setting and determing values for each action
            float x0 = 50;
            int x0Int = (int)x0;
            int dX0 = (x0Int - xStartInt)/(swipeLength / 100);

            float x1 = 100;
            int x1Int = (int)x1;
            Log.d("TEST", "x1Int value after initialisation: "+ x1Int);
            int dX1 = (x1Int - xStartInt) / (swipeLength/100);
            Log.d("TEST", "dX1 value after initialisation: "+ dX1);

            float x2 = 150;
            int x2Int = (int)x2;
            int dX2 = (x2Int - xStartInt) / (swipeLength/100);

            float x3 = 200;
            int x3Int = (int)x3;
            int dX3 = (x3Int - xStartInt) / (swipeLength/100);

            float x4 = 250;
            int x4Int = (int)x4;
            int dX4Other = (x4Int - xStartInt) * 100 / swipeLength;
            Log.d("TEST", "dX4Other value: "+ dX4Other);
            int dX4 = (x4Int - xStartInt)/ (swipeLength/100);
            Log.d("TEST", "dX4 value: "+ dX4);

            float x5;
            if(RReminder.isTablet(appContext)){
                x5 = xStart + swipeLength/2+65;
            } else {
                x5 = xStart + swipeLength/2+50;
            }
            int x5Int = (int)x5;
            int dX5 = (x5Int - xStartInt)/(swipeLength/100);
            Log.d("TEST", "dX5 value: "+ dX5);

            float x6 = xStart + swipeLength + 50;
            float x7;
            if(RReminder.isTablet(appContext)){
                x7 = xStart + swipeLength/2+65;
            } else {
                x7 = xStart + swipeLength/2+35;
            }

            int x7int = (int)x7;
            int dX7 = (x7int - xStartInt)/(swipeLength/100);

            try {
                // sending event - finger touched the screen
                MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, xStart, yStart, 0);
                instr.sendPointerSync(event);
                // sending events - finger is moving over the screen
                eventTime = SystemClock.uptimeMillis();

                //setting up event with different coordinates depending on orientation
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x0, yStart, 0);

                instr.sendPointerSync(event);
                eventTime = SystemClock.uptimeMillis() + 1000;
                    event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x1, yStart, 0);

                instr.sendPointerSync(event);
                int expectedColorId = mActivity.getCurrentColorId(1,dX1);
                Log.d("TEST", "the value for dX1: "+ dX1);
                int actualColorId = Color.TRANSPARENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    Log.d("MAIN_ACTIVITY_TEST", "build version bigger than honeycomb");
                    Drawable recentBackground = rootLayout.getBackground();
                    if (recentBackground instanceof ColorDrawable){
                        Log.d("MAIN_ACTIVITY_TEST", "getting actual color");
                        actualColorId = ((ColorDrawable) recentBackground).getColor();
                    }

                }
                assertEquals("during swipe the background color should change according to getCurrentColorId", expectedColorId, actualColorId);
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x2, yStart, 0);
                instr.sendPointerSync(event);
                eventTime = SystemClock.uptimeMillis() + 1000;

                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x3, yStart, 0);

                instr.sendPointerSync(event);
                expectedColorId = mActivity.getCurrentColorId(1,dX3);
                Log.d("TEST", "the value for dX3: "+ dX3);
                actualColorId = Color.TRANSPARENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    Drawable recentBackground = rootLayout.getBackground();
                    if (recentBackground instanceof ColorDrawable)
                        actualColorId = ((ColorDrawable) recentBackground).getColor();
                }
                assertEquals("during swipe the background color should change according to getCurrentColorId", expectedColorId, actualColorId);
                eventTime = SystemClock.uptimeMillis() + 1000;

                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x4, yStart, 0);
                instr.sendPointerSync(event);
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x5, yStart, 0);
                instr.sendPointerSync(event);
                actualTitle = title.getText().toString().toUpperCase();
                expectedColorId = mActivity.getCurrentColorId(2,dX5);
                Log.d(debug, "color id dX5: "+expectedColorId);
                actualColorId = Color.TRANSPARENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    Drawable recentBackground = rootLayout.getBackground();
                    if (recentBackground instanceof ColorDrawable)
                        actualColorId = ((ColorDrawable) recentBackground).getColor();
                }
                Log.d(debug, "actual color for dX5: "+actualColorId);
                assertEquals("after passing the half of the required swipe length the title should be changed", expectedOnlineRestTitle, actualTitle);
                assertEquals("during swipe dX5 stage (>50) the background color should change according to getCurrentColorId", expectedColorId, actualColorId);

                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x6, yStart, 0);
                instr.sendPointerSync(event);
                actualDescription = description.getText().toString();
                assertEquals("after exceeding required swipe lenght, the message should appear", expectedDescription, actualDescription);
                eventTime = SystemClock.uptimeMillis() + 1500;


                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x7, yStart, 0);
                instr.sendPointerSync(event);
                expectedColorId = mActivity.getCurrentColorId(2,dX7);
                Log.d(debug, "delta x from test: "+ (x7-xStart));
                Log.d("TEST", "the value for dX7 (reverse direction): "+ dX7);
                Log.d(debug, "expected color id dX7 reverse: "+expectedColorId);
                actualColorId = Color.TRANSPARENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    Drawable recentBackground = rootLayout.getBackground();
                    if (recentBackground instanceof ColorDrawable)
                        actualColorId = ((ColorDrawable) recentBackground).getColor();
                }
                Log.d(debug, "actual color id dX7 reverse: "+actualColorId);
                assertEquals("during swipe (reversed direction) the background color should change according to getCurrentColorId", expectedColorId, actualColorId);
                eventTime = SystemClock.uptimeMillis() + 1500;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x4, yStart, 0);
                instr.sendPointerSync(event);
                actualDescription = description.getText().toString();
                assertEquals("after decreasing the swipe lenght to be less than the requirement the description should return to initial content", initialDescription, actualDescription);
                actualTitle = title.getText().toString().toUpperCase();
                assertEquals("after decreasing the swipe lenght to be less than the requirement for title change the title changed back to initial value", expectedOnlineWorkTitle, actualTitle);

                //repeating reaching the required swipe lenght to test if description is working as expected
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x5, yStart, 0);
                instr.sendPointerSync(event);
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x6, yStart, 0);
                instr.sendPointerSync(event);
                actualDescription = description.getText().toString();
                assertEquals("after repeatedly exceeding required swipe lenght, the message should appear", expectedDescription, actualDescription);
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x4, yStart, 0);
                instr.sendPointerSync(event);
                actualDescription = description.getText().toString();
                assertEquals("after decreasing (for second time) the swipe lenght to be less than the requirement the description should return to initial content", initialDescription, actualDescription);

                // release finger, gesture is finished
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x4, yStart, 0);
                instr.sendPointerSync(event);
            } catch (Exception ignored) {
                // Handle exceptions if necessary
            }


        } else {
            //coordinates,values and motion events for landscape orientation
            swipeLength = height * 6 / 10;
            xStart = (float)width - 50.0f;
            yStart = 280;
            yStartInt = (int)yStart;
            xStartInt = (int)xStart;

            Log.d(debug, "y coordinate: "+yStart);
            Log.d(debug, "swipeLenght: "+swipeLength);
            // simulating thick finger touch
            //current values are set for very large screen size
            float y0 = 280;
            int y0Int = (int)y0;
            int dY0 = (y0Int - yStartInt)/(swipeLength / 100);

            float y1 = 300;
            int y1Int = (int)y1;
            //getting the value for color id
            int dY1 = (y1Int - yStartInt) / (swipeLength/100);


            float y2 = 350;
            int y2Int = (int)y2;
            int dY2 = (y2Int - yStartInt) / (swipeLength/100);

            float y3 = 400;
            int y3Int = (int)y3;
            int dY3 = (y3Int - yStartInt) / (swipeLength/100);

            float y4 = 450;
            int y4Int = (int)y4;
            int dY4Other = (y4Int - yStartInt) * 100 / swipeLength;
            Log.d("TEST", "dX4Other value: "+ dY4Other);
            int dY4 = (y4Int - yStartInt)/ (swipeLength/100);
            Log.d("TEST", "dY4 value: "+ dY4);

            float y5 = yStart + swipeLength/2+68;
            int y5Int = (int)y5;
            int dY5 = (y5Int - yStartInt)/(swipeLength/100);
            Log.d("TEST", "dY5 value: "+ dY5);

            float y6 =yStart + swipeLength + 50;
            float y7;
            if(RReminder.isTablet(appContext)){
                y7 = yStart + swipeLength/2+65;
            } else {
                y7 = xStart + swipeLength/2+68;
            }
            int y7int = (int)y7;
            int dY7 = (y7int - yStartInt)/(swipeLength/100);

            try {
                // sending event - finger touched the screen
                MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, xStart, yStart, 0);
                instr.sendPointerSync(event);
                // sending events - finger is moving over the screen
                eventTime = SystemClock.uptimeMillis();

                //setting up event with different coordinates depending on orientation
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y0, 0);

                instr.sendPointerSync(event);
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y1, 0);

                instr.sendPointerSync(event);
                int expectedColorId = mActivity.getCurrentColorId(1,dY1);
                Log.d("TEST", "the value for dY1: "+ dY1);
                int actualColorId = Color.TRANSPARENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    Log.d("MAIN_ACTIVITY_TEST", "build version bigger than honeycomb");
                    Drawable recentBackground = rootLayout.getBackground();
                    if (recentBackground instanceof ColorDrawable){
                        Log.d("MAIN_ACTIVITY_TEST", "getting actual color");
                        actualColorId = ((ColorDrawable) recentBackground).getColor();
                    }

                }
                assertEquals("during swipe the background color should change according to getCurrentColorId", expectedColorId, actualColorId);
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y2, 0);
                instr.sendPointerSync(event);
                eventTime = SystemClock.uptimeMillis() + 1000;

                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y3, 0);

                instr.sendPointerSync(event);
                expectedColorId = mActivity.getCurrentColorId(1,dY3);
                Log.d("TEST", "the value for dY3: "+ dY3);
                actualColorId = Color.TRANSPARENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    Drawable recentBackground = rootLayout.getBackground();
                    if (recentBackground instanceof ColorDrawable)
                        actualColorId = ((ColorDrawable) recentBackground).getColor();
                }
                assertEquals("during swipe the background color should change according to getCurrentColorId", expectedColorId, actualColorId);
                eventTime = SystemClock.uptimeMillis() + 1000;

                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y4, 0);
                instr.sendPointerSync(event);
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y5, 0);
                instr.sendPointerSync(event);
                actualTitle = title.getText().toString().toUpperCase();
                expectedColorId = mActivity.getCurrentColorId(2,dY5);
                Log.d(debug, "color id dY5: "+expectedColorId);
                actualColorId = Color.TRANSPARENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    Drawable recentBackground = rootLayout.getBackground();
                    if (recentBackground instanceof ColorDrawable)
                        actualColorId = ((ColorDrawable) recentBackground).getColor();
                }
                Log.d(debug, "actual color for dY5: "+actualColorId);
                assertEquals("after passing the half of the required swipe length the title should be changed", expectedOnlineRestTitle, actualTitle);
                assertEquals("during swipe dY5 stage (>50) the background color should change according to getCurrentColorId", expectedColorId, actualColorId);

                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y6, 0);
                instr.sendPointerSync(event);
                actualDescription = description.getText().toString();
                assertEquals("after exceeding required swipe lenght, the message should appear", expectedDescription, actualDescription);
                eventTime = SystemClock.uptimeMillis() + 1500;


                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y7, 0);
                instr.sendPointerSync(event);
                expectedColorId = mActivity.getCurrentColorId(2,dY7);
                Log.d("TEST", "the value for dX7 (reverse direction): "+ dY7);
                Log.d(debug, "expected color id dX7 reverse: "+expectedColorId);
                actualColorId = Color.TRANSPARENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    Drawable recentBackground = rootLayout.getBackground();
                    if (recentBackground instanceof ColorDrawable)
                        actualColorId = ((ColorDrawable) recentBackground).getColor();
                }
                Log.d(debug, "actual color id dY7 reverse: "+actualColorId);
                assertEquals("during swipe (reversed direction) the background color should change according to getCurrentColorId", expectedColorId, actualColorId);
                eventTime = SystemClock.uptimeMillis() + 1500;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y4, 0);
                instr.sendPointerSync(event);
                actualDescription = description.getText().toString();
                assertEquals("after decreasing the swipe lenght to be less than the requirement the description should return to initial content", initialDescription, actualDescription);
                actualTitle = title.getText().toString().toUpperCase();
                assertEquals("after decreasing the swipe lenght to be less than the requirement for title change the title changed back to initial value", expectedOnlineWorkTitle, actualTitle);

                //repeating reaching the required swipe lenght to test if description is working as expected
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y5, 0);
                instr.sendPointerSync(event);
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y6, 0);
                instr.sendPointerSync(event);
                actualDescription = description.getText().toString();
                assertEquals("after repeatedly exceeding required swipe lenght, the message should appear", expectedDescription, actualDescription);
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y4, 0);
                instr.sendPointerSync(event);
                actualDescription = description.getText().toString();
                assertEquals("after decreasing (for second time) the swipe lenght to be less than the requirement the description should return to initial content", initialDescription, actualDescription);

                // release finger, gesture is finished
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, xStart, y4, 0);
                instr.sendPointerSync(event);
            } catch (Exception ignored) {
                // Handle exceptions if necessary
            }
        }




        int updatedSwipeRestoreAnimCounter = mActivity.swipeRestoreAnimCounter;
        assertTrue("the swipe animate restore function had been called", (updatedSwipeRestoreAnimCounter == initialSwipeRestoreAnimCount + 1));
        solo.sleep(1000);
        int actualColorId = Color.TRANSPARENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Drawable recentBackground = rootLayout.getBackground();
            if (recentBackground instanceof ColorDrawable)
                actualColorId = ((ColorDrawable) recentBackground).getColor();
        }
        Log.d("TEST", "the int value of work background color is: "+ appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.work));
        assertEquals("after performing swipe gesture and releasing while not reaching the required swipe distance the background color has been returned to initial stage", initialColorId, actualColorId);

    }



    public void testClicksOnActiveMode(){
        int recentColor = Color.TRANSPARENT;
        int initialColor = Color.TRANSPARENT;
        solo.sleep(2000);
        solo.clickOnView(timerLayout);
        instr.waitForIdleSync();
        String titleText = title.getText().toString();
        solo.sleep(2000);
        boolean countdownIsRunning = mActivity.countdown.isRunning;
        boolean swipeIsVisible = swipeArea.isShown();
        //method of getting the background color - ony available in API 11+

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Drawable background = rootLayout.getBackground();
            if (background instanceof ColorDrawable)
                initialColor = ((ColorDrawable) background).getColor();
        }

        solo.clickOnView(timerLayout);
        instr.waitForIdleSync();
        solo.sleep(5000);
        solo.clickOnView(timerLayout);
        instr.waitForIdleSync();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Drawable recentBackground = rootLayout.getBackground();
            if (recentBackground instanceof ColorDrawable)
                recentColor = ((ColorDrawable) recentBackground).getColor();
        }


        assertEquals("the title should be unchanged", titleText, title.getText().toString());
        assertEquals("the countdown should be active", countdownIsRunning, mActivity.countdown.isRunning);
        assertEquals("the swipe area still should be visible", swipeIsVisible, swipeArea.isShown());
        assertEquals("the background color should be unchainged", initialColor, recentColor);


    }

    public void testInactiveDigitColors(){
        SharedPreferences.Editor editor   = preferences.edit();
        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), "02:00");
        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_key), "00:01");
        editor.commit();
        int expectedInactiveColor, expectedActiveColor;
        expectedInactiveColor = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.inactive_digit);
        expectedActiveColor = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.black);
        assertEquals("at launch hour1 should be inactive", expectedInactiveColor, timerHour1.getCurrentTextColor());
        assertEquals("at launch hour2 should be inactive", expectedInactiveColor, timerHour2.getCurrentTextColor());
        assertEquals("at launch colon should be active", expectedActiveColor, timerColon.getCurrentTextColor());
        assertEquals("at launch minute1 should be active", expectedActiveColor, timerMinute1.getCurrentTextColor());
        assertEquals("at launch minute2 should be active", expectedActiveColor, timerMinute2.getCurrentTextColor());
        assertEquals("at launch point should be active", expectedActiveColor, timerPoint.getCurrentTextColor());
        assertEquals("at launch second1 should be active", expectedActiveColor, timerSecond1.getCurrentTextColor());
        assertEquals("at launch second2 should be active", expectedActiveColor, timerSecond2.getCurrentTextColor());

        solo.clickOnView(timerLayout);
        instr.waitForIdleSync();

        assertEquals("after starting active mode hour1 should be inactive", expectedInactiveColor, timerHour1.getCurrentTextColor());
        assertEquals("after starting active mode hour2 should be inactive", expectedInactiveColor, timerHour2.getCurrentTextColor());
        assertEquals("after starting active mode colon should be inactive", expectedInactiveColor, timerColon.getCurrentTextColor());
        assertEquals("after starting active mode minute1 should be inactive", expectedInactiveColor, timerMinute1.getCurrentTextColor());
        assertEquals("after starting active mode minute2 should be inactive", expectedInactiveColor, timerMinute2.getCurrentTextColor());
        assertEquals("after starting active mode point should be active", expectedActiveColor, timerPoint.getCurrentTextColor());
        assertEquals("after starting active mode second1 should be active", expectedActiveColor, timerSecond1.getCurrentTextColor());
        assertEquals("after starting active mode second2 should be active", expectedActiveColor, timerSecond2.getCurrentTextColor());

        Instrumentation.ActivityMonitor monitor = instr.addMonitor(NotificationActivity.class.getName(), null, false);

        Activity nActivity = instr.waitForMonitor(monitor);
        solo.clickOnButton("Close");
        solo.waitForActivity(MainActivity.class,5000);
        solo.assertCurrentActivity("main activity should be open", MainActivity.class);
        instr.waitForIdleSync();

        assertEquals("in two hour long period hour1 should be active", expectedActiveColor, timerHour1.getCurrentTextColor());
        assertEquals("in two hour long period hour2 should be active", expectedActiveColor, timerHour2.getCurrentTextColor());
        assertEquals("in two hour long period colon should be active", expectedActiveColor, timerColon.getCurrentTextColor());
        assertEquals("in two hour long period minute1 should be active", expectedActiveColor, timerMinute1.getCurrentTextColor());
        assertEquals("in two hour long period minute2 should be active", expectedActiveColor, timerMinute2.getCurrentTextColor());
        assertEquals("in two hour long period point should be active", expectedActiveColor, timerPoint.getCurrentTextColor());
        assertEquals("in two hour long period second1 should be active", expectedActiveColor, timerSecond1.getCurrentTextColor());
        assertEquals("in two hour long period second2 should be active", expectedActiveColor, timerSecond2.getCurrentTextColor());
    }

    public void testTimerButtonStartProcess(){
        assertFalse("extend button should be invisible in inactive mode", extendButton.isShown());
        assertFalse("swipe area should be invisible in inactive mode", swipeArea.isShown());
        assertEquals("title was expected to be and actual it was", expectedOfflineTitle, actualOfflineTitle);
        assertFalse("at the launch of app the service shoulnt be running", RReminderMobile.isCounterServiceRunning(appContext));
        assertTrue("the countdown timer should not run at the launch of app", mActivity.countdown == null);

        Intent intent = new Intent (appContext, MobileOnAlarmReceiver.class);
        intent.putExtra(RReminder.PERIOD_TYPE, 1);
        intent.putExtra(RReminder.EXTEND_COUNT, 0);
        intent.setAction(RReminder.ACTION_ALARM_PERIOD_END);
        solo.sleep(2000);
        boolean alarmUp = (PendingIntent.getBroadcast(appContext, 0, intent,PendingIntent.FLAG_NO_CREATE) != null);
        Log.d(debug, "is alarm up in offline mode: "+alarmUp);
        if(alarmUp){
            Log.d(debug, "PendingIntent at the start: "+PendingIntent.getBroadcast(appContext, 0, intent,PendingIntent.FLAG_NO_CREATE).toString());
        }

        assertFalse("the alarm manager shouldn't be running when app is lauched", alarmUp);

        solo.clickOnView(timerLayout);
        instr.waitForIdleSync();
        solo.sleep(5000);
        alarmUp = (PendingIntent.getBroadcast(appContext, (int)mActivity.periodEndTimeValue, intent,PendingIntent.FLAG_ONE_SHOT) != null);
        assertTrue("the alarm is set", alarmUp);
        if (alarmUp) {
            Log.d(debug, "Alarm is already active");
            Log.d(debug, "PendingIntent after lauching reminder: "+PendingIntent.getBroadcast(appContext, (int)mActivity.periodEndTimeValue, intent,PendingIntent.FLAG_ONE_SHOT).toString());
        }
        assertTrue("after pressing timer button the service should be started", RReminderMobile.isCounterServiceRunning(appContext));
        assertTrue("the countdown timer should  run after pressing button", mActivity.countdown.isRunning);
        actualOnlineTitle = title.getText().toString().toUpperCase();
        assertEquals("after launching reminder the title should be work period",expectedOnlineWorkTitle,actualOnlineTitle);
        assertTrue("swipe area should be visible in active mode", swipeArea.isShown());

        solo.sleep(5000);
        Bundle data =  mActivity.getDataFromService();
        long periodEndTime = data.getLong(RReminder.PERIOD_END_TIME);
        long remainingTime = periodEndTime - Calendar.getInstance().getTimeInMillis();
        int timeInSeconds = Math.round(remainingTime / 1000);
        int seconds = timeInSeconds % 60;
        int timeInMinutes  = timeInSeconds / 60;
        int  minutes = timeInMinutes % 60;
        int hours = timeInMinutes / 60;
        String secondString, minuteString, hourString, serviceHour, serviceMinute, serviceSecond;
        if (seconds <10)
        {
            serviceSecond = "0"+ seconds;
        } else {
            serviceSecond = ""+seconds;
        }

        if(minutes<10){
            serviceMinute = "0" + minutes;
        } else {
            serviceMinute = ""+ minutes;
        }

        if(hours<10){
            serviceHour = "0" + hours;
        } else {
            serviceHour = ""+ hours;
        }

        hourString = timerHour1.getText().toString() + timerHour2.getText().toString();
        minuteString = timerMinute1.getText().toString() + timerMinute2.getText().toString();
        secondString = timerSecond1.getText().toString() + timerSecond2.getText().toString();

        assertEquals( "hour values should match", serviceHour, hourString);
        assertEquals("minute values should match", serviceMinute, minuteString);
        assertTrue( "second values should match", (Math.abs(Integer.parseInt(serviceSecond) - seconds)<=1));
    }

    public void testDescription(){
        int expectedColor, actualColor, expectedSize, actualSize;
        SharedPreferences.Editor editor   = preferences.edit();
        editor.putBoolean(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.commit();
        instr.waitForIdleSync();
        String expectedDescription, actualDescription;
        expectedDescription = "";
        actualDescription = description.getText().toString();
        assertEquals("in offline mode desciprion should be empty", expectedDescription, actualDescription);
        solo.clickOnView(timerLayout);
        instr.waitForIdleSync();
        actualDescription = description.getText().toString();
        assertEquals("right after launching reminder the desciprion should be empty", expectedDescription, actualDescription);

        instr.waitForIdleSync();
        solo.clickOnView(extendButton);
        instr.waitForIdleSync();
        solo.clickOnButton("Extend for 5 minutes");
        instr.waitForIdleSync();
        expectedDescription = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.description_extended_one_time);
        actualDescription = description.getText().toString();
        assertEquals("after extend the desciprion should say extended", expectedDescription, actualDescription);
        expectedColor = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.black);
        actualColor = description.getCurrentTextColor();
        assertEquals("the color of description", expectedColor, actualColor);
        if(RReminder.isTablet(appContext)){
            expectedSize = 28;
        } else {
            expectedSize = (RReminder.isPortrait(appContext) ? 20:18);
        }
        actualSize = (int)(description.getTextSize()/scaledDensity);
        assertEquals("the description font size",expectedSize,actualSize);





        solo.clickOnView(extendButton);
        instr.waitForIdleSync();
        solo.clickOnButton("Extend for 5 minutes");
        instr.waitForIdleSync();
        expectedDescription = String.format(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.description_extended),2);
        actualDescription = description.getText().toString();
        assertEquals("after 2 extends the desciprion should say extended 2 times", expectedDescription, actualDescription);

        solo.clickOnView(extendButton);
        instr.waitForIdleSync();
        solo.clickOnButton("Extend for 5 minutes");
        instr.waitForIdleSync();
        int extendCount = mActivity.extendCount;
        expectedDescription = String.format(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.description_extended),3);
        actualDescription = description.getText().toString();
        assertEquals("after 3 extends the desciprion should say extended 3 times", expectedDescription, actualDescription);


    }

    public void testDescriptionEnoughSpace(){

        editor.putBoolean(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.commit();

        solo.clickOnView(timerLayout);
        instr.waitForIdleSync();

        solo.clickOnView(extendButton);
        instr.waitForIdleSync();

        solo.clickOnButton("Extend for 5 minutes");
        instr.waitForIdleSync();

        solo.clickOnView(extendButton);
        instr.waitForIdleSync();

        solo.clickOnButton("Extend for 5 minutes");
        instr.waitForIdleSync();

        solo.sleep(7000);

        Rect rectf = new Rect();
        description.getLocalVisibleRect(rectf);

        char[] thin_symbols =  {'1','t','i','I','j','l', '!', '^', '(', ')', '[', ']', '{', '}', ';', ':', '|', ',', '.'  };
        float lengthSum = 0.0f;

        CharSequence descrText = description.getText();

        int descriptionLength = description.getText().length();
        boolean isThin = false;
        int thinCount = 0;
        int textSize = (RReminder.isPortrait(appContext)) ? 20:18;

        for( int i = 0; i<descriptionLength;i++){
            isThin = false;
            for(int j=0;j<thin_symbols.length;j++){
                if(descrText.charAt(i) == thin_symbols[j]){
                    isThin = true;
                    thinCount++;
                    break;
                }
            }
            if(isThin == false){
                lengthSum+=1.0;
            } else {
                lengthSum+=0.2;
            }
        }

        float descriptionTextWidth = lengthSum * 10.3f * scaledDensity;
        Log.d(debug, "number of thin symbols: "+ thinCount);
        Log.d(debug, "description text width: " + descriptionTextWidth);

        float charWidth = 9 * scaledDensity;
        int charAmountInRow = (int)(rectf.width()/charWidth);
        Log.d(debug, "numbers of chars in a row: " + charAmountInRow);
        int expectedRowCount = (int)descriptionTextWidth / rectf.width();

        expectedRowCount+=1;
        Log.d(debug, "expected row count: " + expectedRowCount);
        int expectedDescriptionHeight = (int)(textSize* scaledDensity * expectedRowCount + 5f);

        solo.sleep(5000);
        Log.d(debug, "actual description height: "+ rectf.height());
        Log.d(debug, "expected description height: "+expectedDescriptionHeight);

        assertTrue("the actual description height should match the expected height", rectf.height()>=expectedDescriptionHeight);
        Log.d(debug, "description symbol count" + descriptionLength);

    }

    public void testServiceUpdatedAfterExtending(){
        solo.clickOnView(timerLayout);
        instr.waitForIdleSync();
        solo.sleep(1000);
        Bundle serviceData = mActivity.getDataFromService();
        int initPeriodType = serviceData.getInt(RReminder.PERIOD_TYPE);
        int initExtendCount = serviceData.getInt(RReminder.EXTEND_COUNT);
        long initPeriodEndTimeValue = serviceData.getLong(RReminder.PERIOD_END_TIME);
        Instrumentation.ActivityMonitor monitor = instr.addMonitor(NotificationActivity.class.getName(), null, false);

        Activity nActivity = instr.waitForMonitor(monitor);
        solo.assertCurrentActivity("notification activity should be open", NotificationActivity.class);
        Button nExtendButton  = (Button)nActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.button_notification_period_end_extend);
        solo.clickOnView(nExtendButton);
        instr.waitForIdleSync();
        String optionOneButton =  String.format(mActivity.getResources().getString(R.string.extend_dialog_button),extendBaseLength);
        solo.clickOnButton(optionOneButton);

        long extendedTimeStamp = Calendar.getInstance().getTimeInMillis();
        long extendedPeriodEndTime = extendedTimeStamp + 60000*preferences.getInt(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key),5);

        instr.waitForIdleSync();
        solo.assertCurrentActivity("main activity should be open", MainActivity.class);

        mActivity = (MainActivity) solo.getCurrentActivity();
        solo.sleep(4000);
        int extendedPeriodType = mActivity.periodType;
        int extendedExtendCount = mActivity.extendCount;
        long extendedPeriodEndTimeValue = mActivity.periodEndTimeValue;
        int expectedPeriodType = 3;

        assertEquals("after extending the period, the updated value should remain the same", expectedPeriodType, extendedPeriodType);
        assertEquals("atfer extending period, the extend count should be increased by 1", initExtendCount + 1, extendedExtendCount);
        Log.d(debug, "the error of extended period end value: "+ Math.abs(extendedPeriodEndTimeValue - extendedPeriodEndTime));
        assertTrue("after extending period the period end time is properly updated",Math.abs(extendedPeriodEndTimeValue - extendedPeriodEndTime) < 1000);
    }


    public void testExtendDialogIsClosedInNotificationAfterPeriodEnds(){
        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_key), "00:02");
        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), "00:01");
        editor.commit();
        solo.clickOnView(timerLayout);
        instr.waitForIdleSync();
        solo.sleep(1000);
        Bundle serviceData = mActivity.getDataFromService();
        int initPeriodType = serviceData.getInt(RReminder.PERIOD_TYPE);
        int initExtendCount = serviceData.getInt(RReminder.EXTEND_COUNT);
        long initPeriodEndTimeValue = serviceData.getLong(RReminder.PERIOD_END_TIME);
        Instrumentation.ActivityMonitor monitorNotification = instr.addMonitor(NotificationActivity.class.getName(), null, false);

        Activity nActivity = instr.waitForMonitor(monitorNotification);
        String extendDialogTitle = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_title);
        solo.assertCurrentActivity("notification activity should be open", NotificationActivity.class);
        Button nExtendButton  = (Button)nActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.button_notification_period_end_extend);
        solo.clickOnView(nExtendButton);
        instr.waitForIdleSync();
        assertTrue("the extend dialog title is visible", solo.searchText(extendDialogTitle));
        solo.sleep(85000);
        // veikt parbaudi, meklejot dialoga title
        assertFalse("the extend dialog title should not be visible", solo.searchText(extendDialogTitle));
        String extendButtonTitle = mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_current_period);
        solo.clickOnButton(extendButtonTitle);
        instr.waitForIdleSync();
        solo.sleep(2000);
        assertTrue("the extend dialog title is visible", solo.searchText(extendDialogTitle));


    }

    public void testNotificationCloseAppInFocus(){

        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), "00:02");
        editor.commit();
        solo.clickOnView(timerLayout);

        Instrumentation.ActivityMonitor monitor = instr.addMonitor(NotificationActivity.class.getName(), null, false);

        Activity nActivity = instr.waitForMonitor(monitor);

        solo.assertCurrentActivity("notification activity should be open", NotificationActivity.class);

        Button closeButton = (Button) nActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_button);
        solo.clickOnView(closeButton);
        instr.waitForIdleSync();
        solo.assertCurrentActivity("main activity should be open", MainActivity.class);
        String expectedTitle = expectedOnlineRestTitle;
        String actualTitle = title.getText().toString().toUpperCase();

        int expectedBgColor = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.rest);
        int actualBgColor = Color.TRANSPARENT;
        Log.d("MAIN_ACTIVITY_TEST", "transparent color value: "+actualBgColor);
        solo.sleep(1000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Drawable background = rootLayout.getBackground();
            if (background instanceof ColorDrawable)
                actualBgColor = ((ColorDrawable) background).getColor();
        }
        Log.d("MAIN_ACTIVITY_TEST", "bg color value: "+actualBgColor);
        solo.sleep(1000);
        assertEquals("the bg color should be blue (rest)", expectedBgColor, actualBgColor);
        assertEquals("after closing notification, main activity should display rest period", expectedTitle, actualTitle);



    }

    public void testNotificationCloseAppInFocusManual(){
        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_key), "1");
        editor.putString(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), "00:02");
        editor.commit();
        solo.clickOnView(timerLayout);
        solo.sleep(2000);
        assertTrue("after pressing timer button the service should be started", RReminderMobile.isCounterServiceRunning(appContext));
        Instrumentation.ActivityMonitor monitor = instr.addMonitor(NotificationActivity.class.getName(), null, false);

        Activity nActivity = instr.waitForMonitor(monitor);
        solo.sleep(2000);
        Button closeButton = (Button) nActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_button);


        solo.clickOnView(closeButton);
        instr.waitForIdleSync();
        solo.assertCurrentActivity("main activity should be open", MainActivity.class);
        String expectedTitle = expectedOnlineRestTitle;
        String actualTitle = title.getText().toString().toUpperCase();
        assertEquals("after extending previously ended period, the active period is work period", expectedTitle, actualTitle);

        int expectedBgColor = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.rest);
        int actualBgColor = Color.TRANSPARENT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Drawable background = rootLayout.getBackground();
            if (background instanceof ColorDrawable)
                actualBgColor = ((ColorDrawable) background).getColor();
        }

        assertEquals("the bg color should be blue (rest)", expectedBgColor, actualBgColor);

    }

    public void testOnGoingNotificationIntent(){
        solo.clickOnView(timerLayout);
        solo.sleep(2000);

        //solo.clickOnMenuItem("Settings",true);
        Intent intent = new Intent(appContext,PreferenceActivity.class);
        mActivity.startActivity(intent);
        Instrumentation.ActivityMonitor monitor = instr.addMonitor(PreferenceActivity.class.getName(), null, false);

        PreferenceActivity pActivity = (PreferenceActivity) instr.waitForMonitor(monitor);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);
        solo.sleep(1000);

        Intent notificationIntent = new Intent(appContext, MainActivity.class);
        notificationIntent.setAction(RReminder.ACTION_VIEW_MAIN_ACTIVITY);
        notificationIntent.putExtra(RReminder.START_COUNTER, false);

        mActivity.startActivity(notificationIntent);
        solo.sleep(1000);

        solo.assertCurrentActivity("main activity should be open", MainActivity.class);
        solo.sleep(1000);
    }

    public void testOnGoingNotificationActionIntent(){
        solo.clickOnView(timerLayout);
        solo.sleep(2000);
        //solo.clickOnMenuItem(mActivity.getResources().getString(R.string.menu_settings),true);
        Intent intent = new Intent(appContext,PreferenceActivity.class);
        mActivity.startActivity(intent);
        Instrumentation.ActivityMonitor monitor = instr.addMonitor(PreferenceActivity.class.getName(), null, false);

        PreferenceActivity pActivity = (PreferenceActivity) instr.waitForMonitor(monitor);

        solo.assertCurrentActivity("preference activity should be open", PreferenceActivity.class);
        solo.sleep(1000);

        Intent turnOffIntent = new Intent (appContext, MainActivity.class);
        turnOffIntent.setAction(RReminder.ACTION_TURN_OFF);
        turnOffIntent.putExtra(RReminder.TURN_OFF, 1);

        mActivity.startActivity(turnOffIntent);
        solo.sleep(1000);

        assertFalse("the countdown timer is cancelled after turn off action", mActivity.countdown.isRunning);
        solo.sleep(1000);
    }

    public void testTimerStopOnPause(){
        assertTrue("at the start the timer shouldnt be active", mActivity.countdown == null);
        solo.clickOnView(timerLayout);
        solo.sleep(2000);

        assertTrue("after launching reminder the countdown timer should be active", mActivity.countdown != null && mActivity.countdown.isRunning);

        mActivity.runOnUiThread(
                new Runnable() {
                    public void run() {
                        instr.callActivityOnPause(mActivity);
                        instr.callActivityOnStop(mActivity);
                    }
                });
        solo.sleep(2000);

        assertTrue("after onPause was called, the countdown timer should be inactive", !mActivity.countdown.isRunning);

        mActivity.runOnUiThread(
                new Runnable() {
                    public void run() {
                        instr.callActivityOnStart(mActivity);
                        instr.callActivityOnResume(mActivity);
                    }
                });

        solo.sleep(2000);
        assertTrue("after onResume was called the countdown timer should be active", mActivity.countdown.isRunning);
    }

    public void testDialogCloseTimerResume(){
        assertTrue("at the start the timer shouldnt be active", mActivity.countdown == null);
        solo.clickOnView(timerLayout);
        instr.waitForIdleSync();
        solo.sleep(2000);
        assertTrue("after launching reminder the countdown timer should be active", mActivity.countdown != null && mActivity.countdown.isRunning);
        solo.clickOnView(extendButton);
        instr.waitForIdleSync();
        solo.sleep(2000);
        assertTrue("after opening extend dialog, the countdown timer should be inactive", !mActivity.countdown.isRunning);

        solo.clickOnButton("Cancel");
        solo.sleep(2000);

        assertTrue("after closing extend dialog, the countdown timer should be active", mActivity.countdown.isRunning);

    }



}