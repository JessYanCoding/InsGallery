package com.luck.picture.lib.widget.instagram;

import android.content.Context;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

/**
 * ================================================
 * Created by JessYan on 2020/4/17 11:29
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InstagramCaptureLayout extends FrameLayout {
    private InstagramCaptureButton mCaptureButton;
    private InstagramCaptureButton mRecordButton;

    public InstagramCaptureLayout(@NonNull Context context) {
        super(context);
        mCaptureButton = new InstagramCaptureButton(context);
        addView(mCaptureButton);
        mRecordButton = new InstagramCaptureButton(context);
        addView(mRecordButton);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int diameterSize = (int) (width / 4f);
        mCaptureButton.measure(MeasureSpec.makeMeasureSpec(diameterSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(diameterSize, MeasureSpec.EXACTLY));
        mRecordButton.measure(MeasureSpec.makeMeasureSpec(diameterSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(diameterSize, MeasureSpec.EXACTLY));

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int viewTop = (getMeasuredHeight() - mCaptureButton.getMeasuredHeight()) / 2;
        int viewLeft = (getMeasuredWidth() - mCaptureButton.getMeasuredWidth()) / 2;
        mCaptureButton.layout(viewLeft, viewTop, viewLeft + mCaptureButton.getMeasuredWidth(), viewTop + mCaptureButton.getMeasuredHeight());

        viewLeft += getMeasuredWidth();
        mRecordButton.layout(viewLeft, viewTop, viewLeft + mCaptureButton.getMeasuredWidth(), viewTop + mCaptureButton.getMeasuredHeight());
    }

    public void setCaptureButtonTranslationX(float translationX) {
        mCaptureButton.setTranslationX(translationX);
        mRecordButton.setTranslationX(translationX);
    }
}
