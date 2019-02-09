package com.colormindapps.rest_reminder_alarm;


import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;


public class CustomTimePreference extends DialogPreference {
	
	private NumberPicker firstPicker, secondPicker;
	
	private int lastHour = 0;
	private int lastMinute = 15;
	private int firstMaxValue;
	private int restoreHour;
	private int restoreMinute;
	private String headerText;
	

	
	
	static int getHour(String time){
		String[] pieces = time.split(":");
		return (Integer.parseInt(pieces[0]));
	}
	
	static int getMinute(String time){
		String[] pieces = time.split(":");
		return (Integer.parseInt(pieces[1]));
	}

	
	public CustomTimePreference(Context context, AttributeSet attrs){
		super(context, attrs);
		init(attrs);
		setDialogLayoutResource(R.layout.custom_time_preference);
		setPositiveButtonText(context.getString(R.string.time_preference_set_text));
		setNegativeButtonText(context.getString(R.string.time_preference_cancel_text));
	}
	
	private void init(AttributeSet attrs){
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomTimePreference);
		firstMaxValue = a.getInteger(R.styleable.CustomTimePreference_firstMaxValue,10);
		headerText = a.getString(R.styleable.CustomTimePreference_customTimeDialogTopText);
		a.recycle();
	}
	
	
	
	
	@Override
	protected View onCreateDialogView(){
		int firstMinValue = 0;
		int secondMinValue = 10;
		int secondMaxValue = 59;
		View root = super.onCreateDialogView();
		TextView tv = root.findViewById(R.id.custom_time_preference_title);
		tv.setText(headerText);
		firstPicker = root.findViewById(R.id.time_preference_first_picker);
		//add a listener for hourpicker to insure, that when hourpicker is set to 0, min value for minutepicker is set to 10 in order to work with doze alarm restrictions
		firstPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
		
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				// TODO Auto-generated method stub
				if(newVal==0){
					secondPicker.setMinValue(10);
					secondPicker.setValue(10);
				} else {
					secondPicker.setMinValue(0);
				}
			}
		});

		secondPicker = (NumberPicker)root.findViewById(R.id.time_preference_second_picker);
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

		firstPicker.setMaxValue(firstMaxValue);
		secondPicker.setMaxValue(secondMaxValue);

		firstPicker.setMinValue(firstMinValue);
		secondPicker.setMinValue(secondMinValue);
		return root;
	}
	
	@Override
	protected void onBindDialogView(View v){
		super.onBindDialogView(v);

		firstPicker.setValue(lastHour);
		if(lastHour==0){
			secondPicker.setMinValue(10);
		} else {
			secondPicker.setMinValue(0);
		}
		secondPicker.setValue(lastMinute);

		
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult){
		super.onDialogClosed(positiveResult);
		if(positiveResult){
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
			
			if(callChangeListener(time)){
				persistString(time);
			}
		}
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index){
		return a.getString(index);
	}
	
	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue){
		String time;
		
		if(restoreValue){
			if (defaultValue == null){
				time = getPersistedString("00:00");
			} else {
				time = getPersistedString(defaultValue.toString());
			}
		} else {
			time = defaultValue.toString();
		}
		
		lastHour = getHour(time);
		lastMinute = getMinute(time);
	}
	
	private static class SavedState extends BaseSavedState {
	    // Member that holds the setting's value
	    // Change this data type to match the type saved by your Preference
	    String value;

	    SavedState(Parcelable superState) {
	        super(superState);
	    }

	    SavedState(Parcel source) {
	        super(source);
	        // Get the current preference's value
	        value = source.readString();  // Change this to read the appropriate data type
	    }

	    @Override
	    public void writeToParcel(Parcel dest, int flags) {
	        super.writeToParcel(dest, flags);
	        // Write the preference's value
	        dest.writeString(value);  // Change this to write the appropriate data type
	    }

	    // Standard creator object using an instance of this class
	    public static final Parcelable.Creator<SavedState> CREATOR =
	            new Parcelable.Creator<SavedState>() {

	        public SavedState createFromParcel(Parcel in) {
	            return new SavedState(in);
	        }

	        public SavedState[] newArray(int size) {
	            return new SavedState[size];
	        }
	    };
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
	    final Parcelable superState = super.onSaveInstanceState();
	    // Check whether this Preference is persistent (continually saved)
	    /*
	    if (isPersistent()) {
	        // No need to save instance state since it's persistent, use superclass state
	        return superState;
	    }
		*/
	    // Create instance of custom BaseSavedState
	    final SavedState myState = new SavedState(superState);
	    // Set the state's value with the class member that holds current setting value
	    if(firstPicker!=null && secondPicker != null){
	    	restoreHour = firstPicker.getValue();
	    	restoreMinute = secondPicker.getValue();
	    }
	    myState.value = String.valueOf(restoreHour) + ":" + String.valueOf(restoreMinute);
	    return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
	    // Check whether we saved the state in onSaveInstanceState
	    if (state == null || !state.getClass().equals(SavedState.class)) {
	        // Didn't save the state, so call superclass
	        super.onRestoreInstanceState(state);
	        return;
	    }
	    // Cast state to custom BaseSavedState and pass to superclass
	    SavedState myState = (SavedState) state;
	    super.onRestoreInstanceState(myState.getSuperState());
	    // Set this Preference's widget to reflect the restored state
		int rotatedHour = getHour(myState.value);
		int rotatedMinute = getMinute(myState.value);
		if (firstPicker!=null && secondPicker != null){
			firstPicker.setValue(rotatedHour);
			secondPicker.setValue(rotatedMinute);
		}
	}

}
