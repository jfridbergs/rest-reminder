package com.colormindapps.rest_reminder_alarm;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.colormindapps.rest_reminder_alarm.data.Session;
import com.colormindapps.rest_reminder_alarm.data.SessionTotals;

import java.util.List;

public class SessionsViewModel extends AndroidViewModel {

    private final RReminderRepository mRepository;
    private final LiveData<List<Session>> mAllSessionsPieView;

    public SessionsViewModel(Application application){
        super(application);
        mRepository = new RReminderRepository(application);
        mAllSessionsPieView = mRepository.getAllSessionsPieData();
    }

    LiveData<List<Session>> getAllSessionsPieView(){
        return mAllSessionsPieView;
    }
    LiveData<List<Session>> getSessionsInPeriod(long from, long to){ return mRepository.getSessionsInPeriod(from, to);}
    LiveData<List<Session>> getSessionsInPeriodASC(long from, long to){ return mRepository.getSessionsInPeriodASC(from, to);}
    LiveData<Session> getSessionByStart(long sessionStartTime){return mRepository.getSessionByStart(sessionStartTime);}
    LiveData<Session> getFirstSession(){return mRepository.getFirstSession();}
    LiveData<Session> getLastSession(){return mRepository.getLastSession();}
    LiveData<Session> getCurrentSession(){return mRepository.getCurrentSession();}
    LiveData<Session> getSessionById(int sessionId){return mRepository.getSessionById(sessionId);}
    LiveData<SessionTotals> getSessionTotals(long start, long end){return mRepository.getSessionTotals(start, end);}
    LiveData<Integer> hasSessions(long from, long to){return mRepository.hasSessions(from, to);}


    public void insert(Session session){
        mRepository.insertSession(session);
    }

    public void deleteOlder(long currentTime){
        mRepository.deleteOlderSessions(currentTime);
    }

    public void deleteSession(Session session) {mRepository.deleteSession(session);}

    public void update(Session session){
        mRepository.updateSession(session);
    }

}
