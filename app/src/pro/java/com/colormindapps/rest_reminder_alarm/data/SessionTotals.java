package com.colormindapps.rest_reminder_alarm.data;

import androidx.room.ColumnInfo;

public class SessionTotals {






    @ColumnInfo(name = "total_duration")
    private final long totalDuration;

    @ColumnInfo(name = "session_count")
    private final int sessionCount;

    @ColumnInfo(name = "session_average_length")
    private final long sessionAverageLength;



    public SessionTotals(long totalDuration, int sessionCount, long sessionAverageLength){
        this.totalDuration = totalDuration;
        this.sessionCount = sessionCount;
        this.sessionAverageLength = sessionAverageLength;
    }



    public long getTotalDuration(){return this.totalDuration; }
    public int getSessionCount(){return this.sessionCount; }
    public long getSessionAverageLength(){return this.sessionAverageLength;}


}
