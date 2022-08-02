package com.colormindapps.rest_reminder_alarm.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "session_table")
public class Session {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "session_id")
    private final int  sessionId;

    @ColumnInfo(name = "session_start")
    private final long sessionStart;

    @ColumnInfo(name = "session_end")
    private long sessionEnd;



    public Session(int sessionId, long sessionStart, long sessionEnd){

        this.sessionId = sessionId;
        this.sessionStart = sessionStart;
        this.sessionEnd = sessionEnd;

    }

    public int getSessionId(){return this.sessionId;}

    public long getSessionStart(){return this.sessionStart; }

    public long getSessionEnd(){return this.sessionEnd; }

    public void setSessionEnd(long end) {this.sessionEnd = end;}

}
