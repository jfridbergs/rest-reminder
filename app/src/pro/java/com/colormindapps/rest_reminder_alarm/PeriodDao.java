package com.colormindapps.rest_reminder_alarm;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PeriodDao {

    @Insert
    void insertPeriod(Period period);

    @Update
    void updatePeriod(Period period);

    @Query("DELETE FROM period_table")
    void deleteAll();


    @Query("SELECT * FROM period_table WHERE session_id = :sessionId ORDER BY end_time ASC")
    LiveData<List<Period>> getSessionPeriods(int sessionId);



}
