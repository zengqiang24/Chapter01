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
import android.view.View;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import java.text.DecimalFormat;


/**
 * 难点
 * 1.计算左右最大的滑动边界
 * 2.惯性滑动
 * 3.只绘制窗口可见区域
 * 5.计算当前刻度
 */
class TuningView extends View {
    private static final String TAG = "TuningCustomView";
    private Drawable long_up_line_drawable, long_down_line_drawable, short_line_drawable, mid_line_drawable, mid_dot_drawable;

    private int scaleTop = 200;
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
    private int startValue = 10;
    private int endValue = 1000;
    private TextPaint mPaint;
    int len = (int) (((endValue - startValue) * 0.1) / 2); // 单位刻度数，每2个频道一个刻度
    int offset = 0;
    int padding = 0;
    int oneUnitGap = long_width + horizontalGap * 4 + short_width * 2 + midline_width;
    int minUnitGap = long_width + horizontalGap;
    GestureDetector detector;
    Scroller mScroller;

    private Paint mIndicatorPaint;
    private int mIndicatorWidth = 5;
    private int mIndicatorHeight = long_height * 2 * 2;
    int leftBorder = 0;
    int rightBorder = 0;

    private Paint mIndicatorScaleTextPaint;

    public TuningView(Context context) {
        super(context);
        init();
    }


    public TuningView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public TuningView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.d(TAG, "onLayout() called with: changed = [" + changed + "], left = [" + left + "], top = [" + top + "], right = [" + right + "], bottom = [" + bottom + "]");
        rightBorder = len * oneUnitGap + padding;
        Log.d(TAG, "onLayout:rightBorder " + rightBorder);  //213+1024= 1237
        leftBorder = padding;
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
        mIndicatorScaleTextPaint.setTextSize(50);
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


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        startValue = 10;
        uiText = 1;
        offset = padding;
        for (int i = 0; i < len; i++) {
            drawOneUnit(canvas, offset, scaleTop);
            uiText = (int) (startValue * 0.1);
            offset = offset + oneUnitGap;
        }
        drawIndicator(canvas);
        drawCurrentScaleText(canvas);
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
        float scale = (float) (0.5 / minUnitGap * px) + 1.0f; // 每像素多少刻度 * 屏幕中间的刻度指示器像素
        return scale;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = detector.onTouchEvent(event); // if result is true, means you handle the touch event in your detector.
        if (!result) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                stopScrolling();
                return true;
            }
        }
        return result;
    }

    private void stopScrolling() {

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
            mScroller.fling(getScrollX(), 0, (int) -(velocityX/3), (int) -velocityY, 0, rightBorder - getWidth(), 0, 0);
            postInvalidate();
            return true;
        }

    }

    //top是左上角纵坐标，left是左上角横坐标，right是右下角横坐标，bottom是右下角纵坐标。
    private void drawOneUnit(Canvas canvas, int left, int top) {
        drawHighLine(canvas, left, top);
        int offset = long_height - short_height;
        int shortTop = top + offset;

        int longIntrinsicWidth = long_width;
        int shortLeft = left + longIntrinsicWidth + horizontalGap;

        drawShortLine(canvas, shortLeft, shortTop);
        Log.d(TAG, "shortTop  = " + shortTop + " shortLeft" + shortLeft);

        int shortIntrinsicWidth = short_width;
        int midLeft = shortLeft + shortIntrinsicWidth + horizontalGap;
        int midTop = shortTop;
        drawMidLine(canvas, midLeft, midTop);

        int shortTop2 = shortTop;
        int midIntrinsicWidth = midline_width;
        int shortLeft2 = midLeft + midIntrinsicWidth + horizontalGap;
        drawShortLine(canvas, shortLeft2, shortTop2);
    }

    public void drawHighLine(Canvas canvas, int left, int top) {
        //drawUpLIne
        int drawableWidth = long_width;
        int drawableHeight = long_height;

        int upRight = left + drawableWidth;
        int upBottom = top + drawableHeight;

        long_up_line_drawable.setBounds(left, top, upRight, upBottom);
        long_up_line_drawable.draw(canvas);
        // drawNumberText

        Rect rect = new Rect();
        String tempText = String.valueOf(uiText);
        mPaint.getTextBounds(tempText, 0, tempText.length(), rect);
        int textWidth = rect.width();
        int textHeight = rect.height();
        int textTop = upBottom + (verticalGap / 2 + textHeight / 2);
        int textLeft = (int) (left - (textWidth / 2));
        Log.d(TAG, "drawHighLine upBottom" + upBottom + " textTop = " + textTop + "drawHighLine: textWidth= " + textWidth + " textLeft = " + textLeft);
        canvas.drawText(uiText + "", textLeft, textTop, mPaint);

        //drawDownLine
        int downRight = left + drawableWidth;
        int downTop = upBottom + verticalGap;
        int downBottom = downTop + drawableHeight;

        long_down_line_drawable.setBounds(left, downTop, downRight, downBottom);
        long_down_line_drawable.draw(canvas);
        startValue += 5;
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
        startValue += 5;
//        uiText = (int) (startValue * 0.1);
        Log.d(TAG, "drawShortLine: downRight = " + downRight + "uiText = " + uiText);
    }

    public void drawMidLine(Canvas canvas, int left, int top) {
        //drawMidLine
        int drawableWidth = midline_width;
        int drawableHeight = midline_height;
        int upRight = left + drawableWidth;
        int upBottom = top + 90;
        mid_line_drawable.setBounds(left, top, upRight, upBottom);
        mid_line_drawable.draw(canvas);

        //dra mid dot
        int midDotWidth = dotWidth;
        int midDotHeight = dotHeight;
        int midDotLeft = left - midDotWidth / 2 + drawableWidth / 2;
        int midDotTop = top + (drawableHeight / 2) - (midDotHeight / 2);
        int midDotRight = midDotLeft + midDotWidth;
        int downBottom = midDotTop + midDotHeight;

        mid_dot_drawable.setBounds(midDotLeft, midDotTop, midDotRight, downBottom);
        mid_dot_drawable.draw(canvas);
        startValue += 5;
//        uiText = (int) (startValue * 0.1);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        Log.d(TAG, "computeScroll() called" + mScroller.getCurrX());
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }
}
