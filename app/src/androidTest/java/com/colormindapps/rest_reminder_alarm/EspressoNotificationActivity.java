package com.colormindapps.rest_reminder_alarm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.fragment.app.DialogFragment;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.truth.Truth.assertWithMessage;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoNotificationActivity {

    Context appContext;
    SharedPreferences preferences, privatePreferences;
    SharedPreferences.Editor editor, privateEditor;
    Calendar customTime;
    Intent intent;
    int extendOptionCount, extendBaseLength;
    String expectedOnlineWorkTitle, expectedOnlineRestTitle;
    int versionCode;

    String debug = "TEST_NOTIFICATION_ACTIVITY";

    @Rule
    public IntentsTestRule<NotificationActivity> mActivityRule =
            new IntentsTestRule<NotificationActivity>(NotificationActivity.class, false, true){

                @Override
                protected Intent getActivityIntent() {
                    customTime = Calendar.getInstance();
                    customTime.set(Calendar.HOUR_OF_DAY, 23);
                    customTime.set(Calendar.MINUTE, 30);
                    customTime.set(Calendar.SECOND, 0);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.putExtra(RReminder.PERIOD_TYPE, 1);
                    intent.putExtra(RReminder.EXTEND_COUNT, 0);
                    intent.putExtra(RReminder.PLAY_SOUND, false);
                    intent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
                    intent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
                    return intent;
                }
            };


    @Before
    public void setUp(){
        Log.d(debug, "setUp");

        long currentTime = Calendar.getInstance().getTimeInMillis();
        currentTime+=360000L;



        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor   = preferences.edit();

        extendOptionCount = preferences.getInt(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key),1);
        extendBaseLength = preferences.getInt(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key),5);
        expectedOnlineWorkTitle = RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.on_work_period).toUpperCase();
        expectedOnlineRestTitle = RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.on_rest_period).toUpperCase();
        versionCode = preferences.getInt(RReminder.VERSION_KEY, 0);

    }

    @After
    public void tearDown(){
        Log.d(debug, "tearDown");
        RReminderMobile.stopCounterService(getApplicationContext(),1);
        RReminderMobile.stopCounterService(getApplicationContext(),2);
        RReminderMobile.stopCounterService(getApplicationContext(),3);
        RReminderMobile.stopCounterService(getApplicationContext(),4);

        editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();

        editor.putBoolean(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_key), "0");
        editor.putBoolean(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_end_period_key), true);
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), RReminder.DEFAULT_REST_PERIOD_STRING);
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_key), "00:45");
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), "00:15");
        editor.putInt(RReminder.VERSION_KEY, versionCode);
        editor.commit();
    }

    @Test
    public void testExtendWorkSingleDescription(){
        mActivityRule.finishActivity();
        Intent workSingleExtendIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        workSingleExtendIntent = new Intent(Intent.ACTION_VIEW);
        workSingleExtendIntent.putExtra(RReminder.PERIOD_TYPE, 3);
        workSingleExtendIntent.putExtra(RReminder.EXTEND_COUNT, 1);
        workSingleExtendIntent.putExtra(RReminder.PLAY_SOUND, false);
        workSingleExtendIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        workSingleExtendIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        mActivityRule.launchActivity(workSingleExtendIntent);

        int expectedFontSize = RReminder.isTablet(getApplicationContext()) ? 21 :16;
        onView(withId(R.id.notification_extend_description)).check(matches(CustomMatchers.withTextSize(expectedFontSize)));
        int expectedColor = getApplicationContext().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.black);
        onView(withId(R.id.notification_extend_description)).check(matches(CustomMatchers.withTextColor(expectedColor)));
        String expectedText = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.notification_end_extend_description_once),"work");
        onView(withId(R.id.notification_extend_description)).check(matches(withText(expectedText)));

    }

    @Test
    public void testExtendRestSingleDescription(){
        mActivityRule.finishActivity();
        Intent restSingleExtendIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        restSingleExtendIntent = new Intent(Intent.ACTION_VIEW);
        restSingleExtendIntent.putExtra(RReminder.PERIOD_TYPE, 4);
        restSingleExtendIntent.putExtra(RReminder.EXTEND_COUNT, 1);
        restSingleExtendIntent.putExtra(RReminder.PLAY_SOUND, false);
        restSingleExtendIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        restSingleExtendIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        mActivityRule.launchActivity(restSingleExtendIntent);

        String expectedText = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.notification_end_extend_description_once),"rest");
        onView(withId(R.id.notification_extend_description)).check(matches(withText(expectedText)));
    }

    @Test
    public void testExtendWorkMultipleDescription(){
        mActivityRule.finishActivity();
        Intent workMultipleExtendIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        workMultipleExtendIntent = new Intent(Intent.ACTION_VIEW);
        workMultipleExtendIntent.putExtra(RReminder.PERIOD_TYPE, 3);
        workMultipleExtendIntent.putExtra(RReminder.EXTEND_COUNT, 3);
        workMultipleExtendIntent.putExtra(RReminder.PLAY_SOUND, false);
        workMultipleExtendIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        workMultipleExtendIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        mActivityRule.launchActivity(workMultipleExtendIntent);

        String expectedText = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.notification_end_extend_description_multiple),"work",3);
        onView(withId(R.id.notification_extend_description)).check(matches(withText(expectedText)));
    }

    @Test
    public void testExtendRestMultipleDescription(){
        mActivityRule.finishActivity();
        Intent restMultipleExtendIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        restMultipleExtendIntent = new Intent(Intent.ACTION_VIEW);
        restMultipleExtendIntent.putExtra(RReminder.PERIOD_TYPE, 4);
        restMultipleExtendIntent.putExtra(RReminder.EXTEND_COUNT, 3);
        restMultipleExtendIntent.putExtra(RReminder.PLAY_SOUND, false);
        restMultipleExtendIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        restMultipleExtendIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        mActivityRule.launchActivity(restMultipleExtendIntent);


        String expectedText = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.notification_end_extend_description_multiple),"rest",3);
        onView(withId(R.id.notification_extend_description)).check(matches(withText(expectedText)));
    }

    @Test
    public void testDescription(){
        int expectedFontSize;
        if(RReminder.isTablet(getApplicationContext())){
            expectedFontSize = 28;
        } else {
            expectedFontSize = RReminder.isPortrait(getApplicationContext()) ? 20:18;
        }
        onView(withId(R.id.notification_description)).check(matches(CustomMatchers.withTextSize(expectedFontSize)));
        int expectedColor = getApplicationContext().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.black);
        onView(withId(R.id.notification_description)).check(matches(CustomMatchers.withTextColor(expectedColor)));

        String expectedText = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.next_period_end_description),"rest","23:30");
        onView(withId(R.id.notification_description)).check(matches(withText(expectedText)));
        //test description content in manual mode
        onView(withId(R.id.notification_description)).check(matches(CustomMatchers.withEnoughSpace()));

        mActivityRule.finishActivity();
        //setting up the test for rest period end
        Intent restIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        restIntent = new Intent(Intent.ACTION_VIEW);
        restIntent.putExtra(RReminder.PERIOD_TYPE, 2);
        restIntent.putExtra(RReminder.EXTEND_COUNT, 0);
        restIntent.putExtra(RReminder.PLAY_SOUND, false);
        restIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        restIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        mActivityRule.launchActivity(restIntent);

        onView(withId(R.id.notification_description)).check(matches(CustomMatchers.withTextSize(expectedFontSize)));
        onView(withId(R.id.notification_description)).check(matches(CustomMatchers.withTextColor(expectedColor)));

        expectedText = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.next_period_end_description), "work","23:30");
        onView(withId(R.id.notification_description)).check(matches(withText(expectedText)));
        onView(withId(R.id.notification_description)).check(matches(CustomMatchers.withEnoughSpace()));

    }

    @Test
    public void testNotificationManualMode(){
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_key), "1");
        editor.commit();
        mActivityRule.finishActivity();

        Intent workIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        workIntent = new Intent(Intent.ACTION_VIEW);
        workIntent.putExtra(RReminder.PERIOD_TYPE, 1);
        workIntent.putExtra(RReminder.EXTEND_COUNT, 0);
        workIntent.putExtra(RReminder.PLAY_SOUND, false);
        workIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        workIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        mActivityRule.launchActivity(workIntent);

        String expectedText = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.notification_end_manual_title), "rest");
        onView(withId(R.id.notification_description)).check(matches(withText(expectedText)));
        onView(withId(R.id.notification_description)).check(matches(CustomMatchers.withEnoughSpace()));

        mActivityRule.finishActivity();
        Intent restIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        restIntent = new Intent(Intent.ACTION_VIEW);
        restIntent.putExtra(RReminder.PERIOD_TYPE, 2);
        restIntent.putExtra(RReminder.EXTEND_COUNT, 0);
        restIntent.putExtra(RReminder.PLAY_SOUND, false);
        restIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        restIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        mActivityRule.launchActivity(restIntent);

        expectedText = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.notification_end_manual_title), "work");
        onView(withId(R.id.notification_description)).check(matches(withText(expectedText)));
        onView(withId(R.id.notification_description)).check(matches(CustomMatchers.withEnoughSpace()));

    }

    @Test
    public void testWorkPeriod3rdExtend(){
        mActivityRule.finishActivity();
        Intent workMultipleExtendIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        workMultipleExtendIntent = new Intent(Intent.ACTION_VIEW);
        workMultipleExtendIntent.putExtra(RReminder.PERIOD_TYPE, 3);
        workMultipleExtendIntent.putExtra(RReminder.EXTEND_COUNT, 3);
        workMultipleExtendIntent.putExtra(RReminder.PLAY_SOUND, false);
        workMultipleExtendIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        workMultipleExtendIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);

        mActivityRule.launchActivity(workMultipleExtendIntent);
        onView(withId(R.id.button_notification_period_end_extend)).perform(click());




        String optionOneButton;
        optionOneButton = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength);
        onView(withText(optionOneButton)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        String expectedTitle = RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.on_work_period).toUpperCase();;
        onView(withId(R.id.period_title)).check(matches(withText(expectedTitle)));

        String expectedDescription = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.description_extended),4);
        onView(withId(R.id.description_text)).check(matches(withText(expectedDescription)));

        int expectedBgColor = getApplicationContext().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.red);
        onView(withId(R.id.mainActivityLayout)).check(matches(CustomMatchers.withBackgroundColor(expectedBgColor)));


    }

    @Test
    public void testRestPeriod3rdExtend(){
        mActivityRule.finishActivity();
        Intent workMultipleExtendIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        workMultipleExtendIntent = new Intent(Intent.ACTION_VIEW);
        workMultipleExtendIntent.putExtra(RReminder.PERIOD_TYPE, 4);
        workMultipleExtendIntent.putExtra(RReminder.EXTEND_COUNT, 3);
        workMultipleExtendIntent.putExtra(RReminder.PLAY_SOUND, false);
        workMultipleExtendIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        workMultipleExtendIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);

        mActivityRule.launchActivity(workMultipleExtendIntent);

        onView(withId(R.id.button_notification_period_end_extend)).perform(click());


        String optionOneButton;
        optionOneButton = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength);
        onView(withText(optionOneButton)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));
        onView(isRoot()).perform(CustomActions.waitFor(1000));

        String expectedTitle = RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.on_rest_period).toUpperCase();;
        onView(withId(R.id.period_title)).check(matches(withText(expectedTitle)));
        String expectedDescription = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.description_extended),4);
        onView(withId(R.id.description_text)).check(matches(withText(expectedDescription)));
        int expectedBgColor = getApplicationContext().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.red);
        onView(withId(R.id.mainActivityLayout)).check(matches(CustomMatchers.withBackgroundColor(expectedBgColor)));

    }

    @Test
    public void testExtendButton(){
        String expectedButtonText = RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_current_period);
        onView(withId(R.id.button_notification_period_end_extend)).check(matches(withText(expectedButtonText)));
        int expectedButtonTextSize = RReminder.isTablet(getApplicationContext()) ? 28 :20;
        onView(withId(R.id.button_notification_period_end_extend)).check(matches(CustomMatchers.withTextSize(expectedButtonTextSize)));
        int expectedColor = getApplicationContext().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.black);
        onView(withId(R.id.button_notification_period_end_extend)).check(matches(CustomMatchers.withTextColor(expectedColor)));


        onView(withId(R.id.button_notification_period_end_extend)).perform(click());

        DialogFragment extendDialog = (DialogFragment)mActivityRule.getActivity().getSupportFragmentManager().findFragmentByTag("extendDialog");

        //check if cliking hintbutton opens a hintdialog


        assertWithMessage("extenddialog is an instance of DialogFragment").that( extendDialog!=null).isTrue();
        assertWithMessage("extenddialog is visible").that( extendDialog.getShowsDialog()).isTrue();


        String optionOneButton, optionTwoButton, optionThreeButton;
        optionOneButton = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength);
        optionTwoButton = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength*2);
        optionThreeButton = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength*3);
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

        onView(withText("Cancel")).perform(click());

        editor.putInt(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key), 2);
        editor.commit();

        onView(withId(R.id.button_notification_period_end_extend)).perform(click());

        onView(withText(optionTwoButton)).check(matches(isDisplayed()));
        onView(withText(optionThreeButton)).check(doesNotExist());

        onView(withText("Cancel")).perform(click());

        editor.putInt(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key), 3);
        editor.commit();

        onView(withId(R.id.button_notification_period_end_extend)).perform(click());

        onView(withText(optionTwoButton)).check(matches(isDisplayed()));
        onView(withText(optionThreeButton)).check(matches(isDisplayed()));

        onView(withText("Cancel")).perform(click());

        editor.putInt(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key), 1);
        editor.commit();

    }

    @Test
    public void testExtendingWorkPeriod(){
        onView(withId(R.id.button_notification_period_end_extend)).perform(click());



        String optionOneButton;
        optionOneButton = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength);

        onView(withText(optionOneButton)).perform(click());


        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        String expectedTitle = RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.on_work_period).toUpperCase();;
        onView(withId(R.id.period_title)).check(matches(withText(expectedTitle)));
        String expectedDescription = RReminderTest.getResourceString(R.string.description_extended_one_time);
        onView(withId(R.id.description_text)).check(matches(withText(expectedDescription)));
    }

    @Test
    public void testExtendingRestPeriod(){
        mActivityRule.finishActivity();
        Intent restIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        restIntent = new Intent(Intent.ACTION_VIEW);
        restIntent.putExtra(RReminder.PERIOD_TYPE, 2);
        restIntent.putExtra(RReminder.EXTEND_COUNT, 0);
        restIntent.putExtra(RReminder.PLAY_SOUND, false);
        restIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        restIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        mActivityRule.launchActivity(restIntent);

        String optionOneButton;
        optionOneButton = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength);
        onView(withId(R.id.button_notification_period_end_extend)).perform(click());
        onView(withText(optionOneButton)).perform(click());

        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        String expectedTitle = RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.on_rest_period).toUpperCase();;
        onView(withId(R.id.period_title)).check(matches(withText(expectedTitle)));
        String expectedDescription = RReminderTest.getResourceString(R.string.description_extended_one_time);
        onView(withId(R.id.description_text)).check(matches(withText(expectedDescription)));
    }

    @Test
    public void testMainActivityRedColorBackground(){
        mActivityRule.finishActivity();
        Intent redIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        redIntent = new Intent(Intent.ACTION_VIEW);
        redIntent.putExtra(RReminder.PERIOD_TYPE, 3);
        redIntent.putExtra(RReminder.EXTEND_COUNT, 3);
        redIntent.putExtra(RReminder.PLAY_SOUND, false);
        redIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        redIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        mActivityRule.launchActivity(redIntent);

        onView(withId(R.id.button_notification_period_end_extend)).perform(click());
        String optionOneButton;
        optionOneButton = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength);
        onView(withText(optionOneButton)).perform(click());


        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));
        onView(isRoot()).perform(CustomActions.waitFor(1000));

        String expectedTitle = RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.on_work_period).toUpperCase();;
        onView(withId(R.id.period_title)).check(matches(withText(expectedTitle)));
        String expectedDescription = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.description_extended),4);
        onView(withId(R.id.description_text)).check(matches(withText(expectedDescription)));

        int expectedBgColor = getApplicationContext().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.red);
        onView(withId(R.id.mainActivityLayout)).check(matches(CustomMatchers.withBackgroundColor(expectedBgColor)));
    }

    @Test
    public void testExtendDialogOrientationChange(){


        editor.putBoolean(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.commit();
        onView(withId(R.id.button_notification_period_end_extend)).perform(click());
        String optionOneButton = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength);
        onView(withText(optionOneButton)).check(matches(isDisplayed()));


        if(RReminder.isPortrait(getApplicationContext())){
            mActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            mActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        onView(withText(optionOneButton)).check(matches(isDisplayed()));
        //close dialog with press of a button
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_close_dialog_text))).perform(click());
        onView(withText(optionOneButton)).check(doesNotExist());

    }

    @Test
    public void testCloseButtonManual(){
        editor.putString(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_key), "1");
        editor.commit();

        mActivityRule.finishActivity();

        Intent workIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        workIntent = new Intent(Intent.ACTION_VIEW);
        workIntent.putExtra(RReminder.PERIOD_TYPE, 1);
        workIntent.putExtra(RReminder.EXTEND_COUNT, 0);
        workIntent.putExtra(RReminder.PLAY_SOUND, false);
        workIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        workIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        mActivityRule.launchActivity(workIntent);


        assertWithMessage("in manual mode when notification is visible, service should not be running").that(RReminderMobile.isCounterServiceRunning(getApplicationContext())).isFalse();

        onView(withId(R.id.notification_button)).perform(click());

        assertWithMessage("in manual mode after pressing button to start next period, service should be running").that(RReminderMobile.isCounterServiceRunning(getApplicationContext())).isTrue();

    }

    @Test
    public void testTime24HourFormat(){
        long periodEndTimeMillis = mActivityRule.getActivity().getIntent().getExtras().getLong(RReminder.PERIOD_END_TIME);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(periodEndTimeMillis);
        String periodEndTime = DateFormat.format(RReminder.TIME_FORMAT_24H, calendar.getTime()).toString();

        String expectedTimeString = String.format(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.next_period_end_description),"rest",periodEndTime);
        String timeStringFromFunction = RReminder.getTimeString(getApplicationContext(), periodEndTimeMillis).toString();

        onView(withId(R.id.notification_description)).check(matches(withText(expectedTimeString)));

        assertWithMessage("the format function works as expected").that(timeStringFromFunction).isEqualTo(periodEndTime);
    }







}
