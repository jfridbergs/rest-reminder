package com.colormindapps.rest_reminder_alarm;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;


public class IntroductionDialog extends DialogFragment {

	
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
        	//OnExtendDialogSelectedListener parentActivity = (OnExtendDialogSelectedListener) getActivity();
        	setParentActivity((OnDialogCloseListener) getActivity());
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnExtendDialogSelectedListener");
        }
    }
    
	private OnDialogCloseListener parentActivity;
	
	private void setParentActivity(OnDialogCloseListener activity){
		parentActivity = activity;
	}
	
	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = requireActivity().getLayoutInflater();
	    
	    

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
		View view = inflater.inflate(R.layout.introduction_dialog,new LinearLayout(getActivity()), false);
	    builder.setView(view)
				.setCancelable(false)
	    // Add action buttons
	           .setPositiveButton(R.string.eula_accept, (dialog, id) -> {
				   parentActivity.dialogIsClosed(true);
				   Objects.requireNonNull(IntroductionDialog.this.getDialog()).cancel();
			   })
				.setNegativeButton(R.string.eula_reject, (dialog, id) -> parentActivity.exitApplication())
;
		TextView eula = view.findViewById(R.id.eula_text);
		eula.setMovementMethod(new ScrollingMovementMethod());
		eula.setText(readFile(requireActivity()));
	    return builder.create();
	}

	private CharSequence readFile(Activity activity) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(
					activity.getResources().openRawResource(R.raw.eula)));
			String line;
			StringBuilder buffer = new StringBuilder();
			while ((line = in.readLine()) != null) buffer.append(line).append('\n');
			return buffer;
		} catch (IOException e) {
			return "";
		} finally {
			closeStream(in);
		}
	}

	/**
	 * Closes the specified stream.
	 *
	 * @param stream The stream to close.
	 */
	private void closeStream(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				// Ignore
			}
		}
	}
	
    public static IntroductionDialog newInstance(int title) {
    	IntroductionDialog frag = new IntroductionDialog();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

	@Override
	public void onDismiss(@NonNull DialogInterface dialog){
		parentActivity.dialogIsClosed(false);
		parentActivity = null;
		super.onDismiss(dialog);
	}

	@Override
	public void onCancel(@NonNull DialogInterface dialog){
	}


}
