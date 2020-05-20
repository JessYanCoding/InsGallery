package com.luck.picture.lib.instagram;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.tools.ScreenUtils;

import androidx.annotation.NonNull;

/**
 * ================================================
 * Created by JessYan on 2020/4/21 15:13
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InstagramRecordIndicator extends FrameLayout {
    private int mRecordedTime;
    private final TextView mTimeView;
    private final IndicatorView mIndicatorView;
    private ObjectAnimator mIndicationrAnimation;

    public InstagramRecordIndicator(@NonNull Context context, PictureSelectionConfig config) {
        super(context);

        mTimeView = new TextView(context);
        mTimeView.setTextSize(14);
        mTimeView.setGravity(Gravity.CENTER_VERTICAL);
        if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK || config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK_BLUE) {
            mTimeView.setTextColor(Color.WHITE);
        } else {
            mTimeView.setTextColor(Color.BLACK);
        }
        addView(mTimeView);

        mIndicatorView = new IndicatorView(context, ScreenUtils.dip2px(context, 4));
        addView(mIndicatorView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = ScreenUtils.dip2px(getContext(), 20);

        mTimeView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
        mIndicatorView.measure(MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 8), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 8), MeasureSpec.EXACTLY));
        setMeasuredDimension(mTimeView.getMeasuredWidth() + mIndicatorView.getMeasuredWidth() * 2, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int viewTop = (getMeasuredHeight() - mIndicatorView.getMeasuredHeight()) / 2;
        int viewLeft = 0;
        mIndicatorView.layout(viewLeft, viewTop, viewLeft + mIndicatorView.getMeasuredWidth(), viewTop + mIndicatorView.getMeasuredHeight());

        viewTop = (getMeasuredHeight() - mTimeView.getMeasuredHeight()) / 2;
        viewLeft = mIndicatorView.getMeasuredWidth() * 2;
        mTimeView.layout(viewLeft, viewTop, viewLeft + mTimeView.getMeasuredWidth(), viewTop + mTimeView.getMeasuredHeight());
    }

    public void setRecordedTime(int recordedTime) {
        if (recordedTime < 0) {
            return;
        }
        mRecordedTime = recordedTime;
        if (recordedTime == 0) {
            mTimeView.setText("0:00");
        } else {
            int minutes = recordedTime / 60;
            int second =  recordedTime % 60;
            mTimeView.setText(String.format("%d:%02d", minutes, second));
        }
    }

    public void playIndicatorAnimation() {
        if (mIndicationrAnimation == null) {
            mIndicationrAnimation = ObjectAnimator.ofFloat(mIndicatorView, "alpha", 1.0f, 0f).setDuration(500);
            mIndicationrAnimation.setRepeatMode(ValueAnimator.REVERSE);
            mIndicationrAnimation.setRepeatCount(ValueAnimator.INFINITE);
        }
        mIndicationrAnimation.start();
    }

    public void stopIndicatorAnimation() {
        if (mIndicationrAnimation != null && mIndicationrAnimation.isRunning()) {
            mIndicationrAnimation.end();
        }
    }

    public void release() {
        if (mIndicationrAnimation != null && mIndicationrAnimation.isRunning()) {
            mIndicationrAnimation.cancel();
        }
        mIndicationrAnimation = null;
    }

    private class IndicatorView extends View {
        private Paint mPaint;
        private int mRadius;

        public IndicatorView(Context context, int radius) {
            super(context);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(0xFFA01309);
            mPaint.setStyle(Paint.Style.FILL);
            mRadius = radius;
        }

        public void setRadius(int radius) {
            mRadius = radius;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawCircle(getMeasuredWidth() / 2f, getMeasuredHeight() / 2f, mRadius, mPaint);
        }
    }
}
