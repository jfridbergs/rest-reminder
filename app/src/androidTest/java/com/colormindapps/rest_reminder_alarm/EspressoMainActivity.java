package com.colormindapps.rest_reminder_alarm;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.internal.util.Checks;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
* Created by ingressus on 09/02/2017.
        */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoMainActivity {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule(MainActivity.class);

    @Test
    public void launchReminder() {

        String expectedOfflineTitle = getResourceString(R.string.reminder_off_title);
        String expectedOnlineWorkTitle = getResourceString(R.string.on_work_period).toUpperCase();
        onView(withId(R.id.period_title)).check(matches(withText(expectedOfflineTitle)));
        onView(withId(R.id.timer_layout)).perform(click());
        onView(withId(R.id.period_title)).check(matches(withText(expectedOnlineWorkTitle)));
    }

    private String getResourceString(int id) {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        return targetContext.getResources().getString(id);
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


}
