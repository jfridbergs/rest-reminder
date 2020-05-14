package com.colormindapps.rest_reminder_alarm;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import androidx.fragment.app.DialogFragment;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.CursorMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.filters.LargeTest;
import androidx.test.internal.util.Checks;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.truth.Truth.assertWithMessage;

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
        editor.putString(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_key), "15:55");
        editor.putString(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), "15:55");
        editor.putInt(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key), 3);
        editor.putInt(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key), 5);
        editor.putBoolean(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.commit();
        scaledDensity = getApplicationContext().getResources().getDisplayMetrics().scaledDensity;

        extendOptionCount = preferences.getInt(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key), 1);
        extendBaseLength = preferences.getInt(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key),5);
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

        onView(isRoot()).perform(CustomActions.waitFor(16000));
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
        onView(isRoot()).perform(CustomActions.waitFor(16000));
        onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
        String expectednotificationRestTitle = getResourceString((R.string.notification_work_end_title));
        onView(withId(R.id.notification_title)).check(matches(withText(expectednotificationRestTitle)));
        int textSize = 50;
        int tabletTextSize = 100;

        int symbolCount = expectednotificationRestTitle.length();
        int expectedSize = RReminder.adjustTitleSize(appContext, symbolCount, false);

        if(RReminder.isTablet(appContext)){
            Assert.assertEquals("the adjustTitleSize on tablet should return size 100 for 12 symbol text string", tabletTextSize, expectedSize);
        } else {
            Assert.assertEquals("the adjustTitleSize should return size 50 for 12 symbol text string", textSize, expectedSize);
        }
        onView(withId(R.id.notification_title)).check(matches(CustomMatchers.withTextSize(expectedSize)));

        int expectedColor = appContext.getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.black);
        onView(withId(R.id.notification_title)).check(matches(CustomMatchers.withTextColor(expectedColor)));

        onView(withId(R.id.notification_turn_off)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        int updatedSetReminderOffCounter = mActivityRule.getActivity().setReminderOffCounter;
        Assert.assertTrue("after pressing turn off button the setReminderOff should be called", (updatedSetReminderOffCounter == initialSetReminderOffCounter));
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
        expectedButtonTextSize = (RReminder.isTablet(appContext) ? 28:20);
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
        onView(isRoot()).perform(CustomActions.waitFor(15000));
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
        onView(withId(R.id.timer_layout)).perform(TouchActions.pressAndHold());
        onView(isRoot()).perform(CustomActions.waitFor(700));
        onView(withId(R.id.timer_layout)).perform(TouchActions.release());
        TouchActions.tearDown();
        int actualFunctionCount = mActivityRule.getActivity().restoreAnimateCounter;
        int diff = actualFunctionCount - restoreFunctionCount;
        assertWithMessage("the restore animation function counter should have been increased").that(diff).isEqualTo(1);

        onView(withId(R.id.timer_layout)).perform(TouchActions.pressAndHold());
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        onView(withId(R.id.timer_layout)).perform(TouchActions.release());


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
    public void testLaunchReminder(){
        onView(withId(R.id.timer_layout)).perform(click());
        onView(withId(R.id.period_title)).check(matches(withText(expectedOnlineWorkTitle)));
    }

    private String getResourceString(int id) {
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        return targetContext.getResources().getString(id);
    }





}
