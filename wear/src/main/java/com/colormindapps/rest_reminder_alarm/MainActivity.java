package com.colormindapps.rest_reminder_alarm;

import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends WearableActivity implements
        GoogleApiClient.ConnectionCallbacks,
        DataApi.DataListener,
        MessageApi.MessageListener,
        CapabilityApi.CapabilityListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView mTitle, mDescription;
    private GoogleApiClient mGoogleApiClient;
    Typeface font, titleFont, swipeFont, timerFont;
    private int periodType = 0, extendCount = -1;
    private long periodEndTimeValue = 0;

    private String debug = "WEAR_MAIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(debug,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        font = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Lt.otf");
        titleFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-ThCn.otf");
        timerFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Lt.otf");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTitle = (TextView) findViewById(R.id.title);
        mDescription = (TextView) findViewById(R.id.description);

        mDescription.setTypeface(font);
        mTitle.setTypeface(titleFont);

        mTitle.setText(getString(R.string.wear_main_title_off));
        mDescription.setText(getString(R.string.extend));


    }

    @Override
    protected void onResume(){
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            Wearable.CapabilityApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }


        super.onPause();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateStatus(int type, long endValue, int extendCount){
        this.periodType = type;
        this.periodEndTimeValue = endValue;
        this.extendCount = extendCount;
        updateDisplay();
    }

    private void updateDisplay() {


        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mTitle.setTextColor(getResources().getColor(android.R.color.white));
            mDescription.setTextColor(getResources().getColor(android.R.color.white));

        } else {
            mContainerView.setBackground(null);
            mTitle.setTextColor(getResources().getColor(android.R.color.black));
            mDescription.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(debug, "onConnected(): Successfully connected to Google API client");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.CapabilityApi.addListener(
                mGoogleApiClient, this, Uri.parse("wear://"), CapabilityApi.FILTER_REACHABLE);
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
    public void onMessageReceived(MessageEvent event) {
        Log.d(debug, "onMessageReceived: " + event);
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

                if(item.getUri().getPath().compareTo("/reminder_status")==0){
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    Log.d(debug, "extend counter value: "+dataMap.getInt(RReminder.EXTEND_COUNT));
                    updateStatus(dataMap.getInt(RReminder.PERIOD_TYPE), dataMap.getLong(RReminder.PERIOD_END_VALUE), dataMap.getInt(RReminder.EXTEND_COUNT));
                }

            }
        }
    }

}
