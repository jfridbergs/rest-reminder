package com.colormindapps.rest_reminder_alarm.wear;


import android.support.test.espresso.IdlingResource;

/**
 * Created by ingressus on 22/02/2017.
 */

public class SleepIdlingResource implements IdlingResource {

    private final long startTime;
    private final long waitingTime;
    private ResourceCallback resourceCallback;

    public SleepIdlingResource(long waitingTime){
        this.startTime = System.currentTimeMillis();
        this.waitingTime = waitingTime;
    }

    @Override
    public String getName(){
        return SleepIdlingResource.class.getName() + ":" + waitingTime;
    }

    @Override
    public boolean isIdleNow(){
        long elapsed = System.currentTimeMillis() - startTime;
        boolean idle = (elapsed >= waitingTime);
        if(idle){
            resourceCallback.onTransitionToIdle();
        }
        return idle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback){
        this.resourceCallback = resourceCallback;
    }

}
