package com.luck.picture.lib.instagram;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.luck.picture.lib.R;

/**
 * ================================================
 * Created by JessYan on 2020/4/15 12:02
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class PageVideo implements Page {
    private PagePhoto mPagePhoto;

    public PageVideo(PagePhoto pagePhoto) {
        mPagePhoto = pagePhoto;
    }

    @Override
    public View getView(Context context) {
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Color.CYAN);
        return frameLayout;
    }

    @Override
    public void refreshData(Context context) {

    }

    @Override
    public void init(int position, ViewGroup parent) {

    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.video);
    }

    @Override
    public Rect disallowInterceptTouchRect() {
        return mPagePhoto.disallowInterceptTouchRect();
    }
}
