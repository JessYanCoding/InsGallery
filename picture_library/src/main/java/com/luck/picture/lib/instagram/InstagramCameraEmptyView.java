package com.luck.picture.lib.instagram;

import android.content.Context;
import android.graphics.Color;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.luck.picture.lib.R;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.permissions.PermissionChecker;
import com.luck.picture.lib.tools.ScreenUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * ================================================
 * Created by JessYan on 2020/4/24 17:43
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InstagramCameraEmptyView extends FrameLayout {
    private TextView mTitleView;
    private TextView mContentView;
    private TextView mActionView;

    public InstagramCameraEmptyView(@NonNull Context context, PictureSelectionConfig config) {
        super(context);

        if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK) {
            setBackgroundColor(Color.parseColor("#1C1C1E"));
        } else if(config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK_BLUE) {
            setBackgroundColor(Color.parseColor("#213040"));
        } else {
            setBackgroundColor(ContextCompat.getColor(context, R.color.picture_color_262626));
        }

        mTitleView = new TextView(context);
        mTitleView.setTextSize(20);
        mTitleView.setTextColor(ContextCompat.getColor(context, R.color.picture_color_c7c7c7));
        mTitleView.setText(R.string.camera_access);
        addView(mTitleView);

        mContentView = new TextView(context);
        mContentView.setTextSize(17);
        mContentView.setTextColor(ContextCompat.getColor(context, R.color.picture_color_light_grey));
        mContentView.setText(R.string.camera_access_content);
        addView(mContentView);

        mActionView = new TextView(context);
        mActionView.setTextSize(17);
        mActionView.setTextColor(ContextCompat.getColor(context, R.color.picture_color_3c98ea));
        mActionView.setText(R.string.enable);
        mActionView.setOnClickListener((v -> PermissionChecker.launchAppDetailsSettings(getContext())));
        addView(mActionView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        mTitleView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 30), MeasureSpec.AT_MOST));
        mContentView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 30), MeasureSpec.AT_MOST));
        mActionView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 30), MeasureSpec.AT_MOST));
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int viewTop = (getMeasuredHeight() - mContentView.getMeasuredHeight()) / 2;
        int viewLeft = (getMeasuredWidth() - mContentView.getMeasuredWidth()) / 2;
        mContentView.layout(viewLeft, viewTop, viewLeft + mContentView.getMeasuredWidth(), viewTop + mContentView.getMeasuredHeight());

        viewTop -= ScreenUtils.dip2px(getContext(), 15) + mTitleView.getMeasuredHeight();
        viewLeft = (getMeasuredWidth() - mTitleView.getMeasuredWidth()) / 2;
        mTitleView.layout(viewLeft, viewTop, viewLeft + mTitleView.getMeasuredWidth(), viewTop + mTitleView.getMeasuredHeight());

        viewTop = mContentView.getBottom() + ScreenUtils.dip2px(getContext(), 15);
        viewLeft = (getMeasuredWidth() - mActionView.getMeasuredWidth()) / 2;
        mActionView.layout(viewLeft, viewTop, viewLeft + mActionView.getMeasuredWidth(), viewTop + mActionView.getMeasuredHeight());
    }
}
