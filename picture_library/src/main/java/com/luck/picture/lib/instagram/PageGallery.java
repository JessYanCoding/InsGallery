package com.luck.picture.lib.instagram;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

import com.luck.picture.lib.R;

/**
 * ================================================
 * Created by JessYan on 2020/4/15 11:59
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class PageGallery implements Page {
    private InstagramGallery mGallery;

    public PageGallery(InstagramGallery gallery) {
        mGallery = gallery;
    }

    @Override
    public View getView(Context context) {
        return mGallery;
    }

    @Override
    public void refreshData(Context context) {

    }

    @Override
    public void init(int position, ViewGroup parent) {

    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.gallery);
    }

    @Override
    public Rect disallowInterceptTouchRect() {
        if (!mGallery.isScrollTop()) {
            Rect rect = new Rect();
            mGallery.getPreviewView().getHitRect(rect);
            return rect;
        }
        return null;
    }
}
