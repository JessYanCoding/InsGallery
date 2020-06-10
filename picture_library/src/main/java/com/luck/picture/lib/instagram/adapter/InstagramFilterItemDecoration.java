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
public class InstagramFilterItemDecoration extends RecyclerView.ItemDecoration {
    private int spacing;
    private int multiplier = 2;

    public InstagramFilterItemDecoration(int spacing) {
        this.spacing = spacing;
    }

    public InstagramFilterItemDecoration(int spacing, int multiplier) {
        this.spacing = spacing;
        this.multiplier = multiplier;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position == 0) {
            outRect.left = spacing * multiplier;
            outRect.right = spacing;
        } else if (position == parent.getAdapter().getItemCount() - 1) {
            outRect.right = spacing * multiplier;
        } else {
            outRect.right = spacing;
        }
    }
}
