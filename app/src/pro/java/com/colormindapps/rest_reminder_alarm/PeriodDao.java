package com.colormindapps.rest_reminder_alarm;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface PeriodDao {

    @Insert
    void insertPeriod(Period period);

    @Update
    void updatePeriod(Period period);

    @Query("DELETE FROM period_table")
    void deleteAll();

    @Query("DELETE FROM period_table WHERE :currentTime - start_time > 2592000000")
    void deleteOlder(long currentTime);

    @Query("SELECT * FROM period_table WHERE is_session_first_period = 1 ORDER BY start_time DESC")
    LiveData<List<Period>> getAllSessions();

    @Query("SELECT * FROM period_table WHERE session_start_time = :sessionStartTime ORDER BY start_time DESC")
    LiveData<List<Period>> getSessionPeriods(long sessionStartTime);

    @Query("SELECT end_time FROM period_table WHERE session_start_time = :sessionStartTime ORDER BY end_time DESC LIMIT 1")
    LiveData<Long> getSessionEndTIme(long sessionStartTime);


}
