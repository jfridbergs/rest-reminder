package com.colormindapps.rest_reminder_alarm;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MyPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener{
	public String changeSummaryKey, workPeriodLengthKey, restPeriodLengthKey, approxPeriodLengthKey, extendCountKey, extendBaseLengthKey, workPeriodSoundKey, restPeriodSoundKey, approxSoundKey, approxEnabledKey;
	public String testModeSummary,testWorkLenghtSummary, testRestLenghtSummary, testWorkAudioSummary, testRestAudioSummary, testProximityLengthSummary, testProximityAudioSummary, testExtendCountSummary, testExtendLengthSummary;
	Uri originalWorkUri, originalRestUri, originalApproxUri, newWorkUri, newRestUri, newApproxUri;
	SharedPreferences sharedPreferences;
	String workPeriodLength, restPeriodLength, approxLength;
	long periodEndTimeValue;
	int periodType, extendCount;
	SharedPreferences.Editor editor;
	Preference workSoundPreference, restSoundPreference, approxSoundPreference;
	Context context;

	public static MyPreferenceFragment newInstance(int periodType, int extendCount,long periodEndTimeValue){
		MyPreferenceFragment fragment = new MyPreferenceFragment();
		Bundle args = new Bundle();
		args.putLong(RReminder.PERIOD_END_TIME, periodEndTimeValue);
		args.putInt(RReminder.PERIOD_TYPE, periodType);
		args.putInt(RReminder.EXTEND_COUNT, extendCount);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		Bundle data = getArguments();
		periodType = data.getInt(RReminder.PERIOD_TYPE);
		extendCount = data.getInt(RReminder.EXTEND_COUNT);
		periodEndTimeValue = data.getLong(RReminder.PERIOD_END_TIME);
		
		addPreferencesFromResource(R.xml.preferences);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		editor = sharedPreferences.edit();
		int value;
		context = getActivity();
		changeSummaryKey = getString(R.string.pref_mode_key);
		workPeriodLengthKey = getString(R.string.pref_work_period_length_key);
		restPeriodLengthKey = getString(R.string.pref_rest_period_length_key);
		approxPeriodLengthKey = getString(R.string.pref_approx_notification_length_key);
		extendCountKey = getString(R.string.pref_period_extend_options_key);
		extendBaseLengthKey = getString(R.string.pref_period_extend_length_key);
		workPeriodSoundKey = getString(R.string.pref_work_period_start_sound_key);
		restPeriodSoundKey = getString(R.string.pref_rest_period_start_sound_key);
		approxSoundKey = getString(R.string.pref_approx_time_sound_key);
		approxEnabledKey = getString(R.string.pref_enable_approx_notification_key);
		
        Preference preference = findPreference(changeSummaryKey);
        // Set summary to be the user-description for the selected value
        value = Integer.parseInt(sharedPreferences.getString(changeSummaryKey,"0"));
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
        
        preference = findPreference(workPeriodLengthKey);
    	workPeriodLength = sharedPreferences.getString(workPeriodLengthKey, getString(R.string.default_work_length_string));
    	String output = RReminder.getFormatedValue(context, 0, workPeriodLength);
    	preference.setSummary(output);

		testWorkLenghtSummary = preference.getSummary().toString();
    	
        preference = findPreference(restPeriodLengthKey);
    	restPeriodLength = sharedPreferences.getString(restPeriodLengthKey,getString(R.string.default_rest_length_string));
    	String output1 =  RReminder.getFormatedValue(context, 0, restPeriodLength);
    	preference.setSummary(output1);

		testRestLenghtSummary = preference.getSummary().toString();
    	
    	preference = findPreference(approxPeriodLengthKey);
    	approxLength = sharedPreferences.getString(approxPeriodLengthKey, getString(R.string.default_approx_length_string));
    	output = RReminder.getFormatedValue(context, 1, approxLength);
    	preference.setSummary(output);

		testProximityLengthSummary = preference.getSummary().toString();
    	
    	workSoundPreference = findPreference(workPeriodSoundKey);   	
    	String valueString = sharedPreferences.getString(workPeriodSoundKey, "DEFAULT_RINGTONE_URI");
    	if (valueString.equals("DEFAULT_RINGTONE_URI")){
    		originalWorkUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    	} else {
    		originalWorkUri = Uri.parse(valueString);
    	}
    	
    	Ringtone ringtone = RingtoneManager.getRingtone(context, originalWorkUri);
    	output = ringtone.getTitle(context);
    	workSoundPreference.setSummary(output);

		testWorkAudioSummary = workSoundPreference.getSummary().toString();
    	
    	restSoundPreference = findPreference(restPeriodSoundKey);   	
    	valueString = sharedPreferences.getString(restPeriodSoundKey, "DEFAULT_RINGTONE_URI");  	
    	if (valueString.equals("DEFAULT_RINGTONE_URI")){
    		originalRestUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    	} else {
    		originalRestUri = Uri.parse(valueString);
    	}
    	ringtone = RingtoneManager.getRingtone(context, originalRestUri);
    	output = ringtone.getTitle(context);
    	restSoundPreference.setSummary(output);

		testRestAudioSummary = restSoundPreference.getSummary().toString();
    	
    	approxSoundPreference = findPreference(approxSoundKey);   	
    	valueString = sharedPreferences.getString(approxSoundKey, "DEFAULT_RINGTONE_URI");  	
    	if (valueString.equals("DEFAULT_RINGTONE_URI")){
    		originalApproxUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    	} else {
    		originalApproxUri = Uri.parse(valueString);
    	}
    	ringtone = RingtoneManager.getRingtone(context, originalApproxUri);
    	output = ringtone.getTitle(context);
    	approxSoundPreference.setSummary(output);

		testProximityAudioSummary = approxSoundPreference.getSummary().toString();
    	
    	
    	preference = findPreference(extendCountKey);
    	value = sharedPreferences.getInt(extendCountKey, RReminder.DEFAULT_EXTEND_COUNT);
    	if(value==1){
    		output=getString(R.string.pref_options_single);
    	} else {
    		output=getString(R.string.pref_options_multiple, value);
    	}
    	preference.setSummary(output);

		testExtendCountSummary = preference.getSummary().toString();
    	
    	preference = findPreference(extendBaseLengthKey);
    	value = sharedPreferences.getInt(extendBaseLengthKey, RReminder.DEFAULT_EXTEND_BASE_LENGTH);
    	if(value==1){
    		output=getString(R.string.pref_minute_single);
    	} else {
    		output=getString(R.string.pref_minute_multiple, value);
    	}
    	preference.setSummary(output);

		testExtendLengthSummary = preference.getSummary().toString();
    	
    	
    	
        
		if(RReminder.isCounterServiceRunning(context)){
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
	
    

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	Preference preference = findPreference(key);
    	int value;
    	String name, outputName;
    	Uri ringtoneUri;
		String updatedWorkPeriodLength, updatedRestPeriodLength, updatedApproxLength;
		//updating preference summary after preferences are changed
        if (key.equals(changeSummaryKey)) {           
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
        } else if (key.equals(workPeriodLengthKey) || key.equals(restPeriodLengthKey)){
        	String valueString = sharedPreferences.getString(key, RReminder.DEFAULT_WORK_PERIOD_STRING);
        	String output = RReminder.getFormatedValue(context, RReminder.PREFERENCE_SUMMARY_HHMM, valueString);
        	preference.setSummary(output);
			if(key.equals(workPeriodLengthKey)){
				testWorkLenghtSummary = preference.getSummary().toString();
			} else {
				testRestLenghtSummary = preference.getSummary().toString();
			}
        	int shortestValue = RReminder.getShortestPeriodLength(context);
        	String approxValue = sharedPreferences.getString(approxPeriodLengthKey, RReminder.DEFAULT_WORK_PERIOD_STRING);
        	int approxHourValue = CustomTimePreference.getHour(approxValue);
        	if(shortestValue % 60 <= approxHourValue ){
        		approxHourValue= (shortestValue % 60)-1;
        		int approxMinValue = 45;
        		String time = String.valueOf(approxHourValue) + ":" + String.valueOf(approxMinValue);
				editor.putString(approxPeriodLengthKey, time);
				editor.commit();
        		Preference approxPreference = findPreference(approxPeriodLengthKey);
				output = RReminder.getFormatedValue(context, RReminder.PREFERENCE_SUMMARY_MMSS, time);
				approxPreference.setSummary(output);
        	}



        } else if (key.equals(approxPeriodLengthKey)){
        	String valueString = sharedPreferences.getString(key, RReminder.DEFAULT_APPROX_TIME_STRING);
        	String output = RReminder.getFormatedValue(context, RReminder.PREFERENCE_SUMMARY_MMSS, valueString);
        	preference.setSummary(output);
			testProximityLengthSummary = preference.getSummary().toString();
        } else if (key.equals(extendCountKey)){
        	value = sharedPreferences.getInt(key, RReminder.DEFAULT_EXTEND_COUNT);
        	String output;
        	if(value==1){
        		output=getString(R.string.pref_options_single);
        	} else {
        		output=String.format(getString(R.string.pref_options_multiple), value);
        	}
        	preference.setSummary(output);
			testExtendCountSummary = preference.getSummary().toString();
        } else if (key.equals(extendBaseLengthKey)) {
        	value = sharedPreferences.getInt(key, RReminder.DEFAULT_EXTEND_COUNT);
        	String output;
        	if(value==1){
        		output=getString(R.string.pref_minute_single);
        	} else {
        		output=String.format(getString(R.string.pref_minute_multiple), value);
        	}
        	preference.setSummary(output);
			testExtendLengthSummary = preference.getSummary().toString();
        } else if (key.equals(workPeriodSoundKey)){
        	
        	name = sharedPreferences.getString(key, "DEFAULT_RINGTONE_URI");
        	if (name.equals("DEFAULT_RINGTONE_URI")){
        		ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        	} else {
        		ringtoneUri = Uri.parse(name);
        	}
        	Ringtone ringtone = RingtoneManager.getRingtone(context, ringtoneUri);
        	outputName = ringtone.getTitle(context);
        	preference.setSummary(outputName);

        	
        }

		//if work period length preference was updated while the current period is work, the current period will be updated according to new value
		if(key.equals(workPeriodLengthKey)){
			if(RReminder.isCounterServiceRunning(context) && Calendar.getInstance().getTimeInMillis()<periodEndTimeValue){
				if(periodType ==1 || periodType ==3){
					//cancelling the current service and alarms
					RReminder.stopCounterService(context,periodType);
					RReminder.cancelCounterAlarm(context,periodType,extendCount,periodEndTimeValue, false,0L);

					//getting new values for service and alarm
					updatedWorkPeriodLength = sharedPreferences.getString(key, RReminder.DEFAULT_WORK_PERIOD_STRING);
					long difference = getUpdatedDiffrerence(workPeriodLength, updatedWorkPeriodLength,false);
					long newPeriodEndValue = periodEndTimeValue + difference;
					SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
					Calendar time = Calendar.getInstance();
					time.setTimeInMillis(newPeriodEndValue);


					//starting counterservice and setting new alarms
					new PeriodManager(context.getApplicationContext()).setPeriod(periodType, newPeriodEndValue, extendCount, false);
					RReminder.startCounterService(context.getApplicationContext(), periodType, extendCount, newPeriodEndValue, false);

					workPeriodLength = updatedWorkPeriodLength;
				}
			}
		} else if(key.equals(restPeriodLengthKey)){
			if(RReminder.isCounterServiceRunning(context)&& Calendar.getInstance().getTimeInMillis()<periodEndTimeValue){
				if(periodType ==2 || periodType ==4){
					//cancelling the current service and alarms
					RReminder.stopCounterService(context,periodType);
					RReminder.cancelCounterAlarm(context,periodType,extendCount,periodEndTimeValue, false,0L);

					//getting new values for service and alarm
					updatedRestPeriodLength = sharedPreferences.getString(key, RReminder.DEFAULT_REST_PERIOD_STRING);
					long difference = getUpdatedDiffrerence(restPeriodLength, updatedRestPeriodLength,false);
					long newPeriodEndValue = periodEndTimeValue + difference;

					//starting counterservice and setting new alarms
					new PeriodManager(context.getApplicationContext()).setPeriod(periodType, newPeriodEndValue, extendCount, false);
					RReminder.startCounterService(context.getApplicationContext(), periodType, extendCount, newPeriodEndValue, false);

					restPeriodLength = updatedRestPeriodLength;
				}
			}
		} else if(key.equals(approxPeriodLengthKey)){
			if(RReminder.isCounterServiceRunning(context) && RReminder.isApproxEnabled(context)&& Calendar.getInstance().getTimeInMillis()<RReminder.getApproxTime(context,periodEndTimeValue)){
				//cancelling the current approx alarm
				long oldApproxTimeValue = periodEndTimeValue - (CustomTimePreference.getHour(approxLength)*60*1000L+CustomTimePreference.getMinute(approxLength)*1000L);
				RReminder.cancelCounterAlarm(context,periodType,extendCount,periodEndTimeValue,true,oldApproxTimeValue);
				new PeriodManager(context.getApplicationContext()).setPeriod(periodType, periodEndTimeValue, extendCount, true);
				approxLength = sharedPreferences.getString(key,"00:30");

			}
		} else if(key.equals(approxEnabledKey)){
			if(RReminder.isCounterServiceRunning(context) && sharedPreferences.getBoolean(key, false)&& Calendar.getInstance().getTimeInMillis()<RReminder.getApproxTime(context,periodEndTimeValue)){
				new PeriodManager(context.getApplicationContext()).setPeriod(periodType, periodEndTimeValue, extendCount, true);
			}
		}
    }

	@Override
	public void onDestroy(){
		super.onDestroy();
		Intent testIntent = new Intent();
		testIntent.setAction(RReminder.CUSTOM_INTENT_TEST_PREFERENCES);
		testIntent.putExtra(RReminder.PREFERENCE_MODE_SUMMARY, testModeSummary);
		testIntent.putExtra(RReminder.PREFERENCE_WORK_LENGTH_SUMMARY, testWorkLenghtSummary);
		testIntent.putExtra(RReminder.PREFERENCE_REST_LENGTH_SUMMARY, testRestLenghtSummary);
		testIntent.putExtra(RReminder.PREFERENCE_WORK_AUDIO_SUMMARY, testWorkAudioSummary);
		testIntent.putExtra(RReminder.PREFERENCE_REST_AUDIO_SUMMARY, testRestAudioSummary);
		testIntent.putExtra(RReminder.PREFERENCE_PROXIMITY_LENGTH_SUMMARY, testProximityLengthSummary);
		testIntent.putExtra(RReminder.PREFERENCE_PROXIMITY_AUDIO_SUMMARY, testProximityAudioSummary);
		testIntent.putExtra(RReminder.PREFERENCE_EXTEND_COUNT_SUMMARY, testExtendCountSummary);
		testIntent.putExtra(RReminder.PREFERENCE_EXTEND_LENGTH_SUMMARY, testExtendLengthSummary);
		getActivity().sendBroadcast(testIntent);
	}
    
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        
        //get latest work sound uri
    	String workString = sharedPreferences.getString(workPeriodSoundKey, "DEFAULT_RINGTONE_URI");  	
    	if (workString.equals("DEFAULT_RINGTONE_URI")){
    		newWorkUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    	} else {
    		newWorkUri = Uri.parse(workString);
    	}
    	if(!newWorkUri.equals(originalWorkUri)){
        	Ringtone ringtone = RingtoneManager.getRingtone(context, newWorkUri);
        	String output = ringtone.getTitle(context);
        	workSoundPreference.setSummary(output);
			testWorkAudioSummary = workSoundPreference.getSummary().toString();
        	originalWorkUri = newWorkUri;
    	}
    	
        //get latest rest sound uri
    	String restString = sharedPreferences.getString(restPeriodSoundKey, "DEFAULT_RINGTONE_URI");  	
    	if (restString.equals("DEFAULT_RINGTONE_URI")){
    		newRestUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    	} else {
    		newRestUri = Uri.parse(restString);
    	}
    	if(!newRestUri.equals(originalRestUri)){
        	Ringtone ringtone = RingtoneManager.getRingtone(context, newRestUri);
        	String output = ringtone.getTitle(context);
        	restSoundPreference.setSummary(output);
			testRestAudioSummary = restSoundPreference.getSummary().toString();
        	originalRestUri = newRestUri;
    	}
    	
        //get latest rest sound uri
    	String approxString = sharedPreferences.getString(approxSoundKey, "DEFAULT_RINGTONE_URI");  	
    	if (approxString.equals("DEFAULT_RINGTONE_URI")){
    		newApproxUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    	} else {
    		newApproxUri = Uri.parse(approxString);
    	}
    	if(!newApproxUri.equals(originalApproxUri)){
        	Ringtone ringtone = RingtoneManager.getRingtone(context, newApproxUri);
        	String output = ringtone.getTitle(context);
        	approxSoundPreference.setSummary(output);
			testProximityAudioSummary = approxSoundPreference.getSummary().toString();
        	originalApproxUri = newApproxUri;
    	}
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

	public long getUpdatedDiffrerence(String oldString, String newString, boolean approx){
		int difference;
		int oldHour = CustomTimePreference.getHour(oldString);
		int oldMinute = CustomTimePreference.getMinute(oldString);
		int newHour = CustomTimePreference.getHour(newString);
		int newMinute = CustomTimePreference.getMinute(newString);
		if (approx){
			difference = (newHour*60*1000 + newMinute*1000) - (oldHour*60*1000 + oldMinute*1000);
		} else {
			difference = (newHour*60*60*1000 + newMinute*60*1000) - (oldHour*60*60*1000 + oldMinute*60*1000);
		}
		return (long)difference;
	}
	
}
