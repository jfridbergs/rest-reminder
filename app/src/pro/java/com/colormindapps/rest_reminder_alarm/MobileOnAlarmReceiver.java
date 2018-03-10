package com.colormindapps.rest_reminder_alarm;

import android.content.Context;
import android.content.Intent;

import com.colormindapps.rest_reminder_alarm.shared.OnAlarmReceiver;


public class MobileOnAlarmReceiver extends OnAlarmReceiver {

	public MobileOnAlarmReceiver(){

	}
	@Override
	public Intent getAlarmServiceIntent(Context context){
		return new Intent(context, MobilePeriodService.class);
	}

}
