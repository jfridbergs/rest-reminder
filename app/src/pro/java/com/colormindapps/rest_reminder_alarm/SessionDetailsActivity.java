package com.colormindapps.rest_reminder_alarm;

import androidx.lifecycle.Observer;
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
    private PeriodViewModel mPeriodViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sessionId = getIntent().getIntExtra(RReminder.SESSION_ID,0);

        sessionTitle = findViewById(R.id.session_Id);

        sessionTitle.setText("Session ID: "+sessionId);

        RecyclerView recyclerView = findViewById(R.id.recyclerview_periods);
        final PeriodListAdapter adapter = new PeriodListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mPeriodViewModel = ViewModelProviders.of(this).get(PeriodViewModel.class);

        mPeriodViewModel.getSessionPeriods(sessionId).observe(this, new Observer<List<Period>>(){
            @Override
            public void onChanged(@Nullable final List<Period> periods){
                adapter.setPeriods(periods);
            }
        });
    }

}
