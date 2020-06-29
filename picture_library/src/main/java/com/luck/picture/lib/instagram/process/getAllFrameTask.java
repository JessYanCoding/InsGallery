package com.luck.picture.lib.instagram.process;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;

import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.SdkVersionUtils;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * ================================================
 * Created by JessYan on 2020/6/22 16:10
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class getAllFrameTask extends AsyncTask<Void, Void, Void> {
    private WeakReference<Context> mContextWeakReference;
    private LocalMedia mMedia;
    private int mTotalThumbsCount;
    private long mStartPosition;
    private long mEndPosition;
    private OnSingleBitmapListener mOnSingleBitmapListener;
    private boolean isStop;

    public getAllFrameTask(Context context, LocalMedia media, int totalThumbsCount, long startPosition,
                           long endPosition, OnSingleBitmapListener onSingleBitmapListener) {
        mContextWeakReference = new WeakReference<>(context);
        mMedia = media;
        mTotalThumbsCount = totalThumbsCount;
        mStartPosition = startPosition;
        mEndPosition = endPosition;
        mOnSingleBitmapListener = onSingleBitmapListener;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Context context = mContextWeakReference.get();
        if (context != null) {
            try {
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                Uri uri;
                if (SdkVersionUtils.checkedAndroid_Q() && PictureMimeType.isContent(mMedia.getPath())) {
                    uri = Uri.parse(mMedia.getPath());
                } else {
                    uri = Uri.fromFile(new File(mMedia.getPath()));
                }
                mediaMetadataRetriever.setDataSource(context, uri);

                long interval = (mEndPosition - mStartPosition) / (mTotalThumbsCount - 1);
                for (long i = 0; i < mTotalThumbsCount; ++i) {
                    if (isStop) {
                        break;
                    }
                    long frameTime = mStartPosition + interval * i;
                    Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(frameTime * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    if(bitmap == null){ continue;}
                    try {
                        int cropWidth = Math.min(bitmap.getWidth(), bitmap.getHeight());
                        int cropOffsetX = (bitmap.getWidth() - cropWidth) / 2;
                        int cropOffsetY = (bitmap.getHeight() - cropWidth) / 2;
                        bitmap = Bitmap.createBitmap(bitmap, cropOffsetX, cropOffsetY, cropWidth, cropWidth);
                    } catch (final Throwable t) {
                        t.printStackTrace();
                    }
                    if (mOnSingleBitmapListener != null) {
                        mOnSingleBitmapListener.onSingleBitmapComplete(bitmap);
                    }
                }
                mediaMetadataRetriever.release();
            } catch (final Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public interface OnSingleBitmapListener {
        void onSingleBitmapComplete(Bitmap bitmap);
    }
}
