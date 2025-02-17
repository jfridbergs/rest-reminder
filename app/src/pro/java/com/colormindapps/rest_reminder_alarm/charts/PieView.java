package com.colormindapps.rest_reminder_alarm.charts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.colormindapps.rest_reminder_alarm.R;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.ArrayList;
import java.util.Random;

public class PieView extends View {



    private final Paint cirPaint;
    private final Paint cirExtPaint;
    private final Paint columnPaint;
    private final Paint whiteLinePaint;
    private final Point pieCenterPoint;
    private final Paint whitePaint;
    private final Paint columnTextPaint;
    private final Paint columnTextExtendPaint;
    private final Paint columnTextExtendAmountPaint;
    private Paint endEdgePaint;
    private final RectF cirRect;
    private final RectF cirExtRect;
    private final RectF cirSelectedRect;
    private final Paint paintGradientBlack;
    private final Paint paintGradientWork;
    private final Paint paintGradientRest;
    private RadialGradient radialGradientBlack, radialGradientWork, radialGradientRest;
    private final Drawable dGears;
    private final Drawable dMug;

    private Typeface font;

    private int mViewWidth;
    private int mViewHeight;
    private int pieRadius;
    private int colorWork, colorRest, colorWorkHighlight, colorRestHighlight;
    private boolean booleanDrawDonut = true;

    private float baseLaneLength = 0;
    private float baseLaneTargetLength;


    private final ArrayList<PieHelper> pieHelperList;
    private final ArrayList<ColumnHelper> columnHelperList;
    private int selectedIndex = NO_SELECTED_INDEX;

    public static final int NO_SELECTED_INDEX = -999;


    private final Runnable drawDonut = new Runnable() {
        @Override
        public void run() {
            boolean needNewFrame;
            int size = pieHelperList.size();
            for(int i=0;i<size;i++){
                if(pieHelperList.get(i).isAnimated()){
                    PieHelper pie = pieHelperList.get(i);
                    //Log.d(debug, "Animate sector nr. "+i);
                    pie.update();
                    if (!pie.isAtRest()) {
                        needNewFrame = true;
                    } else {
                        pie.setAnimateStatus(false);
                        pie.setDrawAllStatus(true);
                        if(i<size-1){
                            pieHelperList.get(i+1).setAnimateStatus(true);
                            needNewFrame = true;
                        } else {
                            needNewFrame = false;
                        }

                    }
                    if (needNewFrame) {
                        postDelayed(this, 7);
                    } else {
                        pieHelperList.get(size-1).setDrawAllStatus(true);
                        //Log.d(debug, "SELECT_RANDOM");
                        Random random = new Random();
                        selectedIndex = random.nextInt(size);
                    }
                }
            }


            invalidate();
        }
    };

    private final Runnable drawBaseLane = new Runnable(){
        @Override
        public void run() {
            baseLaneLength+=50;
            if(baseLaneLength<baseLaneTargetLength){
                postDelayed(this, 7);
            } else {
                removeCallbacks(drawColumns);
                post(drawColumns);

                //start runnable for drawing columns
            }
            invalidate();
        }
    };

    private final Runnable hideBaseLine = new Runnable(){
        @Override
        public void run() {
            baseLaneLength-=50;
            if(baseLaneLength>0){
                postDelayed(this, 7);
            } else {
                removeCallbacks(drawDonut);
                booleanDrawDonut=true;
                pieHelperList.get(0).setAnimateStatus(true);
                post(drawDonut);
            }
            invalidate();
        }
    };

    private final Runnable drawColumns = new Runnable() {
        @Override
        public void run() {
            boolean needNewFrame = false;
            for (ColumnHelper column : columnHelperList){
                column.update();
                if(!column.isAtRest()){
                    needNewFrame = true;
                }
            }
            if(needNewFrame){
                postDelayed(this,7);
            }
            invalidate();
        }
    };

    private final Runnable columnHide = new Runnable() {
        @Override
        public void run() {
            boolean needNewFrame = false;
            for (ColumnHelper column : columnHelperList){
                column.updateHide();
                if(!column.isHidden()){
                    needNewFrame = true;
                }
            }
            if(needNewFrame){
                postDelayed(this,7);
            } else {
                removeCallbacks(hideBaseLine);
                post(hideBaseLine);
            }
            invalidate();
        }
    };

    private final Runnable donutHide = new Runnable() {
        @Override
        public void run() {
            boolean needNewFrame = false;
            for(int i=pieHelperList.size()-1;i>=0;i--){
                if(pieHelperList.get(i).isAnimated()){
                    PieHelper pie = pieHelperList.get(i);
                    pie.updateHide();
                    if (!pie.isHidden()) {
                        needNewFrame = true;
                    } else {
                        pie.setAnimateStatus(false);
                        pie.setDrawAllStatus(false);
                        if(i>0){
                            pieHelperList.get(i-1).setAnimateStatus(true);
                            needNewFrame = true;
                        }

                    }
                    if (needNewFrame) {
                        postDelayed(this, 7);
                        pie.setDrawAllStatus(false);
                    } else {
                        booleanDrawDonut=false;
                        post(drawBaseLane);
                    }
                }
            }

            invalidate();
        }
    };

    public PieView(Context context) {
        this(context, null);
    }

    public PieView(Context context, AttributeSet attrs) {
        super(context, attrs);

        pieHelperList = new ArrayList<>();
        columnHelperList = new ArrayList<>();
        cirPaint = new Paint();
        cirPaint.setAntiAlias(true);
        cirPaint.setColor(Color.GRAY);
        cirExtPaint = new Paint();
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
        whitePaint = new Paint();
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

        endEdgePaint = new Paint();
        endEdgePaint.setAntiAlias(true);




        pieCenterPoint = new Point();
        cirRect = new RectF();
        cirSelectedRect = new RectF();
        cirExtRect = new RectF();

        paintGradientBlack = new Paint();
        paintGradientBlack.setStyle(Paint.Style.FILL);

        paintGradientWork = new Paint();
        paintGradientWork.setStyle(Paint.Style.FILL);

        paintGradientRest = new Paint();
        paintGradientRest.setStyle(Paint.Style.FILL);

        dGears = ContextCompat.getDrawable(context, R.drawable.img_gears);
        dMug = ContextCompat.getDrawable(context, R.drawable.img_coffee_mug);
    }


    public ArrayList<PieHelper> getPeriodList(){
        return pieHelperList;
    }

    public ArrayList<ColumnHelper> getColumnList(){return columnHelperList;}


    public boolean isDonutOnScreen(){
        return booleanDrawDonut;
    }



    public void setDate(ArrayList<PieHelper> helperList) {

        initPies(helperList);
        pieHelperList.clear();

        if (!helperList.isEmpty()) {
            for (PieHelper pieHelper : helperList) {
                pieHelperList.add(new PieHelper(pieHelper.getStartDegree(), pieHelper.getEndDegree(), pieHelper));
            }
        }

        removeCallbacks(drawDonut);
        pieHelperList.get(0).setAnimateStatus(true);
        booleanDrawDonut=true;
        float xStepWidth = mViewWidth/5f;
        baseLaneTargetLength = (2*xStepWidth)+2*xStepWidth+toPx(45);
        post(drawDonut);


    }

    public void setColumnData(ArrayList<ColumnHelper> helperList){
        columnHelperList.clear();
        if(helperList!=null && !helperList.isEmpty()){
            columnHelperList.addAll(helperList);
        }
    }


    public void toColumn(){
        removeCallbacks(donutHide);
        selectedIndex = NO_SELECTED_INDEX;
        pieHelperList.get(pieHelperList.size()-1).setAnimateStatus(true);
        post(donutHide);
    }

    public void toDonut(){
        removeCallbacks(columnHide);
        post(columnHide);
    }


    private void initPies(ArrayList<PieHelper> helperList) {
        float totalAngel = 120;
        for (PieHelper pie : helperList) {
            pie.setDegree(totalAngel, totalAngel + pie.getSweep());
            totalAngel += pie.getSweep();
        }


    }




    @Override
    protected void onDraw(Canvas canvas) {
        long selectedLength;
        long selectedFrom, selectedTo;
        if (pieHelperList.isEmpty()) {
            return;
        }
        if(booleanDrawDonut){
            // for (PieHelper pieHelper : pieHelperList) {
            double edgeCircleRadius = pieRadius/10d;
            int pieHelperSize = pieHelperList.size();
            int selectedColor = 0;
            float selectedMiddleDegree = 0;

            for(int i=0;i<pieHelperSize;i++){
                if(pieHelperList.get(i).isAnimated() || pieHelperList.get(i).isDrawAll() ){
                    //Log.d(debug, "sector nr: "+i+ ", isDrawAll status: "+pieHelperList.get(i).isDrawAll());
                    PieHelper pieHelper = pieHelperList.get(i);
                    boolean selected = (selectedIndex == i);
                    //RectF rect = selected ? cirSelectedRect : cirRect;
                    if(selected && selectedColor==0){
                        //Log.d(debug, "selected sector: "+i);
                        selectedColor = getColorByType(pieHelper.getPeriodType());
                        //Log.d(debug, "selected color id:"+getColorByType(pieHelper.getPeriodType()));
                        //Log.d(debug, "work color id:"+colorWork);
                        selectedMiddleDegree = pieHelper.getMiddleDegree();
                    }
                    //Log.d(debug, "onDraw period type: "+pieHelper.getPeriodType());
                    cirPaint.setColor(getColorByType(pieHelper.getPeriodType()));
                    if(i==0){
                        Point startEdge = getCircleEdgeCenter(120);
                        canvas.drawCircle((float)startEdge.x, (float)startEdge.y, (int)edgeCircleRadius, cirPaint);
                    }
                    if(i==pieHelperSize-1){
                        //Log.d(debug, "endDegree: "+pieHelper.getEndDegree());
                        endEdgePaint = cirPaint;
                    }
                    canvas.drawArc(cirRect, pieHelper.getStartDegree(), pieHelper.getSweep(), true, cirPaint);
                }
            }
           // Log.d(debug, "last sector id: "+(pieHelperSize-1));
            if(pieHelperList.get(pieHelperSize-1).isDrawAll()){
                Point endEdge = getCircleEdgeCenter(420);
                canvas.drawCircle((float)endEdge.x, (float)endEdge.y, (int)edgeCircleRadius, endEdgePaint);
            }





            canvas.drawCircle(pieCenterPoint.x, pieCenterPoint.y,pieRadius*0.8f,whitePaint);

            if(selectedIndex!=NO_SELECTED_INDEX){
                if(selectedColor == colorWork){
                    //Log.d(debug, "draw middle lane work, degree: "+selectedMiddleDegree);
                    drawSelectedPointer(canvas, selectedMiddleDegree, colorWorkHighlight);

                } else {
                    drawSelectedPointer(canvas, selectedMiddleDegree, colorRestHighlight);
                    //Log.d(debug, "draw middle lane rest, degree: "+selectedMiddleDegree);

                }
            }

            if(selectedIndex!=NO_SELECTED_INDEX){
                if(selectedColor == colorWork){
                    canvas.drawCircle(pieCenterPoint.x, pieCenterPoint.y,pieRadius*0.7f, paintGradientWork);
                } else {
                    canvas.drawCircle(pieCenterPoint.x, pieCenterPoint.y,pieRadius*0.7f, paintGradientRest);
                }
            }

            canvas.drawCircle(pieCenterPoint.x, pieCenterPoint.y,pieRadius*0.5f,whitePaint);

            if(selectedIndex!=NO_SELECTED_INDEX){
                int extendCount = pieHelperList.get(selectedIndex).getPeriodExtendCount();
                selectedFrom = pieHelperList.get(selectedIndex).getPeriodStart();
                selectedLength = pieHelperList.get(selectedIndex).getPeriodDuration();
                long initialDuration = pieHelperList.get(selectedIndex).getPeriod().getInitialDuration();
                selectedTo = selectedFrom + selectedLength;
                String extendTimes="";
                String extendLength="";
                if(extendCount>0){
                    if(extendCount==1){
                        extendTimes = getContext().getString(R.string.selected_extended_once);
                    } else {
                        extendTimes = String.format(getContext().getString(R.string.selected_extended_multiple),extendCount);
                    }
                    extendLength = RReminder.getExtendMinsFromMillis(getContext(),selectedLength-initialDuration);
                }
                String selectedDuration = RReminder.getShortDurationFromMillis(getContext(),selectedLength);
                canvas.drawText(selectedDuration,pieCenterPoint.x, pieCenterPoint.y+15, columnTextPaint);
                String selectedTime = RReminder.getTimeString(getContext(), selectedFrom).toString()+ " - " + RReminder.getTimeString(getContext(), selectedTo).toString();
                canvas.drawText(selectedTime,pieCenterPoint.x, pieCenterPoint.y+65, columnTextPaint);
                canvas.drawText(extendTimes,pieCenterPoint.x, pieCenterPoint.y+115, cirExtPaint);
                canvas.drawText(extendLength,pieCenterPoint.x, pieCenterPoint.y+165, cirExtPaint);
                if (selectedColor==colorWork){
                    dGears.setBounds(pieCenterPoint.x-70, pieCenterPoint.y-150, pieCenterPoint.x+70, pieCenterPoint.y-50);
                    dGears.draw(canvas);
                } else {
                    dMug.setBounds(pieCenterPoint.x-40, pieCenterPoint.y-150, pieCenterPoint.x+40, pieCenterPoint.y-50);
                    dMug.draw(canvas);
                }

            }


            //drawLineBesideCir(canvas, 120, false);


            //drawLineBesideCir(canvas, pieHelper.getStartDegree(), selected);
            //drawLineBesideCir(canvas, pieHelper.getEndDegree(), selected);
            //}
        } else {
            int stepCount = 2*columnHelperList.size()+1;
            int xStepWidth = mViewWidth/stepCount;
            float baseLaneStartX = xStepWidth-toPx(45);
            canvas.drawLine(baseLaneStartX, mViewHeight, baseLaneLength, mViewHeight, whiteLinePaint);

            String periodCount;
            String totalLength;
            String extendCount;
            String extendTotalDuration;
            float sizeKoef = 1.0f;

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
                float x0 = (2*i*xStepWidth)+xStepWidth-toPx(33);
                float y0 = mViewHeight-toPx(cHelper.getEndHeight())*sizeKoef;
                float x1 = (2*i*xStepWidth)+2*xStepWidth+toPx(33);
                canvas.drawRect(x0,y0,x1,mViewHeight-1,columnPaint);
                if(cHelper.isAtRest()){
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
                    //Log.d(debug, "dpi to pixel 15: "+toPx(15));
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
        }


    }

    private void drawSelectedPointer(Canvas canvas, float angel, int color) {
        double edgeRadiusD = pieRadius*0.76f;
        int edgeRadius = (int)edgeRadiusD;
        float angleLeft = angel-5f;
        float angleRight = angel+5f;
        double edgeSide = pieRadius*0.70f;
        int sthSide = (int)edgeSide;
        int sth = 1;                                       // And it's
        if (angel % 360 > 180 && angel % 360 < 360) {
            sth = -1;
        }
        float lineToX = (float) (mViewHeight / 2 + Math.cos(Math.toRadians(-angel)) * edgeRadius);
        float lineToY = (float) (mViewHeight / 2 + sth * Math.abs(Math.sin(Math.toRadians(-angel))) * edgeRadius);

        float pointLeftX = (float) (mViewHeight / 2 + Math.cos(Math.toRadians(-angleLeft)) * sthSide);
        float pointLeftY = (float) (mViewHeight / 2 + sth * Math.abs(Math.sin(Math.toRadians(-angleLeft))) * sthSide);

        float pointRightX = (float) (mViewHeight / 2 + Math.cos(Math.toRadians(-angleRight)) * sthSide);
        float pointRightY = (float) (mViewHeight / 2 + sth * Math.abs(Math.sin(Math.toRadians(-angleRight))) * sthSide);

        if(angel>175 && angel<=180){
            float diff = 180 - angel;
            pointRightY = pointRightY-(5-diff)*10;
        }

        if(angel>180 && angel<=185){
            float diff = angel - 180;
            pointLeftY = pointLeftY+(5-diff)*10;
        }

        if(angel>355 && angel<360){
            float diff = 360 - angel;
            pointRightY = pointRightY+(5-diff)*10;
        }

        if(angel>=360 && angel<=365){
            float diff = angel - 360;
            pointLeftY = pointLeftY-(5-diff)*10;
        }

        cirPaint.setColor(color);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(lineToX,lineToY);
        path.lineTo(pointLeftX,pointLeftY);
        path.lineTo(pointRightX,pointRightY);
        path.close();

        canvas.drawPath(path, cirPaint);
    }


    private Point getCircleEdgeCenter(float angle){
        Point point = new Point();
        double edgeRadiusD = pieRadius*0.9;
        int edgeRadius = (int)edgeRadiusD;
        int sth = 1;                                       // And it's
        if (angle % 360 > 180 && angle % 360 < 360) {
            sth = -1;
        }
        double x= (mViewHeight / 2d + Math.cos(Math.toRadians(-angle)) * edgeRadius);
        double y = (mViewHeight / 2d + sth * Math.abs(Math.sin(Math.toRadians(-angle))) * edgeRadius);
        point.set((int)x, (int)y);
        return point;
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            if(clickOnDonut(event)){
                selectedIndex = findPointAt((int) event.getX(), (int) event.getY());
            } else {
                selectedIndex = NO_SELECTED_INDEX;
            }
            if(selectedIndex == PieView.NO_SELECTED_INDEX) {
                if (isDonutOnScreen()){
                    toColumn();
                } else {
                    toDonut();
                }
            } else {
                postInvalidate();
            }

        }

        return true;
    }

    private boolean clickOnDonut(MotionEvent event){
        float x = event.getX();
        float y = event.getY();
        int lengthX = (int) x-pieCenterPoint.x;
        int  lengthY = (int)y-pieCenterPoint.y;
        float expectedLength = pieRadius*0.8f;
        float length = (float)Math.sqrt((lengthX*lengthX)+(lengthY*lengthY));
        return length>expectedLength && length<(float)pieRadius;
    }


    private int findPointAt(int x, int y) {
        double degree = Math.atan2(x - pieCenterPoint.x, y - pieCenterPoint.y) * 180 / Math.PI;
        degree = -(degree - 180) + 270;
        if(degree>480){
            degree-=360;
        }
        int index = 0;
        for (PieHelper pieHelper : pieHelperList) {
            if (degree >= pieHelper.getStartDegree() && degree <= pieHelper.getEndDegree()) {
                return index;
            }
            index++;
        }
        return NO_SELECTED_INDEX;
    }

    private int getColorByType(int type){
        switch(type){
            case 1: case 3:{
                return colorWork;
            }
            case 2: case 4: {
                return colorRest;
            }
            default: return Color.BLACK;
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mViewWidth = measureWidth(widthMeasureSpec);
        mViewHeight = measureHeight(heightMeasureSpec);
        int margin = mViewWidth / 16;
        pieRadius = (mViewWidth) / 2 - margin;
        float pieExtRadius = pieRadius*0.81f;
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
        if(radialGradientBlack==null)
        radialGradientBlack = new RadialGradient(pieCenterPoint.x, pieCenterPoint.y,pieRadius*0.7f, new int[] {Color.BLACK, Color.WHITE}, null, Shader.TileMode.MIRROR);
        paintGradientBlack.setShader(radialGradientBlack);

        colorWork = ContextCompat.getColor(getContext(), R.color.work_chart);
        colorRest = ContextCompat.getColor(getContext(), R.color.rest_chart);
        colorWorkHighlight = ContextCompat.getColor(getContext(), R.color.work);
        colorRestHighlight = ContextCompat.getColor(getContext(), R.color.rest);

        if(radialGradientWork==null)
        radialGradientWork = new RadialGradient(pieCenterPoint.x, pieCenterPoint.y,pieRadius*0.7f, new int[] {colorWorkHighlight, Color.WHITE}, null, Shader.TileMode.MIRROR);
        paintGradientWork.setShader(radialGradientWork);

        if(radialGradientRest==null)
        radialGradientRest = new RadialGradient(pieCenterPoint.x, pieCenterPoint.y,pieRadius*0.7f, new int[] {colorRestHighlight, Color.WHITE}, null, Shader.TileMode.MIRROR);
        paintGradientRest.setShader(radialGradientRest);

        setMeasuredDimension(mViewWidth, mViewHeight);
    }
    private float toPx(float dpi){
        return dpi * getResources().getDisplayMetrics().density;
    }

    private int measureWidth(int measureSpec) {
        int preferred = 3;
        return getMeasurement(measureSpec, preferred);
    }

    private int measureHeight(int measureSpec) {
        int preferred = mViewWidth;
        return getMeasurement(measureSpec, preferred);
    }

    @SuppressLint("SwitchIntDef")
    private int getMeasurement(int measureSpec, int preferred) {
        int specSize = View.MeasureSpec.getSize(measureSpec);
        int measurement;

        switch (View.MeasureSpec.getMode(measureSpec)) {
            case View.MeasureSpec.EXACTLY:
                measurement = specSize;
                break;
            case View.MeasureSpec.AT_MOST:
                measurement = Math.min(preferred, specSize);
                break;
            default:
                measurement = preferred;
                break;
        }
        return measurement;
    }

}