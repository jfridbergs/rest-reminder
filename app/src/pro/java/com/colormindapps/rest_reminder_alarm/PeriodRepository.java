package com.colormindapps.rest_reminder_alarm;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.util.Log;

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

    public long getSessionEndTime(long sessionStartTime){
        long mResult = 0L;
        new SelectSessionEndTask(mPeriodDao, mResult).execute(sessionStartTime);
        return mResult;
    }

    private static class SelectSessionEndTask extends AsyncTask<Long, Void, Period>{
        private PeriodDao mAsyncTaskDao;
        private long mresult;
        SelectSessionEndTask(PeriodDao dao, long result){
            this.mresult = result;
            mAsyncTaskDao = dao;
        }

        @Override
        protected Period doInBackground(final Long... params){
            Period result = mAsyncTaskDao.getSessionEndTIme(params[0]);
            Log.d("PERIOD_REPOSITORY", "parameter value "+params[0]);
            Log.d("PERIOD_REPOSITORY", "result long "+ result.getEndTime());
            return result;
        }

        @Override
       protected void onPostExecute(Period result){
            super.onPostExecute(result);
            mresult = result.getEndTime();
        }
    }

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
