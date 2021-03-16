package com.colormindapps.rest_reminder_alarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

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
        firstInstall();

        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {
            scenario.moveToState(Lifecycle.State.CREATED);
            scenario.moveToState(Lifecycle.State.STARTED);
            scenario.moveToState(Lifecycle.State.RESUMED);
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
        firstInstall();

        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {
            scenario.moveToState(Lifecycle.State.CREATED);
            scenario.moveToState(Lifecycle.State.STARTED);
            scenario.moveToState(Lifecycle.State.RESUMED);
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
    public void testTurnOffHint(){
        firstInstall();

        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {
            scenario.moveToState(Lifecycle.State.CREATED);
            scenario.moveToState(Lifecycle.State.STARTED);
            scenario.moveToState(Lifecycle.State.RESUMED);
            onView(isRoot()).perform(CustomActions.waitFor(1000));
            assertThat(scenario.getState()).isEqualTo(Lifecycle.State.RESUMED);
            onView(isRoot()).perform(CustomActions.waitFor(1000));
            onView(withText(RReminderTest.getResourceString(R.string.eula_accept))).perform(click());

            onView(withId(R.id.timer_layout)).perform(click());
            onView(isRoot()).perform(CustomActions.waitFor(1000));
            //The turn off hint should be visible until the 3rd successful turn off
            // 1st cycle - hint is visible
            String expectedTurnOffHint = RReminderTest.getResourceString(R.string.description_turn_off);
            onView(withId(R.id.description_text)).check(matches(withText(expectedTurnOffHint)));
            timerLongPress(2000);
            onView(withId(R.id.description_text)).check(matches(withText("")));
            // 2nd cycle - hint is visible
            onView(withId(R.id.timer_layout)).perform(click());
            onView(isRoot()).perform(CustomActions.waitFor(1000));
            onView(withId(R.id.description_text)).check(matches(withText(expectedTurnOffHint)));
            timerLongPress(2000);
            onView(withId(R.id.description_text)).check(matches(withText("")));
            // 3rd cycle - hint is visible
            onView(withId(R.id.timer_layout)).perform(click());
            onView(isRoot()).perform(CustomActions.waitFor(1000));
            onView(withId(R.id.description_text)).check(matches(withText(expectedTurnOffHint)));
            timerLongPress(2000);
            onView(withId(R.id.description_text)).check(matches(withText("")));
            // 4th cycle - hint is no longer displayed
            onView(withId(R.id.timer_layout)).perform(click());
            onView(isRoot()).perform(CustomActions.waitFor(1000));
            onView(withId(R.id.description_text)).check(matches(withText("")));
            timerLongPress(2000);
            onView(withId(R.id.description_text)).check(matches(withText("")));
            // 5th cycle - hint is not displayed
            onView(withId(R.id.timer_layout)).perform(click());
            onView(isRoot()).perform(CustomActions.waitFor(1000));
            onView(withId(R.id.description_text)).check(matches(withText("")));
            timerLongPress(2000);
            onView(withId(R.id.description_text)).check(matches(withText("")));

        }
    }

    @Test
    public void testTurnOffHintExtended(){
        firstInstall();

        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {
            scenario.moveToState(Lifecycle.State.CREATED);
            scenario.moveToState(Lifecycle.State.STARTED);
            scenario.moveToState(Lifecycle.State.RESUMED);
            onView(isRoot()).perform(CustomActions.waitFor(1000));
            assertThat(scenario.getState()).isEqualTo(Lifecycle.State.RESUMED);
            onView(isRoot()).perform(CustomActions.waitFor(1000));
            onView(withText(RReminderTest.getResourceString(R.string.eula_accept))).perform(click());

            onView(withId(R.id.timer_layout)).perform(click());
            onView(isRoot()).perform(CustomActions.waitFor(1000));
            // 1st cycle - hint is visible
            String expectedTurnOffHint = RReminderTest.getResourceString(R.string.description_turn_off);
            onView(withId(R.id.description_text)).check(matches(withText(expectedTurnOffHint)));
            //extending the current period
            onView(withId(R.id.button_period_end_extend)).perform(click());
            String expectedDescription = RReminderTest.getResourceString(R.string.description_extended_one_time);
            //after extending the period, the description will initially show hint about extending
            onView(withId(R.id.description_text)).check(matches(withText(expectedDescription)));
            onView(isRoot()).perform(CustomActions.waitFor(12000));
            //after 10 seconds the desription will change to turn off hint
            onView(withId(R.id.description_text)).check(matches(withText(expectedTurnOffHint)));
            //after next 10 seconds the description will change back to hint about being extended
            onView(isRoot()).perform(CustomActions.waitFor(12000));
            onView(withId(R.id.description_text)).check(matches(withText(expectedDescription)));
            //ending the 1st cycle, the description should be empty
            timerLongPress(2000);
            onView(withId(R.id.description_text)).check(matches(withText("")));


            // 2nd cycle - hint is visible
            onView(withId(R.id.timer_layout)).perform(click());
            onView(isRoot()).perform(CustomActions.waitFor(1000));
            onView(withId(R.id.description_text)).check(matches(withText(expectedTurnOffHint)));
            //extending the current period
            onView(withId(R.id.button_period_end_extend)).perform(click());
            //after extending the period, the description will initially show hint about extending
            onView(withId(R.id.description_text)).check(matches(withText(expectedDescription)));
            onView(isRoot()).perform(CustomActions.waitFor(12000));
            //after 10 seconds the desription will change to turn off hint
            onView(withId(R.id.description_text)).check(matches(withText(expectedTurnOffHint)));
            //after next 10 seconds the description will change back to hint about being extended
            onView(isRoot()).perform(CustomActions.waitFor(12000));
            onView(withId(R.id.description_text)).check(matches(withText(expectedDescription)));
            //extending the current period for second time
            onView(withId(R.id.button_period_end_extend)).perform(click());
            String expectedExtendedTwiceDescription = String.format(getApplicationContext().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.description_extended),2);
            //the description will contain hint about 2nd extension
            onView(withId(R.id.description_text)).check(matches(withText(expectedExtendedTwiceDescription)));
            onView(isRoot()).perform(CustomActions.waitFor(12000));
            //after 10 seconds the desription will change to turn off hint
            onView(withId(R.id.description_text)).check(matches(withText(expectedTurnOffHint)));
            onView(isRoot()).perform(CustomActions.waitFor(12000));
            //after 10 seconds the desription will change back to hint about 2nd extension
            onView(withId(R.id.description_text)).check(matches(withText(expectedExtendedTwiceDescription)));
            timerLongPress(2000);
            onView(withId(R.id.description_text)).check(matches(withText("")));

            // 3rd cycle - hint is visible
            onView(withId(R.id.timer_layout)).perform(click());
            onView(isRoot()).perform(CustomActions.waitFor(1000));
            onView(withId(R.id.description_text)).check(matches(withText(expectedTurnOffHint)));
            timerLongPress(2000);
            onView(withId(R.id.description_text)).check(matches(withText("")));

            // 4th cycle - hint is no longer displayed
            onView(withId(R.id.timer_layout)).perform(click());
            onView(isRoot()).perform(CustomActions.waitFor(1000));
            //extending the current period
            onView(withId(R.id.button_period_end_extend)).perform(click());
            onView(withId(R.id.description_text)).check(matches(withText(expectedDescription)));
            //in 4th cycle after extending the description remains the same
            onView(isRoot()).perform(CustomActions.waitFor(15000));
            //after 10 seconds the desription will change back to hint about 2nd extension
            onView(withId(R.id.description_text)).check(matches(withText(expectedDescription)));
            //extending the current period
            onView(withId(R.id.button_period_end_extend)).perform(click());
            onView(withId(R.id.description_text)).check(matches(withText(expectedExtendedTwiceDescription)));
            onView(isRoot()).perform(CustomActions.waitFor(15000));
            //in 4th cycle after extending the description remains the same
            onView(withId(R.id.description_text)).check(matches(withText(expectedExtendedTwiceDescription)));
            timerLongPress(2000);
            onView(withId(R.id.description_text)).check(matches(withText("")));
        }
    }

    @Test
    public void testDisableCountdownOnPause(){
        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {
            scenario.moveToState(Lifecycle.State.CREATED);
            scenario.moveToState(Lifecycle.State.STARTED);
            scenario.moveToState(Lifecycle.State.RESUMED);
            assertThat(scenario.getState()).isEqualTo(Lifecycle.State.RESUMED);
            onView(isRoot()).perform(CustomActions.waitFor(2000));
            Log.d("RREMINDER_MAIN_SCENARION", "activity state: "+scenario.getState());
            Log.d("RREMINDER_MAIN_SCENARION", "move to resumed");
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
                        activity.stopReminder();
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
        expectedSize = 70;
        expectedTabletSize = 140;
        actualSize = RReminder.adjustTitleSize(appContext, count, false);
        if(RReminder.isTablet(appContext)){
            assertWithMessage("normal screen (tablet) with 10 symbol string should have font size 100"). that(expectedTabletSize).isEqualTo(actualSize);
        } else {
            assertWithMessage("normal screen with 10 symbol string should have font size 50"). that(expectedSize).isEqualTo(actualSize);
        }

        count = 20;
        expectedSize = 38;
        expectedTabletSize = 76;
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

    @Test
    public void testTimerStopOnPause() {
        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {
            scenario.moveToState(Lifecycle.State.CREATED);
            scenario.moveToState(Lifecycle.State.STARTED);
            scenario.moveToState(Lifecycle.State.RESUMED);
            assertThat(scenario.getState()).isEqualTo(Lifecycle.State.RESUMED);
            scenario.onActivity(
                    activity -> {
                        assertWithMessage("at the start the timer shouldnt be active").that(activity.countdown == null).isTrue();
                        activity.startReminder();
                    });

            onView(isRoot()).perform(CustomActions.waitFor(2000));

            scenario.onActivity(
                    activity -> {
                        assertWithMessage("after launching reminder the countdown timer should be active").that(activity.countdown != null && activity.countdown.isRunning).isTrue();
                    });

            scenario.moveToState(Lifecycle.State.STARTED);
            scenario.onActivity(
                    activity -> {
                        assertWithMessage("after onPause was called, the countdown timer should be inactive").that(activity.countdown.isRunning).isFalse();
                    });
            scenario.moveToState(Lifecycle.State.STARTED);
            scenario.moveToState(Lifecycle.State.RESUMED);
            onView(isRoot()).perform(CustomActions.waitFor(1000));
            scenario.onActivity(
                    activity -> {
                        assertWithMessage("after onResume was called the countdown timer should be active").that(activity.countdown.isRunning).isTrue();
                    });
        }
    }

    private void timerLongPress(int duration){
        onView(withId(R.id.timer_layout)).perform(TouchActions.pressAndHold());
        onView(isRoot()).perform(CustomActions.waitFor(duration));
        onView(withId(R.id.timer_layout)).perform(TouchActions.release());
        TouchActions.tearDown();
    }

    private void firstInstall(){
        privatePreferences = getApplicationContext().getSharedPreferences(RReminder.PRIVATE_PREF, Context.MODE_PRIVATE);
        privateEditor = privatePreferences.edit();
        privateEditor.putInt(RReminder.VERSION_KEY, 0);
        privateEditor.putBoolean(RReminder.EULA_ACCEPTED, false);
        privateEditor.putBoolean(RReminder.DISPLAY_TURN_OFF_HINT, true);
        privateEditor.putInt(RReminder.TURN_OFF_COUNT,0);
        privateEditor.commit();
    }
}
