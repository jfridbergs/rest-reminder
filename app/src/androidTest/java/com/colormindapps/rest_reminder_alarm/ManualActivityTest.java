package com.colormindapps.rest_reminder_alarm;

import android.app.Instrumentation;
import android.content.pm.ActivityInfo;
import android.test.ActivityInstrumentationTestCase2;
import android.webkit.WebView;
import android.widget.Button;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;
import com.robotium.solo.Solo;

/**
 * Created by ingressus on 08.05.2015..
 */
public class ManualActivityTest extends ActivityInstrumentationTestCase2<ManualActivity> {
    Instrumentation instr;
    Solo solo;
    ManualActivity activity;
    WebView manual, manualLand;
    Button backToTop;

    public ManualActivityTest(){
        super(ManualActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();

        instr = this.getInstrumentation();

        solo = new Solo(instr,activity);


        setActivityInitialTouchMode(false);

        activity = getActivity();
        manual = (WebView) activity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.manual);
        backToTop = (Button) activity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.button_back_to_top);

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testOrientationChange(){
        solo.waitForView(manual);
        solo.sleep(2000);
        int initialY = 1595;
        manual.scrollTo(0, initialY);

        solo.sleep(1000);

        //rotate screen
        if(RReminder.isPortrait(activity.getApplicationContext())){
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        instr.waitForIdleSync();

        solo.sleep(3000);

        int actualY = manual.getScrollY();
        assertEquals("after screen rotation, the screen position should be restored", initialY, actualY);

    }

    public void testBackToTop(){
        solo.waitForView(manual);
        int y = 550;
        solo.sleep(1000);
        manual.scrollTo(0, y);
        solo.sleep(1000);
        solo.clickOnView(backToTop);
        int expectedY = 0;
        solo.sleep(2000);
        int actualY = manual.getScrollY();
        assertEquals("pressing back to top should scroll back to top", expectedY, actualY);


    }
}