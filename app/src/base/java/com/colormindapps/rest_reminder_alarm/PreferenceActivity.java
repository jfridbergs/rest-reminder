package com.colormindapps.rest_reminder_alarm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;


public class PreferenceActivity extends FragmentActivity implements PreferenceActivityLinkedService{

	CounterService mService;
	boolean mBound = false;
	CounterService.CounterBinder binder;
	long periodEndTimeValue;
	int periodType, extendCount;
	String debug = "RREMINDER_PREFERENCE_ACTIVITY";

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className,
									   IBinder service) {
			binder = (CounterService.CounterBinder) service;
			mService = binder.getService();
			mBound = true;
			getFragmentManager().beginTransaction().replace(android.R.id.content, MyPreferenceFragment.newInstance()).commit();

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	       if (savedInstanceState == null)
	        {
				if(RReminderMobile.isCounterServiceRunning(this)){
					Intent intent = new Intent(this, CounterService.class);
					bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
				} else {
					getFragmentManager().beginTransaction().replace(android.R.id.content, MyPreferenceFragment.newInstance()).commit();
				}
	            // Display the fragment as the main content.

	        }

	}

	@Override
	protected void onStop(){
		Log.d(debug, "onStop");
		unbindFromService();
		super.onStop();
	}

	public void unbindFromService(){
		if(mBound){
			unbindService(mConnection);
			mBound = false;
		}
	}
	@Override
	public Bundle getDataFromService() {
		Log.d(debug, "getDataFromService");
		if (mBound) {
			return mService.getData();
		}
		return null;
	}

}
