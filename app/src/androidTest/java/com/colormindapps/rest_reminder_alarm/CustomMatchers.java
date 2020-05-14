package com.colormindapps.rest_reminder_alarm;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.internal.util.Checks;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

public class CustomMatchers {
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



    public static Matcher<View> withTextColor(final int expectedId) {
        return new BoundedMatcher<View, TextView>(TextView.class) {

            @Override
            protected boolean matchesSafely(TextView textView) {
                return textView.getCurrentTextColor() == expectedId;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with text color: ");
                description.appendValue(expectedId);
            }
        };
    }

    public static Matcher<View> withTextSize(final int expectedSize) {
        return new BoundedMatcher<View, TextView>(TextView.class) {

            @Override
            protected boolean matchesSafely(TextView textView) {
                float scaledDensity = getApplicationContext().getResources().getDisplayMetrics().scaledDensity;
                int actualSize = (int)(textView.getTextSize()/scaledDensity);
                Log.d("CUSTOM_MATCHER", "expectedSize: "+expectedSize);
                Log.d("CUSTOM_MATCHER", "actualSize: "+actualSize);
                return actualSize == expectedSize;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with text size: ");
                description.appendValue(expectedSize);
            }
        };
    }
}
