package com.colormindapps.rest_reminder_alarm;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "period_table")
public class Period {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "start_time")
    private long startTime;

    @NonNull
    @ColumnInfo(name = "period_type")
    private int type;

    @ColumnInfo(name = "end_time")
    private long endTime;

    @ColumnInfo(name = "is_session_first_period")
    private int sessionFirstPeriod;

    @ColumnInfo(name = "session_start_time")
    private long sessionStartTime;


    @ColumnInfo(name = "is_extended")
    private int extended;


    @ColumnInfo(name = "extend_count")
    private int extendCount;

    //boolean field with a value 1, if the period has been ended before its intented end time
    @ColumnInfo(name = "is_ended")
    private int ended;

    public Period(@NonNull long startTime, @NonNull int type, long endTime,  int sessionFirstPeriod, long sessionStartTime, int extended,  int extendCount, int ended){

        this.startTime = startTime;
        this.type = type;
        this.endTime = endTime;
        this.sessionFirstPeriod = sessionFirstPeriod;
        this.sessionStartTime = sessionStartTime;
        this.extended = extended;
        this.extendCount = extendCount;
        this.ended = ended;
    }


    public long getStartTime(){return this.startTime; }

    public int getType(){return this.type; }

    public long getEndTime(){return this.endTime; }

    public int getSessionFirstPeriod(){return this.sessionFirstPeriod; }

    public long getSessionStartTime(){return this.sessionStartTime; }

    public int getExtended(){return this.extended;}

    public int getExtendCount(){return this.extendCount;}

    public int getEnded(){return this.ended;}



}
