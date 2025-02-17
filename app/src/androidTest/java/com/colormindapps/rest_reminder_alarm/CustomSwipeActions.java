package com.colormindapps.rest_reminder_alarm;


import android.view.View;

import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.PrecisionDescriber;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swiper;

import org.hamcrest.Matcher;

import static androidx.test.espresso.action.ViewActions.actionWithAssertions;

public class CustomSwipeActions {

    /**
     * Fully customisable Swipe action for any need
     *
     * @param duration length of time a custom swipe should last for, in milliseconds.
     * @param from     for example [GeneralLocation.CENTER]
     * @param to       for example [GeneralLocation.BOTTOM_CENTER]
     */
    public static ViewAction swipeCustom(int duration, GeneralLocation from, GeneralLocation to) {
        CustomSwipe.CUSTOM.setSwipeDuration(duration);
        return actionWithAssertions(new GeneralSwipeAction(
                CustomSwipe.CUSTOM,
                translate(from, 0f, 0f),
                to,
                Press.FINGER)
        );
    }

    /**
     * Translates the given coordinates by the given distances. The distances are given in term
     * of the view's size -- 1.0 means to translate by an amount equivalent to the view's length.
     */
    private static CoordinatesProvider translate(final CoordinatesProvider coords,
                                                 final float dx, final float dy) {
        return new CoordinatesProvider() {
            @Override
            public float[] calculateCoordinates(View view) {
                float xy[] = coords.calculateCoordinates(view);
                xy[0] += dx * view.getWidth();
                xy[1] += dy * view.getHeight();
                return xy;
            }
        };
    }
}


