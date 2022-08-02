package com.colormindapps.rest_reminder_alarm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.colormindapps.rest_reminder_alarm.data.Session;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;
import com.google.android.material.navigation.NavigationView;

import java.util.List;


public class SessionDetailsViewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private long sessionStart;
    private int viewPagerPosition;
    private List<Session> mSessions;
    private ImageView previous, next;

    private ViewPager2 viewPager;
    private ViewPager2.OnPageChangeCallback onPageChangeCallback;


    private final String debug = "SESSION_DETAILS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_details_view_activity);
        sessionStart = getIntent().getLongExtra(RReminder.DB_SESSION_START, 0L);
        long sessionEnd = getIntent().getLongExtra(RReminder.DB_SESSION_END, 0L);

        SessionsViewModel mSessionViewModel = new ViewModelProvider(this).get(SessionsViewModel.class);

        previous = findViewById(R.id.previous);
        previous.setOnClickListener(view -> viewPager.setCurrentItem(viewPagerPosition-1, true));
        next = findViewById(R.id.next);
        next.setOnClickListener(view -> viewPager.setCurrentItem(viewPagerPosition+1, true));

        viewPager = findViewById(R.id.pager);
        Log.d(debug, "session start: "+ sessionStart);
        Log.d(debug, "session end: "+ sessionEnd);

        onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                viewPagerPosition = position;
                if(position ==0){
                    previous.setVisibility(View.GONE);
                } else {
                    previous.setVisibility(View.VISIBLE);
                }
                if(position ==mSessions.size()-1){
                    next.setVisibility(View.GONE);
                } else {
                    next.setVisibility(View.VISIBLE);
                }
            }
        };

        viewPager.registerOnPageChangeCallback(onPageChangeCallback);

        mSessionViewModel.getAllSessionsPieView().observe(this, sessions -> {
            mSessions = sessions;
            launchPagerAdapter();

        });




        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.sessions_title);
        setSupportActionBar(toolbar);

        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

    }

    public void launchPagerAdapter(){
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(1);
        Log.d(debug, "session position: "+getSessionPosition(sessionStart));
        viewPager.setCurrentItem(getSessionPosition(sessionStart), false);
        viewPagerPosition = getSessionPosition(sessionStart);
        if(getSessionPosition(sessionStart) ==0){
            previous.setVisibility(View.GONE);
        } else {
            previous.setVisibility(View.VISIBLE);
        }
        if(getSessionPosition(sessionStart)==mSessions.size()-1){
            next.setVisibility(View.GONE);
        } else {
            next.setVisibility(View.VISIBLE);
        }

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

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return SessionDetailsViewFragment.newInstance(mSessions.get(position).getSessionStart(),mSessions.get(position).getSessionEnd());
        }

        @Override
        public int getItemCount() {
            return mSessions.size();
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
        else if (item.getItemId() == R.id.menu_open_stats){
            Intent i = new Intent(this, StatsActivity.class);
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
        viewPager.unregisterOnPageChangeCallback(onPageChangeCallback);
        super.onDestroy();
    }





}
