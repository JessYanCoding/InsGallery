package com.luck.picture.lib.instagram.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;

import com.luck.picture.lib.entity.LocalMedia;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ================================================
 * Created by JessYan on 2020/6/10 11:09
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.Holder> {
    private OnItemClickListener mOnItemClickListener;
    private Context mContext;
    private List<LocalMedia> mMediaList;
    private List<Bitmap> mBitmaps;

    public MediaAdapter(Context context, List<LocalMedia> mediaList) {
        mContext = context;
        mMediaList = mediaList;
    }

    public void setBitmaps(List<Bitmap> bitmaps) {
        mBitmaps = bitmaps;
    }

    public List<Bitmap> getBitmaps() {
        return mBitmaps;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Holder holder = new Holder(new MediaItemView(parent.getContext()));
        holder.itemView.setOnClickListener(v -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(holder.itemView, holder.getAdapterPosition(), mBitmaps.get(holder.getAdapterPosition()));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ((MediaItemView)holder.itemView).setImage(mBitmaps.get(position));
    }

    @Override
    public int getItemCount() {
        return mBitmaps.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, Bitmap bitmap);
    }

    public static class Holder extends RecyclerView.ViewHolder {
        public Holder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
