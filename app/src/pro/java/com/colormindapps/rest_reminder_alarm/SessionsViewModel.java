package com.colormindapps.rest_reminder_alarm;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class SessionsViewModel extends AndroidViewModel {

    private RReminderRepository mRepository;
    private LiveData<List<Session>> mAllSessions;
    private LiveData<Session> mSession;

    public SessionsViewModel(Application application){
        super(application);
        mRepository = new RReminderRepository(application);
        mAllSessions = mRepository.getAllSessions();
    }

    LiveData<List<Session>> getAllSessions(){
        return mAllSessions;
    }
    LiveData<Session> getSessionId(long sessionStartTime){return mRepository.getSessionId(sessionStartTime);}


    public void insert(Session session){
        mRepository.insertSession(session);
    }

    public void deleteOlder(long currentTime){
        mRepository.deleteOlderSessions(currentTime);
    }

    public void update(Session session){
        mRepository.updateSession(session);
    }
}
