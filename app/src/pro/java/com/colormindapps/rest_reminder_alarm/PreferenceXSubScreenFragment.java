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
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.colormindapps.rest_reminder_alarm.data.Period;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.Calendar;

import static com.colormindapps.rest_reminder_alarm.shared.RReminder.getHourFromString;
import static com.colormindapps.rest_reminder_alarm.shared.RReminder.getMinuteFromString;

public class PreferenceXSubScreenFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    Uri originalWorkUri, originalRestUri, newWorkUri, newRestUri;
    String workPeriodSoundKey, restPeriodSoundKey, workPeriodLengthKey, restPeriodLengthKey, extendCountKey, extendBaseLengthKey, enableShortPeriodsKey, disableBatteryOptKey, extendEnabledKey, startNextEnabledKey, reminderModeKey;
    String testWorkAudioSummary, testRestAudioSummary, testWorkLengthSummary, testRestLengthSummary, testExtendCountSummary, testExtendLengthSummary, testDisableBatteryOptSummary;
    SharedPreferences sharedPreferences;
    Preference workSoundPreference, restSoundPreference, workPeriodLengthPreference, restPeriodLengthPreference, extendCountpreference, extendBasePreference, disableBatteryOptPreference;
    Context context;
    String workPeriodLength, restPeriodLength;
    String preferenceScreenKey;
    DialogFragment batterySettingsDialog;
    String wearWorkLength, wearRestLength, wearReminderMode;
    int wearExtendLength;
    boolean wearExtendEnabled, wearStartNextEnabled;

    private boolean shortDisabled = false;

    int REQUEST_CODE_ALERT_WORK_RINGTONE = 1;
    int REQUEST_CODE_ALERT_REST_RINGTONE = 2;

    private PeriodViewModel mPeriodViewModel;
    private LiveData<Period> currentLDPeriod;
    private Observer<Period> periodObserver;
    private Period mPeriod;

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

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        context = getActivity();

        String output;

        reminderModeKey = getString(R.string.pref_mode_key);
        workPeriodLengthKey = getString(R.string.pref_work_period_length_key);
        restPeriodLengthKey = getString(R.string.pref_rest_period_length_key);
        extendCountKey = getString(R.string.pref_period_extend_options_key);
        extendBaseLengthKey = getString(R.string.pref_period_extend_length_key);
        workPeriodSoundKey = getString(R.string.pref_work_period_start_sound_key);
        restPeriodSoundKey = getString(R.string.pref_rest_period_start_sound_key);
        extendEnabledKey = getString(R.string.pref_enable_extend_key);
        startNextEnabledKey = getString(R.string.pref_end_period_key);
        enableShortPeriodsKey = getString(R.string.pref_enable_short_periods_key);
        disableBatteryOptKey = getString(R.string.pref_disable_battery_optimization_key);

        mPeriodViewModel = new ViewModelProvider(this).get(PeriodViewModel.class);

        if (getArguments() == null || !getArguments().containsKey(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT)) {
            throw new RuntimeException("You must provide a pluginKey by calling setArguments(@NonNull String pluginKey)");
        }
        preferenceScreenKey = getArguments().getString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT);


        assert preferenceScreenKey != null;
        if(preferenceScreenKey.equals(getString(R.string.pref_screen_periods_key))){

            //disable preference for enabling shorter than 10 min periods in order to avoid messy interactions when updating current running periods, that were <10 mins to new min 10 min value (unnecessary work)
            Preference preference = getPreferenceManager().findPreference(enableShortPeriodsKey);
            assert preference != null;
            preference.setEnabled(!RReminderMobile.isCounterServiceRunning(context));

            workSoundPreference = findPreference(workPeriodSoundKey);
            String valueString = sharedPreferences.getString(workPeriodSoundKey, "DEFAULT_RINGTONE_URI");
            assert valueString != null;
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
            assert valueString != null;
            if (valueString.equals("DEFAULT_RINGTONE_URI")){
                originalRestUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            } else {
                originalRestUri = Uri.parse(valueString);
            }
            ringtone = RingtoneManager.getRingtone(context, originalRestUri);
            output = ringtone.getTitle(context);
            restSoundPreference.setSummary(output);

            testRestAudioSummary = restSoundPreference.getSummary().toString();

            //show preference for disabling battery optimization only for devices with Android API >= 23
            disableBatteryOptPreference = findPreference(disableBatteryOptKey);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                output = getString(R.string.pref_disable_battery_optimization_prefix);
                if (isBatteryOptimizationIgnored()){
                    output+=" "+ getString(R.string.pref_disable_battery_optimization_disabled);
                } else {
                    output+=" "+ getString(R.string.pref_disable_battery_optimization_enabled);
                }
                disableBatteryOptPreference.setSummary(output);

                testDisableBatteryOptSummary = disableBatteryOptPreference.getSummary().toString();
            } else {
                PreferenceCategory preferenceCategory = findPreference(getString(R.string.pref_category_period_settings_key));
                assert preferenceCategory != null;
                preferenceCategory.removePreference(disableBatteryOptPreference);
            }



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
            String periodLengthString = ((PeriodLengthPreference) preference).getPeriodLength();
            int periodLength = getHourFromString(periodLengthString) * 60 + getMinuteFromString(periodLengthString);
            if(shortDisabled && periodLength<10){
                ((PeriodLengthPreference) preference).setPeriodLength("00:10");
            }

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
            dialogFragment.show(this.getParentFragmentManager(),
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
        Bundle dataFromCounterService;
        Preference preference = findPreference(key);
        int value;
        String name, outputName;
        Uri ringtoneUri;
        String updatedWorkPeriodLength, updatedRestPeriodLength;

        if(RReminderMobile.isCounterServiceRunning(context)){
            dataFromCounterService = parentActivity.getDataFromService();
            if(dataFromCounterService!=null){
                periodType = dataFromCounterService.getInt(RReminder.PERIOD_TYPE);
                extendCount = dataFromCounterService.getInt(RReminder.EXTEND_COUNT);
                periodEndTimeValue = dataFromCounterService.getLong(RReminder.PERIOD_END_TIME);
            }

        }

        //updating the preferences values on wear device
        wearWorkLength = sharedPreferences.getString(workPeriodLengthKey, RReminder.DEFAULT_WORK_PERIOD_STRING);
        wearRestLength = sharedPreferences.getString(restPeriodLengthKey, RReminder.DEFAULT_REST_PERIOD_STRING);
        wearExtendLength = sharedPreferences.getInt(extendBaseLengthKey, RReminder.DEFAULT_EXTEND_COUNT);
        wearExtendEnabled = sharedPreferences.getBoolean(extendEnabledKey, true);
        wearStartNextEnabled = sharedPreferences.getBoolean(startNextEnabledKey, true);
        wearReminderMode = sharedPreferences.getString(reminderModeKey, "0");


        if(preferenceScreenKey.equals(getString(R.string.pref_screen_periods_key))){
            if (key.equals(workPeriodLengthKey) || key.equals(restPeriodLengthKey)){
                String valueString = sharedPreferences.getString(key, RReminder.DEFAULT_WORK_PERIOD_STRING);
                String output = RReminder.getFormatedValue(context, RReminder.PREFERENCE_SUMMARY_HHMM, valueString);
                assert preference != null;
                preference.setSummary(output);
                if(key.equals(workPeriodLengthKey)){
                    testWorkLengthSummary = preference.getSummary().toString();
                } else {
                    testRestLengthSummary = preference.getSummary().toString();
                }

            }  else if (key.equals(workPeriodSoundKey)){
                name = sharedPreferences.getString(key, "DEFAULT_RINGTONE_URI");
                assert name != null;
                if (name.equals("DEFAULT_RINGTONE_URI")){
                    ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                } else {
                    ringtoneUri = Uri.parse(name);
                }
                Ringtone ringtone = RingtoneManager.getRingtone(context, ringtoneUri);
                outputName = ringtone.getTitle(context);
                assert preference != null;
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
                        long difference = RReminder.getUpdatedDifference(workPeriodLength, updatedWorkPeriodLength);
                        long newPeriodEndValue = periodEndTimeValue + difference;
                        Calendar time = Calendar.getInstance();
                        time.setTimeInMillis(newPeriodEndValue);


                        //starting counterservice and setting new alarms
                        new MobilePeriodManager(context.getApplicationContext()).setPeriod(periodType, newPeriodEndValue, extendCount);
                        RReminderMobile.startCounterService(context.getApplicationContext(), periodType, extendCount, newPeriodEndValue, false);
                        getAndUpdatePeriodDb(newPeriodEndValue,periodType,extendCount);

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
                        long difference = RReminder.getUpdatedDifference(restPeriodLength, updatedRestPeriodLength);
                        long newPeriodEndValue = periodEndTimeValue + difference;

                        //starting counterservice and setting new alarms
                        //setting alarm via alarmmanager if period length is >=10 mins
                        new MobilePeriodManager(context.getApplicationContext()).setPeriod(periodType, newPeriodEndValue, extendCount);
                        RReminderMobile.startCounterService(context.getApplicationContext(), periodType, extendCount, newPeriodEndValue, false);
                        getAndUpdatePeriodDb(newPeriodEndValue,periodType,extendCount);

                        restPeriodLength = updatedRestPeriodLength;
                    }
                }
            } else if (key.equals(enableShortPeriodsKey)) {
                //if short periods setting is disabled, period length is set to 10 mins for each preference that had a stored value <10 before disabling short periods
                boolean shortEnabled = sharedPreferences.getBoolean(key, false);
                if(!shortEnabled){
                    shortDisabled = true;
                    String workPeriod = sharedPreferences.getString(workPeriodLengthKey, context.getString(com.colormindapps.rest_reminder_alarm.shared.R.string.default_work_length_string));
                    String restPeriod = sharedPreferences.getString(restPeriodLengthKey, context.getString(com.colormindapps.rest_reminder_alarm.shared.R.string.default_rest_length_string));
                    assert workPeriod != null;
                    assert restPeriod != null;
                    int workPeriodValue = getHourFromString(workPeriod) * 60 + getMinuteFromString(workPeriod);
                    int restPeriodValue = getHourFromString(restPeriod) * 60 + getMinuteFromString(restPeriod);
                    if(workPeriodValue<10){
                        String output = RReminder.getFormatedValue(context, 0, "00:10");
                        sharedPreferences.edit().putString(workPeriodLengthKey,"00:10" ).commit();
                        workPeriodLengthPreference.setSummary(output);
                    }
                    if(restPeriodValue<10){
                        String output = RReminder.getFormatedValue(context, 0, "00:10");
                        sharedPreferences.edit().putString(restPeriodLengthKey,"00:10" ).commit();
                        restPeriodLengthPreference.setSummary(output);
                    }

                } else {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if(!isBatteryOptimizationIgnored() && batterySettingsDialog==null){
                            batterySettingsDialog = BatterySettingsDialog.newInstance(
                                    R.string.intro_title);
                            batterySettingsDialog.setTargetFragment(this, 0);
                            batterySettingsDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.BatterySettingsDialog );
                            batterySettingsDialog.show(this.getParentFragmentManager(), "batterySettingsDialog");
                        }
                    }
                    shortDisabled = false;
                }
                // TO-DO: write logic for changing less than 10 min periods to 10 mins, if enableshortperiod preference is turned off
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
                assert preference != null;
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
                assert preference != null;
                preference.setSummary(output);
                testExtendLengthSummary = preference.getSummary().toString();
            }
        }

        sendEspressoBroadcast();


    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        //get latest work sound uri
        if(preferenceScreenKey.equals(getString(R.string.pref_screen_periods_key))){
            String workString = sharedPreferences.getString(workPeriodSoundKey, "DEFAULT_RINGTONE_URI");
            assert workString != null;
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
            assert restString != null;
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
            //update disable battery optimization summary when resuming the preference screen (returning from battery optimization device settings)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                String outputName = getString(R.string.pref_disable_battery_optimization_prefix);
                if (isBatteryOptimizationIgnored()){
                    outputName+=" "+ getString(R.string.pref_disable_battery_optimization_disabled);
                } else {
                    outputName+=" "+ getString(R.string.pref_disable_battery_optimization_enabled);
                }
                if(sharedPreferences.getBoolean(disableBatteryOptKey, false)!=isBatteryOptimizationIgnored()){
                    sharedPreferences.edit().putBoolean(disableBatteryOptKey, isBatteryOptimizationIgnored()).commit();
                }
                disableBatteryOptPreference.setSummary(outputName);
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
            assert existingValue != null;
            if (existingValue.length() == 0) {
                // Select "Silent"
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
            } else {
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
            }
            if(preference.getKey().equals(workPeriodSoundKey)){
                startActivityForResult(intent, REQUEST_CODE_ALERT_WORK_RINGTONE);
            } else {
                startActivityForResult(intent, REQUEST_CODE_ALERT_REST_RINGTONE);
            }

            return true;
        } else if(preference.getKey().equals(disableBatteryOptKey)&&Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            openOptimizationSettings();
            return true;
        }
        else {
            return super.onPreferenceTreeClick(preference);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void openOptimizationSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        context.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isBatteryOptimizationIgnored(){
        String packageName = context.getPackageName();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        assert pm != null;
        return pm.isIgnoringBatteryOptimizations(packageName);
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



    @Override
    public void onDestroy(){
        super.onDestroy();
        sendEspressoBroadcast();

    }

    public void getAndUpdatePeriodDb(long newEndTime, int type, int extendCount){
        currentLDPeriod = mPeriodViewModel.getLastPeriod();
        periodObserver = period -> {
            mPeriod = period;
            mPeriod.setType(type);
            mPeriod.setExtendCount(extendCount);
            mPeriod.setDuration(newEndTime-mPeriod.getStartTime());
            mPeriodViewModel.update(mPeriod);
            currentLDPeriod.removeObserver(periodObserver);
        };

        currentLDPeriod.observe(this, periodObserver);
    }

    private void sendEspressoBroadcast(){
        Intent testIntent = new Intent();
        if(preferenceScreenKey.equals(getString(R.string.pref_screen_periods_key))){
            testIntent.setAction(RReminder.CUSTOM_INTENT_TEST_PREFERENCES_PERIOD);
            testIntent.putExtra(RReminder.PREFERENCE_WORK_LENGTH_SUMMARY, testWorkLengthSummary);
            testIntent.putExtra(RReminder.PREFERENCE_REST_LENGTH_SUMMARY, testRestLengthSummary);
            testIntent.putExtra(RReminder.PREFERENCE_WORK_AUDIO_SUMMARY, testWorkAudioSummary);
            testIntent.putExtra(RReminder.PREFERENCE_REST_AUDIO_SUMMARY, testRestAudioSummary);
            testIntent.putExtra(RReminder.PREFERENCE_DISABLE_BATTERY_OPT_SUMMARY, testDisableBatteryOptSummary);
        } else {
            testIntent.setAction(RReminder.CUSTOM_INTENT_TEST_PREFERENCES_EXTEND);
            testIntent.putExtra(RReminder.PREFERENCE_EXTEND_COUNT_SUMMARY, testExtendCountSummary);
            testIntent.putExtra(RReminder.PREFERENCE_EXTEND_LENGTH_SUMMARY, testExtendLengthSummary);
        }
        requireActivity().sendBroadcast(testIntent);
    }
}
