package com.luck.picture.lib.instagram;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.CountDownTimer;
import android.view.View;

import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.tools.ScreenUtils;

import java.lang.ref.WeakReference;

/**
 * ================================================
 * Created by JessYan on 2020/4/22 14:23
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InstagramRecordProgressBar extends View {
    private Paint mPaint;
    private RecordCountDownTimer mTimer;
    private long mMaxTime;
    private float progress;
    private final GradientDrawable defaultIndicator;
    private ValueAnimator mValueAnimator;
    private ValueAnimator.AnimatorUpdateListener mUpdateListener = new UpdateListener(this);

    public InstagramRecordProgressBar(Context context, PictureSelectionConfig config) {
        super(context);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK || config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK_BLUE) {
            mPaint.setColor(Color.WHITE);
            defaultIndicator = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, new int[]{Color.WHITE, Color.BLACK});
        } else {
            mPaint.setColor(Color.BLACK);
            defaultIndicator = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Color.WHITE, Color.BLACK});
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = ScreenUtils.dip2px(getContext(), 4);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, progress, getMeasuredHeight(), mPaint);
        defaultIndicator.setBounds((int) progress, 0, (int) (progress + ScreenUtils.dip2px(getContext(), 8)), getMeasuredHeight());
        defaultIndicator.draw(canvas);
    }

    public void setMaxTime(long maxTime) {
        mMaxTime = maxTime;
        mTimer = new RecordCountDownTimer(mMaxTime, 10, this);
    }

    public void startRecord() {
        if (mTimer != null) {
            mTimer.start();
        }
    }

    public void stopRecord() {
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    public void startRecordAnimation() {
        if (mValueAnimator == null) {
            mValueAnimator = ValueAnimator.ofInt(0, 255);
            mValueAnimator.addUpdateListener(mUpdateListener);
            mValueAnimator.setDuration(500);
            mValueAnimator.setRepeatMode(ValueAnimator.REVERSE);
            mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        }
        mValueAnimator.start();
    }

    public void stopRecordAnimation() {
        if (mValueAnimator != null && mValueAnimator.isRunning()) {
            mValueAnimator.end();
        }
    }

    private void updateProgress(long millisUntilFinished) {
        progress = (mMaxTime - millisUntilFinished) * 1.0f / mMaxTime * getMeasuredWidth();
//        invalidate();
    }

    public void release() {
        if (mValueAnimator != null) {
            mValueAnimator.removeUpdateListener(mUpdateListener);
            if (mValueAnimator.isRunning()) {
                mValueAnimator.cancel();
            }
        }
        if (mTimer != null) {
            mTimer.cancel();
        }
        mUpdateListener = null;
        mTimer = null;
        mValueAnimator = null;
    }

    private static class RecordCountDownTimer extends CountDownTimer {
        private WeakReference<InstagramRecordProgressBar> mRecordProgressBar;

        RecordCountDownTimer(long millisInFuture, long countDownInterval, InstagramRecordProgressBar recordProgressBar) {
            super(millisInFuture, countDownInterval);
            mRecordProgressBar = new WeakReference<>(recordProgressBar);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            InstagramRecordProgressBar recordProgressBar = mRecordProgressBar.get();
            if (recordProgressBar != null) {
                recordProgressBar.updateProgress(millisUntilFinished);
            }
        }

        @Override
        public void onFinish() {

        }
    }

    private static class UpdateListener implements ValueAnimator.AnimatorUpdateListener {
        private WeakReference<InstagramRecordProgressBar> mRecordProgressBar;

        UpdateListener(InstagramRecordProgressBar recordProgressBar) {
            mRecordProgressBar = new WeakReference<>(recordProgressBar);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            InstagramRecordProgressBar recordProgressBar = mRecordProgressBar.get();
            if (recordProgressBar != null) {
                recordProgressBar.defaultIndicator.setAlpha((int) animation.getAnimatedValue());
                recordProgressBar.invalidate();
            }
        }
    }
}
