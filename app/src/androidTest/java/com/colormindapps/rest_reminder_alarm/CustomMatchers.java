package com.colormindapps.rest_reminder_alarm;

import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.internal.util.Checks;

import com.colormindapps.rest_reminder_alarm.charts.ColumnGraphView;
import com.colormindapps.rest_reminder_alarm.charts.PieView;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static androidx.core.util.Preconditions.checkNotNull;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static java.lang.Math.abs;

public class CustomMatchers {
    public static Matcher<View> withBackgroundColor(final int color) {
        Checks.checkNotNull(color);
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View view) {
                Drawable background = view.getBackground();
                if (background instanceof ColorDrawable){
                    Log.d("MATCHER", "actual color: "+((ColorDrawable) background).getColor());
                    Log.d("MATCHER", "expected color: "+color);
                    return color == ((ColorDrawable) background).getColor();
                } else {
                    return false;
                }
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

    public static Matcher<View> hasPeriodCount(final int periodCount) {
        return new BoundedMatcher<View, PieView>(PieView.class) {

            @Override
            protected boolean matchesSafely(PieView pieView) {
                return pieView.getPeriodList().size() == periodCount;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has period count: ");
                description.appendValue(periodCount);
            }
        };
    }

    public static Matcher<View> hasPeriodLength(final int position, final long expectedLength) {
        return new BoundedMatcher<View, PieView>(PieView.class) {

            @Override
            protected boolean matchesSafely(PieView pieView) {
                long length = pieView.getPeriodList().get(position).getPeriodDuration();
                Log.d("CUSTOM_MATCHER", "period length abs: "+abs(length-expectedLength));
                return abs(length-expectedLength) <1500;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected period length: ");
                description.appendValue(expectedLength);

            }
        };
    }

    public static Matcher<View> hasPeriodStart(final int position, final long expectedStart) {
        return new BoundedMatcher<View, PieView>(PieView.class) {

            @Override
            protected boolean matchesSafely(PieView pieView) {
                long periodStart = pieView.getPeriodList().get(position).getPeriodStart();
                Log.d("CUSTOM_MATCHER", "period start abs: "+abs(periodStart-expectedStart));
                return abs(periodStart-expectedStart) <1000;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected period start: ");
                description.appendValue(expectedStart);

            }
        };
    }

    public static Matcher<View> hasPeriodType(final int position, final int expectedType) {
        return new BoundedMatcher<View, PieView>(PieView.class) {

            @Override
            protected boolean matchesSafely(PieView pieView) {
                int type = pieView.getPeriodList().get(position).getPeriodType();
                return type == expectedType;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected period type: ");
                description.appendValue(expectedType);
            }
        };
    }

    public static Matcher<View> hasPercent(final int position, final float expectedPercent) {
        return new BoundedMatcher<View, PieView>(PieView.class) {

            @Override
            protected boolean matchesSafely(PieView pieView) {
                float percent = pieView.getColumnList().get(position).getPercent();
                Log.d("CUSTOM_MATCHER", "period percent abs: "+abs(percent- expectedPercent));
                Log.d("CUSTOM_MATCHER", "actual percent: "+percent);
                return abs(percent- expectedPercent)<1.0f;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected period percent: ");
                description.appendValue(expectedPercent);
            }
        };
    }

    public static Matcher<View> hasEnded(final int position) {
        return new BoundedMatcher<View, PieView>(PieView.class) {

            @Override
            protected boolean matchesSafely(PieView pieView) {
                return pieView.getPeriodList().get(position).getPeriod().getEnded()==1;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected period end status for period: ");
                description.appendValue(position);
            }
        };
    }

    public static Matcher<View> hasExtendCount(final int position, final int expectedExtendCount) {
        return new BoundedMatcher<View, PieView>(PieView.class) {

            @Override
            protected boolean matchesSafely(PieView pieView) {
                return pieView.getPeriodList().get(position).getPeriod().getExtendCount()==expectedExtendCount;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected extend count for period: ");
                description.appendValue(position);
                description.appendText(" is: ");
                description.appendValue(expectedExtendCount);
            }
        };
    }

    public static Matcher<View> hasExtendLength(final int position, final long expectedExtendLength) {
        return new BoundedMatcher<View, PieView>(PieView.class) {

            @Override
            protected boolean matchesSafely(PieView pieView) {
                long actualExtendLength = pieView.getPeriodList().get(position).getPeriod().getDuration() - pieView.getPeriodList().get(position).getPeriod().getInitialDuration();
                Log.d("CUSTOM_MATCHER", "period extend length: "+abs(actualExtendLength-expectedExtendLength));
                return abs(actualExtendLength-expectedExtendLength) <1000;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected extend length for period: ");
                description.appendValue(position);
                description.appendText(" is: ");
                description.appendValue(expectedExtendLength);
            }
        };
    }

    public static Matcher<View> hasTotalLength(final int position, final long expectedLength) {
        return new BoundedMatcher<View, PieView>(PieView.class) {

            @Override
            protected boolean matchesSafely(PieView pieView) {
                /*
                long lastPeriodLength = pieView.getPeriodList().get(pieView.getPeriodList().size()-1).getPeriodDuration();
                int lastPeriodType = pieView.getPeriodList().get(pieView.getPeriodList().size()-1).getPeriodType();
                Log.d("CUSTOM_MATCHER", "last period type: "+lastPeriodType+ ", and length: "+lastPeriodLength);

                 */
                long length = pieView.getColumnList().get(position).getTotalLength();

                Log.d("CUSTOM_MATCHER", "actual length: "+length);
                return abs(length-expectedLength)<1000;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected total length: ");
                description.appendValue(expectedLength);
            }
        };
    }

    public static Matcher<View> hasColumnTotalLength(final int position, final long expectedLength) {
        return new BoundedMatcher<View, ColumnGraphView>(ColumnGraphView.class) {

            @Override
            protected boolean matchesSafely(ColumnGraphView columnGraphView) {
                /*
                long lastPeriodLength = pieView.getPeriodList().get(pieView.getPeriodList().size()-1).getPeriodDuration();
                int lastPeriodType = pieView.getPeriodList().get(pieView.getPeriodList().size()-1).getPeriodType();
                Log.d("CUSTOM_MATCHER", "last period type: "+lastPeriodType+ ", and length: "+lastPeriodLength);

                 */
                long length = columnGraphView.getColumnList().get(position).getTotalLength();

                Log.d("CUSTOM_MATCHER", "actual length: "+length);
                return abs(length-expectedLength)<1000;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("for type: ");
                description.appendValue(position);
                description.appendText(" expected column total length: ");
                description.appendValue(expectedLength);
            }
        };
    }

    public static Matcher<View> hasColumnTotalCount(final int position, final int expectedCount) {
        return new BoundedMatcher<View, ColumnGraphView>(ColumnGraphView.class) {

            @Override
            protected boolean matchesSafely(ColumnGraphView columnGraphView) {
                /*
                long lastPeriodLength = pieView.getPeriodList().get(pieView.getPeriodList().size()-1).getPeriodDuration();
                int lastPeriodType = pieView.getPeriodList().get(pieView.getPeriodList().size()-1).getPeriodType();
                Log.d("CUSTOM_MATCHER", "last period type: "+lastPeriodType+ ", and length: "+lastPeriodLength);

                 */
                int count = columnGraphView.getColumnList().get(position).getCount();

                return count == expectedCount;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("for type: ");
                description.appendValue(position);
                description.appendText(" expected column total count: ");
                description.appendValue(expectedCount);
            }
        };
    }

    public static Matcher<View> hasColumnTotalExtendLength(final int position, final long expectedLength) {
        return new BoundedMatcher<View, ColumnGraphView>(ColumnGraphView.class) {

            @Override
            protected boolean matchesSafely(ColumnGraphView columnGraphView) {
                /*
                long lastPeriodLength = pieView.getPeriodList().get(pieView.getPeriodList().size()-1).getPeriodDuration();
                int lastPeriodType = pieView.getPeriodList().get(pieView.getPeriodList().size()-1).getPeriodType();
                Log.d("CUSTOM_MATCHER", "last period type: "+lastPeriodType+ ", and length: "+lastPeriodLength);

                 */
                long length = columnGraphView.getColumnList().get(position).getTotalExtendDuration();

                Log.d("CUSTOM_MATCHER", "actual length: "+length);
                return abs(length-expectedLength)<1000;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("for type: ");
                description.appendValue(position);
                description.appendText(" expected column total extend length: ");
                description.appendValue(expectedLength);
            }
        };
    }

    public static Matcher<View> hasColumnTotalExtendCount(final int position, final int expectedCount) {
        return new BoundedMatcher<View, ColumnGraphView>(ColumnGraphView.class) {

            @Override
            protected boolean matchesSafely(ColumnGraphView columnGraphView) {
                /*
                long lastPeriodLength = pieView.getPeriodList().get(pieView.getPeriodList().size()-1).getPeriodDuration();
                int lastPeriodType = pieView.getPeriodList().get(pieView.getPeriodList().size()-1).getPeriodType();
                Log.d("CUSTOM_MATCHER", "last period type: "+lastPeriodType+ ", and length: "+lastPeriodLength);

                 */
                int count = columnGraphView.getColumnList().get(position).getExtendCount();

                return count == expectedCount;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("for type: ");
                description.appendValue(position);
                description.appendText(" expected column total extend count: ");
                description.appendValue(expectedCount);
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
                Log.d("CUSTOM_MATCHER", "actual text size: "+(int)(textView.getTextSize()/scaledDensity));
                int actualSize = (int)(textView.getTextSize()/scaledDensity);
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
                return webView.getScrollY() > 1000;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with vertical scroll position size: ");
                description.appendValue(expectedVerticalPosition);
            }
        };
    }

    public static Matcher<View> atPosition(final int position, @NonNull final Matcher<View> itemMatcher) {
        checkNotNull(itemMatcher);
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position + ": ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view) {
                RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    // has no item on such position
                    return false;
                }
                return itemMatcher.matches(viewHolder.itemView);
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
