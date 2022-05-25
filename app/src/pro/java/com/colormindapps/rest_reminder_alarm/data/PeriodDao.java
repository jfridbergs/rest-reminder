package com.colormindapps.rest_reminder_alarm.data;

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

    @Query("SELECT * FROM period_table ORDER BY start_time DESC LIMIT 1")
    LiveData<Period> getLastPeriod();

    @Query("SELECT COUNT(*) FROM period_table WHERE period_type = :type AND start_time >= :sessionStart AND start_time < :sessionEnd")
    LiveData<Integer> getPeriodCount(int type, long sessionStart, long sessionEnd);

    @Query("SELECT 1 as rank, SUM(duration) as total_duration, COUNT(*) as period_count, SUM(extend_count) as extend_count, (SELECT SUM(duration-initial_duration) FROM period_table WHERE period_type = 3 AND start_time >= :sessionStart AND start_time < :sessionEnd) as total_extend_duration FROM period_table WHERE (period_type = 1 OR period_type = 3) AND start_time >= :sessionStart AND start_time < :sessionEnd UNION SELECT 2 as rank, SUM(duration) as total_duration, COUNT(*) as period_count, SUM(extend_count) as extend_count, (SELECT SUM(duration-initial_duration) FROM period_table WHERE period_type = 4 AND start_time >= :sessionStart AND start_time < :sessionEnd) as total_extend_duration FROM period_table WHERE (period_type = 2 OR period_type = 4) AND start_time >= :sessionStart AND start_time < :sessionEnd ORDER BY rank ASC")
    LiveData<List<PeriodTotals>> getPeriodTotals(long sessionStart, long sessionEnd);





}
