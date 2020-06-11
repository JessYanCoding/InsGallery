package com.luck.picture.lib.instagram.process;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.luck.picture.lib.R;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.instagram.adapter.FilterItemView;
import com.luck.picture.lib.instagram.adapter.InstagramFilterAdapter;
import com.luck.picture.lib.instagram.adapter.InstagramFilterItemDecoration;
import com.luck.picture.lib.instagram.adapter.MediaAdapter;
import com.luck.picture.lib.instagram.filter.FilterType;
import com.luck.picture.lib.tools.ScreenUtils;
import com.luck.picture.lib.tools.ToastUtils;
import com.yalantis.ucrop.callback.BitmapLoadCallback;
import com.yalantis.ucrop.model.ExifInfo;
import com.yalantis.ucrop.task.BitmapLoadTask;
import com.yalantis.ucrop.util.BitmapLoadUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter;

/**
 * ================================================
 * Created by JessYan on 2020/6/1 15:27
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InstagramMediaMultiImageContainer extends FrameLayout implements InstagramFilterAdapter.OnItemClickListener {
    private final RecyclerView mMediaRecyclerView;
    private final RecyclerView mFilterRecyclerView;
    private final MediaAdapter mMediaAdapter;
    private final InstagramFilterAdapter mFilterAdapter;
    private final List<Bitmap> mBitmaps;
    private final View mMediaLoadingView;
    private final View mFilterLoadingView;
    private int mSelectionPosition;
    private GPUImage mGpuImage;

    public InstagramMediaMultiImageContainer(@NonNull Context context, PictureSelectionConfig config, List<LocalMedia> medias, boolean isAspectRatio, float aspectRatio) {
        super(context);
        mBitmaps = new ArrayList<>();

        mMediaRecyclerView = new RecyclerView(context);
        mMediaRecyclerView.setOverScrollMode(OVER_SCROLL_NEVER);
        mMediaRecyclerView.setHasFixedSize(true);
        mMediaRecyclerView.addItemDecoration(new InstagramFilterItemDecoration(ScreenUtils.dip2px(context, 9), 3));
        mMediaRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        mMediaAdapter = new MediaAdapter(context, medias);
        addView(mMediaRecyclerView);

        mFilterRecyclerView = new RecyclerView(context);
        mFilterRecyclerView.setOverScrollMode(OVER_SCROLL_NEVER);
        mFilterRecyclerView.setHasFixedSize(true);
        mFilterRecyclerView.addItemDecoration(new InstagramFilterItemDecoration(ScreenUtils.dip2px(context, 9)));
        mFilterRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        mFilterAdapter = new InstagramFilterAdapter(context, config);
        mFilterAdapter.setOnItemClickListener(this);
        addView(mFilterRecyclerView);

        mMediaLoadingView = LayoutInflater.from(context).inflate(R.layout.picture_alert_dialog, this, false);
        addView(mMediaLoadingView);

        mFilterLoadingView = LayoutInflater.from(context).inflate(R.layout.picture_alert_dialog, this, false);
        addView(mFilterLoadingView);

        int maxBitmapSize = BitmapLoadUtils.calculateMaxBitmapSize(getContext());
        new LoadBitmapTask(context, this, mBitmaps, medias, maxBitmapSize).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        mMediaRecyclerView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY));
        mFilterRecyclerView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height - width, MeasureSpec.EXACTLY));
        measureChild(mMediaLoadingView, widthMeasureSpec, heightMeasureSpec);
        measureChild(mFilterLoadingView, widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;

        int viewTop = 0;
        int viewLeft = 0;
        mMediaRecyclerView.layout(viewLeft, viewTop, viewLeft + mMediaRecyclerView.getMeasuredWidth(), viewTop + mMediaRecyclerView.getMeasuredHeight());

        viewTop = width;
        viewLeft = 0;
        mFilterRecyclerView.layout(viewLeft, viewTop, viewLeft + mFilterRecyclerView.getMeasuredWidth(), viewTop + mFilterRecyclerView.getMeasuredHeight());

        viewTop = (width - mMediaLoadingView.getMeasuredHeight()) / 2;
        viewLeft = (width - mMediaLoadingView.getMeasuredWidth()) / 2;
        mMediaLoadingView.layout(viewLeft, viewTop, viewLeft + mMediaLoadingView.getMeasuredWidth(), viewTop + mMediaLoadingView.getMeasuredHeight());

        viewTop = width + ((height - width) - mFilterLoadingView.getMeasuredHeight()) / 2;
        viewLeft = (width - mFilterLoadingView.getMeasuredWidth()) / 2;
        mFilterLoadingView.layout(viewLeft, viewTop, viewLeft + mFilterLoadingView.getMeasuredWidth(), viewTop + mFilterLoadingView.getMeasuredHeight());
    }

    @Override
    public void onItemClick(View view, int position, FilterType filterType) {
        mFilterRecyclerView.smoothScrollBy(view.getLeft() - getMeasuredWidth() / 3, 0);

        if (mSelectionPosition != position) {
            if (mGpuImage == null) {
                mGpuImage = new GPUImage(getContext());
            }
            new ApplyFilterBitmapTask(this, filterType).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            int previousPosition = mSelectionPosition;
            mSelectionPosition = position;
            mFilterAdapter.setSelectionPosition(position);
            ((FilterItemView) view).selection(true);
            RecyclerView.ViewHolder previousHolder = mFilterRecyclerView.findViewHolderForAdapterPosition(previousPosition);
            if (previousHolder != null && previousHolder.itemView != null) {
                ((FilterItemView) previousHolder.itemView).selection(false);
            }
        }
    }

    private static class ApplyFilterBitmapTask extends AsyncTask<Void, Void, List<Bitmap>> {
        private WeakReference<InstagramMediaMultiImageContainer> mContainerWeakReference;
        private FilterType mFilterType;

        public ApplyFilterBitmapTask(InstagramMediaMultiImageContainer container, FilterType filterType) {
            mContainerWeakReference = new WeakReference<>(container);
            mFilterType = filterType;
        }

        @Override
        protected List<Bitmap> doInBackground(Void... voids) {
            InstagramMediaMultiImageContainer container = mContainerWeakReference.get();
            if (container != null) {
                container.mGpuImage.setFilter(FilterType.createFilterForType(container.getContext(), mFilterType));
                List<Bitmap> newBitMaps = new ArrayList<>();
                for (Bitmap bitmap : container.mBitmaps) {
                    newBitMaps.add(container.mGpuImage.getBitmapWithFilterApplied(bitmap));
                }
                return newBitMaps;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Bitmap> bitmaps) {
            InstagramMediaMultiImageContainer container = mContainerWeakReference.get();
            if (container != null && bitmaps != null) {
                container.mMediaAdapter.setBitmaps(bitmaps);
                container.mMediaAdapter.notifyDataSetChanged();
            }
        }
    }

    private static class LoadFilterBitmapTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;
        private WeakReference<InstagramMediaMultiImageContainer> mImageContainer;
        private Bitmap mBitmap;

        public LoadFilterBitmapTask(Context context, InstagramMediaMultiImageContainer imageContainer, Bitmap bitmap) {
            mContext = context;
            mImageContainer = new WeakReference<>(imageContainer);
            mBitmap = bitmap;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            InstagramMediaMultiImageContainer imageContainer = mImageContainer.get();
            if (imageContainer != null) {
                GPUImage gpuImage = new GPUImage(mContext);
                gpuImage.setFilter(new GPUImageGaussianBlurFilter(25));
                imageContainer.mFilterAdapter.getThumbnailBitmaps(mContext, gpuImage.getBitmapWithFilterApplied(mBitmap));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            InstagramMediaMultiImageContainer imageContainer = mImageContainer.get();
            if (imageContainer != null) {
                imageContainer.mFilterLoadingView.setVisibility(View.INVISIBLE);
                imageContainer.mFilterRecyclerView.setAdapter(imageContainer.mFilterAdapter);
            }
        }
    }

    public static class LoadBitmapTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;
        private WeakReference<InstagramMediaMultiImageContainer> mContainerWeakReference;
        private List<Bitmap> mBitmaps;
        private List<LocalMedia> mMedias;
        private int mMaxBitmapSize;

        public LoadBitmapTask(Context context, InstagramMediaMultiImageContainer container, List<Bitmap> bitmaps, List<LocalMedia> medias, int maxBitmapSize) {
            mContext = context;
            mContainerWeakReference = new WeakReference<>(container);
            mBitmaps = bitmaps;
            mMedias = medias;
            mMaxBitmapSize = maxBitmapSize;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            InstagramMediaMultiImageContainer container = mContainerWeakReference.get();
            if (container != null) {
                startLoadBitmapTask(mContext, container, mMedias, mMaxBitmapSize, mBitmaps);
            }
            return null;
        }

        private void startLoadBitmapTask(Context context, InstagramMediaMultiImageContainer container, List<LocalMedia> medias, int maxBitmapSize, List<Bitmap> bitmaps) {
            for (LocalMedia media : medias) {
                Uri uri;
                if (media.isCut()) {
                    uri = Uri.fromFile(new File(media.getCutPath()));
                } else {
                    uri = PictureMimeType.isContent(media.getPath()) ? Uri.parse(media.getPath()) : Uri.fromFile(new File(media.getPath()));
                }
                new BitmapLoadTask(context, uri, uri, maxBitmapSize, maxBitmapSize, new BitmapLoadCallbackImpl(context, container, bitmaps)).execute();
            }
            new FinishLoadBitmapTask(container, bitmaps).execute();
        }
    }

    public static class FinishLoadBitmapTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<InstagramMediaMultiImageContainer> mContainerWeakReference;
        private List<Bitmap> mBitmaps;

        public FinishLoadBitmapTask(InstagramMediaMultiImageContainer container, List<Bitmap> bitmaps) {
            mContainerWeakReference = new WeakReference<>(container);
            mBitmaps = bitmaps;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            InstagramMediaMultiImageContainer container = mContainerWeakReference.get();
            if (container != null) {
                container.mMediaLoadingView.setVisibility(View.INVISIBLE);
                container.mMediaAdapter.setBitmaps(mBitmaps);
                container.mMediaRecyclerView.setAdapter(container.mMediaAdapter);
            }
        }
    }

    private static class BitmapLoadCallbackImpl implements BitmapLoadCallback {
        private Context mContext;
        private List<Bitmap> mBitmaps;
        private WeakReference<InstagramMediaMultiImageContainer> mContainerWeakReference;

        public BitmapLoadCallbackImpl(Context context, InstagramMediaMultiImageContainer container, List<Bitmap> bitmaps) {
            mContext = context;
            mContainerWeakReference = new WeakReference<>(container);
            mBitmaps = bitmaps;
        }

        @Override
        public void onBitmapLoaded(@NonNull Bitmap bitmap, @NonNull ExifInfo exifInfo, @NonNull String imageInputPath, @Nullable String imageOutputPath) {
            mBitmaps.add(bitmap);
            InstagramMediaMultiImageContainer container = mContainerWeakReference.get();
            if (container != null && mBitmaps.size() == 1) {
                new LoadFilterBitmapTask(mContext, container, bitmap).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

        @Override
        public void onFailure(@NonNull Exception bitmapWorkerException) {
            ToastUtils.s(mContext, bitmapWorkerException.getMessage());
        }
    }
}
