package com.colormindapps.rest_reminder_alarm;

import android.content.Intent;
import android.os.Bundle;

import com.colormindapps.rest_reminder_alarm.data.Session;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;

public class CalendarActivity extends AppCompatActivity implements OnMonthChangedListener, OnDateSelectedListener, NavigationView.OnNavigationItemSelectedListener {
    MaterialCalendarView calendarView;

    private SessionsViewModel mSessionsViewModel;
    int yearValue, monthValue;
    HashSet<Integer> hSetNumbers;
    private ArrayList<Integer> shownMonths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calendar);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        calendarView = findViewById(R.id.calendarView);

        Calendar currentDate = Calendar.getInstance();
        yearValue = currentDate.get(Calendar.YEAR);
        monthValue = currentDate.get(Calendar.MONTH);
        Calendar mycal = new GregorianCalendar(yearValue, monthValue, 1);
        int daysInMonth = mycal.getActualMaximum(Calendar.DAY_OF_MONTH);

        shownMonths = new ArrayList<>();
        shownMonths.add(yearValue*100+monthValue);

        mSessionsViewModel = new ViewModelProvider(this).get(SessionsViewModel.class);

        mSessionsViewModel.getFirstSession().observe(this, new Observer<Session>(){
            @Override
            public void onChanged(Session session) {
                if(session!=null){
                    Calendar firstSessionData = Calendar.getInstance();
                    firstSessionData.setTimeInMillis(session.getSessionStart());
                    firstSessionData.set(Calendar.DAY_OF_MONTH,1);
                    Calendar maxDate = Calendar.getInstance();
                    maxDate.set(Calendar.DAY_OF_MONTH,daysInMonth);
                    calendarView.state().edit().setMinimumDate(firstSessionData).setMaximumDate(maxDate).commit();
                    mSessionsViewModel.getFirstSession().removeObserver(this);
                }

            }
        });


        calendarView.setOnMonthChangedListener(this);
        calendarView.setOnDateChangedListener(this);

        decorateMonth(yearValue, monthValue);

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

    public void decorateMonth(int year, int month){
        Calendar mycal = new GregorianCalendar(year, month, 1);
        int daysInMonth = mycal.getActualMaximum(Calendar.DAY_OF_MONTH);
        long periodFrom = RReminder.getMillisFromDate(year,month,1,true);
        long periodTo = RReminder.getMillisFromDate(year,month,daysInMonth,false);


        mSessionsViewModel.getSessionsInPeriod(periodFrom, periodTo).observe(this, new Observer<List<Session>>(){
            @Override
            public void onChanged(@Nullable final List<Session> sessions){
                assert sessions != null;
                decorateCalendar(sessions, year, month);
                mSessionsViewModel.getSessionsInPeriod(periodFrom, periodTo).removeObserver(this);
            }
        });
    }

    public void decorateCalendar(List<Session> sessions, int year, int month){
        ArrayList<Integer> daysArray= new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        for (Session session : sessions){
            long from = session.getSessionStart();
            calendar.setTimeInMillis(from);
            daysArray.add(calendar.get(Calendar.DAY_OF_MONTH));
        }
        hSetNumbers = new HashSet(daysArray);
        ArrayList<CalendarDay> dates = new ArrayList<>();
        for (int day : hSetNumbers){
            dates.add(CalendarDay.from(year, month,day));
        }
        EventDecorator eventDecorator = new EventDecorator(getApplicationContext(), dates);
        calendarView.addDecorator(eventDecorator);
        calendarView.invalidateDecorators();
    }

    @Override
    public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
        int month = date.getMonth();
        int shownValue = date.getYear()*100+month;

            decorateMonth(date.getYear(),month);
            shownMonths.add(shownValue);

    }


    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        int day = date.getDay();
        long periodFrom = RReminder.getMillisFromDate( date.getYear(),date.getMonth(),day,true);
        long periodTo = RReminder.getMillisFromDate(date.getYear(),date.getMonth(),day,false);

        mSessionsViewModel.getSessionsInPeriodASC(periodFrom, periodTo).observe(this, sessions -> {
            Intent intent = new Intent(getApplicationContext(), SessionDetailsViewActivity.class);
            if(sessions.size()>0){
                intent.putExtra(RReminder. DB_SESSION_START, sessions.get(0).getSessionStart());
                intent.putExtra(RReminder. DB_SESSION_END, sessions.get(0).getSessionEnd());
                startActivity(intent);
            }


        });
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
}