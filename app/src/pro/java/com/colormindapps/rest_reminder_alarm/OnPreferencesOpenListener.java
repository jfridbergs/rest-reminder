package com.colormindapps.rest_reminder_alarm;

import android.os.Bundle;

public interface OnPreferencesOpenListener {
	Bundle getDataFromService();
	void updateWearStatusFromPreference(int type, long periodEndTimeValue,int extendCount);
	void updateWearPreferences(String reminderMode, String workLength, String restLength, int extendLength, boolean extendEnabled, boolean startNextEnabled);

}
