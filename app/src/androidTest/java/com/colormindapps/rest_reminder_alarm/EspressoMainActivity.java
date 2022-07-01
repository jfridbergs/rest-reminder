package com.colormindapps.rest_reminder_alarm;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

import androidx.fragment.app.DialogFragment;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.filters.LargeTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.hamcrest.core.IsNot.not;

/**
* Created by ingressus on 09/02/2017.
        */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoMainActivity {

    Context appContext;
    SharedPreferences preferences, privatePreferences;
    SharedPreferences.Editor editor, privateEditor;
    float scaledDensity;
    int extendOptionCount, extendBaseLength, versionCode;
    String expectedOfflineTitle, expectedOnlineWorkTitle, expectedOnlineRestTitle;

    String debug = "TEST_MAIN_ACTIVITY";

    @Rule public IntentsTestRule<MainActivity> mActivityRule =
            new IntentsTestRule<MainActivity>(MainActivity.class);

    @Before
    public void setUp(){
        Log.d(debug, "setUp");
        appContext = getApplicationContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        privatePreferences = mActivityRule.getActivity().getSharedPreferences(RReminder.PRIVATE_PREF, Context.MODE_PRIVATE);
        editor   = preferences.edit();
        privateEditor = privatePreferences.edit();
        editor.putString(getResourceString(R.string.pref_work_period_length_key), "15:55");
        editor.putString(getResourceString(R.string.pref_rest_period_length_key), "15:55");
        editor.putInt(getResourceString(R.string.pref_period_extend_options_key), 3);
        editor.putInt(getResourceString(R.string.pref_period_extend_length_key), 5);
        editor.putBoolean(getResourceString(R.string.pref_enable_extend_key), true);
        editor.commit();
        scaledDensity = getApplicationContext().getResources().getDisplayMetrics().scaledDensity;

        extendOptionCount = preferences.getInt(getResourceString(R.string.pref_period_extend_options_key), 1);
        extendBaseLength = preferences.getInt(getResourceString(R.string.pref_period_extend_length_key),5);
        versionCode = preferences.getInt(RReminder.VERSION_KEY, 0);
        expectedOfflineTitle = getResourceString(R.string.reminder_off_title);
        expectedOnlineWorkTitle = getResourceString(R.string.on_work_period).toUpperCase();
        expectedOnlineRestTitle = getResourceString(R.string.on_rest_period).toUpperCase();
    }

    @After
    public void tearDown(){
        Log.d(debug, "tearDown");
        MainActivity mActivity;
        mActivity = mActivityRule.getActivity();
        RReminderMobile.cancelCounterAlarm(appContext, mActivity.periodType, mActivity.extendCount, mActivity.periodEndTimeValue);
        RReminderMobile.stopCounterService(appContext, mActivity.periodType);

        editor.putBoolean(mActivity.getResources().getString(R.string.pref_enable_extend_key), true);
        editor.putString(mActivity.getResources().getString(R.string.pref_mode_key), "0");
        editor.putBoolean(mActivity.getResources().getString(R.string.pref_end_period_key), true);
        editor.putString(mActivity.getResources().getString(R.string.pref_rest_period_length_key), RReminder.DEFAULT_REST_PERIOD_STRING);
        editor.putString(mActivity.getResources().getString(R.string.pref_work_period_length_key), "00:45");
        editor.putString(mActivity.getResources().getString(R.string.pref_rest_period_length_key), "00:15");
        editor.putInt(RReminder.VERSION_KEY, versionCode);
        editor.commit();
        appContext = null;
        mActivity = null;
    }

    @Test
    public void launchReminder() {
        Log.d(debug, "test1");
        onView(withId(R.id.period_title)).check(matches(withText(expectedOfflineTitle)));
        onView(withId(R.id.timer_layout)).perform(click());
        onView(withId(R.id.period_title)).check(matches(withText(expectedOnlineWorkTitle)));
    }

    @Test
    public void testPreconditions(){
        Log.d(debug, "test2");
        onView(withId(R.id.info_button)).check(matches(isDisplayed()));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));
        onView(withId(R.id.period_title)).check(matches(withText(expectedOfflineTitle)));
    }

    @Test
    public void testTitleContent(){
        int symbolCount, expectedSize, actualSize, expectedColor, actualColor;


        expectedColor = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.white);
        onView(withId(R.id.period_title)).check(matches(CustomMatchers.withTextColor(expectedColor)));
        onView(withId(R.id.period_title)).check(matches(withText(expectedOfflineTitle)));
        symbolCount = expectedOfflineTitle.length();

        expectedSize = RReminder.adjustTitleSize(appContext, symbolCount, false);
        onView(withId(R.id.period_title)).check(matches(CustomMatchers.withTextSize(expectedSize)));

        onView(withId(R.id.timer_layout)).perform(click());
        //onView(isRoot()).perform(CustomActions.waitFor(5000));
        onView(withId(R.id.period_title)).check(matches(withText(expectedOnlineWorkTitle)));
        symbolCount = expectedOnlineWorkTitle.length();
        expectedSize = RReminder.adjustTitleSize(appContext, symbolCount, false);
        onView(withId(R.id.period_title)).check(matches(CustomMatchers.withTextSize(expectedSize)));

        expectedColor = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.black);
        onView(withId(R.id.period_title)).check(matches(CustomMatchers.withTextColor(expectedColor)));

        onView(isRoot()).perform(CustomActions.waitFor(20000));
        //intended(hasComponent(new ComponentName(getTargetContext(), NotificationActivity.class)));

        intended(hasComponent(NotificationActivity.class.getName()));
        onView(withId(R.id.notification_button)).perform(click());
        //intended(hasComponent(new ComponentName(getTargetContext(), MainActivity.class)));

        String expectedOnlineRestTitle = getResourceString((R.string.on_rest_period)).toUpperCase();
        onView(withId(R.id.period_title)).check(matches(withText(expectedOnlineRestTitle)));
        symbolCount = expectedOnlineRestTitle.length();
        expectedSize = RReminder.adjustTitleSize(appContext, symbolCount, false);
        onView(withId(R.id.period_title)).check(matches(CustomMatchers.withTextSize(expectedSize)));

    }

    @Test
    public void testNotificationActivityTurnOffButton(){

        int initialSetReminderOffCounter = mActivityRule.getActivity().setReminderOffCounter;
        onView(withId(R.id.timer_layout)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(20000));
        onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
        String expectednotificationRestTitle = getResourceString((R.string.notification_work_end_title));
        onView(withId(R.id.notification_title)).check(matches(withText(expectednotificationRestTitle)));
        int textSize = 38;
        int tabletTextSize = 76;

        int symbolCount = expectednotificationRestTitle.length();
        int expectedSize = RReminder.adjustTitleSize(appContext, symbolCount, false);

        if(RReminder.isTablet(appContext)){
            Assert.assertEquals("the adjustTitleSize on tablet should return size 76 for 12 symbol text string", tabletTextSize, expectedSize);
        } else {
            Assert.assertEquals("the adjustTitleSize should return size 38 for 12 symbol text string", textSize, expectedSize);
        }
        onView(withId(R.id.notification_title)).check(matches(CustomMatchers.withTextSize(expectedSize)));

        int expectedColor = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.black);
        onView(withId(R.id.notification_title)).check(matches(CustomMatchers.withTextColor(expectedColor)));

        onView(withId(R.id.notification_turn_off)).perform(click());
        Assert.assertTrue("after pressing turn off button the setReminderOff should be called and activity finished", mActivityRule.getActivity().isFinishing());
        //onView(withId(R.id.notification_turn_off)).check(doesNotExist());
        //onView(withId(R.id.timer_layout)).check(matches(isDisplayed()));
    }


    @Test
    public void testTimerButtonOfflineValue(){
        editor.putString(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_key), "00:45");
        editor.commit();

        String offlaneStringFromPreference =  RReminder.getPreferencePeriodLength(getApplicationContext(), 1) + ".00";
        String expectedString = "00:45.00";
        assertWithMessage("the string from preference").that(expectedString).isEqualTo(offlaneStringFromPreference);
        onView(withId(R.id.timer_hour1)).check(matches(withText("0")));
        onView(withId(R.id.timer_hour2)).check(matches(withText("0")));
        onView(withId(R.id.timer_minute1)).check(matches(withText("4")));
        onView(withId(R.id.timer_minute2)).check(matches(withText("5")));
        onView(withId(R.id.timer_second1)).check(matches(withText("0")));
        onView(withId(R.id.timer_second2)).check(matches(withText("0")));
    }

    @Test
    public void testExtendButton(){
        int expectedButtonTextSize;
        editor.putBoolean(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.commit();
        onView(withId(R.id.timer_layout)).perform(click());

        Bundle data =  mActivityRule.getActivity().getDataFromService();
        long initialPeriodEndTime = data.getLong(RReminder.PERIOD_END_TIME);
        int initialExtendCount = data.getInt(RReminder.EXTEND_COUNT);
        String expectedButtonText = getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_current_period);
        onView(withId(R.id.button_period_end_extend)).check(matches(withText(expectedButtonText)));
        expectedButtonTextSize = (RReminder.isTablet(appContext) ? 30:20);
        onView(withId(R.id.button_period_end_extend)).check(matches(CustomMatchers.withTextSize(expectedButtonTextSize)));
        int expectedColor = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.black);
        onView(withId(R.id.button_period_end_extend)).check(matches(CustomMatchers.withTextColor(expectedColor)));
        onView(withId(R.id.button_period_end_extend)).perform(click());


        DialogFragment extendDialog = (DialogFragment)mActivityRule.getActivity().getSupportFragmentManager().findFragmentByTag("extendDialog");

        //check if cliking hintbutton opens a hintdialog


        assertWithMessage("extenddialog is an instance of DialogFragment").that( extendDialog!=null).isTrue();
        assertWithMessage("extenddialog is visible").that( extendDialog.getShowsDialog()).isTrue();

        String optionOneButton, optionTwoButton, optionThreeButton;
        optionOneButton = String.format(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength);
        optionTwoButton =String.format(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength*2);
        optionThreeButton = String.format(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength*3);
        onView(withText(optionOneButton)).check(matches(isDisplayed()));
        switch(extendOptionCount){
            case 1:{
                onView(withText(optionTwoButton)).check(doesNotExist());
                onView(withText(optionThreeButton)).check(doesNotExist());
                break;
            }
            case 2:{
                onView(withText(optionTwoButton)).check(matches(isDisplayed()));
                onView(withText(optionThreeButton)).check(doesNotExist());
                break;
            }
            case 3:{
                onView(withText(optionTwoButton)).check(matches(isDisplayed()));
                onView(withText(optionThreeButton)).check(matches(isDisplayed()));
                break;
            }
            default: break;
        }

        onView(withText(optionOneButton)).perform(click());

        data = mActivityRule.getActivity().getDataFromService();
        long updatedPeriodEndTime = data.getLong(RReminder.PERIOD_END_TIME);
        int updatedExtendCount = data.getInt(RReminder.EXTEND_COUNT);
        long expectedUpdatedPeriodEndTime = initialPeriodEndTime + (long)extendBaseLength * 60000L;
        int expectedExtendCount = initialExtendCount + 1;
        long delta = Math.abs(expectedUpdatedPeriodEndTime - updatedPeriodEndTime);
        assertWithMessage("after extending period the service value period end time should be updated and be equal previous end time plus extend time").that(delta < 100).isTrue();
        assertWithMessage("after extending period the service value extend count should be updated").that(expectedExtendCount).isEqualTo(updatedExtendCount);

        onView(withId(R.id.button_period_end_extend)).perform(click());
        onView(withText(optionOneButton)).perform(click());

        onView(withId(R.id.button_period_end_extend)).perform(click());
        onView(withText(optionOneButton)).perform(click());

        onView(withId(R.id.button_period_end_extend)).perform(click());
        onView(withText(optionOneButton)).perform(click());

        int expectedBgColor = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.red);
        onView(withId(R.id.mainActivityLayout)).check(matches(CustomMatchers.withBackgroundColor(expectedBgColor)));
    }

    @Test
    public void testExtendDialogClosedAfterPeriodEnd(){
        editor.putBoolean(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.commit();
        onView(withId(R.id.timer_layout)).perform(click());
        onView(withId(R.id.button_period_end_extend)).perform(click());
        ExtendDialog extendDialog = (ExtendDialog)mActivityRule.getActivity().getSupportFragmentManager().findFragmentByTag("extendDialog");

        assertWithMessage("extenddialog is an instance of DialogFragment").that(extendDialog != null).isTrue();
        assertWithMessage("extenddialog is visible").that( extendDialog.dialogIsOpen).isTrue();
        onView(isRoot()).perform(CustomActions.waitFor(20000));
        intended(hasComponent(NotificationActivity.class.getName()));
        onView(withId(R.id.notification_button)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));
        assertWithMessage("extenddialog is dismissed").that(extendDialog.dialogIsOpen).isFalse();
    }

    @Test
    public void testExtendDialogOrientationChange(){


        SharedPreferences.Editor editor   = preferences.edit();
        editor.putBoolean(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.commit();
        onView(withId(R.id.timer_layout)).perform(click());
        onView(withId(R.id.button_period_end_extend)).perform(click());

        ExtendDialog extendDialog = (ExtendDialog)mActivityRule.getActivity().getSupportFragmentManager().findFragmentByTag("extendDialog");

        assertWithMessage("extenddialog is an instance of DialogFragment").that(extendDialog != null).isTrue();
        assertWithMessage("extenddialog is visible").that( extendDialog.dialogIsOpen).isTrue();

        //rotate screen
        if(RReminder.isPortrait(appContext)){
            mActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            mActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        assertWithMessage("after orientation change extenddialog is visible").that(extendDialog.getShowsDialog()).isTrue();
        onView(withText(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_close_dialog_text))).perform(click());

        onView(isRoot()).perform(CustomActions.waitFor(5000));
        Log.d("TEST", "getShowsDialog value: " + extendDialog.getShowsDialog());
        assertWithMessage("after pressing close button dialog should be gone").that(extendDialog.dialogIsOpen).isFalse();
    }

    @Test
    public void testTimerButtonLongPress(){
        onView(withId(R.id.timer_layout)).perform(click());
        int restoreFunctionCount = mActivityRule.getActivity().restoreAnimateCounter;
        assertWithMessage("the service is running").that(RReminderMobile.isCounterServiceRunning(appContext)).isTrue();
        Intent intent = new Intent (appContext, MobileOnAlarmReceiver.class);
        intent.putExtra(RReminder.PERIOD_TYPE, 1);
        intent.putExtra(RReminder.EXTEND_COUNT, 0);
        intent.setAction(RReminder.ACTION_ALARM_PERIOD_END);
        boolean alarmUp = (PendingIntent.getBroadcast(appContext, (int)mActivityRule.getActivity().periodEndTimeValue, intent, PendingIntent.FLAG_ONE_SHOT) != null);
        assertWithMessage("the alarm manager is running").that(alarmUp).isTrue();
        timerLongPress(700);
        int actualFunctionCount = mActivityRule.getActivity().restoreAnimateCounter;
        int diff = actualFunctionCount - restoreFunctionCount;
        assertWithMessage("the restore animation function counter should have been increased").that(diff).isEqualTo(1);

        timerLongPress(2000);


        assertWithMessage("the service is not running").that(RReminderMobile.isCounterServiceRunning(appContext)).isFalse();
        alarmUp = (PendingIntent.getBroadcast(appContext, (int)mActivityRule.getActivity().periodEndTimeValue, intent,PendingIntent.FLAG_ONE_SHOT) != null);
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

        assertWithMessage("PeriodService should not be running when the app is turned off").that(periodService).isFalse();
        assertWithMessage("PlaySoundService should not be running when the app is turned off").that(playSoundService).isFalse();
        assertWithMessage("the countdown timer is cancelled after long pressing button").that(mActivityRule.getActivity().countdown.isRunning).isFalse();
        onView(withId(R.id.period_title)).check(matches(withText(expectedOfflineTitle)));

    }

    @Test
    public void testSwipeListenerActive(){
        assertWithMessage("at the start of activity the boolean for swipe area should be false").that(mActivityRule.getActivity().swipeAreaListenerUsed).isFalse();
        onView(withId(R.id.timer_layout)).perform(click());
        onView(withId(R.id.swipe_area_text)).perform(click());
        assertWithMessage("the SwipeAreaListener activity was registered").that(mActivityRule.getActivity().swipeAreaListenerUsed).isTrue();
    }

    @Test
    public void testSwipeBackgroundChange(){

        onView(withId(R.id.timer_layout)).perform(click());
        onView(withId(R.id.swipe_area_text)).perform(swipeRight());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        DisplayMetrics displayMetrics = appContext.getResources().getDisplayMetrics();
        Instrumentation instr = getInstrumentation();

        float swipeLength = displayMetrics.widthPixels*0.6f;
        int[] location = new int[2];
        float x = mActivityRule.getActivity().swipeArea.getLeft();
        float y = mActivityRule.getActivity().swipeArea.getTop();
        mActivityRule.getActivity().swipeArea.getLocationOnScreen(location);
        Log.d("TEST", "x value: "+ location[0]);
        Log.d("TEST", "y value: "+ location[1]);
        int startY = location[1] - 40;
        int endY = location[1] - 40;
        Log.d("TEST", "startY value: "+startY);
        Log.d("TEST", "endY value: "+endY);
        Log.d("TEST", "endX value: "+(int)swipeLength);
        try{
            long downTime = SystemClock.uptimeMillis();
            // event time MUST be retrieved only by this way!
            long eventTime = SystemClock.uptimeMillis();
            MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 50, location[1]+40, 0);
            instr.sendPointerSync(event);
            // sending events - finger is moving over the screen
            eventTime = SystemClock.uptimeMillis();
            //setting up event with different coordinates depending on orientation
            event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, 70+(int)(swipeLength/2), location[1]+40, 0);
            instr.sendPointerSync(event);
            eventTime = SystemClock.uptimeMillis() + 1000;
            event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, 70+(int)swipeLength, location[1]+40, 0);
            instr.sendPointerSync(event);
            event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, 70+(int)swipeLength, location[1]+40, 0);
            instr.sendPointerSync(event);
        } catch (Exception ignored) {
            // Handle exceptions if necessary
        }
        onView(isRoot()).perform(CustomActions.waitFor(2000));
    }

    @Test
    public void testSwipeAreaText(){
        String work,rest, workLand, restLand, expectedText, actualText;
        work = getResourceString(R.string.swipe_area_work);
        rest = getResourceString(R.string.swipe_area_rest);
        workLand = getResourceString(R.string.swipe_area_text_land_work);
        restLand = getResourceString(R.string.swipe_area_text_land_rest);
        onView(withId(R.id.timer_layout)).perform(click());
        expectedText =(RReminder.isPortrait(getApplicationContext())? getApplicationContext().getResources().getString(R.string.swipe_area_text,rest) :  restLand);
        onView(withId(R.id.swipe_area_text)).check(matches(withText(expectedText)));
        if(RReminder.isPortrait(getApplicationContext())){

            onView(withId(R.id.swipe_area_text)).perform(swipeRight());
        } else {
            onView(withId(R.id.swipe_area_text)).perform(swipeDown());
        }
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        expectedText = (RReminder.isPortrait(getApplicationContext())? getApplicationContext().getResources().getString(R.string.swipe_area_text,work) :  workLand);
        onView(withId(R.id.swipe_area_text)).check(matches(withText(expectedText)));
        if(RReminder.isPortrait(getApplicationContext())){
            mActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            mActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        onView(isRoot()).perform(CustomActions.waitFor(2000));
        expectedText =(RReminder.isPortrait(getApplicationContext())? getApplicationContext().getResources().getString(R.string.swipe_area_text,work) : workLand);
        onView(withId(R.id.swipe_area_text)).check(matches(withText(expectedText)));
        if(RReminder.isPortrait(appContext)){
            onView(withId(R.id.swipe_area_text)).perform(swipeRight());
        } else {
            onView(withId(R.id.swipe_area_text)).perform(swipeDown());
        }
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        expectedText = (RReminder.isPortrait(getApplicationContext())? getApplicationContext().getResources().getString(R.string.swipe_area_text,rest) :  restLand);
        onView(withId(R.id.swipe_area_text)).check(matches(withText(expectedText)));
    }

    @Test
    public void testSwipeAreaBehaviour() {
        //disable hardware keyboard option on emulator
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        Instrumentation instr = getInstrumentation();
        float width = displayMetrics.widthPixels;
        float height = displayMetrics.heightPixels;
        int initialSwipeRestoreAnimCount = mActivityRule.getActivity().swipeRestoreAnimCounter;
        int swipeLength;


        Log.d(debug, "height variable value: "+ height);
        Log.d("TEST", "width variable value: "+ width);
        onView(withId(R.id.timer_layout)).perform(click());
        onView(withId(R.id.period_title)).check(matches(withText(expectedOnlineWorkTitle)));
        onView(withId(R.id.description_text)).check(matches(withText("")));
        int expectedInitialColorId = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.work);
        onView(withId(R.id.mainActivityLayout)).check(matches(CustomMatchers.withBackgroundColor(expectedInitialColorId)));
        String expectedDescription = mActivityRule.getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.release_text);




        long downTime = SystemClock.uptimeMillis();
        // event time MUST be retrieved only by this way!
        long eventTime = SystemClock.uptimeMillis();
        float xStart, yStart;
        int xStartInt, yStartInt;

        // Just init your variables, or create your own coords logic :)

        //different event coordinates depending on orientation
        if(RReminder.isPortrait(getApplicationContext())){
            swipeLength = (int)(width * 6 / 10);
            xStart = 50;
            xStartInt = (int)xStart;
            yStart = height - 50.0f;
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
            if(RReminder.isTablet(getApplicationContext())){
                x5 = xStart + swipeLength/2f+65;
            } else {
                x5 = xStart + swipeLength/2f+50;
            }
            int x5Int = (int)x5;
            int dX5 = (x5Int - xStartInt)/(swipeLength/100);
            Log.d("TEST", "dX5 value: "+ dX5);

            float x6 = xStart + swipeLength + 50;
            float x7;
            if(RReminder.isTablet(getApplicationContext())){
                x7 = xStart + swipeLength/2f+65;
            } else {
                x7 = xStart + swipeLength/2f+35;
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
                int expectedColorId = mActivityRule.getActivity().getCurrentColorId(1,dX1);
                Log.d("TEST", "the value for dX1: "+ dX1);
                onView(withId(R.id.mainActivityLayout)).check(matches(CustomMatchers.withBackgroundColor(expectedColorId)));
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x2, yStart, 0);
                instr.sendPointerSync(event);
                eventTime = SystemClock.uptimeMillis() + 1000;

                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x3, yStart, 0);

                instr.sendPointerSync(event);
                expectedColorId = mActivityRule.getActivity().getCurrentColorId(1,dX3);
                Log.d("TEST", "the value for dX3: "+ dX3);
                onView(withId(R.id.mainActivityLayout)).check(matches(CustomMatchers.withBackgroundColor(expectedColorId)));
                eventTime = SystemClock.uptimeMillis() + 1000;

                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x4, yStart, 0);
                instr.sendPointerSync(event);
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x5, yStart, 0);
                instr.sendPointerSync(event);
                expectedColorId = mActivityRule.getActivity().getCurrentColorId(2,dX5);
                onView(withId(R.id.period_title)).check(matches(withText(expectedOnlineRestTitle)));
                onView(withId(R.id.mainActivityLayout)).check(matches(CustomMatchers.withBackgroundColor(expectedColorId)));

                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x6, yStart, 0);
                instr.sendPointerSync(event);
                onView(withId(R.id.description_text)).check(matches(withText(expectedDescription)));
                eventTime = SystemClock.uptimeMillis() + 1500;


                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x7, yStart, 0);
                instr.sendPointerSync(event);
                expectedColorId = mActivityRule.getActivity().getCurrentColorId(2,dX7);
                Log.d(debug, "delta x from test: "+ (x7-xStart));
                Log.d("TEST", "the value for dX7 (reverse direction): "+ dX7);
                Log.d(debug, "expected color id dX7 reverse: "+expectedColorId);
                onView(withId(R.id.mainActivityLayout)).check(matches(CustomMatchers.withBackgroundColor(expectedColorId)));

                eventTime = SystemClock.uptimeMillis() + 1500;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x4, yStart, 0);
                instr.sendPointerSync(event);
                onView(withId(R.id.description_text)).check(matches(withText("")));
                onView(withId(R.id.period_title)).check(matches(withText(expectedOnlineWorkTitle)));

                //repeating reaching the required swipe lenght to test if description is working as expected
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x5, yStart, 0);
                instr.sendPointerSync(event);
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x6, yStart, 0);
                instr.sendPointerSync(event);
                onView(withId(R.id.description_text)).check(matches(withText(expectedDescription)));
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x4, yStart, 0);
                instr.sendPointerSync(event);
                onView(withId(R.id.description_text)).check(matches(withText("")));

                // release finger, gesture is finished
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x4, yStart, 0);
                instr.sendPointerSync(event);
            } catch (Exception ignored) {
                // Handle exceptions if necessary
            }


        } else {
            //coordinates,values and motion events for landscape orientation
            swipeLength = (int)height * 6 / 10;
            xStart = width - 50.0f;
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

            float y5 = yStart + swipeLength/2f+68;
            int y5Int = (int)y5;
            int dY5 = (y5Int - yStartInt)/(swipeLength/100);
            Log.d("TEST", "dY5 value: "+ dY5);

            float y6 =yStart + swipeLength + 50;
            float y7;
            if(RReminder.isTablet(getApplicationContext())){
                y7 = yStart + swipeLength/2f+65;
            } else {
                y7 = xStart + swipeLength/2f+68;
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
                int expectedColorId = mActivityRule.getActivity().getCurrentColorId(1,dY1);
                onView(withId(R.id.mainActivityLayout)).check(matches(CustomMatchers.withBackgroundColor(expectedColorId)));
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y2, 0);
                instr.sendPointerSync(event);
                eventTime = SystemClock.uptimeMillis() + 1000;

                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y3, 0);

                instr.sendPointerSync(event);
                expectedColorId = mActivityRule.getActivity().getCurrentColorId(1,dY3);
                Log.d("TEST", "the value for dY3: "+ dY3);
                onView(withId(R.id.mainActivityLayout)).check(matches(CustomMatchers.withBackgroundColor(expectedColorId)));
                eventTime = SystemClock.uptimeMillis() + 1000;

                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y4, 0);
                instr.sendPointerSync(event);
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y5, 0);
                instr.sendPointerSync(event);
                expectedColorId = mActivityRule.getActivity().getCurrentColorId(2,dY5);
                Log.d(debug, "color id dY5: "+expectedColorId);
                onView(withId(R.id.period_title)).check(matches(withText(expectedOnlineRestTitle)));
                onView(withId(R.id.mainActivityLayout)).check(matches(CustomMatchers.withBackgroundColor(expectedColorId)));

                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y6, 0);
                instr.sendPointerSync(event);
                onView(withId(R.id.description_text)).check(matches(withText(expectedDescription)));
                eventTime = SystemClock.uptimeMillis() + 1500;


                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y7, 0);
                instr.sendPointerSync(event);
                expectedColorId = mActivityRule.getActivity().getCurrentColorId(2,dY7);
                Log.d("TEST", "the value for dX7 (reverse direction): "+ dY7);
                Log.d(debug, "expected color id dX7 reverse: "+expectedColorId);
                onView(withId(R.id.mainActivityLayout)).check(matches(CustomMatchers.withBackgroundColor(expectedColorId)));
                eventTime = SystemClock.uptimeMillis() + 1500;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y4, 0);
                instr.sendPointerSync(event);
                onView(withId(R.id.description_text)).check(matches(withText("")));
                onView(withId(R.id.period_title)).check(matches(withText(expectedOnlineWorkTitle)));

                //repeating reaching the required swipe lenght to test if description is working as expected
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y5, 0);
                instr.sendPointerSync(event);
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y6, 0);
                instr.sendPointerSync(event);
                onView(withId(R.id.description_text)).check(matches(withText(expectedDescription)));
                eventTime = SystemClock.uptimeMillis() + 1000;
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, xStart, y4, 0);
                instr.sendPointerSync(event);
                onView(withId(R.id.description_text)).check(matches(withText("")));

                // release finger, gesture is finished
                event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, xStart, y4, 0);
                instr.sendPointerSync(event);
            } catch (Exception ignored) {
                // Handle exceptions if necessary
            }
        }




        int updatedSwipeRestoreAnimCounter = mActivityRule.getActivity().swipeRestoreAnimCounter;
        assertWithMessage("the swipe animate restore function had been called").that(updatedSwipeRestoreAnimCounter == initialSwipeRestoreAnimCount + 1).isTrue();
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        onView(withId(R.id.mainActivityLayout)).check(matches(CustomMatchers.withBackgroundColor(expectedInitialColorId)));

    }

    @Test
    public void testClicksOnActiveMode(){
        onView(withId(R.id.timer_layout)).perform(click());
        onView(withId(R.id.period_title)).check(matches(withText(expectedOnlineWorkTitle)));
        boolean countdownIsRunning = mActivityRule.getActivity().countdown.isRunning;
        boolean swipeIsVisible = mActivityRule.getActivity().swipeArea.isShown();
        //method of getting the background color - ony available in API 11+
        int expectedInitialColorId = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.work);
        onView(withId(R.id.mainActivityLayout)).check(matches(CustomMatchers.withBackgroundColor(expectedInitialColorId)));
        onView(withId(R.id.timer_layout)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(5000));
        onView(withId(R.id.timer_layout)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(2000));

        onView(withId(R.id.period_title)).check(matches(withText(expectedOnlineWorkTitle)));
        assertWithMessage("the countdown should be active").that(mActivityRule.getActivity().countdown.isRunning).isEqualTo(countdownIsRunning);
        assertWithMessage("the swipe area still should be visible").that(mActivityRule.getActivity().swipeArea.isShown()).isEqualTo(swipeIsVisible);
        onView(withId(R.id.mainActivityLayout)).check(matches(CustomMatchers.withBackgroundColor(expectedInitialColorId)));
    }

    @Test
    public void testInactiveDigitColors(){
        editor.putString(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), "02:00");
        editor.commit();
        int expectedInactiveColor, expectedActiveColor;
        expectedInactiveColor = getApplicationContext().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.inactive_digit);
        expectedActiveColor = getApplicationContext().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.black);

        onView(withId(R.id.timer_hour1)).check(matches(CustomMatchers.withTextColor(expectedInactiveColor)));
        onView(withId(R.id.timer_hour2)).check(matches(CustomMatchers.withTextColor(expectedInactiveColor)));
        onView(withId(R.id.timer_colon)).check(matches(CustomMatchers.withTextColor(expectedActiveColor)));
        onView(withId(R.id.timer_minute1)).check(matches(CustomMatchers.withTextColor(expectedActiveColor)));
        onView(withId(R.id.timer_minute2)).check(matches(CustomMatchers.withTextColor(expectedActiveColor)));
        onView(withId(R.id.timer_point)).check(matches(CustomMatchers.withTextColor(expectedActiveColor)));
        onView(withId(R.id.timer_second1)).check(matches(CustomMatchers.withTextColor(expectedActiveColor)));
        onView(withId(R.id.timer_second2)).check(matches(CustomMatchers.withTextColor(expectedActiveColor)));

        onView(withId(R.id.timer_layout)).perform(click());

        onView(withId(R.id.timer_hour1)).check(matches(CustomMatchers.withTextColor(expectedInactiveColor)));
        onView(withId(R.id.timer_hour2)).check(matches(CustomMatchers.withTextColor(expectedInactiveColor)));
        onView(withId(R.id.timer_colon)).check(matches(CustomMatchers.withTextColor(expectedInactiveColor)));
        onView(withId(R.id.timer_minute1)).check(matches(CustomMatchers.withTextColor(expectedInactiveColor)));
        onView(withId(R.id.timer_minute2)).check(matches(CustomMatchers.withTextColor(expectedInactiveColor)));
        onView(withId(R.id.timer_point)).check(matches(CustomMatchers.withTextColor(expectedActiveColor)));
        onView(withId(R.id.timer_second1)).check(matches(CustomMatchers.withTextColor(expectedActiveColor)));
        onView(withId(R.id.timer_second2)).check(matches(CustomMatchers.withTextColor(expectedActiveColor)));

        onView(isRoot()).perform(CustomActions.waitFor(21000));

        onView(withId(R.id.notification_button)).perform(click());
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));


        onView(withId(R.id.timer_hour1)).check(matches(CustomMatchers.withTextColor(expectedActiveColor)));
        onView(withId(R.id.timer_hour2)).check(matches(CustomMatchers.withTextColor(expectedActiveColor)));
        onView(withId(R.id.timer_colon)).check(matches(CustomMatchers.withTextColor(expectedActiveColor)));
        onView(withId(R.id.timer_minute1)).check(matches(CustomMatchers.withTextColor(expectedActiveColor)));
        onView(withId(R.id.timer_minute2)).check(matches(CustomMatchers.withTextColor(expectedActiveColor)));
        onView(withId(R.id.timer_point)).check(matches(CustomMatchers.withTextColor(expectedActiveColor)));
        onView(withId(R.id.timer_second1)).check(matches(CustomMatchers.withTextColor(expectedActiveColor)));
        onView(withId(R.id.timer_second2)).check(matches(CustomMatchers.withTextColor(expectedActiveColor)));
    }

    @Test
    public void testTimerButtonStartProcess(){
        onView(withId(R.id.button_period_end_extend)).check(matches(not(isDisplayed())));
        onView(withId(R.id.swipe_area_text)).check(matches(not(isDisplayed())));
        onView(withId(R.id.period_title)).check(matches(withText(expectedOfflineTitle)));
        assertWithMessage("at the launch of app the service shoulnt be running").that(RReminderMobile.isCounterServiceRunning(getApplicationContext())).isFalse();
        assertWithMessage("the countdown timer should not run at the launch of app").that(mActivityRule.getActivity().countdown==null).isTrue();


        Intent intent = new Intent (getApplicationContext(), MobileOnAlarmReceiver.class);
        intent.putExtra(RReminder.PERIOD_TYPE, 1);
        intent.putExtra(RReminder.EXTEND_COUNT, 0);
        intent.setAction(RReminder.ACTION_ALARM_PERIOD_END);
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        boolean alarmUp = (PendingIntent.getBroadcast(getApplicationContext(), 0, intent,PendingIntent.FLAG_NO_CREATE) != null);
        Log.d(debug, "is alarm up in offline mode: "+alarmUp);
        if(alarmUp){
            Log.d(debug, "PendingIntent at the start: "+PendingIntent.getBroadcast(appContext, 0, intent,PendingIntent.FLAG_NO_CREATE).toString());
        }
        assertWithMessage("the alarm manager shouldn't be running when app is lauched").that(alarmUp).isFalse();

        onView(withId(R.id.timer_layout)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(5000));
        alarmUp = (PendingIntent.getBroadcast(getApplicationContext(), (int)mActivityRule.getActivity().periodEndTimeValue, intent,PendingIntent.FLAG_ONE_SHOT) != null);
        assertWithMessage("the alarm is set").that(alarmUp).isTrue();
        if (alarmUp) {
            Log.d(debug, "Alarm is already active");
            Log.d(debug, "PendingIntent after lauching reminder: "+PendingIntent.getBroadcast(getApplicationContext(), (int)mActivityRule.getActivity().periodEndTimeValue, intent,PendingIntent.FLAG_ONE_SHOT).toString());
        }
        assertWithMessage("after pressing timer button the service should be started").that(RReminderMobile.isCounterServiceRunning(getApplicationContext())).isTrue();
        assertWithMessage("the countdown timer should  run after pressing button").that(mActivityRule.getActivity().countdown.isRunning).isTrue();
        onView(withId(R.id.period_title)).check(matches(withText(expectedOnlineWorkTitle)));
        onView(withId(R.id.swipe_area_text)).check(matches(isDisplayed()));

        onView(isRoot()).perform(CustomActions.waitFor(5000));
        Bundle data =  mActivityRule.getActivity().getDataFromService();
        long periodEndTime = data.getLong(RReminder.PERIOD_END_TIME);
        long remainingTime = periodEndTime - Calendar.getInstance().getTimeInMillis();
        int timeInSeconds = Math.round(remainingTime / 1000);
        int seconds = timeInSeconds % 60;
        int timeInMinutes  = timeInSeconds / 60;
        int  minutes = timeInMinutes % 60;
        int hours = timeInMinutes / 60;
        char[] serviceHours = new char[2];
        char[] serviceMinutes = new char[2];
        char[]  serviceSeconds = new char[2];
        String secondString, minuteString, hourString, serviceHour, serviceMinute, serviceSecond;
        if (seconds <10)
        {
            serviceSeconds[0] = '0';
            serviceSeconds[1] = (char)(seconds+'0');
        } else {
            serviceSeconds[0] = (char)(seconds/10+'0');
            serviceSeconds[1] = (char)(seconds%10+'0');
        }

        if(minutes<10){
            serviceMinutes[0] = '0';
            serviceMinutes[1] = (char)(minutes+'0');
        } else {
            serviceMinutes[0] = (char)(minutes/10+'0');
            serviceMinutes[1] = (char)(minutes%10+'0');
        }

        if(hours<10){
            serviceHours[0] = '0';
            serviceHours[1] = (char)(hours+'0');
        } else {
            serviceHours[0] = (char)(hours/10+ '0');
            serviceHours[1] = (char)(hours%10+'0');
        }
        Log.d(debug, "servicehours[1] value is: "+serviceSeconds[1]);
        Log.d(debug, "serviceHours%10 value is: "+seconds%10);

        onView(withId(R.id.timer_hour1)).check(matches(withText(""+serviceHours[0])));
        onView(withId(R.id.timer_hour2)).check(matches(withText(""+serviceHours[1])));

        onView(withId(R.id.timer_minute1)).check(matches(withText(""+serviceMinutes[0])));
        onView(withId(R.id.timer_minute2)).check(matches(withText(""+serviceMinutes[1])));

        onView(withId(R.id.timer_second1)).check(matches(withText(""+serviceSeconds[0])));
        onView(withId(R.id.timer_second2)).check(matches(withText(""+serviceSeconds[1])));
    }

    @Test
    public void testDescription(){
        int expectedColor, actualColor, expectedSize, actualSize;
        editor.putBoolean(getApplicationContext().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.commit();
        String expectedDescription;
        expectedDescription = "";
        onView(withId(R.id.description_text)).check(matches(withText(expectedDescription)));
        onView(withId(R.id.timer_layout)).perform(click());
        onView(withId(R.id.description_text)).check(matches(withText(expectedDescription)));
        //extending 1st time for 5 mins
        onView(withId(R.id.button_period_end_extend)).perform(click());
        onView(withId(R.id.extend_dialog_button_extend)).perform(click());
        expectedDescription = getApplicationContext().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.description_extended_one_time);
        onView(withId(R.id.description_text)).check(matches(withText(expectedDescription)));
        expectedColor = getApplicationContext().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.black);
        onView(withId(R.id.description_text)).check(matches(CustomMatchers.withTextColor(expectedColor)));
        if(RReminder.isTablet(getApplicationContext())){
            expectedSize = 30;
        } else {
            expectedSize = (RReminder.isPortrait(getApplicationContext()) ? 20:18);
        }
        onView(withId(R.id.description_text)).check(matches(CustomMatchers.withTextSize(expectedSize)));
        //extending 2nd time for 5 mins
        onView(withId(R.id.button_period_end_extend)).perform(click());
        onView(withId(R.id.extend_dialog_button_extend)).perform(click());
        expectedDescription = String.format(getApplicationContext().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.description_extended),2);
        onView(withId(R.id.description_text)).check(matches(withText(expectedDescription)));
        //extending 3rd time for 5 mins
        onView(withId(R.id.button_period_end_extend)).perform(click());
        onView(withId(R.id.extend_dialog_button_extend)).perform(click());
        expectedDescription = String.format(getApplicationContext().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.description_extended),3);
        onView(withId(R.id.description_text)).check(matches(withText(expectedDescription)));
    }

    @Test
    public void testDescriptionEnoughSpace(){

        editor.putBoolean(getApplicationContext().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.commit();

        onView(withId(R.id.timer_layout)).perform(click());

        onView(withId(R.id.button_period_end_extend)).perform(click());
        onView(withId(R.id.extend_dialog_button_extend)).perform(click());

        onView(withId(R.id.button_period_end_extend)).perform(click());
        onView(withId(R.id.extend_dialog_button_extend)).perform(click());

        onView(isRoot()).perform(CustomActions.waitFor(7000));

        onView(withId(R.id.description_text)).check(matches(CustomMatchers.withEnoughSpace()));

    }

    @Test
    public void testServiceUpdatedAfterExtending(){
        onView(withId(R.id.timer_layout)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        Bundle serviceData = mActivityRule.getActivity().getDataFromService();
        int initPeriodType = serviceData.getInt(RReminder.PERIOD_TYPE);
        int initExtendCount = serviceData.getInt(RReminder.EXTEND_COUNT);
        long initPeriodEndTimeValue = serviceData.getLong(RReminder.PERIOD_END_TIME);
        onView(isRoot()).perform(CustomActions.waitFor(20000));
        intended(hasComponent(new ComponentName(getTargetContext(), NotificationActivity.class)));
        onView(withId(R.id.button_notification_period_end_extend)).perform(click());
        onView(withId(R.id.extend_dialog_button_extend)).perform(click());

        long extendedTimeStamp = Calendar.getInstance().getTimeInMillis();
        long extendedPeriodEndTime = extendedTimeStamp + 60000*preferences.getInt(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key),5);

        onView(withId(R.id.timer_layout)).check(matches(isDisplayed()));

        onView(isRoot()).perform(CustomActions.waitFor(4000));
        int extendedPeriodType = mActivityRule.getActivity().periodType;
        int extendedExtendCount = mActivityRule.getActivity().extendCount;
        long extendedPeriodEndTimeValue = mActivityRule.getActivity().periodEndTimeValue;
        int expectedPeriodType = 3;

        assertWithMessage("after extending the period, the updated value should remain the same").that(expectedPeriodType).isEqualTo(extendedPeriodType);
        assertWithMessage("atfer extending period, the extend count should be increased by 1").that(initExtendCount + 1).isEqualTo(extendedExtendCount);
        Log.d(debug, "the error of extended period end value: "+ Math.abs(extendedPeriodEndTimeValue - extendedPeriodEndTime));
        assertWithMessage("after extending period the period end time is properly updated").that(Math.abs(extendedPeriodEndTimeValue - extendedPeriodEndTime) < 1000).isTrue();
    }

    @Test
    public void testExtendDialogIsClosedInNotificationAfterPeriodEnds(){
        onView(withId(R.id.timer_layout)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(20000));
        intended(hasComponent(new ComponentName(getTargetContext(), NotificationActivity.class)));
        String extendDialogTitle = getResourceString(R.string.extend_dialog_title);
        onView(withId(R.id.button_notification_period_end_extend)).perform(click());
        onView(withId(R.id.extend_dialog_button_extend)).check(matches(isDisplayed()));
        onView(isRoot()).perform(CustomActions.waitFor(13000));
        // veikt parbaudi, meklejot dialoga title
        onView(withId(R.id.extend_dialog_button_extend)).check((doesNotExist()));
        onView(withId(R.id.button_notification_period_end_extend)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.extend_dialog_button_extend)).check(matches(isDisplayed()));
        onView(withText("Cancel")).perform(click());
        onView(withId(R.id.notification_button)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(2000));
    }

    @Test
    public void testNotificationCloseAppInFocus(){

        onView(withId(R.id.timer_layout)).perform(click());

        onView(isRoot()).perform(CustomActions.waitFor(20000));
        intended(hasComponent(new ComponentName(getTargetContext(), NotificationActivity.class)));
        onView(withId(R.id.notification_button)).perform(click());
        onView(withId(R.id.timer_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.period_title)).check(matches(withText(expectedOnlineRestTitle)));

        int expectedBgColor = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.rest);
        onView(withId(R.id.mainActivityLayout)).check(matches(CustomMatchers.withBackgroundColor(expectedBgColor)));
    }

    @Test
    public void testNotificationCloseAppInFocusManual(){
        editor.putString(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_key), "1");
        editor.putString(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), "00:02");
        editor.commit();
        onView(withId(R.id.timer_layout)).perform(click());
        assertWithMessage("after pressing timer button the service should be started").that(RReminderMobile.isCounterServiceRunning(getApplicationContext())).isTrue();
        onView(isRoot()).perform(CustomActions.waitFor(20000));
        onView(withId(R.id.notification_button)).perform(click());
        onView(withId(R.id.period_title)).check(matches(withText(expectedOnlineRestTitle)));

        int expectedBgColor = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.rest);
        onView(withId(R.id.mainActivityLayout)).check(matches(CustomMatchers.withBackgroundColor(expectedBgColor)));
    }

    /*
    @Test
    public void testOnGoingNotificationIntent(){
        onView(withId(R.id.timer_layout)).perform(click());

        //solo.clickOnMenuItem("Settings",true);
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText("Settings")).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        intended(hasComponent(new ComponentName(getTargetContext(), PreferenceActivity.class)));
        onView(isRoot()).perform(CustomActions.waitFor(1000));

        Intent notificationIntent = new Intent(appContext, MainActivity.class);
        notificationIntent.setAction(RReminder.ACTION_VIEW_MAIN_ACTIVITY);
        notificationIntent.putExtra(RReminder.START_COUNTER, false);

        mActivityRule.launchActivity(notificationIntent);
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));

    }

     */

    @Test
    public void testDialogCloseTimerResume(){
        assertWithMessage("at the start the timer shouldnt be active").that(mActivityRule.getActivity().countdown == null).isTrue();
        onView(withId(R.id.timer_layout)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        assertWithMessage("after launching reminder the countdown timer should be active").that(mActivityRule.getActivity().countdown != null && mActivityRule.getActivity().countdown.isRunning).isTrue();
        onView(withId(R.id.button_period_end_extend)).perform(click());
        assertWithMessage("after launching reminder the countdown timer should be active").that(mActivityRule.getActivity().countdown.isRunning).isFalse();
        onView(withText("Cancel")).perform(click());
        assertWithMessage("after closing extend dialog, the countdown timer should be active").that(mActivityRule.getActivity().countdown.isRunning).isTrue();
    }


    @Test
    public void testLaunchReminder(){
        onView(withId(R.id.timer_layout)).perform(click());
        onView(withId(R.id.period_title)).check(matches(withText(expectedOnlineWorkTitle)));
    }

    @Test
    public void testNotificationCloseButton(){
        onView(withId(R.id.timer_layout)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(20000));
        onView(withId(R.id.notification_button)).perform(click());
        onView(withId(R.id.notification_title)).check(doesNotExist());
        onView(withId(R.id.notification_button)).check(doesNotExist());
    }

    private void timerLongPress(int duration){
        onView(withId(R.id.timer_layout)).perform(TouchActions.pressAndHold());
        onView(isRoot()).perform(CustomActions.waitFor(duration));
        onView(withId(R.id.timer_layout)).perform(TouchActions.release());
        TouchActions.tearDown();
    }




    private String getResourceString(int id) {
        Context targetContext = getInstrumentation().getTargetContext();
        return targetContext.getResources().getString(id);
    }





}
