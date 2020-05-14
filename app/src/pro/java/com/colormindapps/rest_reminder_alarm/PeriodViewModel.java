package com.colormindapps.rest_reminder_alarm;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class PeriodViewModel extends AndroidViewModel {

    private RReminderRepository mRepository;
    private LiveData<List<Period>> mSessionPeriods;

    public PeriodViewModel(Application application){
        super(application);
        mRepository = new RReminderRepository(application);
    }

    LiveData<List<Period>> getSessionPeriods(int sessionId){
        mSessionPeriods = mRepository.getSessionPeriods(sessionId);
        return mSessionPeriods;
    }


    public void insert(Period period){
        mRepository.insertPeriod(period);
    }


    public void update(Period period){
        mRepository.updatePeriod(period);
    }
}
