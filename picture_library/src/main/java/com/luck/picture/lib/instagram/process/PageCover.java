package com.luck.picture.lib.instagram.process;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

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
public class PageCover implements Page {
    private PictureSelectionConfig mConfig;
    private LocalMedia mMedia;
    private CoverContainer mContainer;
    private Context mContext;
    private CoverContainer.onSeekListener mOnSeekListener;

    public PageCover(PictureSelectionConfig config, LocalMedia media) {
        mConfig = config;
        mMedia = media;
    }

    @Override
    public View getView(Context context) {
        mContainer = new CoverContainer(context, mMedia);
        mContext = context;
        return mContainer;
    }

    @Override
    public void refreshData(Context context) {

    }

    @Override
    public void init(int position, ViewGroup parent) {
        if (mContainer != null) {
            mContainer.getFrame(mContext, mMedia);
            mContainer.setOnSeekListener(mOnSeekListener);
        }
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.cover);
    }

    @Override
    public Rect disallowInterceptTouchRect() {
        return null;
    }

    public void setOnSeekListener(CoverContainer.onSeekListener onSeekListener) {
        mOnSeekListener = onSeekListener;
    }

    public void cropCover(CountDownLatch count) {
        if (mContainer != null) {
            mContainer.cropCover(count);
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
