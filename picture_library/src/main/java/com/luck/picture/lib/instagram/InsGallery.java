package com.luck.picture.lib.instagram;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;

import com.luck.picture.lib.PictureSelectionModel;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.R;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.engine.CacheResourcesEngine;
import com.luck.picture.lib.engine.ImageEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.luck.picture.lib.style.PictureCropParameterStyle;
import com.luck.picture.lib.style.PictureParameterStyle;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;

import java.util.List;

import androidx.core.content.ContextCompat;

/**
 * ================================================
 * 此类只是作为 InsGallery 的快捷入口, 帮助开发者快速上手,
 * 如果你需要更多的自定义功能还是需要调用 PictureSelector 的接口传入需要自定义的参数,
 * 使用 {@link #applyInstagramOptions(Context, PictureSelectionModel)} 方法即可快速应用 InsGallery 需要的所有设置
 * <p>
 * 你可以理解为 InsGallery 就是依赖于 PictureSelector 创建的一套皮肤, 所以我不会大改他的代码, 尽量沿用他本身的设计
 * 依赖于强大的开源社区后, InsGallery 就可以在保证稳定性的情况下, 拥有更多的精力去实现 UI 效果, 尽量还原一个产品级的 Instagram Gallery
 * InsGallery 使用纯编码, 0 xml 的方式构建, 所有父容器都是使用 FrameLayout 自定义完成, 在保证效率的同时，拥有高度的灵活性。
 * <p>
 * Created by JessYan on 2020/4/27 18:07
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public final class InsGallery {
    public static final int THEME_STYLE_DEFAULT = 0;
    public static final int THEME_STYLE_DARK = 1;
    public static final int THEME_STYLE_DARK_BLUE = 2;
    public static int currentTheme = THEME_STYLE_DEFAULT;

    private InsGallery() {
        throw new IllegalStateException("you can't instantiate me!");
    }

    public static void openGallery(Activity activity, ImageEngine engine, OnResultCallbackListener listener) {
        openGallery(activity, engine, null, null, listener);
    }

    public static void openGallery(Activity activity, ImageEngine engine, CacheResourcesEngine cacheResourcesEngine, OnResultCallbackListener listener) {
        openGallery(activity, engine, cacheResourcesEngine, null, listener);
    }

    public static void openGallery(Activity activity, ImageEngine engine, CacheResourcesEngine cacheResourcesEngine, List<LocalMedia> selectionMedia, OnResultCallbackListener listener) {
        applyInstagramOptions(activity.getApplicationContext(), PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofAll()))// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .imageEngine(engine)// 外部传入图片加载引擎，必传项
                .loadCacheResourcesCallback(cacheResourcesEngine)// 获取图片资源缓存，主要是解决华为10部分机型在拷贝文件过多时会出现卡的问题，这里可以判断只在会出现一直转圈问题机型上使用
                .selectionData(selectionMedia)// 是否传入已选图片
                .forResult(listener);
    }

    public static void openGallery(Activity activity, ImageEngine engine, CacheResourcesEngine cacheResourcesEngine, List<LocalMedia> selectionMedia, InstagramSelectionConfig instagramConfig, OnResultCallbackListener listener) {
        applyInstagramOptions(activity.getApplicationContext(), instagramConfig, PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofAll()))// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .imageEngine(engine)// 外部传入图片加载引擎，必传项
                .loadCacheResourcesCallback(cacheResourcesEngine)// 获取图片资源缓存，主要是解决华为10部分机型在拷贝文件过多时会出现卡的问题，这里可以判断只在会出现一直转圈问题机型上使用
                .selectionData(selectionMedia)// 是否传入已选图片
                .forResult(listener);
    }

    public static void openGallery(Activity activity, ImageEngine engine, CacheResourcesEngine cacheResourcesEngine, List<LocalMedia> selectionMedia) {
        applyInstagramOptions(activity.getApplicationContext(), PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofAll()))// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .imageEngine(engine)// 外部传入图片加载引擎，必传项
                .loadCacheResourcesCallback(cacheResourcesEngine)// 获取图片资源缓存，主要是解决华为10部分机型在拷贝文件过多时会出现卡的问题，这里可以判断只在会出现一直转圈问题机型上使用
                .selectionData(selectionMedia)// 是否传入已选图片
                .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
    }

    public static void openGallery(Activity activity, ImageEngine engine, CacheResourcesEngine cacheResourcesEngine, List<LocalMedia> selectionMedia, InstagramSelectionConfig instagramConfig) {
        applyInstagramOptions(activity.getApplicationContext(), instagramConfig, PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofAll()))// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .imageEngine(engine)// 外部传入图片加载引擎，必传项
                .loadCacheResourcesCallback(cacheResourcesEngine)// 获取图片资源缓存，主要是解决华为10部分机型在拷贝文件过多时会出现卡的问题，这里可以判断只在会出现一直转圈问题机型上使用
                .selectionData(selectionMedia)// 是否传入已选图片
                .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
    }

    public static void openGallery(Activity activity, ImageEngine engine, CacheResourcesEngine cacheResourcesEngine, List<LocalMedia> selectionMedia, int requestCode) {
        applyInstagramOptions(activity.getApplicationContext(), PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofAll()))// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .imageEngine(engine)// 外部传入图片加载引擎，必传项
                .loadCacheResourcesCallback(cacheResourcesEngine)// 获取图片资源缓存，主要是解决华为10部分机型在拷贝文件过多时会出现卡的问题，这里可以判断只在会出现一直转圈问题机型上使用
                .selectionData(selectionMedia)// 是否传入已选图片
                .forResult(requestCode);//结果回调onActivityResult code
    }


    public static PictureSelectionModel applyInstagramOptions(Context context, PictureSelectionModel selectionModel) {
        return applyInstagramOptions(context, InstagramSelectionConfig.createConfig().setCurrentTheme(currentTheme), selectionModel);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public static PictureSelectionModel applyInstagramOptions(Context context, InstagramSelectionConfig instagramConfig, PictureSelectionModel selectionModel) {
        return selectionModel
                .setInstagramConfig(instagramConfig)
                .setPictureStyle(createInstagramStyle(context))// 动态自定义相册主题
                .setPictureCropStyle(createInstagramCropStyle(context))// 动态自定义裁剪主题
                .setPictureWindowAnimationStyle(new PictureWindowAnimationStyle())// 自定义相册启动退出动画
                .isWithVideoImage(false)// 图片和视频是否可以同选,只在ofAll模式下有效
                .maxSelectNum(9)// 最大图片选择数量
                .minSelectNum(1)// 最小选择数量
                .maxVideoSelectNum(1) // 视频最大选择数量，如果没有单独设置的需求则可以不设置，同用maxSelectNum字段
                //.minVideoSelectNum(1)// 视频最小选择数量，如果没有单独设置的需求则可以不设置，同用minSelectNum字段
                .imageSpanCount(4)// 每行显示个数
                .isReturnEmpty(false)// 未选择数据时点击按钮是否可以返回
                //.isAndroidQTransform(false)// 是否需要处理Android Q 拷贝至应用沙盒的操作，只针对compress(false); && enableCrop(false);有效,默认处理
                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)// 设置相册Activity方向，不设置默认使用系统
                .selectionMode(PictureConfig.MULTIPLE)// 多选 or 单选
                .isSingleDirectReturn(false)// 单选模式下是否直接返回，PictureConfig.SINGLE模式下有效
                .isPreviewImage(true)// 是否可预览图片
                .isPreviewVideo(true)// 是否可预览视频
                //.querySpecifiedFormatSuffix(PictureMimeType.ofJPEG())// 查询指定后缀格式资源
                .enablePreviewAudio(false) // 是否可播放音频
                .isCamera(false)// 是否显示拍照按钮
                //.isMultipleSkipCrop(false)// 多图裁剪时是否支持跳过，默认支持
                //.isMultipleRecyclerAnimation(false)// 多图裁剪底部列表显示动画效果
                .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                //.imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                .isEnableCrop(true)// 是否裁剪
                //.basicUCropConfig()//对外提供所有UCropOptions参数配制，但如果PictureSelector原本支持设置的还是会使用原有的设置
                .isCompress(false)// 是否压缩
                //.compressQuality(80)// 图片压缩后输出质量 0~ 100
                .synOrAsy(true)//同步true或异步false 压缩 默认同步
                //.queryMaxFileSize(10)// 只查多少M以内的图片、视频、音频  单位M
                //.compressSavePath(getPath())//压缩图片保存地址
                .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                .showCropGrid(true)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                .isOpenClickSound(true)// 是否开启点击声音
                //.isDragFrame(false)// 是否可拖动裁剪框(固定)
                //.videoMinSecond(10)
                .videoMaxSecond(600)
                .videoMinSecond(3)
                .recordVideoSecond(60)//录制视频秒数 默认60s
                .recordVideoMinSecond(3)//最低录制秒数
                .cutOutQuality(90)// 裁剪输出质量 默认100
                .minimumCompressSize(100)// 小于100kb的图片不压缩
                //.rotateEnabled(false) // 裁剪是否可旋转图片
                //.scaleEnabled(false)// 裁剪是否可放大缩小图片
                //.videoQuality()// 视频录制质量 0 or 1
                //.videoSecond()//显示多少秒以内的视频or音频也可适用
                ;
    }

    public static PictureParameterStyle createInstagramStyle(Context context) {
        // 相册主题
        PictureParameterStyle mPictureParameterStyle = new PictureParameterStyle();
        // 是否改变状态栏字体颜色(黑白切换)
        if (currentTheme == THEME_STYLE_DARK || currentTheme == THEME_STYLE_DARK_BLUE) {
            mPictureParameterStyle.isChangeStatusBarFontColor = false;
        } else {
            mPictureParameterStyle.isChangeStatusBarFontColor = true;
        }
        // 是否开启右下角已完成(0/9)风格
        mPictureParameterStyle.isOpenCompletedNumStyle = false;
        // 是否开启类似QQ相册带数字选择风格
        mPictureParameterStyle.isOpenCheckNumStyle = true;
        // 相册状态栏背景色
        if (currentTheme == THEME_STYLE_DARK) {
            mPictureParameterStyle.pictureStatusBarColor = Color.parseColor("#1C1C1E");
        } else if (currentTheme == THEME_STYLE_DARK_BLUE) {
            mPictureParameterStyle.pictureStatusBarColor = Color.parseColor("#213040");
        } else {
            mPictureParameterStyle.pictureStatusBarColor = Color.parseColor("#FFFFFF");
        }
        // 相册列表标题栏背景色
        if (currentTheme == THEME_STYLE_DARK) {
            mPictureParameterStyle.pictureTitleBarBackgroundColor = Color.parseColor("#1C1C1E");
        } else if (currentTheme == THEME_STYLE_DARK_BLUE) {
            mPictureParameterStyle.pictureTitleBarBackgroundColor = Color.parseColor("#213040");
        } else {
            mPictureParameterStyle.pictureTitleBarBackgroundColor = Color.parseColor("#FFFFFF");
        }
        // 相册列表标题栏右侧上拉箭头
        mPictureParameterStyle.pictureTitleUpResId = R.drawable.picture_arrow_up;
        // 相册列表标题栏右侧下拉箭头
        mPictureParameterStyle.pictureTitleDownResId = R.drawable.picture_arrow_down;
        // 相册文件夹列表选中圆点
        mPictureParameterStyle.pictureFolderCheckedDotStyle = R.drawable.picture_orange_oval;
        // 相册返回箭头
        mPictureParameterStyle.pictureLeftBackIcon = R.drawable.picture_close;
        // 标题栏字体颜色
        if (currentTheme == THEME_STYLE_DARK) {
            mPictureParameterStyle.pictureTitleTextColor = ContextCompat.getColor(context, R.color.picture_color_white);
        } else if (currentTheme == THEME_STYLE_DARK_BLUE) {
            mPictureParameterStyle.pictureTitleTextColor = ContextCompat.getColor(context, R.color.picture_color_white);
        } else {
            mPictureParameterStyle.pictureTitleTextColor = ContextCompat.getColor(context, R.color.picture_color_black);
        }
        // 相册右侧取消按钮字体颜色  废弃 改用.pictureRightDefaultTextColor和.pictureRightDefaultTextColor
        if (currentTheme == THEME_STYLE_DARK) {
            mPictureParameterStyle.pictureRightDefaultTextColor = ContextCompat.getColor(context, R.color.picture_color_1766FF);
        } else if (currentTheme == THEME_STYLE_DARK_BLUE) {
            mPictureParameterStyle.pictureRightDefaultTextColor = Color.parseColor("#2FA6FF");
        } else {
            mPictureParameterStyle.pictureRightDefaultTextColor = ContextCompat.getColor(context, R.color.picture_color_1766FF);
        }
        // 相册父容器背景色
        if (currentTheme == THEME_STYLE_DARK) {
            mPictureParameterStyle.pictureContainerBackgroundColor = ContextCompat.getColor(context, R.color.picture_color_black);
        } else if (currentTheme == THEME_STYLE_DARK_BLUE) {
            mPictureParameterStyle.pictureContainerBackgroundColor = Color.parseColor("#18222D");
        } else {
            mPictureParameterStyle.pictureContainerBackgroundColor = ContextCompat.getColor(context, R.color.picture_color_white);
        }
        // 相册列表勾选图片样式
        mPictureParameterStyle.pictureCheckedStyle = R.drawable.picture_instagram_num_selector;
        // 相册列表底部背景色
        mPictureParameterStyle.pictureBottomBgColor = ContextCompat.getColor(context, R.color.picture_color_fa);
        // 已选数量圆点背景样式
        mPictureParameterStyle.pictureCheckNumBgStyle = R.drawable.picture_num_oval;
        // 相册列表底下预览文字色值(预览按钮可点击时的色值)
        mPictureParameterStyle.picturePreviewTextColor = ContextCompat.getColor(context, R.color.picture_color_fa632d);
        // 相册列表底下不可预览文字色值(预览按钮不可点击时的色值)
        mPictureParameterStyle.pictureUnPreviewTextColor = ContextCompat.getColor(context, R.color.picture_color_9b);
        // 相册列表已完成色值(已完成 可点击色值)
        mPictureParameterStyle.pictureCompleteTextColor = ContextCompat.getColor(context, R.color.picture_color_fa632d);
        // 相册列表未完成色值(请选择 不可点击色值)
        mPictureParameterStyle.pictureUnCompleteTextColor = ContextCompat.getColor(context, R.color.picture_color_9b);
        // 外部预览界面删除按钮样式
        mPictureParameterStyle.pictureExternalPreviewDeleteStyle = R.drawable.picture_icon_black_delete;
        // 外部预览界面是否显示删除按钮
        mPictureParameterStyle.pictureExternalPreviewGonePreviewDelete = true;
//        // 自定义相册右侧文本内容设置
        mPictureParameterStyle.pictureRightDefaultText = context.getString(R.string.next);
        return mPictureParameterStyle;
    }

    public static PictureCropParameterStyle createInstagramCropStyle(Context context) {
        if (currentTheme == THEME_STYLE_DARK) {
            return new PictureCropParameterStyle(
                    Color.parseColor("#1C1C1E"),
                    Color.parseColor("#1C1C1E"),
                    Color.parseColor("#1C1C1E"),
                    ContextCompat.getColor(context, R.color.picture_color_white),
                    false);
        } else if (currentTheme == THEME_STYLE_DARK_BLUE) {
            return new PictureCropParameterStyle(
                    Color.parseColor("#213040"),
                    Color.parseColor("#213040"),
                    Color.parseColor("#213040"),
                    ContextCompat.getColor(context, R.color.picture_color_white),
                    false);
        }
        return new PictureCropParameterStyle(
                ContextCompat.getColor(context, R.color.picture_color_white),
                ContextCompat.getColor(context, R.color.picture_color_white),
                ContextCompat.getColor(context, R.color.picture_color_white),
                ContextCompat.getColor(context, R.color.picture_color_black),
                true);
    }

    public static void setCurrentTheme(int currentTheme) {
        InsGallery.currentTheme = currentTheme;
    }
}
