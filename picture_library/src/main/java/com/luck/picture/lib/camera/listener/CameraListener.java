package com.luck.picture.lib.camera.listener;

import java.io.File;

import androidx.annotation.NonNull;

/**
 * @author：luck
 * @date：2020-01-04 13:38
 * @describe：相机回调监听
 */
public interface CameraListener {
    /**
     * 拍照成功返回
     *
     * @param file
     */
    void onPictureSuccess(@NonNull File file);

    /**
     * 录像成功返回
     *
     * @param file
     */
    void onRecordSuccess(@NonNull File file);

    /**
     * 使用相机出错
     *
     *
     */
    void onError(int videoCaptureError, String message, Throwable cause);
}
