package com.colormindapps.rest_reminder_alarm;

public interface OnDialogCloseListener {

	void stopCountDownTimerForDialog();
	void cancelNotificationForDialog (long periodEndtime,boolean removeOnGoing);
	void resumeCounter(boolean positiveDismissal);
	void bindFromFragment(long newPeriondEndTimeValue);
	void unbindFromFragment();
	void startReminder();
    void dialogIsClosed(boolean eulaAccepted);
	void exitApplication();

}
