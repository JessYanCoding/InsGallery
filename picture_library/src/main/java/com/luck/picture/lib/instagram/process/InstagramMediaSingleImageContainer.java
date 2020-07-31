package com.luck.picture.lib.instagram.process;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.luck.picture.lib.R;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.dialog.PictureCustomDialog;
import com.luck.picture.lib.instagram.InsGallery;
import com.luck.picture.lib.instagram.InstagramPreviewContainer;
import com.luck.picture.lib.instagram.adapter.FilterItemView;
import com.luck.picture.lib.instagram.adapter.InstagramFilterAdapter;
import com.luck.picture.lib.instagram.adapter.InstagramFilterItemDecoration;
import com.luck.picture.lib.instagram.filter.FilterType;
import com.luck.picture.lib.tools.ScreenUtils;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.util.BitmapLoadUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

/**
 * ================================================
 * Created by JessYan on 2020/6/1 15:27
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InstagramMediaSingleImageContainer extends FrameLayout implements InstagramFilterAdapter.OnItemClickListener, ProcessStateCallBack {
    private final GPUImageView mImageView;
    private final RecyclerView mRecyclerView;
    private Paint mPaint;
    private InstagramFilterAdapter mAdapter;
    private int mSelectionPosition;
    private final View mLoadingView;
    private PictureSelectionConfig mConfig;
    private Bitmap mBitmap;
    private int mSelectionFilter;
    private PictureCustomDialog mDialog;
    private GPUImage mGpuImage;

    public InstagramMediaSingleImageContainer(@NonNull Context context, PictureSelectionConfig config, Bitmap bitmap, boolean isAspectRatio, int selectionFilter) {
        super(context);
        mConfig = config;
        mBitmap = bitmap;
        mSelectionFilter = selectionFilter;
        setWillNotDraw(false);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK) {
            mPaint.setColor(Color.parseColor("#363636"));
        } else if (config.instagramSelectionConfig.getCurrentTheme() == InsGallery.THEME_STYLE_DARK_BLUE) {
            mPaint.setColor(Color.parseColor("#004561"));
        } else {
            mPaint.setColor(Color.parseColor("#efefef"));
        }

        mGpuImage = new GPUImage(context);

        mImageView = new GPUImageView(context);
        addView(mImageView);
        if (isAspectRatio) {
            float targetAspectRatio = InstagramPreviewContainer.getInstagramAspectRatio(bitmap.getWidth(), bitmap.getHeight());
            if (targetAspectRatio > 0) {
                mImageView.setRatio(targetAspectRatio);
            }
        }
        mImageView.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);
        mImageView.setImage(bitmap);

        mRecyclerView = new RecyclerView(context);
        mRecyclerView.setOverScrollMode(OVER_SCROLL_NEVER);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new InstagramFilterItemDecoration(ScreenUtils.dip2px(context, 9)));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        mAdapter = new InstagramFilterAdapter(context, config);
        if (mSelectionFilter > 0) {
            mImageView.setFilter(FilterType.createFilterForType(getContext(), mAdapter.getItem(mSelectionFilter)));
            mSelectionPosition = mSelectionFilter;
            mAdapter.setSelectionPosition(mSelectionFilter);
        }
        mAdapter.setOnItemClickListener(this);
        addView(mRecyclerView);

        mLoadingView = LayoutInflater.from(context).inflate(R.layout.picture_alert_dialog, this, false);
        addView(mLoadingView);

        new LoadBitmapTask(context, this, bitmap).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        mImageView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY));
        mRecyclerView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height - width, MeasureSpec.EXACTLY));
        measureChild(mLoadingView, widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;

        int viewTop = (width - mImageView.getMeasuredHeight()) / 2;
        int viewLeft = (width - mImageView.getMeasuredWidth()) / 2;
        mImageView.layout(viewLeft, viewTop, viewLeft + mImageView.getMeasuredWidth(), viewTop + mImageView.getMeasuredHeight());

        viewTop = width;
        viewLeft = 0;
        mRecyclerView.layout(viewLeft, viewTop, viewLeft + mRecyclerView.getMeasuredWidth(), viewTop + mRecyclerView.getMeasuredHeight());

        viewTop += ((height - width) - mLoadingView.getMeasuredHeight()) / 2;
        viewLeft = (width - mLoadingView.getMeasuredWidth()) / 2;
        mLoadingView.layout(viewLeft, viewTop, viewLeft + mLoadingView.getMeasuredWidth(), viewTop + mLoadingView.getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredWidth(), mPaint);
    }

    @Override
    public void onItemClick(View view, int position, FilterType filterType) {
        mRecyclerView.smoothScrollBy(view.getLeft() - getMeasuredWidth() / 3, 0);

        if (mSelectionPosition != position) {
            mImageView.setFilter(FilterType.createFilterForType(getContext(), filterType));
            mGpuImage.setFilter(FilterType.createFilterForType(getContext(), filterType));

            int previousPosition = mSelectionPosition;
            mSelectionPosition = position;
            mAdapter.setSelectionPosition(position);
            ((FilterItemView) view).selection(true);
            RecyclerView.ViewHolder previousHolder = mRecyclerView.findViewHolderForAdapterPosition(previousPosition);
            if (previousHolder != null && previousHolder.itemView != null) {
                ((FilterItemView) previousHolder.itemView).selection(false);
            } else {
                mAdapter.notifyItemChanged(previousPosition);
            }
        }
    }

    public void onSaveImage(GPUImageView.OnPictureSavedListener listener) {
        String fileName = System.currentTimeMillis() + ".jpg";
        new SaveTask(getContext().getApplicationContext(), "Filters", fileName, listener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onBack(InstagramMediaProcessActivity activity) {
        if (mSelectionFilter >= 0 && mSelectionFilter != mSelectionPosition) {
            if (!activity.isFinishing()) {
                if (mDialog == null) {
                    ProcessAlertView layout = new ProcessAlertView(getContext(), mConfig);
                    layout.setOnAlertListener(new ProcessAlertView.onAlertListener() {
                        @Override
                        public void onAgree() {
                            activity.finish();
                        }

                        @Override
                        public void onCancel() {
                            mDialog.dismiss();
                        }
                    });
                    mDialog = new PictureCustomDialog(getContext(), layout);
                    mDialog.setCancelable(true);
                    mDialog.setCanceledOnTouchOutside(false);
                }
                mDialog.show();
            }
        } else {
            activity.setResult(InstagramMediaProcessActivity.RESULT_MEDIA_PROCESS_CANCELED);
            activity.finish();
        }
    }

    @Override
    public void onCenterFeature(InstagramMediaProcessActivity activity, ImageView view) {

    }

    @Override
    public void onProcess(InstagramMediaProcessActivity activity) {
        if (mSelectionFilter == -1) {
            onSaveImage(uri -> {
                activity.setResult(Activity.RESULT_OK, new Intent().putExtra(UCrop.EXTRA_OUTPUT_URI, uri));
                activity.finish();
            });
        } else {
            if (mSelectionFilter != mSelectionPosition) {
                activity.setResult(Activity.RESULT_OK, new Intent().putExtra(InstagramMediaProcessActivity.EXTRA_SINGLE_IMAGE_SELECTION_FILTER, mSelectionPosition));
            }
            activity.finish();
        }
    }

    @Override
    public void onActivityResult(InstagramMediaProcessActivity activity, int requestCode, int resultCode, Intent data) {

    }

    private class SaveTask extends AsyncTask<Void, Void, Void> {
        private final String folderName;
        private final String fileName;
        private final int width;
        private final int height;
        private final GPUImageView.OnPictureSavedListener listener;
        private final Handler handler;
        private final WeakReference<Context> mContextWeakReference;

        public SaveTask(final Context context, final String folderName, final String fileName,
                        final GPUImageView.OnPictureSavedListener listener) {
            this(context, folderName, fileName, 0, 0, listener);
        }

        public SaveTask(final Context context, final String folderName, final String fileName, int width, int height,
                        final GPUImageView.OnPictureSavedListener listener) {
            mContextWeakReference = new WeakReference<>(context);
            this.folderName = folderName;
            this.fileName = fileName;
            this.width = width;
            this.height = height;
            this.listener = listener;
            handler = new Handler();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            try {
//                Bitmap result = width != 0 ? mImageView.capture(width, height) : mImageView.capture();
                Bitmap result = mGpuImage.getBitmapWithFilterApplied(mBitmap);
                saveImage(folderName, fileName, result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private void saveImage(final String folderName, final String fileName, final Bitmap image) {
            Context context = mContextWeakReference.get();
            if (context == null) {
                return;
            }

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
                        (path1, uri) -> {
                            if (listener != null) {
                                handler.post(() -> listener.onPictureSaved(Uri.fromFile(new File(path1))));
                            }
                        });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                BitmapLoadUtils.close(outputStream);
            }
        }
    }

    private static class LoadBitmapTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;
        private WeakReference<InstagramMediaSingleImageContainer> mImageContainer;
        private Bitmap mBitmap;

        public LoadBitmapTask(Context context, InstagramMediaSingleImageContainer imageContainer, Bitmap bitmap) {
            mContext = context;
            mImageContainer = new WeakReference<>(imageContainer);
            mBitmap = bitmap;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            InstagramMediaSingleImageContainer imageContainer = mImageContainer.get();
            if (imageContainer != null) {
                imageContainer.mAdapter.getThumbnailBitmaps(mContext, mBitmap);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            InstagramMediaSingleImageContainer imageContainer = mImageContainer.get();
            if (imageContainer != null) {
                imageContainer.mLoadingView.setVisibility(View.INVISIBLE);
                imageContainer.mRecyclerView.setAdapter(imageContainer.mAdapter);
                if (imageContainer.mSelectionFilter > 0) {
                    imageContainer.mRecyclerView.scrollToPosition(imageContainer.mSelectionFilter - 1);
                }
            }
        }
    }
}
