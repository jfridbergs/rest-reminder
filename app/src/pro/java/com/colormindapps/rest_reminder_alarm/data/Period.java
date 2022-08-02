package com.colormindapps.rest_reminder_alarm.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "period_table")
public class Period {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "period_id")
    private final int  periodId;


    @ColumnInfo(name = "start_time")
    private long startTime;

    @ColumnInfo(name = "duration")
    private long duration;

    @ColumnInfo(name = "period_type")
    private int type;

    @ColumnInfo(name = "extend_count")
    private int extendCount;

    @ColumnInfo(name = "initial_duration")
    private long initialDuration;

    //boolean field with a value 1, if the period has been ended with swipe_to_end_period
    @ColumnInfo(name = "is_ended")
    private int ended;

    public Period(int periodId, int type, long startTime, long duration, int extendCount, long initialDuration, int ended){

        this.periodId = periodId;
        this.type = type;
        this.startTime = startTime;
        this.duration = duration;
        this.extendCount = extendCount;
        this.initialDuration = initialDuration;
        this.ended = ended;
    }

    public int getPeriodId(){return this.periodId; }

    public int getType(){return this.type; }

    public long getStartTime(){return this.startTime; }

    public long getDuration(){return this.duration;}

    public int getExtendCount(){return this.extendCount;}

    public long getInitialDuration(){return this.initialDuration;}

    public int getEnded(){return this.ended;}

    public void setStartTime(long startTime) {this.startTime = startTime;}

    public void setDuration(long duration){this.duration = duration;}

    public void setEnded(int ended){this.ended = ended;}

    public void setType(int type){this.type = type;}

    public void setExtendCount(int extendCount){this.extendCount = extendCount;}

    public void setInitialDuration(long initialDuration){this.initialDuration = initialDuration;}




}
