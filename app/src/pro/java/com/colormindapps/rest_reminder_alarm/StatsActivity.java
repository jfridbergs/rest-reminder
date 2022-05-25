package com.colormindapps.rest_reminder_alarm;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
import com.colormindapps.rest_reminder_alarm.data.TimeInterval;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class StatsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private long sessionStart, sessionEnd;
    boolean showingBack = false;
    private PeriodViewModel mPeriodViewModel;
    private SessionsViewModel mSessionViewModel;
    private List<Period> mPeriods;
    private List<Session> mSessions;
    private Session firstSession;
    private Spinner spinner;
    private List<TimeInterval> yearly, monthly, weekly;

    private ViewPager2 viewPager;

    private FragmentStateAdapter pagerWeeklyAdapter, pagerMonthlyAdapter, pagerYearlyAdapter, pagerOverallAdapter;


    private String debug = "SESSION_DETAILS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_activity);

        mPeriodViewModel = new ViewModelProvider(this).get(PeriodViewModel.class);
        mSessionViewModel = new ViewModelProvider(this).get(SessionsViewModel.class);

        viewPager = findViewById(R.id.pager);

        spinner = findViewById(R.id.intervals_spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.intervals_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        mSessionViewModel.getFirstSession().observe(this, new Observer<Session>(){
            @Override
            public void onChanged(@Nullable final Session session){
                firstSession = session;
                //launchPagerAdapter();
                initIntervals();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);







    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        String selection = (String)parent.getItemAtPosition(pos);
        switch(selection){
            case "overall":
                launchPagerAdapter(0); break;
            case "weekly":
                launchPagerAdapter(1); break;
            case "monthly":
                launchPagerAdapter(2); break;
            case "yearly":
                launchPagerAdapter(3); break;

        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void initIntervals(){
        Calendar date = Calendar.getInstance();
        long currentTime = date.getTimeInMillis();
        long firstSessionStart = firstSession.getSessionStart();
        date.set(Calendar.DAY_OF_MONTH,1);
        date.set(Calendar.MONTH,0);
        date.set(Calendar.HOUR_OF_DAY,0);
        date.set(Calendar.MINUTE,0);
        date.set(Calendar.SECOND,0);
        yearly = new ArrayList<TimeInterval>();
        long intervalEnd = currentTime;
        long intervalStart = date.getTimeInMillis();
        while(intervalStart>firstSessionStart){
            yearly.add(new TimeInterval(intervalStart,intervalEnd));
            date.set(Calendar.YEAR,(date.get(Calendar.YEAR)-1));
            intervalEnd = intervalStart;
            intervalStart = date.getTimeInMillis();
        }
        yearly.add(new TimeInterval(firstSessionStart,intervalEnd));

        monthly = new ArrayList<TimeInterval>();
        date = Calendar.getInstance();
        date.set(Calendar.DAY_OF_MONTH,1);
        date.set(Calendar.HOUR_OF_DAY,0);
        date.set(Calendar.MINUTE,0);
        date.set(Calendar.SECOND,0);
        intervalEnd = currentTime;
        intervalStart = date.getTimeInMillis();
        int month = date.get(Calendar.MONTH);
        while(intervalStart>firstSessionStart){
            monthly.add(new TimeInterval(intervalStart,intervalEnd));
            month-=1;
            if(month<0) {
                date.set(Calendar.YEAR, (date.get(Calendar.YEAR) - 1));
                month = 11;
            }
            date.set(Calendar.MONTH,month);
            intervalEnd = intervalStart;
            intervalStart = date.getTimeInMillis();
        }
        monthly.add(new TimeInterval(firstSessionStart,intervalEnd));

        weekly = new ArrayList<TimeInterval>();
        date = Calendar.getInstance();
        int dayOfWeek = date.get(Calendar.DAY_OF_WEEK)-2;
        date.set(Calendar.DAY_OF_MONTH,(date.get(Calendar.DAY_OF_MONTH)-dayOfWeek));
        date.set(Calendar.HOUR_OF_DAY,0);
        date.set(Calendar.MINUTE,0);
        date.set(Calendar.SECOND,0);
        intervalEnd = currentTime;
        intervalStart = date.getTimeInMillis();
        while(intervalStart>firstSessionStart){
            weekly.add(new TimeInterval(intervalStart,intervalEnd));
            intervalEnd = intervalStart;
            intervalStart-=604800000;
        }
        weekly.add(new TimeInterval(firstSessionStart,intervalEnd));


        spinner.setOnItemSelectedListener(this);

        launchPagerAdapter(0);
    }

    public void launchPagerAdapter(int intervalType){
        viewPager.setVisibility(View.VISIBLE);
        switch(intervalType){
            case 0: {
                if(pagerOverallAdapter==null) pagerOverallAdapter = new ScreenSlidePagerOverallAdapter(this);
                viewPager.setAdapter(pagerOverallAdapter);
            } break;
            case 1: {
                if(pagerWeeklyAdapter==null) pagerWeeklyAdapter = new ScreenSlidePagerWeeklyAdapter(this);
                viewPager.setAdapter(pagerWeeklyAdapter);
            } break;
            case 2: {
                if(pagerMonthlyAdapter==null) pagerMonthlyAdapter = new ScreenSlidePagerMonthlyAdapter(this);
                viewPager.setAdapter(pagerMonthlyAdapter);
            } break;
            case 3: {
                if(pagerYearlyAdapter==null) pagerYearlyAdapter = new ScreenSlidePagerYearlyAdapter(this);
                viewPager.setAdapter(pagerYearlyAdapter);
            } break;
            default: break;
        }
        int limit = Math.min(yearly.size(), 5);
        viewPager.setOffscreenPageLimit(limit);
        viewPager.setCurrentItem(weekly.size(), false);
        //Log.d(debug, "session position: "+getSessionPosition(sessionStart));
        //viewPager.setCurrentItem(getSessionPosition(sessionStart), false);

    }

    private class ScreenSlidePagerWeeklyAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerWeeklyAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            return StatsFragment.newInstance(1, weekly.get(weekly.size()-position-1).getStart(),weekly.get(weekly.size()-position-1).getEnd());
        }

        @Override
        public int getItemCount() {
            return weekly.size();
        }
    }

    private class ScreenSlidePagerMonthlyAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerMonthlyAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            return StatsFragment.newInstance(2, monthly.get(monthly.size()-position-1).getStart(),monthly.get(monthly.size()-position-1).getEnd());
        }

        @Override
        public int getItemCount() {
            return monthly.size();
        }
    }

    private class ScreenSlidePagerYearlyAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerYearlyAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            return StatsFragment.newInstance(3, yearly.get(yearly.size()-position-1).getStart(),yearly.get(yearly.size()-position-1).getEnd());
        }

        @Override
        public int getItemCount() {
            return yearly.size();
        }
    }

    private class ScreenSlidePagerOverallAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerOverallAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            return StatsFragment.newInstance(0,  firstSession.getSessionStart(),Calendar.getInstance().getTimeInMillis());
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }





}
