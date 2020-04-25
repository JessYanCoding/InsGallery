package com.luck.picture.lib.widget.instagram;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

import com.luck.picture.lib.PictureBaseActivity;
import com.luck.picture.lib.R;
import com.luck.picture.lib.camera.listener.CameraListener;
import com.luck.picture.lib.config.PictureSelectionConfig;

/**
 * ================================================
 * Created by JessYan on 2020/4/15 12:02
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class PagePhoto implements Page {
    private PictureBaseActivity mParentActivity;
    private PictureSelectionConfig mConfig;
    private InstagramCameraView mInstagramCameraView;
    private CameraListener mCameraListener;

    public PagePhoto(PictureBaseActivity parentActivity, PictureSelectionConfig config) {
        this.mParentActivity = parentActivity;
        this.mConfig = config;
    }

    @Override
    public View getView(Context context) {
        mInstagramCameraView = new InstagramCameraView(context, mParentActivity, mConfig);
        if (mConfig.recordVideoSecond > 0) {
            mInstagramCameraView.setRecordVideoMaxTime(mConfig.recordVideoSecond);
        }
        if (mConfig.recordVideoMinSecond > 0) {
            mInstagramCameraView.setRecordVideoMinTime(mConfig.recordVideoMinSecond);
        }
        if (mCameraListener != null) {
            mInstagramCameraView.setCameraListener(mCameraListener);
        }
        return mInstagramCameraView;
    }

    @Override
    public void refreshData(Context context) {

    }

    @Override
    public void init(int position, ViewGroup parent) {

    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.photo);
    }

    @Override
    public Rect disallowInterceptTouchRect() {
        return mInstagramCameraView.disallowInterceptTouchRect();
    }

    public void bindToLifecycle() {
        mInstagramCameraView.bindToLifecycle();
    }

    public boolean isBindCamera() {
        return mInstagramCameraView.isBind();
    }

    public void setCaptureButtonTranslationX(float translationX) {
        mInstagramCameraView.setCaptureButtonTranslationX(translationX);
    }

    public void setCameraState(int cameraState) {
        mInstagramCameraView.setCameraState(cameraState);
    }

    public void setCameraListener(CameraListener cameraListener) {
        mCameraListener = cameraListener;
        if (mInstagramCameraView != null) {
            mInstagramCameraView.setCameraListener(mCameraListener);
        }
    }

    public void setEmptyViewVisibility(int visibility) {
        mInstagramCameraView.setEmptyViewVisibility(visibility);
    }
}
