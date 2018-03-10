package com.colormindapps.rest_reminder_alarm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.test.ServiceTestCase;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.Calendar;

/**
 * Created by ingressus on 31.12.2015..
 */
public class CounterServiceTest extends ServiceTestCase<CounterService> {

    long endTime;
    private Context context;
    CounterService cService;

    public CounterServiceTest(){
        super(CounterService.class);
    }

    @Override
    protected void setUp() throws Exception {
        endTime = Calendar.getInstance().getTimeInMillis();
        context = getSystemContext();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

    }

    public void testOnCreateCall(){
        Intent startIntent = new Intent();
        startIntent.putExtra(RReminder.PERIOD_TYPE, 1);
        startIntent.putExtra(RReminder.PERIOD_END_TIME, RReminder.getNextPeriodEndTime(getContext(), 1, endTime, 0, 0L));
        startIntent.putExtra(RReminder.EXTEND_COUNT, 0);
        startIntent.setClass(getContext(), CounterService.class);
        startService(startIntent);
        cService = getService();
        assertTrue("service onCreate has been called", cService.created);
        assertTrue("service onCreate has been called 1 time", cService.onCreateCount == 1);
        Bundle counterServiceData = cService.getData();
        int actualType = counterServiceData.getInt(RReminder.PERIOD_TYPE);
        int actualExtendCount = counterServiceData.getInt(RReminder.EXTEND_COUNT);
        long actualPeriodEndTime = counterServiceData.getLong(RReminder.PERIOD_END_TIME);
        assertEquals("service intent data type",1, actualType);
        assertEquals("service itent data extendcount",0,actualExtendCount);
        assertEquals("service intent data periodendtime", RReminder.getNextPeriodEndTime(getContext(), 1, endTime, 0, 0L),actualPeriodEndTime);
        startService(startIntent);
        assertTrue("service onCreate has not been called for second time", cService.onCreateCount == 1);
        assertEquals("service onStartCommand has been called twice", 2, cService.onStartCommandCount);
        startService(startIntent);
        assertTrue("service onCreate has not been called for second time", cService.onCreateCount == 1);
        assertEquals("service onStartCommand has been called twice", 3, cService.onStartCommandCount);
        shutdownService();
        assertFalse("service onCreate true status should be revoked", cService.created);
    }

}
