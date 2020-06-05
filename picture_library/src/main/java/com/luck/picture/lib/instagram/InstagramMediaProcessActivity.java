package com.luck.picture.lib.instagram;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.FrameLayout;

import com.luck.picture.lib.PictureBaseActivity;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.yalantis.ucrop.UCrop;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * Created by JessYan on 2020/5/29 11:39
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InstagramMediaProcessActivity extends PictureBaseActivity {
    public static final String EXTRA_ASPECT_RATIO  = "extra_aspect_ratio";
    public static final String EXTRA_ASPECT_RATIO_VALUE  = "extra_aspect_ratio_value";
    public static final String EXTRA_SINGLE_IMAGE_URI = "extra_single_image_uri";
    public static final int REQUEST_SINGLE_IMAGE_PROCESS = 339;
    public static final int REQUEST_MULTI_IMAGE_PROCESS = 440;
    public static final int REQUEST_SINGLE_VIDEO_PROCESS = 441;
    private List<LocalMedia> mSelectMedia;
    private InstagramTitleBar mTitleBar;
    private MediaType mMediaType;
    private boolean isAspectRatio;
    private float mAspectRatio;
    private Uri mSingleImageUri;

    public enum MediaType {
        SINGLE_IMAGE, SINGLE_VIDEO, MULTI_IMAGE
    }

    @Override
    public int getResourceId() {
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mSelectMedia = PictureSelector.obtainSelectorList(savedInstanceState);
            mSingleImageUri = savedInstanceState.getParcelable(EXTRA_SINGLE_IMAGE_URI);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        overridePendingTransition(0,0);
        super.onPause();
    }

    @Override
    protected void initWidgets() {
        if (mSelectMedia == null && getIntent() != null) {
            mSelectMedia = getIntent().getParcelableArrayListExtra(PictureConfig.EXTRA_SELECT_LIST);
        }

        if (mSelectMedia == null || mSelectMedia.isEmpty()) {
            finish();
        }

        FrameLayout contentView = new FrameLayout(this) {

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int height = MeasureSpec.getSize(heightMeasureSpec);

                measureChild(mTitleBar, widthMeasureSpec, heightMeasureSpec);
                getChildAt(0).measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height - mTitleBar.getMeasuredHeight(), MeasureSpec.EXACTLY));
                setMeasuredDimension(width, height);
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                mTitleBar.layout(0, 0, mTitleBar.getMeasuredWidth(), mTitleBar.getMeasuredHeight());
                View child = getChildAt(0);
                child.layout( 0, mTitleBar.getMeasuredHeight(), child.getMeasuredWidth(), mTitleBar.getMeasuredHeight() + child.getMeasuredHeight());
            }
        };
        container = contentView;
        setContentView(contentView);

        if (PictureMimeType.isHasVideo(mSelectMedia.get(0).getMimeType())) {
            mMediaType = MediaType.SINGLE_VIDEO;
            createSingleVideoContainer(contentView, mSelectMedia);
        } else if (mSelectMedia.size() > 1) {
            mMediaType = MediaType.MULTI_IMAGE;
        } else {
            mMediaType = MediaType.SINGLE_IMAGE;
            createSingleImageContainer(contentView);
        }

        mTitleBar = new InstagramTitleBar(this, config, mMediaType);
        contentView.addView(mTitleBar);
        mTitleBar.setClickListener(new InstagramTitleBar.OnTitleBarItemOnClickListener() {
            @Override
            public void onLeftViewClick() {
                finish();
            }

            @Override
            public void onCenterViewClick() {

            }

            @Override
            public void onRightViewClick() {
                if (mMediaType == MediaType.SINGLE_IMAGE) {
                    ((InstagramMediaSingleImageContainer)contentView.getChildAt(0)).onSaveImage(uri -> {
                        setResult(RESULT_OK, new Intent().putExtra(UCrop.EXTRA_OUTPUT_URI, uri));
                        finish();
                    });
                }
            }
        });
    }

    @Override
    public void initPictureSelectorStyle() {
        if (config.style != null) {
            if (config.style.pictureContainerBackgroundColor != 0) {
                container.setBackgroundColor(config.style.pictureContainerBackgroundColor);
            }
        }
        mTitleBar.setBackgroundColor(colorPrimary);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSelectMedia.size() > 0) {
            PictureSelector.saveSelectorList(outState, mSelectMedia);
        }
        if (mMediaType == MediaType.SINGLE_IMAGE && mSingleImageUri != null) {
            outState.putParcelable(EXTRA_SINGLE_IMAGE_URI, mSingleImageUri);
        }
    }

    private void createSingleVideoContainer(FrameLayout contentView, List<LocalMedia> selectMedia) {
        if (getIntent() != null) {
            isAspectRatio = getIntent().getBooleanExtra(EXTRA_ASPECT_RATIO, false);
            mAspectRatio = getIntent().getFloatExtra(EXTRA_ASPECT_RATIO_VALUE, 0);
        }
        InstagramMediaSingleVideoContainer singleVideoContainer = new InstagramMediaSingleVideoContainer(this, selectMedia.get(0), isAspectRatio, mAspectRatio);
        contentView.addView(singleVideoContainer, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    }

    private void createSingleImageContainer(FrameLayout contentView) {
        if (getIntent() != null) {
            isAspectRatio = getIntent().getBooleanExtra(EXTRA_ASPECT_RATIO, false);
            mAspectRatio = getIntent().getFloatExtra(EXTRA_ASPECT_RATIO_VALUE, 0);
        }

        if (mSingleImageUri == null && getIntent() != null) {
            mSingleImageUri = getIntent().getParcelableExtra(EXTRA_SINGLE_IMAGE_URI);
        }
        if (mSingleImageUri == null) {
            finish();
        }
        InstagramMediaSingleImageContainer singleImageContainer = new InstagramMediaSingleImageContainer(this, config, mSingleImageUri, isAspectRatio, mAspectRatio);
        contentView.addView(singleImageContainer, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    }

     public static void launchActivity(Activity activity, PictureSelectionConfig config, List<LocalMedia> images, Bundle extras, int requestCode) {
        Intent intent = new Intent(activity.getApplicationContext(), InstagramMediaProcessActivity.class);
        intent.putExtra(PictureConfig.EXTRA_CONFIG, config);
        intent.putParcelableArrayListExtra(PictureConfig.EXTRA_SELECT_LIST,
                (ArrayList<? extends Parcelable>) images);
        if (extras != null) {
            intent.putExtras(extras);
        }
        activity.startActivityForResult(intent, requestCode);
        activity.overridePendingTransition(0,0);
    }
}
