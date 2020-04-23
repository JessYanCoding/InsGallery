package com.luck.picture.lib.widget.instagram;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.luck.picture.lib.R;
import com.luck.picture.lib.camera.listener.CameraListener;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.thread.PictureThreadUtils;
import com.luck.picture.lib.tools.AndroidQTransformUtils;
import com.luck.picture.lib.tools.DateUtils;
import com.luck.picture.lib.tools.MediaUtils;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.luck.picture.lib.tools.ScreenUtils;
import com.luck.picture.lib.tools.SdkVersionUtils;
import com.luck.picture.lib.tools.StringUtils;

import java.io.File;
import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.VideoCapture;
import androidx.camera.view.CameraView;
import androidx.core.content.ContextCompat;

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
    private PictureSelectionConfig mConfig;
    private AppCompatActivity mActivity;
    private final CameraView mCameraView;
    private final ImageView mSwitchView;
    private final ImageView mFlashView;
    private final InstagramCaptureLayout mCaptureLayout;
    private boolean isBind;
    private int mCameraState = STATE_CAPTURE;
    private boolean isFront;
    private CameraListener mCameraListener;
    private long mRecordTime = 0;

    public InstagramCameraView(@NonNull Context context, AppCompatActivity activity, PictureSelectionConfig config) {
        super(context);
        mActivity = activity;
        mConfig = config;

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
                mCameraView.setCaptureMode(androidx.camera.view.CameraView.CaptureMode.IMAGE);
                File imageOutFile = createImageFile();
                mCameraView.takePicture(imageOutFile, ContextCompat.getMainExecutor(getContext().getApplicationContext()), new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        if (SdkVersionUtils.checkedAndroid_Q() && PictureMimeType.isContent(mConfig.cameraPath)) {
                            PictureThreadUtils.executeBySingle(new PictureThreadUtils.SimpleTask<Boolean>() {

                                @Override
                                public Boolean doInBackground() {
                                    return AndroidQTransformUtils.copyPathToDCIM(getContext(),
                                            imageOutFile, Uri.parse(mConfig.cameraPath));
                                }

                                @Override
                                public void onSuccess(Boolean result) {
                                    PictureThreadUtils.cancel(PictureThreadUtils.getSinglePool());
                                }
                            });
                        }
                        if (imageOutFile != null && imageOutFile.exists() && mCameraListener != null) {
                            mCameraListener.onPictureSuccess(imageOutFile);
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        if (mCameraListener != null) {
                            mCameraListener.onError(exception.getImageCaptureError(), exception.getMessage(), exception.getCause());
                        }
                    }
                });
            }

            @Override
            public void recordStart() {
                mCameraView.setCaptureMode(androidx.camera.view.CameraView.CaptureMode.VIDEO);
                mCameraView.startRecording(createVideoFile(), ContextCompat.getMainExecutor(getContext().getApplicationContext()),
                        new VideoCapture.OnVideoSavedCallback() {
                            @Override
                            public void onVideoSaved(@NonNull File file) {
                                if (mRecordTime < mConfig.recordVideoMinSecond && file.exists() && file.delete()) {
                                    return;
                                }
                                if (SdkVersionUtils.checkedAndroid_Q() && PictureMimeType.isContent(mConfig.cameraPath)) {
                                    PictureThreadUtils.executeBySingle(new PictureThreadUtils.SimpleTask<Boolean>() {

                                        @Override
                                        public Boolean doInBackground() {
                                            return AndroidQTransformUtils.copyPathToDCIM(getContext(),
                                                    file, Uri.parse(mConfig.cameraPath));
                                        }

                                        @Override
                                        public void onSuccess(Boolean result) {
                                            PictureThreadUtils.cancel(PictureThreadUtils.getSinglePool());
                                        }
                                    });
                                }
                                if (file != null && file.exists() && mCameraListener != null) {
                                    mCameraListener.onRecordSuccess(file);
                                }
                            }

                            @Override
                            public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                                if (mCameraListener != null) {
                                    mCameraListener.onError(videoCaptureError, message, cause);
                                }
                            }
                        });
            }

            @Override
            public void recordEnd(long time) {
                mRecordTime = time;
                mCameraView.stopRecording();
            }

            @Override
            public void recordShort(long time) {
                mRecordTime = time;
                mCameraView.stopRecording();
            }

            @Override
            public void recordError() {
                if (mCameraListener != null) {
                    mCameraListener.onError(0, "An unknown error", null);
                }
            }
        });
    }

    public File createVideoFile() {
        if (SdkVersionUtils.checkedAndroid_Q()) {
            String diskCacheDir = PictureFileUtils.getVideoDiskCacheDir(getContext());
            File rootDir = new File(diskCacheDir);
            if (!rootDir.exists() && rootDir.mkdirs()) {
            }
            boolean isOutFileNameEmpty = TextUtils.isEmpty(mConfig.cameraFileName);
            String suffix = TextUtils.isEmpty(mConfig.suffixType) ? PictureMimeType.MP4 : mConfig.suffixType;
            String newFileImageName = isOutFileNameEmpty ? DateUtils.getCreateFileName("VID_") + suffix : mConfig.cameraFileName;
            File cameraFile = new File(rootDir, newFileImageName);
            Uri outUri = getOutUri(PictureMimeType.ofVideo());
            if (outUri != null) {
                mConfig.cameraPath = outUri.toString();
            }
            return cameraFile;
        } else {
            String cameraFileName = "";
            if (!TextUtils.isEmpty(mConfig.cameraFileName)) {
                boolean isSuffixOfImage = PictureMimeType.isSuffixOfImage(mConfig.cameraFileName);
                mConfig.cameraFileName = !isSuffixOfImage ? StringUtils
                        .renameSuffix(mConfig.cameraFileName, PictureMimeType.MP4) : mConfig.cameraFileName;
                cameraFileName = mConfig.camera ? mConfig.cameraFileName : StringUtils.rename(mConfig.cameraFileName);
            }
            File cameraFile = PictureFileUtils.createCameraFile(getContext(),
                    PictureMimeType.ofVideo(), cameraFileName, mConfig.suffixType, mConfig.outPutCameraPath);
            mConfig.cameraPath = cameraFile.getAbsolutePath();
            return cameraFile;
        }
    }

    public File createImageFile() {
        if (SdkVersionUtils.checkedAndroid_Q()) {
            String diskCacheDir = PictureFileUtils.getDiskCacheDir(getContext());
            File rootDir = new File(diskCacheDir);
            if (!rootDir.exists() && rootDir.mkdirs()) {
            }
            boolean isOutFileNameEmpty = TextUtils.isEmpty(mConfig.cameraFileName);
            String suffix = TextUtils.isEmpty(mConfig.suffixType) ? PictureFileUtils.POSTFIX : mConfig.suffixType;
            String newFileImageName = isOutFileNameEmpty ? DateUtils.getCreateFileName("IMG_") + suffix : mConfig.cameraFileName;
            File cameraFile = new File(rootDir, newFileImageName);
            Uri outUri = getOutUri(PictureMimeType.ofImage());
            if (outUri != null) {
                mConfig.cameraPath = outUri.toString();
            }
            return cameraFile;
        } else {
            String cameraFileName = "";
            if (!TextUtils.isEmpty(mConfig.cameraFileName)) {
                boolean isSuffixOfImage = PictureMimeType.isSuffixOfImage(mConfig.cameraFileName);
                mConfig.cameraFileName = !isSuffixOfImage ? StringUtils.renameSuffix(mConfig.cameraFileName, PictureMimeType.JPEG) : mConfig.cameraFileName;
                cameraFileName = mConfig.camera ? mConfig.cameraFileName : StringUtils.rename(mConfig.cameraFileName);
            }
            File cameraFile = PictureFileUtils.createCameraFile(getContext(),
                    PictureMimeType.ofImage(), cameraFileName, mConfig.suffixType, mConfig.outPutCameraPath);
            mConfig.cameraPath = cameraFile.getAbsolutePath();
            return cameraFile;
        }
    }

    private Uri getOutUri(int type) {
        return type == PictureMimeType.ofVideo()
                ? MediaUtils.createVideoUri(getContext()) : MediaUtils.createImageUri(getContext());
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

    public void setCameraListener(CameraListener cameraListener) {
        mCameraListener = cameraListener;
    }
}
