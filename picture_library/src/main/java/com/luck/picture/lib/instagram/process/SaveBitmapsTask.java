package com.luck.picture.lib.instagram.process;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;

import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.SdkVersionUtils;
import com.yalantis.ucrop.util.BitmapLoadUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * Created by JessYan on 2020/6/15 16:04
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class SaveBitmapsTask extends AsyncTask<Void, Void, Void> {
    private final WeakReference<Context> mContextWeakReference;
    private final WeakReference<InstagramMediaProcessActivity> mActivityWeakReference;
    private final String folderName;
    private final List<Bitmap> mBitmaps;
    private final List<LocalMedia> mLoadedMedias;
    private final Handler mHandler;
    private int mCount;

    public SaveBitmapsTask(Context context, InstagramMediaProcessActivity activity, String folderName, List<Bitmap> bitmaps, List<LocalMedia> loadedMedias) {
        mContextWeakReference = new WeakReference<>(context);
        mActivityWeakReference = new WeakReference<>(activity);
        this.folderName = folderName;
        mBitmaps = bitmaps;
        mLoadedMedias = loadedMedias;
        mHandler = new Handler();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        for (int i = 0; i < mBitmaps.size(); i++) {
            Bitmap bitmap = mBitmaps.get(i);
            LocalMedia media = mLoadedMedias.get(i);
            saveImage(folderName, bitmap, media);
        }
        return null;
    }

    private void finish() {
        InstagramMediaProcessActivity activity = mActivityWeakReference.get();
        if (activity != null) {
            activity.setResult(Activity.RESULT_OK, new Intent().putParcelableArrayListExtra(PictureConfig.EXTRA_SELECT_LIST, (ArrayList<? extends Parcelable>) mLoadedMedias));
            activity.finish();
        }
    }

    private void saveImage(final String folderName, final Bitmap image, final LocalMedia media) {
        Context context = mContextWeakReference.get();
        if (context == null) {
            return;
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File path = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(path, folderName + "/" + fileName);
        OutputStream outputStream = null;
        try {
            file.getParentFile().mkdirs();
            outputStream = context.getContentResolver().openOutputStream(Uri.fromFile(file));
            image.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            image.recycle();
            MediaScannerConnection.scanFile(context,
                    new String[]{
                            file.toString()
                    }, null,
                    (path1, uri) -> mHandler.post(() -> {
                        media.setCut(true);
                        media.setCutPath(path1);
                        media.setSize(new File(path1).length());
                        media.setAndroidQToPath(SdkVersionUtils.checkedAndroid_Q() ? path1 : media.getAndroidQToPath());
                        mCount++;
                        if (mCount == mBitmaps.size()) {
                            finish();
                        }
                    }));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            BitmapLoadUtils.close(outputStream);
        }
    }
}
