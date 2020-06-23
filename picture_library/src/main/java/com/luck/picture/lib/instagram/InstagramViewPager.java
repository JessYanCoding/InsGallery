package com.luck.picture.lib.instagram;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;

import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.tools.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * ================================================
 * Created by JessYan on 2020/3/26 15:37
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InstagramViewPager extends FrameLayout {
    private int startedTrackingX;
    private int startedTrackingY;
    private float scrollHorizontalPosition;
    private VelocityTracker velocityTracker;
    private android.view.animation.Interpolator Interpolator = new AccelerateInterpolator();
    private int interceptY;
    private int interceptX;
    private List<Page> mItems = new ArrayList<>();
    private List<View> mViews = new ArrayList<>();
    private int mCurrentPosition;
    private int mSelectedPosition;
    private InstagramTabLayout mTabLayout;
    boolean click;
    int startClickX;
    int startClickY;
    long time;
    private AnimatorSet mAnimatorSet;
    private OnPageChangeListener mOnPageChangeListener;
    private int skipRange;
    private boolean scrollEnable = true;
    private boolean isDisplayTabLayout = true;

    public InstagramViewPager(@NonNull Context context) {
        super(context);
    }

    public InstagramViewPager(@NonNull Context context, List<Page> items, PictureSelectionConfig config) {
        super(context);
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items is isEmpty!");
        }
        mItems.addAll(items);
        installView(items);
        mItems.get(0).init(0, this);
        mViews.get(0).setTag(true);
        mItems.get(0).refreshData(context);
        mTabLayout = new InstagramTabLayout(context, items, config);
        addView(mTabLayout);
    }

    public void installView(List<Page> items) {
        for (Page item : items) {
            if (item != null) {
                View view = item.getView(getContext());
                if (view != null) {
                    addView(view);
                    mViews.add(view);
                } else {
                    throw new IllegalStateException("getView(Context) is null!");
                }
            }
        }
    }

    public void addPage(Page page) {
        if (page != null) {
            mItems.add(page);
        }
    }

    public void onResume() {
        for (Page item : mItems) {
            item.onResume();
        }
    }

    public void onPause() {
        for (Page item : mItems) {
            item.onPause();
        }
    }

    public void onDestroy() {
        for (Page item : mItems) {
            item.onDestroy();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int childHeight = height;
        if (mTabLayout.getVisibility() == VISIBLE) {
            measureChild(mTabLayout, widthMeasureSpec, heightMeasureSpec);
            childHeight -= mTabLayout.getMeasuredHeight();
        }
        for (View view : mViews) {
            view.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY));
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;
        int viewTop = 0;
        int viewLeft;

        for (int i = 0; i < mViews.size(); i++) {
            viewLeft = i * getMeasuredWidth();
            View view = mViews.get(i);
            view.layout(viewLeft, viewTop, viewLeft + view.getMeasuredWidth(), viewTop + view.getMeasuredHeight());
        }

        if (mTabLayout.getVisibility() == VISIBLE) {
            viewLeft = 0;
            viewTop = getMeasuredHeight() - mTabLayout.getMeasuredHeight();
            mTabLayout.layout(viewLeft, viewTop, viewLeft + mTabLayout.getMeasuredWidth(), viewTop + mTabLayout.getMeasuredHeight());
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!scrollEnable) {
            return false;
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            interceptX = (int) ev.getX();
            interceptY = (int) ev.getY();
            return false;
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (mViews.size() < 2) {
                return false;
            }

            Rect rect = mItems.get(mCurrentPosition).disallowInterceptTouchRect();
            if (rect != null && rect.contains((int) (ev.getX()), (int) (ev.getY()))) {
                return false;
            }

            float dx = (int) ev.getX() - interceptX;
            float dy = (int) ev.getY() - interceptY;
            if (ev.getPointerCount() < 2 && Math.abs(dx) > ScreenUtils.dip2px(getContext(), 3) && Math.abs(dy) < ScreenUtils.dip2px(getContext(), 5)) {
                startedTrackingX = (int) ev.getX();
                startedTrackingY = (int) ev.getY();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startedTrackingX = (int) event.getX();
            startedTrackingY = (int) event.getY();
            if (velocityTracker != null) {
                velocityTracker.clear();
            }

            click = true;
            startClickX = (int) event.getX();
            startClickY = (int) event.getY();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain();
            }
            velocityTracker.addMovement(event);
            float dx = (int) (event.getX() - startedTrackingX);
            float dy = (int) event.getY() - startedTrackingY;

            if (scrollEnable) {
                moveByX(dx * 1.1f);
            }

            startedTrackingX = (int) event.getX();
            startedTrackingY = (int) event.getY();

            if (click && (Math.abs(event.getX() - startClickX) > ScreenUtils.dip2px(getContext(), 3) || Math.abs(event.getY() - startClickY) > ScreenUtils.dip2px(getContext(), 3))) {
                click = false;
            }
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain();
            }
            velocityTracker.computeCurrentVelocity(1000);

            float velX = velocityTracker.getXVelocity();

            if (velocityTracker != null) {
                velocityTracker.recycle();
                velocityTracker = null;
            }

            if (scrollEnable) {
                int triggerValue = getMeasuredWidth() / 2;
                int position = (int) (Math.abs(scrollHorizontalPosition) / getMeasuredWidth());
                if (Math.abs(scrollHorizontalPosition) % getMeasuredWidth() >= triggerValue) {
                    position++;
                }

                int destination = getDestination(position);

                if (Math.abs(velX) >= 500) {
                    if (velX <= 0) {
                        startChildAnimation(getDestination(mCurrentPosition), 150);
                    } else {
                        startChildAnimation(getDestination(mCurrentPosition - 1), 150);
                    }
                } else {
                    startChildAnimation(destination, 200);
                }
            }

            if (click) {
                Rect rect = new Rect();
                mTabLayout.getHitRect(rect);
                if (rect.contains((int) (event.getX()), (int) (event.getY()))) {
                    long elapsedRealtime = SystemClock.elapsedRealtime();
                    if (elapsedRealtime - time > 300) {
                        time = elapsedRealtime;
                        click = false;
                        if (mTabLayout.getTabSize() > 1) {
                            int tabWidth = getMeasuredWidth() / mTabLayout.getTabSize();
                            selectPagePosition((int) (event.getX() / tabWidth));
                        }
                    }
                }
            }
        }
        return true;
    }

    public void selectPagePosition(int position) {
        long duration = 150;
        int span = Math.abs(mCurrentPosition - position);
        if (span > 1) {
            duration += (span - 1) * 80;
        }
        startChildAnimation(getDestination(position), duration);
    }

    private int getDestination(int position) {
        if (position < 0) {
            position = 0;
        }
        return -(position * getMeasuredWidth());
    }

    private void startChildAnimation(float destination, long duration) {
        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            mAnimatorSet.cancel();
        }
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(
                ObjectAnimator.ofFloat(this, "scrollHorizontalPosition", scrollHorizontalPosition, destination));
        mAnimatorSet.setInterpolator(Interpolator);
        mAnimatorSet.setDuration(duration);
        mAnimatorSet.start();
    }

    public void moveByX(float dx) {
        setScrollHorizontalPosition(scrollHorizontalPosition + dx);
    }


    public void setScrollHorizontalPosition(float value) {
        if (mViews.size() < 2) {
            return;
        }
        float oldHorizontalPosition = scrollHorizontalPosition;
        if (value < -(getMeasuredWidth() * (mViews.size() - 1))) {
            scrollHorizontalPosition = -(getMeasuredWidth() * (mViews.size() - 1));
        } else if (value > 0) {
            scrollHorizontalPosition = 0;
        } else {
            scrollHorizontalPosition = value;
        }

        if (oldHorizontalPosition == scrollHorizontalPosition) {
            return;
        }

        boolean isTranslationX = skipRange <= 0 || scrollHorizontalPosition >= -(getMeasuredWidth() * (mViews.size() - (1 + skipRange)));

        if (isTranslationX) {
            for (View view : mViews) {
                view.setTranslationX(scrollHorizontalPosition);
            }
        } else {
            if (mViews.get(0).getTranslationX() != -(getMeasuredWidth() * (mViews.size() - (1 + skipRange)))) {
                for (View view : mViews) {
                    view.setTranslationX(-(getMeasuredWidth() * (mViews.size() - (1 + skipRange))));
                }
            }
        }

        int position = (int) (Math.abs(scrollHorizontalPosition) / getMeasuredWidth());
        float offset = Math.abs(scrollHorizontalPosition) % getMeasuredWidth();

        mTabLayout.setIndicatorPosition(position, offset / getMeasuredWidth());

        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrolled(position, offset / getMeasuredWidth(), (int) offset);
        }

        if (offset == 0) {
            mSelectedPosition = position;
            mTabLayout.selectTab(position);
            if (mOnPageChangeListener != null) {
                mOnPageChangeListener.onPageSelected(position);
            }
            mItems.get(position).refreshData(getContext());
        }

        if (offset > 0) {
            position++;
        }

        mCurrentPosition = position;
        View currentView = mViews.get(position);
        Object tag = currentView.getTag();
        boolean isInti = false;
        if (tag instanceof Boolean) {
            isInti = (boolean) tag;
        }
        if (!isInti) {
            mItems.get(position).init(position, this);
            currentView.setTag(true);
        }
    }

    public void setSkipRange(int skipRange) {
        if (skipRange < 1 || skipRange >= mItems.size()) {
            return;
        }
        this.skipRange = skipRange;
    }

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        mOnPageChangeListener = onPageChangeListener;
    }

    public void setScrollEnable(boolean scrollEnable) {
        this.scrollEnable = scrollEnable;
    }

    public void displayTabLayout(boolean isDisplay) {
        if (isDisplayTabLayout == isDisplay) {
            return;
        }
        isDisplayTabLayout = isDisplay;
        if (isDisplay) {
            InstagramUtils.setViewVisibility(mTabLayout, View.VISIBLE);
        } else {
            InstagramUtils.setViewVisibility(mTabLayout, View.GONE);
        }
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }
}
