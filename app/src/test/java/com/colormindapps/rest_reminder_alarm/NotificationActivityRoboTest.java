package com.colormindapps.rest_reminder_alarm;

import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowDrawable;
import org.robolectric.util.ActivityController;

import static org.junit.Assert.assertEquals;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class NotificationActivityRoboTest {

    private NotificationActivity nActivity, nActivity1;
    private ImageView image, image1;
    private Context context;
    private ActivityController<NotificationActivity> controller;

    @Before
    public void setup(){

        context = ShadowApplication.getInstance().getApplicationContext();

        Intent intent = new Intent(context, NotificationActivity.class);
        intent.putExtra(RReminder.PERIOD_TYPE,1);
        intent.putExtra(RReminder.EXTEND_COUNT,0);
        intent.putExtra(RReminder.PLAY_SOUND,false);
        intent.putExtra(RReminder.PERIOD_END_TIME,0);
        intent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);

        controller = Robolectric.buildActivity(NotificationActivity.class).withIntent(intent).create().start();
        nActivity = controller.get();


        image = (ImageView) nActivity.findViewById(R.id.notification_image);
    }

    @Test
    public void testImageId(){
        ShadowDrawable shadowDrawable = Shadows.shadowOf(image.getDrawable());
        assertEquals(R.drawable.img_coffee_mug, shadowDrawable.getCreatedFromResId());

        Intent intent = new Intent(context, NotificationActivity.class);
        intent.putExtra(RReminder.PERIOD_TYPE,2);
        intent.putExtra(RReminder.EXTEND_COUNT, 0);
        intent.putExtra(RReminder.PLAY_SOUND,false);
        intent.putExtra(RReminder.PERIOD_END_TIME,0);
        intent.putExtra(RReminder.REDIRECT_SCREEN_OFF, false);

        controller = Robolectric.buildActivity(NotificationActivity.class).withIntent(intent).create().start();
        nActivity1 = controller.get();
        image1 = (ImageView) nActivity1.findViewById(R.id.notification_image);

        shadowDrawable = Shadows.shadowOf(image1.getDrawable());
        assertEquals(R.drawable.img_gears, shadowDrawable.getCreatedFromResId());





    }
}
