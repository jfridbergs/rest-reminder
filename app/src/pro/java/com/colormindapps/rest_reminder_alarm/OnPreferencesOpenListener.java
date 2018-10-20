package com.colormindapps.rest_reminder_alarm;

public interface OnPreferencesOpenListener {

	void updateWearStatusFromPreference(int type, long periodEndTimeValue,int extendCount);
	void updateWearPreferences(String reminderMode, String workLength, String restLength, int extendLength, boolean extendEnabled, boolean startNextEnabled);

}
