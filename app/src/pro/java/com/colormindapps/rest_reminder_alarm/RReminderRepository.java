package com.colormindapps.rest_reminder_alarm;

import android.app.Application;
import androidx.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class RReminderRepository {

    private PeriodDao mPeriodDao;
    private SessionDao mSessionDao;
    private LiveData<List<Session>> mAllSessions;
    private LiveData<Session> mSession;
    private LiveData<List<Period>> mPeriods;

    RReminderRepository(Application application){
        RReminderRoomDatabase db = RReminderRoomDatabase.getDatabase(application);
        mPeriodDao = db.periodDao();
        mSessionDao = db.sessionDao();
        mAllSessions = mSessionDao.getAllSessions();
    }



    LiveData<List<Session>> getAllSessions(){
        return mAllSessions;
    }
    LiveData<Session> getSessionId(long sessionStartTime) {return mSessionDao.getSessionId(sessionStartTime);}
    LiveData<List<Period>> getSessionPeriods(int sessionId){return mPeriodDao.getSessionPeriods(sessionId);}




    public void insertPeriod (Period period){
        new insertPeriodAsyncTask(mPeriodDao).execute(period);
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

    public void insertSession (Session session){
        new insertSessionAsyncTask(mSessionDao).execute(session);
    }

    private static class insertSessionAsyncTask extends AsyncTask<Session, Void, Void> {
        private SessionDao mAsyncTaskDao;
        insertSessionAsyncTask(SessionDao dao){
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Session... params){
            mAsyncTaskDao.insertSession(params[0]);
            return null;
        }
    }

    public void deleteOlderSessions (long currentTime){
        new deleteAsyncTask(mSessionDao).execute(currentTime);
    }

    private static class deleteAsyncTask extends AsyncTask<Long, Void, Void>{
        private SessionDao mAsyncTaskDao;
        deleteAsyncTask(SessionDao dao){
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Long... params){
            if(params[0]>0L){
                mAsyncTaskDao.deleteOlder(params[0]);
            } else {
                mAsyncTaskDao.deleteAll();
            }

            return null;
        }
    }

    public void updatePeriod (Period period){
        new updatePeriodAsyncTask(mPeriodDao).execute(period);
    }

    private static class updatePeriodAsyncTask extends AsyncTask<Period, Void, Void>{
        private PeriodDao mAsyncTaskDao;
        updatePeriodAsyncTask(PeriodDao dao){
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Period... params){
            mAsyncTaskDao.updatePeriod(params[0]);
            return null;
        }
    }

    public void updateSession (Session session){
        new updateSessionAsyncTask(mSessionDao).execute(session);
    }

    private static class updateSessionAsyncTask extends AsyncTask<Session, Void, Void>{
        private SessionDao mAsyncTaskDao;
        updateSessionAsyncTask(SessionDao dao){
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Session... params){
            mAsyncTaskDao.updateSession(params[0]);
            return null;
        }
    }
}
