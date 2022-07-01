package com.colormindapps.rest_reminder_alarm;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.colormindapps.rest_reminder_alarm.data.Period;
import com.colormindapps.rest_reminder_alarm.data.Session;
import com.colormindapps.rest_reminder_alarm.data.TimeInterval;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class StatsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {
    private long sessionStart, sessionEnd;
    boolean showingBack = false;
    private PeriodViewModel mPeriodViewModel;
    private SessionsViewModel mSessionViewModel;
    private List<Period> mPeriods;
    private List<Session> mSessions;
    private Session firstSession;
    private Spinner spinner;
    private Typeface titleFont;
    private List<TimeInterval> yearly, monthly, weekly, daily;

    private ImageView previous, next;

    private int currentIntervalType = 0;

    private ViewPager2 viewPager;
    private ViewPager2.OnPageChangeCallback dailyOnPageChangeCallback, weeklyOnPageChangeCallback, monthlyOnPageChangeCallback, yearlyOnPageChangeCallback;

    private FragmentStateAdapter pagerDailyAdapter, pagerWeeklyAdapter, pagerMonthlyAdapter, pagerYearlyAdapter, pagerOverallAdapter;


    private String debug = "SESSION_DETAILS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_activity);

        titleFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-ThCn.otf");

        mPeriodViewModel = new ViewModelProvider(this).get(PeriodViewModel.class);
        mSessionViewModel = new ViewModelProvider(this).get(SessionsViewModel.class);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);

        dailyOnPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
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

        mSessionViewModel.getFirstSession().observe(this, new Observer<Session>(){
            @Override
            public void onChanged(@Nullable final Session session){
                firstSession = session;
                //launchPagerAdapter();
                if(firstSession!=null){
                    initIntervals();
                    findViewById(R.id.no_data).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.no_data).setVisibility(View.VISIBLE);
                }

            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.stats_activity_title));
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
                unregisterCallback(currentIntervalType);
                launchPagerAdapter(2);
                currentIntervalType = 2;
                break;
            case "month":
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

        daily = new ArrayList<TimeInterval>();
        date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY,0);
        date.set(Calendar.MINUTE,0);
        date.set(Calendar.SECOND,0);
        intervalEnd = currentTime;
        intervalStart = date.getTimeInMillis();
        while(intervalStart>firstSessionStart){
            daily.add(new TimeInterval(intervalStart,intervalEnd));
            intervalEnd = intervalStart;
            intervalStart-=86400000;
        }

        daily.add(new TimeInterval(firstSessionStart,intervalEnd));


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
                if(pagerDailyAdapter==null) pagerDailyAdapter = new ScreenSlidePagerDailyAdapter(this);
                viewPager.setAdapter(pagerDailyAdapter);
                viewPager.registerOnPageChangeCallback(dailyOnPageChangeCallback);
                viewPager.setCurrentItem(daily.size(), false);
            } break;
            case 2: {
                if(pagerWeeklyAdapter==null) pagerWeeklyAdapter = new ScreenSlidePagerWeeklyAdapter(this);
                viewPager.setAdapter(pagerWeeklyAdapter);
                viewPager.registerOnPageChangeCallback(weeklyOnPageChangeCallback);
                viewPager.setCurrentItem(weekly.size(), false);
            } break;
            case 3: {
                if(pagerMonthlyAdapter==null) pagerMonthlyAdapter = new ScreenSlidePagerMonthlyAdapter(this);
                viewPager.setAdapter(pagerMonthlyAdapter);
                viewPager.registerOnPageChangeCallback(monthlyOnPageChangeCallback);
                viewPager.setCurrentItem(monthly.size(), false);
            } break;
            case 4: {
                if(pagerYearlyAdapter==null) pagerYearlyAdapter = new ScreenSlidePagerYearlyAdapter(this);
                viewPager.setAdapter(pagerYearlyAdapter);
                viewPager.registerOnPageChangeCallback(yearlyOnPageChangeCallback);
                viewPager.setCurrentItem(yearly.size(), false);
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
