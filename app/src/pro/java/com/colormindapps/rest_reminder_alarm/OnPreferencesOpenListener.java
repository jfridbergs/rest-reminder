package com.colormindapps.rest_reminder_alarm;

public interface OnPreferencesOpenListener {

	void updateWearStatusFromPreference(int type, long periodEndTimeValue,int extendCount);
	void updateWearPreferences(String workLength, String restLength, int extendLength);

}
