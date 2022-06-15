package com.colormindapps.rest_reminder_alarm.charts;

import android.util.Log;

import com.colormindapps.rest_reminder_alarm.data.Period;

public class PieHelper {
    private float startDegree;
    private float endDegree;
    private float targetStartDegree;
    private float targetEndDegree;
    private float middleDegree;
    private String title;
    private float sweepDegree;
    private float extendStartDegree= 0;
    private boolean isAnimated = false;
    private boolean extended = false;
    private boolean drawAll = false;
    private boolean drawExtended = false;
    private String debug="PIE_HELPER";
    private Period period;

    int velocity = 5;

    public PieHelper (float percent){
        this(percent, null, null);
    }
    public PieHelper(float percent, Period period){
        this(percent, period, null);
    }

    PieHelper(float percent, Period period, String title){
        this.sweepDegree = percent * 300;
        this.period = period;
        this.title = title;
       // Log.d(debug, "period type: "+period.getType());
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
        if(this.period.getType()==3 || this.period.getType()==4){
            this.extended = true;
            long actualDuration = this.period.getDuration();
            long initialDuration = this.period.getInitialDuration();
            float extendFraction = (float)initialDuration /actualDuration;
            this.extendStartDegree = this.startDegree + (this.endDegree - this.startDegree)*extendFraction;
        }
        //Log.d(debug, "extendStartDegree: "+this.extendStartDegree+", startDegree: "+startDegree+", endDegree: "+endDegree);


    }

    PieHelper setTarget(PieHelper targetPie){
        this.targetStartDegree = targetPie.getStartDegree();
        this.targetEndDegree = targetPie.getEndDegree();
        this.title = targetPie.getTitle();
        this.sweepDegree = targetPie.getSweep();
        return this;
    }

    void setDegree(float startDegree, float endDegree){
        this.startDegree = startDegree;
        this.endDegree = endDegree;
    }

    void setAnimateStatus(boolean status){
        this.isAnimated = status;
    }

    void setDrawExtended(boolean status){
        this.drawExtended = status;
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

    boolean isExtended(){return this.extended;}

    boolean drawExtended(){return drawExtended;}


    boolean isAtRest(){
        return (startDegree==targetStartDegree)&&(endDegree==targetEndDegree);
    }

    boolean isHidden(){return endDegree==startDegree;}

    void update(){
        //Log.d(debug, "BEFORE Start degree: " + this.startDegree + ", startTargetDegree: "+ this.targetStartDegree +", end degree "+this.endDegree+", targetEndDegree: "+this.targetEndDegree);
        this.startDegree = updateSelf(startDegree, targetStartDegree, velocity);
        this.endDegree = updateSelf(endDegree, targetEndDegree, velocity);
        this.sweepDegree = endDegree - startDegree;
       // Log.d(debug, "UPDATE endDegree: "+this.endDegree);
       // Log.d(debug, "UPDATE extendStartDegree: "+this.extendStartDegree);
        if(this.extended && this.endDegree>this.extendStartDegree){
           // Log.d(debug, "START_DRAWING_EXT");
            drawExtended = true;
        }
         //Log.d(debug, "AFTER Start degree: " + this.startDegree + ", startTargetDegree: "+ this.targetStartDegree +", end degree "+this.endDegree+", targetEndDegree: "+this.targetEndDegree);

    }

    void updateHide(){
        //Log.d(debug, "BEFORE Start degree: " + this.startDegree + ", startTargetDegree: "+ this.targetStartDegree +", end degree "+this.endDegree+", targetEndDegree: "+this.targetEndDegree);
        this.startDegree = updateSelf(startDegree, targetStartDegree, velocity);
        this.endDegree = updateSelf(endDegree, targetStartDegree, velocity);
        this.sweepDegree = endDegree - startDegree;
        //Log.d(debug, "AFTER Start degree: " + this.startDegree + ", startTargetDegree: "+ this.targetStartDegree +", end degree "+this.endDegree+", targetEndDegree: "+this.targetEndDegree);
    }

    String getPercentStr(){
        float percent = sweepDegree / 300 * 100;
        return String.valueOf((int)percent) + "%";
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

    public float getExtendStartDegree(){return this.extendStartDegree;}

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
