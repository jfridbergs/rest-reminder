package com.colormindapps.rest_reminder_alarm.data;

import androidx.room.ColumnInfo;

public class TimeInterval {






    @ColumnInfo(name = "start")
    private long start;

    @ColumnInfo(name = "end")
    private long end;










    public TimeInterval(long start, long end){
        this.start = start;
        this.end = end;

    }



    public long getStart(){return this.start; }
    public long getEnd(){return this.end; }


}
