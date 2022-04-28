package com.colormindapps.rest_reminder_alarm;

import android.app.Application;
import androidx.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.Calendar;
import java.util.List;

public class RReminderRepository {

    private PeriodDao mPeriodDao;
    private SessionDao mSessionDao;
    private LiveData<List<Session>> mAllSessions, mAllSessionPieView;
    private LiveData<Session> mSession;
    private LiveData<List<Period>> mPeriods;
    private LiveData<Period> mPeriod;
    private RReminderRoomDatabase db;

    RReminderRepository(Application application){
        db = RReminderRoomDatabase.getDatabase(application);
        mPeriodDao = db.periodDao();
        mSessionDao = db.sessionDao();
        mAllSessions = mSessionDao.getAllSessions();
        mAllSessionPieView = mSessionDao.getAllSessionsPieView();
    }



    LiveData<List<Session>> getAllSessions(){
        return mAllSessions;
    }
    LiveData<List<Session>> getAllSessionsPieData(){
        return mAllSessionPieView;
    }
    LiveData<List<Session>> getSessionsInPeriod(long from, long to){
        return mSessionDao.getSessionsInPeriod(from, to);
    }

    LiveData<List<Session>> getSessionsInPeriodASC(long from, long to){
        return mSessionDao.getSessionsInPeriodASC(from, to);
    }
    LiveData<Session> getSessionByStart(long sessionStartTime) {return mSessionDao.getSessionByStart(sessionStartTime);}
    LiveData<Session> getSessionById(int sessionId) {return mSessionDao.getSessionById(sessionId);}
    LiveData<Session> getFirstSession() {return mSessionDao.getFirstSession();}
    LiveData<List<Period>> getSessionPeriods(long sessionStart, long sessionEnd){return mPeriodDao.getSessionPeriods(sessionStart, sessionEnd);}
    LiveData<Period> getPeriod(long startTime) {return mPeriodDao.getPeriod(startTime);}
    LiveData<Integer> getPeriodCount(int type, long start,long end){return mPeriodDao.getPeriodCount(type, start, end);}




    public void insertPeriod (Period period){
        new insertPeriodAsyncTask(mPeriodDao).execute(period);
    }

    public void populateDatabase(){
        db.populateDatabase();
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
        new deleteAsyncTask(mSessionDao, mPeriodDao).execute(currentTime);
    }

    public void deletePeriod(long endTime){
        new deletePeriodAsyncTask(mPeriodDao).execute(endTime);
    }

    private static class deleteAsyncTask extends AsyncTask<Long, Void, Void>{
        private SessionDao mAsyncTaskDao;
        private PeriodDao mAsyncTaskPeriodDao;
        deleteAsyncTask(SessionDao dao, PeriodDao pDao){
            mAsyncTaskDao = dao;
            mAsyncTaskPeriodDao = pDao;
        }

        @Override
        protected Void doInBackground(final Long... params){
            if(params[0]>0L){
                mAsyncTaskDao.deleteOlder(params[0]);
                mAsyncTaskPeriodDao.deleteOlder(params[0]);
            } else {
                mAsyncTaskDao.deleteAll();
                mAsyncTaskPeriodDao.deleteAll();
            }

            return null;
        }
    }

    private static class deletePeriodAsyncTask extends AsyncTask<Long, Void, Void>{
        private PeriodDao mAsyncTaskPeriodDao;
        deletePeriodAsyncTask(PeriodDao pDao){
            mAsyncTaskPeriodDao = pDao;
        }

        @Override
        protected Void doInBackground(final Long... params){
                mAsyncTaskPeriodDao.deletePeriod(params[0]);
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
