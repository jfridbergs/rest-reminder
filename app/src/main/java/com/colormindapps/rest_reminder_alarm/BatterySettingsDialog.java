package com.colormindapps.rest_reminder_alarm;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.M)
public class BatterySettingsDialog extends DialogFragment {

	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = requireActivity().getLayoutInflater();
	    
	    

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
		View view = inflater.inflate(R.layout.battery_settings_dialog,new LinearLayout(getActivity()), false);
	    builder.setView(view)
				.setCancelable(true)
	    // Add action buttons
	           .setPositiveButton(android.R.string.ok, (dialog, id) -> {
	           		openOptimizationSettings();
				   Objects.requireNonNull(BatterySettingsDialog.this.getDialog()).cancel();
			   })
				.setNegativeButton(android.R.string.cancel, (dialog, id) -> {
					Objects.requireNonNull(BatterySettingsDialog.this.getDialog()).cancel();
				})
;
	    return builder.create();
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	public void openOptimizationSettings() {
		Intent intent = new Intent();
		intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
		requireActivity().startActivity(intent);
	}


	
    public static BatterySettingsDialog newInstance(int title) {
    	BatterySettingsDialog frag = new BatterySettingsDialog();
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


}
