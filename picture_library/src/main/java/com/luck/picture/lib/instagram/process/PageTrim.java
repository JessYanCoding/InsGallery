package com.luck.picture.lib.instagram.process;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import com.luck.picture.lib.R;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.instagram.Page;

import java.util.concurrent.CountDownLatch;

/**
 * ================================================
 * Created by JessYan on 2020/4/15 12:02
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class PageTrim implements Page {
    private PictureSelectionConfig mConfig;
    private LocalMedia mMedia;
    private VideoView mVideoView;
    private boolean mIsAspectRatio;
    private TrimContainer.VideoPauseListener mVideoPauseListener;
    private TrimContainer mContainer;

    public PageTrim(PictureSelectionConfig config, LocalMedia media, VideoView videoView, boolean isAspectRatio, TrimContainer.VideoPauseListener videoPauseListener) {
        mConfig = config;
        mMedia = media;
        mVideoView = videoView;
        mIsAspectRatio = isAspectRatio;
        mVideoPauseListener = videoPauseListener;
    }

    @Override
    public View getView(Context context) {
        mContainer = new TrimContainer(context, mConfig, mMedia, mVideoView, mVideoPauseListener);
        return mContainer;
    }

    @Override
    public void refreshData(Context context) {

    }

    @Override
    public void init(int position, ViewGroup parent) {

    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.trim);
    }

    @Override
    public Rect disallowInterceptTouchRect() {
        return null;
    }

    public long getStartTime() {
        if (mContainer != null) {
            return mContainer.getStartTime();
        }
        return 0;
    }

    public void resetStartLine() {
        if (mContainer != null) {
            mContainer.resetStartLine();
        }
    }

    public void trimVideo(InstagramMediaProcessActivity activity, CountDownLatch count) {
        if (mContainer != null) {
//            mContainer.trimVideo(activity, count);
            mContainer.cropVideo(activity, mIsAspectRatio);
        }
    }

    public void playVideo(boolean isPlay, VideoView videoView) {
        if (mContainer != null) {
            mContainer.playVideo(isPlay, videoView);
        }
    }

    @Override
    public void onResume() {
        if (mContainer != null) {
            mContainer.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mContainer != null) {
            mContainer.onPause();
        }
    }

    @Override
    public void onDestroy() {
        if (mContainer != null) {
            mContainer.onDestroy();
            mContainer = null;
        }
    }
}
