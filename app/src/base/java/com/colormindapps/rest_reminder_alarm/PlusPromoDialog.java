package com.colormindapps.rest_reminder_alarm;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;


public class PlusPromoDialog extends DialogFragment {
	WebView patchnotes;

	
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
        	//OnExtendDialogSelectedListener parentActivity = (OnExtendDialogSelectedListener) getActivity();
        	setParentActivity((OnPromoDialogListener) getActivity());
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement OnExtendDialogSelectedListener");
        }
    }
    
	private OnPromoDialogListener parentActivity;
	
	private void setParentActivity(OnPromoDialogListener activity){
		parentActivity = activity;
	}
	
	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = requireActivity().getLayoutInflater();
	    
	    

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
		View view = inflater.inflate(R.layout.plus_promo,new ConstraintLayout(requireActivity()), false);
	    builder.setView(view)
				.setCancelable(true)
	    // Add action buttons
	           .setPositiveButton(R.string.get_plus, (dialog, id) -> {
				   parentActivity.promoDialogIsClosed();
				   parentActivity.openPlusInPlay();
				   Objects.requireNonNull(PlusPromoDialog.this.getDialog()).cancel();
			   })
				.setNegativeButton(R.string.close, (dialog, id) -> {
					parentActivity.promoDialogIsClosed();
					Objects.requireNonNull(PlusPromoDialog.this.getDialog()).cancel();
				})
;
		patchnotes = view.findViewById(R.id.patch_notes);
		patchnotes.loadUrl("file:///android_asset/html/rest_reminder_plus.html");
		patchnotes.getSettings().setMediaPlaybackRequiresUserGesture(false);
	    return builder.create();
	}



	
    public static PlusPromoDialog newInstance(int title) {
    	PlusPromoDialog frag = new PlusPromoDialog();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

	@Override
	public void onDismiss(@NonNull DialogInterface dialog){
		parentActivity = null;
		super.onDismiss(dialog);
	}

	@Override
	public void onCancel(@NonNull DialogInterface dialog){
	}


}
