package com.luck.picture.lib.widget.instagram;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.luck.picture.lib.PictureBaseActivity;
import com.luck.picture.lib.camera.CustomCameraView;
import com.luck.picture.lib.camera.view.CaptureLayout;
import com.luck.picture.lib.config.PictureSelectionConfig;

import java.lang.ref.WeakReference;

import androidx.camera.view.CameraView;

/**
 * ================================================
 * Created by JessYan on 2020/4/15 12:02
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class PagePhoto implements Page {
    CustomCameraView mCameraView;
    PictureBaseActivity mParentActivity;
    PictureSelectionConfig config;

    public PagePhoto(PictureBaseActivity parentActivity, PictureSelectionConfig config) {
        this.mParentActivity = parentActivity;
        this.config = config;
    }

    @Override
    public View getView(Context context) {
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Color.GREEN);
        mCameraView = new CustomCameraView(context);
        initView();
        return mCameraView;
    }

    @Override
    public void refreshData(Context context) {

    }

    @Override
    public void init(int position, ViewGroup parent) {

    }

    @Override
    public String getTitle(Context context) {
        return "照片";
    }

    protected void initView() {
        mCameraView.setPictureSelectionConfig(config);
        // 绑定生命周期
        mCameraView.setBindToLifecycle(new WeakReference<>(mParentActivity).get());
        // 视频最大拍摄时长
        if (config.recordVideoSecond > 0) {
            mCameraView.setRecordVideoMaxTime(config.recordVideoSecond);
        }
        // 视频最小拍摄时长
        if (config.recordVideoMinSecond > 0) {
            mCameraView.setRecordVideoMinTime(config.recordVideoMinSecond);
        }
        // 获取CameraView
        CameraView cameraView = mCameraView.getCameraView();
        if (cameraView != null && config.isCameraAroundState) {
            cameraView.toggleCamera();
        }
        // 获取录制按钮
        CaptureLayout captureLayout = mCameraView.getCaptureLayout();
        if (captureLayout != null) {
            captureLayout.setButtonFeatures(config.buttonFeatures);
        }
        // 拍照预览
        mCameraView.setImageCallbackListener((file, imageView) -> {
            if (config != null && PictureSelectionConfig.imageEngine != null && file != null) {
                PictureSelectionConfig.imageEngine.loadImage(mParentActivity, file.getAbsolutePath(), imageView);
            }
        });

        //左边按钮点击事件
        mCameraView.setOnClickListener(() -> mParentActivity.onBackPressed());
    }
}
