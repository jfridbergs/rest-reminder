package com.colormindapps.rest_reminder_alarm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.colormindapps.rest_reminder_alarm.data.Session;
import com.colormindapps.rest_reminder_alarm.data.TimeInterval;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class StatsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {
    private SessionsViewModel mSessionViewModel;
    private Session firstSession;
    private Spinner spinner;
    private Comparator<TimeInterval> comparator;
    private List<TimeInterval> yearly, monthly, weekly, daily;
    private final int INTERVAL_DAY = 0;
    private final int INTERVAL_WEEK = 1;
    private final int INTERVAL_MONTH = 2;
    private final int INTERVAL_YEAR = 3;

    private ImageView previous, next;

    private int currentIntervalType = 0;
    private int viewPagerPosition;

    private ViewPager2 viewPager;
    private ViewPager2.OnPageChangeCallback dailyOnPageChangeCallback, weeklyOnPageChangeCallback, monthlyOnPageChangeCallback, yearlyOnPageChangeCallback;

    private FragmentStateAdapter pagerDailyAdapter, pagerWeeklyAdapter, pagerMonthlyAdapter, pagerYearlyAdapter, pagerOverallAdapter;


    private final String debug = "SESSION_DETAILS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_activity);

        mSessionViewModel = new ViewModelProvider(this).get(SessionsViewModel.class);
        previous = findViewById(R.id.previous);
        previous.setOnClickListener(view -> viewPager.setCurrentItem(viewPagerPosition-1, true));
        next = findViewById(R.id.next);
        next.setOnClickListener(view -> viewPager.setCurrentItem(viewPagerPosition+1, true));

        comparator = (lhs, rhs) -> {
            // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
            return Long.compare(rhs.getStart(), lhs.getStart());
        };

        dailyOnPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.d(debug, "daily changed position: "+position);
                viewPagerPosition = position;
                if(position ==0){
                    previous.setVisibility(View.GONE);
                } else {
                    previous.setVisibility(View.VISIBLE);
                }
                if(position ==daily.size()-1){
                    next.setVisibility(View.GONE);
                } else {
                    next.setVisibility(View.VISIBLE);
                }
            }
        };

        weeklyOnPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                viewPagerPosition = position;
                if(position ==0){
                    previous.setVisibility(View.GONE);
                } else {
                    previous.setVisibility(View.VISIBLE);
                }
                if(position ==weekly.size()-1){
                    next.setVisibility(View.GONE);
                } else {
                    next.setVisibility(View.VISIBLE);
                }
            }
        };

        monthlyOnPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                viewPagerPosition = position;
                if(position ==0){
                    previous.setVisibility(View.GONE);
                } else {
                    previous.setVisibility(View.VISIBLE);
                }
                if(position ==monthly.size()-1){
                    next.setVisibility(View.GONE);
                } else {
                    next.setVisibility(View.VISIBLE);
                }
            }
        };

        yearlyOnPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                viewPagerPosition = position;
                if(position ==0){
                    previous.setVisibility(View.GONE);
                } else {
                    previous.setVisibility(View.VISIBLE);
                }
                if(position ==yearly.size()-1){
                    next.setVisibility(View.GONE);
                } else {
                    next.setVisibility(View.VISIBLE);
                }
            }

        };

        viewPager = findViewById(R.id.pager);

        spinner = findViewById(R.id.intervals_spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.intervals_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        mSessionViewModel.getFirstSession().observe(this, session -> {
            firstSession = session;
            //launchPagerAdapter();
            if(firstSession!=null){
                initIntervals();
                findViewById(R.id.no_data).setVisibility(View.GONE);
            } else {
                findViewById(R.id.no_data).setVisibility(View.VISIBLE);
            }

        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.stats_activity_title));
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();







    }




    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        String selection = (String)parent.getItemAtPosition(pos);
        switch(selection){
            case "overall":
                next.setVisibility(View.GONE);
                previous.setVisibility(View.GONE);
                unregisterCallback(currentIntervalType);
                launchPagerAdapter(0);
                currentIntervalType = 0;
                break;
            case "day":
                unregisterCallback(currentIntervalType);
                launchPagerAdapter(1);
                currentIntervalType = 1;
                break;
            case "week":
                Log.d(debug, "week item pressed");
                unregisterCallback(currentIntervalType);
                launchPagerAdapter(2);
                currentIntervalType = 2;
                break;
            case "month":
                Log.d(debug, "month item pressed");
                unregisterCallback(currentIntervalType);
                launchPagerAdapter(3);
                currentIntervalType = 3;
                break;
            case "year":
                unregisterCallback(currentIntervalType);
                launchPagerAdapter(4);
                currentIntervalType = 4;
                break;

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
        date.clear(Calendar.MINUTE);
        date.clear(Calendar.SECOND);
        date.clear(Calendar.MILLISECOND);
        yearly = new ArrayList<>();
        long intervalEnd = currentTime;
        long intervalStart = date.getTimeInMillis();
        while(intervalStart>firstSessionStart){
            checkIntervalForSessions(INTERVAL_YEAR, intervalStart,intervalEnd, false);
            //yearly.add(new TimeInterval(intervalStart,intervalEnd));
            date.add(Calendar.YEAR,-1);
            intervalEnd = intervalStart;
            intervalStart = date.getTimeInMillis();
        }
        checkIntervalForSessions(INTERVAL_YEAR, firstSessionStart,intervalEnd, true);
        //yearly.add(new TimeInterval(firstSessionStart,intervalEnd));

        monthly = new ArrayList<>();
        date = Calendar.getInstance();
        date.set(Calendar.DAY_OF_MONTH,1);
        date.set(Calendar.HOUR_OF_DAY,0);
        date.clear(Calendar.MINUTE);
        date.clear(Calendar.SECOND);
        date.clear(Calendar.MILLISECOND);
        intervalEnd = currentTime;
        intervalStart = date.getTimeInMillis();
        while(intervalStart>firstSessionStart){
            checkIntervalForSessions(INTERVAL_MONTH, intervalStart, intervalEnd, false);
            //monthly.add(new TimeInterval(intervalStart,intervalEnd));
            date.add(Calendar.MONTH,-1);
            intervalEnd = intervalStart;
            intervalStart = date.getTimeInMillis();
        }
        checkIntervalForSessions(INTERVAL_MONTH, firstSessionStart, intervalEnd, true);
        //monthly.add(new TimeInterval(firstSessionStart,intervalEnd));

        weekly = new ArrayList<>();
        date = Calendar.getInstance();
        int dayOfWeek = date.get(Calendar.DAY_OF_WEEK)-2;
        date.set(Calendar.DAY_OF_MONTH,(date.get(Calendar.DAY_OF_MONTH)-dayOfWeek));
        date.set(Calendar.HOUR_OF_DAY,0);
        date.clear(Calendar.MINUTE);
        date.clear(Calendar.SECOND);
        date.clear(Calendar.MILLISECOND);
        intervalEnd = currentTime;
        intervalStart = date.getTimeInMillis();
        while(intervalStart>firstSessionStart){
            checkIntervalForSessions(INTERVAL_WEEK, intervalStart, intervalEnd, false);
            //weekly.add(new TimeInterval(intervalStart,intervalEnd));
            intervalEnd = intervalStart;
            date.add(Calendar.WEEK_OF_YEAR,-1);
            intervalStart=date.getTimeInMillis();
        }
        checkIntervalForSessions(INTERVAL_WEEK, intervalStart, intervalEnd, true);
        //weekly.add(new TimeInterval(firstSessionStart,intervalEnd));

        daily = new ArrayList<>();
        date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY,0);
        date.clear(Calendar.MINUTE);
        date.clear(Calendar.SECOND);
        date.clear(Calendar.MILLISECOND);
        intervalEnd = currentTime;
        intervalStart = date.getTimeInMillis();
        while(intervalStart>firstSessionStart){
            checkIntervalForSessions(INTERVAL_DAY, intervalStart, intervalEnd, false);
            //daily.add(new TimeInterval(intervalStart,intervalEnd));
            intervalEnd = intervalStart;
            date.add(Calendar.DAY_OF_MONTH,-1);
            //intervalStart-=86400000;
            intervalStart=date.getTimeInMillis();
        }
        checkIntervalForSessions(INTERVAL_DAY, firstSessionStart, intervalEnd, true);
        //daily.add(new TimeInterval(firstSessionStart,intervalEnd));


        spinner.setOnItemSelectedListener(this);

        launchPagerAdapter(0);
    }



    public void checkIntervalForSessions(int type, long start, long end, boolean last){
        LiveData<Integer> hasSessionLD = mSessionViewModel.hasSessions(start, end);
        Observer <Integer> hasSessionsObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if(integer!=0){
                    switch(type){
                        case INTERVAL_DAY: {
                            daily.add(new TimeInterval(start,end));
                            if (last) {
                                Collections.sort(daily, comparator);
                            }
                        }   break;
                        case INTERVAL_WEEK:{
                            weekly.add(new TimeInterval(start,end));
                            if (last) {
                                //Log.d(debug, "sorting weekly arraylist");
                                Collections.sort(weekly, comparator);
                            } break;
                        }
                        case INTERVAL_MONTH:{
                            monthly.add(new TimeInterval(start,end));
                            if (last) {
                                Log.d(debug, "sorting monthly arraylist");
                                Collections.sort(monthly, comparator);
                            }
                        } break;
                        case INTERVAL_YEAR:{
                            yearly.add(new TimeInterval(start,end));
                            if (last) {
                                Collections.sort(yearly, comparator);
                            }
                        } break;
                        default: break;
                    }
                }

                hasSessionLD.removeObserver(this);
            }
        };
        hasSessionLD.observe(this, hasSessionsObserver);
    }

    public void launchPagerAdapter(int intervalType){
        viewPager.setVisibility(View.VISIBLE);
        switch(intervalType){
            case 0: {
                if(pagerOverallAdapter==null) pagerOverallAdapter = new ScreenSlidePagerOverallAdapter(this);
                viewPager.setAdapter(pagerOverallAdapter);
            } break;
            case 1: {
                if(pagerDailyAdapter==null) pagerDailyAdapter = new ScreenSlidePagerDailyAdapter(this);
                viewPager.setAdapter(pagerDailyAdapter);
                viewPager.registerOnPageChangeCallback(dailyOnPageChangeCallback);
                viewPager.setCurrentItem(daily.size()-1, false);
                viewPagerPosition = daily.size()-1;
                Log.d(debug, "daily size: "+daily.size());
            } break;
            case 2: {
                if(pagerWeeklyAdapter==null) pagerWeeklyAdapter = new ScreenSlidePagerWeeklyAdapter(this);
                viewPager.setAdapter(pagerWeeklyAdapter);
                viewPager.registerOnPageChangeCallback(weeklyOnPageChangeCallback);
                viewPager.setCurrentItem(weekly.size()-1, false);
                viewPagerPosition = weekly.size()-1;
            } break;
            case 3: {
                if(pagerMonthlyAdapter==null) pagerMonthlyAdapter = new ScreenSlidePagerMonthlyAdapter(this);
                viewPager.setAdapter(pagerMonthlyAdapter);
                viewPager.registerOnPageChangeCallback(monthlyOnPageChangeCallback);
                viewPager.setCurrentItem(monthly.size()-1, false);
                viewPagerPosition = monthly.size()-1;
            } break;
            case 4: {
                if(pagerYearlyAdapter==null) pagerYearlyAdapter = new ScreenSlidePagerYearlyAdapter(this);
                viewPager.setAdapter(pagerYearlyAdapter);
                viewPager.registerOnPageChangeCallback(yearlyOnPageChangeCallback);
                viewPager.setCurrentItem(yearly.size()-1, false);
                viewPagerPosition = yearly.size()-1;
            } break;

            default: break;
        }

        viewPager.setOffscreenPageLimit(2);

        //Log.d(debug, "session position: "+getSessionPosition(sessionStart));
        //viewPager.setCurrentItem(getSessionPosition(sessionStart), false);

    }

    public void unregisterCallback(int type){
        switch(type){
            case 1: viewPager.unregisterOnPageChangeCallback(dailyOnPageChangeCallback); break;
            case 2: viewPager.unregisterOnPageChangeCallback(weeklyOnPageChangeCallback); break;
            case 3: viewPager.unregisterOnPageChangeCallback(monthlyOnPageChangeCallback); break;
            case 4: viewPager.unregisterOnPageChangeCallback(yearlyOnPageChangeCallback); break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.timer) {
            Intent ih = new Intent(this, MainActivity.class);
            startActivity(ih);
        }
        else if (item.getItemId() == R.id.menu_help) {
            Intent ih = new Intent(this, ManualActivity.class);
            startActivity(ih);
        } else if (item.getItemId() == R.id.menu_settings_x) {
            Intent i = new Intent(this, PreferenceXActivity.class);
            startActivity(i);
        }
        else if (item.getItemId() == R.id.menu_session_list){
            Intent i = new Intent(this, CalendarActivity.class);
            startActivity(i);
        }
        else if (item.getItemId() == R.id.menu_feedback){
            Intent Email = new Intent(Intent.ACTION_SEND);
            Email.setType("text/email");
            Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "colormindapps@gmail.com" });
            Email.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
            startActivity(Intent.createChooser(Email, "Send Feedback:"));
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy(){
        unregisterCallback(currentIntervalType);
        super.onDestroy();
    }

    private class ScreenSlidePagerDailyAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerDailyAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return StatsFragment.newInstance(1, daily.get(daily.size()-position-1).getStart(),daily.get(daily.size()-position-1).getEnd());
        }

        @Override
        public int getItemCount() {
            return daily.size();
        }
    }

    private class ScreenSlidePagerWeeklyAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerWeeklyAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return StatsFragment.newInstance(2, weekly.get(weekly.size()-position-1).getStart(),weekly.get(weekly.size()-position-1).getEnd());
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

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return StatsFragment.newInstance(3, monthly.get(monthly.size()-position-1).getStart(),monthly.get(monthly.size()-position-1).getEnd());
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

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return StatsFragment.newInstance(4, yearly.get(yearly.size()-position-1).getStart(),yearly.get(yearly.size()-position-1).getEnd());
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

        @NonNull
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
