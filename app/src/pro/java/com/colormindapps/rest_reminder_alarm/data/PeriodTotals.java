package com.colormindapps.rest_reminder_alarm.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

public class PeriodTotals {






    @ColumnInfo(name = "total_duration")
    private long totalDuration;

    @ColumnInfo(name = "period_count")
    private int periodCount;



    @ColumnInfo(name = "extend_count")
    private int extendCount;

    @ColumnInfo(name = "total_extend_duration")
    private long totalExtendDuration;









    public PeriodTotals(long totalDuration,  int periodCount, int extendCount, long totalExtendDuration){
        this.totalDuration = totalDuration;
        this.periodCount = periodCount;
        this.extendCount = extendCount;
        this.totalExtendDuration = totalExtendDuration;
    }



    public long getTotalDuration(){return this.totalDuration; }
    public long getTotalExtendDuration(){return this.totalExtendDuration; }


    public int getPeriodCount(){return this.periodCount;}

    public int getExtendCount(){return this.extendCount;}


    public void setTotalDuration(long totalDuration){
        this.totalDuration = totalDuration;
    }
    public void setTotalExtendDuration(long totalExtendDuration){
        this.totalExtendDuration = totalExtendDuration;
    }



    public void setPeriodCount(int periodCount){
        this.periodCount = periodCount;
    }



    public void setExtendCount(int extendCount){
        this.extendCount = extendCount;
    }

}
