package com.luck.picture.lib.instagram.process;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.instagram.InsGallery;
import com.luck.picture.lib.instagram.adapter.InstagramFrameItemDecoration;
import com.luck.picture.lib.instagram.adapter.VideoTrimmerAdapter;
import com.luck.picture.lib.tools.ScreenUtils;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ================================================
 * Created by JessYan on 2020/6/24 17:07
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class TrimContainer extends FrameLayout {
    private RecyclerView mRecyclerView;
    private LocalMedia mMedia;
    private final VideoTrimmerAdapter mVideoTrimmerAdapter;
    private getAllFrameTask mFrameTask;
    private final VideoRulerView mVideoRulerView;
    private final RangeSeekBarView mRangeSeekBarView;
    private View mLeftShadow;
    private View mRightShadow;
    private int mScrollX;
    private int mThumbsCount;

    public TrimContainer(@NonNull Context context, PictureSelectionConfig config, LocalMedia media) {
        super(context);
        mRecyclerView = new RecyclerView(context);
        mMedia = media;
        if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DEFAULT) {
            mRecyclerView.setBackgroundColor(Color.parseColor("#333333"));
        } else {
            mRecyclerView.setBackgroundColor(Color.BLACK);
        }
        mRecyclerView.setOverScrollMode(OVER_SCROLL_NEVER);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new InstagramFrameItemDecoration(ScreenUtils.dip2px(context, 20)));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        mVideoTrimmerAdapter = new VideoTrimmerAdapter();
        mRecyclerView.setAdapter(mVideoTrimmerAdapter);
        addView(mRecyclerView);
        ObjectAnimator.ofFloat(mRecyclerView, "translationX", ScreenUtils.getScreenWidth(context), 0).setDuration(300).start();

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                mScrollX += dx;
                mVideoRulerView.scrollBy(dx,0);
            }
        });

        mVideoRulerView = new VideoRulerView(context, media.getDuration());
        addView(mVideoRulerView);

        if (media.getDuration() > 60000) {
            mThumbsCount = Math.round(media.getDuration() / 7500f);
        } else if (media.getDuration() < 15000) {
            mThumbsCount = Math.round(media.getDuration() / 1875f);
        } else {
            mThumbsCount = 8;
        }

        mRangeSeekBarView = new RangeSeekBarView(context, 0, media.getDuration(), mThumbsCount);
        mRangeSeekBarView.setSelectedMinValue(0);
        mRangeSeekBarView.setSelectedMaxValue(media.getDuration());
        mRangeSeekBarView.setStartEndTime(0, media.getDuration());
        mRangeSeekBarView.setMinShootTime(3000L);
        mRangeSeekBarView.setNotifyWhileDragging(true);
        addView(mRangeSeekBarView);

        mLeftShadow = new View(context);
        mLeftShadow.setBackgroundColor(0xBF000000);
        addView(mLeftShadow);

        mRightShadow = new View(context);
        mRightShadow.setBackgroundColor(0xBF000000);
        addView(mRightShadow);

        mVideoTrimmerAdapter.setItemCount(mThumbsCount);
        mFrameTask = new getAllFrameTask(context, media, mThumbsCount, 0, (int) media.getDuration(), new OnSingleBitmapListenerImpl(this));
        mFrameTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void trimVideo() {
        if (mThumbsCount < 8) {
            int duration = (int) (mMedia.getDuration() / 1000);
            Log.d("Test", "Start = " + Math.round(mRangeSeekBarView.getNormalizedMinValue() * duration) + " || end = " + Math.round(mRangeSeekBarView.getNormalizedMaxValue() * duration));
        } else {
            float min = (float) (mRangeSeekBarView.getNormalizedMinValue() * mVideoRulerView.getRangWidth() + mScrollX);
            float max = (float) (mRangeSeekBarView.getNormalizedMaxValue() * mVideoRulerView.getRangWidth() + mScrollX);
            Log.d("Test", "Start = " + Math.round(min / mVideoRulerView.getInterval()) + " || end = " + Math.round(max / mVideoRulerView.getInterval()));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        mRecyclerView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 90), MeasureSpec.EXACTLY));
        mRangeSeekBarView.measure(MeasureSpec.makeMeasureSpec(width - ScreenUtils.dip2px(getContext(), 20), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 90), MeasureSpec.EXACTLY));
        mLeftShadow.measure(MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 10), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 90), MeasureSpec.EXACTLY));
        mRightShadow.measure(MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 10), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 90), MeasureSpec.EXACTLY));
        mVideoRulerView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height - ScreenUtils.dip2px(getContext(), 90), MeasureSpec.EXACTLY));
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int viewTop = 0;
        int viewLeft = 0;
        mRecyclerView.layout(viewLeft, viewTop, viewLeft + mRecyclerView.getMeasuredWidth(), viewTop + mRecyclerView.getMeasuredHeight());

        mLeftShadow.layout(viewLeft, viewTop, viewLeft + mLeftShadow.getMeasuredWidth(), viewTop + mLeftShadow.getMeasuredHeight());

        viewLeft = getMeasuredWidth() - ScreenUtils.dip2px(getContext(), 10);
        mRightShadow.layout(viewLeft, viewTop, viewLeft + mRightShadow.getMeasuredWidth(), viewTop + mRightShadow.getMeasuredHeight());

        viewLeft = ScreenUtils.dip2px(getContext(), 20) - ScreenUtils.dip2px(getContext(), 10);
        mRangeSeekBarView.layout(viewLeft, viewTop, viewLeft + mRangeSeekBarView.getMeasuredWidth(), viewTop + mRangeSeekBarView.getMeasuredHeight());

        viewLeft = 0;
        viewTop += mRecyclerView.getMeasuredHeight();
        mVideoRulerView.layout(viewLeft, viewTop, viewLeft + mVideoRulerView.getMeasuredWidth(), viewTop + mVideoRulerView.getMeasuredHeight());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Rect rect = new Rect();
        mVideoRulerView.getHitRect(rect);
        if (rect.contains((int) (ev.getX()), (int) (ev.getY()))) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mRecyclerView.onTouchEvent(event);
        return true;
    }

    public void onDestroy() {
        if (mFrameTask != null) {
            mFrameTask.setStop(true);
            mFrameTask.cancel(true);
            mFrameTask = null;
        }
    }

    public static class OnSingleBitmapListenerImpl implements getAllFrameTask.OnSingleBitmapListener {
        private WeakReference<TrimContainer> mContainerWeakReference;

        public OnSingleBitmapListenerImpl(TrimContainer container) {
            mContainerWeakReference = new WeakReference<>(container);
        }

        @Override
        public void onSingleBitmapComplete(Bitmap bitmap) {
            TrimContainer container = mContainerWeakReference.get();
            if (container != null) {
                container.post(new RunnableImpl(container.mVideoTrimmerAdapter, bitmap));
            }
        }

        public static class RunnableImpl implements Runnable {
            private WeakReference<VideoTrimmerAdapter> mAdapterWeakReference;
            private Bitmap mBitmap;

            public RunnableImpl(VideoTrimmerAdapter adapter, Bitmap bitmap) {
                mAdapterWeakReference = new WeakReference<>(adapter);
                mBitmap = bitmap;
            }

            @Override
            public void run() {
                VideoTrimmerAdapter adapter = mAdapterWeakReference.get();
                if (adapter != null) {
                    adapter.addBitmaps(mBitmap);
                }
            }
        }
    }
}
