package com.colormindapps.rest_reminder_alarm;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class SessionsViewModel extends AndroidViewModel {

    private RReminderRepository mRepository;
    private LiveData<List<Session>> mAllSessions, mAllSessionsPieView;
    private LiveData<Session> mSession;
    String debug =  "SESSIONS_VIEW_MODEL";

    public SessionsViewModel(Application application){
        super(application);
        Log.d(debug, "initiated");
        mRepository = new RReminderRepository(application);
        mAllSessions = mRepository.getAllSessions();
        mAllSessionsPieView = mRepository.getAllSessionsPieData();
    }

    LiveData<List<Session>> getAllSessions(){
        return mAllSessions;
    }
    LiveData<List<Session>> getAllSessionsPieView(){
        return mAllSessionsPieView;
    }
    LiveData<List<Session>> getSessionsInPeriod(long from, long to){ return mRepository.getSessionsInPeriod(from, to);}
    LiveData<List<Session>> getSessionsInPeriodASC(long from, long to){ return mRepository.getSessionsInPeriodASC(from, to);}
    LiveData<Session> getSessionByStart(long sessionStartTime){return mRepository.getSessionByStart(sessionStartTime);}
    LiveData<Session> getFirstSession(){return mRepository.getFirstSession();}
    LiveData<Session> getSessionById(int sessionId){return mRepository.getSessionById(sessionId);}


    public void insert(Session session){
        mRepository.insertSession(session);
    }

    public void deleteOlder(long currentTime){
        mRepository.deleteOlderSessions(currentTime);
    }

    public void update(Session session){
        mRepository.updateSession(session);
    }

    public void populateDatabase(){mRepository.populateDatabase();}
}
