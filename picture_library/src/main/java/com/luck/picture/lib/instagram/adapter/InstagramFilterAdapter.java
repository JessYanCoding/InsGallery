package com.luck.picture.lib.instagram.adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;

import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.instagram.filter.FilterType;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

/**
 * ================================================
 * Created by JessYan on 2020/6/2 16:09
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InstagramFilterAdapter extends RecyclerView.Adapter<InstagramFilterAdapter.Holder> {
    private Context mContext;
    private PictureSelectionConfig mConfig;
    private List<FilterType> mFilters;
    private List<Bitmap> mBitmaps;
    private OnItemClickListener mOnItemClickListener;
    private int mSelectionPosition;

    public InstagramFilterAdapter(Context context, PictureSelectionConfig config) {
        mContext = context;
        mConfig = config;
        mFilters = FilterType.createFilterList();
        mBitmaps = new ArrayList<>();
    }

    public void getThumbnailBitmaps(Context context, Bitmap bitmap) {
        List<GPUImageFilter> imageFilters = FilterType.createImageFilterList(context);
        GPUImage.getBitmapForMultipleFilters(bitmap, imageFilters, item -> mBitmaps.add(item));
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Holder holder = new Holder(new FilterItemView(mContext, mConfig));
        holder.itemView.setOnClickListener(v -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(holder.itemView, holder.getAdapterPosition(), mFilters.get(holder.getAdapterPosition()));
            }
            AnimatorSet set = new AnimatorSet();
            set.playTogether(
                    ObjectAnimator.ofFloat(holder.itemView, "scaleX", 1f, 1.05f, 1f),
                    ObjectAnimator.ofFloat(holder.itemView, "scaleY", 1f, 1.05f, 1f)
            );
            set.setDuration(200);
            set.start();
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ((FilterItemView) holder.itemView).refreshFilter(mFilters.get(position), mBitmaps.get(position), position, mSelectionPosition);
    }

    public FilterType getItem(int position) {
        return mFilters.get(position);
    }

    @Override
    public int getItemCount() {
        return mFilters.size();
    }

    public void setSelectionPosition(int selectionPosition) {
        mSelectionPosition = selectionPosition;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, FilterType filterType);
    }

    public static class Holder extends RecyclerView.ViewHolder {
        public Holder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
