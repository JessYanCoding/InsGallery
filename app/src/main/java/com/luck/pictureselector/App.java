package com.luck.pictureselector;

import android.app.Application;
import android.content.Context;

import com.luck.picture.lib.app.IApp;
import com.luck.picture.lib.app.PictureAppMaster;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.crash.PictureSelectorCrashUtils;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;


/**
 * @author：luck
 * @date：2019-12-03 22:53
 * @describe：Application
 */

public class App extends Application implements IApp, CameraXConfig.Provider {
    @Override
    public void onCreate() {
        super.onCreate();

        /** PictureSelector日志管理配制开始 **/
        // PictureSelector 绑定监听用户获取全局上下文或其他...
        PictureAppMaster.getInstance().setApp(this);
        // PictureSelector Crash日志监听
        PictureSelectorCrashUtils.init((t, e) -> {
            // Crash之后的一些操作可再此处理，没有就忽略...

        });
        /** PictureSelector日志管理配制结束 **/

        //当 App 的某项权限被关闭时, 系统会重建当前 App 进程, 并重建在栈顶的 Activity, 但栈内的其他 Activity 并未被重建
        //如果这时相册 Activity 在栈顶, 当被重建后, 以下属性就会为 null, 因为以下属性是在 MainActivity 中设置的, 但 MainActivity 并未被重建
        //其实 PictureSelector 已经在 onSaveInstanceState 中对 PictureSelectionConfig 进行了保存和重建时恢复
        //但由于这几个属性是静态的, 所以恢复机制失效了, 但 imageEngine 没有被恢复会影响整个相册的运行, 所以现在先以此方案解决
        PictureSelectionConfig.imageEngine = GlideEngine.createGlideEngine();
        PictureSelectionConfig.cacheResourcesEngine = GlideCacheEngine.createCacheEngine();
    }

    @Override
    public Context getAppContext() {
        return this;
    }

    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }
}
