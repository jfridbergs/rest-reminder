package com.colormindapps.rest_reminder_alarm;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class PeriodRepository {

    private PeriodDao mPeriodDao;
    private LiveData<List<Period>> mAllSessions;

    PeriodRepository(Application application){
        PeriodRoomDatabase db = PeriodRoomDatabase.getDatabase(application);
        mPeriodDao = db.periodDao();
        mAllSessions = mPeriodDao.getAllSessions();
    }



    LiveData<List<Period>> getAllSessions(){
        return mAllSessions;
    }
    LiveData<List<Period>> getSessionPeriods(long sessionStartTime){return mPeriodDao.getSessionPeriods(sessionStartTime);}
    LiveData<Long> getSessionEndTime(long sessionStartTime){return mPeriodDao.getSessionEndTIme(sessionStartTime);}

    public void insert (Period period){
        new insertAsyncTask(mPeriodDao).execute(period);
    }

    private static class insertAsyncTask extends AsyncTask<Period, Void, Void> {
        private PeriodDao mAsyncTaskDao;
        insertAsyncTask(PeriodDao dao){
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Period... params){
            mAsyncTaskDao.insertPeriod(params[0]);
            return null;
        }
    }

    public void deleteOlder (long currentTime){
        new deleteAsyncTask(mPeriodDao).execute(currentTime);
    }

    private static class deleteAsyncTask extends AsyncTask<Long, Void, Void>{
        private PeriodDao mAsyncTaskDao;
        deleteAsyncTask(PeriodDao dao){
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Long... params){
            mAsyncTaskDao.deleteOlder(params[0]);
            return null;
        }
    }

    public void update (Period period){
        new updateAsyncTask(mPeriodDao).execute(period);
    }

    private static class updateAsyncTask extends AsyncTask<Period, Void, Void>{
        private PeriodDao mAsyncTaskDao;
        updateAsyncTask(PeriodDao dao){
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Period... params){
            mAsyncTaskDao.updatePeriod(params[0]);
            return null;
        }
    }
}
