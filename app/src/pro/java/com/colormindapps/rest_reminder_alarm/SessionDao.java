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

    @Query("DELETE FROM session_table WHERE :currentTime - session_start > 2592000000")
    void deleteOlder(long currentTime);

    @Query("SELECT * FROM session_table")
    LiveData<List<Session>> getAllSessions();

    @Query("SELECT * FROM session_table WHERE session_start = :sessionStart")
    LiveData<Session> getSessionId(long sessionStart);

}
