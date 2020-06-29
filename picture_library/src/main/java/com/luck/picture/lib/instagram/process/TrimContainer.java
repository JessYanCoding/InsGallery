package com.luck.picture.lib.instagram.process;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.view.MotionEvent;
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
    private final VideoTrimmerAdapter mVideoTrimmerAdapter;
    private getAllFrameTask mFrameTask;
    private final VideoRulerView mVideoRulerView;

    public TrimContainer(@NonNull Context context, PictureSelectionConfig config, LocalMedia media) {
        super(context);
        mRecyclerView = new RecyclerView(context);
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

        mVideoRulerView = new VideoRulerView(context);
        addView(mVideoRulerView);

        int thumbsCount;
        if (media.getDuration() > 60000) {
            thumbsCount = Math.round(media.getDuration() / 7500f);
        } else if (media.getDuration() < 15000) {
            thumbsCount = Math.round(media.getDuration() / 1875f);
        } else {
            thumbsCount = 8;
        }
        mVideoTrimmerAdapter.setItemCount(thumbsCount);
        mFrameTask = new getAllFrameTask(context, media, thumbsCount, 0, (int) media.getDuration(), new OnSingleBitmapListenerImpl(this));
        mFrameTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        mRecyclerView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 90), MeasureSpec.EXACTLY));
        mVideoRulerView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height - ScreenUtils.dip2px(getContext(), 90), MeasureSpec.EXACTLY));
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int viewTop = 0;
        int viewLeft = 0;
        mRecyclerView.layout(viewLeft, viewTop, viewLeft + mRecyclerView.getMeasuredWidth(), viewTop + mRecyclerView.getMeasuredHeight());

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
