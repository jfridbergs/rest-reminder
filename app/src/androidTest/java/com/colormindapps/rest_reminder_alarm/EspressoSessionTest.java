package com.colormindapps.rest_reminder_alarm;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.colormindapps.rest_reminder_alarm.RReminderTest.getResourceString;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoSessionTest {
    SharedPreferences privatePreferences, preferences;
    SharedPreferences.Editor privateEditor, editor;
    float scaledDensity;
    int extendOptionCount, extendBaseLength, versionCode;
    int cancelType, cancelExtendCount;
    long cancelPeriodEndTimeValue;
    String expectedOfflineTitle, expectedOnlineWorkTitle, expectedOnlineRestTitle;
    int colorWork, colorRest;
    int currentDayOfMonth;
    ActivityScenario<MainActivity> scenario;

    String debug = "MAIN_SCENARIO_TEST";

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule
            = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp(){
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor   = preferences.edit();
        scaledDensity = getApplicationContext().getResources().getDisplayMetrics().scaledDensity;

        expectedOfflineTitle = getResourceString(R.string.reminder_off_title);
        expectedOnlineWorkTitle = getResourceString(R.string.on_work_period).toUpperCase();
        expectedOnlineRestTitle = getResourceString(R.string.on_rest_period).toUpperCase();
        extendOptionCount = 3;
        extendBaseLength = 1;

        colorWork = getApplicationContext().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.work);
        colorRest = getApplicationContext().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.rest);


        editor.putBoolean(getResourceString(R.string.pref_enable_extend_key), true);
        editor.putBoolean(getResourceString(R.string.pref_enable_approx_notification_key), true);
        editor.putBoolean(getResourceString(R.string.pref_end_period_key), true);
        editor.putString(getResourceString(R.string.pref_work_period_length_key), "15:55");
        editor.putString(getResourceString(R.string.pref_rest_period_length_key), "15:55");
        editor.putBoolean(getResourceString(R.string.pref_enable_short_periods_key), false);
        editor.putInt(getResourceString(R.string.pref_period_extend_options_key), 3);
        editor.putInt(getResourceString(R.string.pref_period_extend_length_key), 1);
        editor.commit();

        activityScenarioRule.getScenario().onActivity(MainActivity::deleteDbForTests);
        currentDayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    @After
    public void tearDown(){
        activityScenarioRule.getScenario().onActivity(activity -> {
            setActivityValues(activity.periodType, activity.extendCount, activity.periodEndTimeValue);
        });


        RReminderMobile.cancelCounterAlarm(getApplicationContext(), cancelType, cancelExtendCount, cancelPeriodEndTimeValue);
        RReminderMobile.stopCounterService(getApplicationContext(), cancelType);

        editor.putBoolean(getResourceString(R.string.pref_enable_extend_key), false);
        editor.putString(getResourceString(R.string.pref_mode_key), "0");
        editor.putBoolean(getResourceString(R.string.pref_end_period_key), true);
        editor.putBoolean(getResourceString(R.string.pref_enable_extend_key), true);
        editor.putBoolean(getResourceString(R.string.pref_enable_short_periods_key), false);
        editor.putString(getResourceString(R.string.pref_work_period_length_key), "00:45");
        editor.putString(getResourceString(R.string.pref_rest_period_length_key), "00:15");
        editor.putInt(getResourceString(R.string.pref_period_extend_options_key), 3);
        editor.putInt(getResourceString(R.string.pref_period_extend_length_key), 5);
        editor.commit();
    }

    public void setActivityValues(int type, int extendCount, long periodEndTimeValue){
        cancelType = type;
        cancelExtendCount = extendCount;
        cancelPeriodEndTimeValue = periodEndTimeValue;
    }

    public void openSession(){
       onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
       onView(withId(R.id.menu_session_list)).perform(click());
       onView(allOf(isDescendantOfA(withId(R.id.calendarView)), withText("4"), isDisplayed())).perform(click());
       onView(isRoot()).perform(CustomActions.waitFor(1000));
       onView(withId(R.id.session_date)).check(matches(isDisplayed()));
       onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodCount(2)));
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
        String expectedDateStringFirst = RReminder.getSessionDateString(0,sessionStartFirst);
        String expectedDateStringSecond = RReminder.getSessionDateString(0,sessionStartSecond);
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(withText(expectedOfflineTitle)));

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.menu_session_list)).perform(click());
        onView(allOf(isDescendantOfA(withId(R.id.calendarView)), withText(Integer.toString(currentDayOfMonth)), isDisplayed())).perform(click());

       // onView(withText(expectedTimeStringFirst)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.session_clock), isDisplayed())).check(matches(withText(expectedTimeStringFirst)));
        onView(allOf(withId(R.id.session_date), isDisplayed())).check(matches(withText(expectedDateStringFirst)));
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(allOf(withId(R.id.next), isDisplayed())).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        //onView(withText(expectedTimeStringSecond)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.session_clock), isDisplayed())).check(matches(withText(expectedTimeStringSecond)));
        onView(allOf(withId(R.id.session_date), isDisplayed())).check(matches(withText(expectedDateStringSecond)));

    }

    @Test
    public void testSessionDefaultAutomaticMode(){
        testSessionDefaultBehaviour();
    }

    @Test
    public void testSessionDefaultManualMode(){
        editor.putString(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_key), "1");
        editor.commit();
        testSessionDefaultBehaviour();
    }

    public void testSessionDefaultBehaviour(){
        long sessionStart = Calendar.getInstance().getTimeInMillis();
        int expectedCount = 7;
        long[] endTime  = runSession(true);
        long sessionEnd = Calendar.getInstance().getTimeInMillis();
        String startString = RReminder.getTimeString(getApplicationContext(), sessionStart).toString();
        String endString = RReminder.getTimeString(getApplicationContext(), sessionEnd).toString();
        String expectedTimeString = startString +" - "+ endString;
        String firstIntermediateEndTime = RReminder.getTimeString(getApplicationContext(), endTime[0]).toString();
        String secondIntermediateEndTime = RReminder.getTimeString(getApplicationContext(), endTime[1]).toString();
        String thirdIntermediateEndTime = RReminder.getTimeString(getApplicationContext(), endTime[2]).toString();
        float expectedWorkLength = 80*1000;
        //subtract 6 seconds when turning off delay
        long sessionLength = endTime[6]-sessionStart;
        Log.d(debug, "session length: "+sessionLength/1000);
        //vajadzēja būt 110, bet ir 116. PIeskaitīts klāt pēdējais periods?
        float expectedWorkPercent = expectedWorkLength/sessionLength*100;

        onView(withId(R.id.period_title)).check(matches(withText(expectedOfflineTitle)));

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.menu_session_list)).perform(click());
        onView(allOf(isDescendantOfA(withId(R.id.calendarView)), withText(Integer.toString(currentDayOfMonth)), isDisplayed())).perform(click());

        onView(allOf(withId(R.id.session_clock), isDisplayed())).check(matches(withText(expectedTimeString)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodCount(expectedCount)));

        //check the first and last periods for their end time values
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(0,endTime[0]-sessionStart)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(0,RReminder.WORK)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(6,endTime[6]-endTime[5])));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(6,RReminder.WORK)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(1,endTime[1]-endTime[0])));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(1,RReminder.REST)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(5,endTime[5]-endTime[4])));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(5,RReminder.REST)));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPercent(0,expectedWorkPercent)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasTotalLength(1,30*1000)));

    }

    @Test
    public void testSessionSwipePeriodEnd(){
        long[] endTimes = new long[3];
        long sessionStart = Calendar.getInstance().getTimeInMillis();
        int expectedCount = 7;
        long firstSwipeAfter = 5000;
        long secondSwipeAfter = 2000;

        onView(withId(R.id.timer_layout)).perform(click());
        //run full length work period
        runFullWorkPeriod(true);
        endTimes[0] = Calendar.getInstance().getTimeInMillis()-500;
        //run full length rest period
        runFullRestPeriod(false);
        //after 5 seconds swipe work period end
        onView(isRoot()).perform(CustomActions.waitFor(firstSwipeAfter));
        if(RReminder.isPortrait(getApplicationContext())){
            onView(withId(R.id.swipe_area_text)).perform(swipeRight());
        } else {
            onView(withId(R.id.swipe_area_text)).perform(swipeDown());
        }
        endTimes[1] = Calendar.getInstance().getTimeInMillis();
        //run full length rest period
        runFullRestPeriod(true);
        //run full
        runFullWorkPeriod(false);
        //after 2 seconds swipe rest period
        onView(isRoot()).perform(CustomActions.waitFor(secondSwipeAfter));
        if(RReminder.isPortrait(getApplicationContext())){
            onView(withId(R.id.swipe_area_text)).perform(swipeRight());
        } else {
            onView(withId(R.id.swipe_area_text)).perform(swipeDown());
        }
        endTimes[2] = Calendar.getInstance().getTimeInMillis();
        //run full work period
        runFullWorkPeriod(true);
        timerLongPress(2000);
        long sessionEnd = Calendar.getInstance().getTimeInMillis()-2000;

        String startString = RReminder.getTimeString(getApplicationContext(), sessionStart).toString();
        String endString = RReminder.getTimeString(getApplicationContext(), sessionEnd).toString();
        String expectedTimeString = startString +" - "+ endString;
        String secondIntermediateEndTime = RReminder.getTimeString(getApplicationContext(), endTimes[1]).toString();
        String thirdIntermediateEndTime = RReminder.getTimeString(getApplicationContext(), endTimes[2]).toString();
        String forcedToEndString = "forced to end";

        onView(withId(R.id.period_title)).check(matches(withText(expectedOfflineTitle)));

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.menu_session_list)).perform(click());
        onView(allOf(isDescendantOfA(withId(R.id.calendarView)), withText(Integer.toString(currentDayOfMonth)), isDisplayed())).perform(click());

        //onView(allOf(withId(R.id.session_clock), isDisplayed())).check(matches(withText(expectedTimeString)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodCount(expectedCount)));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(0,endTimes[0]-sessionStart)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(0,RReminder.WORK)));
        onView(withId(R.id.pie_view)).check(matches(not(CustomMatchers.hasEnded(0))));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(2,firstSwipeAfter)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(2,RReminder.WORK)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasEnded(2)));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodStart(3,endTimes[1])));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(3,RReminder.REST)));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(5,secondSwipeAfter)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(5,RReminder.REST)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasEnded(5)));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodStart(6,endTimes[2])));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(6,RReminder.WORK)));
        /*






        onView(withId(R.id.recyclerview_periods))
                .perform(scrollToPosition(5))
                .check(matches(atPosition(5, hasDescendant(withText(containsString(thirdIntermediateEndTime))))));
        onView(withId(R.id.recyclerview_periods))
                .check(matches(atPosition(5, hasDescendant(withText(containsString(forcedToEndString))))));
        onView(withId(R.id.recyclerview_periods))
                .check(matches(atPosition(5, withBackgroundColor(colorRest))));

        onView(withId(R.id.recyclerview_periods))
                .perform(scrollToPosition(7))
                .check(matches(atPosition(7, hasDescendant(withText(containsString(endString))))));
        onView(withId(R.id.recyclerview_periods))
                .check(matches(atPosition(7, withBackgroundColor(colorRest))));

         */
    }



    @Test
    public void testSessionExtendedOneOption(){
        editor.putInt(getResourceString(R.string.pref_period_extend_options_key), 1);
        editor.commit();
        long[] endTimes = new long[3];
        long sessionStart = Calendar.getInstance().getTimeInMillis();
        int expectedCount = 4;
        String extendString = "ext: ";

        onView(withId(R.id.timer_layout)).perform(click());
        //after 5 seconds extend current work period
        onView(isRoot()).perform(CustomActions.waitFor(5000));
        onView(withId(R.id.button_period_end_extend)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(35000));
        timerLongPress(500);
        onView(isRoot()).perform(CustomActions.waitFor(40000));
        onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_button)).perform(click());
        endTimes[0] = Calendar.getInstance().getTimeInMillis();
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));

        runFullRestPeriod(false);
        endTimes[1] = Calendar.getInstance().getTimeInMillis()-1000;
        runFullWorkPeriod(false);
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        onView(withId(R.id.button_period_end_extend)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(38000));
        onView(withId(R.id.button_period_end_extend)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(45000));
        timerLongPress(500);
        onView(isRoot()).perform(CustomActions.waitFor(45000));
        onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_button)).perform(click());
        endTimes[2] = Calendar.getInstance().getTimeInMillis()-1000;
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));
        timerLongPress(2000);
        long sessionEnd = Calendar.getInstance().getTimeInMillis()-2000;



        String startString = RReminder.getTimeString(getApplicationContext(), sessionStart).toString();
        String endString = RReminder.getTimeString(getApplicationContext(), sessionEnd).toString();
        String expectedTimeString = startString +" - "+ endString;
        String firstIntermediateEndTime = RReminder.getTimeString(getApplicationContext(), endTimes[0]).toString();
        String secondIntermediateEndTime = RReminder.getTimeString(getApplicationContext(), endTimes[1]).toString();
        String thirdIntermediateEndTime = RReminder.getTimeString(getApplicationContext(), endTimes[2]).toString();
        onView(withId(R.id.period_title)).check(matches(withText(expectedOfflineTitle)));

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.menu_session_list)).perform(click());
        onView(allOf(isDescendantOfA(withId(R.id.calendarView)), withText(Integer.toString(currentDayOfMonth)), isDisplayed())).perform(click());

        onView(allOf(withId(R.id.session_clock), isDisplayed())).check(matches(withText(expectedTimeString)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodCount(expectedCount)));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(0,80*1000)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(0,RReminder.WORK_EXTENDED)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendCount(0,1)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendLength(0,60*1000)));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(1,10000)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(1,RReminder.REST)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendCount(1,0)));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(3,130*1000)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(3,RReminder.REST_EXTENDED)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendCount(3,2)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendLength(3,120*1000)));

    }

    @Test
    public void testSessionExtendedMultipleOptions(){
        long[] endTimes = new long[3];
        long sessionStart = Calendar.getInstance().getTimeInMillis();
        int expectedCount = 4;
        String extendString = "ext: ";

        onView(withId(R.id.timer_layout)).perform(click());
        //after 5 seconds extend current work period
        onView(isRoot()).perform(CustomActions.waitFor(5000));
        onView(withId(R.id.button_period_end_extend)).perform(click());
        onView(withId(R.id.extend_dialog_button_extend)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(35000));
        onView(withId(R.id.button_period_end_extend)).perform(click());
        onView(withId(R.id.extend_dialog_button_extend)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(50000));
        timerLongPress(500);
        onView(isRoot()).perform(CustomActions.waitFor(50000));
        onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_button)).perform(click());
        endTimes[0] = Calendar.getInstance().getTimeInMillis();
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));

        runFullRestPeriod(false);
        endTimes[1] = Calendar.getInstance().getTimeInMillis()-1000;
        runFullWorkPeriod(false);
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        onView(withId(R.id.button_period_end_extend)).perform(click());
        onView(withId(R.id.extend_dialog_button_extend1)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(38000));
        timerLongPress(500);
        onView(isRoot()).perform(CustomActions.waitFor(45000));
        timerLongPress(500);
        onView(isRoot()).perform(CustomActions.waitFor(45000));
        onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_button)).perform(click());
        endTimes[2] = Calendar.getInstance().getTimeInMillis()-1000;
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));
        timerLongPress(2000);

        long sessionEnd = Calendar.getInstance().getTimeInMillis()-2000;

        String startString = RReminder.getTimeString(getApplicationContext(), sessionStart).toString();
        String endString = RReminder.getTimeString(getApplicationContext(), sessionEnd).toString();
        String expectedTimeString = startString +" - "+ endString;
        String firstIntermediateEndTime = RReminder.getTimeString(getApplicationContext(), endTimes[0]).toString();
        String secondIntermediateEndTime = RReminder.getTimeString(getApplicationContext(), endTimes[1]).toString();
        String thirdIntermediateEndTime = RReminder.getTimeString(getApplicationContext(), endTimes[2]).toString();

        onView(withId(R.id.period_title)).check(matches(withText(expectedOfflineTitle)));

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.menu_session_list)).perform(click());
        onView(allOf(isDescendantOfA(withId(R.id.calendarView)), withText(Integer.toString(currentDayOfMonth)), isDisplayed())).perform(click());

        onView(allOf(withId(R.id.session_clock), isDisplayed())).check(matches(withText(expectedTimeString)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodCount(expectedCount)));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(0,140*1000)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(0,RReminder.WORK_EXTENDED)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendCount(0,2)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendLength(0,120*1000)));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(1,10000)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(1,RReminder.REST)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendCount(1,0)));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(2,20000)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(2,RReminder.WORK)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendCount(2,0)));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(3,130*1000)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(3,RReminder.REST_EXTENDED)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendCount(3,1)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendLength(3,120*1000)));

    }

    @Test
    public void testSessionExtendFromNotificationAct(){
        long[] endTimes = new long[3];
        long sessionStart = Calendar.getInstance().getTimeInMillis();
        int expectedCount = 3;
        String extendString = "ext: ";

        onView(withId(R.id.timer_layout)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(20000));
        onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
        onView(withId(R.id.button_notification_period_end_extend)).perform(click());
        onView(withId(R.id.extend_dialog_button_extend)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));
        onView(isRoot()).perform(CustomActions.waitFor(30000));
        timerLongPress(500);
        onView(isRoot()).perform(CustomActions.waitFor(28000));
        endTimes[0] = Calendar.getInstance().getTimeInMillis();
        onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_button)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));

        onView(isRoot()).perform(CustomActions.waitFor(10000));
        onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
        onView(withId(R.id.button_notification_period_end_extend)).perform(click());
        onView(withId(R.id.extend_dialog_button_extend)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));
        onView(isRoot()).perform(CustomActions.waitFor(30000));
        timerLongPress(500);
        onView(isRoot()).perform(CustomActions.waitFor(30000));
        onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
        onView(withId(R.id.button_notification_period_end_extend)).perform(click());
        onView(withId(R.id.extend_dialog_button_extend)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));
        onView(isRoot()).perform(CustomActions.waitFor(30000));
        timerLongPress(500);
        onView(isRoot()).perform(CustomActions.waitFor(28500));
        endTimes[1] = Calendar.getInstance().getTimeInMillis();
        onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_button)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(500));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));
        runFullWorkPeriod(false);
        timerLongPress(2000);

        long sessionEnd = Calendar.getInstance().getTimeInMillis()-3000;

        String startString = RReminder.getTimeString(getApplicationContext(), sessionStart).toString();
        String endString = RReminder.getTimeString(getApplicationContext(), sessionEnd).toString();
        String expectedTimeString = startString +" - "+ endString;
        String firstIntermediateEndTime = RReminder.getTimeString(getApplicationContext(), endTimes[0]).toString();

        onView(withId(R.id.period_title)).check(matches(withText(expectedOfflineTitle)));

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.menu_session_list)).perform(click());
        onView(allOf(isDescendantOfA(withId(R.id.calendarView)), withText(Integer.toString(currentDayOfMonth)), isDisplayed())).perform(click());

        onView(allOf(withId(R.id.session_clock), isDisplayed())).check(matches(withText(expectedTimeString)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodCount(expectedCount)));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(0,80*1000)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(0,RReminder.WORK_EXTENDED)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendCount(0,1)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendLength(0,60*1000)));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(1,endTimes[1]-endTimes[0])));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodStart(1,endTimes[0])));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(1,RReminder.REST_EXTENDED)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendCount(1,2)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendLength(1,endTimes[1]-endTimes[0]-10*1000)));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(2,20000)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodStart(2,endTimes[1])));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(2,RReminder.WORK)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendCount(2,0)));



    }

    @Test
    public void testSessionPeriodPreferenceChange(){
        editor.putBoolean(getResourceString(R.string.pref_enable_short_periods_key), true);
        editor.putString(getResourceString(R.string.pref_work_period_length_key), "00:01");
        editor.putString(getResourceString(R.string.pref_rest_period_length_key), "00:02");
        editor.commit();
        int expectedCount = 2;
        long[] endTimes = new long[2];
        String extendString = "ext: ";
        long sessionStart = Calendar.getInstance().getTimeInMillis();
        onView(withId(R.id.timer_layout)).perform(click());
        //after 5 seconds open preferences and set work period length to 1 min
        onView(isRoot()).perform(CustomActions.waitFor(5000));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.menu_settings_x)).perform(click());
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title))).perform(click());
        changePeriodLengthPreference(RReminder.WORK,0,2);
        Espresso.pressBack();
        Espresso.pressBack();
        onView(isRoot()).perform(CustomActions.waitFor(50000));
        timerLongPress(500);
        onView(isRoot()).perform(CustomActions.waitFor(30000));
        timerLongPress(500);
        onView(isRoot()).perform(CustomActions.waitFor(35000));
        onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
        endTimes[0] = Calendar.getInstance().getTimeInMillis();
        onView(withId(R.id.notification_button)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.menu_settings_x)).perform(click());
        onView(withText(RReminderTest.getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_screen_periods_title))).perform(click());
        changePeriodLengthPreference(RReminder.REST,0,1);
        Espresso.pressBack();
        Espresso.pressBack();
        onView(isRoot()).perform(CustomActions.waitFor(53000));
        onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
        endTimes[1] = Calendar.getInstance().getTimeInMillis();
        onView(withId(R.id.notification_button)).perform(click());
        timerLongPress(2000);

        long sessionEnd = Calendar.getInstance().getTimeInMillis()-3000;

        String startString = RReminder.getTimeString(getApplicationContext(), sessionStart).toString();
        String endString = RReminder.getTimeString(getApplicationContext(), sessionEnd).toString();
        String expectedTimeString = startString +" - "+ endString;
        String firstIntermediateEndTime = RReminder.getTimeString(getApplicationContext(), endTimes[0]).toString();
        String secondIntermediateEndTime = RReminder.getTimeString(getApplicationContext(), endTimes[1]).toString();

        onView(withId(R.id.period_title)).check(matches(withText(expectedOfflineTitle)));

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.menu_session_list)).perform(click());
        onView(allOf(isDescendantOfA(withId(R.id.calendarView)), withText(Integer.toString(currentDayOfMonth)), isDisplayed())).perform(click());

        onView(allOf(withId(R.id.session_clock), isDisplayed())).check(matches(withText(expectedTimeString)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodCount(expectedCount)));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(0,120*1000)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(0,RReminder.WORK)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendCount(0,0)));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(1,60*1000)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(1,RReminder.REST)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendCount(1,0)));


    }

    @Test
    public void testExtendThenSwipe(){
        long[] endTimes = new long[3];
        long sessionStart = Calendar.getInstance().getTimeInMillis();
        int expectedCount = 4;
        onView(withId(R.id.timer_layout)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(10000));
        onView(withId(R.id.button_period_end_extend)).perform(click());
        onView(withId(R.id.extend_dialog_button_extend)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(20000));
        if(RReminder.isPortrait(getApplicationContext())){
            onView(withId(R.id.swipe_area_text)).perform(swipeRight());
        } else {
            onView(withId(R.id.swipe_area_text)).perform(swipeDown());
        }
        endTimes[0] = Calendar.getInstance().getTimeInMillis();
        onView(isRoot()).perform(CustomActions.waitFor(11000));
        onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_button)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(500));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));
        onView(withId(R.id.period_title)).check(matches(withText(expectedOnlineWorkTitle)));
        onView(isRoot()).perform(CustomActions.waitFor(20000));
        onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
        onView(withId(R.id.button_notification_period_end_extend)).perform(click());
        onView(withId(R.id.extend_dialog_button_extend)).perform(click());
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));
        onView(isRoot()).perform(CustomActions.waitFor(20000));
        if(RReminder.isPortrait(getApplicationContext())){
            onView(withId(R.id.swipe_area_text)).perform(swipeRight());
        } else {
            onView(withId(R.id.swipe_area_text)).perform(swipeDown());
        }
        endTimes[1] = Calendar.getInstance().getTimeInMillis();
        runFullRestPeriod(true);
        timerLongPress(2000);

        long sessionEnd = Calendar.getInstance().getTimeInMillis()-3000;
        String startString = RReminder.getTimeString(getApplicationContext(), sessionStart).toString();
        String endString = RReminder.getTimeString(getApplicationContext(), sessionEnd).toString();
        String expectedTimeString = startString +" - "+ endString;

        onView(withId(R.id.period_title)).check(matches(withText(expectedOfflineTitle)));

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.menu_session_list)).perform(click());
        onView(allOf(isDescendantOfA(withId(R.id.calendarView)), withText(Integer.toString(currentDayOfMonth)), isDisplayed())).perform(click());

        onView(allOf(withId(R.id.session_clock), isDisplayed())).check(matches(withText(expectedTimeString)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodCount(expectedCount)));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(0,endTimes[0]-sessionStart)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(0,RReminder.WORK_EXTENDED)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendCount(0,1)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendLength(0,endTimes[0]-sessionStart-20000)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasEnded(0)));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(1,10000)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(1,RReminder.REST)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendCount(1,0)));
        onView(withId(R.id.pie_view)).check(matches(not(CustomMatchers.hasEnded(1))));

        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodLength(2,endTimes[1]-endTimes[0]-10000)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasPeriodType(2,RReminder.WORK_EXTENDED)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendCount(2,1)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasExtendLength(2,endTimes[1]-endTimes[0]-10000-20000)));
        onView(withId(R.id.pie_view)).check(matches(CustomMatchers.hasEnded(2)));
        onView(allOf(withId(R.id.session_clock), isDisplayed())).check(matches(withText(expectedTimeString)));
    }

    @Test
    public void testStatsOverallPage(){

        String expectedTitle=RReminderTest.getResourceString(R.string.stats_title_overall);
        String expectedTotalLength = "70 h 50 min";
        int expectedSessionCount = 16;
        String expectedAvgSessionLength = "4 h 26 min";
        int expectedWorkCount = 70;
        int expectedRestCount = 54;
        long expectedWorkLength = 3280*60*1000;
        long expectedRestLength = 970*60*1000;
        int expectedWorkExtendCount = 26;
        int expectedRestExtendCount = 32;
        long expectedWorkExtendLength = 130*60*1000;
        long expectedRestExtendLength = 160*60*1000;

        populateAndOpenStats();

        onView(allOf(withId(R.id.session_date_from), isDisplayed())).check(matches(withText(expectedTitle)));
        onView(allOf(withId(R.id.value_total), isDisplayed())).check(matches(withText(expectedTotalLength)));
        onView(allOf(withId(R.id.value_count), isDisplayed())).check(matches(withText(expectedSessionCount+"")));
        onView(allOf(withId(R.id.value_average), isDisplayed())).check(matches(withText(expectedAvgSessionLength)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalLength(0,expectedWorkLength)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalLength(1,expectedRestLength)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalCount(0,expectedWorkCount)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalCount(1,expectedRestCount)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendCount(0,expectedWorkExtendCount)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendCount(1,expectedRestExtendCount)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendLength(0,expectedWorkExtendLength)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendLength(1,expectedRestExtendLength)));
    }

    @Test
    public void testStatsDailyPage(){
        Calendar day = Calendar.getInstance();
        day.set(2022,5,24,8,0);
        long time = day.getTimeInMillis();
        String expectedTitle=RReminder.getSessionDateString(0,time);
        String expectedTotalLength = "9 h 5 min";
        int expectedSessionCount = 2;
        String expectedAvgSessionLength = "4 h 33 min";
        int expectedWorkCount = 9;
        int expectedRestCount = 7;
        long expectedWorkLength = 420*60*1000;
        long expectedRestLength = 125*60*1000;
        int expectedWorkExtendCount = 3;
        int expectedRestExtendCount = 4;
        long expectedWorkExtendLength = 15*60*1000;
        long expectedRestExtendLength = 20*60*1000;

        day.set(Calendar.DAY_OF_MONTH,17);
        time = day.getTimeInMillis();
        String expectedPreviousTitle=RReminder.getSessionDateString(0,time);
        String expectedPreviousTotalLength = "5 h 0 min";
        int expectedPreviousSessionCount = 1;
        String expectedPreviousAvgSessionLength = "5 h 0 min";
        int expectedPreviousWorkCount = 5;
        int expectedPreviousRestCount = 4;
        long expectedPreviousWorkLength = 230*60*1000;
        long expectedPreviousRestLength = 70*60*1000;
        int expectedPreviousWorkExtendCount = 1;
        int expectedPreviousRestExtendCount = 2;
        long expectedPreviousWorkExtendLength = 5*60*1000;
        long expectedPreviousRestExtendLength = 10*60*1000;

        populateAndOpenStats();
        onView(withId(R.id.intervals_spinner)).perform(click());
        onView(withText("day")).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));

        onView(allOf(withId(R.id.session_date_from), isDisplayed())).check(matches(withText(expectedTitle)));
        onView(allOf(withId(R.id.value_total), isDisplayed())).check(matches(withText(expectedTotalLength)));
        onView(allOf(withId(R.id.value_count), isDisplayed())).check(matches(withText(expectedSessionCount+"")));
        onView(allOf(withId(R.id.value_average), isDisplayed())).check(matches(withText(expectedAvgSessionLength)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalLength(0,expectedWorkLength)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalLength(1,expectedRestLength)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalCount(0,expectedWorkCount)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalCount(1,expectedRestCount)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendCount(0,expectedWorkExtendCount)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendCount(1,expectedRestExtendCount)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendLength(0,expectedWorkExtendLength)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendLength(1,expectedRestExtendLength)));

        onView(withId(R.id.previous)).check(matches(isDisplayed()));
        onView(withId(R.id.previous)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));

        onView(allOf(withId(R.id.session_date_from), isDisplayed())).check(matches(withText(expectedPreviousTitle)));
        onView(allOf(withId(R.id.value_total), isDisplayed())).check(matches(withText(expectedPreviousTotalLength)));
        onView(allOf(withId(R.id.value_count), isDisplayed())).check(matches(withText(expectedPreviousSessionCount+"")));
        onView(allOf(withId(R.id.value_average), isDisplayed())).check(matches(withText(expectedPreviousAvgSessionLength)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalLength(0,expectedPreviousWorkLength)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalLength(1,expectedPreviousRestLength)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalCount(0,expectedPreviousWorkCount)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalCount(1,expectedPreviousRestCount)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendCount(0,expectedPreviousWorkExtendCount)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendCount(1,expectedPreviousRestExtendCount)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendLength(0,expectedPreviousWorkExtendLength)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendLength(1,expectedPreviousRestExtendLength)));
    }

    @Test
    public void testStatsWeeklyPage(){
        Calendar day = Calendar.getInstance();
        day.set(2022,5,20,0,0);
        long intervalStart = day.getTimeInMillis();
        day.set(2022,5,26,23,55);
        long intervalEnd = day.getTimeInMillis();
        String expectedTitle=RReminder.getSessionDateWeekString(intervalStart, intervalEnd);
        String expectedTotalLength = "9 h 5 min";
        int expectedSessionCount = 2;
        String expectedAvgSessionLength = "4 h 33 min";
        int expectedWorkCount = 9;
        int expectedRestCount = 7;
        long expectedWorkLength = 420*60*1000;
        long expectedRestLength = 125*60*1000;
        int expectedWorkExtendCount = 3;
        int expectedRestExtendCount = 4;
        long expectedWorkExtendLength = 15*60*1000;
        long expectedRestExtendLength = 20*60*1000;

        day.set(2022,5,13,0,0);
        intervalStart = day.getTimeInMillis();
        day.set(2022,5,19,23,55);
        intervalEnd = day.getTimeInMillis();

        String expectedPreviousTitle=RReminder.getSessionDateWeekString(intervalStart, intervalEnd);
        String expectedPreviousTotalLength = "10 h 0 min";
        int expectedPreviousSessionCount = 2;
        String expectedPreviousAvgSessionLength = "5 h 0 min";
        int expectedPreviousWorkCount = 10;
        int expectedPreviousRestCount = 8;
        long expectedPreviousWorkLength = 460*60*1000;
        long expectedPreviousRestLength = 140*60*1000;
        int expectedPreviousWorkExtendCount = 2;
        int expectedPreviousRestExtendCount = 4;
        long expectedPreviousWorkExtendLength = 10*60*1000;
        long expectedPreviousRestExtendLength = 20*60*1000;

        day.set(2022,5,6,0,0);
        intervalStart = day.getTimeInMillis();
        day.set(2022,5,12,23,55);
        intervalEnd = day.getTimeInMillis();

        String expectedMinusTwoTitle=RReminder.getSessionDateWeekString(intervalStart, intervalEnd);
        String expectedMinusTwoTotalLength = "12 h 15 min";
        int expectedMinusTwoSessionCount = 3;
        String expectedMinusTwoAvgSessionLength = "4 h 5 min";
        int expectedMinusTwoWorkCount = 12;
        int expectedMinusTwoRestCount = 9;
        long expectedMinusTwoWorkLength = 570*60*1000;
        long expectedMinusTwoRestLength = 165*60*1000;
        int expectedMinusTwoWorkExtendCount = 6;
        int expectedMinusTwoRestExtendCount = 6;
        long expectedMinusTwoWorkExtendLength = 30*60*1000;
        long expectedMinusTwoRestExtendLength = 30*60*1000;

        populateAndOpenStats();
        onView(withId(R.id.intervals_spinner)).perform(click());
        onView(withText("week")).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));

        onView(allOf(withId(R.id.session_date_from), isDisplayed())).check(matches(withText(expectedTitle)));
        onView(allOf(withId(R.id.value_total), isDisplayed())).check(matches(withText(expectedTotalLength)));
        onView(allOf(withId(R.id.value_count), isDisplayed())).check(matches(withText(expectedSessionCount+"")));
        onView(allOf(withId(R.id.value_average), isDisplayed())).check(matches(withText(expectedAvgSessionLength)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalLength(0,expectedWorkLength)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalLength(1,expectedRestLength)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalCount(0,expectedWorkCount)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalCount(1,expectedRestCount)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendCount(0,expectedWorkExtendCount)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendCount(1,expectedRestExtendCount)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendLength(0,expectedWorkExtendLength)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendLength(1,expectedRestExtendLength)));

        onView(withId(R.id.previous)).check(matches(isDisplayed()));
        onView(withId(R.id.previous)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));

        onView(allOf(withId(R.id.session_date_from), isDisplayed())).check(matches(withText(expectedPreviousTitle)));
        onView(allOf(withId(R.id.value_total), isDisplayed())).check(matches(withText(expectedPreviousTotalLength)));
        onView(allOf(withId(R.id.value_count), isDisplayed())).check(matches(withText(expectedPreviousSessionCount+"")));
        onView(allOf(withId(R.id.value_average), isDisplayed())).check(matches(withText(expectedPreviousAvgSessionLength)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalLength(0,expectedPreviousWorkLength)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalLength(1,expectedPreviousRestLength)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalCount(0,expectedPreviousWorkCount)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalCount(1,expectedPreviousRestCount)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendCount(0,expectedPreviousWorkExtendCount)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendCount(1,expectedPreviousRestExtendCount)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendLength(0,expectedPreviousWorkExtendLength)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendLength(1,expectedPreviousRestExtendLength)));

        onView(withId(R.id.previous)).check(matches(isDisplayed()));
        onView(withId(R.id.previous)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));

        onView(allOf(withId(R.id.session_date_from), isDisplayed())).check(matches(withText(expectedMinusTwoTitle)));
        onView(allOf(withId(R.id.value_total), isDisplayed())).check(matches(withText(expectedMinusTwoTotalLength)));
        onView(allOf(withId(R.id.value_count), isDisplayed())).check(matches(withText(expectedMinusTwoSessionCount+"")));
        onView(allOf(withId(R.id.value_average), isDisplayed())).check(matches(withText(expectedMinusTwoAvgSessionLength)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalLength(0,expectedMinusTwoWorkLength)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalLength(1,expectedMinusTwoRestLength)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalCount(0,expectedMinusTwoWorkCount)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalCount(1,expectedMinusTwoRestCount)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendCount(0,expectedMinusTwoWorkExtendCount)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendCount(1,expectedMinusTwoRestExtendCount)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendLength(0,expectedMinusTwoWorkExtendLength)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendLength(1,expectedMinusTwoRestExtendLength)));

    }

    @Test
    public void testStatsWeekPageSpecialCases(){
        Calendar day = Calendar.getInstance();
        day.set(2022,4,30,0,0);
        long intervalStart = day.getTimeInMillis();
        day.set(2022,5,5,23,55);
        long intervalEnd = day.getTimeInMillis();

       String expectedMonthsSpecialTitle= RReminder.getSessionDateWeekString(intervalStart, intervalEnd);

        day.set(2021,11,27,0,0);
        intervalStart = day.getTimeInMillis();
        day.set(2022,0,2,23,55);
        intervalEnd = day.getTimeInMillis();

        String expectedYearsSpecialTitle= RReminder.getSessionDateWeekString(intervalStart, intervalEnd);

        populateAndOpenStats();
        onView(withId(R.id.intervals_spinner)).perform(click());
        onView(withText("week")).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(2000));

        onView(withId(R.id.previous)).check(matches(isDisplayed()));
        onView(withId(R.id.previous)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));

        onView(withId(R.id.previous)).check(matches(isDisplayed()));
        onView(withId(R.id.previous)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));

        onView(withId(R.id.previous)).check(matches(isDisplayed()));
        onView(withId(R.id.previous)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));

        onView(allOf(withId(R.id.session_date_from), isDisplayed())).check(matches(withText(expectedMonthsSpecialTitle)));

        onView(withId(R.id.previous)).check(matches(isDisplayed()));
        onView(withId(R.id.previous)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));

        onView(withId(R.id.previous)).check(matches(isDisplayed()));
        onView(withId(R.id.previous)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));

        onView(allOf(withId(R.id.session_date_from), isDisplayed())).check(matches(withText(expectedYearsSpecialTitle)));
    }

    @Test
    public void testStatsMonthPage(){
        Calendar day = Calendar.getInstance();
        day.set(2022,5,24,8,0);
        long time = day.getTimeInMillis();
        String expectedTitle=RReminder.getSessionDateString(1,time+1000*60*60);
        String expectedTotalLength = "35 h 25 min";
        int expectedSessionCount = 8;
        String expectedAvgSessionLength = "4 h 26 min";
        int expectedWorkCount = 35;
        int expectedRestCount = 27;
        long expectedWorkLength = 1640*60*1000;
        long expectedRestLength = 485*60*1000;
        int expectedWorkExtendCount =13;
        int expectedRestExtendCount = 16;
        long expectedWorkExtendLength = 65*60*1000;
        long expectedRestExtendLength = 80*60*1000;

        day.set(2022,4,24,8,0);
        time = day.getTimeInMillis();
        String expectedPreviousTitle=RReminder.getSessionDateString(1,time+1000*60*60);
        String expectedPreviousTotalLength = "13 h 10 min";
        int expectedPreviousSessionCount = 3;
        String expectedPreviousAvgSessionLength = "4 h 23 min";
        int expectedPreviousWorkCount = 13;
        int expectedPreviousRestCount = 10;
        long expectedPreviousWorkLength = 610*60*1000;
        long expectedPreviousRestLength = 180*60*1000;
        int expectedPreviousWorkExtendCount = 5;
        int expectedPreviousRestExtendCount = 6;
        long expectedPreviousWorkExtendLength = 25*60*1000;
        long expectedPreviousRestExtendLength = 30*60*1000;

        populateAndOpenStats();
        onView(withId(R.id.intervals_spinner)).perform(click());
        onView(withText("month")).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(2000));

        onView(allOf(withId(R.id.session_date_from), isDisplayed())).check(matches(withText(expectedTitle)));
        onView(allOf(withId(R.id.value_total), isDisplayed())).check(matches(withText(expectedTotalLength)));
        onView(allOf(withId(R.id.value_count), isDisplayed())).check(matches(withText(expectedSessionCount+"")));
        onView(allOf(withId(R.id.value_average), isDisplayed())).check(matches(withText(expectedAvgSessionLength)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalLength(0,expectedWorkLength)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalLength(1,expectedRestLength)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalCount(0,expectedWorkCount)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalCount(1,expectedRestCount)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendCount(0,expectedWorkExtendCount)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendCount(1,expectedRestExtendCount)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendLength(0,expectedWorkExtendLength)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendLength(1,expectedRestExtendLength)));

        onView(withId(R.id.previous)).check(matches(isDisplayed()));
        onView(withId(R.id.previous)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));

        onView(allOf(withId(R.id.session_date_from), isDisplayed())).check(matches(withText(expectedPreviousTitle)));
        onView(allOf(withId(R.id.value_total), isDisplayed())).check(matches(withText(expectedPreviousTotalLength)));
        onView(allOf(withId(R.id.value_count), isDisplayed())).check(matches(withText(expectedPreviousSessionCount+"")));
        onView(allOf(withId(R.id.value_average), isDisplayed())).check(matches(withText(expectedPreviousAvgSessionLength)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalLength(0,expectedPreviousWorkLength)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalLength(1,expectedPreviousRestLength)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalCount(0,expectedPreviousWorkCount)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalCount(1,expectedPreviousRestCount)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendCount(0,expectedPreviousWorkExtendCount)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendCount(1,expectedPreviousRestExtendCount)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendLength(0,expectedPreviousWorkExtendLength)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendLength(1,expectedPreviousRestExtendLength)));
    }

    @Test
    public void testStatsYearPage(){
        Calendar day = Calendar.getInstance();
        day.set(2022,5,24,8,0);
        long time = day.getTimeInMillis();
        String expectedTitle=RReminder.getSessionDateString(2,time+1000*60*60);
        String expectedTotalLength = "52 h 40 min";
        int expectedSessionCount = 12;
        String expectedAvgSessionLength = "4 h 23 min";
        int expectedWorkCount = 52;
        int expectedRestCount = 40;
        long expectedWorkLength = 2440*60*1000;
        long expectedRestLength = 720*60*1000;
        int expectedWorkExtendCount =20;
        int expectedRestExtendCount = 24;
        long expectedWorkExtendLength = 100*60*1000;
        long expectedRestExtendLength = 120*60*1000;

        day.set(2021,4,24,8,0);
        time = day.getTimeInMillis();
        String expectedPreviousTitle=RReminder.getSessionDateString(2,time+1000*60*60);
        String expectedPreviousTotalLength = "18 h 10 min";
        int expectedPreviousSessionCount = 4;
        String expectedPreviousAvgSessionLength = "4 h 33 min";
        int expectedPreviousWorkCount = 18;
        int expectedPreviousRestCount = 14;
        long expectedPreviousWorkLength = 840*60*1000;
        long expectedPreviousRestLength = 250*60*1000;
        int expectedPreviousWorkExtendCount = 6;
        int expectedPreviousRestExtendCount = 8;
        long expectedPreviousWorkExtendLength = 30*60*1000;
        long expectedPreviousRestExtendLength = 40*60*1000;

        populateAndOpenStats();
        onView(withId(R.id.intervals_spinner)).perform(click());
        onView(withText("year")).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(2000));

        onView(allOf(withId(R.id.session_date_from), isDisplayed())).check(matches(withText(expectedTitle)));
        onView(allOf(withId(R.id.value_total), isDisplayed())).check(matches(withText(expectedTotalLength)));
        onView(allOf(withId(R.id.value_count), isDisplayed())).check(matches(withText(expectedSessionCount+"")));
        onView(allOf(withId(R.id.value_average), isDisplayed())).check(matches(withText(expectedAvgSessionLength)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalLength(0,expectedWorkLength)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalLength(1,expectedRestLength)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalCount(0,expectedWorkCount)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalCount(1,expectedRestCount)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendCount(0,expectedWorkExtendCount)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendCount(1,expectedRestExtendCount)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendLength(0,expectedWorkExtendLength)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendLength(1,expectedRestExtendLength)));

        onView(withId(R.id.previous)).check(matches(isDisplayed()));
        onView(withId(R.id.previous)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(1000));

        onView(allOf(withId(R.id.session_date_from), isDisplayed())).check(matches(withText(expectedPreviousTitle)));
        onView(allOf(withId(R.id.value_total), isDisplayed())).check(matches(withText(expectedPreviousTotalLength)));
        onView(allOf(withId(R.id.value_count), isDisplayed())).check(matches(withText(expectedPreviousSessionCount+"")));
        onView(allOf(withId(R.id.value_average), isDisplayed())).check(matches(withText(expectedPreviousAvgSessionLength)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalLength(0,expectedPreviousWorkLength)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalLength(1,expectedPreviousRestLength)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalCount(0,expectedPreviousWorkCount)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalCount(1,expectedPreviousRestCount)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendCount(0,expectedPreviousWorkExtendCount)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendCount(1,expectedPreviousRestExtendCount)));

        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendLength(0,expectedPreviousWorkExtendLength)));
        onView(allOf(withId(R.id.column_graph_view), isDisplayed())).check(matches(CustomMatchers.hasColumnTotalExtendLength(1,expectedPreviousRestExtendLength)));
    }

    @Test
    public void testDeleteDbFromPreferences(){
        populateAndOpenStats();
        onView(withId(R.id.value_count)).check(matches(isDisplayed()));
        pressBack();
        openPreferences();
        onView(withText(R.string.pref_delete_recorded_sessions_title)).perform(click());
        onView(withText(R.string.clear_db_dialog_warning)).check(matches(isDisplayed()));
        onView(withText("OK")).perform(click());
        pressBack();
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.menu_open_stats)).perform(click());
        onView(allOf(withId(R.id.no_data), isDisplayed())).check(matches(withText(com.colormindapps.rest_reminder_alarm.R.string.no_sessions_recorded)));
    }



    public long[] runSession(boolean longer){
        long[] endTimes = new long[7];
        onView(withId(R.id.timer_layout)).perform(click());

        runFullWorkPeriod(true);
        endTimes[0] = Calendar.getInstance().getTimeInMillis()-500;

        runFullRestPeriod(false);
        endTimes[1] = Calendar.getInstance().getTimeInMillis()-500;

        runFullWorkPeriod(false);
        endTimes[2] = Calendar.getInstance().getTimeInMillis()-500;
        runFullRestPeriod(false);
        endTimes[3] = Calendar.getInstance().getTimeInMillis()-500;

        if(longer){
            runFullWorkPeriod(false);
            endTimes[4] = Calendar.getInstance().getTimeInMillis()-500;
            runFullRestPeriod(false);
            endTimes[5] = Calendar.getInstance().getTimeInMillis()-500;

            runFullWorkPeriod(false);
            endTimes[6] = Calendar.getInstance().getTimeInMillis()-500;
        }

        timerLongPress(2000);
        return endTimes;
    }

    public void runFullWorkPeriod(boolean longer){
        if (longer){
            onView(isRoot()).perform(CustomActions.waitFor(20000));
        } else {
            onView(isRoot()).perform(CustomActions.waitFor(19000));
        }
        onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_button)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(500));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));
    }

    public void runFullRestPeriod(boolean longer){
        if (longer){
            onView(isRoot()).perform(CustomActions.waitFor(10000));
        } else {
            onView(isRoot()).perform(CustomActions.waitFor(9000));
        }
        onView(withId(R.id.notification_title)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_button)).perform(click());
        onView(isRoot()).perform(CustomActions.waitFor(500));
        onView(withId(R.id.period_title)).check(matches(isDisplayed()));
    }

    private void timerLongPress(int duration){
        onView(withId(R.id.timer_layout)).perform(TouchActions.pressAndHold());
        onView(isRoot()).perform(CustomActions.waitFor(duration));
        onView(withId(R.id.timer_layout)).perform(TouchActions.release());
        TouchActions.tearDown();
    }

    public void openPreferences(){
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.menu_settings_x)).perform(click());
    }

    public void populateAndOpenStats(){
        activityScenarioRule.getScenario().onActivity(MainActivity::populateDbForTests);
        onView(isRoot()).perform(CustomActions.waitFor(3000));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.menu_open_stats)).perform(click());
    }

    public void changePeriodLengthPreference(int periodType, int hour, int mins){
        switch (periodType){
            case RReminder.WORK : {
                onView(withText(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_title))).perform(click());
                break;
            }
            case RReminder.REST : {
                onView(withText(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_title))).perform(click());
                break;
            }
            case 99 : {
                onView(withText(getResourceString(com.colormindapps.rest_reminder_alarm.R.string.pref_approx_notification_length_title))).perform(click());
                break;
            }
            default: break;
        }
        onView(withId(R.id.time_preference_first_picker)).perform(CustomActions.setValue(hour));
        onView(withId(R.id.time_preference_second_picker)).perform(CustomActions.setValue(mins));
        onView(withText("OK")).perform(click());

    }



}
