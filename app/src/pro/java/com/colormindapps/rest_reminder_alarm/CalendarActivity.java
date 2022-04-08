package com.colormindapps.rest_reminder_alarm;

import android.content.Intent;
import android.os.Bundle;

import com.colormindapps.rest_reminder_alarm.shared.RReminder;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.colormindapps.rest_reminder_alarm.databinding.ActivityCalendarBinding;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;

public class CalendarActivity extends AppCompatActivity implements OnMonthChangedListener, OnDateSelectedListener {
    MaterialCalendarView calendarView;

    private EventDecorator eventDecorator;
    private SessionsViewModel mSessionsViewModel;
    int yearValue, monthValue;
    HashSet<Integer> hSetNumbers;
    private ArrayList<Integer> shownMonths;
    private String debug = "RR_CALENDAR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calendar);
        Toolbar toolbar = findViewById(R.id.calendar_toolbar);
        setSupportActionBar(toolbar);
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

    }

    public void decorateMonth(int year, int month){
        Log.d(debug, "decorateMonth, value: "+month);
        Calendar mycal = new GregorianCalendar(year, month, 1);
        int daysInMonth = mycal.getActualMaximum(Calendar.DAY_OF_MONTH);
        long periodFrom = RReminder.getMillisFromDate(year,month,1,true);
        long periodTo = RReminder.getMillisFromDate(year,month,daysInMonth,false);


        mSessionsViewModel.getSessionsInPeriod(periodFrom, periodTo).observe(this, new Observer<List<Session>>(){
            @Override
            public void onChanged(@Nullable final List<Session> sessions){
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
        Log.d(debug, "arraylistDates size: "+dates.size());
        eventDecorator = new EventDecorator(getApplicationContext(),dates);
        calendarView.addDecorator(eventDecorator);
        calendarView.invalidateDecorators();
    }

    @Override
    public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
        Log.d(debug, "onMonthChanged");
        //noinspection ConstantConditions
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        getSupportActionBar().setTitle( df.format("MMMM, yyyy", date.getDate()));
        int month = date.getMonth()-1;
        int shownValue = date.getYear()*100+month;
        if(!shownMonths.contains(shownValue)){
            Log.d(debug, "new month/year, add more decorations");
            decorateMonth(date.getYear(),month);
            shownMonths.add(shownValue);
        } else {
            Log.d(debug, "month/year was already shown, dont decorate");
        }
    }


    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        int day = date.getDay();
        Log.d(debug, "day value: "+day);
        Log.d(debug, "hashset size: "+hSetNumbers.size());
            Log.d(debug, "day value is in hashset");
            Intent intent = new Intent(this,SessionsListActivity.class);
            intent.putExtra(RReminder.YEAR, date.getYear());
            intent.putExtra(RReminder.MONTH, date.getMonth());
            intent.putExtra(RReminder.DAY, day);
            startActivity(intent);
    }
}