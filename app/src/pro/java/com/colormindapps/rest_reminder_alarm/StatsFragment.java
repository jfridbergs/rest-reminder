package com.colormindapps.rest_reminder_alarm;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.colormindapps.rest_reminder_alarm.charts.ColumnGraphView;
import com.colormindapps.rest_reminder_alarm.charts.ColumnHelper;
import com.colormindapps.rest_reminder_alarm.charts.PieHelper;
import com.colormindapps.rest_reminder_alarm.charts.PieView;
import com.colormindapps.rest_reminder_alarm.data.Period;
import com.colormindapps.rest_reminder_alarm.data.PeriodTotals;
import com.colormindapps.rest_reminder_alarm.data.SessionTotals;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.ArrayList;
import java.util.List;

public class StatsFragment extends Fragment {

    TextView intervalDateFrom, intervalDateTo, sessionCount, sessionTotalLength, sessionAverageLength, noData;
    private ConstraintLayout constraintLayout;
    ColumnGraphView columnGraphView;
    Typeface titleFont;
    int intervalType;
    long  intervalStart, intervalEnd;
    private PeriodViewModel mPeriodViewModel;
    private SessionsViewModel mSessionsViewModel;
    List<Period> mPeriods;
    List<PeriodTotals> mPeriodTotals;
    SessionTotals mSessionTotals;
    private String debug = "RR_CARD_FRONT";

    public static StatsFragment newInstance(int intervalType, long intervalStart, long intervalEnd) {

        StatsFragment fragment = new StatsFragment();
        Bundle args = new Bundle();
        args.putInt("interval_type", intervalType);
        args.putLong("interval_start", intervalStart);
        args.putLong("interval_end", intervalEnd);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle data = getArguments();
        if(data!=null){
            intervalStart = data.getLong("interval_start");
            intervalEnd = data.getLong("interval_end");
            intervalType = data.getInt("interval_type");
        }
        View view = inflater.inflate(R.layout.stats_fragment, container, false);

        titleFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/HelveticaNeueLTPro-ThCn.otf");

        constraintLayout = view.findViewById(R.id.totals);

        noData = view.findViewById(R.id.no_data);
        intervalDateFrom = view.findViewById(R.id.session_date_from);
        intervalDateTo = view.findViewById(R.id.session_date_to);
        sessionCount = view.findViewById(R.id.value_count);
        sessionTotalLength = view.findViewById(R.id.value_total);
        sessionAverageLength = view.findViewById(R.id.value_average);
        columnGraphView = view.findViewById(R.id.column_graph_view);

        mPeriodViewModel = new ViewModelProvider(requireActivity()).get(PeriodViewModel.class);
        mSessionsViewModel = new ViewModelProvider(requireActivity()).get(SessionsViewModel.class);


        intervalDateFrom.setTypeface(titleFont);
        intervalDateTo.setTypeface(titleFont);

        switch(intervalType){
            case 0: {
                intervalDateFrom.setText("Overall stats");
                break;
            }
            case 1: {
                intervalDateFrom.setText(RReminder.getSessionDateString(0,intervalStart));
                intervalDateTo.setText(RReminder.getSessionDateString(0,intervalEnd-1000000));
                break;
            }
            case 2: {
                intervalDateFrom.setText(RReminder.getSessionDateString(1,intervalStart+1000*60*60));
                break;
            }
            case 3: {
                intervalDateFrom.setText(RReminder.getSessionDateString(2,intervalStart+1000*60*60));
                break;
            }
            default: break;
        }

        fillContent();







        return view;
    }

    public void fillContent(){
        mSessionsViewModel.getSessionTotals(intervalStart, intervalEnd).observe(getViewLifecycleOwner(), new Observer<SessionTotals>(){
            @Override
            public void onChanged(@Nullable final SessionTotals sessionTotals){
                mSessionTotals = sessionTotals;
                if(sessionTotals.getSessionCount()>0){
                    sessionCount.setText(""+sessionTotals.getSessionCount());
                    sessionTotalLength.setText(RReminder.getShortDurationFromMillis(getContext(),sessionTotals.getTotalDuration()));
                    sessionAverageLength.setText(RReminder.getShortDurationFromMillis(getContext(),sessionTotals.getSessionAverageLength()));
                    fillGraph();
                } else {
                    constraintLayout.setVisibility(View.GONE);
                    columnGraphView.setVisibility(View.GONE);
                    noData.setVisibility(View.VISIBLE);
                }

            }
        });

    }

    private void fillGraph(){
        mPeriodViewModel.getPeriodTotals(intervalStart, intervalEnd).observe(getViewLifecycleOwner(), new Observer<List<PeriodTotals>>(){
            @Override
            public void onChanged(@Nullable final List<PeriodTotals> periodTotals){
                mPeriodTotals = periodTotals;
                int periodCount = mPeriodTotals.get(0).getPeriodCount();
                long totalDuration = mPeriodTotals.get(0).getTotalDuration();
                Log.d(debug, "TOTALS FOR WORK: period count: "+periodCount+", totalLength+ "+RReminder.getDurationFromMillis(getContext(),totalDuration));
                set(columnGraphView);
            }
        });
    }

    private void set(ColumnGraphView columnGraphView){
        float workPercent = ((float)mPeriodTotals.get(0).getTotalDuration() / mSessionTotals.getTotalDuration())*100;
        int workPercentInt = Math.round(workPercent);
        int restPercentInt = 100-workPercentInt;

        ArrayList<ColumnHelper> columnHelperList = new ArrayList<ColumnHelper>();
        columnHelperList.add(new ColumnHelper(workPercentInt, getResources().getColor(R.color.work_chart), mPeriodTotals.get(0).getPeriodCount(), mPeriodTotals.get(0).getTotalDuration(), mPeriodTotals.get(0).getExtendCount(), mPeriodTotals.get(0).getTotalExtendDuration()));
        columnHelperList.add(new ColumnHelper(restPercentInt, getResources().getColor(R.color.rest_chart), mPeriodTotals.get(1).getPeriodCount(), mPeriodTotals.get(1).getTotalDuration(), mPeriodTotals.get(1).getExtendCount(), mPeriodTotals.get(1).getTotalExtendDuration()));
        columnGraphView.setColumnData(columnHelperList);
    }


}
