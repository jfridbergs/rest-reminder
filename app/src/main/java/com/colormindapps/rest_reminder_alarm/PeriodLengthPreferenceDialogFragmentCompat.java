package com.colormindapps.rest_reminder_alarm;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import org.w3c.dom.Text;

import java.time.Period;
import java.util.Objects;

public class PeriodLengthPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    private NumberPicker firstPicker, secondPicker;

    private int lastHour = 0;
    private int lastMinute = 15;
    private int restoreHour = 0;
    private int restoreMinute = 0;
    private String restoreValue;
    private final String SAVE_PERIOD_PREF_STATE = "save_period_preference_state";



    public static PeriodLengthPreferenceDialogFragmentCompat newInstance(String key){
        final PeriodLengthPreferenceDialogFragmentCompat fragment = new PeriodLengthPreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null && savedInstanceState.getString(SAVE_PERIOD_PREF_STATE)!=null){
            restoreValue = savedInstanceState.getString(SAVE_PERIOD_PREF_STATE);
            assert restoreValue != null;
            restoreHour = RReminder.getHour(restoreValue);
            restoreMinute = RReminder.getMinute(restoreValue);
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        int firstMinValue = 0;
        int secondMinValue = 10;
        int secondMaxValue = 59;

        TextView title = view.findViewById(R.id.custom_time_preference_title);

        firstPicker = view.findViewById(R.id.time_preference_first_picker);
        secondPicker = view.findViewById(R.id.time_preference_second_picker);




        // Exception when there is no both NumberPickers
        if (firstPicker == null || secondPicker == null) {
            throw new IllegalStateException("Dialog view must contain" +
                    " a NumberPicker");
        }
        //if short periods are not enabled, set min value of minute picker to 10 mins
        if(!RReminder.isShortPeriodsEnabled(requireContext())){
            firstPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
                // TODO Auto-generated method stub
                if(newVal==0){
                    secondPicker.setMinValue(10);
                    secondPicker.setValue(10);
                } else {
                    secondPicker.setMinValue(0);
                }
            });
        } else {
            //when short periods are enabled, set min value for min picker to 0 and display warning about inconsistent behaviour
            secondMinValue = 0;
        }


		/*
		secondPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				// TODO Auto-generated method stub
				tempMinute = newVal;
				tempHour = firstPicker.getValue();
			}
		});
		*/



        // Get the period length from the related Preference
        String periodLength;
        DialogPreference preference = getPreference();
        if (preference instanceof PeriodLengthPreference) {
            periodLength =
                    ((PeriodLengthPreference) preference).getPeriodLength();
            title.setText(((PeriodLengthPreference) preference).getHeaderText());
            int firstMaxValue = ((PeriodLengthPreference) preference).getFirstMaxValue();

            firstPicker.setMaxValue(firstMaxValue);
            secondPicker.setMaxValue(secondMaxValue);

            firstPicker.setMinValue(firstMinValue);
            secondPicker.setMinValue(secondMinValue);

            // Set the time to the NumberPickers
            if (periodLength != null) {
                lastHour = RReminder.getHour(periodLength);
                lastMinute = RReminder.getMinute(periodLength);
                if(restoreValue != null){
                    firstPicker.setValue(restoreHour);
                } else {
                    firstPicker.setValue(lastHour);
                }
                //if short periods are not allowed, set min value of minute picker to 10 (if hourpicker has a value of 0)
                if(!RReminder.isShortPeriodsEnabled(requireContext())){
                    if(lastHour==0){
                        secondPicker.setMinValue(10);
                    } else {
                        secondPicker.setMinValue(0);
                    }
                }
                if(restoreValue != null){
                    secondPicker.setValue(restoreMinute);
                } else {
                    secondPicker.setValue(lastMinute);
                }

            }
        }


    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // generate value to save
            lastHour = firstPicker.getValue();
            lastMinute = secondPicker.getValue();

            if (lastHour ==0 && lastMinute == 0){
                lastMinute =1;
            }


            String lastHourString = String.valueOf(lastHour);
            String lastMinuteString = String.valueOf(lastMinute);
            if(lastHour<10){
                lastHourString = "0"+ lastHourString;
            }

            if(lastMinute<10){
                lastMinuteString = "0"+ lastMinuteString;
            }

            String time = lastHourString + ":" + lastMinuteString;


            // Get the related Preference and save the value
            DialogPreference preference = getPreference();
            if (preference instanceof PeriodLengthPreference) {
                PeriodLengthPreference periodLengthPreference =
                        ((PeriodLengthPreference) preference);
                // This allows the client to ignore the user value.
                if (periodLengthPreference.callChangeListener(
                        time)) {
                    // Save the value
                    periodLengthPreference.setPeriodLength(time);
                }
            }
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if(firstPicker!=null && secondPicker != null){
            restoreHour = firstPicker.getValue();
            restoreMinute = secondPicker.getValue();
        }
        outState.putString(SAVE_PERIOD_PREF_STATE, restoreHour + ":" + restoreMinute);
    }


}
