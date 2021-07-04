package com.colormindapps.rest_reminder_alarm;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.List;


public class SessionDetailsActivity extends AppCompatActivity {
    private TextView sessionTitle;
    private int sessionId;
    private long sessionStart, sessionEnd;
    private PeriodViewModel mPeriodViewModel;

    private String debug = "SESSION_DETAILS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sessionId = getIntent().getIntExtra(RReminder.DB_SESSION_ID,0);
        sessionStart = getIntent().getLongExtra(RReminder.DB_SESSION_START,0l);
        sessionEnd = getIntent().getLongExtra(RReminder.DB_SESSION_END,0l);


        sessionTitle = findViewById(R.id.session_Id);

        sessionTitle.setText("Session ID: "+sessionId+ ". Started: "+RReminder.getTimeString(this, sessionStart).toString());

        RecyclerView recyclerView = findViewById(R.id.recyclerview_periods);
        final PeriodListAdapter adapter = new PeriodListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mPeriodViewModel = new ViewModelProvider(this).get(PeriodViewModel.class);
        Log.d(debug, "session start: "+ sessionStart);
        Log.d(debug, "session end: "+ sessionEnd);
        mPeriodViewModel.getSessionPeriods(sessionStart, sessionEnd).observe(this, new Observer<List<Period>>(){
            @Override
            public void onChanged(@Nullable final List<Period> periods){
                adapter.setPeriods(periods);
            }
        });
    }

}
