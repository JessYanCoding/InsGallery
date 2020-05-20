package com.luck.picture.lib.instagram;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.luck.picture.lib.R;
import com.luck.picture.lib.tools.ScreenUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * ================================================
 * Created by JessYan on 2020/3/26 15:37
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InstagramGallery extends FrameLayout {
    private ViewGroup mPreviewView;
    private ViewGroup mGalleryView;
    private int startedTrackingX;
    private int startedTrackingY;
    private float scrollPosition;
    private VelocityTracker velocityTracker;
    private Interpolator interpolator = new LinearInterpolator();
    private int galleryHeight;
    private int interceptY;
    private boolean scrollTop;
    private View maskView;
    private TextView emptyView;
    private int previewBottomMargin;
    private int viewVisibility = VISIBLE;
    private AnimatorSet mAnimatorSet;

    public InstagramGallery(@NonNull Context context) {
        super(context);
    }

    public InstagramGallery(@NonNull Context context, ViewGroup previewView, ViewGroup galleryView) {
        super(context);
        installView(previewView, galleryView);
    }

    public void installView(ViewGroup previewView, ViewGroup galleryView) {
        this.mPreviewView = previewView;
        this.mGalleryView = galleryView;
        if (previewView != null) {
            addView(previewView);
        }
        if (galleryView != null) {
            addView(galleryView);
        }
        installMaskView(getContext());
        installEmptyView(getContext());
    }

    private void installMaskView(@NonNull Context context) {
        maskView = new View(context);
        maskView.setBackgroundColor(0x99000000);
        addView(maskView);
        maskView.setVisibility(GONE);
        maskView.setOnClickListener(v -> startChildAnimation(false, 200));
    }

    private void installEmptyView(@NonNull Context context) {
        emptyView = new TextView(context);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setLineSpacing(ScreenUtils.dip2px(context, 3), 1.0f);
        emptyView.setTextSize(16);
        emptyView.setTextColor(ContextCompat.getColor(context, R.color.picture_color_aab2bd));
        emptyView.setText(context.getString(R.string.picture_empty));
        addView(emptyView);
        emptyView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (mPreviewView != null && mPreviewView.getVisibility() == VISIBLE) {
            mPreviewView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY));
        }

        if (mGalleryView != null && mGalleryView.getVisibility() == VISIBLE) {
            galleryHeight = mGalleryView.getLayoutParams().height;
            if (galleryHeight <= 0) {
                galleryHeight = getGalleryHeight(width, height);
            }
            mGalleryView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(galleryHeight, MeasureSpec.EXACTLY));
        }

        if (maskView.getVisibility() == VISIBLE) {
            maskView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(width - previewBottomMargin, MeasureSpec.EXACTLY));
        }

        if (emptyView.getVisibility() == VISIBLE) {
            emptyView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int viewTop = 0;
        int viewLeft = 0;
        if (maskView.getVisibility() == VISIBLE) {
            maskView.layout(viewLeft, viewTop, viewLeft + maskView.getMeasuredWidth(), viewTop + maskView.getMeasuredHeight());
        }
        if (mPreviewView != null && mGalleryView != null && mPreviewView.getVisibility() == VISIBLE && mGalleryView.getVisibility() == VISIBLE) {

            mPreviewView.layout(viewLeft, viewTop, viewLeft + mPreviewView.getMeasuredWidth(), viewTop + mPreviewView.getMeasuredHeight());

            viewTop += mPreviewView.getMeasuredHeight();
            mGalleryView.layout(viewLeft, viewTop, viewLeft + mGalleryView.getMeasuredWidth(), viewTop + mGalleryView.getMeasuredHeight());
        }

        if (emptyView.getVisibility() == VISIBLE) {
            viewTop = (getMeasuredHeight() - emptyView.getMeasuredHeight()) / 2;
            viewLeft = (getMeasuredWidth() - emptyView.getMeasuredWidth()) / 2;
            emptyView.layout(viewLeft, viewTop, viewLeft + emptyView.getMeasuredWidth(), viewTop + emptyView.getMeasuredHeight());
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (viewVisibility != VISIBLE) {
            return super.onInterceptTouchEvent(ev);
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            Rect rect = new Rect();
            mPreviewView.getHitRect(rect);
            rect.set(rect.left, rect.bottom - ScreenUtils.dip2px(getContext(), 15), rect.right, rect.bottom + ScreenUtils.dip2px(getContext(), 15));
            if (rect.contains((int) (ev.getX()), (int) (ev.getY()))) {
                return true;
            } else {
                interceptY = (int) ev.getY();
                return false;
            }
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            Rect rect = new Rect();
            mGalleryView.getHitRect(rect);
            if (mGalleryView instanceof GalleryView && rect.contains((int) (ev.getX()), (int) (ev.getY()))) {
                float dy = (int) ev.getY() - interceptY;
                if (dy > 5 && ((GalleryView) mGalleryView).isScrollTop()) {
                    startedTrackingY = (int) ev.getY();
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        } else if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            return false;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mPreviewView == null || mGalleryView == null || viewVisibility != VISIBLE) {
            return super.onTouchEvent(event);
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startedTrackingX = (int) event.getX();
            startedTrackingY = (int) event.getY();
            if (velocityTracker != null) {
                velocityTracker.clear();
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain();
            }
            velocityTracker.addMovement(event);
            float dx = (int) (event.getX() - startedTrackingX);
            float dy = (int) event.getY() - startedTrackingY;

            if (scrollPosition >= 0) {
                moveByY(dy * 0.25f);
            } else {
                moveByY(dy);
            }

            measureGallerayHeight(dy);

            startedTrackingY = (int) event.getY();
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain();
            }
            velocityTracker.computeCurrentVelocity(1000);

            float velY = velocityTracker.getYVelocity();

            if (velocityTracker != null) {
                velocityTracker.recycle();
                velocityTracker = null;
            }

            int triggerValue = getMeasuredWidth() / 2;

            if (Math.abs(velY) >= 3500) {
                if (velY <= 0) {
                    startChildAnimation(true, 150);
                } else {
                    startChildAnimation(false, 150);
                }
            } else {
                if (scrollPosition <= -triggerValue) {
                    startChildAnimation(true, 200);
                } else {
                    startChildAnimation(false, 200);
                }
            }
        }
        return true;
    }

    private void startChildAnimation(boolean scrollTop, long duration) {
        startChildAnimation(scrollTop, duration, null);
    }

    private void startChildAnimation(final boolean scrollTop, long duration, AnimationCallback callback) {
        this.scrollTop = scrollTop;
        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            mAnimatorSet.cancel();
        }
        mAnimatorSet = new AnimatorSet();
        if (scrollTop) {
            setGalleryHeight(getMeasuredHeight() - getPreviewFoldHeight());
            mAnimatorSet.playTogether(
                    ObjectAnimator.ofFloat(this, "scrollPosition", scrollPosition, -(getMeasuredWidth() - getPreviewFoldHeight())));
        } else {
            mAnimatorSet.playTogether(
                    ObjectAnimator.ofFloat(this, "scrollPosition", scrollPosition, 0),
                    ObjectAnimator.ofInt(this, "galleryHeight", galleryHeight, getGalleryHeight(getMeasuredWidth(), getMeasuredHeight())));
        }
        mAnimatorSet.setDuration(duration);
        mAnimatorSet.setInterpolator(interpolator);
        if (callback != null) {
            mAnimatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    callback.onAnimationStart();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    callback.onAnimationEnd();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        mAnimatorSet.start();
    }

    private void measureGallerayHeight(float dy) {
        int height = mGalleryView.getLayoutParams().height;
        if (height <= 0) {
            height = getGalleryHeight(getMeasuredWidth(), getMeasuredHeight());
        }
        if (dy < 0) {
            height += Math.abs(dy);
            if (height > getMeasuredHeight() - getPreviewFoldHeight()) {
                height = getMeasuredHeight() - getPreviewFoldHeight();
            }
        } else if (dy > 0) {
            height -= Math.abs(dy);
            if (height < getGalleryHeight(getMeasuredWidth(), getMeasuredHeight())) {
                height = getGalleryHeight(getMeasuredWidth(), getMeasuredHeight());
            }
        }

        mGalleryView.getLayoutParams().height = height;
        mGalleryView.requestLayout();
    }

    public void moveByY(float dy) {
        setScrollPosition(scrollPosition + dy);
    }

    public void setGalleryHeight(int galleryHeight) {
        mGalleryView.getLayoutParams().height = galleryHeight;
        mGalleryView.requestLayout();
    }

    public void setScrollPosition(float value) {
        float oldScrollPosition = scrollPosition;
        if (value < -(getMeasuredWidth() - getPreviewFoldHeight())) {
            scrollPosition = -(getMeasuredWidth() - getPreviewFoldHeight());
        } else {
            scrollPosition = value;
        }

        if (oldScrollPosition == scrollPosition) {
            return;
        }

        mPreviewView.setTranslationY(scrollPosition);
        mGalleryView.setTranslationY(scrollPosition);
        maskView.setTranslationY(scrollPosition);

        if (scrollPosition < 0) {
            maskView.setAlpha(Math.abs(scrollPosition) / (getMeasuredWidth() - getPreviewFoldHeight()));

            if (maskView.getVisibility() != VISIBLE) {
                maskView.setVisibility(View.VISIBLE);
            }
        } else if (scrollPosition == 0) {
            if (maskView.getVisibility() != View.GONE) {
                maskView.setVisibility(View.GONE);
            }
        }

        if (scrollPosition <= -(getMeasuredWidth() - getPreviewFoldHeight())) {
            scrollTop = true;
        } else {
            scrollTop = false;
        }
    }

    public void expandPreview() {
        if (scrollTop) {
            startChildAnimation(false, 200);
        }
    }

    public void expandPreview(AnimationCallback callback) {
        if (scrollTop) {
            startChildAnimation(false, 200, callback);
        }
    }

    public void closePreview() {
        if (!scrollTop) {
            startChildAnimation(true, 200);
        }
    }

    public boolean isScrollTop() {
        return scrollTop;
    }

    public void setPreviewBottomMargin(int previewBottomMargin) {
        this.previewBottomMargin = previewBottomMargin;
    }

    private int getGalleryHeight(int ParentWidth, int ParentHeight) {
        return ParentHeight - ParentWidth;
    }

    private int getPreviewFoldHeight() {
        return ScreenUtils.dip2px(getContext(), 60);
    }

    public ViewGroup getPreviewView() {
        return mPreviewView;
    }

    public ViewGroup getGalleryView() {
        return mGalleryView;
    }

    public TextView getEmptyView() {
        return emptyView;
    }

    public void setViewVisibility(int visibility) {
        viewVisibility = visibility;
        InstagramUtils.setViewVisibility(mPreviewView, visibility);
        InstagramUtils.setViewVisibility(mGalleryView, visibility);
    }

    public interface AnimationCallback {
        void onAnimationStart();

        void onAnimationEnd();
    }

    public void setInitGalleryHeight() {
        mGalleryView.getLayoutParams().height = -1;
    }
}
