package com.colormindapps.rest_reminder_alarm;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.Calendar;

@Database(entities = {Period.class}, version = 1)
public abstract class PeriodRoomDatabase extends RoomDatabase {

    public abstract PeriodDao periodDao();

    private static volatile PeriodRoomDatabase INSTANCE;

    static PeriodRoomDatabase getDatabase(final Context context){
        if(INSTANCE == null){
            synchronized (PeriodRoomDatabase.class){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), PeriodRoomDatabase.class, "period_database").addCallback(sRoomDatabaseCallback).build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){
            @Override
            public void onOpen(@NonNull SupportSQLiteDatabase db){
                super.onOpen(db);
                new PopulateDbAsync(INSTANCE).execute();
            }

    };

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {
        private final PeriodDao mDao;

        PopulateDbAsync(PeriodRoomDatabase db){
            mDao = db.periodDao();
        }

        @Override
        protected Void doInBackground(final Void... params){
            mDao.deleteAll();
            long time = Calendar.getInstance().getTimeInMillis();
            //first record is a day old session
            long time1 = time - 86400000L;
            long endTIme1 = time1 + 45*60*1000;
            Period period = new Period(time1,1,endTIme1,1,time1,0,0, 0);
            mDao.insertPeriod(period);

            //adding additional periods for the first session for session activitiy

            //second (rest period - 15 mins)
            long endtime1_2 = endTIme1 + 15*60*1000;

            period = new Period(endTIme1,2,endtime1_2,0,time1,0,0,0);
            mDao.insertPeriod(period);

            //third period is a work period that has been cut short by 20 mins, period length 25 mins
            long endTime1_3 = endtime1_2 + 25*60*1000;
            period = new Period(endtime1_2,1,endTime1_3,0,time1,0,0,1);
            mDao.insertPeriod(period);


            //fourt period is a rest period that has been extended 1 time by 5 mins, period length 20 mins
            long endTime1_4 = endTime1_3 + 20*60*1000;
            period = new Period(endTime1_3,2,endTime1_4,0,time1,1,1,0);
            mDao.insertPeriod(period);

            //-- end of first session records
            //second session record is a 31 day old session
            long time2 = time - 2592000000L - 86400000L;
            period = new Period(time2,1,0L,1,time2,0,0,0);
            mDao.insertPeriod(period);

            //third session record is a week old session
            long time3 = time - 604800000L;
            period = new Period(time3,1,0L,1,time3,0,0,0);
            mDao.insertPeriod(period);

            //fourth record is a 37 days old session
            long time4 = time - 2592000000L - 604800000L;
            period = new Period(time4,1,0L,1,time4,0,0,0);
            mDao.insertPeriod(period);


            return null;
        }
    }
}
