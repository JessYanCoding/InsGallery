package com.luck.picture.lib.instagram.process;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.luck.picture.lib.R;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.instagram.InsGallery;
import com.luck.picture.lib.instagram.InstagramUtils;
import com.luck.picture.lib.tools.ScreenUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * ================================================
 * Created by JessYan on 2020/6/11 18:16
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class ProcessAlertView extends FrameLayout {
    private TextView mTitleView;
    private TextView mSubtitleView;
    private TextView mAgreeView;
    private TextView mCancelView;
    private Paint mPaint;
    private onAlertListener mOnAlertListener;


    public ProcessAlertView(@NonNull Context context, PictureSelectionConfig config) {
        super(context);
        setWillNotDraw(false);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DEFAULT) {
            mPaint.setColor(Color.parseColor("#dddddd"));
        } else {
            mPaint.setColor(Color.parseColor("#3f3f3f"));
        }

        Drawable background;
        if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK) {
            background = InstagramUtils.createRoundRectDrawable(ScreenUtils.dip2px(context, 4), Color.parseColor("#2c2c2c"));
        } else if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK_BLUE) {
            background = InstagramUtils.createRoundRectDrawable(ScreenUtils.dip2px(context, 4), Color.parseColor("#18222D"));
        } else {
            background = InstagramUtils.createRoundRectDrawable(ScreenUtils.dip2px(context, 4), Color.WHITE);
        }

        setBackground(background);

        mTitleView = new TextView(context);
        mTitleView.setGravity(Gravity.CENTER);
        mTitleView.setTextSize(18);
        mTitleView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DEFAULT) {
            mTitleView.setTextColor(Color.BLACK);
        } else {
            mTitleView.setTextColor(Color.WHITE);
        }
        mTitleView.setText(R.string.discard_edits);
        addView(mTitleView);

        mSubtitleView = new TextView(context);
        mSubtitleView.setPadding(ScreenUtils.dip2px(getContext(), 20), 0, ScreenUtils.dip2px(getContext(), 20), 0);
        mSubtitleView.setGravity(Gravity.CENTER);
        mSubtitleView.setTextSize(14);
        mSubtitleView.setTextColor(Color.GRAY);
        mSubtitleView.setText(R.string.discard_edits_alert);
        addView(mSubtitleView);

        mAgreeView = new TextView(context);
        mAgreeView.setGravity(Gravity.CENTER);
        mAgreeView.setTextSize(17);
         if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK_BLUE) {
             mAgreeView.setTextColor(Color.parseColor("#2FA6FF"));
         } else {
             mAgreeView.setTextColor(ContextCompat.getColor(context, R.color.picture_color_1766FF));
         }
        mAgreeView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        mAgreeView.setText(R.string.discard);
        addView(mAgreeView);
        mAgreeView.setOnClickListener(v -> {
            if (mOnAlertListener != null) {
                mOnAlertListener.onAgree();
            }
        });

        mCancelView = new TextView(context);
        mCancelView.setGravity(Gravity.CENTER);
        mCancelView.setTextSize(17);
        if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DEFAULT) {
            mCancelView.setTextColor(Color.BLACK);
        } else {
            mCancelView.setTextColor(Color.WHITE);
        }
        mCancelView.setText(R.string.picture_cancel);
        addView(mCancelView);
        mCancelView.setOnClickListener(v -> {
            if (mOnAlertListener != null) {
                mOnAlertListener.onCancel();
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec) - ScreenUtils.dip2px(getContext(), 60);
        int height = ScreenUtils.dip2px(getContext(), 210);
        mTitleView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
        mSubtitleView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
        mAgreeView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 50), MeasureSpec.EXACTLY));
        mCancelView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 50), MeasureSpec.EXACTLY));
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int viewTop = ScreenUtils.dip2px(getContext(), 30);
        int viewLeft = 0;
        mTitleView.layout(viewLeft, viewTop, viewLeft + mTitleView.getMeasuredWidth(), viewTop + mTitleView.getMeasuredHeight());

        viewTop = ScreenUtils.dip2px(getContext(), 10) + mTitleView.getBottom();
        mSubtitleView.layout(viewLeft, viewTop, viewLeft + mSubtitleView.getMeasuredWidth(), viewTop + mSubtitleView.getMeasuredHeight());

        viewTop = getMeasuredHeight() - mCancelView.getMeasuredHeight() - mAgreeView.getMeasuredHeight();
        mAgreeView.layout(viewLeft, viewTop, viewLeft + mAgreeView.getMeasuredWidth(), viewTop + mAgreeView.getMeasuredHeight());

        viewTop = getMeasuredHeight() - mCancelView.getMeasuredHeight();
        mCancelView.layout(viewLeft, viewTop, viewLeft + mCancelView.getMeasuredWidth(), viewTop + mCancelView.getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(0, getMeasuredHeight() - mCancelView.getMeasuredHeight() - mAgreeView.getMeasuredHeight(), getMeasuredWidth(), getMeasuredHeight() - mCancelView.getMeasuredHeight() - mAgreeView.getMeasuredHeight(), mPaint);
        canvas.drawLine(0, getMeasuredHeight() - mCancelView.getMeasuredHeight(), getMeasuredWidth(), getMeasuredHeight() - mCancelView.getMeasuredHeight(), mPaint);
    }

    public void setOnAlertListener(onAlertListener onAlertListener) {
        mOnAlertListener = onAlertListener;
    }

    public interface onAlertListener {
        void onAgree();
        void onCancel();
    }
}
