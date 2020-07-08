package com.luck.picture.lib.instagram.process;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.VideoView;

import com.luck.picture.lib.R;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.instagram.AnimatorListenerImpl;
import com.luck.picture.lib.instagram.InsGallery;
import com.luck.picture.lib.instagram.adapter.InstagramFrameItemDecoration;
import com.luck.picture.lib.instagram.adapter.VideoTrimmerAdapter;
import com.luck.picture.lib.thread.PictureThreadUtils;
import com.luck.picture.lib.tools.DateUtils;
import com.luck.picture.lib.tools.ScreenUtils;
import com.luck.picture.lib.tools.SdkVersionUtils;
import com.luck.picture.lib.tools.ToastUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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
    private final int mPadding;
    private RecyclerView mRecyclerView;
    private LocalMedia mMedia;
    private VideoView mVideoView;
    private final VideoTrimmerAdapter mVideoTrimmerAdapter;
    private getAllFrameTask mFrameTask;
    private final VideoRulerView mVideoRulerView;
    private final RangeSeekBarView mRangeSeekBarView;
    private View mLeftShadow;
    private View mRightShadow;
    private View mIndicatorView;
    private int mScrollX;
    private int mThumbsCount;
    private ObjectAnimator mPauseAnim;
    private ObjectAnimator mIndicatorAnim;
    private float mIndicatorPosition;
    private LinearInterpolator mInterpolator;
    private boolean isRangeChange = true;

    public TrimContainer(@NonNull Context context, PictureSelectionConfig config, LocalMedia media, VideoView videoView, VideoPauseListener videoPauseListener) {
        super(context);
        mPadding = ScreenUtils.dip2px(context, 20);
        mRecyclerView = new RecyclerView(context);
        mMedia = media;
        mVideoView = videoView;
        if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DEFAULT) {
            mRecyclerView.setBackgroundColor(Color.parseColor("#333333"));
        } else {
            mRecyclerView.setBackgroundColor(Color.BLACK);
        }
        mRecyclerView.setOverScrollMode(OVER_SCROLL_NEVER);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new InstagramFrameItemDecoration(mPadding));
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
                mVideoRulerView.scrollBy(dx, 0);
                if (media.getDuration() > 60000) {
                    changeRange(videoView, videoPauseListener);
                }
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
        mRangeSeekBarView.setOnRangeSeekBarChangeListener((bar, minValue, maxValue, action, isMin, pressedThumb) -> changeRange(videoView, videoPauseListener));
        addView(mRangeSeekBarView);

        mLeftShadow = new View(context);
        mLeftShadow.setBackgroundColor(0xBF000000);
        addView(mLeftShadow);

        mRightShadow = new View(context);
        mRightShadow.setBackgroundColor(0xBF000000);
        addView(mRightShadow);

        mIndicatorView = new View(context);
        mIndicatorView.setBackgroundColor(Color.WHITE);
        addView(mIndicatorView);
        mIndicatorView.setVisibility(View.GONE);

        mVideoTrimmerAdapter.setItemCount(mThumbsCount);
        mFrameTask = new getAllFrameTask(context, media, mThumbsCount, 0, (int) media.getDuration(), new OnSingleBitmapListenerImpl(this));
        mFrameTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void changeRange(VideoView videoView, VideoPauseListener videoPauseListener) {
        videoView.seekTo((int) getStartTime());
        if (videoView.isPlaying()) {
            videoPauseListener.onVideoPause();
        }
        isRangeChange = true;
        mIndicatorPosition = 0;
    }

    public void playVideo(boolean isPlay, VideoView videoView) {
        if (mPauseAnim != null && mPauseAnim.isRunning()) {
            mPauseAnim.cancel();
        }

        if (isPlay) {
            mIndicatorView.setVisibility(View.VISIBLE);
            mPauseAnim = ObjectAnimator.ofFloat(mIndicatorView, "alpha", 0, 1.0f).setDuration(200);

            if (isRangeChange) {
                isRangeChange = false;
                long startTime;
                if (mIndicatorPosition > 0) {
                    startTime = Math.round((mIndicatorPosition - ScreenUtils.dip2px(getContext(), 20)) / mVideoRulerView.getInterval() * 1000);
                } else {
                    startTime = getStartTime();
                }
                mIndicatorAnim = ObjectAnimator.ofFloat(mIndicatorView, "translationX", mIndicatorPosition > 0 ? mIndicatorPosition : mRangeSeekBarView.getStartLine() + ScreenUtils.dip2px(getContext(), 10),
                        mRangeSeekBarView.getEndLine() + ScreenUtils.dip2px(getContext(), 10)).setDuration(getEndTime() - startTime);
                mIndicatorAnim.addUpdateListener(animation -> mIndicatorPosition = (float) animation.getAnimatedValue());
                mIndicatorAnim.addListener(new AnimatorListenerImpl() {
                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        if (videoView != null) {
                            videoView.seekTo((int) startTime);
                        }
                    }
                });
                mIndicatorAnim.setRepeatMode(ValueAnimator.RESTART);
                mIndicatorAnim.setRepeatCount(ValueAnimator.INFINITE);
                if (mInterpolator == null) {
                    mInterpolator = new LinearInterpolator();
                }
                mIndicatorAnim.setInterpolator(mInterpolator);
                mIndicatorAnim.start();
            } else {
                if (mIndicatorAnim != null && mIndicatorAnim.isPaused()) {
                    mIndicatorAnim.resume();
                }
            }
        } else {
            mPauseAnim = ObjectAnimator.ofFloat(mIndicatorView, "alpha", 1.0f, 0).setDuration(200);
            mPauseAnim.addListener(new AnimatorListenerImpl() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mIndicatorView.setVisibility(GONE);
                }
            });

            if (mIndicatorAnim != null && mIndicatorAnim.isRunning()) {
                mIndicatorAnim.pause();
            }
        }
        mPauseAnim.start();
    }

    private long getStartTime() {
        if (mThumbsCount < 8) {
            return Math.round(mRangeSeekBarView.getNormalizedMinValue() * mMedia.getDuration());
        } else {
            double min = mRangeSeekBarView.getNormalizedMinValue() * mRangeSeekBarView.getMeasuredWidth() + mScrollX;
            return Math.round((min > 0 ? min + ScreenUtils.dip2px(getContext(), 1) : min) / mVideoRulerView.getInterval() * 1000);
        }
    }

    private long getEndTime() {
        if (mThumbsCount < 8) {
            return Math.round(mRangeSeekBarView.getNormalizedMaxValue() * mMedia.getDuration());
        } else {
            double max = mRangeSeekBarView.getNormalizedMaxValue() * mVideoRulerView.getRangWidth() + mScrollX;
            return Math.round((max - ScreenUtils.dip2px(getContext(), 1)) / mVideoRulerView.getInterval() * 1000);
        }
    }

    public void trimVideo(InstagramMediaProcessActivity activity) {
        activity.showLoadingView(true);

        long startTime = getStartTime();
        long endTime = getEndTime();

        PictureThreadUtils.executeByIo(new PictureThreadUtils.SimpleTask<File>() {

            @Override
            public File doInBackground() {
                Uri inputUri;
                if (SdkVersionUtils.checkedAndroid_Q() && PictureMimeType.isContent(mMedia.getPath())) {
                    inputUri = Uri.parse(mMedia.getPath());
                } else {
                    inputUri = Uri.fromFile(new File(mMedia.getPath()));
                }

                try {
                    File outputDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES), "TrimVideos");
                    outputDir.mkdir();
                    File outputFile = File.createTempFile(DateUtils.getCreateFileName("trim_"), ".mp4", outputDir);

                    ParcelFileDescriptor parcelFileDescriptor = getContext().getContentResolver().openFileDescriptor(inputUri, "r");
                    if (parcelFileDescriptor != null) {
                        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
//                        boolean succeeded = VideoClipUtils.trimUsingMp4Parser(fileDescriptor, outputFile.getAbsolutePath(), startTime, endTime);
//                        if (!succeeded) {
                        boolean succeeded = VideoClipUtils.genVideoUsingMuxer(fileDescriptor, outputFile.getAbsolutePath(), startTime, endTime, true, true);
//                        }
                        if (succeeded) {
                            return outputFile;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public void onSuccess(File result) {
                if (result != null) {
                    mMedia.setDuration(endTime - startTime);
                    mMedia.setPath(result.getAbsolutePath());
                    mMedia.setAndroidQToPath(SdkVersionUtils.checkedAndroid_Q() ? result.getAbsolutePath() : mMedia.getAndroidQToPath());
                    List<LocalMedia> list = new ArrayList<>();
                    list.add(mMedia);
                    activity.showLoadingView(false);
                    activity.setResult(Activity.RESULT_OK, new Intent().putParcelableArrayListExtra(PictureConfig.EXTRA_SELECT_LIST, (ArrayList<? extends Parcelable>) list));
                    activity.finish();
                } else {
                    ToastUtils.s(getContext(), getContext().getString(R.string.video_clip_failed));
                }
            }
        });
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
        if (mIndicatorView.getVisibility() == View.VISIBLE) {
            mIndicatorView.measure(MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 2), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(ScreenUtils.dip2px(getContext(), 90), MeasureSpec.EXACTLY));
        }
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

        viewTop = 0;
        if (mIndicatorView.getVisibility() == View.VISIBLE) {
            mIndicatorView.layout(viewLeft, viewTop, viewLeft + mIndicatorView.getMeasuredWidth(), viewTop + mIndicatorView.getMeasuredHeight());
        }
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

    public void onResume() {
        if (mVideoView != null) {
            mVideoView.seekTo((int) getStartTime());
        }
    }

    public void onPause() {
        if (mIndicatorAnim != null && mIndicatorAnim.isRunning()) {
            mIndicatorAnim.cancel();
        }
        isRangeChange = true;
        mIndicatorPosition = 0;
        mIndicatorView.setVisibility(GONE);
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

    public interface VideoPauseListener {
        void onVideoPause();
    }
}
