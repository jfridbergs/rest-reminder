package com.colormindapps.rest_reminder_alarm.shared;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import androidx.annotation.IntDef;
import androidx.core.content.ContextCompat;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;

import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;


public class RReminder {




	public static final String PERIOD_TYPE = "period_type";
	public static final String TURN_OFF = "turn_off";
	public static final String MANUAL_MODE_NEXT_PERIOD_TYPE = "manual_mode_next_period_type";
	public static final String PERIOD_END_TIME = "period_end";
	public static final String PREVIOUS_PERIOD_END_TIME = "previous_period_end";
	public static final String IS_SHORT_PERIOD = "is_short_period";
	public static final String NEXT_PERIOD_END_TIME = "next_period_end";
	public static final String EXTENDED_PERIOD_TYPE = "extended_period_type";
	public static final String START_COUNTER = "start_counter";
	public static final String EXTEND_COUNT = "extend_count";
	public static final String COUNTER_TIME_VALUE = "counter_time_value";
	public static final String ACTIVITY_TYPE = "activity_type";
	public static final String EXCLUDE_ONGOING = "exclude_ongoing";
	public static final String PERIOD_COMPLETED = "period_completed";
	public static final String PLAY_SOUND = "play_sound";
	public static final String ANIMATE_INFO = "animate_info";
	public static final String PERIOD_END_REDIRECT = "period_end_redirect";
	public static final String PERIOD_END_DISMISS = "period_end_dismiss";
	public static final String REDIRECT_SCREEN_OFF = "intent-extra_redirect_screen_off";
	public static final String COUNTERSERVICE_STATUS = "counter_service_status";
	public static final String DATA_API_SOURCE = "data_api_source";
	public static final String DATA_API_MOBILE_ON = "data_api_mobile_on";
	public static final String DATA_API_WEAR_ON = "data_api_wear_on";
	public static final int DEFAULT_EXTEND_COUNT = 1;
	public static final int DEFAULT_EXTEND_BASE_LENGTH = 5;
	public static final int NOTIFICATION_ID = 1;

	public static final String YEAR="date_year";
	public static final String MONTH="date_month";
	public static final String DAY="calendar_day";

	public static final String CHANNEL_ONGOING_ID = "notification_channel_ongoing";
	public static final String CHANNEL_PERIOD_END_ID = "notification_channel_period_end";
	public static final int NOTIFICATION_CHANNEL_ONGOING = 1;
	public static final int NOTIFICATION_CHANNEL_PERIOD_END = 0;
	//
	public static final String DEFAULT_WORK_PERIOD_STRING = "00:45";
	public static final String DEFAULT_REST_PERIOD_STRING = "00:15";
	public static final String DEFAULT_APPROX_TIME_STRING = "01:00";
	public static final String ACTION_TURN_OFF = "com.colormindapps.rest_reminder_alarm.ACTION_TURN_OFF_SCHEDULER";
	public static final String ACTION_UPDATE_MAIN_UI = "com.colormindapps.rest_reminder_alarm.ACTION_UPDATE_MAIN_UI";
	public static final String ACTION_MANUAL_START_NEXT_PERIOD = "com.colormindapps.rest_reminder_alarm.ACTION_MANUAL_START_NEXT_PERIOD";
	public static final String ACTION_VIEW_NOTIFICATION_ACTIVITY = "com.colormindapps.rest_reminder_alarm.ACTION_VIEW_PERIOD_END_NOTIFICATION";
	public static final String ACTION_CLEAR_NOTIFICATION = "com.colormindapps.rest_reminder_alarm.ACTION_CLEAR_NOTIFICATION";
	public static final String ACTION_WEAR_NOTIFICATION_EXTEND= "com.colormindapps.rest_reminder_alarm.ACTION_WEAR_NOTIFICATION_EXTEND";
	public static final String ACTION_WEAR_NOTIFICATION_START_NEXT= "com.colormindapps.rest_reminder_alarm.ACTION_WEAR_NOTIFICATION_START_NEXT";
	public static final String ACTION_WEAR_COMMANDS_EXTEND= "com.colormindapps.rest_reminder_alarm.ACTION_WEAR_COMMANDS_EXTEND";
	public static final String ACTION_WEAR_COMMANDS_START_NEXT= "com.colormindapps.rest_reminder_alarm.ACTION_WEAR_COMMANDS_START_NEXT";
	public static final String ACTION_VIEW_MAIN_ACTIVITY = "com.colormindapps.rest_reminder_alarm.ACTION_VIEW_MAIN_ACTIVITY";
	public static final String ACTION_ALARM_PERIOD_END = "com.colormindapps.rest_reminder_alarm.ACTION_ALARM_PERIOD_END";
    public static final String PERIOD_EXTENDED_FROM_NOTIFICATION_ACTIVITY = "com.colormindapps.rest_reminder_alarm.ACTION_PERIOD_EXTENDED_FROM_NOTIFICATION_ACT";

	public static final String WEAR_ACTION_START_NEXT_PERIOD = "com.colormindapps.rest_reminder_alarm.ACTION_WEAR_START_NEXT_PERIOD";

	public static final String DATA_API_REMINDER_STATUS_PATH = "/reminder_status";
	public static final String DATA_API_REMINDER_PREFERENCES_PATH = "/reminder_preferences";
	public static final String DATA_API_WEAR_SERVICE_EXCEPTION = "data_api_wear_service";

	public static final int DATA_API_SOURCE_MOBILE = 0;
	public static final int DATA_API_SOURCE_WEAR = 1;
	public static final int DATA_API_SOURCE_WEAR_SERVICE = 2;
	public static final int DATA_API_SOURCE_LINKED_MOBILE_ON = 3;
	public static final int DATA_API_SOURCE_LINKED_WEAR_ON = 4;

	public static final int PREFERENCE_SUMMARY_HHMM = 0;
	public static final int PREFERENCE_SUMMARY_MMSS = 1;

	public static final int WEAR_ANIMATE_ON = 0;
	public static final int WEAR_ANIMATE_WORK_TO_OFF = 1;
	public static final int WEAR_ANIMATE_REST_TO_OFF = 2;
	public static final int WEAR_ANIMATE_RED_TO_OFF = 3;

	public static final String WEAR_PREF_REMINDER_MODE = "wear_pref_reminder_mode";
	public static final String WEAR_PREF_WORK_LENGTH = "wear_pref_work_length";
	public static final String WEAR_PREF_REST_LENGTH = "wear_pref_rest_length";
	public static final String WEAR_PREF_EXTEND_LENGTH = "wear_pref_extend_length";
	public static final String WEAR_PREF_EXTEND_ENABLED = "wear_pref_extend_enabled";
	public static final String WEAR_PREF_START_NEXT_ENABLED = "wear_pref_start_next_enabled";

	public static final String PREF_REMINDER_MODE_KEY = "reminder_mode";
	public static final String PREF_WORK_LENGTH_KEY = "work_period_length";
	public static final String PREF_REST_LENGTH_KEY = "rest_period_length";
	public static final String PREF_EXTEND_ENABLED_KEY = "enable_end_extend";
	public static final String PREF_START_NEXT_ENABLED_KEY = "enable_end_period";
	public static final String PREF_EXTEND_BASE_LENGTH_KEY = "end_extend_length_key";
	public static final String PREF_EXTEND_OPTIONS_KEY = "end_extend_options_key";
	public static final String PREF_APPROX_LENGTH_KEY = "approx_notification_length_key";
	public static final String PREFERENCE_SCREEN_KEY = "preference_screen_key";

	public static final int EXTEND_PERIOD_SINGLE_OPTION = 1;
	public static final int EXTEND_PERIOD_WEAR = 2;

	//constants for ShowCaseView
	public static final int SHOWCASEVIEW_START = 1;
	public static final int SHOWCASEVIEW_STOP = 2;
	public static final int SHOWCASEVIEW_EXTEND = 3;
	public static final int SHOWCASEVIEW_FORCE_END = 4;
	public static final int SHOWCASEVIEW_MENU = 5;

	//variables for testing purposes. remove after finishing testing
	public static final String CUSTOM_INTENT_TEST_PREFERENCES_MODE = "com.colormindapps.rest_reminder_alarm.ACTION_TEST_PREFERENCES_MODE";
	public static final String CUSTOM_INTENT_TEST_PREFERENCES_PERIOD = "com.colormindapps.rest_reminder_alarm.ACTION_TEST_PREFERENCES_PERIOD";
	public static final String CUSTOM_INTENT_TEST_PREFERENCES_EXTEND = "com.colormindapps.rest_reminder_alarm.ACTION_TEST_PREFERENCES_EXTEND";
	public static final String PREFERENCE_MODE_SUMMARY = "preference_mode_summary";
	public static final String PREFERENCE_WORK_LENGTH_SUMMARY = "preference_work_length_summary";
	public static final String PREFERENCE_REST_LENGTH_SUMMARY = "preference_rest_length_summary";
	public static final String PREFERENCE_PROXIMITY_LENGTH_SUMMARY = "preference_proximity_length_summary";
	public static final String PREFERENCE_EXTEND_COUNT_SUMMARY = "preference_extend_count_summary";
	public static final String PREFERENCE_EXTEND_LENGTH_SUMMARY = "preference_extend_length_summary";
	public static final String PREFERENCE_WORK_AUDIO_SUMMARY = "preference_work_audio_summary";
	public static final String PREFERENCE_REST_AUDIO_SUMMARY = "preference_rest_audio_summary";
	public static final String PREFERENCE_PROXIMITY_AUDIO_SUMMARY = "preference_proximity_audio_summary";
	public static final String PREFERENCE_DISABLE_BATTERY_OPT_SUMMARY = "preference_disable_battery_opt_summary";
	//end of testing variables

	
    public static final String PRIVATE_PREF = "myapp";
    public static final String VERSION_KEY = "version_number";
	public static final String EULA_ACCEPTED="eula_accepted";
	public static final String DISPLAY_TURN_OFF_HINT = "display_turn_off_hint";
	public static final String TURN_OFF_COUNT = "turn_off_count";
	public static final String EULA_ACCEPT_TIME="eula_accept_time";
    public static final String TIME_FORMAT_24H = "kk:mm";
    public static final String TIME_FORMAT_12H = "hh:mm aa";
    public static final String DATE_FORMAT = "dd/MM/yyyy kk:mm.ss";
    public static final String SESSION_DATE_FORMAT = "EEE, dd/MM/yyyy";

	public static final int WORK = 1;
	public static final int REST = 2;
	public static final int WORK_EXTENDED = 3;
	public static final int REST_EXTENDED = 4;
	public static final int PERIOD_OFF = 0;

	public static final String DB_SESSION_ID = "db_session_id";
	public static final String DB_SESSION_START = "db_session_start";
	public static final String DB_SESSION_END = "db_session_end";

	@IntDef({WORK, REST, WORK_EXTENDED, REST_EXTENDED, PERIOD_OFF})
	@Retention(RetentionPolicy.SOURCE)
	public @interface PeriodType {}
    
	public static int getShortestPeriodLength(Context context){
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String workPeriod = prefs.getString(PREF_WORK_LENGTH_KEY, context.getString(R.string.default_work_length_string));
		String restPeriod = prefs.getString(PREF_REST_LENGTH_KEY, context.getString(R.string.default_rest_length_string));
		int workPeriodValue = getHourFromString(workPeriod) * 60 + getMinuteFromString(workPeriod);
		int restPeriodValue = getHourFromString(restPeriod) * 60 + getMinuteFromString(restPeriod);
		if(workPeriodValue > restPeriodValue){
			return restPeriodValue;
		} else {
			return workPeriodValue;
		}
	}

	
	public static long getTimeAfterExtend(Context context, int multiplier, long timeRemaining){
		int addMins = getExtendBaseLength(context);
		long currentTime = Calendar.getInstance().getTimeInMillis();
		long addTime = timeRemaining + (long)addMins*60000L* (long)multiplier;
		return currentTime + addTime;
	}
	
	public static int getExtendBaseLength(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getInt(PREF_EXTEND_BASE_LENGTH_KEY,DEFAULT_EXTEND_BASE_LENGTH);
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

	public static String getDateString(long time){
		CharSequence periodEndTime, timeFormatString;
		timeFormatString = DATE_FORMAT;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		periodEndTime = DateFormat.format(timeFormatString,calendar.getTime());
		return periodEndTime.toString();
	}

	public static String getSessionDateString(long time){
		CharSequence periodEndTime, timeFormatString;
		timeFormatString = SESSION_DATE_FORMAT;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		periodEndTime = DateFormat.format(timeFormatString,calendar.getTime());
		return periodEndTime.toString();
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
	
	public static String getPreferencePeriodLength(Context context, int type){
		String result;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		switch(type){
		case WORK:
			result = prefs.getString(PREF_WORK_LENGTH_KEY,DEFAULT_WORK_PERIOD_STRING);
			break;
		case REST:
			result = prefs.getString(PREF_REST_LENGTH_KEY,DEFAULT_REST_PERIOD_STRING);
			break;
		default:
			result = prefs.getString(PREF_WORK_LENGTH_KEY,DEFAULT_WORK_PERIOD_STRING);
			break;
		}
		return result;
	}

	public static int getNextPeriodType( int previousType){
		switch(previousType){
			case RReminder.WORK:case RReminder.WORK_EXTENDED:  return  RReminder.REST;
			case RReminder.REST:case RReminder.REST_EXTENDED:  return  RReminder.WORK;
			default: return RReminder.PERIOD_OFF;
		}
	}

	public static int getHour(String time){
		String[] pieces = time.split(":");
		return (Integer.parseInt(pieces[0]));
	}

	public static int getMinute(String time){
		String[] pieces = time.split(":");
		return (Integer.parseInt(pieces[1]));
	}

	public static long getUpdatedDifference(String oldString, String newString){
		int difference;
		int oldHour = getHour(oldString);
		int oldMinute = getMinute(oldString);
		int newHour = getHour(newString);
		int newMinute = getMinute(newString);
		difference = (newHour*60*60*1000 + newMinute*60*1000) - (oldHour*60*60*1000 + oldMinute*60*1000);

		return difference;
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

	public static boolean isNotificationColorizeEnabled(Context context){
		String key = context.getString(R.string.pref_colorize_notifications_key);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(key, true);
	}
	
	public static boolean isVibrateEnabledSupport(Context context){
		String key = context.getString(R.string.pref_enable_vibrate_key);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		return context.getSystemService(Context.VIBRATOR_SERVICE) != null && prefs.getBoolean(key, true);


	}

	public static long getMillisFromDate(int year, int month, int day, boolean start){
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day);
		if(start){
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
		} else {
			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(Calendar.MINUTE,59);
			calendar.set(Calendar.SECOND,59);
		}

		return calendar.getTimeInMillis();
	}

	
	public static boolean isExtendEnabled(Context context){
		String key = context.getString(R.string.pref_enable_extend_key);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(key, true);
	}

	public static boolean isShortPeriodsEnabled(Context context){
		String key = context.getString(R.string.pref_enable_short_periods_key);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(key, false);
	}
	
	public static boolean isLedEnabled(Context context){
		String key = context.getString(R.string.pref_show_led_key);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(key, true);
	}
	
	public static int getExtendOptionsCount(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getInt(PREF_EXTEND_OPTIONS_KEY, 1);
	}
	
	public static long getApproxTime(Context context, long periodEndTime){
		String approxTime;
		long calendar;
		long approxTimeValue;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		approxTime = prefs.getString(PREF_APPROX_LENGTH_KEY,DEFAULT_APPROX_TIME_STRING);
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
		case WORK: case WORK_EXTENDED:
			ringtoneString = preference.getString(context.getString(R.string.pref_rest_period_start_sound_key),"DEFAULT_SOUND");
			break;
		case REST: case REST_EXTENDED:
			ringtoneString = preference.getString(context.getString(R.string.pref_work_period_start_sound_key),"DEFAULT_SOUND");
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

	public static int getNotificationBackgroundColorId(Context context, int periodType, int extendCount){
		switch(periodType){
			case 1: case 3:
			{
				if(extendCount>3){
					return ContextCompat.getColor(context, R.color.notif_bg_red);
				} else {
					return ContextCompat.getColor(context, R.color.notif_bg_rest);
				}
			}
			case 2: case 4:
			{
				if(extendCount>3){
					return ContextCompat.getColor(context, R.color.notif_bg_red);
				} else {
					return ContextCompat.getColor(context, R.color.notif_bg_work);
				}

			}
			default: return 0;

		}
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

	@PeriodType
	public static int getNextType(int oldType){
		int type;
		switch(oldType){
		case WORK: case WORK_EXTENDED: type = REST; break;
		case REST: case REST_EXTENDED: type = WORK; break;
		default: type = WORK; break;
		}
		return type;
	}

	@TargetApi(Build.VERSION_CODES.O)
	public static NotificationChannel createNotificationChannel(Context context, int type) {
		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is new and not in the support library

		CharSequence name;
		String description;
		int importance;
		String channel_ID;
		if(type == RReminder.NOTIFICATION_CHANNEL_ONGOING){
			importance = NotificationManager.IMPORTANCE_LOW;
			name = context.getString(R.string.channel_name_ongoing);
			description = context.getString(R.string.channel_description_ongoing);
			channel_ID = RReminder.CHANNEL_ONGOING_ID;
		} else {
			importance = NotificationManager.IMPORTANCE_HIGH;
			name = context.getString(R.string.channel_name_period_end);
			description = context.getString(R.string.channel_description_period_end);
			channel_ID = RReminder.CHANNEL_PERIOD_END_ID;
		}

		NotificationChannel channel = new NotificationChannel(channel_ID, name, importance);
		channel.setDescription(description);
		if(type==RReminder.NOTIFICATION_CHANNEL_PERIOD_END){
			channel.setSound(null, null);
		}

		return channel;

	}
	

	
	public static long getNextPeriodEndTime (Context context, int type, long calendar, int extendMultiplier, long timeTillEndExtend){
		String period;
		int addMins;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		switch(type){
		case WORK:
			period = prefs.getString(context.getString(R.string.pref_work_period_length_key),DEFAULT_WORK_PERIOD_STRING );
			addMins = getHourFromString(period)*60 + getMinuteFromString(period);
			break;
		case REST:
			period = prefs.getString(context.getString(R.string.pref_rest_period_length_key), DEFAULT_REST_PERIOD_STRING);
			addMins = getHourFromString(period)*60 + getMinuteFromString(period);
			break;
		case WORK_EXTENDED: case REST_EXTENDED:
			addMins = prefs.getInt(context.getString(R.string.pref_period_extend_length_key), RReminder.DEFAULT_EXTEND_BASE_LENGTH) * extendMultiplier;
			break;
		default: addMins = 0; break;
		}
		//for testing purposes only
		if(addMins==955){
			switch(type){
				case WORK:{
					calendar+=20000 + timeTillEndExtend;
					break;
				}
				case REST:{
					calendar+=10000 + timeTillEndExtend;
					break;
				}
				default: break;
			}
		} else {
			calendar+=(long)addMins * 60000L + timeTillEndExtend;
		}

		return calendar;
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

	public static boolean isMultiSmall(Context context){
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		float scaleFactor = dm.density;
		float smallestWidth = Math.min(dm.widthPixels/scaleFactor, dm.heightPixels/scaleFactor);
		return smallestWidth<350.0;
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
		} else if (length <=25 && length > 10){
			size = 38;
			if(smallTitle){
				return 26;
			}
			if(isTablet){
				size = size * 2;
			}
			return size;
		} else {
			if(isPortrait(context) && !isMultiSmall(context)){
				size=70;
			} else {
				size = 50;
			}

			if(smallTitle){
				return 34;
			}
			
			if(isTablet){
				size = size * 2;
			}
		}
		return size;
	}


	public static String getFormatedValue(Context context,  int type, String value){
		String firstPart = "", secondPart = "";
		int hour = getHourFromString(value);
		int minute = getMinuteFromString(value);
		switch (type){
			case RReminder.PREFERENCE_SUMMARY_HHMM:
				if (hour > 0){
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

	public static String getDurationFromMillis(Context context,long millis){
		String firstPart = "", secondPart = " ";
		int timeInSeconds =  Math.round(millis / 1000);
		int timeInMinutes  = timeInSeconds / 60;
		int minutes = timeInMinutes % 60;
		int hours = timeInMinutes / 60;

		if (hours > 0){
			if(hours==1){
				firstPart=context.getString(R.string.pref_hour_single);
			} else {
				firstPart=context.getString(R.string.pref_hour_multiple, hours);
			}
			firstPart+=" "+context.getString(R.string.pref_summary_and)+ " ";
		}
		if(minutes==1){
			secondPart=context.getString(R.string.pref_minute_single);
		} else {
			secondPart=context.getString(R.string.pref_minute_multiple, minutes);
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


	public static int dip2px(Context context, float dipValue){
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int)(dipValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue){
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int)(pxValue / scale + 0.5f);
	}



	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}

	public static PutDataRequest createStatusData(int source, int type, long periodEndValue, int extendCount, boolean mobileOn, boolean wearOn){
		PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/reminder_status");
		putDataMapRequest.getDataMap().putInt(DATA_API_SOURCE,source);
		putDataMapRequest.getDataMap().putInt(PERIOD_TYPE,type);
		putDataMapRequest.getDataMap().putLong(PERIOD_END_TIME,periodEndValue);
		putDataMapRequest.getDataMap().putInt(EXTEND_COUNT, extendCount);
		putDataMapRequest.getDataMap().putBoolean(DATA_API_MOBILE_ON, mobileOn);
		putDataMapRequest.getDataMap().putBoolean(DATA_API_WEAR_ON, wearOn);
		PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
		return putDataRequest;


	}





	
	

	
}
