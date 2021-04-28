package com.colormindapps.rest_reminder_alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.PreferenceMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.times;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoPreferences {

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    float scaledDensity;
    String expectedOfflineTitle, expectedOnlineWorkTitle, expectedOnlineRestTitle;
    int extendOptionCount, extendBaseLength;
    String workPeriodLengthKey, restPeriodLengthKey, workPeriodSoundKey, restPeriodSoundKey, extendCountKey, extendBaseLengthKey, reminderModeKey;
    IntentFilter filter;
    MyReceiver receiver;
    String actualModeSummary, actualWorkLengthSummary, actualRestLengthSummary, actualWorkAudioSummary, actualRestAudioSummary, actualExtendCountSummary, actualExtendLenghtSummary;
    String expectedModeSummary, expectedWorkLengthSummary, expectedRestLengthSummary, expectedWorkAudioSummary, expectedRestAudioSummary, expectedExtendCountSummary, expectedExtendBaseLengthSummary;
    Uri originalWorkUri, originalRestUri, originalApproxUri;

    String debug = "ESPRESSO_PREFERENCES";

    @Rule
    public IntentsTestRule<MainActivity> mActivityRule =
            new IntentsTestRule<MainActivity>(MainActivity.class, false, true);

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
        reminderModeKey = RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_key);

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
        receiver = new MyReceiver();
        mActivityRule.getActivity().registerReceiver(receiver, filter);
    }

    @After
    public void tearDown(){
        RReminderMobile.cancelCounterAlarm(getApplicationContext(), mActivityRule.getActivity().periodType, mActivityRule.getActivity().extendCount, mActivityRule.getActivity().periodEndTimeValue);
        RReminderMobile.stopCounterService(getApplicationContext(), mActivityRule.getActivity().periodType);
        mActivityRule.getActivity().unregisterReceiver(receiver);

        editor.putBoolean(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), false);
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_key), "0");
        editor.putBoolean(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_end_period_key), true);
        editor.putBoolean(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.putBoolean(RReminderTest.getResourceString(R.string.pref_enable_short_periods_key), false);
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_key), "00:45");
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), "00:15");
        editor.putInt(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key), 3);
        editor.putInt(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key), 5);
        editor.commit();
    }

    @Test
    public void testPreferenceDescriptions() {

        openPreferences();
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        intended(hasComponent(PreferenceXActivity.class.getName()));
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title))).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        Espresso.pressBack();
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_period_extend_title))).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        Espresso.pressBack();
        Espresso.pressBack();
        // MainActivity should be visible on screen
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));




        expectedModeSummary = RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_summary_automatic);
        expectedWorkLengthSummary = RReminder.getFormatedValue(getApplicationContext(), 0, preferences.getString(workPeriodLengthKey, RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.default_work_length_string)));
        expectedRestLengthSummary = RReminder.getFormatedValue(getApplicationContext(), 0, preferences.getString(restPeriodLengthKey, RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.default_rest_length_string)));

        String temp = preferences.getString(workPeriodSoundKey, "DEFAULT_RINGTONE_URI");

        if (temp.equals("DEFAULT_RINGTONE_URI")){
            originalWorkUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {
            originalWorkUri = Uri.parse(temp);
        }

        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), originalWorkUri);
        expectedWorkAudioSummary = ringtone.getTitle(getApplicationContext());

        temp = preferences.getString(restPeriodSoundKey, "DEFAULT_RINGTONE_URI");

        if (temp.equals("DEFAULT_RINGTONE_URI")){
            originalRestUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {
            originalRestUri = Uri.parse(temp);
        }

        ringtone = RingtoneManager.getRingtone(getApplicationContext(), originalRestUri);
        expectedRestAudioSummary = ringtone.getTitle(getApplicationContext());

        Integer value = preferences.getInt(extendCountKey, RReminder.DEFAULT_EXTEND_COUNT);
        if(value==1){
            expectedExtendCountSummary=RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_options_single);
        } else {
            expectedExtendCountSummary = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_options_multiple), value);
        }

        value = preferences.getInt(extendBaseLengthKey, RReminder.DEFAULT_EXTEND_BASE_LENGTH);
        if(value==1){
            expectedExtendBaseLengthSummary= RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_minute_single);
        } else {
            expectedExtendBaseLengthSummary = getApplicationContext().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_minute_multiple, value);
        }

        assertWithMessage("the summary of mode preference should say automatic").that(expectedModeSummary).isEqualTo( actualModeSummary);
       assertWithMessage("the summary of work length preference should match").that(expectedWorkLengthSummary).isEqualTo(actualWorkLengthSummary);
        assertWithMessage("the summary of rest length preference should match").that(expectedRestLengthSummary).isEqualTo(actualRestLengthSummary);
       assertWithMessage("the summary of work period audio preference should match").that(expectedWorkAudioSummary).isEqualTo(actualWorkAudioSummary);
       assertWithMessage("the summary of rest period audio preference should match").that(expectedRestAudioSummary).isEqualTo(actualRestAudioSummary);
        assertWithMessage("the summary of extend count preference should match").that(expectedExtendCountSummary).isEqualTo(actualExtendCountSummary);
        assertWithMessage("tthe summary of extend base length preference should match").that(expectedExtendBaseLengthSummary).isEqualTo(actualExtendLenghtSummary);

    }

    @Test
    public void testUpdatedWorkPeriodAfterPreferenceChange(){
        onView(withId(R.id.timer_layout)).perform(click());
        long periodEndTimeValue = mActivityRule.getActivity().periodEndTimeValue;
        long expectedPeriodEndTime1 = periodEndTimeValue + 20*60*1000;
        openPreferences();
        intended(hasComponent(PreferenceXActivity.class.getName()));
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title))).perform(click());


        changePeriodLengthPreference(RReminder.WORK,0,40);


        Espresso.pressBack();
        Espresso.pressBack();

        long actualEndTimeValue1 = mActivityRule.getActivity().periodEndTimeValue;
        long difference = Math.abs(actualEndTimeValue1-expectedPeriodEndTime1);
        Log.d("PREFERENCE_TEST", "difference: "+difference);
        assertWithMessage("after changing work length preference, the service should be updated").that( difference<1000).isTrue();

        expectedPeriodEndTime1 = actualEndTimeValue1 - 10*60*1000;
        openPreferences();
        intended(hasComponent(PreferenceXActivity.class.getName()), times(2));
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title))).perform(click());

        changePeriodLengthPreference(RReminder.WORK,0,30);


        Espresso.pressBack();
        Espresso.pressBack();

        actualEndTimeValue1 = mActivityRule.getActivity().periodEndTimeValue;
        difference = Math.abs(actualEndTimeValue1-expectedPeriodEndTime1);
        Log.d(debug, "difference: "+difference);
        assertWithMessage("after changing work length preference 2nd time, the service should be updated").that( difference<1000).isTrue();
    }

    @Test
    public void testShortPeriodPreferenceDisabled(){
        boolean shortPeriodsEnabled = preferences.getBoolean(getApplicationContext().getResources().getString(R.string.pref_enable_short_periods_key), false);
        assertWithMessage("at the start of the test short periods are disabled").that( shortPeriodsEnabled).isFalse();

        onView(withId(R.id.timer_layout)).perform(click());

        openPreferences();
        intended(hasComponent(PreferenceXActivity.class.getName()));
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title))).perform(click());
        onView(withText(RReminderTest.getResourceString(R.string.pref_enable_short_periods_title))).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        shortPeriodsEnabled = preferences.getBoolean(getApplicationContext().getResources().getString(R.string.pref_enable_short_periods_key), false);
        assertWithMessage("while the reminder is running, the enable short periods preference should be disabled (clicking on it wont have any effect)").that( shortPeriodsEnabled).isFalse();
        Espresso.pressBack();
        Espresso.pressBack();
        timerLongPress(2000);
        openPreferences();
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title))).perform(click());
        onView(withText(RReminderTest.getResourceString(R.string.pref_enable_short_periods_title))).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        shortPeriodsEnabled = preferences.getBoolean(getApplicationContext().getResources().getString(R.string.pref_enable_short_periods_key), false);
        assertWithMessage("when reminder is no longer running, short period preference can be interacted with, one click on it enables short periods").that( shortPeriodsEnabled).isTrue();
        Espresso.pressBack();
        Espresso.pressBack();
        onView(withId(R.id.timer_layout)).perform(click());

        openPreferences();
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title))).perform(click());
        onView(withText(RReminderTest.getResourceString(R.string.pref_enable_short_periods_title))).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        shortPeriodsEnabled = preferences.getBoolean(getApplicationContext().getResources().getString(R.string.pref_enable_short_periods_key), false);
        assertWithMessage("turning the reminder back on disables the enable short periods preference, it is still enabled after a click on it").that( shortPeriodsEnabled).isTrue();
    }

    @Test
    public void testShortPeriodPreference(){
        boolean shortPeriodsEnabled = preferences.getBoolean(getApplicationContext().getResources().getString(R.string.pref_enable_short_periods_key), false);
        assertWithMessage("at the start of the test short periods are disabled").that( shortPeriodsEnabled).isFalse();
        openPreferences();
        intended(hasComponent(PreferenceXActivity.class.getName()));
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title))).perform(click());
        onView(withText(RReminderTest.getResourceString(R.string.pref_enable_short_periods_title))).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        shortPeriodsEnabled = preferences.getBoolean(getApplicationContext().getResources().getString(R.string.pref_enable_short_periods_key), false);
        assertWithMessage("short period preference is now enabled").that( shortPeriodsEnabled).isTrue();
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_title))).perform(click());
        onView(withId(R.id.short_period_warning)).check(matches(isDisplayed()));
        //TO-DO: finish the test
        onView(withId(R.id.time_preference_second_picker)).perform(CustomActions.setValue(5));
        onView(withText("OK")).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        expectedWorkLengthSummary = RReminder.getFormatedValue(getApplicationContext(), 0, "00:05");
        Log.d("ESPR_PREF", "actualWorkLengthSummary: "+actualWorkLengthSummary);
        assertWithMessage("the summary of work length preference should be 5 minutes").that(actualWorkLengthSummary).isEqualTo(expectedWorkLengthSummary);
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_title))).perform(click());
        onView(withId(R.id.short_period_warning)).check(matches(isDisplayed()));
        onView(withId(R.id.time_preference_second_picker)).perform(CustomActions.setValue(3));
        onView(withText("OK")).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));

        expectedRestLengthSummary = RReminder.getFormatedValue(getApplicationContext(), 0,"00:03");
        assertWithMessage("the summary of rest length preference should be 3 minutes").that(actualRestLengthSummary).isEqualTo(expectedRestLengthSummary);
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_title))).perform(click());
        onView(withId(R.id.time_preference_second_picker)).check(matches(CustomMatchers.withNumberPickerValue(5)));
        onView(withText("Cancel")).perform(click());
        onView(withText(RReminderTest.getResourceString(R.string.pref_enable_short_periods_title))).perform(click());
        expectedWorkLengthSummary = RReminder.getFormatedValue(getApplicationContext(), 0, preferences.getString(workPeriodLengthKey, RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.default_work_length_string)));
        assertWithMessage("after disabling the short period the summary of work length preference should be 10 minutes").that(actualWorkLengthSummary).isEqualTo(expectedWorkLengthSummary);
        expectedRestLengthSummary = RReminder.getFormatedValue(getApplicationContext(), 0, preferences.getString(restPeriodLengthKey, RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.default_rest_length_string)));
        assertWithMessage("after disabling the short periodsthe summary of rest length preference should be 10 minutes").that(actualRestLengthSummary).isEqualTo(expectedRestLengthSummary);
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_title))).perform(click());
        onView(withId(R.id.short_period_warning)).check(matches(not(isDisplayed())));
        onView(withId(R.id.time_preference_first_picker)).check(matches(CustomMatchers.withNumberPickerValue(0)));
        onView(withId(R.id.time_preference_second_picker)).check(matches(CustomMatchers.withNumberPickerValue(10)));

        onView(withId(R.id.time_preference_second_picker)).perform(CustomActions.setValue(3));
        onView(withId(R.id.time_preference_second_picker)).check(matches(CustomMatchers.withNumberPickerValue(53)));
        onView(withText("Cancel")).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_title))).perform(click());
        onView(withId(R.id.short_period_warning)).check(matches(not(isDisplayed())));
        onView(withId(R.id.time_preference_first_picker)).check(matches(CustomMatchers.withNumberPickerValue(0)));
        onView(withId(R.id.time_preference_second_picker)).check(matches(CustomMatchers.withNumberPickerValue(10)));

        onView(withId(R.id.time_preference_second_picker)).perform(CustomActions.setValue(7));
        onView(withId(R.id.time_preference_second_picker)).check(matches(CustomMatchers.withNumberPickerValue(57)));

    }

    @Test
    public void testMultiplePeriodChangesInOneSession(){

        //testing work period preferences with 2 changes within one preference session
        onView(withId(R.id.timer_layout)).perform(click());

        long periodEndTimeValue = mActivityRule.getActivity().periodEndTimeValue;
        //Since we are changing work period length to 25 mins, we need to add 5 mins to the expected time, because the original work period length is set to 20 mins
        long expectedPeriodEndTime1 = periodEndTimeValue + 5*60*1000;
        openPreferences();
        intended(hasComponent(PreferenceXActivity.class.getName()));
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title))).perform(click());


        changePeriodLengthPreference(RReminder.WORK,0,40);

        onView(isRoot()).perform(CustomActions.waitFor(2000));

        changePeriodLengthPreference(RReminder.WORK,0,25);

        Espresso.pressBack();
        Espresso.pressBack();

        long actualEndTimeValue1 = mActivityRule.getActivity().periodEndTimeValue;
        long difference = Math.abs(actualEndTimeValue1-expectedPeriodEndTime1);
        Log.d(debug, "difference: "+difference);
        assertWithMessage("after changing work length preference 2 times, the service should be updated and match the latest change").that( difference<1000).isTrue();


        //testing rest period preferences with 3 changes within 1 preference session
        //switching to rest period
        onView(withId(R.id.swipe_area_text)).perform(swipeRight());
        onView(isRoot()).perform(CustomActions.waitFor(1000));

        periodEndTimeValue = mActivityRule.getActivity().periodEndTimeValue;
        //Since we are changing final period length to 12 mins, we need to subtrack 3 mins from the expected time, because the original rest period length is set to 15 mins
        expectedPeriodEndTime1 = periodEndTimeValue - 3*60*1000;

        openPreferences();
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        intended(hasComponent(PreferenceXActivity.class.getName()), times(2));

        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title))).perform(click());

        changePeriodLengthPreference(RReminder.REST, 0,30);

        onView(isRoot()).perform(CustomActions.waitFor(2000));

        changePeriodLengthPreference(RReminder.REST, 0,18);

        onView(isRoot()).perform(CustomActions.waitFor(2000));

        changePeriodLengthPreference(RReminder.REST, 0,12);

        Espresso.pressBack();
        Espresso.pressBack();

        actualEndTimeValue1 = mActivityRule.getActivity().periodEndTimeValue;
        difference = Math.abs(actualEndTimeValue1-expectedPeriodEndTime1);
        Log.d("PREFERENCE_TEST", "difference: "+difference);
        assertWithMessage("after changing rest length preference 3 time, the service should be updated and match the latest change").that( difference<1000).isTrue();

    }

    @Test
    public void testUpdatedRestPeriodAfterPreferenceChange(){
        onView(withId(R.id.timer_layout)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.swipe_area_text)).perform(swipeRight());
        long periodEndTimeValue = mActivityRule.getActivity().periodEndTimeValue;
        //Since we are changing rest period lenght to 40 mins, we need to add 25 mins to the expected time, because the original rest period length is set to 15 mins
        long expectedPeriodEndTime1 = periodEndTimeValue + 25*60*1000;

        openPreferences();
        intended(hasComponent(PreferenceXActivity.class.getName()));
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title))).perform(click());

        changePeriodLengthPreference(RReminder.REST, 0,40);

        Espresso.pressBack();
        Espresso.pressBack();

        long actualEndTimeValue1 = mActivityRule.getActivity().periodEndTimeValue;
        long difference = Math.abs(actualEndTimeValue1-expectedPeriodEndTime1);
        Log.d("PREFERENCE_TEST", "difference: "+difference);
        assertWithMessage("after changing rest length preference, the service should be updated").that( difference<1000).isTrue();

        expectedPeriodEndTime1 = actualEndTimeValue1 - 10*60*1000;
        openPreferences();
        intended(hasComponent(PreferenceXActivity.class.getName()), times(2));

        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title))).perform(click());

        changePeriodLengthPreference(RReminder.REST,0,30);

        Espresso.pressBack();
        Espresso.pressBack();

        actualEndTimeValue1 = mActivityRule.getActivity().periodEndTimeValue;
        difference = Math.abs(actualEndTimeValue1-expectedPeriodEndTime1);
        Log.d("PREFERENCE_TEST", "difference: "+difference);
        assertWithMessage("after changing rest length preference 2nd time, the service should be updated").that( difference<1000).isTrue();
    }

    @Test
    public void testUpdatedSummary(){

        openPreferences();
        intended(hasComponent(PreferenceXActivity.class.getName()));
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_title) + ": automatic")).perform(click());
        onView(withText("Manual")).perform(click());
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title))).perform(click());

        changePeriodLengthPreference(RReminder.WORK,1,30);

        changePeriodLengthPreference(RReminder.REST, 2,45);

        Espresso.pressBack();

        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_period_extend_title))).perform(click());
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_title))).perform(click());
        onView(withId(R.id.number_preference_picker)).perform(CustomActions.setValue(2));
        onView(withText("OK")).perform(click());
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_title))).perform(click());
        onView(withId(R.id.number_preference_picker)).perform(CustomActions.setValue(17));
        onView(withText("OK")).perform(click());


        Espresso.pressBack();
        Espresso.pressBack();

        expectedModeSummary = RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_summary_manual);
        expectedWorkLengthSummary = RReminder.getFormatedValue(getApplicationContext(), 0, preferences.getString(workPeriodLengthKey, RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.default_work_length_string)));
        expectedRestLengthSummary = RReminder.getFormatedValue(getApplicationContext(), 0, preferences.getString(restPeriodLengthKey, RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.default_rest_length_string)));

        String temp = preferences.getString(workPeriodSoundKey, "DEFAULT_RINGTONE_URI");

        if (temp.equals("DEFAULT_RINGTONE_URI")){
            originalWorkUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {
            originalWorkUri = Uri.parse(temp);
        }

        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), originalWorkUri);
        expectedWorkAudioSummary = ringtone.getTitle(getApplicationContext());

        temp = preferences.getString(restPeriodSoundKey, "DEFAULT_RINGTONE_URI");

        if (temp.equals("DEFAULT_RINGTONE_URI")){
            originalRestUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {
            originalRestUri = Uri.parse(temp);
        }

        ringtone = RingtoneManager.getRingtone(getApplicationContext(), originalRestUri);
        expectedRestAudioSummary = ringtone.getTitle(getApplicationContext());


        Integer value = preferences.getInt(extendCountKey, RReminder.DEFAULT_EXTEND_COUNT);
        if(value==1){
            expectedExtendCountSummary= RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_options_single);
        } else {
            expectedExtendCountSummary= getApplicationContext().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_options_multiple, value);
        }

        value = preferences.getInt(extendBaseLengthKey, RReminder.DEFAULT_EXTEND_BASE_LENGTH);
        if(value==1){
            expectedExtendBaseLengthSummary= RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_minute_single);
        } else {
            expectedExtendBaseLengthSummary = getApplicationContext().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_minute_multiple, value);
        }


        assertWithMessage("after updating reminder mode preference the summary should be updated").that(expectedModeSummary).isEqualTo(actualModeSummary);
        assertWithMessage("after updating work period length preference the summary should be updated").that(expectedWorkLengthSummary).isEqualTo(actualWorkLengthSummary);
        assertWithMessage("after updating rest period length preference the summary should be updated").that(expectedRestLengthSummary).isEqualTo(actualRestLengthSummary);
        //assertEquals("after updating work period ringtone preference the summary should be updated", expectedWorkAudioSummary, actualWorkAudioSummary);
        //assertEquals("after updating rest period ringtone preference the summary should be updated", expectedRestAudioSummary, actualRestAudioSummary);
        assertWithMessage("after updating extend options count preference the summary should be updated").that(expectedExtendCountSummary).isEqualTo(actualExtendCountSummary);
        assertWithMessage("after updating extend lenght preference the summary should be updated").that(expectedExtendBaseLengthSummary).isEqualTo(actualExtendLenghtSummary);
    }

    @Test
    public void testEnablingFunctionality(){
        onView(withId(R.id.timer_layout)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.button_period_end_extend)).check(matches(isDisplayed()));
        onView(withId(R.id.swipe_area_text)).check(matches(isDisplayed()));
        openPreferences();
        intended(hasComponent(PreferenceXActivity.class.getName()));
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_end_period_title))).perform(click());
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_title))).perform(click());
        Espresso.pressBack();
        onView(withId(R.id.button_period_end_extend)).check(matches(not(isDisplayed())));
        onView(withId(R.id.swipe_area_text)).check(matches(not(isDisplayed())));
        openPreferences();
        intended(hasComponent(PreferenceXActivity.class.getName()), times(2));
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_end_period_title))).perform(click());
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_title))).perform(click());
        Espresso.pressBack();
        onView(withId(R.id.button_period_end_extend)).check(matches(isDisplayed()));
        onView(withId(R.id.swipe_area_text)).check(matches(isDisplayed()));

    }

    @Test
    public void testExtendOptionsChanged(){
        editor.putBoolean(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_key), "00:05");
        editor.commit();

        onView(withId(R.id.timer_layout)).perform(click());


        String extendButtonOneOption,optionOneButton, optionTwoButton, optionThreeButton;
        extendButtonOneOption = String.format(RReminderTest.getResourceString(R.string.extend_period_one_option),extendBaseLength);
        optionOneButton =String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength);
        optionTwoButton = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength*2);
        optionThreeButton =String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength*3);
        onView(withId(R.id.button_period_end_extend)).check(matches(withText(extendButtonOneOption)));
        openPreferences();
        intended(hasComponent(PreferenceXActivity.class.getName()));
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_period_extend_title))).perform(click());
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_title))).perform(click());
        onView(withId(R.id.number_preference_picker)).perform(CustomActions.setValue(3));
        onView(withText("OK")).perform(click());
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_title))).perform(click());
        onView(withId(R.id.number_preference_picker)).perform(CustomActions.setValue(15));
        onView(withText("OK")).perform(click());
        Espresso.pressBack();
        Espresso.pressBack();

        extendBaseLength = preferences.getInt(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key), 5);
        optionOneButton = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength);
        optionTwoButton = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength*2);
        optionThreeButton = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength*3);
        onView(withId(R.id.button_period_end_extend)).perform(click());
        onView(withId(R.id.extend_dialog_button_extend)).check(matches(withText(optionOneButton)));
        onView(withId(R.id.extend_dialog_button_extend1)).check(matches(withText(optionTwoButton)));
        onView(withId(R.id.extend_dialog_button_extend2)).check(matches(withText(optionThreeButton)));

    }

    @Test
    public void testLedPreference(){
        boolean ledActiveStatus = preferences.getBoolean(getApplicationContext().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_show_led_key), true);
        assertWithMessage("default value of LED preference should be true").that(ledActiveStatus).isTrue();

        openPreferences();
        intended(hasComponent(PreferenceXActivity.class.getName()));
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_show_led_title))).perform(click());
        Espresso.pressBack();

        ledActiveStatus = preferences.getBoolean(getApplicationContext().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_show_led_key), true);
        assertWithMessage("the changed LED preference should be stored correctly").that(ledActiveStatus).isFalse();

        openPreferences();
        intended(hasComponent(PreferenceXActivity.class.getName()), times(2));
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_show_led_title))).perform(click());
        Espresso.pressBack();

        ledActiveStatus = preferences.getBoolean(getApplicationContext().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_show_led_key), true);
        assertWithMessage("the changed LED preference should be stored correctly").that(ledActiveStatus).isTrue();
    }





    public void openPreferences(){
        try {
            openActionBarOverflowOrOptionsMenu(getApplicationContext());
        } catch (Exception e) {
            Log.e(debug, "no overflow menu");
        }
        onView(anyOf(withText(RReminderTest.getResourceString(R.string.menu_settings)), withId(R.id.menu_settings_x))).perform(click());
    }

    private void timerLongPress(int duration){
        onView(withId(R.id.timer_layout)).perform(TouchActions.pressAndHold());
        onView(isRoot()).perform(CustomActions.waitFor(duration));
        onView(withId(R.id.timer_layout)).perform(TouchActions.release());
        TouchActions.tearDown();
    }

    public void changePeriodLengthPreference(int periodType, int hour, int mins){
        switch (periodType){
            case RReminder.WORK : {
                onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_title))).perform(click());
                break;
            }
            case RReminder.REST : {
                onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_title))).perform(click());
                break;
            }
            case 99 : {
                onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_approx_notification_length_title))).perform(click());
                break;
            }
            default: break;
        }
        onView(withId(R.id.time_preference_first_picker)).perform(CustomActions.setValue(hour));
        onView(withId(R.id.time_preference_second_picker)).perform(CustomActions.setValue(mins));
        onView(withText("OK")).perform(click());

    }


    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(debug, "intent with summaries received");
            if(intent.getAction()!=null){
                switch(intent.getAction()){
                    case RReminder.CUSTOM_INTENT_TEST_PREFERENCES_MODE :
                    {
                        actualModeSummary = intent.getExtras().getString(RReminder.PREFERENCE_MODE_SUMMARY);
                        break;
                    }
                    case RReminder.CUSTOM_INTENT_TEST_PREFERENCES_PERIOD :
                    {
                        actualWorkLengthSummary = intent.getExtras().getString(RReminder.PREFERENCE_WORK_LENGTH_SUMMARY);
                        actualRestLengthSummary = intent.getExtras().getString(RReminder.PREFERENCE_REST_LENGTH_SUMMARY);
                        actualWorkAudioSummary = intent.getExtras().getString(RReminder.PREFERENCE_WORK_AUDIO_SUMMARY);
                        actualRestAudioSummary = intent.getExtras().getString(RReminder.PREFERENCE_REST_AUDIO_SUMMARY);
                        break;
                    }
                    case RReminder.CUSTOM_INTENT_TEST_PREFERENCES_EXTEND :
                    {
                        actualExtendCountSummary = intent.getExtras().getString(RReminder.PREFERENCE_EXTEND_COUNT_SUMMARY);
                        actualExtendLenghtSummary = intent.getExtras().getString(RReminder.PREFERENCE_EXTEND_LENGTH_SUMMARY);
                        break;
                    }
                    default: break;
                }
            }


        }
    }
}
