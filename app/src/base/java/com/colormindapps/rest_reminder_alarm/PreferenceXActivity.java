package com.colormindapps.rest_reminder_alarm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;


public class PreferenceXActivity extends AppCompatActivity implements PreferenceActivityLinkedService, PreferenceFragmentCompat.OnPreferenceStartScreenCallback{

	CounterService mService;
	boolean mBound = false;
	CounterService.CounterBinder binder;
	long periodEndTimeValue;
	int periodType, extendCount;

	String debug = "PREFERENCE_ACTIVITY_X";

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className,
									   IBinder service) {
			binder = (CounterService.CounterBinder) service;
			mService = binder.getService();
			mBound = true;
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.settings, new MyPreferenceXFragment())
					.commit();

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d(debug, "onCreate");
		setContentView(R.layout.activity_preferences_x);
	       if (savedInstanceState == null)
	        {
				if(RReminderMobile.isCounterServiceRunning(this)){
					Intent intent = new Intent(this, CounterService.class);
					bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
				} else {
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.settings, new MyPreferenceXFragment())
							.commit();
				}
	            // Display the fragment as the main content.

	        }

	}

	@Override
	public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragmentCompat,
										   PreferenceScreen preferenceScreen) {
		Log.d(debug, "callback called to attach the preference sub screen");
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		PreferenceXSubScreenFragment fragment = PreferenceXSubScreenFragment.newInstance("Advanced Settings Subscreen");
		Bundle args = new Bundle();
		//Defining the sub screen as new root for the  subscreen
		args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.getKey());
		fragment.setArguments(args);
		ft.replace(R.id.settings, fragment, preferenceScreen.getKey());
		ft.addToBackStack(null);
		ft.commit();
		return true;
	}

	@Override
	protected void onStop(){
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
		if (mBound) {
			return mService.getData();
		}
		return null;
	}

}
