package com.colormindapps.rest_reminder_alarm.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "session_table")
public class Session {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "session_id")
    private int  sessionId;

    @NonNull
    @ColumnInfo(name = "session_start")
    private long sessionStart;

    @ColumnInfo(name = "session_end")
    private long sessionEnd;



    public Session(@NonNull int sessionId, @NonNull long sessionStart, long sessionEnd){

        this.sessionId = sessionId;
        this.sessionStart = sessionStart;
        this.sessionEnd = sessionEnd;

    }

    public int getSessionId(){return this.sessionId;}

    public long getSessionStart(){return this.sessionStart; }

    public long getSessionEnd(){return this.sessionEnd; }

    public void setSessionId(int id){
        this.sessionId = id;
    }

    public void setSessionEnd(long end) {this.sessionEnd = end;}

}
