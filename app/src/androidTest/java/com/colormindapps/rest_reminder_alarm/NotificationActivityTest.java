package com.colormindapps.rest_reminder_alarm;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.test.ActivityInstrumentationTestCase2;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;
import com.robotium.solo.Solo;

import java.util.Calendar;


public class NotificationActivityTest extends ActivityInstrumentationTestCase2<NotificationActivity> {

    ImageView image;
    TextView description, title, mActivityTitle, mActivityDescription, extendDescription;
    Button notificationButton, extendButton;
    Context appContext;
    Instrumentation instr;
    float scaledDensity;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Resources resources;
    Calendar customTime;
    Intent intent;
    int extendBaseLength, extendOptionCount;
    String expectedOnlineWorkTitle, expectedOnlineRestTitle;

    private int versionCode;
    private Solo solo;

    public NotificationActivityTest(){
        super(NotificationActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();
        instr = this.getInstrumentation();




        setActivityInitialTouchMode(false);
        long currentTime = Calendar.getInstance().getTimeInMillis();
        currentTime+=360000L;

        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(RReminder.PERIOD_TYPE, 1);
        intent.putExtra(RReminder.EXTEND_COUNT, 0);
        intent.putExtra(RReminder.PLAY_SOUND, false);
        intent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        intent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        setActivityIntent(intent);

        solo = new Solo(instr,getActivity());

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        editor   = preferences.edit();




        title = (TextView) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_title);
        image = (ImageView) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_image);
        description = (TextView) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_description);
        extendDescription = (TextView) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_extend_description);
        notificationButton = (Button) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_button);
        extendButton = (Button) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.button_notification_period_end_extend);

        extendOptionCount = preferences.getInt(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key),1);
        extendBaseLength = preferences.getInt(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key),5);
        expectedOnlineWorkTitle = getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.on_work_period).toUpperCase();
        expectedOnlineRestTitle = getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.on_rest_period).toUpperCase();
        versionCode = preferences.getInt(RReminder.VERSION_KEY, 0);

    }

    public void tearDown() throws Exception {
        RReminderMobile.stopCounterService(getActivity().getApplicationContext(),1);
        RReminderMobile.stopCounterService(getActivity().getApplicationContext(),2);
        RReminderMobile.stopCounterService(getActivity().getApplicationContext(),3);
        RReminderMobile.stopCounterService(getActivity().getApplicationContext(),4);

        editor = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();

        editor.putBoolean(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.putString(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_key), "0");
        editor.putBoolean(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_end_period_key), true);
        editor.putString(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), RReminder.DEFAULT_REST_PERIOD_STRING);
        editor.putString(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_work_period_length_key), "00:45");
        editor.putString(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_rest_period_length_key), "00:15");
        editor.putInt(RReminder.VERSION_KEY, versionCode);
        editor.commit();
        solo.finishOpenedActivities();
        super.tearDown();
    }
    /*
    public void testImageId(){
        solo.sleep(5000);
        String hello = "Hello";
        assertTrue(image!=null);
    }
    */

    public void testExtendDescription(){
        Intent workSingleExtendIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        workSingleExtendIntent = new Intent(Intent.ACTION_VIEW);
        workSingleExtendIntent.putExtra(RReminder.PERIOD_TYPE, 3);
        workSingleExtendIntent.putExtra(RReminder.EXTEND_COUNT, 1);
        workSingleExtendIntent.putExtra(RReminder.PLAY_SOUND, false);
        workSingleExtendIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        workSingleExtendIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        clearReferences();
        getActivity().finish();
        setActivity(null);
        setActivityIntent(workSingleExtendIntent);

        solo.sleep(5000);
        extendDescription = (TextView) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_extend_description);


        int expectedFontSize = RReminder.isTablet(getActivity().getApplicationContext()) ? 21 :16;
        int actualFontSize =(int)( extendDescription.getTextSize()/getActivity().getResources().getDisplayMetrics().scaledDensity);
        assertEquals("notification extend description size", expectedFontSize, actualFontSize);
        int expectedColor = getActivity().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.black);
        int actualColor = extendDescription.getCurrentTextColor();
        assertEquals("the extend description should be black", expectedColor, actualColor);
        String expectedText = String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.notification_end_extend_description_once),"work");
        String actualText = extendDescription.getText().toString();
        assertEquals("the extend description text", expectedText, actualText);

        Intent restSingleExtendIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        restSingleExtendIntent = new Intent(Intent.ACTION_VIEW);
        restSingleExtendIntent.putExtra(RReminder.PERIOD_TYPE, 4);
        restSingleExtendIntent.putExtra(RReminder.EXTEND_COUNT, 1);
        restSingleExtendIntent.putExtra(RReminder.PLAY_SOUND, false);
        restSingleExtendIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        restSingleExtendIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        clearReferences();
        getActivity().finish();
        setActivity(null);
        setActivityIntent(restSingleExtendIntent);
        solo.sleep(5000);
        extendDescription = (TextView) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_extend_description);


        expectedText = String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.notification_end_extend_description_once),"rest");
        actualText = extendDescription.getText().toString();
        assertEquals("the extend description text", expectedText, actualText);

        Intent workMultipleExtendIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        workMultipleExtendIntent = new Intent(Intent.ACTION_VIEW);
        workMultipleExtendIntent.putExtra(RReminder.PERIOD_TYPE, 3);
        workMultipleExtendIntent.putExtra(RReminder.EXTEND_COUNT, 3);
        workMultipleExtendIntent.putExtra(RReminder.PLAY_SOUND, false);
        workMultipleExtendIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        workMultipleExtendIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        clearReferences();
        getActivity().finish();
        setActivity(null);
        setActivityIntent(workMultipleExtendIntent);

        solo.sleep(5000);
        extendDescription = (TextView) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_extend_description);



        expectedText = String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.notification_end_extend_description_multiple),"work",3);
        actualText = extendDescription.getText().toString();
        assertEquals("the extend description text", expectedText, actualText);

        Intent restMultipleExtendIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        restMultipleExtendIntent = new Intent(Intent.ACTION_VIEW);
        restMultipleExtendIntent.putExtra(RReminder.PERIOD_TYPE, 4);
        restMultipleExtendIntent.putExtra(RReminder.EXTEND_COUNT, 3);
        restMultipleExtendIntent.putExtra(RReminder.PLAY_SOUND, false);
        restMultipleExtendIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        restMultipleExtendIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        clearReferences();
        getActivity().finish();
        setActivity(null);
        setActivityIntent(restMultipleExtendIntent);

        solo.sleep(5000);
        extendDescription = (TextView) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_extend_description);

        expectedText = String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.notification_end_extend_description_multiple),"rest",3);
        actualText = extendDescription.getText().toString();
        assertEquals("the extend description text", expectedText, actualText);
        extendDescription = null;
    }

    public void testDescription(){
        int expectedFontSize;
        if(RReminder.isTablet(getActivity().getApplicationContext())){
            expectedFontSize = 28;
        } else {
            expectedFontSize = RReminder.isPortrait(getActivity().getApplicationContext()) ? 20:18;
        }
        int actualFontSize =(int)( description.getTextSize()/getActivity().getResources().getDisplayMetrics().scaledDensity);
        assertEquals("notification description size", expectedFontSize, actualFontSize);
        int expectedColor = getActivity().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.black);
        int actualColor = description.getCurrentTextColor();
        assertEquals("the description should be black", expectedColor, actualColor);

        String expectedText = String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.next_period_end_description),"rest","23:30");
        String actualText = description.getText().toString();
        assertEquals("the description text", expectedText, actualText);


        //test description content in manual mode

        Rect rectf = new Rect();
        description.getLocalVisibleRect(rectf);
        assertTrue("the actual description height should match the expected height", hasEnoughSpaceForDescription(description.getText(),rectf.height(), rectf.width(),expectedFontSize));

        //setting up the test for rest period end
        Intent restIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        restIntent = new Intent(Intent.ACTION_VIEW);
        restIntent.putExtra(RReminder.PERIOD_TYPE, 2);
        restIntent.putExtra(RReminder.EXTEND_COUNT, 0);
        restIntent.putExtra(RReminder.PLAY_SOUND, false);
        restIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        restIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        clearReferences();
        getActivity().finish();
        setActivity(null);
        setActivityIntent(restIntent);

        solo.sleep(5000);
        description = (TextView) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_description);

        actualFontSize =(int)( description.getTextSize()/getActivity().getResources().getDisplayMetrics().scaledDensity);
        assertEquals("notification description size should be 20 or 28 (rest period)", expectedFontSize, actualFontSize);

        expectedColor = getActivity().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.black);
        actualColor = description.getCurrentTextColor();
        assertEquals("the description should be black (rest period)", expectedColor, actualColor);

        expectedText = String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.next_period_end_description), "work","23:30");
        actualText = description.getText().toString();
        assertEquals("the description text (rest period)", expectedText, actualText);

        description.getLocalVisibleRect(rectf);
        assertTrue("the actual description height should match the expected height (rest period)", hasEnoughSpaceForDescription(description.getText(),rectf.height(), rectf.width(), expectedFontSize));

    }

    public void testNotificationManualMode(){
        int expectedFontSize;
        editor.putString(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_key), "1");
        editor.commit();

        solo.sleep(3000);

        Intent workIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        workIntent = new Intent(Intent.ACTION_VIEW);
        workIntent.putExtra(RReminder.PERIOD_TYPE, 1);
        workIntent.putExtra(RReminder.EXTEND_COUNT, 0);
        workIntent.putExtra(RReminder.PLAY_SOUND, false);
        workIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        workIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        clearReferences();
        getActivity().finish();
        setActivity(null);
        setActivityIntent(workIntent);

        solo.sleep(5000);
        if(RReminder.isTablet(getActivity().getApplicationContext())){
            expectedFontSize = 28;
        } else {
            expectedFontSize = (RReminder.isPortrait(getActivity().getApplicationContext())? 20:18);
        }
        description = (TextView) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_description);
        Rect rectf = new Rect();
        String expectedText = String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.notification_end_manual_title), "rest");
        String actualText = description.getText().toString();
        assertEquals("the description text in manual mode after work period ended", expectedText, actualText);
        description.getLocalVisibleRect(rectf);
        assertTrue("the actual description height should match the expected height (work period)", hasEnoughSpaceForDescription(description.getText(),rectf.height(), rectf.width(), expectedFontSize));


        Intent restIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        restIntent = new Intent(Intent.ACTION_VIEW);
        restIntent.putExtra(RReminder.PERIOD_TYPE, 2);
        restIntent.putExtra(RReminder.EXTEND_COUNT, 0);
        restIntent.putExtra(RReminder.PLAY_SOUND, false);
        restIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        restIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        clearReferences();
        getActivity().finish();
        setActivity(null);
        setActivityIntent(restIntent);

        solo.sleep(5000);
        rectf = new Rect();

        description = (TextView) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_description);
        expectedText = String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.notification_end_manual_title), "work");
        actualText = description.getText().toString();
        assertEquals("the description text in manual mode after rest period ended", expectedText, actualText);
        description.getLocalVisibleRect(rectf);
        assertTrue("the actual description height should match the expected height (rest period)", hasEnoughSpaceForDescription(description.getText(),rectf.height(), rectf.width(), expectedFontSize));

    }

    public void testWorkPeriod3rdExtend(){
        Intent workMultipleExtendIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        workMultipleExtendIntent = new Intent(Intent.ACTION_VIEW);
        workMultipleExtendIntent.putExtra(RReminder.PERIOD_TYPE, 3);
        workMultipleExtendIntent.putExtra(RReminder.EXTEND_COUNT, 3);
        workMultipleExtendIntent.putExtra(RReminder.PLAY_SOUND, false);
        workMultipleExtendIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        workMultipleExtendIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        //prevent memory leak
        title = null;
        image = null;
        description = null;
        extendDescription = null;
        notificationButton = null;
        extendButton = null;
        clearReferences();
        getActivity().finish();
        setActivity(null);
        setActivityIntent(workMultipleExtendIntent);

        solo.sleep(5000);

        title = (TextView) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_title);
        image = (ImageView) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_image);
        description = (TextView) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_description);
        extendButton = (Button) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.button_notification_period_end_extend);

        solo.clickOnView(extendButton);
        instr.waitForIdleSync();



        String optionOneButton, optionTwoButton, optionThreeButton;
        extendBaseLength= PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getInt(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key),5);
        optionOneButton = String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength);



        solo.clickOnButton(optionOneButton);


        Instrumentation.ActivityMonitor monitor = instr.addMonitor(MainActivity.class.getName(), null, false);

        Activity mActivity = instr.waitForMonitor(monitor);
        solo.assertCurrentActivity("main activity should be open", MainActivity.class);

        solo.sleep(3000);
        RelativeLayout rootLayout = (RelativeLayout) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.mainActivityLayout);
        mActivityTitle = (TextView) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.period_title);
        String expectedTitle = getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.on_work_period).toUpperCase();;
        String actualTitle = mActivityTitle.getText().toString().toUpperCase();
        assertEquals("after extending previously ended period, the active period is work period", expectedTitle, actualTitle);
        mActivityDescription = (TextView) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.description_text);
        String expectedDescription = String.format(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.description_extended),4);
        String actualDescription = mActivityDescription.getText().toString();
        assertEquals("after extending the period the description should say extended", expectedDescription, actualDescription);

        int expectedBgColor = getActivity().getApplicationContext().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.red);
        int actualBgColor = Color.TRANSPARENT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Drawable background = rootLayout.getBackground();
            if (background instanceof ColorDrawable)
                actualBgColor = ((ColorDrawable) background).getColor();
        }

        assertEquals("after 4th extend the background color should change to red", expectedBgColor, actualBgColor);

    }

    public void testRestPeriod3rdExtend(){
        Intent workMultipleExtendIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        workMultipleExtendIntent = new Intent(Intent.ACTION_VIEW);
        workMultipleExtendIntent.putExtra(RReminder.PERIOD_TYPE, 4);
        workMultipleExtendIntent.putExtra(RReminder.EXTEND_COUNT, 3);
        workMultipleExtendIntent.putExtra(RReminder.PLAY_SOUND, false);
        workMultipleExtendIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        workMultipleExtendIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        clearReferences();
        getActivity().finish();
        setActivity(null);
        setActivityIntent(workMultipleExtendIntent);

        solo.sleep(5000);

        title = (TextView) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_title);
        image = (ImageView) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_image);
        description = (TextView) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_description);
        extendButton = (Button) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.button_notification_period_end_extend);

        solo.clickOnView(extendButton);
        instr.waitForIdleSync();



        String optionOneButton, optionTwoButton, optionThreeButton;
        extendBaseLength= PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getInt(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key),5);
        optionOneButton = String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength);



        solo.clickOnButton(optionOneButton);


        Instrumentation.ActivityMonitor monitor = instr.addMonitor(MainActivity.class.getName(), null, false);

        Activity mActivity = instr.waitForMonitor(monitor);
        solo.assertCurrentActivity("main activity should be open", MainActivity.class);

        solo.sleep(3000);
        RelativeLayout rootLayout = (RelativeLayout) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.mainActivityLayout);
        mActivityTitle = (TextView) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.period_title);
        String expectedTitle = getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.on_rest_period).toUpperCase();;
        String actualTitle = mActivityTitle.getText().toString().toUpperCase();
        assertEquals("after extending previously ended period, the active period is work period", expectedTitle, actualTitle);
        mActivityDescription = (TextView) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.description_text);
        String expectedDescription = String.format(mActivity.getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.description_extended),4);
        String actualDescription = mActivityDescription.getText().toString();
        assertEquals("after extending the period the description should say extended", expectedDescription, actualDescription);

        int expectedBgColor = getActivity().getApplicationContext().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.red);
        int actualBgColor = Color.TRANSPARENT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Drawable background = rootLayout.getBackground();
            if (background instanceof ColorDrawable)
                actualBgColor = ((ColorDrawable) background).getColor();
        }

        assertEquals("after 4th extend the background color should change to red", expectedBgColor, actualBgColor);
    }

    public void testExtendButton(){
        solo.sleep(3000);
        String expectedButtonText = getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_current_period);
        String actualButtonText = extendButton.getText().toString();
        assertEquals("the actual extend button text matches the string saved in getActivity().getResources()", expectedButtonText, actualButtonText);
        int expectedButtonTextSize = RReminder.isTablet(getActivity().getApplicationContext()) ? 28 :20;
        int actualButtonTextSize = (int)(extendButton.getTextSize()/getActivity().getResources().getDisplayMetrics().scaledDensity);
        assertEquals("the actual extend button text size matches the value saved in dimensions", expectedButtonTextSize, actualButtonTextSize);
        int expectedColor = getActivity().getApplicationContext().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.black);
        int actualColor = extendButton.getCurrentTextColor();
        assertEquals("the extend button text color is black", expectedColor, actualColor);


        solo.clickOnView(extendButton);
        instr.waitForIdleSync();

        DialogFragment extendDialog = (DialogFragment)getActivity().getSupportFragmentManager().findFragmentByTag("extendDialog");

        //check if cliking hintbutton opens a hintdialog


        assertTrue("extenddialog is an instance of DialogFragment", extendDialog != null);
        assertTrue("extenddialog is visible", extendDialog.getShowsDialog());

        String optionOneButton, optionTwoButton, optionThreeButton;
        optionOneButton = String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength);
        optionTwoButton = String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength*2);
        optionThreeButton = String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength*3);
        assertTrue("the first extend option button should be visible",solo.searchText(optionOneButton, true));

        switch(extendOptionCount){
            case 1:{
                assertFalse(" when option count is one, the second extend option button should not be visible",solo.searchText(optionTwoButton, true));
                assertFalse(" when option count is one, the third extend option button should not be visible",solo.searchText(optionThreeButton, true));
                break;
            }
            case 2:{
                assertTrue(" when option count is two, the second extend option button should be visible",solo.searchText(optionTwoButton, true));
                assertFalse(" when option count is two, the third extend option button should not be visible",solo.searchText(optionThreeButton, true));
                break;
            }
            case 3:{
                assertTrue(" when option count is three, the second extend option button should be visible",solo.searchText(optionTwoButton, true));
                assertTrue(" when option count is three, the third extend option button should be visible",solo.searchText(optionThreeButton, true));
                break;
            }
            default: break;
        }

        solo.clickOnButton("Cancel");

        editor.putInt(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key), 2);
        editor.commit();

        solo.clickOnView(extendButton);
        instr.waitForIdleSync();

        assertTrue(" when option count is two, the second extend option button should be visible",solo.searchText(optionTwoButton, true));
        assertFalse(" when option count is two, the third extend option button should not be visible",solo.searchText(optionThreeButton, true));

        solo.clickOnButton("Cancel");

        editor.putInt(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key), 3);
        editor.commit();

        solo.clickOnView(extendButton);
        instr.waitForIdleSync();

        assertTrue(" when option count is three, the second extend option button should be visible", solo.searchText(optionTwoButton, true));
        assertTrue(" when option count is three, the third extend option button should be visible", solo.searchText(optionThreeButton, true));

        solo.clickOnButton("Cancel");

        editor.putInt(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_options_key), 1);
        editor.commit();

    }

    public void testExtendingWorkPeriod(){
        solo.sleep(3000);

        solo.clickOnView(extendButton);
        instr.waitForIdleSync();



        String optionOneButton, optionTwoButton, optionThreeButton;
        optionOneButton = String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength);



        solo.clickOnButton(optionOneButton);


        Instrumentation.ActivityMonitor monitor = instr.addMonitor(MainActivity.class.getName(), null, false);

        Activity mActivity = instr.waitForMonitor(monitor);
        solo.assertCurrentActivity("main activity should be open", MainActivity.class);

        solo.sleep(3000);
        mActivityTitle = (TextView) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.period_title);
        String expectedTitle = getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.on_work_period).toUpperCase();;
        String actualTitle = mActivityTitle.getText().toString().toUpperCase();
        assertEquals("after extending previously ended period, the active period is work period", expectedTitle, actualTitle);
        mActivityDescription = (TextView) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.description_text);
        String expectedDescription = getActivity().getResources().getString(R.string.description_extended_one_time);
        String actualDescription = mActivityDescription.getText().toString();
        assertEquals("after extending the period the description should say extended", expectedDescription, actualDescription);



    }

    public void testExtendingRestPeriod(){
        Intent restIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        restIntent = new Intent(Intent.ACTION_VIEW);
        restIntent.putExtra(RReminder.PERIOD_TYPE, 2);
        restIntent.putExtra(RReminder.EXTEND_COUNT, 0);
        restIntent.putExtra(RReminder.PLAY_SOUND, false);
        restIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        restIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        clearReferences();
        getActivity().finish();
        setActivity(null);
        setActivityIntent(restIntent);

        solo.sleep(3000);
        Button extendButton1 = (Button) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.button_notification_period_end_extend);

        String optionOneButton, optionTwoButton, optionThreeButton;
        extendBaseLength= PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getInt(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key),5);
        optionOneButton = String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength);

        solo.clickOnView(extendButton1);
        instr.waitForIdleSync();

        solo.clickOnButton(optionOneButton);


        Instrumentation.ActivityMonitor monitor = instr.addMonitor(MainActivity.class.getName(), null, false);

        Activity mActivity = instr.waitForMonitor(monitor);
        solo.assertCurrentActivity("main activity should be open", MainActivity.class);

        solo.sleep(3000);
        mActivityTitle = (TextView) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.period_title);
        String expectedTitle = getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.on_rest_period).toUpperCase();;
        String actualTitle = mActivityTitle.getText().toString().toUpperCase();
        assertEquals("after extending previously ended period, the active period is rest period", expectedTitle, actualTitle);
        mActivityDescription = (TextView) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.description_text);
        String expectedDescription = getActivity().getResources().getString(R.string.description_extended_one_time);
        String actualDescription = mActivityDescription.getText().toString();
        assertEquals("after extending the period the description should say extended", expectedDescription, actualDescription);
    }

    public void testMainActivityRedColorBackground(){
        Intent redIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        redIntent = new Intent(Intent.ACTION_VIEW);
        redIntent.putExtra(RReminder.PERIOD_TYPE, 3);
        redIntent.putExtra(RReminder.EXTEND_COUNT, 3);
        redIntent.putExtra(RReminder.PLAY_SOUND, false);
        redIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        redIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        clearReferences();
        getActivity().finish();
        setActivity(null);
        setActivityIntent(redIntent);

        Button extendButton1 = (Button) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.button_notification_period_end_extend);
        solo.sleep(5000);

        solo.clickOnView(extendButton1);
        instr.waitForIdleSync();



        String optionOneButton, optionTwoButton, optionThreeButton;
        extendBaseLength= PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getInt(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key),5);
        optionOneButton = String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength);


        solo.clickOnButton(optionOneButton);


        Instrumentation.ActivityMonitor monitor = instr.addMonitor(MainActivity.class.getName(), null, false);

        Activity mActivity = instr.waitForMonitor(monitor);
        solo.assertCurrentActivity("main activity should be open", MainActivity.class);

        solo.sleep(3000);
        RelativeLayout rootLayout = (RelativeLayout) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.mainActivityLayout);
        mActivityTitle = (TextView) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.period_title);
        String expectedTitle = getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.on_work_period).toUpperCase();;
        String actualTitle = mActivityTitle.getText().toString().toUpperCase();
        assertEquals("after extending previously ended period, the active period is work period", expectedTitle, actualTitle);
        mActivityDescription = (TextView) mActivity.findViewById(com.colormindapps.rest_reminder_alarm.R.id.description_text);
        String expectedDescription = String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.description_extended),4);
        String actualDescription = mActivityDescription.getText().toString();
        assertEquals("after extending the period the description should say extended", expectedDescription, actualDescription);

        int expectedBgColor = getActivity().getApplicationContext().getResources().getColor(com.colormindapps.rest_reminder_alarm.R.color.red);
        int actualBgColor = Color.TRANSPARENT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Drawable background = rootLayout.getBackground();
            if (background instanceof ColorDrawable)
                actualBgColor = ((ColorDrawable) background).getColor();
        }

        assertEquals("after 4th extend the background color should change to red", expectedBgColor, actualBgColor);
    }


    public void testExtendDialogOrientationChange(){


        SharedPreferences.Editor editor   = preferences.edit();
        editor.putBoolean(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_enable_extend_key), true);
        editor.commit();
        solo.clickOnView(extendButton);
        instr.waitForIdleSync();

        solo.sleep(2000);

        assertTrue("extenddialog is visible", solo.searchText(String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength)));

        solo.sleep(1000);
        clearReferences();
        solo = null;

        //rotate screen
        if(RReminder.isPortrait(getActivity().getApplicationContext())){
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        solo = new Solo(instr,getActivity());
        instr.waitForIdleSync();
        solo.sleep(3000);
        extendBaseLength = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getInt(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_period_extend_length_key),5);
        assertTrue("after orientation change extenddialog is visible", solo.searchText(String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength)));
        //close dialog with press of a button
        solo.clickOnButton(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_close_dialog_text));
        instr.waitForIdleSync();
        solo.sleep(5000);
        assertFalse("after pressing close button dialog should be gone", solo.searchText(String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_button),extendBaseLength)));




    }

    public void testCloseButton(){
        solo.clickOnView(notificationButton);
        instr.waitForIdleSync();
        String notificationTitle = getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.notification_work_end_title);
        String closeButtonText = getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.extend_dialog_close_dialog_text);
        assertFalse(solo.searchText(notificationTitle));
        assertFalse(solo.searchButton(closeButtonText));

        RReminderMobile.stopCounterService(getActivity().getApplicationContext(),2);
        //RReminder.cancelCounterAlarm(getActivity().getApplicationContext(), 2,0);

    }

    public void testCloseButtonManual(){
        editor.putString(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.pref_mode_key), "1");
        editor.commit();

        solo.sleep(3000);

        Intent workIntent;
        customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 30);
        customTime.set(Calendar.SECOND, 0);
        workIntent = new Intent(Intent.ACTION_VIEW);
        workIntent.putExtra(RReminder.PERIOD_TYPE, 1);
        workIntent.putExtra(RReminder.EXTEND_COUNT, 0);
        workIntent.putExtra(RReminder.PLAY_SOUND, false);
        workIntent.putExtra(RReminder.PERIOD_END_TIME, customTime.getTimeInMillis());
        workIntent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);
        clearReferences();
        getActivity().finish();
        setActivity(null);
        setActivityIntent(workIntent);

        solo.sleep(5000);

        assertFalse("in manual mode when notification is visible, service should not be running", RReminderMobile.isCounterServiceRunning(getActivity().getApplicationContext()));

        notificationButton = (Button) getActivity().findViewById(com.colormindapps.rest_reminder_alarm.R.id.notification_button);

        solo.clickOnView(notificationButton);
        instr.waitForIdleSync();
        solo.sleep(2000);

        assertTrue("in manual mode after pressing button to start next period, service should not be running", RReminderMobile.isCounterServiceRunning(getActivity().getApplicationContext()));

    }

    public void testTime24HourFormat(){
        long periodEndTimeMillis = getActivity().getIntent().getExtras().getLong(RReminder.PERIOD_END_TIME);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(periodEndTimeMillis);
        String periodEndTime = DateFormat.format(RReminder.TIME_FORMAT_24H, calendar.getTime()).toString();

        String expectedTimeString = String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.next_period_end_description),"rest",periodEndTime);
        String timeStringFromFunction = RReminder.getTimeString(getActivity().getApplicationContext(), periodEndTimeMillis).toString();

        String actualTimeString = description.getText().toString();

        assertEquals("the format function works as expected", timeStringFromFunction, periodEndTime);
        assertEquals("the description should match", expectedTimeString, actualTimeString);
    }

    public boolean hasEnoughSpaceForDescription(CharSequence text, int height, int width, int textSize ){
        //for now function works for a static text size 20sp
        char[] thin_symbols =  {'1','t','i','I','j','l', '!', '^', '(', ')', '[', ']', '{', '}', ';', ':', '|', ',', '.'  };
        float lengthSum = 0.0f;
        int descriptionLength = text.length();

        boolean isThin;
        //determining the approximate width that would require to display the text in single row
        for( int i = 0; i<descriptionLength;i++){
            isThin = false;
            for(int j=0;j<thin_symbols.length;j++){
                if(text.charAt(i)== thin_symbols[j]){
                    isThin = true;
                    break;
                }
            }
            if(isThin == false){
                lengthSum+=1.0;
            } else {
                lengthSum+=0.2;
            }
        }
        //10.3f value represents the approximate char width for 20sp text size
        float descriptionTextWidth = lengthSum * 9.8f * getActivity().getResources().getDisplayMetrics().scaledDensity;
        int expectedRowCount = (int)descriptionTextWidth / width;
        expectedRowCount+=1;

        int expectedDescriptionHeight = (int)(textSize* getActivity().getResources().getDisplayMetrics().scaledDensity * expectedRowCount + 6f);
        solo.sleep(5000);
        return height>= expectedDescriptionHeight;
    }

    public void clearReferences(){
        //prevent memory leak
        appContext = null;
        resources  = null;
        preferences = null;
        editor = null;
        title = null;
        image = null;
        description = null;
        extendDescription = null;
        notificationButton = null;
        extendButton = null;

        extendOptionCount = 0;
        extendBaseLength = 0;
        expectedOnlineWorkTitle = null;
        expectedOnlineRestTitle = null;
        versionCode = 0;
    }

    /*
    public void testTime12HourFormat(){
        long periodEndTimeMillis = getActivity().getIntent().getExtras().getLong(RReminder.PERIOD_END_TIME);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(periodEndTimeMillis);
        String periodEndTime = DateFormat.format(RReminder.TIME_FORMAT_12H, calendar.getTime()).toString();

        String expectedTimeString = String.format(getActivity().getResources().getString(com.colormindapps.rest_reminder_alarm.R.string.next_rest_period_end_description),periodEndTime);
        String timeStringFromFunction = RReminder.getTimeString(appContext, periodEndTimeMillis).toString();

        String actualTimeString = description.getText().toString();

        assertEquals("the format function works as expected",timeStringFromFunction, periodEndTime);
        assertEquals("the description should match", expectedTimeString, actualTimeString);
    }
    */
}