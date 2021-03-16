package com.colormindapps.rest_reminder_alarm;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PreferenceXSubScreenFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = PreferenceXSubScreenFragment.class.getName();
    Uri originalWorkUri, originalRestUri, newWorkUri, newRestUri;
    String workPeriodSoundKey, restPeriodSoundKey, workPeriodLengthKey, restPeriodLengthKey, extendCountKey, extendBaseLengthKey;
    String testWorkAudioSummary, testRestAudioSummary, testWorkLengthSummary, testRestLengthSummary, testExtendCountSummary, testExtendLengthSummary;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Preference workSoundPreference, restSoundPreference, workPeriodLengthPreference, restPeriodLengthPreference, extendCountpreference, extendBasePreference;
    Context context;
    String workPeriodLength, restPeriodLength;
    String preferenceScreenKey;

    int REQUEST_CODE_ALERT_WORK_RINGTONE = 1;
    int REQUEST_CODE_ALERT_REST_RINGTONE = 2;

    public static final String PAGE_ID = "page_id";
    private PreferenceActivityLinkedService parentActivity;

    public static PreferenceXSubScreenFragment newInstance(String pageId) {
        PreferenceXSubScreenFragment f = new PreferenceXSubScreenFragment();
        Bundle args = new Bundle();
        args.putString(PAGE_ID, pageId);
        f.setArguments(args);
        return (f);
    }

    private void setParentActivity(PreferenceActivityLinkedService activity){
        parentActivity = activity;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // rootKey is the name of preference sub screen key name , here--customPrefKey
        setPreferencesFromResource(R.xml.preferences_x, rootKey);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = sharedPreferences.edit();
        Log.d(TAG, "onCreatePreferences of the sub screen " + rootKey);
        context = getActivity();

        String output;

        workPeriodLengthKey = getString(R.string.pref_work_period_length_key);
        restPeriodLengthKey = getString(R.string.pref_rest_period_length_key);
        extendCountKey = getString(R.string.pref_period_extend_options_key);
        extendBaseLengthKey = getString(R.string.pref_period_extend_length_key);
        workPeriodSoundKey = getString(R.string.pref_work_period_start_sound_key);
        restPeriodSoundKey = getString(R.string.pref_rest_period_start_sound_key);

        if (getArguments() == null || !getArguments().containsKey(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT)) {
            throw new RuntimeException("You must provide a pluginKey by calling setArguments(@NonNull String pluginKey)");
        }
        preferenceScreenKey = getArguments().getString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT);

        if(preferenceScreenKey.equals(getString(R.string.pref_screen_periods_key))){
            Log.d("X_SUBSCREEN_FRAGMENT", "setting the preference summaries for period preferences");
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


            workPeriodLengthPreference = findPreference(workPeriodLengthKey);
            workPeriodLength = sharedPreferences.getString(workPeriodLengthKey, getString(R.string.default_work_length_string));
            output = RReminder.getFormatedValue(context, 0, workPeriodLength);
            workPeriodLengthPreference.setSummary(output);

            testWorkLengthSummary = workPeriodLengthPreference.getSummary().toString();

            restPeriodLengthPreference = findPreference(restPeriodLengthKey);
            restPeriodLength = sharedPreferences.getString(restPeriodLengthKey,getString(R.string.default_rest_length_string));
            String output1 =  RReminder.getFormatedValue(context, 0, restPeriodLength);
            restPeriodLengthPreference.setSummary(output1);

            testRestLengthSummary = restPeriodLengthPreference.getSummary().toString();
        } else if (preferenceScreenKey.equals(getString(R.string.pref_screen_period_extend_key))){
            extendCountpreference = findPreference(extendCountKey);
            int value = sharedPreferences.getInt(extendCountKey, RReminder.DEFAULT_EXTEND_COUNT);
            if(value==1){
                output=getString(R.string.pref_options_single);
            } else {
                output=getString(R.string.pref_options_multiple, value);
            }
            extendCountpreference.setSummary(output);

            testExtendCountSummary = extendCountpreference.getSummary().toString();

            extendBasePreference = findPreference(extendBaseLengthKey);
            value = sharedPreferences.getInt(extendBaseLengthKey, RReminder.DEFAULT_EXTEND_BASE_LENGTH);
            if(value==1){
                output=getString(R.string.pref_minute_single);
            } else {
                output=getString(R.string.pref_minute_multiple, value);
            }
            extendBasePreference.setSummary(output);

            testExtendLengthSummary = extendBasePreference.getSummary().toString();
        }






    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        // Try if the preference is one of our custom Preferences
        DialogFragment dialogFragment = null;
        if (preference instanceof PeriodLengthPreference) {
            // Create a new instance of TimePreferenceDialogFragment with the key of the related
            // Preference
            dialogFragment = PeriodLengthPreferenceDialogFragmentCompat
                    .newInstance(preference.getKey());
        } else if (preference instanceof NumberXPreference) {
            // Create a new instance of TimePreferenceDialogFragment with the key of the related
            // Preference
            dialogFragment = NumberXPreferenceDialogFragmentCompat
                    .newInstance(preference.getKey());
        }

        // If it was one of our cutom Preferences, show its dialog
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(),
                    "android.support.v7.preference" +
                            ".PreferenceFragment.DIALOG");
        }
        // Could not be handled here. Try with the super method.
        else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        int periodType = 0;
        int extendCount = 0;
        long periodEndTimeValue = 0L;
        boolean failCondition = true;
        Bundle dataFromCounterSerivce;
        Preference preference = findPreference(key);
        int value;
        String name, outputName;
        Uri ringtoneUri;
        String updatedWorkPeriodLength, updatedRestPeriodLength;

        if(RReminderMobile.isCounterServiceRunning(context)){
            dataFromCounterSerivce = parentActivity.getDataFromService();
            if(dataFromCounterSerivce!=null){
                periodType = dataFromCounterSerivce.getInt(RReminder.PERIOD_TYPE);
                extendCount = dataFromCounterSerivce.getInt(RReminder.EXTEND_COUNT);
                periodEndTimeValue = dataFromCounterSerivce.getLong(RReminder.PERIOD_END_TIME);
            }

        }
        if(preferenceScreenKey.equals(getString(R.string.pref_screen_periods_key))){
            if (key.equals(workPeriodLengthKey) || key.equals(restPeriodLengthKey)){
                String valueString = sharedPreferences.getString(key, RReminder.DEFAULT_WORK_PERIOD_STRING);
                String output = RReminder.getFormatedValue(context, RReminder.PREFERENCE_SUMMARY_HHMM, valueString);
                preference.setSummary(output);
                if(key.equals(workPeriodLengthKey)){
                    testWorkLengthSummary = preference.getSummary().toString();
                } else {
                    testRestLengthSummary = preference.getSummary().toString();
                }

            }  else if (key.equals(workPeriodSoundKey)){

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
                if(RReminderMobile.isCounterServiceRunning(context) && Calendar.getInstance().getTimeInMillis()<periodEndTimeValue){
                    if(periodType ==1 || periodType ==3){
                        //cancelling the current service and alarms
                        RReminderMobile.stopCounterService(context,periodType);
                        RReminderMobile.cancelCounterAlarm(context,periodType,extendCount,periodEndTimeValue);

                        //getting new values for service and alarm
                        updatedWorkPeriodLength = sharedPreferences.getString(key, RReminder.DEFAULT_WORK_PERIOD_STRING);
                        long difference = getUpdatedDiffrerence(workPeriodLength, updatedWorkPeriodLength);
                        long newPeriodEndValue = periodEndTimeValue + difference;
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        Calendar time = Calendar.getInstance();
                        time.setTimeInMillis(newPeriodEndValue);


                        //starting counterservice and setting new alarms
                        new MobilePeriodManager(context.getApplicationContext()).setPeriod(periodType, newPeriodEndValue, extendCount);
                        RReminderMobile.startCounterService(context.getApplicationContext(), periodType, extendCount, newPeriodEndValue, false);

                        workPeriodLength = updatedWorkPeriodLength;
                    }
                }
            } else if(key.equals(restPeriodLengthKey)){
                if(RReminderMobile.isCounterServiceRunning(context)&& Calendar.getInstance().getTimeInMillis()<periodEndTimeValue){
                    if(periodType ==2 || periodType ==4){
                        //cancelling the current service and alarms
                        RReminderMobile.stopCounterService(context,periodType);
                        RReminderMobile.cancelCounterAlarm(context,periodType,extendCount,periodEndTimeValue);

                        //getting new values for service and alarm
                        updatedRestPeriodLength = sharedPreferences.getString(key, RReminder.DEFAULT_REST_PERIOD_STRING);
                        long difference = getUpdatedDiffrerence(restPeriodLength, updatedRestPeriodLength);
                        long newPeriodEndValue = periodEndTimeValue + difference;

                        //starting counterservice and setting new alarms
                        new MobilePeriodManager(context.getApplicationContext()).setPeriod(periodType, newPeriodEndValue, extendCount);
                        RReminderMobile.startCounterService(context.getApplicationContext(), periodType, extendCount, newPeriodEndValue, false);

                        restPeriodLength = updatedRestPeriodLength;
                    }
                }
            }
        } else if (preferenceScreenKey.equals(getString(R.string.pref_screen_period_extend_key))){
            if (key.equals(extendCountKey)){
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
                value = sharedPreferences.getInt(key, RReminder.DEFAULT_EXTEND_BASE_LENGTH);
                String output;
                if(value==1){
                    output=getString(R.string.pref_minute_single);
                } else {
                    output=String.format(getString(R.string.pref_minute_multiple), value);
                }
                preference.setSummary(output);
                testExtendLengthSummary = preference.getSummary().toString();
            }
        }




    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        //get latest work sound uri
        if(preferenceScreenKey.equals(getString(R.string.pref_screen_periods_key))){
            String workString = sharedPreferences.getString(workPeriodSoundKey, "DEFAULT_RINGTONE_URI");
            if (workString.equals("DEFAULT_RINGTONE_URI")){
                newWorkUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            } else {
                newWorkUri = Uri.parse(workString);
            }
            if(!newWorkUri.equals(originalWorkUri)){
                Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), newWorkUri);
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
        }



    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals(workPeriodSoundKey) || preference.getKey().equals(restPeriodSoundKey)) {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

            String existingValue = sharedPreferences.getString(preference.getKey(), "DEFAULT_RINGTONE_URI"); // TODO
            if (existingValue != null) {
                if (existingValue.length() == 0) {
                    // Select "Silent"
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                } else {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
                }
            } else {
                // No ringtone has been selected, set to the default
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
            }
            if(preference.getKey().equals(workPeriodSoundKey)){
                startActivityForResult(intent, REQUEST_CODE_ALERT_WORK_RINGTONE);
            } else {
                startActivityForResult(intent, REQUEST_CODE_ALERT_REST_RINGTONE);
            }

            return true;
        } else {
            return super.onPreferenceTreeClick(preference);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ALERT_WORK_RINGTONE && data != null) {
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (ringtone != null) {
                sharedPreferences.edit().putString(workPeriodSoundKey, ringtone.toString()).commit();
            } else {
                // "Silent" was selected
                sharedPreferences.edit().putString(workPeriodSoundKey, "DEFAULT_RINGTONE_URI").commit();
            }
        }
        else if (requestCode == REQUEST_CODE_ALERT_REST_RINGTONE && data != null) {
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (ringtone != null) {
                sharedPreferences.edit().putString(restPeriodSoundKey, ringtone.toString()).commit();
            } else {
                // "Silent" was selected
                sharedPreferences.edit().putString(restPeriodSoundKey, "DEFAULT_RINGTONE_URI").commit();
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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

    public long getUpdatedDiffrerence(String oldString, String newString){
        int difference;
        int oldHour = CustomTimePreference.getHour(oldString);
        int oldMinute = CustomTimePreference.getMinute(oldString);
        int newHour = CustomTimePreference.getHour(newString);
        int newMinute = CustomTimePreference.getMinute(newString);
        difference = (newHour*60*60*1000 + newMinute*60*1000) - (oldHour*60*60*1000 + oldMinute*60*1000);

        return (long)difference;
    }
}
