package com.colormindapps.rest_reminder_alarm;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.colormindapps.rest_reminder_alarm.data.Period;
import com.colormindapps.rest_reminder_alarm.data.PeriodTotals;

import java.util.List;

public class PeriodViewModel extends AndroidViewModel {

    private final RReminderRepository mRepository;

    public PeriodViewModel(Application application){
        super(application);
        mRepository = new RReminderRepository(application);
    }

    LiveData<List<Period>> getSessionPeriods(long sessionStart, long sessionEnd){
        return mRepository.getSessionPeriods(sessionStart, sessionEnd);
    }


    LiveData<Period> getLastPeriod(){return mRepository.getLastPeriod();}
    LiveData<List<Period>>getLastTwoPeriods(){return mRepository.getLastTwoPeriods();}



    public void insert(Period period){
        mRepository.insertPeriod(period);
    }

    public void deletePeriod(Period period){mRepository.deletePeriod(period);}

    public void deleteShortSessionPeriods(long sessionStartTime){mRepository.deleteShortSessionPeriods(sessionStartTime);}


    public void update(Period period){
        mRepository.updatePeriod(period);
    }

    LiveData<List<PeriodTotals>> getPeriodTotals(long start, long end){
        return mRepository.getPeriodTotals(start, end);
    }

}
