package com.luck.picture.lib.instagram.adapter;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ================================================
 * Created by JessYan on 2020/6/10 11:29
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InstagramFrameItemDecoration extends RecyclerView.ItemDecoration {
    private int spacing;

    public InstagramFrameItemDecoration(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position == 0) {
            outRect.left = spacing;
            outRect.right = 1;
        } else if (position == parent.getAdapter().getItemCount() - 1) {
            outRect.right = spacing;
        } else {
            outRect.right = 1;
        }
    }
}
