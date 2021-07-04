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

    @Query("DELETE FROM period_table WHERE end_time = :endTime")
    void deletePeriod(long endTime);

    @Query("DELETE FROM period_table WHERE :currentTime - end_time > 2592000000")
    void deleteOlder(long currentTime);


    @Query("SELECT * FROM period_table WHERE end_time > :sessionStart AND end_time<=:sessionEnd ORDER BY end_time ASC")
    LiveData<List<Period>> getSessionPeriods(long sessionStart, long sessionEnd);

    @Query("SELECT * FROM period_table WHERE end_time=:endTime")
    LiveData<Period> getPeriod(long endTime);




}
