package com.colormindapps.rest_reminder_alarm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.Calendar;
import java.util.List;


public class SessionsListActivity extends AppCompatActivity implements OnSessionListener {

    private SessionsViewModel mSessionsViewModel;
    private String debug = "RREMINDER_SESSION_LIST_ACTIVITY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);

        RecyclerView recyclerView = findViewById(R.id.recyclerview_sessions);
        final SessionListAdapter adapter = new SessionListAdapter(this, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mSessionsViewModel = ViewModelProviders.of(this).get(SessionsViewModel.class);

        mSessionsViewModel.getAllSessions().observe(this, new Observer<List<Session>>(){
            @Override
            public void onChanged(@Nullable final List<Session> sessions){
                adapter.setSessions(sessions);
                Log.d(debug, "adding items to adapter");

            }
        });
        Log.d(debug, "item count: "+ adapter.getItemCount());


        Button fab = findViewById(R.id.delete_all);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long time = Calendar.getInstance().getTimeInMillis();
                long endTime = time +60*60*1000;
                Session session = new Session(0,time, endTime);
                mSessionsViewModel.deleteOlder(0L);
            }
        });

        Button fab1 = findViewById(R.id.delete_old_sessions);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long time = Calendar.getInstance().getTimeInMillis();
                mSessionsViewModel.deleteOlder(time);
            }
        });
    }



    @Override
    public void onSessionClick(int sessionId) {
        Intent intent = new Intent(this, SessionDetailsActivity.class);
        intent.putExtra(RReminder.SESSION_ID, sessionId);
        startActivity(intent);
    }




}

