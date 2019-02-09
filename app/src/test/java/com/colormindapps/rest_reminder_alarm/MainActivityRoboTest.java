package com.colormindapps.rest_reminder_alarm;

import android.app.AlarmManager;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;
import com.google.android.gms.common.ConnectionResult;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.gms.ShadowGooglePlayServicesUtil;
import org.robolectric.util.ActivityController;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MainActivityRoboTest {

    private MainActivity mActivity;
    private ActivityController<MainActivity> controller;
    private TextView activityTitle, swipeArea;
    private String expectedText, actualText;
    private RelativeLayout timerLayout;
    private Button delayButton;
    private long mCalendar;
    private Context context;

    ShadowAlarmManager shadowAlarmManager;
    AlarmManager alarmManager;

    @Before
    public void setup(){
        // force success every time
        ShadowGooglePlayServicesUtil.setIsGooglePlayServicesAvailable(ConnectionResult.SUCCESS);
        alarmManager = (AlarmManager) RuntimeEnvironment.application.getSystemService(Context.ALARM_SERVICE);
        shadowAlarmManager = Shadows.shadowOf(alarmManager);

        context = ShadowApplication.getInstance().getApplicationContext();

        controller = Robolectric.buildActivity(MainActivity.class).create().start().resume().visible();
        mActivity = controller.get();
        expectedText = RuntimeEnvironment.application.getString(R.string.reminder_off_title);
        activityTitle = (TextView) mActivity.findViewById(R.id.period_title);
        timerLayout = (RelativeLayout) mActivity.findViewById(R.id.timer_layout);
        swipeArea = (TextView) mActivity.findViewById(R.id.swipe_area_text);
        delayButton = (Button) mActivity.findViewById(R.id.button_period_end_extend);
    }

    @Test
    public void testMyActivityAppearsAsExpectedInitially(){
        Robolectric.getForegroundThreadScheduler().advanceBy(1000);
        assertEquals(activityTitle != null, true);
        assertEquals(timerLayout!=null, true);
        assertEquals(View.INVISIBLE, delayButton.getVisibility());
        actualText = activityTitle.getText().toString();
        assertEquals(expectedText, actualText);


    }

    @Test
    public void testAlarmRemovedProperly(){
        Assert.assertNull(shadowAlarmManager.getNextScheduledAlarm());

       int periodType = 1;
        mCalendar = RReminder.getNextPeriodEndTime(context, periodType, Calendar.getInstance().getTimeInMillis(), 1, 0L);
        new MobilePeriodManager(context).setPeriod(periodType, mCalendar,0);

        ShadowAlarmManager.ScheduledAlarm nextPeriodEnd = shadowAlarmManager.getNextScheduledAlarm();
        long nextAlarmEndTime = nextPeriodEnd.triggerAtTime;

        Assert.assertNotNull(nextPeriodEnd);
        assertEquals("next alarm should match the value initially set", mCalendar, nextAlarmEndTime);

        new MobilePeriodManager(context).setPeriod(periodType, mCalendar, 0);

        RReminderMobile.cancelCounterAlarm(context, periodType, 0,nextAlarmEndTime);

        nextPeriodEnd = shadowAlarmManager.getNextScheduledAlarm();
        Assert.assertNull(nextPeriodEnd);

    }

    @Test
    public void testUpdatedAlarmAfterSwipe(){

        Assert.assertNull(shadowAlarmManager.getNextScheduledAlarm());

        int periodType = 1;
        mCalendar = RReminder.getNextPeriodEndTime(context, periodType, Calendar.getInstance().getTimeInMillis(), 1, 0L);
        new MobilePeriodManager(context).setPeriod(periodType, mCalendar, 0);

        ShadowAlarmManager.ScheduledAlarm nextPeriodEnd = shadowAlarmManager.getNextScheduledAlarm();
        long nextAlarmEndTime = nextPeriodEnd.triggerAtTime;

        Assert.assertNotNull(nextPeriodEnd);
        assertEquals("next alarm should match the value initially set", mCalendar, nextAlarmEndTime);

        long expectedUpdatedEndTime = RReminder.getNextPeriodEndTime(context,2, Calendar.getInstance().getTimeInMillis(), 1, 0L);
        RReminderMobile.cancelCounterAlarm(context, periodType, 0,nextAlarmEndTime);
        new MobilePeriodManager(context).setPeriod(periodType, expectedUpdatedEndTime, 0);

        ShadowAlarmManager.ScheduledAlarm updatedNextPeriodEnd = shadowAlarmManager.getNextScheduledAlarm();
        long updatedNextAlarmEndTime = updatedNextPeriodEnd.triggerAtTime;
        Assert.assertNotNull(updatedNextPeriodEnd);
        assertEquals("next updated alarm should match the value initially set", expectedUpdatedEndTime, updatedNextAlarmEndTime);

    }

    @Test
    public void testUpdatedAlarmAfterDelay() throws InterruptedException {

        Assert.assertNull(shadowAlarmManager.getNextScheduledAlarm());

        int periodType = 1;
        mCalendar = RReminder.getNextPeriodEndTime(context, periodType, Calendar.getInstance().getTimeInMillis(), 1, 0L);
        new MobilePeriodManager(context).setPeriod(periodType, mCalendar, 0);

        ShadowAlarmManager.ScheduledAlarm nextPeriodEnd = shadowAlarmManager.getNextScheduledAlarm();
        long nextAlarmEndTime = nextPeriodEnd.triggerAtTime;

        Assert.assertNotNull(nextPeriodEnd);
        assertEquals("next alarm should match the value initially set", mCalendar, nextAlarmEndTime);

        new MobilePeriodManager(context).setPeriod(periodType, mCalendar, 0);
        System.out.println(Calendar.getInstance().toString());

        Thread.sleep(5000L);
        System.out.println(Calendar.getInstance().toString());
        long timeRemaining = mCalendar - Calendar.getInstance().getTimeInMillis();
        RReminderMobile.cancelCounterAlarm(context, periodType, 0,nextAlarmEndTime);
        long functionCalendar = RReminder.getTimeAfterExtend(context.getApplicationContext(),1,timeRemaining);
        new MobilePeriodManager(context).setPeriod(3, functionCalendar, 1);

        ShadowAlarmManager.ScheduledAlarm updatedNextPeriodEnd = shadowAlarmManager.getNextScheduledAlarm();
        long updatedNextAlarmEndTime = updatedNextPeriodEnd.triggerAtTime;
        Assert.assertNotNull(updatedNextPeriodEnd);
        assertEquals("next updated alarm should match the value initially set", functionCalendar, updatedNextAlarmEndTime);




    }
}
