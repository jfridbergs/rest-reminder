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


    @ColumnInfo(name = "end_time")
    private long endTime;

    @NonNull
    @ColumnInfo(name = "period_type")
    private int type;

    @ColumnInfo(name = "extend_count")
    private int extendCount;

    //boolean field with a value 1, if the period has been ended with swipe_to_end_period
    @ColumnInfo(name = "is_ended")
    private int ended;

    public Period(@NonNull int periodId,  @NonNull int type, long endTime, int extendCount, int ended){

        this.periodId = periodId;
        this.type = type;
        this.endTime = endTime;
        this.extendCount = extendCount;
        this.ended = ended;
    }

    public int getPeriodId(){return this.periodId; }

    public int getType(){return this.type; }

    public long getEndTime(){return this.endTime; }

    public int getExtendCount(){return this.extendCount;}

    public int getEnded(){return this.ended;}

    public void setEndTime(long endTime) {this.endTime = endTime;}

    public void setEnded(int ended){this.ended = ended;}

    public void setType(int type){this.type = type;}

    public void setExtendCount(int extendCount){this.extendCount = extendCount;}




}
