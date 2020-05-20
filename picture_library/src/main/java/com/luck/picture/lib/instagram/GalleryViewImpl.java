package com.luck.picture.lib.instagram;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ================================================
 * Created by JessYan on 2020/3/30 16:14
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class GalleryViewImpl extends RecyclerView implements GalleryView {
    private int startedTrackingY;
    private VelocityTracker velocityTracker;

    public GalleryViewImpl(@NonNull Context context) {
        super(context);
        setOverScrollMode(OVER_SCROLL_NEVER);
        addOnScrollListener(new OnScrollListener() {
            int state;

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                state = newState;
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (state == SCROLL_STATE_SETTLING) {
                    if (isScrollTop()) {
                        ((InstagramGallery) getParent()).expandPreview();
                    }
                }
            }
        });
    }

    @Override
    public boolean isScrollTop() {
        View child = getChildAt(0);
        if (child != null) {
            RecyclerView.ViewHolder holder = findContainingViewHolder(child);
            if (holder != null && holder.getAdapterPosition() == 0) {
                int top = child.getTop();
                if (top >= 0) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startedTrackingY = (int) event.getY();
            if (velocityTracker != null) {
                velocityTracker.clear();
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain();
            }
            velocityTracker.addMovement(event);
            if (startedTrackingY == 0) {
                startedTrackingY = (int) event.getY();
            }
            float dy = (int) event.getY() - startedTrackingY;

            if (dy > 0 && isScrollTop()) {
                getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }

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

            if (Math.abs(velY) >= 5000) {
                if (velY <= 0) {
                    ((InstagramGallery) getParent()).closePreview();
                }
            }
        }
        return super.onTouchEvent(event);
    }
}
