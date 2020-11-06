package com.dodola.breakpad;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import java.text.DecimalFormat;


/**
 * 难点
 * 1.计算左右最大的滑动边界
 * 2.惯性滑动
 * 3.只绘制窗口可见区域(防止一次性绘制刻度长度过长，导致界面卡顿的问题)
 * 5.计算当前刻度
 */
class TuningCustomView extends View {
    private static final String TAG = "TuningCustomView";
    private Drawable long_up_line_drawable, long_down_line_drawable, short_line_drawable, mid_line_drawable, mid_dot_drawable;

    private int scaleTop = 50;
    private int long_height = 79;
    private int long_width = 2;

    private int short_height = 17;
    private int short_width = 2;

    private int midline_height = 92;
    private int midline_width = 2;

    private int dotWidth = 10;
    private int dotHeight = 10;
    private int horizontalGap = 30; //左右刻度间隔30dp,  每个刻度代表0.5个频道
    private int verticalGap = 52; // 上下刻度中间间隔52dp 绘制文字
    private int textSize = 30;
    private int mLength = 100; //假设100个频道

    private int uiText = 0;
    private int startValue = 0;
    private int endValue = 10000;
    private TextPaint mPaint;
    int len = (int) (((endValue - startValue) * 0.1) / 2); // 单位刻度数，每2个频道一个刻度
    int unitScaleOffset = 0;
    int padding = 0;
    int oneUnitGap = long_width + horizontalGap * 4 + short_width * 2 + midline_width;
    int minUnitGap = long_width + horizontalGap;
    GestureDetector detector;
    VelocityTracker tracker;
    Scroller mScroller;

    private Paint mIndicatorPaint;
    private int mIndicatorWidth = 5;
    private int mIndicatorHeight = long_height * 2 * 2;
    int leftBorder = 0;
    int rightBorder = 0;

    private Paint mIndicatorScaleTextPaint;

    public TuningCustomView(Context context) {
        super(context);
        init();
    }


    public TuningCustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public TuningCustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            Log.d(TAG, "onLayout() called with: changed = [" + changed + "], left = [" + left + "], top = [" + top + "], right = [" + right + "], bottom = [" + bottom + "]");
            rightBorder = len * oneUnitGap + padding;
            Log.d(TAG, "onLayout:rightBorder " + rightBorder);  //213+1024= 1237
            leftBorder = padding;
        }

    }

    @SuppressLint("ResourceAsColor")
    private void init() {
        mScroller = new Scroller(getContext());
        mScroller.computeScrollOffset();
        mPaint = new TextPaint();
        mPaint.setColor(getResources().getColor(R.color.gray));
        mPaint.setTextSize(textSize);
        mPaint.setStrokeWidth(1);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        mIndicatorPaint = new Paint();
        mIndicatorPaint.setTextSize(textSize);
        mIndicatorPaint.setStrokeWidth(5);
        mIndicatorPaint.setAntiAlias(true);
        mIndicatorPaint.setStyle(Paint.Style.FILL);
        mIndicatorPaint.setColor(getResources().getColor(R.color.colorAccent));

        mIndicatorScaleTextPaint = new Paint();
        mIndicatorScaleTextPaint.setTextSize(20);
        mIndicatorScaleTextPaint.setStrokeWidth(10);
        mIndicatorScaleTextPaint.setAntiAlias(true);
        mIndicatorScaleTextPaint.setStyle(Paint.Style.FILL);
        mIndicatorScaleTextPaint.setColor(getResources().getColor(R.color.colorAccent));

        long_up_line_drawable = getResources().getDrawable(R.drawable.ic_long_up_line);
        long_down_line_drawable = getResources().getDrawable(R.drawable.ic_long_down_line);
        short_line_drawable = getResources().getDrawable(R.drawable.ic_short_line);
        mid_line_drawable = getResources().getDrawable(R.drawable.ic_mid_line);
        mid_dot_drawable = getResources().getDrawable(R.drawable.ic_middot);
        detector = new GestureDetector(this.getContext(), new MyListener());
    }


    boolean isInit = false;
    int lastLeft = -1;

    @Override
    protected void onDraw(Canvas canvas) {
        //1.define the range of scale
        int startValueScale = (int) (0.5 / minUnitGap * getScrollX() * 10); //eg. 10.5 -> 105
        int endValueScale = (int) (0.5 / minUnitGap * (getScrollX() + getWidth()) * 10);

        //covert the scale to px, and draw these scales.
        for (int currentScale = startValueScale; currentScale <= endValueScale; currentScale++) {
            int left = covertScaleToLeftPx(currentScale);
            if (currentScale % 10 == 0) {
                drawHighLine(canvas, left, scaleTop, currentScale);
            } else {
                drawShortLine(canvas, left, scaleTop + long_height - short_height);
            }
        }

        drawIndicator(canvas);
        drawCurrentScaleText(canvas);
    }

    private int covertScaleToLeftPx(int scale) { //1 =  6px
        return (int) (scale * 0.1 * minUnitGap * 2);
    }

    private void drawIndicator(Canvas canvas) {
        int startIndex = getIndicatorStartX();
        canvas.drawLine(startIndex, scaleTop, startIndex + mIndicatorWidth, scaleTop + mIndicatorHeight, mIndicatorPaint);
    }

    private int getIndicatorStartX() {
        return getWidth() / 2 + mIndicatorWidth / 2 + getScrollX();// x坐标，随view的滑动而变化。 只有view.left相对父容器的距离不会变化的。
    }

    private void drawCurrentScaleText(Canvas canvas) {
        DecimalFormat scaleText = new DecimalFormat("0.0");
        String currentScale = scaleText.format(getCurrentScale());
        Rect rect = new Rect();
        mIndicatorScaleTextPaint.getTextBounds(currentScale, 0, currentScale.length(), rect);
        int textWidth = rect.width();
        int textHeight = rect.height();
        int textTop = scaleTop - textHeight / 2;
        int indicatorStart = getIndicatorStartX() - textWidth / 2;
        canvas.drawText(currentScale, indicatorStart, textTop, mIndicatorScaleTextPaint);
    }

    public float getCurrentScale() {
        float px = getIndicatorStartX();
        float scale = (float) (0.5 / minUnitGap * px); // 每像素多少刻度 * 屏幕中间的刻度指示器像素
        return scale;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = detector.onTouchEvent(event); // if result is true, means you handle the touch event in your detector.
        if (!result) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                return true;
            }
        }
        return result;
    }

    class MyListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true; // start to handle touch event
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //e1 是第一次down下去的坐标。 e2是最新的坐标， distaneX是每次移动改变的坐标的差值。
            Log.d(TAG, "onScroll() called with: e1 = [" + e1 + "], e2 = [" + e2 + "], distanceX = [" + distanceX + "], distanceY = [" + distanceY + "]");
            if (getScrollX() + distanceX < leftBorder) { // 手指向右滑动，x移动距离的坐标在左侧边缘左边时，认定为越界
                // getScrollX =  rawx- nowX ;   如果为负，则说明view右移动。
                scrollTo(leftBorder, 0);
                return true;
            } else if (getScrollX() + getWidth() + distanceX > rightBorder) { // 手指向左滑动，x移动距离的坐标在
                int maxX = rightBorder - getWidth();
                scrollTo(maxX, 0);
                return true;
            } else {
                scrollBy((int) distanceX, 0);
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            mScroller.fling();
            Log.d(TAG, "onFling() called with: e1 = [" + e1 + "], e2 = [" + e2 + "], velocityX = [" + velocityX + "], velocityY = [" + velocityY + "]");
//            // Before flinging, aborts the current animation.
            mScroller.fling(getScrollX(), 0, (int) -(velocityX), (int) -velocityY, 0, rightBorder - getWidth(), 0, 0);
            postInvalidate();
            return true;
        }

    }

    public void drawHighLine(Canvas canvas, int left, int top, int scaleValue) {
        //drawUpLIne
        int drawableWidth = long_width;
        int drawableHeight = long_height;

        int upRight = left + drawableWidth;
        int upBottom = top + drawableHeight;

        long_up_line_drawable.setBounds(left, top, upRight, upBottom);
        long_up_line_drawable.draw(canvas);
        // drawNumberText
        DecimalFormat scaleText = new DecimalFormat("0");
        String currentScale = scaleText.format(scaleValue * 0.1);
        Rect rect = new Rect();
        mPaint.getTextBounds(currentScale, 0, currentScale.length(), rect);
        int textWidth = rect.width();
        int textHeight = rect.height();
        int textTop = upBottom + (verticalGap / 2 + textHeight / 2);
        int textLeft = (left - (textWidth / 2));
        canvas.drawText(currentScale, textLeft, textTop, mPaint);

        //drawDownLine
        int downRight = left + drawableWidth;
        int downTop = upBottom + verticalGap;
        int downBottom = downTop + drawableHeight;

        long_down_line_drawable.setBounds(left, downTop, downRight, downBottom);
        long_down_line_drawable.draw(canvas);
    }

    public void drawShortLine(Canvas canvas, int left, int top) {
        //drawUpShortLine
        int drawableWidth = short_width;
        int drawableHeight = short_height;
        int upRight = left + drawableWidth;
        int upBottom = top + drawableHeight;
        short_line_drawable.setBounds(left, top, upRight, upBottom);
        short_line_drawable.draw(canvas);
        //drawDownShortLine
        int downRight = left + drawableWidth;
        int downTop = upBottom + verticalGap;
        int downBottom = downTop + drawableHeight;
        short_line_drawable.setBounds(left, downTop, downRight, downBottom);
        short_line_drawable.draw(canvas);
        Log.d(TAG, "drawShortLine: downRight = " + downRight + "uiText = " + uiText);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            Log.d(TAG, "computeScroll() called" + mScroller.getCurrX());
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }
}
