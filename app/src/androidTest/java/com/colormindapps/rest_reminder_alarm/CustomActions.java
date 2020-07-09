package com.colormindapps.rest_reminder_alarm;

import android.view.View;
import android.webkit.WebView;
import android.widget.NumberPicker;

import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.espresso.util.TreeIterables;

import org.hamcrest.Matcher;

import java.util.concurrent.TimeoutException;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

public class CustomActions {
    public static ViewAction scrollToY(final int posY) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isDisplayed(), isAssignableFrom(WebView.class));
            }

            @Override
            public String getDescription() {
                return "Scrolling to " + posY + " Y coordinate";
            }

            @Override
            public void perform(UiController uiController, final View view) {
                ((WebView) view).scrollTo(0, posY);
            }
        };
    }

    public static ViewAction waitFor(final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + " milliseconds.";
            }

            @Override
            public void perform(UiController uiController, final View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }

    public static ViewAction setValue(final int value){
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isDisplayed(), isAssignableFrom(NumberPicker.class));
            }

            @Override
            public String getDescription() {
                return "set value to " + value;
            }

            @Override
            public void perform(UiController uiController, final View view) {
                ((NumberPicker) view).setValue(value);
            }
        };
    }
}
