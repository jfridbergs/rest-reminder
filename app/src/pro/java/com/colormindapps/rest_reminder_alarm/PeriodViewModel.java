package com.colormindapps.rest_reminder_alarm;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

public class PeriodViewModel extends AndroidViewModel {

    private PeriodRepository mRepository;
    private LiveData<List<Period>> mSessionPeriods;

    public PeriodViewModel(Application application, long sessionStartTime){
        super(application);
        mRepository = new PeriodRepository(application);
        mSessionPeriods = mRepository.getSessionPeriods(sessionStartTime);
    }

    LiveData<List<Period>> getSessionPeriods(){
        return mSessionPeriods;
    }


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
