package com.colormindapps.rest_reminder_alarm;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import androidx.preference.SwitchPreference;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.Objects;


public class PreferenceXFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener{
	public String reminderModeKey, workPeriodLengthKey, restPeriodLengthKey,enableColorizedNotificationsKey, enableExtendKey,changeSummaryKey,extendEnabledKey,startNextEnabledKey,extendBaseLengthKey, prefScreenExtendKey, clearDbKey;
	public String testModeSummary;
	SharedPreferences sharedPreferences;
	String wearWorkLength, wearRestLength, wearReminderMode;
	int wearExtendLength;
	boolean wearExtendEnabled, wearStartNextEnabled;

	Context context;
	private PreferenceActivityLinkedService parentActivity;



	public static PreferenceXFragment newInstance() {
		return new PreferenceXFragment();
	}

	private void setParentActivity(PreferenceActivityLinkedService activity){
		parentActivity = activity;
	}


	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.preferences_x, rootKey);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
		int value;
		context = getActivity();

		reminderModeKey = getString(R.string.pref_mode_key);
		prefScreenExtendKey =  getString(R.string.pref_screen_period_extend_key);
		enableExtendKey = getString(R.string.pref_enable_extend_key);
		enableColorizedNotificationsKey = getString(R.string.pref_colorize_notifications_key);
		clearDbKey = getString(R.string.pref_delete_recorded_sessions_key);

		final SwitchPreference customPreference = findPreference(enableExtendKey);
		final Preference customSettings = findPreference(prefScreenExtendKey);
		// First time loading the preference screen, we check the saved settings and enable/disable the custom settings, based on the custom check box
		//get the customSettings value from shared preferences
		if (sharedPreferences.getBoolean(enableExtendKey, true)) {
			assert customPreference != null;
			customPreference.setChecked(true);
			assert customSettings != null;
			customSettings.setEnabled(true);
		} else {
			assert customPreference != null;
			customPreference.setChecked(false);
			assert customSettings != null;
			customSettings.setEnabled(false);
		}

		Preference preference = findPreference(reminderModeKey);
		// Set summary to be the user-description for the selected value
		value = Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString(reminderModeKey, "0")));
		switch(value){
			case 0: {
				assert preference != null;
				preference.setTitle(getString(R.string.pref_mode_title_first_part,getString(R.string.pref_mode_title_automatic)));
				preference.setSummary(getString(R.string.pref_mode_summary_automatic));
				break;
			}
			case 1:{
				assert preference != null;
				preference.setTitle(getString(R.string.pref_mode_title_first_part,getString(R.string.pref_mode_title_manual)));
				preference.setSummary(getString(R.string.pref_mode_summary_manual));
				break;
			}
			default: break;
		}

		assert preference != null;
		testModeSummary = preference.getSummary().toString();

		//display the preference for enabling notification color only on devices with oreo or newer
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			PreferenceCategory mCategory =  findPreference(getString(R.string.pref_category_basic_settings_key));
			assert mCategory != null;
			mCategory.removePreference(findPreference(enableColorizedNotificationsKey));
		} else {
			preference = getPreferenceManager().findPreference(enableColorizedNotificationsKey);
			assert preference != null;
			preference.setEnabled(!RReminderMobile.isCounterServiceRunning(context));
		}

		preference = getPreferenceManager().findPreference(getString(R.string.pref_show_is_on_icon_key));
		assert preference != null;
		preference.setEnabled(!RReminderMobile.isCounterServiceRunning(context));
		Vibrator vib = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);

		assert vib != null;
		preference = getPreferenceManager().findPreference(getString(R.string.pref_enable_vibrate_key));
		if(vib.hasVibrator()){
			assert preference != null;
			preference.setEnabled(true);
		} else {
			sharedPreferences.edit().putBoolean(getString(R.string.pref_enable_vibrate_key), false).commit();
			assert preference != null;
			preference.setEnabled(false);
		}
	}



	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		workPeriodLengthKey = getString(R.string.pref_work_period_length_key);
		restPeriodLengthKey = getString(R.string.pref_rest_period_length_key);
		extendBaseLengthKey = getString(R.string.pref_period_extend_length_key);
		extendEnabledKey = getString(R.string.pref_enable_extend_key);
		startNextEnabledKey = getString(R.string.pref_end_period_key);
		changeSummaryKey = getString(R.string.pref_mode_key);
	}


	
    

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    	Preference preference = findPreference(key);
    	int value;

		//updating the preferences values on wear device
		wearWorkLength = sharedPreferences.getString(workPeriodLengthKey, RReminder.DEFAULT_WORK_PERIOD_STRING);
		wearRestLength = sharedPreferences.getString(restPeriodLengthKey, RReminder.DEFAULT_REST_PERIOD_STRING);
		wearExtendLength = sharedPreferences.getInt(extendBaseLengthKey, RReminder.DEFAULT_EXTEND_COUNT);
		wearExtendEnabled = sharedPreferences.getBoolean(extendEnabledKey, true);
		wearStartNextEnabled = sharedPreferences.getBoolean(startNextEnabledKey, true);
		wearReminderMode = sharedPreferences.getString(changeSummaryKey, "0");



		//updating preference summary after preferences are changed
        if (key.equals(reminderModeKey)) {
            // Set summary to be the user-description for the selected value
            value = Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString(key, "0")));
            switch(value){
        	case 0:{
				assert preference != null;
				preference.setTitle(getString(R.string.pref_mode_title_first_part,getString(R.string.pref_mode_title_automatic)));
				preference.setSummary(getString(R.string.pref_mode_summary_automatic));
				break;
			}
        	case 1:{
				assert preference != null;
				preference.setTitle(getString(R.string.pref_mode_title_first_part,getString(R.string.pref_mode_title_manual)));
				preference.setSummary(getString(R.string.pref_mode_summary_manual));
				break;
			}
        	default: break;
            }

			assert preference != null;
			testModeSummary = preference.getSummary().toString();
        }   else if (key.equals(enableExtendKey)){
			Preference extendSettings = findPreference(prefScreenExtendKey);
			if (sharedPreferences.getBoolean(key, true)) {
				assert extendSettings != null;
				extendSettings.setEnabled(true);
			} else {
				assert extendSettings != null;
				extendSettings.setEnabled(false);
			}
		}


    }

	@Override
	public boolean onPreferenceTreeClick(Preference preference) {
		 if(preference.getKey().equals(clearDbKey)) {
			DialogFragment clearDbDialog = ClearDbDialog.newInstance(
					R.string.intro_title);
			clearDbDialog.setTargetFragment(this, 0);
			clearDbDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.BatteryDialogTitle );
			clearDbDialog.show(this.getParentFragmentManager(), "clearDbDialog");
			return true;
		}
		else {
			return super.onPreferenceTreeClick(preference);
		}
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		Intent testIntent = new Intent();
		testIntent.setAction(RReminder.CUSTOM_INTENT_TEST_PREFERENCES_MODE);
		testIntent.putExtra(RReminder.PREFERENCE_MODE_SUMMARY, testModeSummary);
		requireActivity().sendBroadcast(testIntent);
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
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		try {
			setParentActivity((PreferenceActivityLinkedService) getActivity());
		} catch (ClassCastException e) {
			throw new ClassCastException(context + " must implement PreferenceActivityLinkedService");
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(@NonNull Activity activity) {
		super.onAttach(activity);
		if (Build.VERSION.SDK_INT < 23) {
			try {
				setParentActivity((PreferenceActivityLinkedService)activity);
			} catch (ClassCastException e) {
				throw new ClassCastException(activity + " must implement PreferenceActivityLinkedService");
			}
		}
	}


	
}
