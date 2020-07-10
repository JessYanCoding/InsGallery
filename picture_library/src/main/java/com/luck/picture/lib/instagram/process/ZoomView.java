package com.luck.picture.lib.instagram.process;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.luck.picture.lib.tools.ScreenUtils;

/**
 * ================================================
 * Created by JessYan on 2020/7/9 11:26
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class ZoomView extends androidx.appcompat.widget.AppCompatImageView {
    private Paint mPaint;
    private Bitmap mBitmap;

    public ZoomView(Context context) {
        super(context);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(0XFFDDDDDD);
        mPaint.setStrokeWidth(ScreenUtils.dip2px(getContext(),2));
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        }
        canvas.drawRect(ScreenUtils.dip2px(getContext(), 1), ScreenUtils.dip2px(getContext(), 1), getMeasuredWidth()- ScreenUtils.dip2px(getContext(), 1), getMeasuredHeight() - ScreenUtils.dip2px(getContext(), 1), mPaint);
    }
}
