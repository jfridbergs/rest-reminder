package com.colormindapps.rest_reminder_alarm;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.colormindapps.rest_reminder_alarm.data.Period;
import com.colormindapps.rest_reminder_alarm.data.PeriodDao;
import com.colormindapps.rest_reminder_alarm.data.Session;
import com.colormindapps.rest_reminder_alarm.data.SessionDao;

import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Period.class, Session.class}, version = 1)
public abstract class RReminderRoomDatabase extends RoomDatabase {

    public abstract PeriodDao periodDao();
    public abstract SessionDao sessionDao();



    private static volatile RReminderRoomDatabase INSTANCE;

    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static RReminderRoomDatabase getDatabase(final Context context){
        if(INSTANCE == null){
            synchronized (RReminderRoomDatabase.class){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), RReminderRoomDatabase.class, "rreminder_database").addCallback(sRoomDatabaseCallback).build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){
            @Override
            public void onOpen(@NonNull SupportSQLiteDatabase db){
                super.onOpen(db);
                //new PopulateSessionDbAsync(INSTANCE).execute();
            }

    };

    void insertPeriod(Period period) {
        RReminderRoomDatabase.databaseExecutor.execute(() -> INSTANCE.periodDao().insertPeriod(period));
    }

}
