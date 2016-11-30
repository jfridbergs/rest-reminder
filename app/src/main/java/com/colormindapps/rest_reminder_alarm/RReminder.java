package com.colormindapps.rest_reminder_alarm;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;

import java.util.Calendar;


public class RReminder {
	public static final String PERIOD_TYPE = "period_type";
	public static final String TURN_OFF = "turn_off";
	public static final String MANUAL_MODE_NEXT_PERIOD_TYPE = "manual_mode_next_period_type";
	public static final String PERIOD_END_TIME = "period_end";
	public static final String EXTENDED_PERIOD_TYPE = "extended_period_type";
	public static final String START_COUNTER = "start_counter";
	public static final String EXTEND_COUNT = "extend_count";
	public static final String COUNTER_TIME_VALUE = "counter_time_value";
	public static final String ACTIVITY_TYPE = "activity_type";
	public static final String EXCLUDE_ONGOING = "exclude_ongoing";
	public static final String PLAY_SOUND = "play_sound";
	public static final String ANIMATE_INFO = "animate_info";
	public static final String PERIOD_END_REDIRECT = "period_end_redirect";
	public static final String PERIOD_END_DISMISS = "period_end_dismiss";
	public static final String REDIRECT_SCREEN_OFF = "intent-extra_redirect_screen_off";
	public static final String COUNTERSERVICE_STATUS = "counter_service_status";
	public static final int DEFAULT_EXTEND_COUNT = 1;
	public static final int DEFAULT_EXTEND_BASE_LENGTH = 5;
	public static final int NOTIFICATION_ID = 1;
	public static final String DEFAULT_WORK_PERIOD_STRING = "00:45";
	public static final String DEFAULT_REST_PERIOD_STRING = "00:15";
	public static final String DEFAULT_APPROX_TIME_STRING = "00:30";
	public static final String CUSTOM_INTENT_TURN_OFF = "com.colormindapps.rest_reminder_alarm.ACTION_TURN_OFF_SCHEDULER";
	public static final String CUSTOM_INTENT_MANUAL_START_NEXT_PERIOD = "com.colormindapps.rest_reminder_alarm.ACTION_MANUAL_START_NEXT_PERIOD";
	public static final String CUSTOM_INTENT_VIEW_NOTIFICATION_ACTIVITY = "com.colormindapps.rest_reminder_alarm.ACTION_VIEW_PERIOD_END_NOTIFICATION";
	public static final String CUSTOM_INTENT_CLEAR_NOTIFICATION = "com.colormindapps.rest_reminder_alarm.ACTION_CLEAR_NOTIFICATION";
	public static final String CUSTOM_INTENT_WEAR_EXTEND_PERIOD= "com.colormindapps.rest_reminder_alarm.ACTION_WEAR_EXTEND";
	public static final String CUSTOM_INTENT_VIEW_MAIN_ACTIVITY = "com.colormindapps.rest_reminder_alarm.ACTION_VIEW_MAIN_ACTIVITY";
	public static final String CUSTOM_INTENT_ALARM_PERIOD_END = "com.colormindapps.rest_reminder_alarm.ACTION_ALARM_PERIOD_END";
	public static final String CUSTOM_INTENT_APPROXIMATE_PERIOD_END = "com.colormindapps.rest_reminder_alarm.ACTION_APPROXIMATE_PERIOD_END";
    public static final String PERIOD_EXTENDED_FROM_NOTIFICATION_ACTIVITY = "com.colormindapps.rest_reminder_alarm.ACTION_PERIOD_EXTENDED_FROM_NOTIFICATION_ACT";

	public static final int PREFERENCE_SUMMARY_HHMM = 0;
	public static final int PREFERENCE_SUMMARY_MMSS = 1;

	public static final int EXTEND_PERIOD_SINGLE_OPTION = 1;
	public static final int EXTEND_PERIOD_WEAR = 2;

	//constants for ShowCaseView
	public static final int SHOWCASEVIEW_START = 1;
	public static final int SHOWCASEVIEW_STOP = 2;
	public static final int SHOWCASEVIEW_EXTEND = 3;
	public static final int SHOWCASEVIEW_FORCE_END = 4;
	public static final int SHOWCASEVIEW_MENU = 5;

	//variables for testing purposes. remove after finishing testing
	public static final String CUSTOM_INTENT_TEST_PREFERENCES = "com.colormindapps.rest_reminder_alarm.ACTION_TEST_PREFERENCES";
	public static final String PREFERENCE_MODE_SUMMARY = "preference_mode_summary";
	public static final String PREFERENCE_WORK_LENGTH_SUMMARY = "preference_work_length_summary";
	public static final String PREFERENCE_REST_LENGTH_SUMMARY = "preference_rest_length_summary";
	public static final String PREFERENCE_PROXIMITY_LENGTH_SUMMARY = "preference_proximity_length_summary";
	public static final String PREFERENCE_EXTEND_COUNT_SUMMARY = "preference_extend_count_summary";
	public static final String PREFERENCE_EXTEND_LENGTH_SUMMARY = "preference_extend_length_summary";
	public static final String PREFERENCE_WORK_AUDIO_SUMMARY = "preference_work_audio_summary";
	public static final String PREFERENCE_REST_AUDIO_SUMMARY = "preference_rest_audio_summary";
	public static final String PREFERENCE_PROXIMITY_AUDIO_SUMMARY = "preference_proximity_audio_summary";
	//end of testing variables

	
    public static final String PRIVATE_PREF = "myapp";
    public static final String VERSION_KEY = "version_number";
	public static final String EULA_ACCEPTED="eula_accepted";
    public static final String TIME_FORMAT_24H = "kk:mm";
    public static final String TIME_FORMAT_12H = "hh:mm aa";
    
	public static int getShortestPeriodLength(Context context){
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String workPeriod = prefs.getString(context.getString(R.string.pref_work_period_length_key), context.getString(R.string.default_work_length_string));
		String restPeriod = prefs.getString(context.getString(R.string.pref_rest_period_length_key), context.getString(R.string.default_rest_length_string));
		int workPeriodValue = getHourFromString(workPeriod) * 60 + getMinuteFromString(workPeriod);
		int restPeriodValue = getHourFromString(restPeriod) * 60 + getMinuteFromString(restPeriod);
		if(workPeriodValue > restPeriodValue){
			return restPeriodValue;
		} else {
			return workPeriodValue;
		}
	}
	
	public static long getTimeAfterExtend(Context context, int extendType, long timeRemaining){
		int addMins = getExtendBaseLength(context);
		long currentTime = Calendar.getInstance().getTimeInMillis();
		long addTime = timeRemaining + (long)addMins*60000L* (long)extendType;
		return currentTime + addTime;
	}
	
	public static int getExtendBaseLength(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getInt(context.getString(R.string.pref_period_extend_length_key),DEFAULT_EXTEND_BASE_LENGTH);
	}
	
	public static CharSequence getTimeString(Context context, long time){
		CharSequence periodEndTime, timeFormatString;
		if (DateFormat.is24HourFormat(context)){
			timeFormatString = TIME_FORMAT_24H;
		} else {
			timeFormatString = TIME_FORMAT_12H;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		periodEndTime = DateFormat.format(timeFormatString,calendar.getTime());
		return periodEndTime;
	}
	
	public static int getMode(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String modeKey = context.getString(R.string.pref_mode_key);
		String mode = prefs.getString(modeKey, null);
		if (mode!= null){
			return Integer.parseInt(mode);
		} else {
			return 0;
		}
	}
	
	public static String getPreferencePeriodLength(Context context,int type){
		String result;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		switch(type){
		case 1: 
			result = prefs.getString(context.getString(R.string.pref_work_period_length_key),context.getString(R.string.default_work_length_string));
			break;
		case 2:
			result = prefs.getString(context.getString(R.string.pref_rest_period_length_key),context.getString(R.string.default_rest_length_string));
			break;
		default: 
			result = prefs.getString(context.getString(R.string.pref_work_period_length_key),context.getString(R.string.default_work_length_string));
			break;
		}
		return result;
	}
	
	public static boolean isApproxEnabled(Context context){
		String key = context.getString(R.string.pref_enable_approx_notification_key);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(key, false);
	}
	
	public static boolean isEndPeriodEnabled(Context context){
		String key = context.getString(R.string.pref_end_period_key);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(key, true);
	}
	
	public static boolean isVibrateEnabled(Context context, Vibrator v){
		String key = context.getString(R.string.pref_enable_vibrate_key);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		return v.hasVibrator() && prefs.getBoolean(key, true);

		
	}
	
	public static boolean isVibrateEnabledSupport(Context context){
		String key = context.getString(R.string.pref_enable_vibrate_key);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		return context.getSystemService(Context.VIBRATOR_SERVICE) != null && prefs.getBoolean(key, true);


	}
	
	public static boolean isExtendEnabled(Context context){
		String key = context.getString(R.string.pref_enable_extend_key);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(key, true);
	}
	
	public static boolean isLedEnabled(Context context){
		String key = context.getString(R.string.pref_show_led_key);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(key, true);
	}
	
	public static int getExtendOptionsCount(Context context){
		String key = context.getString(R.string.pref_period_extend_options_key);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getInt(key, 1);
	}
	
	public static long getApproxTime(Context context, long periodEndTime){
		String approxKey;
		String approxTime;
		long calendar;
		long approxTimeValue;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		approxKey = context.getString(R.string.pref_approx_notification_length_key);
		approxTime = prefs.getString(approxKey,context.getString(R.string.default_approx_length_string));
		int shortestPeriodLength = getShortestPeriodLength(context) * 60;
		if(getHourFromString(approxTime)*60 + getMinuteFromString(approxTime) >= shortestPeriodLength){
			approxTimeValue = shortestPeriodLength - 1;
		} else {
			approxTimeValue = getHourFromString(approxTime)*60 + getMinuteFromString(approxTime);
		}
		calendar = periodEndTime - approxTimeValue*1000L;
		return calendar;
		
	}
	
	public static Uri getRingtone(Context context, int type){
		Uri uri;
		String ringtoneString;
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
		
		switch(type){
		case 1: case 3: 
			ringtoneString = preference.getString(context.getString(R.string.pref_work_period_start_sound_key),"DEFAULT_SOUND");
			break;
		case 2: case 4: 
			ringtoneString = preference.getString(context.getString(R.string.pref_rest_period_start_sound_key),"DEFAULT_SOUND");
			break;
		case 99: 
			ringtoneString = preference.getString(context.getString(R.string.pref_approx_time_sound_key),"DEFAULT_SOUND");
			break;
		default: 
			ringtoneString = preference.getString(context.getString(R.string.pref_work_period_start_sound_key),"DEFAULT_SOUND");
			break;
		}
		if (ringtoneString.equals("DEFAULT_SOUND")){
			uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		} else {
			uri = Uri.parse(ringtoneString);
		}
		return uri;
	}
	
	public static void cancelCounterAlarm(Context context, int type, int extendCount, long endTime, boolean approxOnly, long oldApproxValue){
		AlarmManager mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pi, pa;
		if (isApproxEnabled(context)){
			Intent ia = new Intent(context, OnAlarmReceiver.class);
			ia.putExtra(PERIOD_TYPE, type);
			ia.putExtra(EXTEND_COUNT, 0);
			ia.setAction(CUSTOM_INTENT_APPROXIMATE_PERIOD_END);
			if(approxOnly){
				pa = PendingIntent.getBroadcast(context, (int)oldApproxValue, ia, PendingIntent.FLAG_ONE_SHOT);
			} else {
				pa = PendingIntent.getBroadcast(context, (int)getApproxTime(context, endTime), ia, PendingIntent.FLAG_ONE_SHOT);
			}

			mAlarmManager.cancel(pa);
			pa.cancel();
		}

		if(!approxOnly){
			Intent i = new Intent(context, OnAlarmReceiver.class);
			i.putExtra(PERIOD_TYPE, type);
			i.putExtra(EXTEND_COUNT, extendCount);
			i.setAction(CUSTOM_INTENT_ALARM_PERIOD_END);
			pi = PendingIntent.getBroadcast(context, (int)endTime, i, PendingIntent.FLAG_ONE_SHOT);
			mAlarmManager.cancel(pi);
			pi.cancel();
		}
	}
	
	public static void stopCounterService(Context context, int type){
		Intent i = new Intent(context, CounterService.class);
		i.putExtra(PERIOD_TYPE,type);
		context.stopService(i);	
	}
	
	public static void startCounterService(Context context, int type, int extendCount, long periodEndTime, boolean excludeOngoing ){
		Intent i = new Intent(context, CounterService.class);
		i.putExtra(PERIOD_TYPE,type);
		i.putExtra(PERIOD_END_TIME, periodEndTime);
		i.putExtra(EXTEND_COUNT, extendCount);
		i.putExtra(EXCLUDE_ONGOING, excludeOngoing);
		context.startService(i);
	}
	
	
	public static boolean isActiveModeNotificationEnabled(Context context){
		String key = context.getString(R.string.pref_show_is_on_icon_key);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(key, true);
	}

	
	public static boolean dismissDialogs(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(PERIOD_END_DISMISS, false);
	}

	
	public static void addDismissDialogFlag(Context context){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Editor ed = sp.edit();
        ed.putBoolean(RReminder.PERIOD_END_DISMISS, true);
        ed.commit();
	}

	
	public static void removeDismissDialogFlag(Context context){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Editor ed = sp.edit();
        ed.putBoolean(RReminder.PERIOD_END_DISMISS, false);
        ed.commit();
	}
	
	public static int getNextType(int oldType){
		int type;
		switch(oldType){
		case 1: case 3: type = 2; break;
		case 2: case 4: type = 1; break;
		default: type = 1; break;
		}
		return type;
	}
	
	public static boolean isCounterServiceRunning(Context context) {

	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		if(manager != null){
			if(manager.getRunningServices(Integer.MAX_VALUE)!=null){
				for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
					if (CounterService.class.getName().equals(service.service.getClassName())) {
						return true;
					}
				}
			}

		}
		return false;
		/*
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(Scheduler.COUNTERSERVICE_STATUS, false);
		*/
	}
	
	public static long getNextPeriodEndTime (Context context, int type, long calendar, int extendMultiplier, long timeTillEndExtend){
		String period;
		int addMins;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		switch(type){
		case 1: 
			period = prefs.getString(context.getString(R.string.pref_work_period_length_key), context.getString(R.string.default_work_length_string));
			addMins = getHourFromString(period)*60 + getMinuteFromString(period);
			break;
		case 2: 
			period = prefs.getString(context.getString(R.string.pref_rest_period_length_key), context.getString(R.string.default_rest_length_string));
			addMins = getHourFromString(period)*60 + getMinuteFromString(period);
			break;
		case 3: case 4:
			addMins = prefs.getInt(context.getString(R.string.pref_period_extend_length_key), RReminder.DEFAULT_EXTEND_BASE_LENGTH) * extendMultiplier;
			break;
		default: addMins = 0; break;
		}
		
		calendar+=(long)addMins * 60000L + timeTillEndExtend;
		return calendar;
	}
	
	public static Notification updateOnGoingNotification(Context context, int type, long periodEndTime, boolean showTurnOff){
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		Intent notificationIntent = new Intent(context, MainActivity.class);
		notificationIntent.setAction(RReminder.CUSTOM_INTENT_VIEW_MAIN_ACTIVITY);
		notificationIntent.putExtra(RReminder.START_COUNTER, false);
		notificationIntent.putExtra(RReminder.PERIOD_TYPE, type);
		PendingIntent pi = PendingIntent.getActivity(context, 15 , notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && showTurnOff) {
			Intent turnOffIntent = new Intent (context, MainActivity.class);
			turnOffIntent.setAction(RReminder.CUSTOM_INTENT_TURN_OFF);
			turnOffIntent.putExtra(RReminder.TURN_OFF, 1);
			turnOffIntent.putExtra(RReminder.PERIOD_END_TIME,periodEndTime);
			PendingIntent pIntentTurnOff = PendingIntent.getActivity(context, 0, turnOffIntent, PendingIntent.FLAG_ONE_SHOT);
			builder.addAction(R.drawable.ic_notify_turn_off , context.getString(R.string.notify_turn_off), pIntentTurnOff);
		} 
		//note = new Notification(android.R.drawable.stat_notify_sync, null, System.currentTimeMillis());
		builder.setContentTitle(context.getString(R.string.notify_reminder_is_on_title));
		builder.setPriority(Notification.PRIORITY_MAX);
		switch(type){
		case 1: case 3: {
			builder.setContentText(context.getString(R.string.notify_reminder_is_on_work_message));
			builder.setSmallIcon(R.drawable.ic_notify_work_period);
			break;
		}
		case 2: case 4:  {
			builder.setContentText(context.getString(R.string.notify_reminder_is_on_rest_message));
			builder.setSmallIcon(R.drawable.ic_notify_rest_period);
			break;
		}
		default:
			builder.setContentText(context.getString(R.string.notify_reminder_is_on_work_message));
			builder.setSmallIcon(R.drawable.ic_notify_work_period);
			break;
		}
		
		//note.setLatestEventInfo(context, context.getString(R.string.notify_reminder_is_on_title), context.getString(R.string.notify_scheduler_is_on_message), pi);
		//note.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
		builder.setContentIntent(pi);
		builder.setOngoing(true);
		builder.setAutoCancel(false);
		return builder.build();
	}

	/*
	//alternative function for getting screen measurements
	public static int[] getScreenDimensions(Context context){
		int[] dimensions = new int[2];
		int width = 0, height = 0;
		final DisplayMetrics metrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(metrics);
		Display display = windowManager.getDefaultDisplay();
		Method mGetRawH = null, mGetRawW = null;

		try {
			// For JellyBean 4.2 (API 17) and onward
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
				display.getRealMetrics(metrics);

				dimensions[0] = metrics.widthPixels;
				dimensions[1] = metrics.heightPixels;
			} else {
				mGetRawH = Display.class.getMethod("getRawHeight");
				mGetRawW = Display.class.getMethod("getRawWidth");

				try {
					dimensions[0] = (Integer) mGetRawW.invoke(display);
					dimensions[1] = (Integer) mGetRawH.invoke(display);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (NoSuchMethodException e3) {
			e3.printStackTrace();
		}
		return dimensions;
	}

	*/

	public static boolean isTablet(Context context){
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		float scaleFactor = dm.density;
		float smallestWidth = Math.min(dm.widthPixels/scaleFactor, dm.heightPixels/scaleFactor);
		return smallestWidth>=500.0;
	}

	public static boolean isPortrait(Context context){
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		return dm.heightPixels > dm.widthPixels;
	}
	
	public static int adjustTitleSize(Context context, int length, boolean smallTitle){
		int size;
		boolean isTablet = isTablet(context);
		if(length > 35){
			size = 22;
			if(smallTitle){
				return 15;
			}
			if(isTablet){
				size = size * 2;
			}
			return size;
		} else if (length <= 35 && length >25){
			size = 28;
			if(smallTitle){
				return 20;
			}
			if(isTablet){
				size = size * 2;
			}
			return size;
		} else if (length <=25 && length > 15){
			size = 35;
			if(smallTitle){
				return 26;
			}
			if(isTablet){
				size = size * 2;
			}
			return size;
		} else {
			size = 50;
			if(smallTitle){
				return 34;
			}
			
			if(isTablet){
				size = size * 2;
			}
		}
		return size;
	}


	public static String getFormatedValue(Context context, int type, String value){
		String firstPart = "", secondPart = "";
		int hour = getHourFromString(value);
		int minute = getMinuteFromString(value);
		switch (type){
			case RReminder.PREFERENCE_SUMMARY_HHMM:
				if (hour > 0){;
					if(hour==1){
						firstPart=context.getString(R.string.pref_hour_single);
					} else {
						firstPart=context.getString(R.string.pref_hour_multiple, hour);
					}
					firstPart+=" "+context.getString(R.string.pref_summary_and)+ " ";
				}
				if(minute==1){
					secondPart=context.getString(R.string.pref_minute_single);
				} else {
					secondPart=context.getString(R.string.pref_minute_multiple, minute);
				}
				break;
			case PREFERENCE_SUMMARY_MMSS:
				if (hour > 0){
					if(hour==1){
						firstPart=context.getString(R.string.pref_minute_single);
					} else {
						firstPart=context.getString(R.string.pref_minute_multiple, hour);
					}
					firstPart+=" "+context.getString(R.string.pref_summary_and)+ " ";
				}
				if(minute==1){
					secondPart=context.getString(R.string.pref_second_single);
				} else {
					secondPart=context.getString(R.string.pref_second_multiple, minute);
				}
				break;
			default: firstPart = "Invalid type exception"; break;
		}
		return firstPart + secondPart;
	}

	public static int getHourFromString(String time){
		String[] pieces = time.split(":");
		return (Integer.parseInt(pieces[0]));
	}

	public static int getMinuteFromString(String time){
		String[] pieces = time.split(":");
		return (Integer.parseInt(pieces[1]));
	}


	
	

	
}
