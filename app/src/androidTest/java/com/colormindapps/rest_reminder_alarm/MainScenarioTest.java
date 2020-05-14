package com.colormindapps.rest_reminder_alarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.util.Log;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

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
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.hamcrest.core.IsNot.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainScenarioTest {
    SharedPreferences privatePreferences, preferences;
    SharedPreferences.Editor privateEditor, editor;
    float scaledDensity;
    int extendOptionCount, extendBaseLength, versionCode;
    ActivityScenario<MainActivity> scenario;

    String debug = "MAIN_SCENARIO_TEST";

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule
            = new ActivityScenarioRule<>(MainActivity.class);
/*
    @Before
    public void setUp(){
        Log.d(debug, "setUp");
        scenario = ActivityScenario.launch(MainActivity.class);
        appContext = getApplicationContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        privatePreferences = appContext.getSharedPreferences(RReminder.PRIVATE_PREF, Context.MODE_PRIVATE);
        editor   = preferences.edit();
        privateEditor = privatePreferences.edit();
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_key), "15:55");
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), "15:55");
        editor.putInt(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key), 3);
        editor.putInt(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key), 5);
        editor.putBoolean(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.commit();
        scaledDensity = getApplicationContext().getResources().getDisplayMetrics().scaledDensity;

        extendOptionCount = preferences.getInt(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key), 1);
        extendBaseLength = preferences.getInt(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key),5);
        versionCode = preferences.getInt(RReminder.VERSION_KEY, 0);
    }

    @After
    public void tearDown(){
        Log.d(debug, "tearDown");

        scenario.onActivity(
                activity -> {
                    RReminderMobile.cancelCounterAlarm(appContext, activity.periodType, activity.extendCount, activity.periodEndTimeValue);
                    RReminderMobile.stopCounterService(appContext, activity.periodType);
                    });






        editor.putBoolean(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_key), "0");
        editor.putBoolean(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_end_period_key), true);
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), RReminder.DEFAULT_REST_PERIOD_STRING);
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_key), "00:45");
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), "00:15");
        editor.putInt(RReminder.VERSION_KEY, versionCode);
        editor.commit();
        appContext = null;
    }
*/
    @Test
    public void testIntroductionDialog(){
        privatePreferences = getApplicationContext().getSharedPreferences(RReminder.PRIVATE_PREF, Context.MODE_PRIVATE);
        privateEditor = privatePreferences.edit();
        privateEditor.putInt(RReminder.VERSION_KEY, 0);
        privateEditor.putBoolean(RReminder.EULA_ACCEPTED, false);
        privateEditor.commit();

        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {
            assertThat(scenario.getState()).isEqualTo(Lifecycle.State.RESUMED);
            scenario.onActivity(
                    activity -> {
                        DialogFragment introDialog = (DialogFragment)activity.getSupportFragmentManager().findFragmentByTag("introductionDialog");
                        assertThat(introDialog!=null).isTrue();
                        assertThat(introDialog.getShowsDialog()).isTrue();
                        if(RReminder.isPortrait(getApplicationContext())){
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        } else {
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        }
                    });
            scenario.recreate();
            scenario.onActivity(
                    activity -> {
                        DialogFragment introDialog = (DialogFragment)activity.getSupportFragmentManager().findFragmentByTag("introductionDialog");
                        assertThat(introDialog!=null).isTrue();
                        assertThat(introDialog.getShowsDialog()).isTrue();

                        //assertThat(introDialog.getShowsDialog()).isFalse();

                    });

             onView(withText(RReminderTest.getResourceString(R.string.eula_accept))).perform(click());
            onView(withId(R.id.eula_text)).check(doesNotExist());
        }
    }

    @Test
    public void testIntroductionDialogClose(){
        privatePreferences = getApplicationContext().getSharedPreferences(RReminder.PRIVATE_PREF, Context.MODE_PRIVATE);
        privateEditor = privatePreferences.edit();
        privateEditor.putInt(RReminder.VERSION_KEY, 0);
        privateEditor.putBoolean(RReminder.EULA_ACCEPTED, false);
        privateEditor.commit();


        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {
            assertThat(scenario.getState()).isEqualTo(Lifecycle.State.RESUMED);
            scenario.onActivity(
                    activity -> {
                        DialogFragment introDialog = (DialogFragment)activity.getSupportFragmentManager().findFragmentByTag("introductionDialog");
                        assertThat(introDialog!=null).isTrue();
                        assertThat(introDialog.getShowsDialog()).isTrue();
                    });
            onView(withText(RReminderTest.getResourceString(R.string.eula_accept))).perform(click());
            onView(withId(R.id.eula_text)).check(doesNotExist());
            scenario.onActivity(
                    activity -> {
                        assertThat(RReminderMobile.isCounterServiceRunning(getApplicationContext())).isFalse();
                    });

            String expectedOfflineTitle = RReminderTest.getResourceString(R.string.reminder_off_title);
            onView(withId(R.id.period_title)).check(matches(withText(expectedOfflineTitle)));
            int expectedColor = getApplicationContext().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.black);
            onView(withId(R.id.mainActivityLayout)).check(matches(CustomMatchers.withBackgroundColor(expectedColor)));
        }
    }

    @Test
    public void testDisableCountdownOnPause(){
        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {
            scenario.recreate();
            assertThat(scenario.getState()).isEqualTo(Lifecycle.State.RESUMED);
            onView(isRoot()).perform(CustomActions.waitFor(2000));
            Log.d("MAIN_SCENARION", "activity state: "+scenario.getState());
            Log.d("MAIN_SCENARION", "move to resumed");
            scenario.onActivity(
                    activity -> {
                        assertThat(activity.countdown==null).isTrue();
                        activity.startReminder();
                    });
            onView(isRoot()).perform(CustomActions.waitFor(5000));
            scenario.onActivity(
                    activity -> {
                        assertThat(activity.countdown.isRunning).isTrue();
                    });
            scenario.moveToState(Lifecycle.State.STARTED);
            Log.d("MAIN_SCENARION", "activity state: "+scenario.getState());
            scenario.onActivity(
                    activity -> {
                        assertThat(activity.countdown.isRunning).isFalse();
                    });


        }
    }

    @Test
    public void testAdjustTitleSize(){
        int count, expectedSize, actualSize,expectedTabletSize;
        Context appContext = getApplicationContext();
        //BEFORE TEST: ADD EXCEPTION TO adjustTitleSize so that it throws exception, when a negative value is passed as argument
        //normal screen
        count = 10;
        expectedSize = 50;
        expectedTabletSize = 100;
        actualSize = RReminder.adjustTitleSize(appContext, count, false);
        if(RReminder.isTablet(appContext)){
            assertWithMessage("normal screen (tablet) with 10 symbol string should have font size 100"). that(expectedTabletSize).isEqualTo(actualSize);
        } else {
            assertWithMessage("normal screen with 10 symbol string should have font size 50"). that(expectedSize).isEqualTo(actualSize);
        }

        count = 20;
        expectedSize = 35;
        expectedTabletSize = 70;
        actualSize = RReminder.adjustTitleSize(appContext, count, false);
        if(RReminder.isTablet(appContext)){
            assertWithMessage("normal screen (tablet) with 20 symbol string should have font size 70"). that(expectedTabletSize).isEqualTo(actualSize);
        } else {
            assertWithMessage("normal screen with 20 symbol string should have font size 35"). that(expectedSize).isEqualTo(actualSize);
        }

        count = 30;
        expectedSize = 28;
        expectedTabletSize = 56;
        actualSize = RReminder.adjustTitleSize(appContext, count, false);
        if(RReminder.isTablet(appContext)){
            assertWithMessage("normal screen (tablet) with 30 symbol string should have font size 56"). that(expectedTabletSize).isEqualTo(actualSize);
        } else {
            assertWithMessage("normal screen with 30 symbol string should have font size 28"). that(expectedSize).isEqualTo(actualSize);
        }

        count = 40;
        expectedSize = 22;
        expectedTabletSize = 44;
        actualSize = RReminder.adjustTitleSize(appContext, count, false);
        if(RReminder.isTablet(appContext)){
            assertWithMessage("normal screen (tablet) with 40 symbol string should have font size 44"). that(expectedTabletSize).isEqualTo(actualSize);
        } else {
            assertWithMessage("normal screen with 40 symbol string should have font size 22"). that(expectedSize).isEqualTo(actualSize);
        }

        //tablet scrren
        //small screen
        count = 10;
        expectedSize = 34;
        actualSize = RReminder.adjustTitleSize(appContext, count, true);
        assertWithMessage("small screen with 10 symbol string should have font size 34"). that(expectedSize).isEqualTo(actualSize);
        count = 20;
        expectedSize = 26;
        actualSize = RReminder.adjustTitleSize(appContext, count, true);
        assertWithMessage("small screen with 20 symbol string should have font size 26"). that(expectedSize).isEqualTo(actualSize);
        count = 30;
        expectedSize = 20;
        actualSize = RReminder.adjustTitleSize(appContext, count, true);
        assertWithMessage("small screen with 30 symbol string should have font size 20"). that(expectedSize).isEqualTo(actualSize);
        count = 40;
        expectedSize = 15;
        actualSize = RReminder.adjustTitleSize(appContext, count, true);
        assertWithMessage("small screen with 40 symbol string should have font size 15"). that(expectedSize).isEqualTo(actualSize);
    }
}
