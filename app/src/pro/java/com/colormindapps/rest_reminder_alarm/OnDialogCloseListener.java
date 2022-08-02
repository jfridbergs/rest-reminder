package com.colormindapps.rest_reminder_alarm;

public interface OnDialogCloseListener {

	void cancelNotificationForDialog(long periodEndTime, boolean removeOnGoing);
	void resumeCounter(boolean positiveDismissal);
	void bindFromFragment(long newPeriodEndTimeValue);
	void unbindFromFragment();
	void startReminder();
    void dialogIsClosed(boolean eulaAccepted);
	void exitApplication();

}
