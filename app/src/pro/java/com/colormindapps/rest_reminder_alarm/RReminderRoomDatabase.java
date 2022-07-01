package com.colormindapps.rest_reminder_alarm;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.colormindapps.rest_reminder_alarm.data.Period;
import com.colormindapps.rest_reminder_alarm.data.PeriodDao;
import com.colormindapps.rest_reminder_alarm.data.Session;
import com.colormindapps.rest_reminder_alarm.data.SessionDao;

import java.util.Calendar;
import java.util.Random;

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

    public void insertPeriod (Period period){
        new insertPeriodAsyncTask(INSTANCE.periodDao()).execute(period);
    }

    public void populateDatabase(){
        new PopulateSessionDbAsync(INSTANCE).execute();
    }

    private static class insertPeriodAsyncTask extends AsyncTask<Period, Void, Void> {
        private PeriodDao mAsyncTaskDao;
        insertPeriodAsyncTask(PeriodDao dao){
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Period... params){
            mAsyncTaskDao.insertPeriod(params[0]);
            return null;
        }
    }

    private static class PopulateSessionDbAsync extends AsyncTask<Void, Void, Void> {
        private final SessionDao mDao;
        private final PeriodDao mPeriodDao;

        PopulateSessionDbAsync(RReminderRoomDatabase db){
            mDao = db.sessionDao();
            mPeriodDao = db.periodDao();
        }

        public void insertSession(long from, int rounds, int work, int rest){
            Session session;
            long endTime = from;
            Random rand = new Random();
            for (int i=0;i<rounds;i++){

                int randomNum = rand.nextInt((5 - 1) + 1) + 1;
                if(randomNum==1){
                    endTime = insertExtendedWorkPeriod(endTime, work, rand.nextInt(3)+1);
                } else {
                    endTime = insertDefaultWorkPeriod(endTime, work);
                }
                randomNum = rand.nextInt((4 - 1) + 1) + 1;
                if(randomNum==1){
                    endTime = insertExtendedRestPeriod(endTime, rest, rand.nextInt(3)+1);
                } else {
                    endTime = insertDefaultRestPeriod(endTime, rest);
                }
            }
            session = new Session(0,from, endTime);
            mDao.insertSession(session);

        }



        public long insertDefaultWorkPeriod( long startTime, int length){
            long workLength = (long) length *60*1000;
            Period period = new Period(0,1, startTime,workLength,0,0l,0);
            mPeriodDao.insertPeriod(period);
            return startTime+workLength;
        }

        public long insertDefaultRestPeriod( long startTime, int length){
            long restLength = (long) length *60*1000;
            Period period = new Period(0,2, startTime,restLength,0, 0l,0);
            mPeriodDao.insertPeriod(period);
            return startTime+restLength;
        }

        public long insertExtendedWorkPeriod( long startTime, int length, int count){
            long initWorkLength = (long) length *60*1000;
            long workLength = initWorkLength + (long) count*5*60*1000;
            Period period = new Period(0,3, startTime,workLength,count,initWorkLength,0);
            mPeriodDao.insertPeriod(period);
            return startTime+workLength;
        }

        public long insertExtendedRestPeriod( long startTime, int length, int count){
            long initRestLength = (long) length *60*1000;
            long restLength = initRestLength + (long) count *3*60*1000;
            Period period = new Period(0,4, startTime,restLength,count, initRestLength,0);
            mPeriodDao.insertPeriod(period);
            return startTime+restLength;
        }

        @Override
        protected Void doInBackground(final Void... params){
            //mDao.deleteAll();
            //mPeriodDao.deleteAll();
            Calendar date = Calendar.getInstance();
            date.set(2022,3,16,8,0);
            long time = date.getTimeInMillis();
            mDao.deleteOlder(time);
            mPeriodDao.deleteOlder(time);
            date.set(Calendar.DAY_OF_MONTH,15);
            time = date.getTimeInMillis();

                date.set(Calendar.YEAR,2021);
                for (int j=0;j<12;j++){
                    date.set(Calendar.MONTH,j);
                    for (int k=1;k<26;k=k+5){
                        date.set(Calendar.DAY_OF_MONTH,k);
                        insertSession(date.getTimeInMillis(),5,15+k,10+j);
                    }
                }



            Calendar current = Calendar.getInstance();
            date.set(Calendar.YEAR,current.get(Calendar.YEAR));

            for(int i=0; i<current.get(Calendar.MONTH);i++){
                date.set(Calendar.MONTH,i);
                for (int k=1;k<25;k=k+5){
                    date.set(Calendar.DAY_OF_MONTH,k);
                    Log.d("DATABASE", "year (month): "+date.get(Calendar.YEAR));
                    insertSession(date.getTimeInMillis(),5+i,15+k,10+i);
                }
            }
            date.set(Calendar.MONTH,current.get(Calendar.MONTH));
            Log.d("RR_DB", "CURRENT_DAY_OF_MONTH: "+current.get(Calendar.DAY_OF_MONTH));
            Log.d("RR_DB", "CURRENT_MONTH: "+current.get(Calendar.MONTH));
            for(int i = 1; i<current.get(Calendar.DAY_OF_MONTH);i=i+2){
                date.set(Calendar.DAY_OF_MONTH,i);
                Log.d("DATABASE", "year (day): "+date.get(Calendar.YEAR));
                insertSession(date.getTimeInMillis(),5+(i/2),15+i,10+i);
            }
            /*
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


             */

            return null;
        }
    }
}
