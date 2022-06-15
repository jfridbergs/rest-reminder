package com.colormindapps.rest_reminder_alarm;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.colormindapps.rest_reminder_alarm.data.Period;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.List;


public class SessionDetailsActivity extends AppCompatActivity implements OnFlipCardListener{
    private TextView sessionTitle;
    private int sessionId;
    private long sessionStart, sessionEnd;
    boolean showingBack = false;
    private PeriodViewModel mPeriodViewModel;
    private List<Period> mPeriods;


    private String debug = "SESSION_DETAILS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_details_activity);
        sessionId = getIntent().getIntExtra(RReminder.DB_SESSION_ID,0);
        sessionStart = getIntent().getLongExtra(RReminder.DB_SESSION_START,0l);
        sessionEnd = getIntent().getLongExtra(RReminder.DB_SESSION_END,0l);

        mPeriodViewModel = new ViewModelProvider(this).get(PeriodViewModel.class);
        Log.d(debug, "session start: "+ sessionStart);
        Log.d(debug, "session end: "+ sessionEnd);
        mPeriodViewModel.getSessionPeriods(sessionStart, sessionEnd).observe(this, new Observer<List<Period>>(){
            @Override
            public void onChanged(@Nullable final List<Period> periods){
                mPeriods = periods;
                if (savedInstanceState == null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.container, SessionDetailsFragment.newInstance(sessionId, sessionStart,sessionEnd))
                            .commit();
                }
            }
        });


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);







    }

    public List<Period> getPeriods(){
        return mPeriods;
    }

    public void flipCard() {
        if (showingBack) {
            getSupportFragmentManager().popBackStack();
            showingBack=false;
            return;
        }

        // Flip to the back.

        showingBack = true;

        // Create and commit a new fragment transaction that adds the fragment for
        // the back of the card, uses custom animations, and is part of the fragment
        // manager's back stack.

        getSupportFragmentManager()
                .beginTransaction()

                // Replace the default fragment animations with animator resources
                // representing rotations when switching to the back of the card, as
                // well as animator resources representing rotations when flipping
                // back to the front (e.g. when the system Back button is pressed).
                .setCustomAnimations(
                        R.animator.card_flip_right_in,
                        R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in,
                        R.animator.card_flip_left_out)

                // Replace any fragments currently in the container view with a
                // fragment representing the next page (indicated by the
                // just-incremented currentPage variable).
                .replace(R.id.container, CardBackFragment.newInstance(sessionStart,sessionEnd))

                // Add this transaction to the back stack, allowing users to press
                // Back to get to the front of the card.
                .addToBackStack(null)

                // Commit the transaction.
                .commit();
    }





}
