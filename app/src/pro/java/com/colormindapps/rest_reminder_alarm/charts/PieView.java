package com.colormindapps.rest_reminder_alarm.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.colormindapps.rest_reminder_alarm.MainActivity;
import com.colormindapps.rest_reminder_alarm.R;
import com.colormindapps.rest_reminder_alarm.Session;
import com.colormindapps.rest_reminder_alarm.shared.RReminder;

import java.util.ArrayList;

public class PieView extends View {

    public interface OnPieClickListener {
        void onPieClick(int index);
    }

    private Paint cirPaint;
    private Paint columnPaint;
    private Paint whiteLinePaint;
    private Point pieCenterPoint;
    private Paint textPaint;
    private Paint whitePaint;
    private Paint columnTextPaint;
    private RectF cirRect;
    private RectF cirSelectedRect;
    private Paint paintGradientBlack, paintGradientWork, paintGradientRest;
    private RadialGradient radialGradientBlack, radialGradientWork, radialGradientRest;
    private Drawable dGears, dMug;

    private int mViewWidth;
    private int mViewHeight;
    private int margin;
    private int pieRadius;
    private int colorWork, colorRest, colorWorkHighlight, colorRestHighlight, blackTransparent;
    private boolean drawEndEdge = false;
    private boolean booleanDrawDonut = true;

    private float baseLaneLength = 0;
    private float baseLaneTargetLength;
    private String debug = "PIE_VIEW";

    private OnPieClickListener onPieClickListener;

    private long sessionStart, sessionEnd;

    private ArrayList<PieHelper> pieHelperList;
    private ArrayList<ColumnHelper> columnHelperList;
    private int selectedIndex = NO_SELECTED_INDEX;
    private int selectedColum = NO_SELECTED_COLUMN;

    private boolean showPercentLabel = true;
    public static final int NO_SELECTED_INDEX = -999;
    public static final int NO_SELECTED_COLUMN = -999;
    private final int[] DEFAULT_COLOR_LIST = {Color.parseColor("#33B5E5"),
            Color.parseColor("#AA66CC"),
            Color.parseColor("#99CC00"),
            Color.parseColor("#FFBB33"),
            Color.parseColor("#FF4444")};


    private Runnable drawDonut = new Runnable() {
        @Override
        public void run() {
            boolean needNewFrame = false;
            int size = pieHelperList.size();
            for(int i=0;i<size;i++){
                if(pieHelperList.get(i).isAnimated()){
                    PieHelper pie = pieHelperList.get(i);
                    Log.d(debug, "Animate sector nr. "+i);
                    pie.update();
                    if (!pie.isAtRest()) {
                        needNewFrame = true;
                    } else {
                        pie.setAnimateStatus(false);
                        pie.setDrawAllStatus(true);
                        if(i<size-1){
                            pieHelperList.get(i+1).setAnimateStatus(true);
                            needNewFrame = true;
                        }

                    }
                    if (needNewFrame) {
                        postDelayed(this, 10);
                    } else {
                        pieHelperList.get(size-1).setDrawAllStatus(true);
                    }
                }
            }


            invalidate();
        }
    };

    private Runnable drawBaseLane = new Runnable(){
        @Override
        public void run() {
            boolean needNewFrame = false;
            baseLaneLength+=50;
            if(baseLaneLength<baseLaneTargetLength){
                postDelayed(this, 10);
            } else {
                removeCallbacks(drawColumns);
                post(drawColumns);

                //start runnable for drawing columns
            }
            invalidate();
        }
    };

    private Runnable hideBaseLine = new Runnable(){
        @Override
        public void run() {
            boolean needNewFrame = false;
            baseLaneLength-=50;
            if(baseLaneLength>0){
                postDelayed(this, 10);
            } else {
                removeCallbacks(drawDonut);
                booleanDrawDonut=true;
                pieHelperList.get(0).setAnimateStatus(true);
                post(drawDonut);
            }
            invalidate();
        }
    };

    private Runnable drawColumns = new Runnable() {
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
                postDelayed(this,10);
            }
            invalidate();
        }
    };

    private Runnable columnHide = new Runnable() {
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
                postDelayed(this,10);
            } else {
                removeCallbacks(hideBaseLine);
                post(hideBaseLine);
            }
            invalidate();
        }
    };

    private Runnable donutHide = new Runnable() {
        @Override
        public void run() {
            boolean needNewFrame = false;
            for(int i=pieHelperList.size()-1;i>=0;i--){
                if(pieHelperList.get(i).isAnimated()){
                    PieHelper pie = pieHelperList.get(i);
                    Log.d(debug, " HIDE Animate sector nr. "+i);
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
                        postDelayed(this, 10);
                        pie.setDrawAllStatus(false);
                    } else {
                        booleanDrawDonut=false;
                        baseLaneTargetLength = mViewWidth;
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

        pieHelperList = new ArrayList<PieHelper>();

        columnHelperList = new ArrayList<>();
        cirPaint = new Paint();
        cirPaint.setAntiAlias(true);
        cirPaint.setColor(Color.GRAY);
        columnPaint = new Paint();
        columnPaint.setAntiAlias(true);
        columnPaint.setColor(Color.GRAY);
        whiteLinePaint = new Paint(cirPaint);
        whiteLinePaint.setColor(Color.BLACK);
        whiteLinePaint.setStrokeWidth(2f);
        whitePaint = new Paint();
        whitePaint.setAntiAlias(true);
        whitePaint.setColor(Color.WHITE);
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(RReminder.sp2px(getContext(), 15));
        textPaint.setStrokeWidth(5);
        textPaint.setTextAlign(Paint.Align.CENTER);

        columnTextPaint = new Paint();
        columnTextPaint.setAntiAlias(true);
        columnTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.black_transparent));
        columnTextPaint.setTextSize(RReminder.sp2px(getContext(), 15));
        columnTextPaint.setStrokeWidth(5);
        columnTextPaint.setTextAlign(Paint.Align.CENTER);

        pieCenterPoint = new Point();
        cirRect = new RectF();
        cirSelectedRect = new RectF();

        paintGradientBlack = new Paint();
        paintGradientBlack.setStyle(Paint.Style.FILL);

        paintGradientWork = new Paint();
        paintGradientWork.setStyle(Paint.Style.FILL);

        paintGradientRest = new Paint();
        paintGradientRest.setStyle(Paint.Style.FILL);

        dGears = ContextCompat.getDrawable(context, R.drawable.img_gears);
        dMug = ContextCompat.getDrawable(context, R.drawable.img_coffee_mug);

        


    }

    public void showPercentLabel(boolean show) {
        showPercentLabel = show;
        postInvalidate();
    }

    public void setOnPieClickListener(OnPieClickListener listener) {
        onPieClickListener = listener;
    }

    public void setDate(ArrayList<PieHelper> helperList) {
        initPies(helperList);
        pieHelperList.clear();
        removeSelectedPie();

        if (helperList != null && !helperList.isEmpty()) {
            for (PieHelper pieHelper : helperList) {
                pieHelperList.add(new PieHelper(pieHelper.getStartDegree(), pieHelper.getStartDegree(), pieHelper));
            }
        } else {
            pieHelperList.clear();
        }

        removeCallbacks(drawDonut);
        Log.d(debug, "startAnimate");
        pieHelperList.get(0).setAnimateStatus(true);
        booleanDrawDonut=true;
        post(drawDonut);


    }

    public void setSession(long start, long end){
        this.sessionStart = start;
        this.sessionEnd = end;
    }

    public void setColumnData(ArrayList<ColumnHelper> helperList){
        columnHelperList.clear();
        if(helperList!=null && !helperList.isEmpty()){
            for (ColumnHelper columnHelper : helperList){
                columnHelperList.add(columnHelper);
            }
        }
    }


    public void toColumn(){
        removeCallbacks(donutHide);
        selectedIndex = NO_SELECTED_INDEX;
        Log.d(debug, "startHideAnimate");
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

    private void initColumns(ArrayList<ColumnHelper> columnList){

    }

    public void selectedPie(int index) {
        selectedIndex = index;
        if (onPieClickListener != null) onPieClickListener.onPieClick(index);
        postInvalidate();
    }

    public void removeSelectedPie() {
        selectedIndex = NO_SELECTED_INDEX;
        if (onPieClickListener != null) onPieClickListener.onPieClick(NO_SELECTED_INDEX);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(debug, "onDraw");
        long selectedLength = 0;
        long selectedFrom, selectedTo;
        if (pieHelperList.isEmpty()) {
            return;
        }
        if(booleanDrawDonut){
            // for (PieHelper pieHelper : pieHelperList) {
            Paint endEdgePaint = new Paint();
            endEdgePaint.setAntiAlias(true);
            double edgeCircleRadius = pieRadius/10;
            int pieHelperSize = pieHelperList.size();
            int selectedColor = 0;
            float selectedMiddleDegree = 0;

            for(int i=0;i<pieHelperSize;i++){
                if(pieHelperList.get(i).isAnimated() || pieHelperList.get(i).isDrawAll() ){
                    Log.d(debug, "sector nr: "+i+ ", isDrawAll status: "+pieHelperList.get(i).isDrawAll());
                    PieHelper pieHelper = pieHelperList.get(i);
                    boolean selected = (selectedIndex == i);
                    //RectF rect = selected ? cirSelectedRect : cirRect;
                    RectF rect = cirRect;
                    if(selected && selectedColor==0){
                        Log.d(debug, "selected sector: "+i);
                        selectedColor = getColorByType(pieHelper.getPeriodType());
                        Log.d(debug, "selected color id:"+getColorByType(pieHelper.getPeriodType()));
                        Log.d(debug, "work color id:"+colorWork);
                        selectedMiddleDegree = pieHelper.getMiddleDegree();
                    }
                    Log.d(debug, "onDraw period type: "+pieHelper.getPeriodType());
                    cirPaint.setColor(getColorByType(pieHelper.getPeriodType()));
                    if(i==0){
                        Point startEdge = getCircleEdgeCenter(120,false);
                        canvas.drawCircle((float)startEdge.x, (float)startEdge.y, (int)edgeCircleRadius, cirPaint);
                    }
                    if(i==pieHelperSize-1){
                        Log.d(debug, "endDegree: "+pieHelper.getEndDegree());
                        endEdgePaint = cirPaint;
                    }
                    canvas.drawArc(rect, pieHelper.getStartDegree(), pieHelper.getSweep(), true, cirPaint);
                    drawPercentText(canvas, pieHelper);
                }
            }
            Log.d(debug, "last sector id: "+(pieHelperSize-1));
            if(pieHelperList.get(pieHelperSize-1).isDrawAll()){
                Point endEdge = getCircleEdgeCenter(420,false);
                canvas.drawCircle((float)endEdge.x, (float)endEdge.y, (int)edgeCircleRadius, endEdgePaint);
            }



            canvas.drawCircle(pieCenterPoint.x, pieCenterPoint.y,pieRadius*0.8f,whitePaint);

            if(selectedIndex!=NO_SELECTED_INDEX){
                if(selectedColor == colorWork){
                    Log.d(debug, "draw middle lane work, degree: "+selectedMiddleDegree);
                    drawSelectedPointer(canvas, selectedMiddleDegree, colorWorkHighlight);

                } else {
                    drawSelectedPointer(canvas, selectedMiddleDegree, colorRestHighlight);
                    Log.d(debug, "draw middle lane rest, degree: "+selectedMiddleDegree);

                }
            }

            if(selectedIndex==NO_SELECTED_INDEX){
                //canvas.drawCircle(pieCenterPoint.x, pieCenterPoint.y,pieRadius*0.7f, paintGradientBlack);
            } else {
                if(selectedColor == colorWork){
                    Log.d(debug, "selected color WORK");
                    canvas.drawCircle(pieCenterPoint.x, pieCenterPoint.y,pieRadius*0.7f, paintGradientWork);
                } else {
                    Log.d(debug, "selected color REST");
                    canvas.drawCircle(pieCenterPoint.x, pieCenterPoint.y,pieRadius*0.7f, paintGradientRest);
                }
            }

            canvas.drawCircle(pieCenterPoint.x, pieCenterPoint.y,pieRadius*0.5f,whitePaint);

            if(selectedIndex==NO_SELECTED_INDEX){
                long length = sessionEnd-sessionStart;
                String duration = RReminder.getDurationFromMillis(getContext(),length);
                canvas.drawText(duration,pieCenterPoint.x, pieCenterPoint.y-25, textPaint);
                String time = RReminder.getTimeString(getContext(), sessionStart).toString()+ " - " + RReminder.getTimeString(getContext(), sessionEnd).toString();
                canvas.drawText(time,pieCenterPoint.x, pieCenterPoint.y+25, textPaint);
            } else {
                selectedTo = pieHelperList.get(selectedIndex).getPeriodEndTime();
                if(selectedIndex==0){
                    selectedFrom = sessionStart;
                } else {
                    selectedFrom = pieHelperList.get(selectedIndex-1).getPeriodEndTime();
                }
                int extendCount = pieHelperList.get(selectedIndex).getPeriodExtendCount();
                selectedLength = selectedTo - selectedFrom;
                String extendText="";
                if(extendCount>0){
                    if(extendCount==1){
                        extendText = getContext().getString(R.string.description_extended_one_time);
                    } else {
                        extendText = String.format(getContext().getString(R.string.description_extended),extendCount);
                    }
                }
                String selectedDuration = RReminder.getDurationFromMillis(getContext(),selectedLength);
                canvas.drawText(selectedDuration,pieCenterPoint.x, pieCenterPoint.y+15, textPaint);
                String selectedTime = RReminder.getTimeString(getContext(), selectedFrom).toString()+ " - " + RReminder.getTimeString(getContext(), selectedTo).toString();
                canvas.drawText(selectedTime,pieCenterPoint.x, pieCenterPoint.y+65, textPaint);
                canvas.drawText(extendText,pieCenterPoint.x, pieCenterPoint.y+115, textPaint);
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
            canvas.drawLine(100, mViewHeight, baseLaneLength-100, mViewHeight, whiteLinePaint);
            int stepCount = 2*columnHelperList.size()+1;
            Log.d(debug, "step count: "+stepCount);
            int xStepWidth = mViewWidth/stepCount;
            String periodCount = "";
            String totalLength = "";
            String extendCount = "";
            for(int i=0; i<columnHelperList.size();i++){

                ColumnHelper cHelper = columnHelperList.get(i);
                columnPaint.setColor(cHelper.getColor());
                float x0 = (2*i*xStepWidth)+xStepWidth-50;
                float y0 = mViewHeight-cHelper.getEndHeight();
                float x1 = (2*i*xStepWidth)+2*xStepWidth+50;
                canvas.drawRect(x0,y0,x1,mViewHeight-1,columnPaint);
                if(cHelper.isAtRest()){
                    float imgX0, imgX1, imgY0, imgY1;
                    imgX0 = x0+50+xStepWidth/2f-70;
                    imgX1 = x0+50+xStepWidth/2f+70;
                    imgY0 = y0+50;
                    imgY1 = y0+150;
                    if(i==0){
                        dGears.setBounds((int)imgX0, (int)imgY0, (int)imgX1, (int)imgY1);
                        dGears.draw(canvas);
                        //canvas.drawLine((int)(x0+xStepWidth/2f), (int)y0, (int)(x0+xStepWidth/2f), mViewHeight, whiteLinePaint);
                    } else {
                        float mugX0 = imgX0+30;
                        float mugX1 = imgX1-30;
                        dMug.setBounds((int)mugX0, (int)imgY0, (int)mugX1, (int)imgY1);
                        dMug.draw(canvas);
                    }
                    periodCount = ((cHelper.getCount())>1 ? cHelper.getCount()+ " "+getContext().getString(R.string.periods) : cHelper.getCount()+ " "+getContext().getString(R.string.period));
                    canvas.drawText(periodCount,imgX0+65, y0+220, columnTextPaint);
                    totalLength = RReminder.getDurationFromMillis(getContext(),cHelper.getTotalLength());
                    canvas.drawText(totalLength,imgX0+65, y0+270, columnTextPaint);
                    if(cHelper.getExtendCount()>0){
                        extendCount = ((cHelper.getExtendCount())>1 ? cHelper.getExtendCount()+" "+getContext().getString(R.string.extensions) : cHelper.getExtendCount()+" "+getContext().getString(R.string.extension));
                        canvas.drawText(extendCount,imgX0+65, y0+320, columnTextPaint);
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
        int sth2 = edgeRadius; // Sorry I'm really don't know how to name the variable..
        int sth = 1;                                       // And it's
        if (angel % 360 > 180 && angel % 360 < 360) {
            sth = -1;
        }
        float lineToX = (float) (mViewHeight / 2 + Math.cos(Math.toRadians(-angel)) * sth2);
        float lineToY = (float) (mViewHeight / 2 + sth * Math.abs(Math.sin(Math.toRadians(-angel))) * sth2);

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
        Log.d(debug, "draw selected line");
        //canvas.drawLine(pieCenterPoint.x, pieCenterPoint.y, lineToX, lineToY, cirPaint);
    }


    private Point getCircleEdgeCenter(float angle, boolean selectedCir){
        Point point = new Point();
        double edgeRadiusD = pieRadius*0.9;
        int edgeRadius = (int)edgeRadiusD;
        int sth2 = selectedCir ? mViewHeight / 2 : edgeRadius; // Sorry I'm really don't know how to name the variable..
        int sth = 1;                                       // And it's
        if (angle % 360 > 180 && angle % 360 < 360) {
            sth = -1;
        }
        double x= (mViewHeight / 2 + Math.cos(Math.toRadians(-angle)) * sth2);
        double y = (mViewHeight / 2 + sth * Math.abs(Math.sin(Math.toRadians(-angle))) * sth2);
        point.set((int)x, (int)y);
        return point;
    }

    private void drawPercentText(Canvas canvas, PieHelper pieHelper) {
        if (!showPercentLabel) return;
        float angel = (pieHelper.getStartDegree() + pieHelper.getEndDegree()) / 2;
        int sth = 1;
        if (angel % 360 > 180 && angel % 360 < 360) {
            sth = -1;
        }
        float x = (float) (mViewHeight / 2 + Math.cos(Math.toRadians(-angel)) * pieRadius / 2);
        float y = (float) (mViewHeight / 2 + sth * Math.abs(Math.sin(Math.toRadians(-angel))) * pieRadius / 2);
        canvas.drawText(pieHelper.getPercentStr(), x, y, textPaint);
    }

    private void drawText(Canvas canvas, PieHelper pieHelper) {
        if (pieHelper.getTitle() == null) return;
        float angel = (pieHelper.getStartDegree() + pieHelper.getEndDegree()) / 2;
        int sth = 1;
        if (angel % 360 > 180 && angel % 360 < 360) {
            sth = -1;
        }
        float x = (float) (mViewHeight / 2 + Math.cos(Math.toRadians(-angel)) * pieRadius / 2);
        float y = (float) (mViewHeight / 2 + sth * Math.abs(Math.sin(Math.toRadians(-angel))) * pieRadius / 2);
        canvas.drawText(pieHelper.getTitle(), x, y, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            if(clickOnDonut(event)){
                selectedIndex = findPointAt((int) event.getX(), (int) event.getY());
            } else {
                selectedIndex = NO_SELECTED_INDEX;
            }

            if (onPieClickListener != null) {
                onPieClickListener.onPieClick(selectedIndex);
            }
            postInvalidate();
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
        Log.d(debug, "touch degree: "+degree);
        int index = 0;
        for (PieHelper pieHelper : pieHelperList) {
            Log.d(debug, "start degree: "+pieHelper.getStartDegree()+", end degree: "+pieHelper.getEndDegree());
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
        Log.d(debug, "onMeasure");
        mViewWidth = measureWidth(widthMeasureSpec);
        mViewHeight = measureHeight(heightMeasureSpec);
        margin = mViewWidth / 16;
        pieRadius = (mViewWidth) / 2 - margin;
        pieCenterPoint.set(pieRadius + margin, pieRadius + margin);
        cirRect.set(pieCenterPoint.x - pieRadius,
                pieCenterPoint.y - pieRadius,
                pieCenterPoint.x + pieRadius,
                pieCenterPoint.y + pieRadius);
        cirSelectedRect.set(2, //minor margin for bigger circle
                2,
                mViewWidth - 2,
                mViewHeight - 2);

        radialGradientBlack = new RadialGradient(pieCenterPoint.x, pieCenterPoint.y,pieRadius*0.7f, new int[] {Color.BLACK, Color.WHITE}, null, Shader.TileMode.MIRROR);
        paintGradientBlack.setShader(radialGradientBlack);

        colorWork = ContextCompat.getColor(getContext(), R.color.work_chart);
        colorRest = ContextCompat.getColor(getContext(), R.color.rest_chart);
        colorWorkHighlight = ContextCompat.getColor(getContext(), R.color.work);
        colorRestHighlight = ContextCompat.getColor(getContext(), R.color.rest);


        radialGradientWork = new RadialGradient(pieCenterPoint.x, pieCenterPoint.y,pieRadius*0.7f, new int[] {colorWorkHighlight, Color.WHITE}, null, Shader.TileMode.MIRROR);
        paintGradientWork.setShader(radialGradientWork);

        radialGradientRest = new RadialGradient(pieCenterPoint.x, pieCenterPoint.y,pieRadius*0.7f, new int[] {colorRestHighlight, Color.WHITE}, null, Shader.TileMode.MIRROR);
        paintGradientRest.setShader(radialGradientRest);

        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    private int measureWidth(int measureSpec) {
        int preferred = 3;
        return getMeasurement(measureSpec, preferred);
    }

    private int measureHeight(int measureSpec) {
        int preferred = mViewWidth;
        return getMeasurement(measureSpec, preferred);
    }

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