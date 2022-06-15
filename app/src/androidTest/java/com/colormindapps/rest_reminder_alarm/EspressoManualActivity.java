package com.colormindapps.rest_reminder_alarm;

import android.content.pm.ActivityInfo;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoManualActivity {

    String debug = "MANUAL_ACTIVITY_TEST";

    @Rule
    public IntentsTestRule<ManualActivity> mActivityRule =
            new IntentsTestRule<ManualActivity>(ManualActivity.class, false, true);

    @Test
    public void testOrientationChange(){
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        int initialY = 1595;
        onView(withId(R.id.manual)).perform(CustomActions.scrollToY(initialY));
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        if(RReminder.isPortrait(getApplicationContext())){
            mActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            mActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        onView(withId(R.id.manual)).check(matches(CustomMatchers.withVerticalPositionSaved(initialY)));

    }

    @Test
    public void testBackToTop(){
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        int y = 550;
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.manual)).perform(CustomActions.scrollToY(y));
        onView(isRoot()).perform(CustomActions.waitFor(1000));
        onView(withId(R.id.button_back_to_top)).perform(click());
        int expectedY = 0;
        onView(isRoot()).perform(CustomActions.waitFor(2000));
        onView(withId(R.id.manual)).check(matches(CustomMatchers.withVerticalPosition(expectedY)));


    }
}
