package com.colormindapps.rest_reminder_alarm;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;

public class NumberXPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    private NumberPicker numberPicker;
    private TextView title;

    private int lastValue, restoreValue = -1;
    private final String SAVE_NUMBER_PREF_STATE = "save_number_preference_state";


    public static NumberXPreferenceDialogFragmentCompat newInstance(String key){
       final NumberXPreferenceDialogFragmentCompat fragment = new NumberXPreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null){
            Log.d("NUMBER_X_PREFERENCE", "restoring numberpicker value");
            restoreValue = savedInstanceState.getInt(SAVE_NUMBER_PREF_STATE);
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        title = (TextView)view.findViewById(R.id.number_preference_title);
        numberPicker = (NumberPicker) view.findViewById(R.id.number_preference_picker);
        DialogPreference preference = getPreference();
        if (preference instanceof NumberXPreference) {
            title.setText(((NumberXPreference) preference).getHeaderText());

            numberPicker.setMinValue(((NumberXPreference) preference).getMinValue());
            numberPicker.setMaxValue(((NumberXPreference) preference).getMaxValue());
            if(restoreValue>=0){
                numberPicker.setValue(restoreValue);
            } else {
                numberPicker.setValue(((NumberXPreference) preference).getPreferenceInt());
            }

            
        }
        // Exception when there is no both NumberPickers
        if (numberPicker == null) {
            throw new IllegalStateException("Dialog view must contain" +
                    " a NumberPicker");
        }


    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // generate value to save
            lastValue = numberPicker.getValue();


            // Get the related Preference and save the value
            DialogPreference preference = getPreference();
            if (preference instanceof NumberXPreference) {
                NumberXPreference numberXPreference =
                        ((NumberXPreference) preference);
                // This allows the client to ignore the user value.
                if (numberXPreference.callChangeListener(
                        lastValue)) {
                    // Save the value
                    numberXPreference.setPreferenceInt(lastValue);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if(numberPicker!=null){
            lastValue = numberPicker.getValue();
        }
        outState.putInt(SAVE_NUMBER_PREF_STATE,lastValue);
    }


}
