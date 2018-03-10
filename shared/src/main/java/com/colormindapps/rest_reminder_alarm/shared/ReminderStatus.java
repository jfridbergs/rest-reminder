package com.colormindapps.rest_reminder_alarm.shared;

/**
 * Created by ingressus on 04/09/2017.
 */

public class ReminderStatus {

    private boolean mobileOn, wearOn;
    private int periodType;
    private int commandSource;
    private long periodEndTime;
    private int extendCount;

    public ReminderStatus(int periodType, long periodEndTime, int extendCount, int commandSource, boolean mobileOn, boolean wearOn){
        this.periodType = periodType;
        this.periodEndTime = periodEndTime;
        this.extendCount = extendCount;
        this.commandSource = commandSource;
        this.mobileOn = mobileOn;
        this.wearOn = wearOn;
    }

    public boolean isMobileOn(){
        return mobileOn;
    }

    public boolean isWearOn(){
        return wearOn;
    }

    public int getPeriodType(){
        return periodType;
    }

    public long getPeriodEndTime(){
        return periodEndTime;
    }

    public int getExtendCount(){
        return extendCount;
    }

    public int getCommandSource(){
        return commandSource;
    }
}
