package com.colormindapps.rest_reminder_alarm;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.colormindapps.rest_reminder_alarm.data.Period;
import com.colormindapps.rest_reminder_alarm.data.Session;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.List;


public class SessionDetailsViewActivity extends AppCompatActivity{
    private long sessionStart, sessionEnd;
    boolean showingBack = false;
    private PeriodViewModel mPeriodViewModel;
    private SessionsViewModel mSessionViewModel;
    private List<Period> mPeriods;
    private List<Session> mSessions;

    private ViewPager2 viewPager;

    private FragmentStateAdapter pagerAdapter;


    private String debug = "SESSION_DETAILS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_details_view_activity);
        sessionStart = getIntent().getLongExtra(RReminder.DB_SESSION_START,0l);
        sessionEnd = getIntent().getLongExtra(RReminder.DB_SESSION_END,0l);

        mPeriodViewModel = new ViewModelProvider(this).get(PeriodViewModel.class);
        mSessionViewModel = new ViewModelProvider(this).get(SessionsViewModel.class);

        viewPager = findViewById(R.id.pager);
        Log.d(debug, "session start: "+ sessionStart);
        Log.d(debug, "session end: "+ sessionEnd);

        mSessionViewModel.getAllSessionsPieView().observe(this, new Observer<List<Session>>(){
            @Override
            public void onChanged(@Nullable final List<Session> sessions){
                mSessions = sessions;
                launchPagerAdapter();

            }
        });




        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);







    }

    public void launchPagerAdapter(){
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        int limit = Math.min(mSessions.size(), 5);
        viewPager.setOffscreenPageLimit(limit);
        Log.d(debug, "session position: "+getSessionPosition(sessionStart));
        viewPager.setCurrentItem(getSessionPosition(sessionStart), false);

    }

    public List<Period> getPeriods(){
        return mPeriods;
    }

    public int getSessionPosition(long sessionStart){
        int i = 0;
        for (Session session : mSessions) {
            if (session.getSessionStart()==sessionStart) {
                return i;
            } else {
                i++;
            }
        }
        return 0;
    }



    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            return CardFrontViewFragment.newInstance(mSessions.get(position).getSessionStart(),mSessions.get(position).getSessionEnd());
        }

        @Override
        public int getItemCount() {
            return mSessions.size();
        }
    }





}
