package com.colormindapps.rest_reminder_alarm.DB;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.InstrumentationRegistry;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.colormindapps.rest_reminder_alarm.RReminderRoomDatabase;
import com.colormindapps.rest_reminder_alarm.data.Session;
import com.colormindapps.rest_reminder_alarm.data.SessionDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DeleteOlder {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    private SessionDao sessionDao;
    private RReminderRoomDatabase db;

    @Before
    public void createDb(){
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(), RReminderRoomDatabase.class).allowMainThreadQueries().build();
        sessionDao = db.sessionDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void deleteOlderSessions() throws InterruptedException {
        sessionDao.deleteAll();
        long currentTime = Calendar.getInstance().getTimeInMillis();
        //one day old session
        long time1 = currentTime - 86400000L;
        long endTime1 = time1 + 45*60*1000;
        Session firstSession = new Session(0,time1,endTime1);
        sessionDao.insertSession(firstSession);

        //31 day old session
        long time2 = currentTime - 2592000000L - 86400000L;
        long endTime2 = time2 + (150*60*1000);
        Session secondSession = new Session(0,time2,endTime2);
        sessionDao.insertSession(secondSession);

        //third session record is a week old session
        long time3 = currentTime - 604800000L;
        long endTime3 = time3 + (300*60*1000);
        Session thirdSession = new Session(0,time3, endTime3);
        sessionDao.insertSession(thirdSession);

        //fourth record is a 37 days old session
        long time4 = currentTime - 2592000000L - 604800000L;
        long endTime4 = time4 + (55*60*1000);
        Session fourthSession = new Session(0,time4,endTime4);
        sessionDao.insertSession(fourthSession);

        List <Session> sessionList = LiveDataTestUtil.getValue(sessionDao.getAllSessions());

        assertEquals(4, LiveDataTestUtil.getValue(sessionDao.getAllSessions()).size());
        sessionDao.deleteOlder(currentTime);

        sessionList = LiveDataTestUtil.getValue(sessionDao.getAllSessions());

        assertEquals(2, LiveDataTestUtil.getValue(sessionDao.getAllSessions()).size());

       /* assertEquals(sessionList.get(0),firstSession);
        assertThat(sessionList.get(1).equals(secondSession));
        assertThat(sessionList.get(2).equals(firstSession));
        assertThat(sessionList.get(3).equals(fourthSession));

        */
    }
}
