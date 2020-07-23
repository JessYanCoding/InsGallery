package com.luck.picture.lib.instagram;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import com.luck.picture.lib.R;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.ScreenUtils;
import com.luck.picture.lib.tools.SdkVersionUtils;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;
import com.yalantis.ucrop.callback.BitmapCropCallback;
import com.yalantis.ucrop.util.FileUtils;
import com.yalantis.ucrop.util.MimeType;
import com.yalantis.ucrop.view.GestureCropImageView;
import com.yalantis.ucrop.view.OverlayView;
import com.yalantis.ucrop.view.TransformImageView;
import com.yalantis.ucrop.view.UCropView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ================================================
 * Created by JessYan on 2020/3/30 16:33
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InstagramPreviewContainer extends FrameLayout {
    public static final int PLAY_IMAGE_MODE = 0;
    public static final int PLAY_VIDEO_MODE = 1;
    private UCropView mUCropView;
    private VideoView mVideoView;
    private PictureSelectionConfig config;
    private ImageView mThumbView;
    private GestureCropImageView mGestureCropImageView;
    private OverlayView mOverlayView;
    private ImageView mRatioView;
    private ImageView mMultiView;
    private boolean mCropGridShowing;
    private Handler mHandler;
    private boolean isAspectRatio;
    private boolean isMulti;
    private onSelectionModeChangedListener mListener;
    private int mPlayMode;
    private MediaPlayer mMediaPlayer;
    private boolean isPause;
    private ObjectAnimator mThumbAnimator;
    private ImageView mPlayButton;
    private boolean isLoadingVideo;
    private int mPositionWhenPaused = -1;
    private PlayVideoRunnable mPlayVideoRunnable;
    private ShowGridRunnable mShowGridRunnable;

    private TransformImageView.TransformImageListener mImageListener = new TransformImageView.TransformImageListener() {
        @Override
        public void onRotate(float currentAngle) {
        }

        @Override
        public void onScale(float currentScale) {

        }

        @Override
        public void onBitmapLoadComplete(@NonNull Bitmap bitmap) {
            resetAspectRatio();
        }

        @Override
        public void onLoadComplete() {

        }

        @Override
        public void onLoadFailure(@NonNull Exception e) {
        }

    };
    private AnimatorSet mAnimatorSet;
    private ObjectAnimator mPlayAnimator;
    private float mAspectRadio;

    public InstagramPreviewContainer(@NonNull Context context, PictureSelectionConfig config) {
        super(context);
        this.config = config;
        mHandler = new Handler(context.getMainLooper());

        if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK) {
            setBackgroundColor(Color.parseColor("#363636"));
        } else if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK_BLUE) {
            setBackgroundColor(Color.parseColor("#004561"));
        } else {
            setBackgroundColor(Color.parseColor("#efefef"));
        }

        mVideoView = new VideoView(context);
        addView(mVideoView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        mVideoView.setOnClickListener((v -> pauseVideo()));
        mVideoView.setOnPreparedListener(mp -> {
            mMediaPlayer = mp;
            mp.setLooping(true);
            changeVideoSize(mp, isAspectRatio);
            mp.setOnInfoListener((mp1, what, extra) -> {
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    // video started
                    if (mThumbView.getVisibility() == VISIBLE && isLoadingVideo) {
                        isLoadingVideo = false;
                        mThumbAnimator = ObjectAnimator.ofFloat(mThumbView, "alpha", 1.0f, 0).setDuration(400);
                        mThumbAnimator.start();
                    }
                    return true;
                }
                return false;
            });
        });


        mThumbView = new ImageView(context);
        mThumbView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mThumbView.setOnClickListener(v -> pauseVideo());
        addView(mThumbView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mPlayButton = new ImageView(context);
        mPlayButton.setImageResource(R.drawable.discover_play);
        mPlayButton.setOnClickListener(v -> pauseVideo());
        mPlayButton.setVisibility(GONE);
        addView(mPlayButton, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));

        mUCropView = new UCropView(getContext(), null);
        mGestureCropImageView = mUCropView.getCropImageView();
        mOverlayView = mUCropView.getOverlayView();

        mGestureCropImageView.setPadding(0, 0, 0, 0);
        mGestureCropImageView.setTargetAspectRatio(1.0f);
        mGestureCropImageView.setRotateEnabled(false);
        mGestureCropImageView.setTransformImageListener(mImageListener);
        mGestureCropImageView.setMaxScaleMultiplier(15.0f);

        mOverlayView.setPadding(0, 0, 0, 0);
        mOverlayView.setShowCropGrid(false);
        mOverlayView.setShowCropFrame(false);
        if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK) {
            mOverlayView.setDimmedColor(Color.parseColor("#363636"));
            mOverlayView.setCropGridColor(ContextCompat.getColor(context, R.color.picture_color_black));
        } else if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK_BLUE){
            mOverlayView.setDimmedColor(Color.parseColor("#004561"));
            mOverlayView.setCropGridColor(Color.parseColor("#18222D"));
        } else {
            mOverlayView.setDimmedColor(Color.parseColor("#efefef"));
            mOverlayView.setCropGridColor(ContextCompat.getColor(context, R.color.picture_color_white));
        }

        mGestureCropImageView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (!mCropGridShowing) {
                    mOverlayView.setShowCropGrid(true);
                    mOverlayView.invalidate();
                    mCropGridShowing = true;
                } else if (mShowGridRunnable != null){
                    mHandler.removeCallbacks(mShowGridRunnable);
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (mShowGridRunnable == null) {
                    mShowGridRunnable = new ShowGridRunnable(this);
                }
                mHandler.postDelayed(mShowGridRunnable, 800);
            }
            return false;
        });

        addView(mUCropView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        mRatioView = new ImageView(context);

        CombinedDrawable ratiodDrawable = new CombinedDrawable(InstagramUtils.createSimpleSelectorCircleDrawable(ScreenUtils.dip2px(context, 30), 0x88000000, Color.BLACK),
                context.getResources().getDrawable(R.drawable.discover_telescopic).mutate());
        ratiodDrawable.setCustomSize(ScreenUtils.dip2px(context, 30), ScreenUtils.dip2px(context, 30));

        mRatioView.setImageDrawable(ratiodDrawable);
        FrameLayout.LayoutParams ratioLayoutParams = new LayoutParams(ScreenUtils.dip2px(context, 30), ScreenUtils.dip2px(context, 30), Gravity.BOTTOM | Gravity.LEFT);
        ratioLayoutParams.leftMargin = ScreenUtils.dip2px(context, 15);
        ratioLayoutParams.bottomMargin = ScreenUtils.dip2px(context, 12);
        addView(mRatioView, ratioLayoutParams);
        mRatioView.setOnClickListener((v) -> {
            isAspectRatio = !isAspectRatio;
            if (mPlayMode == PLAY_IMAGE_MODE) {
                resetAspectRatio();
            } else if (mPlayMode == PLAY_VIDEO_MODE) {
                changeVideoSize(mMediaPlayer, isAspectRatio);
            }
            if (mListener != null) {
                mListener.onRatioChange(isAspectRatio);
            }
        });

        mMultiView = new ImageView(context);

        CombinedDrawable multiDrawable = new CombinedDrawable(InstagramUtils.createSimpleSelectorCircleDrawable(ScreenUtils.dip2px(context, 30), 0x88000000, Color.BLACK),
                context.getResources().getDrawable(R.drawable.discover_many).mutate());
        multiDrawable.setCustomSize(ScreenUtils.dip2px(context, 30), ScreenUtils.dip2px(context, 30));

        mMultiView.setImageDrawable(multiDrawable);
        FrameLayout.LayoutParams multiLayoutParams = new LayoutParams(ScreenUtils.dip2px(context, 30), ScreenUtils.dip2px(context, 30), Gravity.BOTTOM | Gravity.RIGHT);
        multiLayoutParams.rightMargin = ScreenUtils.dip2px(context, 15);
        multiLayoutParams.bottomMargin = ScreenUtils.dip2px(context, 12);
        addView(mMultiView, multiLayoutParams);
        mMultiView.setOnClickListener(v -> setMultiMode(!isMulti));

        View divider = new View(getContext());
        if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK) {
            divider.setBackgroundColor(ContextCompat.getColor(context, R.color.picture_color_black));
        } else if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK_BLUE){
            divider.setBackgroundColor(Color.parseColor("#18222D"));
        } else {
            divider.setBackgroundColor(ContextCompat.getColor(context, R.color.picture_color_white));
        }
        FrameLayout.LayoutParams dividerParms = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(getContext(), 2), Gravity.BOTTOM);
        addView(divider, dividerParms);
    }

    public void setMultiMode(boolean multi) {
        isMulti = multi;
        if (multi) {
            mRatioView.setVisibility(View.GONE);
        } else {
            mRatioView.setVisibility(View.VISIBLE);
        }
        if (mListener != null) {
            mListener.onSelectionModeChange(multi);
        }
    }

    public boolean isMulti() {
        return isMulti;
    }

    private void pauseVideo() {
        pauseVideo(!isPause);
    }

    public void pauseVideo(boolean pause) {
        if (isPause == pause) {
            return;
        }
        isPause = pause;
        if (mPlayMode != PLAY_VIDEO_MODE) {
            return;
        }
        if (mPlayAnimator != null && mPlayAnimator.isRunning()) {
            mPlayAnimator.cancel();
        }
        if (pause) {
            mPlayButton.setVisibility(VISIBLE);
            mPlayAnimator = ObjectAnimator.ofFloat(mPlayButton, "alpha", 0, 1.0f).setDuration(200);
            mVideoView.pause();
        } else {
            mPlayAnimator = ObjectAnimator.ofFloat(mPlayButton, "alpha", 1.0f, 0).setDuration(200);
            mPlayAnimator.addListener(new AnimatorListenerImpl() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mPlayButton.setVisibility(GONE);
                }
            });
            mVideoView.start();
        }
        mPlayAnimator.start();
    }

    private void resetAspectRatio() {
        mAspectRadio = 0;
        if (isAspectRatio) {
            Drawable drawable = mGestureCropImageView.getDrawable();
            if (drawable != null) {
                mAspectRadio = getInstagramAspectRatio(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            }
        }

        mGestureCropImageView.setTargetAspectRatio(isAspectRatio ? mAspectRadio : 1.0f);
        mGestureCropImageView.onImageLaidOut();
    }

    public static float getInstagramAspectRatio(int width, int height) {
        float aspectRatio = 0;
        if (height > width * 1.266f) {
            aspectRatio = width / (width * 1.266f);
        } else if (width > height * 1.9f) {
            aspectRatio = height * 1.9f / height;
        }
        return aspectRatio;
    }

    public boolean isAspectRatio() {
        return isAspectRatio;
    }

    public float getAspectRadio() {
        return mAspectRadio;
    }

    public void setImageUri(@NonNull Uri inputUri, @Nullable Uri outputUri) {
        if (mPlayMode != PLAY_IMAGE_MODE) {
            return;
        }
        if (inputUri != null && outputUri != null) {
            try {
                boolean isOnTouch = isOnTouch(inputUri);
                mGestureCropImageView.setScaleEnabled(isOnTouch ? true : isOnTouch);
                mGestureCropImageView.setImageUri(inputUri, outputUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void playVideo(LocalMedia media, RecyclerView.ViewHolder holder) {
        if (mPlayMode != PLAY_VIDEO_MODE) {
            return;
        }
        if (mThumbAnimator != null && mThumbAnimator.isRunning()) {
            mThumbAnimator.cancel();
        }
        if (mPlayVideoRunnable != null) {
            mHandler.removeCallbacks(mPlayVideoRunnable);
        }
        Drawable drawable = null;
        if (holder != null && holder instanceof InstagramImageGridAdapter.ViewHolder) {
            drawable = ((InstagramImageGridAdapter.ViewHolder) holder).ivPicture.getDrawable();
        }
        if (drawable != null) {
            mThumbView.setImageDrawable(drawable);
        } else {
            mThumbView.setImageDrawable(null);
            if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK) {
                mThumbView.setBackgroundColor(Color.BLACK);
            } else if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK_BLUE){
                mThumbView.setBackgroundColor(Color.parseColor("#18222D"));
            } else {
                mThumbView.setBackgroundColor(Color.WHITE);
            }
        }
        mPlayButton.setVisibility(GONE);
        isLoadingVideo = false;
        mPlayVideoRunnable = new PlayVideoRunnable(this, media);
        mHandler.postDelayed(mPlayVideoRunnable, 600);
    }

    public void checkModel(int mode) {
        mPlayMode = mode;

        if (mVideoView.getVisibility() == VISIBLE && mVideoView.isPlaying()) {
            mVideoView.pause();
            isPause = true;
        }
        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            mAnimatorSet.cancel();
        }
        mAnimatorSet = new AnimatorSet();
        List<Animator> animators = new ArrayList<>();
        if (mode == PLAY_IMAGE_MODE) {
            InstagramUtils.setViewVisibility(mUCropView, View.VISIBLE);
            animators.add(ObjectAnimator.ofFloat(mUCropView, "alpha", 0.1f, 1.0f));
            mAnimatorSet.addListener(new AnimatorListenerImpl() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    InstagramUtils.setViewVisibility(mVideoView, View.GONE);
                    InstagramUtils.setViewVisibility(mThumbView, View.GONE);
                }
            });
        } else if (mode == PLAY_VIDEO_MODE) {
            InstagramUtils.setViewVisibility(mVideoView, View.VISIBLE);
            InstagramUtils.setViewVisibility(mThumbView, View.VISIBLE);
            InstagramUtils.setViewVisibility(mUCropView, View.GONE);
            animators.add(ObjectAnimator.ofFloat(mVideoView, "alpha", 0f, 1.0f));
            animators.add(ObjectAnimator.ofFloat(mThumbView, "alpha", 0.1f, 1.0f));
        }
        mAnimatorSet.setDuration(800);
        mAnimatorSet.playTogether(animators);
        mAnimatorSet.start();
    }

    /**
     * 是否可以触摸
     *
     * @param inputUri
     * @return
     */
    private boolean isOnTouch(Uri inputUri) {
        if (inputUri == null) {
            return true;
        }
        boolean isHttp = MimeType.isHttp(inputUri.toString());
        if (isHttp) {
            // 网络图片
            String lastImgType = MimeType.getLastImgType(inputUri.toString());
            return !MimeType.isGifForSuffix(lastImgType);
        } else {
            String mimeType = MimeType.getMimeTypeFromMediaContentUri(getContext(), inputUri);
            if (mimeType.endsWith("image/*")) {
                String path = FileUtils.getPath(getContext(), inputUri);
                mimeType = MimeType.getImageMimeType(path);
            }
            return !MimeType.isGif(mimeType);
        }
    }

    public void setListener(onSelectionModeChangedListener listener) {
        mListener = listener;
    }

    /**
     * 修改预览View的大小, 以用来适配屏幕
     */
    public void changeVideoSize(MediaPlayer mediaPlayer, boolean isAspectRatio) {
        if (mediaPlayer == null || mVideoView == null) {
            return;
        }
        try {
            mediaPlayer.getVideoWidth();
        } catch (Exception e) {
            return;
        }
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        int parentWidth = getMeasuredWidth();
        int parentHeight = getMeasuredHeight();

        float instagramAspectRatio = getInstagramAspectRatio(videoWidth, videoHeight);
        float targetAspectRatio = videoWidth * 1.0f / videoHeight;

        int height = (int) (parentWidth / targetAspectRatio);

        int adjustWidth;
        int adjustHeight;
        if (isAspectRatio) {
            if (height > parentHeight) {
                adjustWidth = (int) (parentWidth * (instagramAspectRatio > 0 ? instagramAspectRatio : targetAspectRatio));
                adjustHeight = height;
            } else {
                if (instagramAspectRatio > 0) {
                    adjustWidth = (int) (parentHeight * targetAspectRatio);
                    adjustHeight = (int) (parentHeight / instagramAspectRatio);
                } else {
                    adjustWidth = parentWidth;
                    adjustHeight = height;
                }
            }
        } else {
            if (height < parentHeight) {
                adjustWidth = (int) (parentHeight * targetAspectRatio);
                adjustHeight = parentHeight;
            } else {
                adjustWidth = parentWidth;
                adjustHeight = height;
            }
        }

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mVideoView.getLayoutParams();
        layoutParams.width = adjustWidth;
        layoutParams.height = adjustHeight;
        mVideoView.setLayoutParams(layoutParams);
    }

    public void onPause() {
        // Stop video when the activity is pause.
        if (mPlayMode == PLAY_VIDEO_MODE) {
            mPositionWhenPaused = mVideoView.getCurrentPosition();
            mVideoView.stopPlayback();
        }
    }

    public AsyncTask createCropAndSaveImageTask(BitmapCropCallback cropCallback) {
       return mGestureCropImageView.createCropAndSaveImageTask(UCropActivity.DEFAULT_COMPRESS_FORMAT, UCropActivity.DEFAULT_COMPRESS_QUALITY, cropCallback);
    }

    public void cropAndSaveImage(PictureSelectorInstagramStyleActivity activity) {
        mGestureCropImageView.cropAndSaveImage(UCropActivity.DEFAULT_COMPRESS_FORMAT, UCropActivity.DEFAULT_COMPRESS_QUALITY, new BitmapCropCallback() {

            @Override
            public void onBitmapCropped(@NonNull Uri resultUri, int offsetX, int offsetY, int imageWidth, int imageHeight) {
                setResultUri(activity, resultUri, mGestureCropImageView.getTargetAspectRatio(), offsetX, offsetY, imageWidth, imageHeight);
            }

            @Override
            public void onCropFailure(@NonNull Throwable t) {
                setResultError(activity, t);
            }
        });
    }

    protected void setResultUri(PictureSelectorInstagramStyleActivity activity, Uri uri, float resultAspectRatio, int offsetX, int offsetY, int imageWidth, int imageHeight) {
        activity.onActivityResult(UCrop.REQUEST_CROP, Activity.RESULT_OK, new Intent()
                .putExtra(UCrop.EXTRA_OUTPUT_URI, uri)
                .putExtra(UCrop.EXTRA_OUTPUT_CROP_ASPECT_RATIO, resultAspectRatio)
                .putExtra(UCrop.EXTRA_OUTPUT_IMAGE_WIDTH, imageWidth)
                .putExtra(UCrop.EXTRA_OUTPUT_IMAGE_HEIGHT, imageHeight)
                .putExtra(UCrop.EXTRA_OUTPUT_OFFSET_X, offsetX)
                .putExtra(UCrop.EXTRA_OUTPUT_OFFSET_Y, offsetY));
    }

    protected void setResultError(PictureSelectorInstagramStyleActivity activity, Throwable throwable) {
        activity.onActivityResult(UCrop.REQUEST_CROP, UCrop.RESULT_ERROR, new Intent().putExtra(UCrop.EXTRA_ERROR, throwable));
    }

    public int getPlayMode() {
        return mPlayMode;
    }

    public void onResume() {
        // Resume video player
        if (mPlayMode == PLAY_VIDEO_MODE) {
            if (!mVideoView.isPlaying()) {
                mVideoView.start();
            }
            if (isPause) {
                mPlayButton.setVisibility(GONE);
                isPause = false;
            }
            if (mPositionWhenPaused >= 0) {
                mVideoView.seekTo(mPositionWhenPaused);
                mPositionWhenPaused = -1;
            }
        }
    }

    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mThumbAnimator != null && mThumbAnimator.isRunning()) {
            mThumbAnimator.cancel();
            mThumbAnimator = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        mListener = null;
        mVideoView = null;
        mUCropView = null;
        mGestureCropImageView = null;
        mOverlayView = null;
    }

    private static class PlayVideoRunnable implements Runnable {
        private WeakReference<InstagramPreviewContainer> mPreviewContainer;
        private LocalMedia mMedia;

        PlayVideoRunnable(InstagramPreviewContainer previewContainer, LocalMedia media) {
            mPreviewContainer = new WeakReference<>(previewContainer);
            this.mMedia = media;
        }

        @Override
        public void run() {
            InstagramPreviewContainer previewContainer = mPreviewContainer.get();
            if (previewContainer == null) {
                return;
            }
            if (SdkVersionUtils.checkedAndroid_Q() && PictureMimeType.isContent(mMedia.getPath())) {
                previewContainer.mVideoView.setVideoURI(Uri.parse(mMedia.getPath()));
            } else {
                previewContainer.mVideoView.setVideoPath(mMedia.getPath());
            }
            previewContainer.mVideoView.start();
            previewContainer.isPause = false;
            previewContainer.isLoadingVideo = true;
        }
    }

    private static class ShowGridRunnable implements Runnable {
        private WeakReference<InstagramPreviewContainer> mPreviewContainer;

        ShowGridRunnable(InstagramPreviewContainer previewContainer) {
            mPreviewContainer = new WeakReference<>(previewContainer);
        }

        @Override
        public void run() {
            InstagramPreviewContainer previewContainer = mPreviewContainer.get();
            if (previewContainer == null) {
                return;
            }
            if (previewContainer.mCropGridShowing) {
                previewContainer.mOverlayView.setShowCropGrid(false);
                previewContainer.mOverlayView.invalidate();
                previewContainer.mCropGridShowing = false;
            }
        }
    }

    public interface onSelectionModeChangedListener {
        void onSelectionModeChange(boolean isMulti);
        void onRatioChange(boolean isOneToOne);
    }
}
