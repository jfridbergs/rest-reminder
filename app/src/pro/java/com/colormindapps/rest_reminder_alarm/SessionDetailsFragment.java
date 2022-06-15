package com.colormindapps.rest_reminder_alarm;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.colormindapps.rest_reminder_alarm.charts.ColumnHelper;
import com.colormindapps.rest_reminder_alarm.charts.PieHelper;
import com.colormindapps.rest_reminder_alarm.charts.PieView;
import com.colormindapps.rest_reminder_alarm.data.Period;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.ArrayList;
import java.util.List;

public class SessionDetailsFragment extends Fragment {

    TextView sessionDate, sessionClock, sessionDuration;
    PieView pieView;
    Typeface titleFont;
    long sessionStart, sessionEnd, sessionLength;
    int sessionId;
    List<Period> mPeriods;
    OnFlipCardListener parentActivity;
    private String debug = "RR_CARD_FRONT";

    public static SessionDetailsFragment newInstance(int sessionId, long sessionStart, long sessionEnd) {

        SessionDetailsFragment fragment = new SessionDetailsFragment();
        Bundle args = new Bundle();
        args.putInt("session_id", sessionId);
        args.putLong("session_start", sessionStart);
        args.putLong("session_end", sessionEnd);
        fragment.setArguments(args);
        return fragment;
    }

    private void setParentActivity(OnFlipCardListener activity){
        parentActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle data = getArguments();
        if(data!=null){
            sessionId = data.getInt("session_id");
            sessionStart = data.getLong("session_start");
            sessionEnd = data.getLong("session_end");
            sessionLength = sessionEnd - sessionStart;
        }


        mPeriods = parentActivity.getPeriods();
        View view = inflater.inflate(R.layout.session_details_fragment, container, false);

        titleFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/HelveticaNeueLTPro-ThCn.otf");

        sessionDate = view.findViewById(R.id.session_date);
        sessionClock = view.findViewById(R.id.session_clock);
        sessionDuration = view.findViewById(R.id.session_duration);

        sessionDate.setTypeface(titleFont);
        sessionClock.setTypeface(titleFont);
        sessionDuration.setTypeface(titleFont);

        sessionDate.setText(RReminder.getSessionDateString(0,sessionStart));
        long lastPeriodLength = mPeriods.get(mPeriods.size()-1).getDuration();
        long sessionEndTitle = sessionEnd;
        long sessionLengthTitle = 0l;
        if(lastPeriodLength<60*1000){
            sessionEndTitle-=lastPeriodLength;
            sessionLengthTitle = sessionEndTitle-sessionStart;
        } else {
            sessionLengthTitle = sessionEnd-sessionStart;
        }

        sessionClock.setText(String.format(getString(R.string.time_from_to),RReminder.getTimeString(getContext(), sessionStart).toString(),RReminder.getTimeString(getContext(), sessionEndTitle).toString()));
        sessionDuration.setText(RReminder.getDurationFromMillis(getContext(),sessionLengthTitle));

       // Button button = view.findViewById(R.id.flip_to_list);
       // button.setOnClickListener(v -> parentActivity.flipCard());

        pieView = view.findViewById(R.id.pie_view);
        set(pieView);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            setParentActivity((OnFlipCardListener) getActivity());
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnFlipCardListener");
        }
    }


    private void randomSet(PieView pieView){
        ArrayList<PieHelper> pieHelperArrayList = new ArrayList<PieHelper>();
        ArrayList<Integer> intList = new ArrayList<Integer>();
        int totalNum = (int) (5*Math.random()) + 5;

        int totalInt = 0;
        for(int i=0; i<totalNum; i++){
            int ranInt = (int)(Math.random()*10)+1;
            intList.add(ranInt);
            totalInt += ranInt;
        }
        for(int i=0; i<totalNum; i++){
            pieHelperArrayList.add(new PieHelper(100f*intList.get(i)/totalInt));
        }

        pieView.selectedPie(PieView.NO_SELECTED_INDEX);
        pieView.showPercentLabel(true);
        pieView.setDate(pieHelperArrayList);
    }

    private void set(PieView pieView){
        ArrayList<PieHelper> pieHelperArrayList = new ArrayList<>();
        long lastPeriodLength = mPeriods.get(mPeriods.size()-1).getDuration();
        int periodCount;
        if(lastPeriodLength<60*1000){
            sessionLength-=lastPeriodLength;
            sessionEnd-=lastPeriodLength;
            periodCount=mPeriods.size()-2;
        } else {
            periodCount= mPeriods.size()-1;
        }
        int percentSum = 0;
        long nextPeriodStart = 0;
        long totalWork = 0;
        long totalRest = 0;
        int workCount = 0;
        int restCount = 0;
        int workExtendCount = 0;
        int restExtendCount = 0;
        Log.d(debug, "Period count: "+mPeriods.size());
        for (int i=0; i<=periodCount;i++){
            long periodLength = mPeriods.get(i).getDuration();
            float percent = ((float)periodLength / sessionLength)*100;
            Log.d(debug, "session length: "+sessionLength);
            Log.d(debug, "period length: "+periodLength);
            Log.d(debug, "percent: "+percent);
            int percentInt = Math.round(percent);
            Log.d(debug, "round percent: "+percentInt);
            int periodType = mPeriods.get(i).getType();
            int color;
            switch(periodType){
                case 1: case 3:{
                    color = getResources().getColor(R.color.work_chart);
                    totalWork+=periodLength;
                    workCount++;
                    workExtendCount+=mPeriods.get(i).getExtendCount();
                    break;
                }
                case 2: case 4: {
                    color = getResources().getColor(R.color.rest_chart);
                    totalRest+=periodLength;
                    restCount++;
                    restExtendCount+=mPeriods.get(i).getExtendCount();
                    break;
                }
                default: color = Color.BLACK;break;
            }
            percentSum+=percentInt;
            if(percentSum==99){
                percentInt+=1;
            }
            Log.d(debug, "Percent sum: "+percentSum);
            pieHelperArrayList.add(new PieHelper(percentInt, mPeriods.get(i)));

        }
        /*
        ArrayList<PieHelper> pieHelperArrayList = new ArrayList<PieHelper>();
        pieHelperArrayList.add(new PieHelper(20, Color.BLACK));
        pieHelperArrayList.add(new PieHelper(40));
        pieHelperArrayList.add(new PieHelper(40));
        //pieHelperArrayList.add(new PieHelper(12));
        //pieHelperArrayList.add(new PieHelper(32));
        */
        pieView.setDate(pieHelperArrayList);
        pieView.setSession(sessionStart,sessionEnd);

        float workPercent = ((float)totalWork / sessionLength)*100;
        int workPercentInt = Math.round(workPercent);
        int restPercentInt = 100-workPercentInt;

        ArrayList<ColumnHelper> columnHelperList = new ArrayList<ColumnHelper>();
        columnHelperList.add(new ColumnHelper(workPercentInt, getResources().getColor(R.color.work_chart), workCount, totalWork, workExtendCount,0));
        columnHelperList.add(new ColumnHelper(restPercentInt, getResources().getColor(R.color.rest_chart), restCount, totalRest, restExtendCount,0));
        pieView.setColumnData(columnHelperList);
        pieView.setOnPieClickListener(new PieView.OnPieClickListener() {
            @Override
            public void onPieClick(int index) {
                if(index == PieView.NO_SELECTED_INDEX) {
                    if (pieView.isDonutOnScreen()){
                        pieView.toColumn();
                    } else {
                        pieView.toDonut();
                    }
                }
            }
        });
        //pieView.selectedPie(2);
    }

}
