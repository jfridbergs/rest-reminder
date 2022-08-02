package com.colormindapps.rest_reminder_alarm;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import java.util.Objects;


public class ClearDbDialog extends DialogFragment {

	private OnClearDbDialogCloseListener parentActivity;

	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = requireActivity().getLayoutInflater();
	    
	    

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
		View view = inflater.inflate(R.layout.clear_db_dialog,new LinearLayout(getActivity()), false);
	    builder.setView(view)
				.setCancelable(true)
				.setTitle(R.string.clear_db_dialog_title)
	    // Add action buttons
	           .setPositiveButton(android.R.string.ok, (dialog, id) -> {
	           		parentActivity.deleteDB();
				   Objects.requireNonNull(ClearDbDialog.this.getDialog()).cancel();
			   })
				.setNegativeButton(android.R.string.cancel, (dialog, id) -> Objects.requireNonNull(ClearDbDialog.this.getDialog()).cancel())
;
	    return builder.create();
	}

	private void setParentActivity(OnClearDbDialogCloseListener activity){
		parentActivity = activity;
	}


	
    public static ClearDbDialog newInstance(int title) {
    	ClearDbDialog frag = new ClearDbDialog();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

	@Override
	public void onDismiss(@NonNull DialogInterface dialog){
		super.onDismiss(dialog);
	}

	@Override
	public void onCancel(@NonNull DialogInterface dialog){
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		try {
			//OnExtendDialogSelectedListener parentActivity = (OnExtendDialogSelectedListener) getActivity();
			setParentActivity((OnClearDbDialogCloseListener) getActivity());
		} catch (ClassCastException e) {
			throw new ClassCastException(context + " must implement OnClearDbDialogCloseListener");
		}
	}


}
