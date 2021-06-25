package com.colormindapps.rest_reminder_alarm;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Calendar;

@Database(entities = {Period.class, Session.class}, version = 1)
public abstract class RReminderRoomDatabase extends RoomDatabase {

    public abstract PeriodDao periodDao();
    public abstract SessionDao sessionDao();



    private static volatile RReminderRoomDatabase INSTANCE;

    static RReminderRoomDatabase getDatabase(final Context context){
        Log.d("ROOM_DATABASE", "getDatabase");
        if(INSTANCE == null){
            Log.d("ROOM_DATABASE", "instance IS null");
            synchronized (RReminderRoomDatabase.class){
                if(INSTANCE == null){
                    Log.d("ROOM_DATABASE", "instance IS null IN SYNCRONIZED");
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), RReminderRoomDatabase.class, "rreminder_database").addCallback(sRoomDatabaseCallback).build();
                }
            }
        } else {
            Log.d("ROOM_DATABASE", "instance IS NOT null");
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){
            @Override
            public void onOpen(@NonNull SupportSQLiteDatabase db){
                super.onOpen(db);
                Log.d("ROOM_DATABASE", "onOpen");
                //new PopulateSessionDbAsync(INSTANCE).execute();
            }

    };

    private static class PopulateSessionDbAsync extends AsyncTask<Void, Void, Void> {
        private final SessionDao mDao;
        private final PeriodDao mPeriodDao;

        PopulateSessionDbAsync(RReminderRoomDatabase db){
            mDao = db.sessionDao();
            mPeriodDao = db.periodDao();
        }

        @Override
        protected Void doInBackground(final Void... params){
            mDao.deleteAll();
            mPeriodDao.deleteAll();
            long time = Calendar.getInstance().getTimeInMillis();
            //first record is a day old session
            long time1 = time - 86400000L;
            long endTime1 = time1 + 45*60*1000;
            Session session = new Session(12,time1,endTime1);
            mDao.insertSession(session);

            //adding couple periods to the first session
            //Period period = new Period(time1, 1, endTime1, 12, 0,0,0);
           // mPeriodDao.insertPeriod(period);

            //2. period - rest - extended 2 times with total length of 25 mins
            long period2end = endTime1 + 25*60*1000;
           // period = new Period(endTime1, 2, period2end, 12, 1,2,0);
           // mPeriodDao.insertPeriod(period);


            //-- end of first session records
            //second session record is a 31 day old session
            long time2 = time - 2592000000L - 86400000L;
            long endTime2 = time2 + (150*60*1000);
            session = new Session(0,time2,endTime2);
            mDao.insertSession(session);

            //third session record is a week old session
            long time3 = time - 604800000L;
            long endTime3 = time3 + (300*60*1000);
            session = new Session(0,time3, endTime3);
            mDao.insertSession(session);

            //fourth record is a 37 days old session
            long time4 = time - 2592000000L - 604800000L;
            long endTime4 = time4 + (55*60*1000);
            session = new Session(0,time4,endTime4);
            mDao.insertSession(session);


            return null;
        }
    }
}
