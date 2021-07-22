package com.colormindapps.rest_reminder_alarm.DB;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.colormindapps.rest_reminder_alarm.CustomActions;
import com.colormindapps.rest_reminder_alarm.CustomMatchers;
import com.colormindapps.rest_reminder_alarm.MainActivity;
import com.colormindapps.rest_reminder_alarm.PreferenceXActivity;
import com.colormindapps.rest_reminder_alarm.R;
import com.colormindapps.rest_reminder_alarm.RReminderMobile;
import com.colormindapps.rest_reminder_alarm.RReminderRoomDatabase;
import com.colormindapps.rest_reminder_alarm.RReminderTest;
import com.colormindapps.rest_reminder_alarm.TouchActions;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.times;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.colormindapps.rest_reminder_alarm.CustomMatchers.atPosition;
import static com.colormindapps.rest_reminder_alarm.CustomMatchers.withBackgroundColor;
import static com.colormindapps.rest_reminder_alarm.Espresso.RecyclerViewItemCountAssertion.withItemCount;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.StringContains.containsString;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoDatabase {

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    float scaledDensity;
    String expectedOfflineTitle, expectedOnlineWorkTitle, expectedOnlineRestTitle;
    int extendOptionCount, extendBaseLength;

    String debug = "ESPRESSO_PREFERENCES";

    @Rule
    public IntentsTestRule<MainActivity> mActivityRule =
            new IntentsTestRule<MainActivity>(MainActivity.class, false, true);

    @Before
    public void setUp(){
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor   = preferences.edit();
        scaledDensity = getApplicationContext().getResources().getDisplayMetrics().scaledDensity;

        expectedOfflineTitle = RReminderTest.getResourceString(R.string.reminder_off_title);
        expectedOnlineWorkTitle = RReminderTest.getResourceString(R.string.on_work_period).toUpperCase();
        expectedOnlineRestTitle = RReminderTest.getResourceString(R.string.on_rest_period).toUpperCase();
        extendOptionCount = preferences.getInt(RReminderTest.getResourceString(R.string.pref_period_extend_options_key), 1);
        extendBaseLength = preferences.getInt(RReminderTest.getResourceString(R.string.pref_period_extend_length_key), 5);


        editor.putBoolean(RReminderTest.getResourceString(R.string.pref_enable_extend_key), true);
        editor.putBoolean(RReminderTest.getResourceString(R.string.pref_enable_approx_notification_key), true);
        editor.putBoolean(RReminderTest.getResourceString(R.string.pref_end_period_key), true);
        editor.putString(RReminderTest.getResourceString(R.string.pref_work_period_length_key), "15:55");
        editor.putString(RReminderTest.getResourceString(R.string.pref_rest_period_length_key), "15:55");
        editor.putBoolean(RReminderTest.getResourceString(R.string.pref_enable_short_periods_key), false);
        editor.putInt(RReminderTest.getResourceString(R.string.pref_period_extend_options_key), 1);
        editor.commit();

    }

    @After
    public void tearDown(){
        RReminderMobile.cancelCounterAlarm(getApplicationContext(), mActivityRule.getActivity().periodType, mActivityRule.getActivity().extendCount, mActivityRule.getActivity().periodEndTimeValue);
        RReminderMobile.stopCounterService(getApplicationContext(), mActivityRule.getActivity().periodType);

        editor.putBoolean(RReminderTest.getResourceString(R.string.pref_enable_extend_key), false);
        editor.putString(RReminderTest.getResourceString(R.string.pref_mode_key), "0");
        editor.putBoolean(RReminderTest.getResourceString(R.string.pref_end_period_key), true);
        editor.putBoolean(RReminderTest.getResourceString(R.string.pref_enable_extend_key), true);
        editor.putBoolean(RReminderTest.getResourceString(R.string.pref_enable_short_periods_key), false);
        editor.putString(RReminderTest.getResourceString(R.string.pref_work_period_length_key), "00:45");
        editor.putString(RReminderTest.getResourceString(R.string.pref_rest_period_length_key), "00:15");
        editor.putInt(RReminderTest.getResourceString(R.string.pref_period_extend_options_key), 3);
        editor.putInt(RReminderTest.getResourceString(R.string.pref_period_extend_length_key), 5);
        editor.commit();
    }

    @Test
    public void testSessionStoredProperly(){
        //run reminder for multiple periods - 75 seconds, notif activity is updated with last period info
        long sessionStartFirst = Calendar.getInstance().getTimeInMillis();
        runSession(false);
        long sessionEndFirst = Calendar.getInstance().getTimeInMillis();
        String startString = RReminder.getTimeString(getApplicationContext(), sessionStartFirst).toString();
        String endString = RReminder.getTimeString(getApplicationContext(), sessionEndFirst).toString();
        String expectedTimeStringFirst = startString +" - "+ endString;
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(withText(expectedOfflineTitle)));


        //launch second session

        long sessionStartSecond = Calendar.getInstance().getTimeInMillis();
        runSession(false);
        long sessionEndSecond = Calendar.getInstance().getTimeInMillis();
        startString = RReminder.getTimeString(getApplicationContext(), sessionStartSecond).toString();
        endString = RReminder.getTimeString(getApplicationContext(), sessionEndSecond).toString();
        String expectedTimeStringSecond = startString +" - "+ endString;
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(withText(expectedOfflineTitle)));

        onView(withId(R.id.button_open_sessions)).perform(click());

        onView(withText(expectedTimeStringFirst)).check(matches(isDisplayed()));
        onView(withText(expectedTimeStringSecond)).check(matches(isDisplayed()));

    }

    @Test
    public void testDeleteAll(){
        long sessionStartFirst = Calendar.getInstance().getTimeInMillis();
        runSession(false);
        long sessionEndFirst = Calendar.getInstance().getTimeInMillis();
        String startString = RReminder.getTimeString(getApplicationContext(), sessionStartFirst).toString();
        String endString = RReminder.getTimeString(getApplicationContext(), sessionEndFirst).toString();
        String expectedTimeStringFirst = startString +" - "+ endString;

        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(withText(expectedOfflineTitle)));

        onView(withId(R.id.button_open_sessions)).perform(click());

        onView(withText(expectedTimeStringFirst)).check(matches(isDisplayed()));
        onView(withId(R.id.delete_all)).perform(click());
        onView(withText(expectedTimeStringFirst)).check(doesNotExist());
    }

    @Test
    public void testSessionPeriodDefaultAutomatic(){
        long sessionStart = Calendar.getInstance().getTimeInMillis();
        int expectedCount = 6;
        long[] endTime  = runSession(true);
        long sessionEnd = Calendar.getInstance().getTimeInMillis();
        String startString = RReminder.getTimeString(getApplicationContext(), sessionStart).toString();
        String endString = RReminder.getTimeString(getApplicationContext(), sessionEnd).toString();
        String expectedTimeString = startString +" - "+ endString;
        String firstIntermediateEndTime = RReminder.getTimeString(getApplicationContext(), endTime[0]).toString();
        String secondIntermediateEndTime = RReminder.getTimeString(getApplicationContext(), endTime[1]).toString();
        String thirdIntermediateEndTime = RReminder.getTimeString(getApplicationContext(), endTime[2]).toString();
        int colorWork = getApplicationContext().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.work);
        int colorRest = getApplicationContext().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.rest);

        onView(withId(R.id.button_open_sessions)).perform(click());

        onView(withText(expectedTimeString)).check(matches(isDisplayed()));
        onView(withText(expectedTimeString)).perform(click());

        onView(withId(R.id.session_Id)).check(matches(withText(containsString(startString))));
        onView(withId(R.id.recyclerview_periods)).check(withItemCount(6));
        //check the first and last periods for their end time values
        onView(withId(R.id.recyclerview_periods))
        .perform(scrollToPosition(0))
                .check(matches(atPosition(0, hasDescendant(withText(containsString(firstIntermediateEndTime))))));
        onView(withId(R.id.recyclerview_periods))
                .check(matches(atPosition(0, withBackgroundColor(colorWork))));
        onView(withId(R.id.recyclerview_periods))
                .perform(scrollToPosition(5))
                .check(matches(atPosition(5, hasDescendant(withText(containsString(endString))))));
        onView(withId(R.id.recyclerview_periods))
                .check(matches(atPosition(5, withBackgroundColor(colorRest))));
        //check periods in middle for their end times
        onView(withId(R.id.recyclerview_periods))
                .perform(scrollToPosition(1))
                .check(matches(atPosition(1, hasDescendant(withText(containsString(secondIntermediateEndTime))))));
        onView(withId(R.id.recyclerview_periods))
                .check(matches(atPosition(1, withBackgroundColor(colorRest))));
        onView(withId(R.id.recyclerview_periods))
                .perform(scrollToPosition(3))
                .check(matches(atPosition(3, hasDescendant(withText(containsString(thirdIntermediateEndTime))))));
        onView(withId(R.id.recyclerview_periods))
                .check(matches(atPosition(3, withBackgroundColor(colorRest))));

    }

    public long[] runSession(boolean longer){
        long[] endTimes = new long[3];
        onView(withId(R.id.timer_layout)).perform(click());

        onView(isRoot()).perform(CustomActions.waitFor(20000));
        onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
        endTimes[0] = Calendar.getInstance().getTimeInMillis();
        onView(withId(R.id.notification_button)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));

        onView(isRoot()).perform(CustomActions.waitFor(10000));
        onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_button)).perform(click());
        endTimes[1] = Calendar.getInstance().getTimeInMillis();
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));


        onView(isRoot()).perform(CustomActions.waitFor(20000));
        onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_button)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));

        if(longer){
            onView(isRoot()).perform(CustomActions.waitFor(10000));
            onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
            onView(withId(R.id.notification_button)).perform(click());
            onView(isRoot()).perform(CustomActions.waitFor(1000));
            onView(withId(R.id.period_title)).check(matches(isDisplayed()));
            endTimes[2] = Calendar.getInstance().getTimeInMillis();

            onView(isRoot()).perform(CustomActions.waitFor(20000));
            onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
            onView(withId(R.id.notification_button)).perform(click());
            onView(isRoot()).perform(CustomActions.waitFor(1000));
            onView(withId(R.id.period_title)).check(matches(isDisplayed()));
        }

        timerLongPress(2000);
        return endTimes;
    }

    private void timerLongPress(int duration){
        onView(withId(R.id.timer_layout)).perform(TouchActions.pressAndHold());
        onView(isRoot()).perform(CustomActions.waitFor(duration));
        onView(withId(R.id.timer_layout)).perform(TouchActions.release());
        TouchActions.tearDown();
    }


}
