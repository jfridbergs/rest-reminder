package com.colormindapps.rest_reminder_alarm.wear;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.colormindapps.rest_reminder_alarm.R;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class CommandsActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        DataApi.DataListener,
        CapabilityApi.CapabilityListener,
        GoogleApiClient.OnConnectionFailedListener{

    private int periodType, extendCount;
    private int colorRest, colorWork, colorRed;
    private RelativeLayout mContainerView;
    private String debug = "COMMAND_ACTIVITY";

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commands);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mContainerView = (RelativeLayout) findViewById(R.id.container);

        colorWork = ContextCompat.getColor(CommandsActivity.this,R.color.work);
        colorRest = ContextCompat.getColor(CommandsActivity.this,R.color.rest);
        colorRed = ContextCompat.getColor(CommandsActivity.this,R.color.red);

        if(getIntent().getExtras()!=null){
            periodType = getIntent().getExtras().getInt(RReminder.PERIOD_TYPE);
        }
        setBgColor();

    }

    public void setBgColor(){
        switch(periodType){
            case 1: mContainerView.setBackgroundColor(colorWork); break;
            case 2: mContainerView.setBackgroundColor(colorRest); break;
            case 3: {
                if(extendCount>3){
                    mContainerView.setBackgroundColor(colorRed);
                } else {
                    mContainerView.setBackgroundColor(colorWork);
                }
                break;
            }
            case 4: {
                if(extendCount>3){
                    mContainerView.setBackgroundColor(colorRed);
                } else {
                    mContainerView.setBackgroundColor(colorRest);
                }
                break;
            }
            default: break;
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        Log.d(debug,"onPause");
        if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.CapabilityApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        super.onPause();
        finish();
    }

    public void closeCommands(View view){

        Intent intent = new Intent(this, WearMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void extendCurrent(View view){
        Intent intent = new Intent(this, WearMainActivity.class);
        intent.setAction(RReminder.ACTION_WEAR_COMMANDS_EXTEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void startNext(View view){
        Intent intent = new Intent(this, WearMainActivity.class);
        intent.setAction(RReminder.ACTION_WEAR_COMMANDS_START_NEXT);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void updateActivityStatus(int type, int extendCount){
        this.periodType = type;
        this.extendCount = extendCount;
        setBgColor();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(debug, "onConnected(): Successfully connected to Google API client");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.CapabilityApi.addListener(
                mGoogleApiClient, this, Uri.parse("wear://"), CapabilityApi.FILTER_REACHABLE);

        getData(RReminder.DATA_API_REMINDER_STATUS_PATH);
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
                DataItem item = event.getDataItem();

                if(item.getUri().getPath().compareTo(RReminder.DATA_API_REMINDER_STATUS_PATH)==0){
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    updateActivityStatus(dataMap.getInt(RReminder.PERIOD_TYPE), dataMap.getInt(RReminder.EXTEND_COUNT));
                }



            }
        }
    }


    private void getData(final String pathToContent) {
        Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
            @Override
            public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {

                Uri uri = new Uri.Builder()
                        .scheme(PutDataRequest.WEAR_URI_SCHEME)
                        .path(pathToContent)
                        .authority(getLocalNodeResult.getNode().getId())
                        .build();

                Wearable.DataApi.getDataItem(mGoogleApiClient, uri)
                        .setResultCallback(
                                new ResultCallback<DataApi.DataItemResult>() {
                                    @Override
                                    public void onResult(DataApi.DataItemResult dataItemResult) {

                                        if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
                                            DataMap data = DataMap.fromByteArray(dataItemResult.getDataItem().getData());
                                            updateActivityStatus(data.getInt(RReminder.PERIOD_TYPE), data.getInt(RReminder.EXTEND_COUNT));


                                        }
                                    }
                                }
                        );
            }
        });
    }
}
