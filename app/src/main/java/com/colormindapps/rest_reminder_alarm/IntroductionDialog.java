package com.colormindapps.rest_reminder_alarm;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;


public class IntroductionDialog extends DialogFragment {

	
    @Override
    public void onAttach(Context context) {
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
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	    
	    

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
		View view = inflater.inflate(R.layout.introduction_dialog,new LinearLayout(getActivity()), false);
	    builder.setView(view)
				.setCancelable(false)
	    // Add action buttons
	           .setPositiveButton(R.string.eula_accept, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
                       parentActivity.dialogIsClosed(true);
	            	   IntroductionDialog.this.getDialog().cancel();
	               }
	           })
				.setNegativeButton(R.string.eula_reject, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   parentActivity.exitApplication();
	               }
	           })
;
		TextView eula = (TextView) view.findViewById(R.id.eula_text);
		eula.setMovementMethod(new ScrollingMovementMethod());
		eula.setText(readFile(getActivity(),R.raw.eula));
	    return builder.create();
	}

	private CharSequence readFile(Activity activity, int id) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(
					activity.getResources().openRawResource(id)));
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
	public void onDismiss(DialogInterface dialog){
		parentActivity.dialogIsClosed(false);
		parentActivity = null;
		super.onDismiss(dialog);
	}

	@Override
	public void onCancel(DialogInterface dialog){
	}


}
