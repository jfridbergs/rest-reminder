package com.colormindapps.rest_reminder_alarm;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.Calendar;
import java.util.List;


public class SessionsListActivity extends AppCompatActivity implements OnSessionListener {

    private SessionsViewModel mPeriodViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final SessionListAdapter adapter = new SessionListAdapter(this, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mPeriodViewModel = ViewModelProviders.of(this).get(SessionsViewModel.class);

        mPeriodViewModel.getAllSessions().observe(this, new Observer<List<Period>>(){
            @Override
            public void onChanged(@Nullable final List<Period> periods){
                long[] endTime = new long[periods.size()];
                adapter.setSessions(periods);
                for(int i = 0; i<periods.size(); i++){
                    Long endTimeValue = mPeriodViewModel.getSessionEndTime(periods.get(i).getStartTime());
                    endTime[i] = endTimeValue;
                }
                adapter.setEndTimeValues(endTime);
            }
        });

        Button fab = findViewById(R.id.add_session);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long time = Calendar.getInstance().getTimeInMillis();
                Period period = new Period(time,1,0L,1,time,0,0,0);
                mPeriodViewModel.insert(period);
            }
        });

        Button fab1 = findViewById(R.id.delete_old_sessions);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long time = Calendar.getInstance().getTimeInMillis();
                mPeriodViewModel.deleteOlder(time);
            }
        });
    }

    @Override
    public void onSessionClick(long sessionStartTime) {
        Intent intent = new Intent(this, SessionDetailsActivity.class);
        intent.putExtra(RReminder.SESSION_START_TIME, sessionStartTime);
        startActivity(intent);
    }




}

