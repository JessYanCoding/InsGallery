package com.luck.picture.lib.widget.instagram;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.luck.picture.lib.R;
import com.luck.picture.lib.tools.ScreenUtils;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.view.CameraView;

/**
 * ================================================
 * Created by JessYan on 2020/4/16 18:40
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InstagramCameraView extends FrameLayout {
    public static final int STATE_CAPTURE = 1;
    public static final int STATE_RECORDER = 2;
    private static final int TYPE_FLASH_AUTO = 0x021;
    private static final int TYPE_FLASH_ON = 0x022;
    private static final int TYPE_FLASH_OFF = 0x023;
    private int mTypeFlash = TYPE_FLASH_OFF;
    private AppCompatActivity mActivity;
    private final CameraView mCameraView;
    private final ImageView mSwitchView;
    private final ImageView mFlashView;
    private final InstagramCaptureLayout mCaptureLayout;
    private boolean isBind;
    private int mCameraState = STATE_CAPTURE;
    private boolean isFront;

    public InstagramCameraView(@NonNull Context context, AppCompatActivity activity) {
        super(context);
        mActivity = activity;

        mCameraView = new CameraView(context);
        addView(mCameraView);

        mSwitchView = new ImageView(context);
        mSwitchView.setImageResource(R.drawable.discover_flip);
        mSwitchView.setOnClickListener((v) -> {
            isFront = !isFront;
            ObjectAnimator.ofFloat(mSwitchView, "rotation", mSwitchView.getRotation() - 180f).setDuration(400).start();
            mCameraView.toggleCamera();
        });
        addView(mSwitchView);

        mFlashView = new ImageView(context);
        mFlashView.setImageResource(R.drawable.discover_flash_off);
        mFlashView.setOnClickListener((v) -> {
            mTypeFlash++;
            if (mTypeFlash > TYPE_FLASH_OFF) {
                mTypeFlash = TYPE_FLASH_AUTO;
            }
            setFlashRes();
        });
        addView(mFlashView);

        mCaptureLayout = new InstagramCaptureLayout(context);
        addView(mCaptureLayout);
        mCaptureLayout.setCaptureListener(new InstagramCaptureListener() {
            @Override
            public void takePictures() {

            }

            @Override
            public void recordStart() {

            }

            @Override
            public void recordEnd(long time) {

            }

            @Override
            public void recordShort(long time) {

            }

            @Override
            public void recordError() {

            }
        });
    }

    @SuppressLint("MissingPermission")
    public void bindToLifecycle() {
        if (!isBind) {
            isBind = true;
            mCameraView.bindToLifecycle(new WeakReference<>(mActivity).get());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        mCameraView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(width - ScreenUtils.dip2px(getContext(), 2), MeasureSpec.EXACTLY));
        mSwitchView.measure(MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 30), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 30), MeasureSpec.EXACTLY));
        mFlashView.measure(MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 30), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 30), MeasureSpec.EXACTLY));
        mCaptureLayout.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height - width + ScreenUtils.dip2px(getContext(), 2), MeasureSpec.EXACTLY));

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int viewTop = 0;
        int viewLeft = 0;
        mCameraView.layout(viewLeft, viewTop, viewLeft + mCameraView.getMeasuredWidth(), viewTop + mCameraView.getMeasuredHeight());

        viewTop = getMeasuredWidth() - ScreenUtils.dip2px(getContext(), 12) - mSwitchView.getMeasuredHeight();
        viewLeft = ScreenUtils.dip2px(getContext(), 14);
        mSwitchView.layout(viewLeft, viewTop, viewLeft + mSwitchView.getMeasuredWidth(), viewTop + mSwitchView.getMeasuredHeight());

        viewLeft = getMeasuredWidth() - ScreenUtils.dip2px(getContext(), 10) - mFlashView.getMeasuredWidth();
        mFlashView.layout(viewLeft, viewTop, viewLeft + mFlashView.getMeasuredWidth(), viewTop + mFlashView.getMeasuredHeight());

        viewTop = getMeasuredWidth() - ScreenUtils.dip2px(getContext(), 2);
        viewLeft = 0;
        mCaptureLayout.layout(viewLeft, viewTop, viewLeft + mCaptureLayout.getMeasuredWidth(), viewTop + mCaptureLayout.getMeasuredHeight());
    }

    public void setCaptureButtonTranslationX(float translationX) {
        mCaptureLayout.setCaptureButtonTranslationX(translationX);
    }

    public void setCameraState(int cameraState) {
        if (mCameraState == cameraState) {
            return;
        }
        mCameraState = cameraState;
        if (mCameraState == STATE_CAPTURE) {
            InstagramUtils.setViewVisibility(mFlashView, View.VISIBLE);
        } else if (mCameraState == STATE_RECORDER) {
            InstagramUtils.setViewVisibility(mFlashView, View.INVISIBLE);
        }
        mCaptureLayout.setCameraState(cameraState);
    }

    public CameraView getCameraView() {
        return mCameraView;
    }

    private void setFlashRes() {
        switch (mTypeFlash) {
            case TYPE_FLASH_AUTO:
                mFlashView.setImageResource(R.drawable.discover_flash_a);
                mCameraView.setFlash(ImageCapture.FLASH_MODE_AUTO);
                break;
            case TYPE_FLASH_ON:
                mFlashView.setImageResource(R.drawable.discover_flash_on);
                mCameraView.setFlash(ImageCapture.FLASH_MODE_ON);
                break;
            case TYPE_FLASH_OFF:
                mFlashView.setImageResource(R.drawable.discover_flash_off);
                mCameraView.setFlash(ImageCapture.FLASH_MODE_OFF);
                break;
            default:
                break;
        }
    }

    public Rect disallowInterceptTouchRect() {
        return mCaptureLayout.disallowInterceptTouchRect();
    }

    public void setRecordVideoMaxTime(int maxDurationTime) {
        mCaptureLayout.setRecordVideoMaxTime(maxDurationTime);
    }

    public void setRecordVideoMinTime(int minDurationTime) {
        mCaptureLayout.setRecordVideoMinTime(minDurationTime);
    }
}
