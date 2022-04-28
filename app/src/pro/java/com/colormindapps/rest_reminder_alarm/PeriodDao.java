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

    @Query("DELETE FROM period_table WHERE start_time = :startTime")
    void deletePeriod(long startTime);

    @Query("DELETE FROM period_table WHERE start_time + duration < :currentTime")
    void deleteOlder(long currentTime);


    @Query("SELECT * FROM period_table WHERE start_time >= :sessionStart AND start_time<=:sessionEnd ORDER BY start_time ASC")
    LiveData<List<Period>> getSessionPeriods(long sessionStart, long sessionEnd);

    @Query("SELECT * FROM period_table WHERE start_time=:startTime")
    LiveData<Period> getPeriod(long startTime);

    @Query("SELECT COUNT(*) FROM period_table WHERE period_type = :type AND start_time >= :sessionStart AND start_time < :sessionEnd")
    LiveData<Integer> getPeriodCount(int type, long sessionStart, long sessionEnd);




}
