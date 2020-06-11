package com.luck.picture.lib.instagram.process;

import android.content.Context;
import android.widget.FrameLayout;

import com.luck.picture.lib.entity.LocalMedia;

import androidx.annotation.NonNull;

/**
 * ================================================
 * Created by JessYan on 2020/6/1 15:27
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InstagramMediaSingleVideoContainer extends FrameLayout implements ProcessStateCallBack {

    public InstagramMediaSingleVideoContainer(@NonNull Context context, LocalMedia media, boolean isAspectRatio, float aspectRatio) {
        super(context);
    }

    @Override
    public void onBack(InstagramMediaProcessActivity activity) {
        activity.setResult(InstagramMediaProcessActivity.RESULT_MEDIA_PROCESS_CANCELED);
        activity.finish();
    }

    @Override
    public void onCenterFeature(InstagramMediaProcessActivity activity) {

    }

    @Override
    public void onProcess(InstagramMediaProcessActivity activity) {

    }
}
