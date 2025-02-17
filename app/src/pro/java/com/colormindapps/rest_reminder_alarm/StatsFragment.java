package com.colormindapps.rest_reminder_alarm;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.colormindapps.rest_reminder_alarm.charts.ColumnGraphView;
import com.colormindapps.rest_reminder_alarm.charts.ColumnHelper;
import com.colormindapps.rest_reminder_alarm.data.PeriodTotals;
import com.colormindapps.rest_reminder_alarm.data.SessionTotals;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.ArrayList;
import java.util.List;

public class StatsFragment extends Fragment {

    TextView intervalDateFrom, sessionCount, sessionTotalLength, sessionAverageLength, noData;
    private ConstraintLayout constraintLayout;
    ColumnGraphView columnGraphView;
    Typeface titleFont;
    int intervalType;
    long  intervalStart, intervalEnd;
    private PeriodViewModel mPeriodViewModel;
    private SessionsViewModel mSessionsViewModel;
    List<PeriodTotals> mPeriodTotals;
    SessionTotals mSessionTotals;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle data = getArguments();
        if(data!=null){
            intervalStart = data.getLong("interval_start");
            intervalEnd = data.getLong("interval_end");
            intervalType = data.getInt("interval_type");
        }
        View view = inflater.inflate(R.layout.stats_fragment, container, false);

        if(getActivity()!=null){
            titleFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/HelveticaNeueLTPro-ThCn.otf");
        }
        constraintLayout = view.findViewById(R.id.totals);

        noData = view.findViewById(R.id.no_data);
        intervalDateFrom = view.findViewById(R.id.session_date_from);
        sessionCount = view.findViewById(R.id.value_count);
        sessionTotalLength = view.findViewById(R.id.value_total);
        sessionAverageLength = view.findViewById(R.id.value_average);
        columnGraphView = view.findViewById(R.id.column_graph_view);

        mPeriodViewModel = new ViewModelProvider(requireActivity()).get(PeriodViewModel.class);
        mSessionsViewModel = new ViewModelProvider(requireActivity()).get(SessionsViewModel.class);


        intervalDateFrom.setTypeface(titleFont);

        switch(intervalType){
            case 0: {
                intervalDateFrom.setText(getString(R.string.stats_title_overall));
                break;
            }
            case 1: {
                intervalDateFrom.setText(RReminder.getSessionDateString(0,intervalEnd-1000000));

                break;
            }
            case 2: {
                intervalDateFrom.setText(RReminder.getSessionDateWeekString(intervalStart, intervalEnd-1000000));

                break;
            }
            case 3: {
                intervalDateFrom.setText(RReminder.getSessionDateString(1,intervalStart+1000*60*60));
                break;
            }
            case 4: {
                intervalDateFrom.setText(RReminder.getSessionDateString(2,intervalStart+1000*60*60));
                break;
            }
            default: break;
        }

        fillContent();







        return view;
    }

    public void fillContent(){
        mSessionsViewModel.getSessionTotals(intervalStart, intervalEnd).observe(getViewLifecycleOwner(), sessionTotals -> {
            mSessionTotals = sessionTotals;
            if(sessionTotals.getSessionCount()>0){
                String sessionCountString = ""+sessionTotals.getSessionCount();
                sessionCount.setText(sessionCountString);
                sessionTotalLength.setText(RReminder.getShortDurationFromMillis(getContext(),sessionTotals.getTotalDuration()));
                sessionAverageLength.setText(RReminder.getShortDurationFromMillis(getContext(),sessionTotals.getSessionAverageLength()));
                fillGraph();
            } else {
                constraintLayout.setVisibility(View.GONE);
                columnGraphView.setVisibility(View.GONE);
                noData.setVisibility(View.VISIBLE);
            }

        });

    }

    private void fillGraph(){
        mPeriodViewModel.getPeriodTotals(intervalStart, intervalEnd).observe(getViewLifecycleOwner(), periodTotals -> {
            mPeriodTotals = periodTotals;
            set(columnGraphView);
        });
    }

    private void set(ColumnGraphView columnGraphView){
        float workPercent = ((float)mPeriodTotals.get(0).getTotalDuration() / mSessionTotals.getTotalDuration())*100;
        float restPercent = 100-workPercent;

        ArrayList<ColumnHelper> columnHelperList = new ArrayList<>();
        columnHelperList.add(new ColumnHelper(workPercent, getResources().getColor(R.color.work_chart), mPeriodTotals.get(0).getPeriodCount(), mPeriodTotals.get(0).getTotalDuration(), mPeriodTotals.get(0).getExtendCount(), mPeriodTotals.get(0).getTotalExtendDuration()));
        columnHelperList.add(new ColumnHelper(restPercent, getResources().getColor(R.color.rest_chart), mPeriodTotals.get(1).getPeriodCount(), mPeriodTotals.get(1).getTotalDuration(), mPeriodTotals.get(1).getExtendCount(), mPeriodTotals.get(1).getTotalExtendDuration()));
        columnGraphView.setColumnData(columnHelperList);
    }


}
