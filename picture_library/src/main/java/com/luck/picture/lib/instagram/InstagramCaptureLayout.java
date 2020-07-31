package com.luck.picture.lib.instagram;

import android.Manifest;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import com.luck.picture.lib.R;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.permissions.PermissionChecker;
import com.luck.picture.lib.tools.ScreenUtils;
import com.luck.picture.lib.tools.ToastUtils;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;

/**
 * ================================================
 * Created by JessYan on 2020/4/17 11:29
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InstagramCaptureLayout extends FrameLayout {
    private static final int LONG_PRESS = 1;
    private static final int TIMER = 2;
    private InstagramCaptureButton mCaptureButton;
    private InstagramCaptureButton mRecordButton;
    private InstagramRecordProgressBar mRecordProgressBar;
    private InstagramRecordIndicator mRecordIndicator;
    private InstagramCaptureListener mCaptureListener;
    private Handler mHandler;
    private int mCameraState = InstagramCameraView.STATE_CAPTURE;
    boolean click;
    int startClickX;
    int startClickY;
    long time;
    private boolean mInLongPress;
    private boolean mIsRecordEnd;
    private int mRecordedTime;
    private int mMaxDurationTime;
    private int mMinDurationTime;
    private boolean isCameraBind;

    public InstagramCaptureLayout(@NonNull Context context, PictureSelectionConfig config) {
        super(context);
        mHandler = new GestureHandler(context.getMainLooper(), this);

        mRecordProgressBar = new InstagramRecordProgressBar(context, config);
        addView(mRecordProgressBar);
        mRecordProgressBar.setVisibility(View.INVISIBLE);
        mCaptureButton = new InstagramCaptureButton(context);
        addView(mCaptureButton);
        mRecordButton = new InstagramCaptureButton(context);
        addView(mRecordButton);
        mRecordIndicator = new InstagramRecordIndicator(context, config);
        addView(mRecordIndicator);
        mRecordIndicator.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        measureChild(mRecordProgressBar, widthMeasureSpec, heightMeasureSpec);

        int diameterSize = (int) (width / 4.2f);
        mCaptureButton.measure(MeasureSpec.makeMeasureSpec(diameterSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(diameterSize, MeasureSpec.EXACTLY));
        mRecordButton.measure(MeasureSpec.makeMeasureSpec(diameterSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(diameterSize, MeasureSpec.EXACTLY));

        measureChild(mRecordIndicator, widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int viewTop = ScreenUtils.dip2px(getContext(), 1);
        int viewLeft = 0;
        mRecordProgressBar.layout(viewLeft, viewTop, viewLeft + mRecordProgressBar.getMeasuredWidth(), viewTop + mRecordProgressBar.getMeasuredHeight());

        viewTop = (getMeasuredHeight() - mCaptureButton.getMeasuredHeight()) / 2;
        viewLeft = (getMeasuredWidth() - mCaptureButton.getMeasuredWidth()) / 2;
        mCaptureButton.layout(viewLeft, viewTop, viewLeft + mCaptureButton.getMeasuredWidth(), viewTop + mCaptureButton.getMeasuredHeight());

        viewLeft += getMeasuredWidth();
        mRecordButton.layout(viewLeft, viewTop, viewLeft + mCaptureButton.getMeasuredWidth(), viewTop + mCaptureButton.getMeasuredHeight());

        viewTop = (viewTop - mRecordIndicator.getMeasuredHeight()) / 2;
        viewLeft = (getMeasuredWidth() - mRecordIndicator.getMeasuredWidth()) / 2;
        mRecordIndicator.layout(viewLeft, viewTop, viewLeft + mRecordIndicator.getMeasuredWidth(), viewTop + mRecordIndicator.getMeasuredHeight());
    }

    public void setCaptureListener(InstagramCaptureListener captureListener) {
        mCaptureListener = captureListener;
    }

    public void setCaptureButtonTranslationX(float translationX) {
        mCaptureButton.setTranslationX(translationX);
        mRecordButton.setTranslationX(translationX);
    }

    public Rect disallowInterceptTouchRect() {
        if (mCameraState == InstagramCameraView.STATE_RECORDER && mInLongPress) {
            Rect rect = new Rect();
            getHitRect(rect);
            return rect;
        }
        return null;
    }

    public void setCameraState(int cameraState) {
        if (mCameraState == cameraState) {
            return;
        }
        mCameraState = cameraState;
        if (mCameraState == InstagramCameraView.STATE_RECORDER) {
            InstagramUtils.setViewVisibility(mRecordProgressBar, View.VISIBLE);
            mRecordProgressBar.startRecordAnimation();
        } else {
            mRecordProgressBar.stopRecordAnimation();
            InstagramUtils.setViewVisibility(mRecordProgressBar, View.INVISIBLE);
        }
    }

    public void setCameraBind(boolean cameraBind) {
        isCameraBind = cameraBind;
    }

    public void resetRecordEnd() {
        mIsRecordEnd = false;
    }

    public boolean isInLongPress() {
        return mInLongPress;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mCaptureButton == null || mRecordButton == null) {
            return super.onTouchEvent(event);
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mCameraState == InstagramCameraView.STATE_CAPTURE) {
                click = true;
                startClickX = (int) event.getX();
                startClickY = (int) event.getY();

                Rect rect = new Rect();
                mCaptureButton.getHitRect(rect);
                if (rect.contains((int) (event.getX()), (int) (event.getY()))) {
                    if (PermissionChecker.checkSelfPermission(getContext(), Manifest.permission.CAMERA) && !isCameraBind) {
                        ToastUtils.s(getContext(), getContext().getString(R.string.camera_init));
                        return super.onTouchEvent(event);
                    }

                    mCaptureButton.pressButton(true);
                }
            }

            if (mCameraState == InstagramCameraView.STATE_RECORDER) {
                Rect recordRect = new Rect();
                mRecordButton.getHitRect(recordRect);
                if (recordRect.contains((int) (event.getX()), (int) (event.getY()))) {
                    if (PermissionChecker.checkSelfPermission(getContext(), Manifest.permission.CAMERA) && !isCameraBind) {
                        ToastUtils.s(getContext(), getContext().getString(R.string.camera_init));
                        return super.onTouchEvent(event);
                    }

                    mRecordButton.pressButton(true);
                    mInLongPress = false;
                    mHandler.removeMessages(TIMER);
                    mHandler.removeMessages(LONG_PRESS);
                    mHandler.sendMessageAtTime(
                            mHandler.obtainMessage(
                                    LONG_PRESS),
                            event.getDownTime() + ViewConfiguration.getLongPressTimeout());
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (mCameraState == InstagramCameraView.STATE_CAPTURE && click && (Math.abs(event.getX() - startClickX) > ScreenUtils.dip2px(getContext(), 3) || Math.abs(event.getY() - startClickY) > ScreenUtils.dip2px(getContext(), 3))) {
                click = false;
                mCaptureButton.pressButton(false);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mCameraState == InstagramCameraView.STATE_CAPTURE && click) {
                Rect rect = new Rect();
                mCaptureButton.getHitRect(rect);
                if (rect.contains((int) (event.getX()), (int) (event.getY()))) {
                    long elapsedRealtime = SystemClock.elapsedRealtime();
                    if (elapsedRealtime - time > 1000) {
                        time = elapsedRealtime;
                        click = false;
                        if (mCaptureListener != null) {
                            mCaptureListener.takePictures();
                        }
                    }
                }
                mCaptureButton.pressButton(false);
            }

            if (mCameraState == InstagramCameraView.STATE_RECORDER && mRecordButton.isPress() && !mIsRecordEnd) {
                mRecordButton.pressButton(false);

                boolean isCamera = PermissionChecker.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
                boolean isRecordAudio = PermissionChecker.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO);
                if (!isCamera || !isRecordAudio) {
                    return true;
                }

                if (mInLongPress) {
                    mInLongPress = false;
                    mHandler.removeMessages(TIMER);
                    mRecordIndicator.stopIndicatorAnimation();
                    mRecordIndicator.setVisibility(View.INVISIBLE);
                    mRecordProgressBar.stopRecord();
                    if (mRecordedTime < mMinDurationTime) {
                        if (mCaptureListener != null) {
                            mCaptureListener.recordShort(mRecordedTime);
                        }
                        ToastUtils.s(getContext(), getContext().getString(R.string.alert_record, mMinDurationTime));
                    } else if (mCaptureListener != null) {
                        mIsRecordEnd = true;
                        mCaptureListener.recordEnd(mRecordedTime);
                    }
                    mRecordedTime = 0;
                } else {
                    ToastUtils.s(getContext(), getContext().getString(R.string.press_to_record));
                    mHandler.removeMessages(LONG_PRESS);
                    mHandler.removeMessages(TIMER);
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (mCameraState == InstagramCameraView.STATE_CAPTURE) {
                mCaptureButton.pressButton(false);
            }
            if (mCameraState == InstagramCameraView.STATE_RECORDER) {
                mRecordButton.pressButton(false);
                mHandler.removeMessages(LONG_PRESS);
                if (mInLongPress && !mIsRecordEnd) {
                    mHandler.removeMessages(TIMER);
                    mRecordIndicator.stopIndicatorAnimation();
                    mRecordIndicator.setVisibility(View.INVISIBLE);
                    mRecordProgressBar.stopRecord();
                    mRecordedTime = 0;
                }
                mInLongPress = false;
            }
        }
        return true;
    }

    public void setRecordVideoMaxTime(int maxDurationTime) {
        mMaxDurationTime = maxDurationTime;
        mRecordProgressBar.setMaxTime(maxDurationTime * 1000);
    }

    public void setRecordVideoMinTime(int minDurationTime) {
        mMinDurationTime = minDurationTime;
    }

    private static class GestureHandler extends Handler {
        private WeakReference<InstagramCaptureLayout> mCaptureLayout;

        public GestureHandler(Looper looper, InstagramCaptureLayout captureLayout) {
            super(looper);
            mCaptureLayout = new WeakReference<>(captureLayout);
        }

        @Override
        public void handleMessage(Message msg) {
            InstagramCaptureLayout captureLayout = mCaptureLayout.get();
            if (captureLayout == null) {
                return;
            }
            switch (msg.what) {
                case LONG_PRESS:
                    captureLayout.dispatchLongPress();
                    break;
                case TIMER:
                    if (captureLayout.mInLongPress) {
                        captureLayout.mRecordedTime++;
                        captureLayout.mRecordIndicator.setRecordedTime(captureLayout.mRecordedTime);
                        if (captureLayout.mRecordedTime < captureLayout.mMaxDurationTime) {
                            captureLayout.mHandler.sendEmptyMessageDelayed(TIMER, 1000);
                        } else {
                            captureLayout.mInLongPress = false;
                            captureLayout.mIsRecordEnd = true;
                            captureLayout.mRecordIndicator.stopIndicatorAnimation();
                            captureLayout.mRecordIndicator.setVisibility(View.INVISIBLE);
                            captureLayout.mRecordProgressBar.stopRecord();
                            if (captureLayout.mCaptureListener != null) {
                                captureLayout.mCaptureListener.recordEnd(captureLayout.mRecordedTime);
                            }
                            captureLayout.mRecordedTime = 0;
                        }
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown message " + msg); //never
            }
        }
    }

    private void dispatchLongPress() {
        boolean isCamera = PermissionChecker.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
        boolean isRecordAudio = PermissionChecker.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO);
        if (!isCamera || !isRecordAudio) {
            if (mCaptureListener != null) {
                mCaptureListener.recordError();
            }
            return;
        }
        if (mIsRecordEnd) {
            return;
        }
        mInLongPress = true;
        if (mCaptureListener != null) {
            mCaptureListener.recordStart();
        }
        mRecordIndicator.setVisibility(View.VISIBLE);
        mRecordIndicator.setRecordedTime(mRecordedTime);
        mRecordIndicator.playIndicatorAnimation();
        mRecordProgressBar.startRecord();
        mHandler.sendEmptyMessageDelayed(TIMER, 1000);
    }

    public void release() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mRecordProgressBar != null) {
            mRecordProgressBar.release();
        }
        if (mRecordIndicator != null) {
            mRecordIndicator.release();
        }
        mRecordProgressBar = null;
        mRecordIndicator = null;
        mHandler = null;
        mCaptureListener = null;
        mCaptureButton = null;
        mRecordButton = null;
    }
}
