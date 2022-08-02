package com.colormindapps.rest_reminder_alarm;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.colormindapps.rest_reminder_alarm.data.Period;
import com.colormindapps.rest_reminder_alarm.data.PeriodDao;
import com.colormindapps.rest_reminder_alarm.data.PeriodTotals;
import com.colormindapps.rest_reminder_alarm.data.Session;
import com.colormindapps.rest_reminder_alarm.data.SessionDao;
import com.colormindapps.rest_reminder_alarm.data.SessionTotals;

import java.util.List;

public class RReminderRepository {

    private final PeriodDao mPeriodDao;
    private final SessionDao mSessionDao;
    private final LiveData<List<Session>> mAllSessions;
    private final LiveData<List<Session>> mAllSessionPieView;
    private final RReminderRoomDatabase db;

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
    LiveData<Session> getLastSession() {return mSessionDao.getLastSession();}
    LiveData<Session> getCurrentSession() {return mSessionDao.getCurrentSession();}
    LiveData<Integer> hasSessions(long from, long to){return mSessionDao.hasSessions(from, to);}
    LiveData<List<Period>> getSessionPeriods(long sessionStart, long sessionEnd){return mPeriodDao.getSessionPeriods(sessionStart, sessionEnd);}
    LiveData<Period> getLastPeriod(){return mPeriodDao.getLastPeriod();}
    LiveData<List<Period>> getLastTwoPeriods(){return mPeriodDao.getLastTwoPeriods();}
    LiveData<List<PeriodTotals>> getPeriodTotals(long start, long end){return mPeriodDao.getPeriodTotals(start, end);}
    LiveData<SessionTotals> getSessionTotals(long start, long end){return mSessionDao.getSessionTotals(start, end);}


    void insertPeriod(Period period) {
        RReminderRoomDatabase.databaseExecutor.execute(() -> mPeriodDao.insertPeriod(period));
    }

    void deletePeriod(Period period) {
        RReminderRoomDatabase.databaseExecutor.execute(() -> mPeriodDao.deletePeriod(period));
    }

    void deleteSession(Session session) {
        RReminderRoomDatabase.databaseExecutor.execute(() -> mSessionDao.deleteSession(session));
    }


    public void populateDatabase(){
        db.populateDatabase();
    }

    public void populateDatabaseForStats(){
        db.populateDatabaseForStats();
    }







    void insertSession(Session session) {
        RReminderRoomDatabase.databaseExecutor.execute(() -> mSessionDao.insertSession(session));
    }






    void deleteOlderSessions(long currentTime) {
        RReminderRoomDatabase.databaseExecutor.execute(() -> {
            if(currentTime>0L){
                mSessionDao.deleteOlder(currentTime);
                mPeriodDao.deleteOlder(currentTime);
            } else {
                mSessionDao.deleteAll();
                mPeriodDao.deleteAll();
            }
        });
    }


    void deleteShortSessionPeriods(long sessionStartTime) {
        RReminderRoomDatabase.databaseExecutor.execute(() -> mPeriodDao.deleteShortSessionPeriods(sessionStartTime));
    }


    void updatePeriod(Period period) {
        RReminderRoomDatabase.databaseExecutor.execute(() -> mPeriodDao.updatePeriod(period));
    }


    void updateSession(Session session) {
        RReminderRoomDatabase.databaseExecutor.execute(() -> mSessionDao.updateSession(session));
    }


}
