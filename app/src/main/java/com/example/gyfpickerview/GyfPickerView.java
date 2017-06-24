package com.example.gyfpickerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import java.util.ArrayList;

/**
 * Created by 龚勇峰 on 2017/6/22.
 */

public class GyfPickerView extends View {
    //文字大小
    private int textSize;
    //文字顔色，默认color.blcak
    private int textColor;
    //文字之间的间隔；默认10dp
    private int textPading;
    //文字放大比例，默认2.0f
    private float textMaxScale;
    //文本最小的alpha
    private float textMinAlpha;
    //时候循环，默认循环
    private boolean isRecycleMode;
    //文本对象，主要有 top bottom asent，desent，四个属性
    private Paint.FontMetrics fm;
    //显示的行数，默认是3
    private int maxShowNum;

    private Scroller scroller;
    //速率追踪器
    private VelocityTracker velocityTracker;
    //数据
    private ArrayList<String> dataList = new ArrayList<>();
    //中间x坐标
    private int cx;
    //中间y坐标
    private int cy;
    //文字的最大宽度
    private float textMaxwidth;
    //文字的高度
    private int textHeight;
    //实际内容的宽度
    private int contentWidth;
    //实际内容的高度
    private int contentHeight;


    //按下时y的坐标
    private float downY;
    //本次滑动Y坐标的偏移值
    private float offsetY;
    //在fling之前的offsetY
    private float oldOffsetY;
    //当前的选中项
    private int curIndex;
    //滚动的偏移项
    private int offsetIndex;
    //回弹距离
    private float bonceDistance;
    //是否处于滑动状态
    private boolean isSilding = false;
    private TextPaint textPaint;

    private int minNumVelocity;
    private int maxNumVelocity;
    private int scaleTouchSlop;

    public GyfPickerView(Context context) {
        this(context, null);
    }

    public GyfPickerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GyfPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GyfPickerView);
        textSize = a.getDimensionPixelSize(R.styleable.GyfPickerView_gyfTextSize,
                getDimension(TypedValue.COMPLEX_UNIT_SP, 16, context));
        textColor = a.getColor(R.styleable.GyfPickerView_gyfTextColor, Color.BLACK);
        textPading = a.getDimensionPixelSize(R.styleable.GyfPickerView_gyfTextPading, getDimension(
                TypedValue.COMPLEX_UNIT_DIP, 20, context));
        textMaxScale = a.getFloat(R.styleable.GyfPickerView_gfyTextMaxScale, 2.0f);
        textMinAlpha = a.getFloat(R.styleable.GyfPickerView_gyfTextMinAlpha, 0.4f);
        isRecycleMode = a.getBoolean(R.styleable.GyfPickerView_gyfRecycleMode, true);
        maxShowNum = a.getInteger(R.styleable.GyfPickerView_gyfMaxShowNum, 3);
        a.recycle();

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        fm = textPaint.getFontMetrics();

        scroller = new Scroller(context);
        velocityTracker = VelocityTracker.obtain();

        minNumVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        maxNumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        scaleTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //测量宽度
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        contentWidth = (int) (textMaxwidth * textMaxScale + getPaddingLeft() + getPaddingRight());
        if (mode != MeasureSpec.EXACTLY) {
            //处理warp_content的情况
            width = contentWidth;

        }
        //测量高度
        mode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        textHeight = (int) (fm.bottom - fm.top);
        contentHeight = textHeight * maxShowNum + textPading * maxShowNum;
        if (mode != MeasureSpec.EXACTLY) {
            height = contentHeight + getPaddingTop() + getPaddingBottom();
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //确定中心点坐标
        cx = w / 2;
        cy = h / 2;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        addVelocityTracker(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!scroller.isFinished()) {
                    scroller.forceFinished(true);
                    finishScroll();
                }
                downY = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                offsetY = event.getY() - downY;
                if (isSilding || Math.abs(offsetY) > scaleTouchSlop) {
                    isSilding = true;
                    reDraw();
                }
                break;

            case MotionEvent.ACTION_UP:
                int scrollYVelocity = 2 * getScrollYVelocity() / 3;
                if (Math.abs(scrollYVelocity) > minNumVelocity) {
                    oldOffsetY = offsetY;
                    scroller.fling(0, 0, 0, scrollYVelocity, 0, 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
                    invalidate();
                } else {
                    finishScroll();
                }

                // 没有滑动，则判断点击事件
                if (!isSilding) {
                    if (downY < contentHeight / 3)
                        moveBy(-1);
                    else if (downY > 2 * contentHeight / 3)
                        moveBy(1);
                }

                isSilding = false;
                recycleVelocityTacvker();
                break;
        }
        return true;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dataList.isEmpty())
            return;
        //规定的绘制区域，我们要绘制maxShowCount+2个，但只显示maxShowCount
        canvas.clipRect(cx - contentWidth / 2, cy - contentHeight / 2,
                cx + contentWidth / 2, cy + contentHeight / 2);
        //绘制文字
        int size = dataList.size();
        int contentPadding = textHeight + textPading;
        int half = maxShowNum / 2 + 1;
        for (int i = -half; i <= half; i++) {
            //当前要绘制的字符对应的下标
            int index = curIndex - offsetIndex + i;
            if (isRecycleMode) {
                if (index < 0)
                    index = (index + 1) % dataList.size() + dataList.size() - 1;
                else if (index > dataList.size() - 1)
                    index = index % dataList.size();
            }
            if (index >= 0 && index < size) {
                //计算每个字的中间坐标
                int tempY = cy + i * contentPadding;
                tempY += offsetY % contentPadding;
                //根据每个字中间y坐标到cy的距离，计算出scale的值
                float scale = 1.0f - (1.0f * Math.abs(tempY - cy) / contentPadding);


                //根据textMaxScale，计算出tempScale，范围为1-textMaxScale
                float tempScale = scale * (textMaxScale - 1.0f) + 1.0f;
                tempScale = tempScale < 1.0f ? 1.0f : tempScale;

                //计算文字的alpha值
                float textAlpha=textMinAlpha;
                if (textMaxScale != 1) {
                    float tempAlpha = (tempScale - 1) / (textMaxScale - 1);
                    textAlpha = (1 - textMinAlpha) * tempAlpha + textMinAlpha;
                }

                textPaint.setTextSize(textSize * tempScale);
                textPaint.setAlpha((int) (255 * textAlpha));

                // 绘制
                Paint.FontMetrics tempFm = textPaint.getFontMetrics();
                String text = dataList.get(index);
                float textWidth = textPaint.measureText(text);
                canvas.drawText(text, cx - textWidth / 2, tempY - (tempFm.ascent + tempFm.descent) / 2, textPaint);

            }
        }
    }

    //标准尺寸转化
    private int getDimension(int unit, float value, Context context) {
        return (int) TypedValue.applyDimension(unit, value, context.getResources().getDisplayMetrics());
    }

    /**
     * 设置要显示的数据
     *
     * @param dataList 要显示的数据
     */
    public void setDataList(ArrayList<String> dataList) {
        this.dataList.clear();
        this.dataList.addAll(dataList);

        // 更新maxTextWidth
        if (null != dataList && dataList.size() > 0) {
            int size = dataList.size();
            for (int i = 0; i < size; i++) {
                float tempWidth = textPaint.measureText(dataList.get(i));
                if (tempWidth > textMaxwidth)
                    textMaxwidth = tempWidth;
            }
            curIndex = 0;
        }
        requestLayout();
        invalidate();
    }

    //添加速率追踪器
    private void  addVelocityTracker(MotionEvent event){
     if (velocityTracker==null)
         velocityTracker=VelocityTracker.obtain();
     velocityTracker.addMovement(event);
 }
 //回收
 private void recycleVelocityTacvker(){
        if (velocityTracker!=null)
            velocityTracker.recycle();
        velocityTracker=null;
    }
    //得到速率值
    private int getScrollYVelocity() {
        velocityTracker.computeCurrentVelocity(1000, maxNumVelocity);
        int velocity = (int) velocityTracker.getYVelocity();
        return velocity;
    }
    //滚动时重新绘制
    private void reDraw() {
        // curIndex需要偏移的量
        int i = (int) (offsetY / (textHeight + textPading));
        if (isRecycleMode || (curIndex - i >= 0 && curIndex - i < dataList.size())) {
            if (offsetIndex != i) {
                offsetIndex = i;

                if (null != onScrollChangedListener)
                    onScrollChangedListener.onScrollChanged(getNowIndex(-offsetIndex),this);
            }
            postInvalidate();
        } else {
            finishScroll();
        }
    }
    //结束滚动
    private void finishScroll() {
        // 判断结束滑动后应该停留在哪个位置
        int centerPadding = textHeight + textPading;
        float v = offsetY % centerPadding;
        if (v > 0.5f * centerPadding)
            ++offsetIndex;
        else if (v < -0.5f * centerPadding)
            --offsetIndex;

        // 重置curIndex
        curIndex = getNowIndex(-offsetIndex);

        // 计算回弹的距离
        bonceDistance = offsetIndex * centerPadding - offsetY;
        offsetY += bonceDistance;

        // 更新
        if (null != onScrollChangedListener)
            onScrollChangedListener.onScrollFinished(curIndex,this);

        // 重绘
        reset();
        postInvalidate();
    }
    //得到当前的选中项
    private int getNowIndex(int offsetIndex) {
        int index = curIndex + offsetIndex;
        if (isRecycleMode) {
            if (index < 0)
                index = (index + 1) % dataList.size() + dataList.size() - 1;
            else if (index > dataList.size() - 1)
                index = index % dataList.size();
        } else {
            if (index < 0)
                index = 0;
            else if (index > dataList.size() - 1)
                index = dataList.size() - 1;
        }
        return index;
    }
    /**
     * 滚动指定的偏移量
     *
     * @param offsetIndex 指定的偏移量
     */
    public void moveBy(int offsetIndex) {
        moveTo(getNowIndex(offsetIndex));
    }
    /**
     * 滚动到指定位置
     *
     * @param index 需要滚动到的指定位置
     */
    public void moveTo(int index) {
        if (index < 0 || index >= dataList.size() || curIndex == index)
            return;

        if (!scroller.isFinished())
            scroller.forceFinished(true);

        finishScroll();

        int dy = 0;
        int centerPadding = textHeight + textPading;
        if (!isRecycleMode) {
            dy = (curIndex - index) * centerPadding;
        } else {
            int offsetIndex = curIndex - index;
            int d1 = Math.abs(offsetIndex) * centerPadding;
            int d2 = (dataList.size() - Math.abs(offsetIndex)) * centerPadding;

            if (offsetIndex > 0) {
                if (d1 < d2)
                    dy = d1; // ascent
                else
                    dy = -d2; // descent
            } else {
                if (d1 < d2)
                    dy = -d1; // descent
                else
                    dy = d2; // ascent
            }
        }
        scroller.startScroll(0, 0, 0, dy, 500);
        invalidate();
    }
//回复数据
    private void reset() {
        offsetY = 0;
        oldOffsetY = 0;
        offsetIndex = 0;
        bonceDistance = 0;
    }

    /**
     * 滚动发生变化时的回调接口
     */
    public interface OnScrollChangedListener {
        public void onScrollChanged(int curIndex,View view);

        public void onScrollFinished(int curIndex,View view);
    }
    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            offsetY = oldOffsetY + scroller.getCurrY();

            if (!scroller.isFinished())
                reDraw();
            else
                finishScroll();
        }
    }
    private OnScrollChangedListener onScrollChangedListener;

    public void setOnScrollChangedListener(OnScrollChangedListener onScrollChangedListener) {
        this.onScrollChangedListener = onScrollChangedListener;
    }
}
