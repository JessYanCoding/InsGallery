package com.luck.picture.lib.hula;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;

import androidx.core.content.ContextCompat;

import com.luck.picture.lib.PictureSelectionModel;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.R;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.engine.CacheResourcesEngine;
import com.luck.picture.lib.engine.ImageEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.instagram.InsGallery;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.luck.picture.lib.style.PictureCropParameterStyle;
import com.luck.picture.lib.style.PictureParameterStyle;

import java.util.List;

public final class HulaGallery {

    private HulaGallery() {
    }

    public static void openGallery(Activity activity, ImageEngine engine, CacheResourcesEngine cacheResourcesEngine, List<LocalMedia> selectionMedia, OnResultCallbackListener listener) {
        applyHulaOptions(activity.getApplicationContext(), PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofAll()))// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .imageEngine(engine)// 外部传入图片加载引擎，必传项
                .loadCacheResourcesCallback(cacheResourcesEngine)// 获取图片资源缓存，主要是解决华为10部分机型在拷贝文件过多时会出现卡的问题，这里可以判断只在会出现一直转圈问题机型上使用
                .selectionData(selectionMedia)// 是否传入已选图片
                .forResult(listener);
    }

    public static void openGallery(Activity activity, ImageEngine engine, CacheResourcesEngine cacheResourcesEngine, List<LocalMedia> selectionMedia) {
        applyHulaOptions(activity.getApplicationContext(), PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofAll()))// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .imageEngine(engine)// 外部传入图片加载引擎，必传项
                .loadCacheResourcesCallback(cacheResourcesEngine)// 获取图片资源缓存，主要是解决华为10部分机型在拷贝文件过多时会出现卡的问题，这里可以判断只在会出现一直转圈问题机型上使用
                .selectionData(selectionMedia)// 是否传入已选图片
                .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public static PictureSelectionModel applyHulaOptions(Context context, PictureSelectionModel selectionModel) {
        InsGallery.setCurrentTheme(InsGallery.THEME_STYLE_DARK);
        return InsGallery.applyInstagramOptions(context, selectionModel)
                .setLanguage(Constants.LanguageConfig.HULA)
                .setPictureStyle(createHulaStyle(context))
                .setPictureCropStyle(createHulaCropStyle(context))
                .isOpenClickSound(false)
                ;
    }

    public static PictureParameterStyle createHulaStyle(Context context) {
        PictureParameterStyle style = InsGallery.createInstagramStyle(context);
        style.pictureContainerBackgroundColor = ContextCompat.getColor(context, R.color.picture_color_black);
        style.pictureTitleBarBackgroundColor = Color.parseColor("#33000000");
        style.pictureStatusBarColor = Color.parseColor("#000000");
        style.pictureNavBarColor = Color.parseColor("#000000");
        style.pictureRightDefaultTextColor = Color.parseColor("#333333");
        return style;
    }

    public static PictureCropParameterStyle createHulaCropStyle(Context context) {
        return new PictureCropParameterStyle(
                Color.parseColor("#33000000"),
                Color.parseColor("#000000"),
                Color.parseColor("#000000"),
                ContextCompat.getColor(context, R.color.picture_color_white),
                false);
    }

}
