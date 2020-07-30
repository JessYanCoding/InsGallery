package com.luck.picture.lib.instagram.process;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import com.luck.picture.lib.R;
import com.luck.picture.lib.tools.ScreenUtils;

import androidx.core.content.ContextCompat;

/**
 * ================================================
 * Created by JessYan on 2020/7/30 10:35
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InstagramLoadingView extends View {
    private Paint mPaint;
    private RectF mBackRect;
    private int mHeight;
    private int mWidth;
    private final int mProgressWidth;
    private final RectF mProgressRect;
    private float mProgress;

    public InstagramLoadingView(Context context) {
        super(context);
        mWidth = ScreenUtils.dip2px(getContext(), 45);
        mHeight = ScreenUtils.dip2px(getContext(), 45);
        mProgressWidth = ScreenUtils.dip2px(getContext(), 25);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mBackRect = new RectF(0, 0, mWidth, mHeight);

        float progressLeft = (mWidth - mProgressWidth) / 2F;
        float progressTop = (mHeight - mProgressWidth) / 2F;
        mProgressRect = new RectF(progressLeft, progressTop, progressLeft + mProgressWidth, progressTop + mProgressWidth);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mWidth, mHeight);
    }

    public void updateProgress(double progress) {
        mProgress = Math.round(360 * progress);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.picture_color_a83));
        canvas.drawRoundRect(mBackRect, ScreenUtils.dip2px(getContext(), 5), ScreenUtils.dip2px(getContext(), 5), mPaint);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(ScreenUtils.dip2px(getContext(), 2));
        mPaint.setColor(0xFF444444);
        canvas.drawCircle(getMeasuredWidth() / 2F, getMeasuredHeight() / 2F, mProgressWidth / 2F, mPaint);


        mPaint.setColor(Color.WHITE);
        canvas.drawArc(mProgressRect, -90, mProgress, false, mPaint);
    }
}
