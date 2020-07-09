package com.colormindapps.rest_reminder_alarm;

import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.internal.util.Checks;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

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
                    Log.d("MATCHER", "expectedColor: "+color);
                    Log.d("MATCHER", "actual color: "+((ColorDrawable) background).getColor());
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

    public static Matcher<View> withNumberPickerValue(final int expectedValue) {
        return new BoundedMatcher<View, NumberPicker>(NumberPicker.class) {

            @Override
            protected boolean matchesSafely(NumberPicker numberPicker) {
                return numberPicker.getValue() == expectedValue;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with numberpicker value: ");
                description.appendValue(expectedValue);
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

    public static Matcher<View> withVerticalPosition(final int expectedVerticalPosition) {
        return new BoundedMatcher<View, WebView>(WebView.class) {

            @Override
            protected boolean matchesSafely(WebView webView) {
                Log.d("CUSTOM_MATCHERS", "vertical position: "+ webView.getScrollY());
                return webView.getScrollY() == expectedVerticalPosition;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with vertical scroll position size: ");
                description.appendValue(expectedVerticalPosition);
            }
        };
    }

    public static Matcher<View> withVerticalPositionSaved(final int expectedVerticalPosition) {
        return new BoundedMatcher<View, WebView>(WebView.class) {

            @Override
            protected boolean matchesSafely(WebView webView) {
                Log.d("CUSTOM_MATCHERS", "vertical position: "+ webView.getScrollY());
                return webView.getScrollY() > 1000;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with vertical scroll position size: ");
                description.appendValue(expectedVerticalPosition);
            }
        };
    }



    public static Matcher<View> withEnoughSpace() {
        return new BoundedMatcher<View, TextView>(TextView.class) {

            @Override
            protected boolean matchesSafely(TextView textView) {
                String debug = "CUSTOM_MATCHER";
                Rect rectf = new Rect();
                textView.getLocalVisibleRect(rectf);

                char[] thin_symbols =  {'1','t','i','I','j','l', '!', '^', '(', ')', '[', ']', '{', '}', ';', ':', '|', ',', '.'  };
                float lengthSum = 0.0f;

                CharSequence descrText = textView.getText();

                int descriptionLength = textView.getText().length();
                boolean isThin = false;
                int thinCount = 0;
                int textSize = (RReminder.isPortrait(getApplicationContext())) ? 20:18;

                for( int i = 0; i<descriptionLength;i++){
                    isThin = false;
                    for(int j=0;j<thin_symbols.length;j++){
                        if(descrText.charAt(i) == thin_symbols[j]){
                            isThin = true;
                            thinCount++;
                            break;
                        }
                    }
                    if(!isThin){
                        lengthSum+=1.0;
                    } else {
                        lengthSum+=0.2;
                    }
                }
                float scaledDensity = getApplicationContext().getResources().getDisplayMetrics().scaledDensity;
                float descriptionTextWidth = lengthSum * 10.3f * scaledDensity;
                Log.d(debug, "number of thin symbols: "+ thinCount);
                Log.d(debug, "description text width: " + descriptionTextWidth);

                float charWidth = 9 * scaledDensity;
                int charAmountInRow = (int)(rectf.width()/charWidth);
                Log.d(debug, "numbers of chars in a row: " + charAmountInRow);
                int expectedRowCount = (int)descriptionTextWidth / rectf.width();

                expectedRowCount+=1;
                Log.d(debug, "expected row count: " + expectedRowCount);
                int expectedDescriptionHeight = (int)(textSize* scaledDensity * expectedRowCount + 5f);

                Log.d(debug, "actual description height: "+ rectf.height());
                Log.d(debug, "expected description height: "+expectedDescriptionHeight);
                return rectf.height()>=expectedDescriptionHeight;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("there is enough space for whole text");
            }
        };
    }


}
