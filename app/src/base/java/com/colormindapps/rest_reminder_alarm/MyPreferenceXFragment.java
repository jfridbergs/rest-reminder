package com.colormindapps.rest_reminder_alarm;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MyPreferenceXFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener{
	public String reminderModeKey, workPeriodLengthKey, restPeriodLengthKey, extendCountKey, extendBaseLengthKey, workPeriodSoundKey, restPeriodSoundKey, enableColorizedNotificationsKey, enableExtendKey, prefScreenExtendKey;
	public String testModeSummary,testWorkLenghtSummary, testRestLenghtSummary, testWorkAudioSummary, testRestAudioSummary, testExtendCountSummary, testExtendLengthSummary;
	Uri originalWorkUri, originalRestUri, newWorkUri, newRestUri;
	SharedPreferences sharedPreferences;

	SharedPreferences.Editor editor;
	Preference workSoundPreference, restSoundPreference;
	Context context;
	private PreferenceActivityLinkedService parentActivity;


	String debug = "PreferenceFragment X";

	public static MyPreferenceXFragment newInstance() {
		return new MyPreferenceXFragment();
	}

	private void setParentActivity(PreferenceActivityLinkedService activity){
		parentActivity = activity;
	}


	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		Log.d(debug, "onCreatePreference()");
		setPreferencesFromResource(R.xml.preferences_x, rootKey);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		editor = sharedPreferences.edit();
		int value;
		context = getActivity();

		reminderModeKey = getString(R.string.pref_mode_key);
		prefScreenExtendKey =  getString(R.string.pref_screen_period_extend_key);
		enableExtendKey = getString(R.string.pref_enable_extend_key);
		enableColorizedNotificationsKey = getString(R.string.pref_colorize_notifications_key);

		final SwitchPreference customPreference = (SwitchPreference) findPreference(enableExtendKey);
		final Preference customSettings = (Preference) findPreference(prefScreenExtendKey);
		// First time loading the preference screen, we check the saved settings and enable/disable the custom settings, based on the custom check box
		//get the customSettings value from shared preferences
		if (sharedPreferences.getBoolean(enableExtendKey, true)) {
			customPreference.setChecked(true);
			customSettings.setEnabled(true);
		} else {
			customPreference.setChecked(false);
			customSettings.setEnabled(false);
		}

		Preference preference = findPreference(reminderModeKey);
		// Set summary to be the user-description for the selected value
		value = Integer.parseInt(sharedPreferences.getString(reminderModeKey,"0"));
		switch(value){
			case 0: {
				preference.setTitle(getString(R.string.pref_mode_title_first_part,getString(R.string.pref_mode_title_automatic)));
				preference.setSummary(getString(R.string.pref_mode_summary_automatic));
				break;
			}
			case 1:{
				preference.setTitle(getString(R.string.pref_mode_title_first_part,getString(R.string.pref_mode_title_manual)));
				preference.setSummary(getString(R.string.pref_mode_summary_manual));
				break;
			}
			default: break;
		}

		testModeSummary = preference.getSummary().toString();

		//display the preference for enabling notification color only on devices with oreo or newer
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			PreferenceCategory mCategory = (PreferenceCategory) findPreference(getString(R.string.pref_category_basic_settings_key));
			mCategory.removePreference(findPreference(enableColorizedNotificationsKey));
		} else {

			if(RReminderMobile.isCounterServiceRunning(context)){
				getPreferenceManager().findPreference(enableColorizedNotificationsKey).setEnabled(false);
			} else {
				getPreferenceManager().findPreference(enableColorizedNotificationsKey).setEnabled(true);
			}
		}




		if(RReminderMobile.isCounterServiceRunning(context)){
			getPreferenceManager().findPreference(getString(R.string.pref_show_is_on_icon_key)).setEnabled(false);
		} else {
			getPreferenceManager().findPreference(getString(R.string.pref_show_is_on_icon_key)).setEnabled(true);
		}
		Vibrator vib = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

		if(vib.hasVibrator()){
			getPreferenceManager().findPreference(getString(R.string.pref_enable_vibrate_key)).setEnabled(true);
		} else {
			sharedPreferences.edit().putBoolean(getString(R.string.pref_enable_vibrate_key), false).commit();
			getPreferenceManager().findPreference(getString(R.string.pref_enable_vibrate_key)).setEnabled(false);
		}
	}



	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}


	
    

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    	Preference preference = findPreference(key);
    	int value;

		//updating preference summary after preferences are changed
        if (key.equals(reminderModeKey)) {
            // Set summary to be the user-description for the selected value
            value = Integer.parseInt(sharedPreferences.getString(key,"0"));
            switch(value){
        	case 0:{
				preference.setTitle(getString(R.string.pref_mode_title_first_part,getString(R.string.pref_mode_title_automatic)));
				preference.setSummary(getString(R.string.pref_mode_summary_automatic));
				break;
			}
        	case 1:{
				preference.setTitle(getString(R.string.pref_mode_title_first_part,getString(R.string.pref_mode_title_manual)));
				preference.setSummary(getString(R.string.pref_mode_summary_manual));
				break;
			}
        	default: break;
            }

			testModeSummary = preference.getSummary().toString();
        }   else if (key.equals(enableExtendKey)){
			Preference extendSettings = findPreference(prefScreenExtendKey);
			if (sharedPreferences.getBoolean(key, true)) {
				extendSettings.setEnabled(true);
			} else {
				extendSettings.setEnabled(false);
			}
		}


    }

	@Override
	public void onDestroy(){
		super.onDestroy();
		Intent testIntent = new Intent();
		testIntent.setAction(RReminder.CUSTOM_INTENT_TEST_PREFERENCES_MODE);
		testIntent.putExtra(RReminder.PREFERENCE_MODE_SUMMARY, testModeSummary);
		getActivity().sendBroadcast(testIntent);
	}
    
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

	@TargetApi(23)
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			setParentActivity((PreferenceActivityLinkedService) getActivity());
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement PreferenceActivityLinkedService");
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (Build.VERSION.SDK_INT < 23) {
			try {
				setParentActivity((PreferenceActivityLinkedService)activity);
			} catch (ClassCastException e) {
				throw new ClassCastException(activity.toString() + " must implement PreferenceActivityLinkedService");
			}
		}
	}


	
}
