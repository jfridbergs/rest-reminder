package com.colormindapps.rest_reminder_alarm.charts;

import android.util.Log;

public class ColumnHelper {
    private float height;
    private float targetHeight;
    private int color;
    private int count;
    private int extendCount;
    private long totalLength;
    private boolean isAnimated = false;
    private String debug="PIE_HELPER";

    int velocity = 25;


    public ColumnHelper(float percent){
        this(percent, 0, 0, 0l,0);
    }

    public ColumnHelper(float percent,  int color, int count, long totalLength, int extendCount){
        this.targetHeight = percent * 1000 / 100;
        this.color = color;
        this.count = count;
        this.extendCount = extendCount;
        this.totalLength = totalLength;
    }


    ColumnHelper(float endHeight, ColumnHelper targetColumn){
        this.height = endHeight;
        this.targetHeight = targetColumn.getEndHeight();
        this.color = targetColumn.getColor();
        this.count = targetColumn.count;
        this.extendCount = targetColumn.extendCount;
        this.totalLength = targetColumn.totalLength;
    }

    ColumnHelper setTarget(ColumnHelper targetColumn){
        this.height = targetColumn.getEndHeight();
        this.color = targetColumn.getColor();
        return this;
    }

    void setHeight( float height){
        this.height = height;
    }

    void setAnimateStatus(boolean status){
        this.isAnimated = status;
    }


    boolean isAnimated(){
        return this.isAnimated;
    }


    boolean isColorSetted(){return color != 0;}

    boolean isAtRest(){
        return (height==targetHeight);
    }

    boolean isHidden(){return height==0;}

    void update(){
        //Log.d(debug, "BEFORE Start degree: " + this.startDegree + ", startTargetDegree: "+ this.targetStartDegree +", end degree "+this.endDegree+", targetEndDegree: "+this.targetEndDegree);
        this.height = updateSelf(height, targetHeight, velocity);
        // Log.d(debug, "AFTER Start degree: " + this.startDegree + ", startTargetDegree: "+ this.targetStartDegree +", end degree "+this.endDegree+", targetEndDegree: "+this.targetEndDegree);

    }

    void updateHide(){
        //Log.d(debug, "BEFORE Start degree: " + this.startDegree + ", startTargetDegree: "+ this.targetStartDegree +", end degree "+this.endDegree+", targetEndDegree: "+this.targetEndDegree);
        this.height = updateSelf(height, 0, velocity);
        //Log.d(debug, "AFTER Start degree: " + this.startDegree + ", startTargetDegree: "+ this.targetStartDegree +", end degree "+this.endDegree+", targetEndDegree: "+this.targetEndDegree);
    }

    String getPercentStr(){
        float percent = height / 500 * 100;
        return String.valueOf((int)percent) + "%";
    }

    public int getColor(){ return color; }

    public int getCount(){
        return count;
    }

    public long getTotalLength(){ return totalLength;}

    public int getExtendCount(){return extendCount;
    }

    public float getEndHeight(){
        return height;
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

