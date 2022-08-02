package com.colormindapps.rest_reminder_alarm.data;

import androidx.room.ColumnInfo;


public class PeriodTotals {






    @ColumnInfo(name = "total_duration")
    private final long totalDuration;

    @ColumnInfo(name = "period_count")
    private final int periodCount;



    @ColumnInfo(name = "extend_count")
    private int extendCount;

    @ColumnInfo(name = "total_extend_duration")
    private final long totalExtendDuration;









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









    public void setExtendCount(int extendCount){
        this.extendCount = extendCount;
    }

}
