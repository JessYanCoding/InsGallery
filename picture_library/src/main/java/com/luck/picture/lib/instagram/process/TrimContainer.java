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
import android.media.MediaMetadataRetriever;
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
import com.luck.picture.lib.instagram.InstagramPreviewContainer;
import com.luck.picture.lib.instagram.adapter.InstagramFrameItemDecoration;
import com.luck.picture.lib.instagram.adapter.VideoTrimmerAdapter;
import com.luck.picture.lib.thread.PictureThreadUtils;
import com.luck.picture.lib.tools.DateUtils;
import com.luck.picture.lib.tools.ScreenUtils;
import com.luck.picture.lib.tools.SdkVersionUtils;
import com.luck.picture.lib.tools.ToastUtils;
import com.otaliastudios.transcoder.Transcoder;
import com.otaliastudios.transcoder.TranscoderListener;
import com.otaliastudios.transcoder.TranscoderOptions;
import com.otaliastudios.transcoder.sink.DataSink;
import com.otaliastudios.transcoder.sink.DefaultDataSink;
import com.otaliastudios.transcoder.source.ClipDataSource;
import com.otaliastudios.transcoder.source.FilePathDataSource;
import com.otaliastudios.transcoder.source.UriDataSource;
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy;
import com.otaliastudios.transcoder.strategy.TrackStrategy;
import com.otaliastudios.transcoder.strategy.size.AspectRatioResizer;
import com.otaliastudios.transcoder.strategy.size.FractionResizer;
import com.otaliastudios.transcoder.strategy.size.PassThroughResizer;
import com.otaliastudios.transcoder.strategy.size.Resizer;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
    private PictureSelectionConfig mConfig;
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
    private boolean mIsPreviewStart = true;
    private InstagramLoadingDialog mLoadingDialog;
    private Future<Void> mTranscodeFuture;

    public TrimContainer(@NonNull Context context, PictureSelectionConfig config, LocalMedia media, VideoView videoView, VideoPauseListener videoPauseListener) {
        super(context);
        mPadding = ScreenUtils.dip2px(context, 20);
        mRecyclerView = new RecyclerView(context);
        mConfig = config;
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
                if (media.getDuration() > 60000 && dx != 0) {
                    changeRange(videoView, videoPauseListener, true);
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
        mRangeSeekBarView.setOnRangeSeekBarChangeListener((bar, minValue, maxValue, action, isMin, pressedThumb) -> changeRange(videoView, videoPauseListener, pressedThumb == RangeSeekBarView.Thumb.MIN));
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

    private void changeRange(VideoView videoView, VideoPauseListener videoPauseListener, boolean isPreviewStart) {
        videoPauseListener.onChange();
        mIsPreviewStart = isPreviewStart;
        if (isPreviewStart) {
            videoView.seekTo((int) getStartTime());
        } else {
            videoView.seekTo((int) getEndTime());
        }
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
            if (!mIsPreviewStart) {
                videoView.seekTo((int) getStartTime());
            }

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

    public long getStartTime() {
        if (mThumbsCount < 8) {
            return Math.round(mRangeSeekBarView.getNormalizedMinValue() * mMedia.getDuration());
        } else {
            double min = mRangeSeekBarView.getNormalizedMinValue() * mRangeSeekBarView.getMeasuredWidth() + mScrollX;
            return Math.round((min > 0 ? min + ScreenUtils.dip2px(getContext(), 1) : min) / mVideoRulerView.getInterval() * 1000);
        }
    }

    public long getEndTime() {
        if (mThumbsCount < 8) {
            return Math.round(mRangeSeekBarView.getNormalizedMaxValue() * mMedia.getDuration());
        } else {
            double max = mRangeSeekBarView.getNormalizedMaxValue() * mVideoRulerView.getRangWidth() + mScrollX;
            return Math.round((max - ScreenUtils.dip2px(getContext(), 1)) / mVideoRulerView.getInterval() * 1000);
        }
    }

    public void cropVideo(InstagramMediaProcessActivity activity, boolean isAspectRatio) {
        showLoadingView(true);
        long startTime = getStartTime();
        long endTime = getEndTime();

        long startTimeUS = getStartTime() * 1000;
        long endTimeUS = getEndTime() * 1000;

        File transcodeOutputFile;
        try {
            File outputDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES), "TrimVideos");
            //noinspection ResultOfMethodCallIgnored
            outputDir.mkdir();
            transcodeOutputFile = File.createTempFile(DateUtils.getCreateFileName("trim_"), ".mp4", outputDir);
        } catch (IOException e) {
            ToastUtils.s(getContext(), "Failed to create temporary file.");
            return;
        }

        Resizer resizer = new PassThroughResizer();
        if (mConfig.instagramSelectionConfig.isCropVideo()) {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            Uri uri;
            if (SdkVersionUtils.checkedAndroid_Q() && PictureMimeType.isContent(mMedia.getPath())) {
                uri = Uri.parse(mMedia.getPath());
            } else {
                uri = Uri.fromFile(new File(mMedia.getPath()));
            }
            mediaMetadataRetriever.setDataSource(getContext(), uri);
            int videoWidth = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            int videoHeight = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            float instagramAspectRatio = InstagramPreviewContainer.getInstagramAspectRatio(videoWidth, videoHeight);
            mediaMetadataRetriever.release();

            if (isAspectRatio && instagramAspectRatio > 0) {
                resizer = new AspectRatioResizer(instagramAspectRatio);
            } else if (!isAspectRatio) {
                resizer = new AspectRatioResizer(1f);
            }
        }
        TrackStrategy videoStrategy = new DefaultVideoStrategy.Builder()
                .addResizer(resizer)
                .addResizer(new FractionResizer(1f))
                .build();

        DataSink sink = new DefaultDataSink(transcodeOutputFile.getAbsolutePath());
        TranscoderOptions.Builder builder = Transcoder.into(sink);
        if (PictureMimeType.isContent(mMedia.getPath())) {
            builder.addDataSource(new ClipDataSource(new UriDataSource(getContext(), Uri.parse(mMedia.getPath())), startTimeUS, endTimeUS));
        } else {
            builder.addDataSource(new ClipDataSource(new FilePathDataSource(mMedia.getPath()), startTimeUS, endTimeUS));
        }
        mTranscodeFuture = builder.setListener(new TranscoderListenerImpl(this, activity, startTime, endTime, transcodeOutputFile))
                .setVideoTrackStrategy(videoStrategy)
                .transcode();
    }

    private void showLoadingView(boolean isShow) {
        if (((Activity) getContext()).isFinishing()) {
            return;
        }
        if (isShow) {
            if (mLoadingDialog == null) {
                mLoadingDialog = new InstagramLoadingDialog(getContext());
                mLoadingDialog.setOnCancelListener(dialog -> {
                    if (mTranscodeFuture != null) {
                        mTranscodeFuture.cancel(true);
                    }
                });
            }
            if (mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
            }
            mLoadingDialog.updateProgress(0);
            mLoadingDialog.show();
        } else {
            if (mLoadingDialog != null
                    && mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
            }
        }
    }

    public void trimVideo(InstagramMediaProcessActivity activity, CountDownLatch count) {
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
                            count.countDown();
                            try {
                                count.await(1500, TimeUnit.MILLISECONDS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
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
        resetStartLine();
    }

    public void resetStartLine() {
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
        if (mTranscodeFuture != null) {
            mTranscodeFuture.cancel(true);
            mTranscodeFuture = null;
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
        void onChange();

        void onVideoPause();
    }

    private static class TranscoderListenerImpl implements TranscoderListener {
        private WeakReference<TrimContainer> mContainerWeakReference;
        private WeakReference<InstagramMediaProcessActivity> mActivityWeakReference;
        private long mStartTime;
        private long mEndTime;
        private File mTranscodeOutputFile;

        public TranscoderListenerImpl(TrimContainer container, InstagramMediaProcessActivity activity, long startTime, long endTime, File transcodeOutputFile) {
            mContainerWeakReference = new WeakReference<>(container);
            mActivityWeakReference = new WeakReference<>(activity);
            mStartTime = startTime;
            mEndTime = endTime;
            mTranscodeOutputFile = transcodeOutputFile;
        }

        @Override
        public void onTranscodeProgress(double progress) {
            TrimContainer trimContainer = mContainerWeakReference.get();
            if (trimContainer == null) {
                return;
            }
            if (trimContainer.mLoadingDialog != null
                    && trimContainer.mLoadingDialog.isShowing()) {
                trimContainer.mLoadingDialog.updateProgress(progress);
            }
        }

        @Override
        public void onTranscodeCompleted(int successCode) {
            TrimContainer trimContainer = mContainerWeakReference.get();
            InstagramMediaProcessActivity activity = mActivityWeakReference.get();
            if (trimContainer == null || activity == null) {
                return;
            }
            if (successCode == Transcoder.SUCCESS_TRANSCODED) {
                trimContainer.mMedia.setDuration(mEndTime - mStartTime);
                trimContainer.mMedia.setPath(mTranscodeOutputFile.getAbsolutePath());
                trimContainer.mMedia.setAndroidQToPath(SdkVersionUtils.checkedAndroid_Q() ? mTranscodeOutputFile.getAbsolutePath() : trimContainer.mMedia.getAndroidQToPath());
                List<LocalMedia> list = new ArrayList<>();
                list.add(trimContainer.mMedia);
                activity.setResult(Activity.RESULT_OK, new Intent().putParcelableArrayListExtra(PictureConfig.EXTRA_SELECT_LIST, (ArrayList<? extends Parcelable>) list));
                activity.finish();
            } else if (successCode == Transcoder.SUCCESS_NOT_NEEDED) {

            }
            trimContainer.showLoadingView(false);
        }

        @Override
        public void onTranscodeCanceled() {
            TrimContainer trimContainer = mContainerWeakReference.get();
            if (trimContainer == null) {
                return;
            }
            trimContainer.showLoadingView(false);
        }

        @Override
        public void onTranscodeFailed(@NonNull Throwable exception) {
            TrimContainer trimContainer = mContainerWeakReference.get();
            if (trimContainer == null) {
                return;
            }
            exception.printStackTrace();
            ToastUtils.s(trimContainer.getContext(), trimContainer.getContext().getString(R.string.video_clip_failed));
            trimContainer.showLoadingView(false);
        }
    }
}
