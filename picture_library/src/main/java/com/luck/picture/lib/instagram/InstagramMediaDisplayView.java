package com.luck.picture.lib.instagram;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import com.luck.picture.lib.entity.LocalMedia;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * ================================================
 * Created by JessYan on 2020/5/29 17:28
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InstagramMediaDisplayView extends FrameLayout {
    private ImageView mImageView;
    private VideoView mVideoView;

    public InstagramMediaDisplayView(@NonNull Context context, List<LocalMedia> selectMedia, InstagramMediaProcessActivity.MediaType mediaType, boolean isAspectRatio) {
        super(context);

        switch (mediaType) {
            case SINGLE_IMAGE:
                mImageView = new ImageView(context);
                selectMedia.get(0);
                addView(mImageView);
                break;
            case SINGLE_VIDEO:
                mVideoView = new VideoView(context);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = width;
        setMeasuredDimension(width, height);
    }


}
