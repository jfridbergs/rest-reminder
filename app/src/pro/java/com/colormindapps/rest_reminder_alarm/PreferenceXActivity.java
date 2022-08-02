package com.colormindapps.rest_reminder_alarm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;


public class PreferenceXActivity extends AppCompatActivity implements PreferenceActivityLinkedService, PreferenceFragmentCompat.OnPreferenceStartScreenCallback, OnClearDbDialogCloseListener,
		OnPreferencesOpenListener {

	CounterService mService;
	boolean mBound = false;
	CounterService.CounterBinder binder;
	long periodEndTimeValue;
	int periodType, extendCount;




	private final ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className,
									   IBinder service) {
			binder = (CounterService.CounterBinder) service;
			mService = binder.getService();
			mBound = true;
			Bundle data = getDataFromService();
			periodType = data.getInt(RReminder.PERIOD_TYPE);
			extendCount = data.getInt(RReminder.EXTEND_COUNT);
			periodEndTimeValue = data.getLong(RReminder.PERIOD_END_TIME);
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.settings, new PreferenceXFragment())
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
		setContentView(R.layout.activity_preferences_x);
	       if (savedInstanceState == null)
	        {
				if(RReminderMobile.isCounterServiceRunning(this)){
					Intent intent = new Intent(this, CounterService.class);
					bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
				} else {
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.settings, new PreferenceXFragment())
							.commit();
				}
	            // Display the fragment as the main content.

	        }

	}

	@Override
	public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragmentCompat,
										   PreferenceScreen preferenceScreen) {
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
	protected void onStart(){
		super.onStart();

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


	@Override
	public void deleteDB() {
		SessionsViewModel mSessionsViewModel = new ViewModelProvider(this).get(SessionsViewModel.class);
		mSessionsViewModel.deleteOlder(0);
	}
}
