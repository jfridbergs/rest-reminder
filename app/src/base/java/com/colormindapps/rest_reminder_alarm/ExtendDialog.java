package com.colormindapps.rest_reminder_alarm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.Calendar;
import java.util.Objects;


public class ExtendDialog extends DialogFragment{
	
	
	private int extendCount;
	private int periodType;
	private int activityType;
	private long periodEndTimeValue;
	private OnDialogCloseListener parentActivity;
	public boolean dialogIsOpen;
	private boolean positiveDismissal = false;
	private Context context;
	
	private void setParentActivity(OnDialogCloseListener activity){
		parentActivity = activity;
	}
	
	@Override
	public void onDismiss(@NonNull DialogInterface dialog){
		parentActivity.resumeCounter(positiveDismissal);
		dialogIsOpen = false;
		positiveDismissal = false;
		parentActivity = null;
		super.onDismiss(dialog);
	}
	
	
	
	
	
	
	

	
	public static ExtendDialog newInstance(int title, int periodType, int extendCount, long periodEndTimeValue, int activityType){
		ExtendDialog fragment = new ExtendDialog();
		Bundle args = new Bundle();
		args.putInt("title", title);
		args.putInt(RReminder.PERIOD_TYPE, periodType);
		args.putInt(RReminder.EXTEND_COUNT, extendCount);
		args.putLong(RReminder.PERIOD_END_TIME, periodEndTimeValue);
		args.putInt(RReminder.ACTIVITY_TYPE, activityType);
		fragment.setArguments(args);
		return fragment;
	}
	

	
	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		Bundle data = getArguments();
		context = getActivity();
		if(data!=null){
			periodType = data.getInt(RReminder.PERIOD_TYPE);
			extendCount = data.getInt(RReminder.EXTEND_COUNT);
			periodEndTimeValue = data.getLong(RReminder.PERIOD_END_TIME);
			activityType = data.getInt(RReminder.ACTIVITY_TYPE);
		}
		LayoutInflater inflater = requireActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.extend_dialog,new LinearLayout(context), false);
		builder.setView(view);
		builder.setNegativeButton(R.string.extend_dialog_close_dialog_text, (dialog, id) -> Objects.requireNonNull(ExtendDialog.this.getDialog()).cancel());
		
		int extendBaseLength = RReminder.getExtendBaseLength(context);

		Button extendOptionButton = view.findViewById(R.id.extend_dialog_button_extend);
		extendOptionButton.setText(String.format(getString(R.string.extend_dialog_button), extendBaseLength));
		extendOptionButton.setOnClickListener(v -> buttonAction(context,1));
		
		if(RReminder.getExtendOptionsCount(context)>1){
			Button extendOptionButton1 = view.findViewById(R.id.extend_dialog_button_extend1);
			extendOptionButton1.setText(String.format(getString(R.string.extend_dialog_button), extendBaseLength*2));
			extendOptionButton1.setVisibility(View.VISIBLE);
			extendOptionButton1.setOnClickListener(v -> buttonAction(context, 2));
		}
		
		if(RReminder.getExtendOptionsCount(context)>2){
			Button extendOptionButton2 = view.findViewById(R.id.extend_dialog_button_extend2);
			extendOptionButton2.setText(String.format(getString(R.string.extend_dialog_button), extendBaseLength*3));
			extendOptionButton2.setVisibility(View.VISIBLE);
			extendOptionButton2.setOnClickListener(v -> buttonAction(context, 3));
		}
		dialogIsOpen = true;	
		return builder.create();
	}

	@Override
	public void onCancel(@NonNull DialogInterface dialog){

		super.onCancel(dialog);
	}
	
	private void buttonAction(Context context, int multiplier){
		String toastText; 
		long timeRemaining = 0L;
		positiveDismissal = true;
		switch(activityType){
			case 0:
				timeRemaining = periodEndTimeValue - Calendar.getInstance().getTimeInMillis();
				parentActivity.unbindFromFragment();
				RReminderMobile.cancelCounterAlarm(context.getApplicationContext(), periodType, extendCount,periodEndTimeValue);
				toastText = getString(R.string.toast_period_end_extended);
				break;
			case 1:
				RReminderMobile.cancelCounterAlarm(context.getApplicationContext(), RReminder.getNextType(periodType), extendCount,periodEndTimeValue);
				toastText = getString(R.string.notification_toast_period_extended);
				break;
			default:
				toastText = "activity type exception"; break;
		}

		int functionType = periodType;
		long functionCalendar;


		
		switch(functionType){
		case RReminder.WORK:  functionType = RReminder.WORK_EXTENDED; break;
		case RReminder.REST:  functionType = RReminder.REST_EXTENDED; break;
		default: break;
		}
		
		extendCount+=1;
		functionCalendar = RReminder.getTimeAfterExtend(context.getApplicationContext(), multiplier, timeRemaining);
		new MobilePeriodManager(context.getApplicationContext()).setPeriod(functionType, functionCalendar, extendCount);
		RReminderMobile.startCounterService(context.getApplicationContext(), functionType, extendCount, functionCalendar, false, false);
		parentActivity.cancelNotificationForDialog(functionCalendar,false);
		switch(activityType){
		case 0:
			parentActivity.bindFromFragment(functionCalendar);
			Objects.requireNonNull(ExtendDialog.this.getDialog()).cancel();
			dialogIsOpen = false;
			break;
		case 1:
			Intent i = new Intent(context, MainActivity.class);
            i.setAction(RReminder.PERIOD_EXTENDED_FROM_NOTIFICATION_ACTIVITY);
			i.putExtra(RReminder.PERIOD_END_TIME, functionCalendar);
			i.putExtra(RReminder.START_COUNTER, false);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			dialogIsOpen = false;
			context.startActivity(i);
			requireActivity().finish();
			break;
		default: break;
		}

		Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
	}
	

	
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
        	//OnExtendDialogSelectedListener parentActivity = (OnExtendDialogSelectedListener) getActivity();
        	setParentActivity((OnDialogCloseListener) getActivity());
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement OnExtendDialogSelectedListener");
        }
    }

}
