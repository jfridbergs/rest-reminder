package com.colormindapps.rest_reminder_alarm.charts;

import android.util.Log;

public class PieHelper {
    private float startDegree;
    private float endDegree;
    private float targetStartDegree;
    private float targetEndDegree;
    private String title;
    private int color;
    private float sweepDegree;
    private boolean isAnimated = false;
    private boolean drawAll = false;
    private String debug="PIE_HELPER";

    int velocity = 5;


    public PieHelper(float percent){
        this(percent, null, 0);
    }

    public PieHelper(float percent, int color){
        this(percent, null, color);
    }


    PieHelper(float percent, String title){
        this(percent, title, 0);
    }


    PieHelper(float percent, String title, int color){
        this.sweepDegree = percent * 300 / 100;
        this.title = title;
        this.color = color;
    }


    PieHelper(float startDegree, float endDegree, PieHelper targetPie){
        this.startDegree = startDegree;
        this.endDegree = endDegree;
        targetStartDegree = targetPie.getStartDegree();
        targetEndDegree = targetPie.getEndDegree();
        this.sweepDegree = targetPie.getSweep();
        this.title = targetPie.getTitle();
        this.color = targetPie.getColor();
    }

    PieHelper setTarget(PieHelper targetPie){
        this.targetStartDegree = targetPie.getStartDegree();
        this.targetEndDegree = targetPie.getEndDegree();
        this.title = targetPie.getTitle();
        this.color = targetPie.getColor();
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

    void setDrawAllStatus(boolean status){
        this.drawAll = status;
    }

    boolean isAnimated(){
        return this.isAnimated;
    }

    boolean isDrawAll(){
        return this.drawAll;
    }

    boolean isColorSetted(){return color != 0;}

    boolean isAtRest(){
        return (startDegree==targetStartDegree)&&(endDegree==targetEndDegree);
    }

    boolean isHidden(){return endDegree==startDegree;}

    void update(){
        //Log.d(debug, "BEFORE Start degree: " + this.startDegree + ", startTargetDegree: "+ this.targetStartDegree +", end degree "+this.endDegree+", targetEndDegree: "+this.targetEndDegree);
        this.startDegree = updateSelf(startDegree, targetStartDegree, velocity);
        this.endDegree = updateSelf(endDegree, targetEndDegree, velocity);
        this.sweepDegree = endDegree - startDegree;
        // Log.d(debug, "AFTER Start degree: " + this.startDegree + ", startTargetDegree: "+ this.targetStartDegree +", end degree "+this.endDegree+", targetEndDegree: "+this.targetEndDegree);

    }

    void updateHide(){
        Log.d(debug, "BEFORE Start degree: " + this.startDegree + ", startTargetDegree: "+ this.targetStartDegree +", end degree "+this.endDegree+", targetEndDegree: "+this.targetEndDegree);
        this.startDegree = updateSelf(startDegree, targetStartDegree, velocity);
        this.endDegree = updateSelf(endDegree, targetStartDegree, velocity);
        this.sweepDegree = endDegree - startDegree;
        Log.d(debug, "AFTER Start degree: " + this.startDegree + ", startTargetDegree: "+ this.targetStartDegree +", end degree "+this.endDegree+", targetEndDegree: "+this.targetEndDegree);
    }

    String getPercentStr(){
        float percent = sweepDegree / 300 * 100;
        return String.valueOf((int)percent) + "%";
    }

    public int getColor(){ return color; }

    public String getTitle(){
        return title;
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
