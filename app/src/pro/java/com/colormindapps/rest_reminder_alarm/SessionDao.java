package com.colormindapps.rest_reminder_alarm;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SessionDao {

    @Insert
    void insertSession(Session session);

    @Update
    void updateSession(Session session);

    @Query("DELETE FROM session_table")
    void deleteAll();

    @Query("DELETE FROM session_table WHERE session_end < :currentTime")
    void deleteOlder(long currentTime);

    @Query("SELECT * FROM session_table ORDER BY session_start DESC")
    LiveData<List<Session>> getAllSessions();

    @Query("SELECT * FROM session_table ORDER BY session_start ASC")
    LiveData<List<Session>> getAllSessionsPieView();

    @Query("SELECT * FROM session_table WHERE session_start>=:from AND session_start<:to ORDER BY session_start DESC")
    LiveData<List<Session>> getSessionsInPeriod(long from, long to);

    @Query("SELECT * FROM session_table WHERE session_start>=:from AND session_start<:to ORDER BY session_start ASC")
    LiveData<List<Session>> getSessionsInPeriodASC(long from, long to);


    @Query("SELECT * FROM session_table WHERE session_start = :sessionStart")
    LiveData<Session> getSessionByStart(long sessionStart);

    @Query("SELECT * FROM session_table ORDER BY session_start ASC LIMIT 1")
    LiveData<Session> getFirstSession();

    @Query("SELECT * FROM session_table WHERE session_id = :sessionId")
    LiveData<Session> getSessionById(int sessionId);

}
