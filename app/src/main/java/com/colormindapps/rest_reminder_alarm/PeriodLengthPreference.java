package com.colormindapps.rest_reminder_alarm;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class PeriodLengthPreference extends DialogPreference {
    private String mPeriodLength;
    private int firstMaxValue;
    private String headerText;
    private int mDialogLayoutResId = R.layout.period_length_pref_dialog;

    public PeriodLengthPreference (Context context){
        this(context, null);
    }

    public PeriodLengthPreference(Context context, AttributeSet attrs){
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public PeriodLengthPreference(Context context, AttributeSet attrs, int defStyleAttr){
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public PeriodLengthPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs){
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomTimePreference);
        firstMaxValue = a.getInteger(R.styleable.CustomTimePreference_firstMaxValue,10);
        headerText = a.getString(R.styleable.CustomTimePreference_customTimeDialogTopText);
        a.recycle();
    }

    public int getFirstMaxValue(){
        return firstMaxValue;
    }

    public String getHeaderText(){
        return headerText;
    }

    public String getPeriodLength(){
        return mPeriodLength;
    }

    public void setPeriodLength(String periodLength){
        mPeriodLength = periodLength;
        persistString(periodLength);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // Default value from attribute. Fallback value is set to 0.
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        // Read the value. Use the default value if it is not possible.
        if (defaultValue == null){
            setPeriodLength(getPersistedString("00:00"));
        } else {
            setPeriodLength(getPersistedString(defaultValue.toString()));
        }
    }

    @Override
    public int getDialogLayoutResource() {
        return mDialogLayoutResId;
    }
}
