package com.colormindapps.rest_reminder_alarm;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

public class SessionsViewModel extends AndroidViewModel {

    private PeriodRepository mRepository;
    private LiveData<List<Period>> mAllSessions;
    private Period mPeriod;

    public SessionsViewModel(Application application){
        super(application);
        mRepository = new PeriodRepository(application);
        mAllSessions = mRepository.getAllSessions();
    }

    LiveData<List<Period>> getAllSessions(){
        return mAllSessions;
    }
    LiveData<Long> getSessionEndTime(long sessionStartTime){return mRepository.getSessionEndTime(sessionStartTime);}


    public void insert(Period period){
        mRepository.insert(period);
    }

    public void deleteOlder(long currentTime){
        mRepository.deleteOlder(currentTime);
    }

    public void update(Period period){
        mRepository.update(period);
    }
}
