package com.colormindapps.rest_reminder_alarm;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

public class RReminderTest {

    public static String getResourceString(int id) {
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        return targetContext.getResources().getString(id);
    }
}
