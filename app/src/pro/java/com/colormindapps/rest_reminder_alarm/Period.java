package com.colormindapps.rest_reminder_alarm;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "period_table")
public class Period {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "period_id")
    private int  periodId;

    @NonNull
    @ColumnInfo(name = "previous_period_id")
    private int previousPeriodId;

    @ColumnInfo(name = "end_time")
    private long endTime;

    @NonNull
    @ColumnInfo(name = "period_type")
    private int type;

    @ColumnInfo(name = "session_id")
    private int sessionId;

    @ColumnInfo(name = "is_extended")
    private int extended;

    @ColumnInfo(name = "extend_count")
    private int extendCount;

    //boolean field with a value 1, if the period has been ended before its intented end time
    @ColumnInfo(name = "is_ended")
    private int ended;

    public Period(@NonNull int periodId, @NonNull int previousPeriodId, @NonNull int type, long endTime, int sessionId, int extended,  int extendCount, int ended){

        this.periodId = periodId;
        this.previousPeriodId = previousPeriodId;
        this.type = type;
        this.endTime = endTime;
        this.sessionId = sessionId;
        this.extended = extended;
        this.extendCount = extendCount;
        this.ended = ended;
    }

    public int getPeriodId(){return this.periodId; }

    public int getPreviousPeriodId(){return this.previousPeriodId; }

    public int getType(){return this.type; }

    public long getEndTime(){return this.endTime; }

    public int getSessionId(){return this.sessionId; }

    public int getExtended(){return this.extended;}

    public int getExtendCount(){return this.extendCount;}

    public int getEnded(){return this.ended;}



}
