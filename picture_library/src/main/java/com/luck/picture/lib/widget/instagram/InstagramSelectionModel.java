package com.luck.picture.lib.widget.instagram;

import android.app.Activity;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.luck.picture.lib.PictureCustomCameraActivity;
import com.luck.picture.lib.PictureSelectionModel;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.PictureSelectorActivity;
import com.luck.picture.lib.PictureSelectorCameraEmptyActivity;
import com.luck.picture.lib.PictureSelectorWeChatStyleActivity;
import com.luck.picture.lib.R;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;
import com.luck.picture.lib.tools.DoubleUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class InstagramSelectionModel extends PictureSelectionModel {

    private InstagramSelectionConfig selectionConfig;

    public InstagramSelectionModel(PictureSelector selector, int chooseMode) {
        super(selector, chooseMode);
        selectionConfig = InstagramSelectionConfig.getCleanInstance();
        try {
            fatherToChild(super.selectionConfig, this.selectionConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InstagramSelectionModel(PictureSelector selector, int chooseMode, boolean camera) {
        super(selector, chooseMode, camera);
        selectionConfig = InstagramSelectionConfig.getCleanInstance();
        try {
            fatherToChild(super.selectionConfig, this.selectionConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> void fatherToChild(T father, T child) throws Exception {
        if (child.getClass().getSuperclass() != father.getClass()) {
            throw new Exception("child 不是 father 的子类");
        }
        Class<?> fatherClass = father.getClass();
        Field[] declaredFields = fatherClass.getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            Field field = declaredFields[i];
            Method method = fatherClass.getDeclaredMethod("get" + upperHeadChar(field.getName()));
            Object obj = method.invoke(father);
            field.setAccessible(true);
            field.set(child, obj);
        }
    }

    /**
     * 首字母大写，in:deleteDate，out:DeleteDate
     */
    public static String upperHeadChar(String in) {
        String head = in.substring(0, 1);
        String out = head.toUpperCase() + in.substring(1);
        return out;
    }

    /**
     * @param isInstagramStyle Select style with or without Instagram enabled
     * @return
     */
    public InstagramSelectionModel isInstagramStyle(boolean isInstagramStyle) {
        selectionConfig.isInstagramStyle = isInstagramStyle;
        return this;
    }

    /**
     * @param maxVideoSelectNum PictureSelector video max selection
     * @return
     */
    public InstagramSelectionModel maxVideoSelectNum(int maxVideoSelectNum) {
        selectionConfig.maxVideoSelectNum = maxVideoSelectNum;
        return this;
    }

    public InstagramSelectionModel recordVideoMinSecond(int recordVideoMinSecond) {
        selectionConfig.recordVideoMinSecond = recordVideoMinSecond;
        return this;
    }

    /**
     * Start to select media and wait for result.
     *
     * @param requestCode Identity of the request Activity or Fragment.
     */
    public void forResult(int requestCode) {
        if (!DoubleUtils.isFastDoubleClick()) {
            Activity activity = selector.getActivity();
            if (activity == null || selectionConfig == null) {
                return;
            }
            Intent intent;
            if (selectionConfig.camera && selectionConfig.isUseCustomCamera) {
                intent = new Intent(activity, PictureCustomCameraActivity.class);
            } else {
                intent = new Intent(activity, selectionConfig.camera
                        ? PictureSelectorCameraEmptyActivity.class :
                        selectionConfig.isWeChatStyle ? PictureSelectorWeChatStyleActivity.class
                                : selectionConfig.isInstagramStyle ? PictureSelectorInstagramStyleActivity.class : PictureSelectorActivity.class);
            }
            Fragment fragment = selector.getFragment();
            if (fragment != null) {
                fragment.startActivityForResult(intent, requestCode);
            } else {
                activity.startActivityForResult(intent, requestCode);
            }
            PictureWindowAnimationStyle windowAnimationStyle = selectionConfig.windowAnimationStyle;
            activity.overridePendingTransition(windowAnimationStyle != null &&
                    windowAnimationStyle.activityEnterAnimation != 0 ?
                    windowAnimationStyle.activityEnterAnimation :
                    R.anim.picture_anim_enter, R.anim.picture_anim_fade_in);
        }
    }

    /**
     * # replace for setPictureWindowAnimationStyle();
     * Start to select media and wait for result.
     * <p>
     * # Use PictureWindowAnimationStyle to achieve animation effects
     *
     * @param requestCode Identity of the request Activity or Fragment.
     */
    @Deprecated
    public void forResult(int requestCode, int enterAnim, int exitAnim) {
        if (!DoubleUtils.isFastDoubleClick()) {
            Activity activity = selector.getActivity();
            if (activity == null) {
                return;
            }
            Intent intent = new Intent(activity, selectionConfig != null && selectionConfig.camera
                    ? PictureSelectorCameraEmptyActivity.class :
                    selectionConfig.isWeChatStyle ? PictureSelectorWeChatStyleActivity.class
                            : selectionConfig.isInstagramStyle ? PictureSelectorInstagramStyleActivity.class : PictureSelectorActivity.class);
            Fragment fragment = selector.getFragment();
            if (fragment != null) {
                fragment.startActivityForResult(intent, requestCode);
            } else {
                activity.startActivityForResult(intent, requestCode);
            }
            activity.overridePendingTransition(enterAnim, exitAnim);
        }
    }

    /**
     * Start to select media and wait for result.
     *
     * @param listener The resulting callback listens
     */
    public void forResult(OnResultCallbackListener listener) {
        if (!DoubleUtils.isFastDoubleClick()) {
            Activity activity = selector.getActivity();
            if (activity == null || selectionConfig == null) {
                return;
            }
            // 绑定回调监听
            PictureSelectionConfig.listener = new WeakReference<>(listener).get();

            Intent intent;
            if (selectionConfig.camera && selectionConfig.isUseCustomCamera) {
                intent = new Intent(activity, PictureCustomCameraActivity.class);
            } else {
                intent = new Intent(activity, selectionConfig.camera
                        ? PictureSelectorCameraEmptyActivity.class :
                        selectionConfig.isWeChatStyle ? PictureSelectorWeChatStyleActivity.class
                                : selectionConfig.isInstagramStyle ? PictureSelectorInstagramStyleActivity.class : PictureSelectorActivity.class);
            }
            Fragment fragment = selector.getFragment();
            if (fragment != null) {
                fragment.startActivity(intent);
            } else {
                activity.startActivity(intent);
            }
            PictureWindowAnimationStyle windowAnimationStyle = selectionConfig.windowAnimationStyle;
            activity.overridePendingTransition(windowAnimationStyle != null &&
                    windowAnimationStyle.activityEnterAnimation != 0 ?
                    windowAnimationStyle.activityEnterAnimation :
                    R.anim.picture_anim_enter, R.anim.picture_anim_fade_in);
        }
    }

    /**
     * Start to select media and wait for result.
     *
     * @param requestCode Identity of the request Activity or Fragment.
     * @param listener    The resulting callback listens
     */
    public void forResult(int requestCode, OnResultCallbackListener listener) {
        if (!DoubleUtils.isFastDoubleClick()) {
            Activity activity = selector.getActivity();
            if (activity == null || selectionConfig == null) {
                return;
            }
            // 绑定回调监听
            PictureSelectionConfig.listener = new WeakReference<>(listener).get();
            Intent intent;
            if (selectionConfig.camera && selectionConfig.isUseCustomCamera) {
                intent = new Intent(activity, PictureCustomCameraActivity.class);
            } else {
                intent = new Intent(activity, selectionConfig.camera
                        ? PictureSelectorCameraEmptyActivity.class :
                        selectionConfig.isWeChatStyle ? PictureSelectorWeChatStyleActivity.class
                                : selectionConfig.isInstagramStyle ? PictureSelectorInstagramStyleActivity.class : PictureSelectorActivity.class);
            }
            Fragment fragment = selector.getFragment();
            if (fragment != null) {
                fragment.startActivityForResult(intent, requestCode);
            } else {
                activity.startActivityForResult(intent, requestCode);
            }
            PictureWindowAnimationStyle windowAnimationStyle = selectionConfig.windowAnimationStyle;
            activity.overridePendingTransition(windowAnimationStyle != null &&
                    windowAnimationStyle.activityEnterAnimation != 0 ?
                    windowAnimationStyle.activityEnterAnimation :
                    R.anim.picture_anim_enter, R.anim.picture_anim_fade_in);
        }
    }

}
