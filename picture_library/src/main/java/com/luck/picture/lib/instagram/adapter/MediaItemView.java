package com.luck.picture.lib.instagram.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.luck.picture.lib.R;
import com.luck.picture.lib.instagram.CombinedDrawable;
import com.luck.picture.lib.instagram.InstagramUtils;
import com.luck.picture.lib.tools.ScreenUtils;

import androidx.annotation.NonNull;

/**
 * ================================================
 * Created by JessYan on 2020/6/10 10:25
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class MediaItemView extends FrameLayout {
    private ImageView mImageView;
    private ImageView mIconView;

    public MediaItemView(@NonNull Context context) {
        super(context);
        mImageView = new ImageView(context);
        mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(mImageView);

        mIconView = new ImageView(context);
        CombinedDrawable iconDrawable = InstagramUtils.createCircleDrawableWithIcon(context, ScreenUtils.dip2px(context, 30), R.drawable.discover_filter);
        InstagramUtils.setCombinedDrawableColor(iconDrawable, Color.BLACK, true);
        mIconView.setImageDrawable(iconDrawable);
        addView(mIconView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(heightMeasureSpec) - ScreenUtils.dip2px(getContext(), 75);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        mImageView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY));
        mIconView.measure(MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 30), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 30), MeasureSpec.EXACTLY));
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int viewTop = (getMeasuredHeight() - mImageView.getMeasuredHeight()) / 2;
        int viewLeft = (getMeasuredWidth() - mImageView.getMeasuredWidth()) / 2;
        mImageView.layout(viewLeft, viewTop, viewLeft + mImageView.getMeasuredWidth(), viewTop + mImageView.getMeasuredHeight());

        viewTop = viewTop + mImageView.getMeasuredHeight() - ScreenUtils.dip2px(getContext(), 12) - mIconView.getMeasuredHeight();
        viewLeft = ScreenUtils.dip2px(getContext(), 12);
        mIconView.layout(viewLeft, viewTop, viewLeft + mIconView.getMeasuredWidth(), viewTop + mIconView.getMeasuredHeight());
    }

    public void setImage(Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
    }
}
