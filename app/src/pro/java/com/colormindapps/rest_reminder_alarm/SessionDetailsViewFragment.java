package com.colormindapps.rest_reminder_alarm;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.colormindapps.rest_reminder_alarm.charts.ColumnHelper;
import com.colormindapps.rest_reminder_alarm.charts.PieHelper;
import com.colormindapps.rest_reminder_alarm.charts.PieView;
import com.colormindapps.rest_reminder_alarm.data.Period;
import com.colormindapps.rest_reminder_alarm.data.PeriodTotals;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.ArrayList;
import java.util.List;

public class SessionDetailsViewFragment extends Fragment {

    TextView sessionDate, sessionClock, sessionDuration;
    PieView pieView;
    Typeface titleFont;
    long sessionStart, sessionEnd, sessionLength;
    private PeriodViewModel mPeriodViewModel;
    List<Period> mPeriods;
    List<PeriodTotals> mPeriodTotals;

    public static SessionDetailsViewFragment newInstance(long sessionStart, long sessionEnd) {

        SessionDetailsViewFragment fragment = new SessionDetailsViewFragment();
        Bundle args = new Bundle();
        args.putLong("session_start", sessionStart);
        args.putLong("session_end", sessionEnd);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle data = getArguments();
        if(data!=null){
            sessionStart = data.getLong("session_start");
            sessionEnd = data.getLong("session_end");
            sessionLength = sessionEnd - sessionStart;
        }

        View view = inflater.inflate(R.layout.session_details_view_fragment, container, false);
        if(getActivity()!=null){
            titleFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/HelveticaNeueLTPro-ThCn.otf");
        }


        sessionDate = view.findViewById(R.id.session_date);
        sessionClock = view.findViewById(R.id.session_clock);
        sessionDuration = view.findViewById(R.id.session_duration);

        sessionDate.setTypeface(titleFont);
        sessionClock.setTypeface(titleFont);
        sessionDuration.setTypeface(titleFont);

        sessionDate.setText(RReminder.getSessionDateString(0,sessionStart));

        mPeriodViewModel = new ViewModelProvider(requireActivity()).get(PeriodViewModel.class);
        pieView = view.findViewById(R.id.pie_view);

        mPeriodViewModel.getPeriodTotals(sessionStart, sessionEnd).observe(getViewLifecycleOwner(), periodTotals -> {
            mPeriodTotals = periodTotals;
            launchPeriodsQuery();
        });

        return view;
    }

    private void launchPeriodsQuery(){
        mPeriodViewModel.getSessionPeriods(sessionStart, sessionEnd).observe(getViewLifecycleOwner(), periods -> {
            mPeriods = periods;
            long sessionLengthTitle = sessionEnd-sessionStart;

            sessionClock.setText(String.format(getString(R.string.time_from_to),RReminder.getTimeString(getContext(), sessionStart).toString(),RReminder.getTimeString(getContext(), sessionEnd).toString()));
            sessionDuration.setText(RReminder.getDurationFromMillis(getContext(),sessionLengthTitle));
            set(pieView);
        });
    }

    private void set(PieView pieView){
        ArrayList<PieHelper> pieHelperArrayList = new ArrayList<>();
        int periodCount = mPeriods.size()-1;
        long sessionLengthPie = 0L;
        for (Period period :  mPeriods) {
            sessionLengthPie+=period.getDuration();
        }

        float fractionTotal=0;
        //Log.d(debug, "Period count: "+mPeriods.size());
        for (int i=0; i<=periodCount;i++){
            long periodLength = mPeriods.get(i).getDuration();
            float fraction = (float)periodLength / sessionLengthPie;
            fractionTotal+=fraction;


           // Log.d(debug, "Percent sum: "+percentSum);
            pieHelperArrayList.add(new PieHelper(fraction, mPeriods.get(i)));

        }
        pieView.setDate(pieHelperArrayList);

        float workPercent = ((float)mPeriodTotals.get(0).getTotalDuration() / sessionLength)*100;
        float restPercent = 100-workPercent;

        ArrayList<ColumnHelper> columnHelperList = new ArrayList<>();
        columnHelperList.add(new ColumnHelper(workPercent, getResources().getColor(R.color.work_chart), mPeriodTotals.get(0).getPeriodCount(), mPeriodTotals.get(0).getTotalDuration(), mPeriodTotals.get(0).getExtendCount(), mPeriodTotals.get(0).getTotalExtendDuration()));
        columnHelperList.add(new ColumnHelper(restPercent, getResources().getColor(R.color.rest_chart), mPeriodTotals.get(1).getPeriodCount(), mPeriodTotals.get(1).getTotalDuration(), mPeriodTotals.get(1).getExtendCount(), mPeriodTotals.get(1).getTotalExtendDuration()));
        pieView.setColumnData(columnHelperList);

    }

}
