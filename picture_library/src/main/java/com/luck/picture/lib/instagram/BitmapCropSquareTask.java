package com.luck.picture.lib.instagram;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import com.luck.picture.lib.tools.ToastUtils;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.util.BitmapLoadUtils;

import java.io.File;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

/**
 * ================================================
 * Created by JessYan on 2020/6/5 18:48
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class BitmapCropSquareTask extends AsyncTask<Void, Void, Throwable> {
    private final Bitmap mBitmap;
    private final String mImageOutputPath;
    private final WeakReference<PictureSelectorInstagramStyleActivity>  mActivityWeakReference;
    public static final String EXTRA_FROM_CAMERA = "extra_from_camera";

    public BitmapCropSquareTask(Bitmap bitmap, String imageOutputPath, PictureSelectorInstagramStyleActivity activity) {
        mBitmap = bitmap;
        mImageOutputPath = imageOutputPath;
        mActivityWeakReference = new WeakReference<>(activity);
    }

    @Override
    protected Throwable doInBackground(Void... voids) {
        PictureSelectorInstagramStyleActivity activity = mActivityWeakReference.get();
        if (activity == null) {
            return new NullPointerException("Activity is null");
        } else if(mBitmap == null) {
            return new NullPointerException("Bitmap is null");
        } else if(mImageOutputPath.isEmpty()) {
            return new NullPointerException("ImageOutputPath is null");
        }

        OutputStream outputStream = null;
        try {
            int cropWidth = Math.min(mBitmap.getWidth(), mBitmap.getHeight());
            int cropOffsetX = (mBitmap.getWidth() - cropWidth) / 2;
            int cropOffsetY = (mBitmap.getHeight() - cropWidth) / 2;
            Bitmap croppedBitmap = Bitmap.createBitmap(mBitmap, cropOffsetX, cropOffsetY, cropWidth, cropWidth);

            outputStream = activity.getApplicationContext().getContentResolver().openOutputStream(Uri.fromFile(new File(mImageOutputPath)));
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            croppedBitmap.recycle();
        } catch (Throwable t) {
            t.printStackTrace();
            return t;
        } finally {
            BitmapLoadUtils.close(outputStream);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Throwable throwable) {
        PictureSelectorInstagramStyleActivity activity = mActivityWeakReference.get();
        if (activity != null && throwable == null) {
            activity.onActivityResult(UCrop.REQUEST_CROP, Activity.RESULT_OK, new Intent()
                    .putExtra(EXTRA_FROM_CAMERA, true)
                    .putExtra(UCrop.EXTRA_OUTPUT_URI, Uri.fromFile(new File(mImageOutputPath))));
        } else if (throwable != null) {
            ToastUtils.s(activity, throwable.getMessage());
        }
    }
}
