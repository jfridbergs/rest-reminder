package com.colormindapps.rest_reminder_alarm.charts;


public class ColumnHelper {
    private float height;
    private final float targetHeight;
    private final int color;
    private final float percent;
    private final int count;
    private final int extendCount;
    private final long totalLength;
    private final long totalExtendDuration;

    int velocity = 25;



    public ColumnHelper(float percent,  int color, int count, long totalLength, int extendCount, long totalExtendDuration){
        this.percent = percent;
        this.targetHeight = percent * 1000 / 100;
        this.color = color;
        this.count = count;
        this.extendCount = extendCount;
        this.totalLength = totalLength;
        this.totalExtendDuration = totalExtendDuration;
    }



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


    public int getColor(){ return color; }

    public int getCount(){
        return count;
    }

    public float getPercent(){
        return percent;
    }

    public long getTotalLength(){ return totalLength;}

    public long getTotalExtendDuration(){ return totalExtendDuration;}

    public int getExtendCount(){return extendCount;
    }

    public float getEndHeight(){
        return height;
    }

    public float getTargetHeight(){return this.targetHeight;}


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

