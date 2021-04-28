package com.colormindapps.rest_reminder_alarm;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;


public class PatchNotesDialog extends DialogFragment {
	WebView patchnotes;

	
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
		View view = inflater.inflate(R.layout.patch_notes_dialog,new LinearLayout(getActivity()), false);
	    builder.setView(view)
				.setCancelable(true)
	    // Add action buttons
	           .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
                       parentActivity.dialogIsClosed(true);
	            	   PatchNotesDialog.this.getDialog().cancel();
	               }
	           })
;
		patchnotes = (WebView) view.findViewById(R.id.patch_notes);
		patchnotes.loadUrl("file:///android_asset/html/patchnotes.html");
	    return builder.create();
	}


	
    public static PatchNotesDialog newInstance(int title) {
    	PatchNotesDialog frag = new PatchNotesDialog();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

	@Override
	public void onDismiss(DialogInterface dialog){
		parentActivity = null;
		super.onDismiss(dialog);
	}

	@Override
	public void onCancel(DialogInterface dialog){
	}


}
