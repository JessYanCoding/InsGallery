package com.luck.picture.lib.instagram.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.instagram.InsGallery;
import com.luck.picture.lib.instagram.filter.FilterType;
import com.luck.picture.lib.tools.ScreenUtils;

import androidx.annotation.NonNull;

/**
 * ================================================
 * Created by JessYan on 2020/6/2 15:35
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class FilterItemView extends FrameLayout {
    private TextView mTitleView;
    private PictureSelectionConfig mConfig;
    private ImageView mImageView;

    public FilterItemView(@NonNull Context context, PictureSelectionConfig config) {
        super(context);
        mConfig = config;
        mTitleView = new TextView(context);
        mTitleView.setTextColor(Color.parseColor("#999999"));
        mTitleView.setTextSize(12);
        mTitleView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        mTitleView.setGravity(Gravity.CENTER);
        addView(mTitleView);

        mImageView = new ImageView(context);
        mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(mImageView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = ScreenUtils.dip2px(getContext(), 100);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        mTitleView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
        mImageView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY));
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int viewLeft = (getMeasuredWidth() - mTitleView.getMeasuredWidth()) / 2;
        int viewTop = (getMeasuredHeight() - mTitleView.getMeasuredHeight() - mImageView.getMeasuredHeight() - ScreenUtils.dip2px(getContext(), 5)) / 2;
        mTitleView.layout(viewLeft, viewTop, viewLeft + mTitleView.getMeasuredWidth(), viewTop + mTitleView.getMeasuredHeight());

        viewLeft = 0;
        viewTop = viewTop + ScreenUtils.dip2px(getContext(), 5) + mTitleView.getMeasuredHeight();
        mImageView.layout(viewLeft, viewTop, viewLeft + mImageView.getMeasuredWidth(), viewTop + mImageView.getMeasuredHeight());
    }

    public void selection(boolean isSelection) {
        if (isSelection) {
            if (mConfig.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DEFAULT) {
                mTitleView.setTextColor(Color.parseColor("#262626"));
            } else {
                mTitleView.setTextColor(Color.parseColor("#fafafa"));
            }
            setTranslationY(-ScreenUtils.dip2px(getContext(), 10));
        } else {
            setTranslationY(0);
            mTitleView.setTextColor(Color.parseColor("#999999"));
        }
    }

    public void refreshFilter(FilterType filterType, Bitmap bitmap, int position, int selectionPosition) {
        if (position == selectionPosition) {
            selection(true);
        } else {
            selection(false);
        }
        mTitleView.setText(filterType.getName());
        mImageView.setImageBitmap(bitmap);
    }
}
