package com.colormindapps.rest_reminder_alarm;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.colormindapps.rest_reminder_alarm.data.Period;
import com.colormindapps.rest_reminder_alarm.data.PeriodTotals;

import java.util.List;

public class PeriodViewModel extends AndroidViewModel {

    private RReminderRepository mRepository;
    private LiveData<List<Period>> mSessionPeriods;
    private LiveData<Period> mPeriod;

    public PeriodViewModel(Application application){
        super(application);
        mRepository = new RReminderRepository(application);
    }

    LiveData<List<Period>> getSessionPeriods(long sessionStart, long sessionEnd){
        mSessionPeriods = mRepository.getSessionPeriods(sessionStart, sessionEnd);
        return mSessionPeriods;
    }

    LiveData<Period> getPeriod(long endTime){
        mPeriod = mRepository.getPeriod(endTime);
        return mPeriod;
    }

    LiveData<Period> getLastPeriod(){return mRepository.getLastPeriod();}

    public void deletePeriod(long startTime){mRepository.deletePeriod(startTime);}


    public void insert(Period period){
        mRepository.insertPeriod(period);
    }


    public void update(Period period){
        mRepository.updatePeriod(period);
    }

    LiveData<Integer> getPeriodCount(int type, long start, long end) {
        return mRepository.getPeriodCount(type, start, end);
    }

    LiveData<List<PeriodTotals>> getPeriodTotals(long start, long end){
        return mRepository.getPeriodTotals(start, end);
    }

}
