package com.colormindapps.rest_reminder_alarm.charts;


import com.colormindapps.rest_reminder_alarm.data.Period;

public class PieHelper {
    private float startDegree;
    private float endDegree;
    private float targetStartDegree;
    private float targetEndDegree;
    private float middleDegree;
    private final String title;
    private float sweepDegree;
    private boolean isAnimated = false;
    private boolean drawAll = false;
    private final Period period;

    int velocity = 2;

    public PieHelper(float percent, Period period){
        this(percent, period, null);
    }

    PieHelper(float percent, Period period, String title){
        this.sweepDegree = percent * 300;
        this.period = period;
        this.title = title;
    }


    PieHelper(float startDegree, float endDegree, PieHelper targetPie){
        this.startDegree = startDegree;
        this.endDegree = endDegree;
        targetStartDegree = targetPie.getStartDegree();
        targetEndDegree = targetPie.getEndDegree();
        this.middleDegree = (targetEndDegree - startDegree)/2 + startDegree;
        this.sweepDegree = targetPie.getSweep();
        this.title = targetPie.getTitle();
        this.period = targetPie.getPeriod();
    }



    void setDegree(float startDegree, float endDegree){
        this.startDegree = startDegree;
        this.endDegree = endDegree;
    }

    void setAnimateStatus(boolean status){
        this.isAnimated = status;
    }


    void setDrawAllStatus(boolean status){
        this.drawAll = status;
    }

    boolean isAnimated(){
        return this.isAnimated;
    }

    boolean isDrawAll(){
        return this.drawAll;
    }

    boolean isAtRest(){
        return (startDegree==targetStartDegree)&&(endDegree==targetEndDegree);
    }

    boolean isHidden(){return endDegree==startDegree;}

    void update(){
        this.startDegree = updateSelf(startDegree, targetStartDegree, velocity);
        this.endDegree = updateSelf(endDegree, targetEndDegree, velocity);
        this.sweepDegree = endDegree - startDegree;

    }

    void updateHide(){
        this.startDegree = updateSelf(startDegree, targetStartDegree, velocity);
        this.endDegree = updateSelf(endDegree, targetStartDegree, velocity);
        this.sweepDegree = endDegree - startDegree;
    }


    public Period getPeriod(){
        return period;
    }

    public int getPeriodType(){
        return period.getType();
    }

    public long getPeriodDuration(){
        return period.getDuration();
    }

    public long getPeriodStart(){
        return period.getStartTime();
    }

    public int getPeriodExtendCount(){return period.getExtendCount();}

    public String getTitle(){
        return title;
    }

    public float getMiddleDegree(){
        return middleDegree;
    }

    public float getSweep(){
        return sweepDegree;
    }

    public float getStartDegree(){
        return startDegree;
    }

    public float getEndDegree(){
        return endDegree;
    }

    private float updateSelf(float origin, float target, int velocity){
        if (origin < target) {
            origin += velocity;
        } else if (origin > target){
            origin-= velocity;
        }
        if(Math.abs(target-origin)<velocity){
            origin = target;
        }
        return origin;
    }
}
