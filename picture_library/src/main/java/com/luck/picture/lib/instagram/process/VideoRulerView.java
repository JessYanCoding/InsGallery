package com.luck.picture.lib.instagram.process;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.view.View;

import com.luck.picture.lib.R;
import com.luck.picture.lib.tools.ScreenUtils;

import androidx.core.content.ContextCompat;

/**
 * ================================================
 * Created by JessYan on 2020/6/29 17:38
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class VideoRulerView extends View {
    private long mDuration;
    private final Paint mPaint;
    private final int mPaddingLeft;
    private final int mPaddingRight;
    private final int mShortLineWidth;
    private final int mLongLineWidth;
    private final int mShortLineHeight;
    private final int mLongLineHeight;
    private final TextPaint mTextPaint;
    private int mLineCount;
    private float mInterval;
    private int mStep;

    public VideoRulerView(Context context, long duration) {
        super(context);
        mDuration = duration;
        mPaddingLeft = ScreenUtils.dip2px(context, 20);
        mPaddingRight = ScreenUtils.dip2px(context, 20);
        mLongLineWidth = ScreenUtils.dip2px(context, 1);
        mShortLineWidth = 1;
        mLongLineHeight = ScreenUtils.dip2px(context, 90);
        mShortLineHeight = mLongLineHeight / 2;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(ContextCompat.getColor(context, R.color.picture_color_light_grey));

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(ScreenUtils.sp2px(context, 12));
        mTextPaint.setColor(ContextCompat.getColor(context, R.color.picture_color_light_grey));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mLineCount = mDuration > 60000 ? 60 : (int) (mDuration / 1000);
        if (mDuration < 15000) {
            mInterval = Math.round(mDuration / 1875f) * ((getMeasuredWidth() - mPaddingLeft - mPaddingRight) / 8f) / mLineCount;
            mLineCount = Math.round((getMeasuredWidth() - mPaddingLeft) / mInterval);
        } else {
            mInterval = (getMeasuredWidth() - mPaddingLeft - mPaddingRight) * 1f / mLineCount;
        }

        if (mDuration > 60000) {
            mLineCount = (int) (mDuration / 1000);
            mInterval = Math.round(mDuration / 7500f) * ((getMeasuredWidth() - mPaddingLeft - mPaddingRight) / 8f + 1) / mLineCount;
        }
        mStep = mDuration > 30000 ? 10 : 5;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawLongLineAndText(canvas, mPaddingLeft, 0);
        for (int i = 0; i < mLineCount; i++) {
            int index = i + 1;
            float left = mPaddingLeft + index * 1f * mInterval;
            if (index % mStep == 0) {
                drawLongLineAndText(canvas, left, index);
            } else {
                drawShortLine(canvas, left);
            }
        }
    }

    public int getWidthByScrollX(int scrollX) {
        return scrollX + getRangWidth();
    }

    public int getRangWidth() {
        return getMeasuredWidth() - mPaddingLeft - mPaddingRight;
    }

    public float getInterval() {
        return mInterval;
    }

    private void drawShortLine(Canvas canvas, float left) {
        mPaint.setStrokeWidth(mShortLineWidth);
        canvas.drawLine(left, getMeasuredHeight() - mShortLineHeight, left, getMeasuredHeight(), mPaint);
    }

    private void drawLongLineAndText(Canvas canvas, float left, int time) {
        mPaint.setStrokeWidth(mLongLineWidth);
        canvas.drawLine(left, getMeasuredHeight() - mLongLineHeight, left, getMeasuredHeight(), mPaint);
        String text;
        if (time < 60) {
            text = String.format(":%02d", time);
        } else {
            text = String.format("%d:%02d", time / 60, time % 60);
        }
        canvas.drawText(text, left - mTextPaint.measureText(text) / 2, getMeasuredHeight() - mLongLineHeight - ScreenUtils.dip2px(getContext(), 5), mTextPaint);
    }
}
