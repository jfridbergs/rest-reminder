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



public class NumberPreference extends DialogPreference {
	private NumberPicker numberPicker;
	private int lastValue, minValue, maxValue;
	private int restoreValue;
	private String headerText;

	
	public NumberPreference(Context context, AttributeSet attrs){
		super(context, attrs);
		init(attrs);
		setDialogLayoutResource(R.layout.number_preference);
		setPositiveButtonText(context.getString(R.string.time_preference_set_text));
		setNegativeButtonText(context.getString(R.string.time_preference_cancel_text));
	}
	
	private void init(AttributeSet attrs){
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.NumberPreference);
		minValue = a.getInteger(R.styleable.NumberPreference_numberMinValue,1);
		maxValue = a.getInteger(R.styleable.NumberPreference_numberMaxValue,10);
		headerText = a.getString(R.styleable.NumberPreference_numberDialogTopText);
		a.recycle();
	}
	
	@Override
	protected View onCreateDialogView(){
		View root = super.onCreateDialogView();
		TextView tv = (TextView)root.findViewById(R.id.number_preference_title);
		tv.setText(headerText);
		numberPicker = (NumberPicker)root.findViewById(R.id.number_preference_picker);
		numberPicker.setMaxValue(maxValue);
		numberPicker.setMinValue(minValue);
		
		return root;
	}
	
	@Override
	protected void onBindDialogView(View v){
		super.onBindDialogView(v);
		numberPicker.setValue(lastValue);
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult){
		super.onDialogClosed(positiveResult);
		if(positiveResult){
			lastValue = numberPicker.getValue();
			
			if(callChangeListener(lastValue)){
				persistInt(lastValue);
			}
		}
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index){
		return a.getInt(index,1);
	}
	
	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue){
		int value;
		if(restoreValue){
			if(defaultValue == null){
				value = getPersistedInt(1);
			} else {
				value = getPersistedInt(Integer.parseInt(defaultValue.toString()));
			}
		} else {
			value = Integer.parseInt(defaultValue.toString());
		}
		
		lastValue = value;
	}
	
	private static class NumberSavedState extends BaseSavedState {
	    // Member that holds the setting's value
	    // Change this data type to match the type saved by your Preference
	    int value;

	    NumberSavedState(Parcelable superState) {
	        super(superState);
	    }

	    NumberSavedState(Parcel source) {
	        super(source);
	        // Get the current preference's value
	        value = source.readInt();  // Change this to read the appropriate data type
	    }

	    @Override
	    public void writeToParcel(Parcel dest, int flags) {
	        super.writeToParcel(dest, flags);
	        // Write the preference's value
	        dest.writeInt(value);  // Change this to write the appropriate data type
	    }

	    // Standard creator object using an instance of this class
	    public static final Parcelable.Creator<NumberSavedState> CREATOR =
	            new Parcelable.Creator<NumberSavedState>() {

	        public NumberSavedState createFromParcel(Parcel in) {
	            return new NumberSavedState(in);
	        }

	        public NumberSavedState[] newArray(int size) {
	            return new NumberSavedState[size];
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
	    final NumberSavedState myState = new NumberSavedState(superState);
	    // Set the state's value with the class member that holds current setting value
	    if(numberPicker!=null){
	    	restoreValue = numberPicker.getValue();
	    }
	    myState.value = restoreValue;
	    return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
	    // Check whether we saved the state in onSaveInstanceState
	    if (state == null || !state.getClass().equals(NumberSavedState.class)) {
	        // Didn't save the state, so call superclass
	        super.onRestoreInstanceState(state);
	        return;
	    }

	    // Cast state to custom BaseSavedState and pass to superclass
	    NumberSavedState myState = (NumberSavedState) state;
	    super.onRestoreInstanceState(myState.getSuperState());
	    
	    // Set this Preference's widget to reflect the restored state
	    if(numberPicker!=null){
	    	numberPicker.setValue(myState.value);
	    }
	}
}