package com.colormindapps.rest_reminder_alarm;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class NumberXPreference extends DialogPreference {
    private int mPreferenceInt;
    private int minValue, maxValue;
    private String headerText;

    public NumberXPreference(Context context){
        this(context, null);
    }

    public NumberXPreference(Context context, AttributeSet attrs){
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public NumberXPreference(Context context, AttributeSet attrs, int defStyleAttr){
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public NumberXPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs){
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.NumberPreference);
        minValue = a.getInteger(R.styleable.NumberPreference_numberMinValue,1);
        maxValue = a.getInteger(R.styleable.NumberPreference_numberMaxValue,10);
        headerText = a.getString(R.styleable.NumberPreference_numberDialogTopText);
        a.recycle();
    }

    public int getMinValue(){
        return minValue;
    }
    public int getMaxValue(){
        return maxValue;
    }

    public String getHeaderText(){
        return headerText;
    }

    public int getPreferenceInt(){
        return mPreferenceInt;
    }

    public void setPreferenceInt(int preferenceInt){
        mPreferenceInt = preferenceInt;
        persistInt(mPreferenceInt);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // Default value from attribute. Fallback value is set to 0.
        return a.getInt(index,1);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        // Read the value. Use the default value if it is not possible.
        if (defaultValue == null){
            setPreferenceInt(getPersistedInt(1));
        } else {
            setPreferenceInt(getPersistedInt((int)defaultValue));
        }
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.number_preference;
    }
}
