package com.colormindapps.rest_reminder_alarm.wear;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.colormindapps.rest_reminder_alarm.R;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;
import com.colormindapps.rest_reminder_alarm.shared.ReminderStatus;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Calendar;

/**
 * Created by ingressus on 13/01/2017.
 */

public class WearPeriodService extends JobIntentService implements
        GoogleApiClient.ConnectionCallbacks,
        DataApi.DataListener,
        CapabilityApi.CapabilityListener,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient mGoogleApiClient;
    private Node connectedNode;

    private String debug = "WEAR_PERIOD_SERVICE";
    Intent periodIntent;
    private NotificationCompat.Action extendAction, endPeriodAction;

    ReminderStatus statusData;

    static final int JOB_ID = 1001;

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, WearPeriodService.class, JOB_ID, work);
    }

    @Override
    public IBinder onBind(Intent intent){
        return mBinder;
    }

    private final IBinder mBinder = new CounterBinder();
    public class CounterBinder extends Binder {
        WearPeriodService getService(){
            return WearPeriodService.this;
        }
    }


    @Override
    protected void onHandleWork(Intent intent) {

        periodIntent = intent;

    }

    public void doServiceWork(){
        Log.d(debug, "doServiceWork()");
        int type = periodIntent.getExtras().getInt(RReminder.PERIOD_TYPE);

        int extendCount = periodIntent.getExtras().getInt(RReminder.EXTEND_COUNT);
        long finishedPeriodTime = periodIntent.getExtras().getLong(RReminder.PERIOD_END_TIME);
        Log.d(debug, "values: type: "+type + ", extendcount: "+extendCount);
        Log.d( debug, "period end time value: " + periodIntent.getExtras().getLong(RReminder.PERIOD_END_TIME));

        //checking if app is running on a main device and if it has more recent period running than the one on wear device
        //if thats the case, the wear app will skip its own data, which was set by periodmanager and wait for more recent main app period to end
        Log.d(debug, "checking if app is running on a main device with never period values");
        if(statusData!=null && statusData.isMobileOn() && statusData.getPeriodEndTime()> finishedPeriodTime && Math.abs(finishedPeriodTime-statusData.getPeriodEndTime())>60000){
            updateReminderStatus(statusData.getPeriodType(),statusData.getPeriodEndTime(),statusData.getExtendCount(), statusData.isMobileOn(), true);

        } else {
            int nextType = RReminder.getNextType(type);
            Log.d(debug, "period type: "+nextType);
            long nextPeriodEndTime = RReminder.getNextPeriodEndTime(this, nextType, Calendar.getInstance().getTimeInMillis(),1,0L);

            //setting up alarm for next period end
            new WearPeriodManager(this).setPeriod(nextType,nextPeriodEndTime,0);
            Log.d(debug, "updating reminder status with new period data");
            updateReminderStatus(nextType,nextPeriodEndTime,0, statusData.isMobileOn(), true);

            //sending broadcast to update mainactivity
            Intent intentNext = new Intent();
            intentNext.setAction(RReminder.WEAR_ACTION_START_NEXT_PERIOD);
            intentNext.putExtra(RReminder.PERIOD_TYPE,nextType);
            intentNext.putExtra(RReminder.PERIOD_END_TIME,nextPeriodEndTime);
            intentNext.putExtra(RReminder.EXTEND_COUNT,0);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intentNext);

            launchNotification(type, extendCount,nextPeriodEndTime, nextType);
        }


    }

    public void launchNotification(int type, int extendCount, long nextPeriodEndTime, int typeOFF){

        int notificationId = 001;
        int pIntentId = (int)Calendar.getInstance().getTimeInMillis();
        String work, rest;
        work = getString(R.string.work);
        rest = getString(R.string.rest);

        //NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();

        //EXTEND ACTION
        //extend period action intent
        //check if extend option is enabled on parent device
        if(RReminder.isExtendEnabled(this.getApplicationContext())){
            Intent extendIntent = new Intent(this,WearMainActivity.class);
            extendIntent.setAction(RReminder.ACTION_WEAR_NOTIFICATION_EXTEND);

            // type of currently running period, that needs to be cancelled
            extendIntent.putExtra(RReminder.PERIOD_TYPE,typeOFF);
            //period type, that needs to extended
            extendIntent.putExtra(RReminder.EXTENDED_PERIOD_TYPE, type);
            //data needed to cancel the current running period
            extendIntent.putExtra(RReminder.PERIOD_END_TIME,nextPeriodEndTime);
            extendIntent.putExtra(RReminder.EXTEND_COUNT,extendCount);
            PendingIntent extendPendingIntent = PendingIntent.getActivity(this,pIntentId,extendIntent,0);


            //action itself
             extendAction = new NotificationCompat.Action.Builder(
                    R.drawable.ic_notify_wear_extend, getString(R.string.notify_extend),extendPendingIntent).build();
        }



        //END PERIOD ACTION

        //extend period action intent
        //check, if endPeriod option is enabled on parent device
        if(RReminder.isEndPeriodEnabled(this.getApplicationContext())){
            Intent endPeriodIntent = new Intent(this,WearMainActivity.class);
            endPeriodIntent.setAction(RReminder.ACTION_WEAR_NOTIFICATION_START_NEXT);
            //required data to cancel current running period
            endPeriodIntent.putExtra(RReminder.PERIOD_TYPE, typeOFF);
            endPeriodIntent.putExtra(RReminder.PERIOD_END_TIME, nextPeriodEndTime);

            PendingIntent endPeriodPendingIntent = PendingIntent.getActivity(this,pIntentId+5,endPeriodIntent,0);


            //action itself
            endPeriodAction = new NotificationCompat.Action.Builder(
                    R.drawable.ic_notify_end_period, getString(R.string.notify_end_period),endPeriodPendingIntent).build();
        }



        //TURN_OFF ACTION

        //Turn off action intent
        Intent turnOffIntent = new Intent(this, WearMainActivity.class);
        turnOffIntent.setAction(RReminder.ACTION_TURN_OFF);
        turnOffIntent.putExtra(RReminder.PERIOD_TYPE, typeOFF);
        turnOffIntent.putExtra(RReminder.EXTEND_COUNT,0);
        turnOffIntent.putExtra(RReminder.PERIOD_END_TIME, nextPeriodEndTime);
        PendingIntent turnOffPIntent =
                PendingIntent.getActivity(this, pIntentId+10, turnOffIntent, 0);

        NotificationCompat.Action turnOffAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_notify_turn_off, getString(R.string.notify_turn_off), turnOffPIntent).build();


/*
        NotificationCompat.Action.WearableExtender actionExtender =
                new NotificationCompat.Action.WearableExtender()
                        .setHintLaunchesActivity(true)
                        .setHintDisplayActionInline(true);

        wearableExtender.addAction(turnOffAction.extend(actionExtender).build());
        wearableExtender.addAction( extendAction.build());

*/





        //setting up main notification

        NotificationCompat.BigTextStyle bigTextStyle = new android.support.v7.app.NotificationCompat.BigTextStyle();
        String eventDescription;
        // Build intent for notification content
        Intent mainIntent = new Intent(this, WearMainActivity.class);

        PendingIntent mainPendingIntent =
                PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, RReminder.CHANNEL_PERIOD_END_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            // Enables launching app in Wear 2.0 while keeping the old Notification Style behavior.
            android.support.v7.app.NotificationCompat.Action mainAction = new android.support.v7.app.NotificationCompat.Action.Builder(
                    R.mipmap.ic_launcher,
                    "Open",
                    mainPendingIntent)
                    .build();

            notificationBuilder.addAction(mainAction);

        } else {
            // Wear 1.+ still functions the same, so we set the main content intent.
            notificationBuilder.setContentIntent(mainPendingIntent);
        }

        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);

        //adding actions to main notification
        //notificationBuilder.extend(wearableExtender);
        notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);

        notificationBuilder.addAction(turnOffAction);
        if(RReminder.isExtendEnabled(this.getApplicationContext())){
            notificationBuilder.addAction( extendAction);
        }
        if(RReminder.isEndPeriodEnabled(this.getApplicationContext())){
            notificationBuilder.addAction(endPeriodAction);
        }








        switch(type){
            case 1:case 3: {
                bigTextStyle.setBigContentTitle((getString(R.string.notify_work_period_end_ticker_message)));
                bigTextStyle.bigText(getString(R.string.notify_wear_big_text, rest, RReminder.getTimeString(this, nextPeriodEndTime)));
                notificationBuilder.setContentTitle(getString(R.string.notify_work_period_end_ticker_message));
                notificationBuilder.setColor(ContextCompat.getColor(this,R.color.rest));

                eventDescription = getString(R.string.notify_work_period_end_message_part_one) + " ";
                if (extendCount>0){
                    eventDescription+=String.format(getString(R.string.notify_period_end_message_extend), extendCount);
                }
                eventDescription+=getString(R.string.notify_work_period_end_message_part_two);
                notificationBuilder.setContentText(eventDescription);
                break;
            }
            case 2:case 4: {
                bigTextStyle.setBigContentTitle((getString(R.string.notify_rest_period_end_ticker_message)));
                bigTextStyle.bigText(getString(R.string.notify_wear_big_text, work, RReminder.getTimeString(this, nextPeriodEndTime)));
                notificationBuilder.setContentTitle(getString(R.string.notify_rest_period_end_ticker_message));
                notificationBuilder.setColor(ContextCompat.getColor(this,R.color.work));

                eventDescription = getString(R.string.notify_rest_period_end_message_part_one) + " ";
                if (extendCount>0){
                    eventDescription+=String.format(getString(R.string.notify_period_end_message_extend), extendCount);
                }
                eventDescription+=getString(R.string.notify_rest_period_end_message_part_two);
                notificationBuilder.setContentText(eventDescription);
                break;
            }
            default: break;
        }

        notificationBuilder.setStyle(bigTextStyle);
        notificationBuilder.setDefaults(Notification.DEFAULT_ALL);


        /*
         notificationBuilder.setVibrate(new long[] {0,300});
        notificationBuilder.setSound(RingtoneManager.getActualDefaultRingtoneUri(this.getApplicationContext(), RingtoneManager.TYPE_RINGTONE));
        Log.d(debug, "notification sound uri: "+ RingtoneManager.getActualDefaultRingtoneUri(this.getApplicationContext(), RingtoneManager.TYPE_RINGTONE));
        */

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Issue the notification with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());
    }


    @Override
    public void onCreate() {
        Log.d(debug, "onCreate");
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        Log.d(debug, "onDestroy");
        if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.CapabilityApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(debug, "onConnected(): Successfully connected to Google API client");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.CapabilityApi.addListener(
                mGoogleApiClient, this, Uri.parse("wear://"), CapabilityApi.FILTER_REACHABLE);

        getConnectedNode();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(debug, "onConnectionSuspended(): Connection to Google API client was suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(debug, "onConnectionFailed(): Failed to connect, with result: " + result);
    }

    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        Log.d(debug, "onCapabilityChanged: " + capabilityInfo);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(debug, "onDataChanged(): " + dataEvents);

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {

            }
        }
    }

    private void getData(final String pathToContent) {

        Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
            @Override
            public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {

                String nodeID;
                final String nodeSource;
                if (connectedNode != null){
                    nodeID = connectedNode.getId();
                    nodeSource = "connected";
                } else {
                    nodeID = getLocalNodeResult.getNode().getId();
                    nodeSource = "local";
                }

                Uri uri = new Uri.Builder()
                        .scheme(PutDataRequest.WEAR_URI_SCHEME)
                        .path(pathToContent)
                        .authority(nodeID)
                        .build();

                Wearable.DataApi.getDataItem(mGoogleApiClient, uri)
                        .setResultCallback(
                                new ResultCallback<DataApi.DataItemResult>() {
                                    @Override
                                    public void onResult(DataApi.DataItemResult dataItemResult) {

                                        if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
                                            DataMap data = DataMap.fromByteArray(dataItemResult.getDataItem().getData());
                                            Log.d(debug, "getData(), node source: "+nodeSource);
                                            statusData = setReminderData(data.getInt(RReminder.PERIOD_TYPE), data.getLong(RReminder.PERIOD_END_TIME), data.getInt(RReminder.EXTEND_COUNT), data.getInt(RReminder.DATA_API_SOURCE),data.getBoolean(RReminder.DATA_API_MOBILE_ON));

                                        }
                                        doServiceWork();
                                    }
                                }
                        );
            }
        });
    }

    private void getConnectedNode()
    {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                for (Node node : nodes.getNodes()) {
                    connectedNode = node;
                }
                getData(RReminder.DATA_API_REMINDER_STATUS_PATH);
            }
        });
    }

    public ReminderStatus setReminderData(int periodType, long periodEndTime, int extendCount, int commandSource, boolean mobileOn){
        return new ReminderStatus(periodType, periodEndTime, extendCount, commandSource, mobileOn, true);

    }

    public void updateReminderStatus(int type, long periodEndTimeValue, int extendCount, boolean mobileOn, boolean wearOn){
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient,RReminder.createStatusData(RReminder.DATA_API_SOURCE_WEAR_SERVICE,type,periodEndTimeValue,extendCount, mobileOn, wearOn));
    }

}
