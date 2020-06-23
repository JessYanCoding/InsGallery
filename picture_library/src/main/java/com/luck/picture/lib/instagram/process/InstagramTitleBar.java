package com.luck.picture.lib.instagram.process;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.luck.picture.lib.R;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.instagram.InsGallery;
import com.luck.picture.lib.tools.ScreenUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * ================================================
 * Created by JessYan on 2020/5/29 11:51
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InstagramTitleBar extends FrameLayout {
    private ImageView mLeftView;
    private ImageView mCenterView;
    private TextView mRightView;
    private OnTitleBarItemOnClickListener mClickListener;

    public InstagramTitleBar(@NonNull Context context, PictureSelectionConfig config, InstagramMediaProcessActivity.MediaType mediaType) {
        super(context);
        mLeftView = new ImageView(context);
        mLeftView.setImageResource(R.drawable.discover_return);
        mLeftView.setPadding(ScreenUtils.dip2px(context, 15), 0, ScreenUtils.dip2px(context, 15), 0);
        mLeftView.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onLeftViewClick();
            }
        });
        addView(mLeftView);

        mCenterView = new ImageView(context);
        mCenterView.setPadding(ScreenUtils.dip2px(context, 10), 0, ScreenUtils.dip2px(context, 10), 0);
        mCenterView.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onCenterViewClick(mCenterView);
            }
        });
        addView(mCenterView);
        switch (mediaType) {
            case SINGLE_VIDEO:
                mCenterView.setImageResource(R.drawable.discover_volume_off);
                break;
            default:
                mCenterView.setVisibility(View.GONE);
                break;
        }

        if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DEFAULT) {
            mLeftView.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.picture_color_black), PorterDuff.Mode.MULTIPLY));
            mCenterView.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.picture_color_black), PorterDuff.Mode.MULTIPLY));
        }

        mRightView = new TextView(context);
        mRightView.setPadding(ScreenUtils.dip2px(context, 10), 0, ScreenUtils.dip2px(context, 10), 0);
        int textColor;
        if (config.style.pictureRightDefaultTextColor != 0) {
            textColor = config.style.pictureRightDefaultTextColor;
        } else {
            if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK) {
                textColor = ContextCompat.getColor(context, R.color.picture_color_1766FF);
            } else if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK_BLUE) {
                textColor = Color.parseColor("#2FA6FF");
            } else {
                textColor = ContextCompat.getColor(context, R.color.picture_color_1766FF);
            }
        }
        mRightView.setTextColor(textColor);
        mRightView.setTextSize(14);
        mRightView.setText(context.getString(R.string.next));
        mRightView.setGravity(Gravity.CENTER);
        mRightView.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onRightViewClick();
            }
        });
        addView(mRightView);
    }

    public void setRightViewText(String text) {
        mRightView.setText(text);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = ScreenUtils.dip2px(getContext(), 48);
        mLeftView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

        if (mCenterView.getVisibility() == View.VISIBLE) {
            mCenterView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }

        mRightView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int viewTop = (getMeasuredHeight() - mLeftView.getMeasuredHeight()) / 2;
        int viewLeft = 0;
        mLeftView.layout(viewLeft, viewTop, viewLeft + mLeftView.getMeasuredWidth(), viewTop + mLeftView.getMeasuredHeight());

        if (mCenterView.getVisibility() == View.VISIBLE) {
            viewTop = (getMeasuredHeight() - mCenterView.getMeasuredHeight()) / 2;
            viewLeft = (getMeasuredWidth() - mCenterView.getMeasuredWidth()) / 2;
            mCenterView.layout(viewLeft, viewTop, viewLeft + mCenterView.getMeasuredWidth(), viewTop + mCenterView.getMeasuredHeight());
        }

        viewTop = (getMeasuredHeight() - mRightView.getMeasuredHeight()) / 2;
        viewLeft = getMeasuredWidth() - mRightView.getMeasuredWidth();
        mRightView.layout(viewLeft, viewTop, viewLeft + mRightView.getMeasuredWidth(), viewTop + mRightView.getMeasuredHeight());

    }

    public void setClickListener(OnTitleBarItemOnClickListener clickListener) {
        mClickListener = clickListener;
    }

    public interface OnTitleBarItemOnClickListener {
        void onLeftViewClick();
        void onCenterViewClick(ImageView view);
        void onRightViewClick();
    }
}
