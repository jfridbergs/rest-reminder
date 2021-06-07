package com.colormindapps.rest_reminder_alarm;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.preference.PreferenceManager;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoPreferencesOnly {

    //Made this additional test file for preference tests, as it is launched from PreferenceActivity instead of MainActivity, because the function for rotating screen requires the correct base activity
    // (cant call rotate screen in preference activity by using MainActivity test rule) or i dont know yey, how to set up multiple test rules.

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    float scaledDensity;
    String expectedOfflineTitle, expectedOnlineWorkTitle, expectedOnlineRestTitle;
    int extendOptionCount, extendBaseLength;
    String workPeriodLengthKey, restPeriodLengthKey, workPeriodSoundKey, restPeriodSoundKey, extendCountKey, extendBaseLengthKey;
    IntentFilter filter;
    String actualModeSummary, actualWorkLengthSummary, actualRestLengthSummary, actualWorkAudioSummary, actualRestAudioSummary, actualExtendCountSummary, actualExtendLenghtSummary;
    String expectedModeSummary, expectedWorkLengthSummary, expectedRestLengthSummary, expectedWorkAudioSummary, expectedRestAudioSummary, expectedExtendCountSummary, expectedExtendBaseLengthSummary;
    Uri originalWorkUri, originalRestUri, originalApproxUri;

    String debug = "ESPRESSO_PREFERENCES_ONLY";
    @Rule
    public IntentsTestRule<PreferenceXActivity> pActivityRule =
            new IntentsTestRule<PreferenceXActivity>(PreferenceXActivity.class, false, true);

    @Before
    public void setUp(){



        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor   = preferences.edit();
        scaledDensity = getApplicationContext().getResources().getDisplayMetrics().scaledDensity;

        expectedOfflineTitle = RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.reminder_off_title).toUpperCase();
        expectedOnlineWorkTitle = RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.on_work_period).toUpperCase();
        expectedOnlineRestTitle = RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.on_rest_period).toUpperCase();
        extendOptionCount = preferences.getInt(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key), 1);
        extendBaseLength = preferences.getInt(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key), 5);

        workPeriodLengthKey = RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_key);
        restPeriodLengthKey = RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key);
        workPeriodSoundKey = RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_start_sound_key);
        restPeriodSoundKey = RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_start_sound_key);
        extendCountKey = RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key);
        extendBaseLengthKey = RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key);

        filter = new IntentFilter();
        filter.addAction(RReminder.CUSTOM_INTENT_TEST_PREFERENCES_MODE);
        filter.addAction(RReminder.CUSTOM_INTENT_TEST_PREFERENCES_PERIOD);
        filter.addAction(RReminder.CUSTOM_INTENT_TEST_PREFERENCES_EXTEND);

        editor.putBoolean(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.putBoolean(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_approx_notification_key), true);
        editor.putBoolean(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_end_period_key), true);
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_key), "00:20");
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), "00:15");
        editor.putInt(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key), 1);
        editor.commit();
    }

    @After
    public void tearDown(){

        editor.putBoolean(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), false);
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_key), "0");
        editor.putBoolean(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_end_period_key), true);
        editor.putBoolean(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_key), "00:45");
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), "00:15");
        editor.putInt(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key), 3);
        editor.putInt(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key), 5);
        editor.commit();
    }

    @Test
    public void testNumberPickerPreferenceStateAfterRotation(){
        int expectedHour, expectedMinute, actualHour, actualMinute;

        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title))).perform(click());
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_title))).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.time_preference_first_picker)).perform(CustomActions.setValue(3));
        onView(withId(R.id.time_preference_second_picker)).perform(CustomActions.setValue(47));

        expectedHour = 3;
        expectedMinute = 47;
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        if(RReminder.isPortrait(getApplicationContext())){
            pActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            pActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        //after rotating screen the preference activity should be visible
        onView(withId(R.id.time_preference_first_picker)).check(matches(isDisplayed()));
        onView(withId(R.id.time_preference_first_picker)).check(matches(CustomMatchers.withNumberPickerValue(expectedHour)));
        onView(withId(R.id.time_preference_second_picker)).check(matches(CustomMatchers.withNumberPickerValue(expectedMinute)));

        if(RReminder.isPortrait(getApplicationContext())){
            pActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            pActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        Espresso.pressBack();
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_title))).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.time_preference_first_picker)).perform(CustomActions.setValue(2));
        onView(withId(R.id.time_preference_second_picker)).perform(CustomActions.setValue(29));
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        expectedHour = 2;
        expectedMinute = 29;

        if(RReminder.isPortrait(getApplicationContext())){
            pActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            pActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        onView(withId(R.id.time_preference_first_picker)).check(matches(isDisplayed()));
        onView(withId(R.id.time_preference_first_picker)).check(matches(CustomMatchers.withNumberPickerValue(expectedHour)));
        onView(withId(R.id.time_preference_second_picker)).check(matches(CustomMatchers.withNumberPickerValue(expectedMinute)));

        if(RReminder.isPortrait(getApplicationContext())){
            pActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            pActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        onView(isRoot()).perform(CustomActions.waitFor(2000));

        Espresso.pressBack();




        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_title))).check(matches(isDisplayed()));

        Espresso.pressBack();
        onView(isRoot()).perform(swipeUp());
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_period_extend_title))).perform(click());
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_title))).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.number_preference_picker)).perform(CustomActions.setValue(3));
        onView(isRoot()).perform(CustomActions.waitFor(1000));

        int expectedCount = 3;


        if(RReminder.isPortrait(getApplicationContext())){
            pActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            pActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        onView(withId(R.id.number_preference_picker)).check(matches(isDisplayed()));
        onView(withId(R.id.number_preference_picker)).check(matches(CustomMatchers.withNumberPickerValue(expectedCount)));
        if(RReminder.isPortrait(getApplicationContext())){
            pActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            pActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        onView(withText("CANCEL")).perform(click());



        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_title))).check(matches(isDisplayed()));
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_title))).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.number_preference_picker)).perform(CustomActions.setValue(57));
        onView(isRoot()).perform(CustomActions.waitFor(1000));

        int expectedBaseDuration = 57;

        if(RReminder.isPortrait(getApplicationContext())){
            pActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            pActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        onView(withId(R.id.number_preference_picker)).check(matches(isDisplayed()));
        onView(withId(R.id.number_preference_picker)).check(matches(CustomMatchers.withNumberPickerValue(expectedBaseDuration)));

    }
}
