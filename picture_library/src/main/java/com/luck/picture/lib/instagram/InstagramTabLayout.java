package com.luck.picture.lib.instagram;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.luck.picture.lib.R;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.tools.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;

/**
 * ================================================
 * Created by JessYan on 2020/4/14 11:05
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InstagramTabLayout extends FrameLayout {
    private List<String> titles = new ArrayList<>();
    private List<View> tabViews = new ArrayList<>();
    private int selectedIndicatorHeight;
    private int layoutHeight;
    private final Paint selectedIndicatorPaint;
    private final GradientDrawable defaultSelectionIndicator;
    private int indicatorLeft = -1;
    private int indicatorRight = -1;
    private int tabWidth;
    private PictureSelectionConfig config;

    public InstagramTabLayout(Context context, List<Page> items, PictureSelectionConfig config) {
        super(context);
        this.config = config;
        fillTitles(items);
        installTabView(context, titles);
        setWillNotDraw(false);
        selectedIndicatorPaint = new Paint();
        defaultSelectionIndicator = new GradientDrawable();
        selectedIndicatorHeight = ScreenUtils.dip2px(context, 1);

        if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK) {
            selectedIndicatorPaint.setColor(ContextCompat.getColor(context, R.color.picture_color_white));
        } else if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK_BLUE){
            selectedIndicatorPaint.setColor(Color.parseColor("#2FA6FF"));
        } else {
            selectedIndicatorPaint.setColor(ContextCompat.getColor(context, R.color.picture_color_black));
        }

        selectTab(0);
    }

    private void installTabView(Context context, List<String> titles) {
        for (int i = 0; i < titles.size(); i++) {
            TextView tabView = new TextView(context);
            tabView.setTextSize(15);
            if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK) {
                tabView.setTextColor(Color.parseColor("#9B9B9D"));
            } else if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK_BLUE){
                tabView.setTextColor(Color.parseColor("#7E93A0"));
            } else {
                tabView.setTextColor(Color.parseColor("#92979F"));
            }
            tabView.setGravity(Gravity.CENTER);
            tabView.setText(titles.get(i));
            addView(tabView);
            tabViews.add(tabView);
        }
    }

    private void fillTitles(List<Page> items) {
        for (Page item : items) {
            if (item != null) {
                String title = item.getTitle(getContext());
                if (!TextUtils.isEmpty(title)) {
                    titles.add(title);
                } else {
                    throw new IllegalStateException("getTitle(Context) is null!");
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int measuredHeight = layoutHeight > 0 ? layoutHeight : ScreenUtils.dip2px(getContext(), 44);
        if (!tabViews.isEmpty()) {
            tabWidth = width / tabViews.size();
            if (indicatorLeft == -1) {
                indicatorLeft = 0;
                indicatorRight = tabWidth;
            }

            for (View view : tabViews) {
                view.measure(MeasureSpec.makeMeasureSpec(tabWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(measuredHeight - selectedIndicatorHeight, MeasureSpec.EXACTLY));
            }
        }

        setMeasuredDimension(width, measuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int viewTop = 0;
        int viewLeft;

        if (!tabViews.isEmpty()) {
            for (int i = 0; i < tabViews.size(); i++) {
                viewLeft = i * tabWidth;
                View view = tabViews.get(i);
                view.layout(viewLeft, viewTop, viewLeft + view.getMeasuredWidth(), viewTop + view.getMeasuredHeight());
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (indicatorLeft >= 0 && indicatorRight > indicatorLeft) {
            Drawable selectedIndicator;
            selectedIndicator =
                    DrawableCompat.wrap(defaultSelectionIndicator);
            selectedIndicator.setBounds(indicatorLeft, getMeasuredHeight() - selectedIndicatorHeight, indicatorRight, getMeasuredHeight());
            if (selectedIndicatorPaint != null) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                    // Drawable doesn't implement setTint in API 21
                    selectedIndicator.setColorFilter(
                            selectedIndicatorPaint.getColor(), PorterDuff.Mode.SRC_IN);
                } else {
                    DrawableCompat.setTint(selectedIndicator, selectedIndicatorPaint.getColor());
                }
            }
            selectedIndicator.draw(canvas);
        }
    }

    public int getTabSize() {
        return tabViews.size();
    }

    public void setIndicatorPosition(int position, float positionOffset) {
        int left = position * tabWidth;
        if (positionOffset > 0) {
            float offset = positionOffset * tabWidth;
            left += offset;
        }
        setIndicatorPosition(left, left + tabWidth);
    }

    public void setIndicatorPosition(int left) {
        setIndicatorPosition(left, left + tabWidth);
    }

    public void setIndicatorPosition(int left, int right) {
        if (left != indicatorLeft || right != indicatorRight) {
            // If the indicator's left/right has changed, invalidate
            indicatorLeft = left;
            indicatorRight = right;
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void selectTab(int position) {
        if (position < 0 || position >= tabViews.size()) {
            return;
        }
        for (int i = 0; i < tabViews.size(); i++) {
            View tabView = tabViews.get(i);
            if (position == i) {
                if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK) {
                    ((TextView) tabView).setTextColor(ContextCompat.getColor(getContext(), R.color.picture_color_white));
                } else if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK_BLUE){
                    ((TextView) tabView).setTextColor(ContextCompat.getColor(getContext(), R.color.picture_color_white));
                } else {
                    ((TextView) tabView).setTextColor(ContextCompat.getColor(getContext(), R.color.picture_color_black));
                }
            } else {
                if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK) {
                    ((TextView) tabView).setTextColor(Color.parseColor("#9B9B9D"));
                } else if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK_BLUE){
                    ((TextView) tabView).setTextColor(Color.parseColor("#7E93A0"));
                } else {
                    ((TextView) tabView).setTextColor(Color.parseColor("#92979F"));
                }
            }
        }
    }

    public void setLayoutHeight(int layoutHeight) {
        this.layoutHeight = layoutHeight;
    }
}
