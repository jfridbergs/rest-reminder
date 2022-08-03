package com.colormindapps.rest_reminder_alarm.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.colormindapps.rest_reminder_alarm.R;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.ArrayList;

public class ColumnGraphView extends View {


    private final Paint columnPaint;
    private final Paint whiteLinePaint;
    private final Point pieCenterPoint;
    private final Paint columnTextPaint;
    private final Paint columnTextExtendPaint;
    private final Paint columnTextExtendAmountPaint;
    private final RectF cirRect;
    private final RectF cirExtRect;
    private final RectF cirSelectedRect;
    private final Drawable dGears;
    private final Drawable dMug;

    private Typeface font;

    private int mViewWidth;
    private int mViewHeight;



    private final ArrayList<ColumnHelper> columnHelperList;


    public ColumnGraphView(Context context) {
        this(context, null);
    }

    public ColumnGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);


        columnHelperList = new ArrayList<>();
        Paint cirPaint = new Paint();
        cirPaint.setAntiAlias(true);
        cirPaint.setColor(Color.GRAY);
        Paint cirExtPaint = new Paint();
        cirExtPaint.setAntiAlias(true);
        cirExtPaint.setColor(Color.BLACK);
        cirExtPaint.setTextSize(RReminder.sp2px(getContext(), RReminder.isTablet(getContext())?24:15));
        cirExtPaint.setStrokeWidth(5);
        cirExtPaint.setTextAlign(Paint.Align.CENTER);
        cirExtPaint.setTypeface(font);
        columnPaint = new Paint();
        columnPaint.setAntiAlias(true);
        columnPaint.setColor(Color.GRAY);
        whiteLinePaint = new Paint(cirPaint);
        whiteLinePaint.setColor(Color.BLACK);
        whiteLinePaint.setStrokeWidth(2f);
        Paint whitePaint = new Paint();
        whitePaint.setAntiAlias(true);
        whitePaint.setColor(Color.WHITE);
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(RReminder.sp2px(getContext(), RReminder.isTablet(getContext())?24:15));
        textPaint.setStrokeWidth(5);
        textPaint.setTextAlign(Paint.Align.CENTER);

        font = Typeface.createFromAsset(getContext().getAssets(), "fonts/HelveticaNeueLTPro-Lt.otf");

        columnTextPaint = new Paint();
        columnTextPaint.setAntiAlias(true);
        columnTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.black_transparent));
        columnTextPaint.setTextSize(RReminder.sp2px(getContext(), RReminder.isTablet(getContext())?24:15));
        columnTextPaint.setStrokeWidth(5);
        columnTextPaint.setTextAlign(Paint.Align.CENTER);
        columnTextPaint.setTypeface(font);

        columnTextExtendPaint = new Paint();
        columnTextExtendPaint.setAntiAlias(true);
        columnTextExtendPaint.setColor(ContextCompat.getColor(getContext(), R.color.black));
        columnTextExtendPaint.setTextSize(RReminder.sp2px(getContext(), RReminder.isTablet(getContext())?24:15));
        columnTextExtendPaint.setStrokeWidth(7);
        columnTextExtendPaint.setTextAlign(Paint.Align.CENTER);
        columnTextExtendPaint.setTypeface(font);

        columnTextExtendAmountPaint = new Paint();
        columnTextExtendAmountPaint.setAntiAlias(true);
        columnTextExtendAmountPaint.setColor(ContextCompat.getColor(getContext(), R.color.black));
        columnTextExtendAmountPaint.setTextSize(RReminder.sp2px(getContext(), RReminder.isTablet(getContext())?17:12));
        columnTextExtendAmountPaint.setStrokeWidth(7);
        columnTextExtendAmountPaint.setTextAlign(Paint.Align.CENTER);
        columnTextExtendAmountPaint.setTypeface(font);



        pieCenterPoint = new Point();
        cirRect = new RectF();
        cirSelectedRect = new RectF();
        cirExtRect = new RectF();

        Paint paintGradientBlack = new Paint();
        paintGradientBlack.setStyle(Paint.Style.FILL);

        Paint paintGradientWork = new Paint();
        paintGradientWork.setStyle(Paint.Style.FILL);

        Paint paintGradientRest = new Paint();
        paintGradientRest.setStyle(Paint.Style.FILL);

        dGears = ContextCompat.getDrawable(context, R.drawable.img_gears);
        dMug = ContextCompat.getDrawable(context, R.drawable.img_coffee_mug);


    }


    public void setColumnData(ArrayList<ColumnHelper> helperList){
        columnHelperList.clear();
        if(helperList!=null && !helperList.isEmpty()){
            columnHelperList.addAll(helperList);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (columnHelperList.isEmpty()) {
            return;
        }
            int stepCount = 2*columnHelperList.size()+1;
            int xStepWidth = mViewWidth/stepCount;
            float baseLaneStartX = xStepWidth-toPx(45);
            float baseLaneEndX = (2*xStepWidth)+2*xStepWidth+toPx(45);
            canvas.drawLine(baseLaneStartX, mViewHeight-10, baseLaneEndX, mViewHeight-10, whiteLinePaint);
            float sizeKoef = 1.0f;
            String periodCount;
            String totalLength;
            String extendCount ;
            String extendTotalDuration;


            for (ColumnHelper helper : columnHelperList){
                if(helper.getPercent() >=50){
                    if(toPx(helper.getTargetHeight())>mViewHeight){
                        sizeKoef = mViewHeight/toPx(helper.getTargetHeight());
                        break;
                    }
                }

            }

            for(int i=0; i<columnHelperList.size();i++){
                ColumnHelper cHelper = columnHelperList.get(i);
                columnPaint.setColor(cHelper.getColor());
                //since we dont need to leave so much space between columns, we add ~50px (toPx(33)) on each side of column
                float x0 = (2*i*xStepWidth)+xStepWidth-toPx(33);
                float y0 = mViewHeight-toPx(cHelper.getTargetHeight())*sizeKoef;
                float x1 = (2*i*xStepWidth)+2*xStepWidth+toPx(33);
                canvas.drawRect(x0,y0,x1,mViewHeight-11,columnPaint);
                    float imgX0, imgX1, imgY0, imgY1;
                    imgX0 = x0+xStepWidth/2f+toPx(33)-toPx(30);
                    imgX1 = x0+xStepWidth/2f+toPx(33)+toPx(30);
                    if(cHelper.getPercent()<25){
                        imgY0 = y0-toPx(RReminder.isTablet(getContext())?208:148);
                        imgY1 = y0-toPx(RReminder.isTablet(getContext())?168:108);
                    } else {
                        imgY0 = y0+toPx(20);
                        imgY1 = y0+toPx(60);
                    }

                    if(i==0){
                        dGears.setBounds((int)imgX0, (int)imgY0, (int)imgX1, (int)imgY1);
                        dGears.draw(canvas);
                        //canvas.drawLine((int)(x0+xStepWidth/2f), (int)y0, (int)(x0+xStepWidth/2f), mViewHeight, whiteLinePaint);
                    } else {
                        float mugX0 = imgX0+toPx(12);
                        float mugX1 = imgX1-toPx(12);
                        dMug.setBounds((int)mugX0, (int)imgY0, (int)mugX1, (int)imgY1);
                        dMug.draw(canvas);
                    }
                    float textX0 = (imgX1-imgX0)/2+imgX0;
                    periodCount = ((cHelper.getCount())>1 ? cHelper.getCount()+ " "+getContext().getString(R.string.periods) : cHelper.getCount()+ " "+getContext().getString(R.string.period));
                    totalLength = RReminder.getShortDurationFromMillis(getContext(),cHelper.getTotalLength());
                    extendCount = ((cHelper.getExtendCount())>1 ? cHelper.getExtendCount()+" "+getContext().getString(R.string.extensions) : cHelper.getExtendCount()+" "+getContext().getString(R.string.extension));
                    extendTotalDuration = RReminder.getExtendMinsFromMillis(getContext(),cHelper.getTotalExtendDuration());

                    if(cHelper.getPercent()<25){
                        if(cHelper.getExtendCount()>0){
                            canvas.drawText(extendCount,textX0, y0-toPx(RReminder.isTablet(getContext())?60:40), columnTextExtendPaint);
                            canvas.drawText(extendTotalDuration,textX0, y0-toPx(RReminder.isTablet(getContext())?40:25), columnTextExtendAmountPaint);
                        }
                        canvas.drawText(periodCount,textX0, y0-toPx(RReminder.isTablet(getContext())?120:80), columnTextPaint);
                        canvas.drawText(totalLength,textX0, y0-toPx(RReminder.isTablet(getContext())?90:60), columnTextPaint);

                    } else {
                        canvas.drawText(periodCount,textX0, y0+toPx(RReminder.isTablet(getContext())?112:88), columnTextPaint);
                        canvas.drawText(totalLength,textX0, y0+toPx(RReminder.isTablet(getContext())?142:108), columnTextPaint);
                        if(cHelper.getExtendCount()>0){
                            canvas.drawText(extendCount,textX0, y0+toPx(RReminder.isTablet(getContext())?172:128), columnTextExtendPaint);
                            canvas.drawText(extendTotalDuration,textX0, y0+toPx(RReminder.isTablet(getContext())?195:143), columnTextExtendAmountPaint);
                        }
                    }


            }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mViewWidth = measureWidth(widthMeasureSpec);
        mViewHeight = measureHeight(heightMeasureSpec);
        int margin = mViewWidth / 16;
        int pieRadius = (mViewWidth) / 2 - margin;
        float pieExtRadius = pieRadius *0.81f;
        pieCenterPoint.set(pieRadius + margin, pieRadius + margin);
        cirRect.set(pieCenterPoint.x - pieRadius,
                pieCenterPoint.y - pieRadius,
                pieCenterPoint.x + pieRadius,
                pieCenterPoint.y + pieRadius);

        cirExtRect.set(pieCenterPoint.x - pieExtRadius,
                pieCenterPoint.y - pieExtRadius,
                pieCenterPoint.x + pieExtRadius,
                pieCenterPoint.y + pieExtRadius);
        cirSelectedRect.set(2, //minor margin for bigger circle
                2,
                mViewWidth - 2,
                mViewHeight - 2);


        setMeasuredDimension(mViewWidth, mViewHeight);
    }
    private float toPx(float dpi){
        return dpi * getResources().getDisplayMetrics().density;
    }

    public ArrayList<ColumnHelper> getColumnList(){return columnHelperList;}

    private int measureWidth(int measureSpec) {
        int preferred = 3;
        return getMeasurement(measureSpec, preferred);
    }

    private int measureHeight(int measureSpec) {
        int preferred = mViewWidth;
        return getMeasurement(measureSpec, preferred);
    }

    private int getMeasurement(int measureSpec, int preferred) {
        int specSize = MeasureSpec.getSize(measureSpec);
        int measurement = 0;

        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.EXACTLY:
                measurement = specSize;
                break;
            case MeasureSpec.AT_MOST:
                measurement = Math.min(preferred, specSize);
                break;
            default:
                measurement = preferred;
                break;
            case MeasureSpec.UNSPECIFIED:
                break;
        }
        return measurement;
    }

}