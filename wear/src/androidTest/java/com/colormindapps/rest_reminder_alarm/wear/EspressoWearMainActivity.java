package com.colormindapps.rest_reminder_alarm.wear;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.IdlingResource;
import androidx.test.filters.LargeTest;
import androidx.test.internal.util.Checks;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.core.content.ContextCompat;
import android.view.View;

import com.colormindapps.rest_reminder_alarm.R;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
* Created by ingressus on 09/02/2017.
        */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoWearMainActivity {
    Context targetContext;
    String expectedOfflineTitle, expectedOnlineRestTitle, expectedOnlineWorkTitle;
    String descrExtendedOnce, descrExtendedTwice, descrExtendedMultiple;
    int offColor, workColor, restColor, redColor;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    @Rule
    public ActivityTestRule<WearMainActivity> mActivityRule = new ActivityTestRule(WearMainActivity.class);

    @Before
    public void setUp(){
        targetContext = InstrumentationRegistry.getTargetContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(targetContext);
        editor = prefs.edit();

        IdlingPolicies.setMasterPolicyTimeout(180, TimeUnit.SECONDS);
        IdlingPolicies.setIdlingResourceTimeout(120, TimeUnit.SECONDS);

        expectedOfflineTitle = getResourceString(R.string.reminder_off_title);
        expectedOnlineWorkTitle = getResourceString(R.string.on_work_period).toUpperCase();
        expectedOnlineRestTitle = getResourceString(R.string.on_rest_period).toUpperCase();
        descrExtendedOnce = getResourceString(R.string.description_extended_one_time);
        descrExtendedTwice = getResourceStringWithIntParam(R.string.description_extended,2);
        descrExtendedMultiple = getResourceStringWithIntParam(R.string.description_extended,4);
        offColor = getColor(R.color.black);
        workColor = getColor(R.color.work);
        restColor = getColor(R.color.rest);
        redColor = getColor(R.color.red);
    }

    @After
    public void cleanUp(){
        final WearMainActivity activity = mActivityRule.getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.stopReminder();
            }
        });
    }

    @Test
    public void launchReminder() {

        String expectedOfflineTitle = getResourceString(R.string.reminder_off_title);
        String expectedOnlineWorkTitle = getResourceString(R.string.on_work_period).toUpperCase();
        int offColor = getColor(R.color.black);
        int workColor = getColor(R.color.work);
        onView(withId(R.id.title)).check(matches(withText(expectedOfflineTitle)));
        onView(withId(R.id.container)).check(matches(withBackgroundColor(offColor)));
        onView(withId(R.id.timer_layout)).perform(click());
        onView(withId(R.id.title)).check(matches(withText(expectedOnlineWorkTitle)));
        onView(withId(R.id.container)).check(matches(withBackgroundColor(workColor)));
        onView(withId(R.id.timer_layout)).perform(click());
        onView(withId(R.id.title)).check(matches(withText(expectedOfflineTitle)));
        onView(withId(R.id.container)).check(matches(withBackgroundColor(offColor)));

    }

    private String getResourceString(int id) {
        return targetContext.getResources().getString(id);
    }

    private String getResourceStringWithIntParam(int id, int param){
        return targetContext.getResources().getString(id, param);
    }

    private int getColor(int id){
        return ContextCompat.getColor(targetContext,id);
    }


    public static Matcher<View> withBackgroundColor(final int color) {
        Checks.checkNotNull(color);
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View view) {
                Drawable background = view.getBackground();
                if (background instanceof ColorDrawable){
                    return color == ((ColorDrawable) background).getColor();
                } else return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with background color: ");
            }
        };
    }

    @Test
    public void updateUiOnDataChanged(){
        final WearMainActivity activity = mActivityRule.getActivity();


        onView(withId(R.id.title)).check(matches(withText(expectedOfflineTitle)));
        onView(withId(R.id.container)).check(matches(withBackgroundColor(offColor)));
        onView(withId(R.id.description)).check(matches(withText("")));
        onView(withId(R.id.timer_layout)).perform(click());

        //updating wear to default rest period
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.updateActivity(2, RReminder.getNextPeriodEndTime(targetContext,2, Calendar.getInstance().getTimeInMillis(),0,0),0, false);
            }
        });

        onView(withId(R.id.title)).check(matches(withText(expectedOnlineRestTitle)));
        onView(withId(R.id.container)).check(matches(withBackgroundColor(restColor)));
        onView(withId(R.id.description)).check(matches(withText("")));

        //updating wear to rest period with single extend
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.updateActivity(4, RReminder.getNextPeriodEndTime(targetContext,4, Calendar.getInstance().getTimeInMillis(),0,0),1, false);
            }
        });

        onView(withId(R.id.title)).check(matches(withText(expectedOnlineRestTitle)));
        onView(withId(R.id.container)).check(matches(withBackgroundColor(restColor)));
        onView(withId(R.id.description)).check(matches(withText(descrExtendedOnce)));

        //updating wear to work period with single extend
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.updateActivity(3, RReminder.getNextPeriodEndTime(targetContext,3, Calendar.getInstance().getTimeInMillis(),0,0),1, false);
            }
        });

        onView(withId(R.id.title)).check(matches(withText(expectedOnlineWorkTitle)));
        onView(withId(R.id.container)).check(matches(withBackgroundColor(workColor)));
        onView(withId(R.id.description)).check(matches(withText(descrExtendedOnce)));

        //updating wear to work period with two extensions
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.updateActivity(3, RReminder.getNextPeriodEndTime(targetContext,3, Calendar.getInstance().getTimeInMillis(),0,0),2, false);
            }
        });

        onView(withId(R.id.title)).check(matches(withText(expectedOnlineWorkTitle)));
        onView(withId(R.id.container)).check(matches(withBackgroundColor(workColor)));
        onView(withId(R.id.description)).check(matches(withText(descrExtendedTwice)));

        //updating wear to rest period with two extensions
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.updateActivity(4, RReminder.getNextPeriodEndTime(targetContext,4, Calendar.getInstance().getTimeInMillis(),0,0),2, false);
            }
        });
        onView(withId(R.id.title)).check(matches(withText(expectedOnlineRestTitle)));
        onView(withId(R.id.container)).check(matches(withBackgroundColor(restColor)));
        onView(withId(R.id.description)).check(matches(withText(descrExtendedTwice)));

        //updating wear to off state
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.updateActivity(0, 0L,0, false);
            }
        });

        onView(withId(R.id.title)).check(matches(withText(expectedOfflineTitle)));
        onView(withId(R.id.container)).check(matches(withBackgroundColor(offColor)));
        onView(withId(R.id.description)).check(matches(withText("")));

        //updating wear to work period with two extensions
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.updateActivity(3, RReminder.getNextPeriodEndTime(targetContext,3, Calendar.getInstance().getTimeInMillis(),0,0),4, false);
            }
        });

        onView(withId(R.id.title)).check(matches(withText(expectedOnlineWorkTitle)));
        onView(withId(R.id.container)).check(matches(withBackgroundColor(redColor)));
        onView(withId(R.id.description)).check(matches(withText(descrExtendedMultiple)));

        //updating wear to rest period with two extensions
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.updateActivity(4, RReminder.getNextPeriodEndTime(targetContext,4, Calendar.getInstance().getTimeInMillis(),0,0),4, false);
            }
        });

        onView(withId(R.id.title)).check(matches(withText(expectedOnlineRestTitle)));
        onView(withId(R.id.container)).check(matches(withBackgroundColor(redColor)));
        onView(withId(R.id.description)).check(matches(withText(descrExtendedMultiple)));

    }

    @Test
    public void testPeriodChange(){
        editor.putString(RReminder.PREF_WORK_LENGTH_KEY,"00:01");
        editor.putString(RReminder.PREF_REST_LENGTH_KEY, "00:01");
        editor.apply();

        onView(withId(R.id.title)).check(matches(withText(expectedOfflineTitle)));
        onView(withId(R.id.container)).check(matches(withBackgroundColor(offColor)));

        onView(withId(R.id.timer_layout)).perform(click());

        onView(withId(R.id.title)).check(matches(withText(expectedOnlineWorkTitle)));
        onView(withId(R.id.container)).check(matches(withBackgroundColor(workColor)));

        IdlingResource idlingResource = new SleepIdlingResource(60000);
        Espresso.registerIdlingResources(idlingResource);

        onView(withId(R.id.title)).check(matches(withText(expectedOnlineRestTitle)));
        onView(withId(R.id.container)).check(matches(withBackgroundColor(restColor)));

        Espresso.unregisterIdlingResources(idlingResource);


    }



}
