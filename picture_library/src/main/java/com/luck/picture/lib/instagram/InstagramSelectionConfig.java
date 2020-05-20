package com.luck.picture.lib.instagram;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.luck.picture.lib.config.PictureSelectionConfig;

/**
 * ================================================
 * Created by JessYan on 2020/5/20 15:05
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public final class InstagramSelectionConfig implements Parcelable {
    private int currentTheme = InsGallery.THEME_STYLE_DEFAULT;

    public static InstagramSelectionConfig createConfig() {
        return new InstagramSelectionConfig();
    }

    public static void convertIntent(PictureSelectionConfig selectionConfig, Intent origin) {
        if (origin == null) {
            return;
        }
        if (selectionConfig != null && selectionConfig.instagramSelectionConfig != null) {
            origin.setClassName(origin.getComponent().getPackageName(), PictureSelectorInstagramStyleActivity.class.getName());
        }
    }

    public InstagramSelectionConfig setCurrentTheme(int currentTheme) {
        this.currentTheme = currentTheme;
        return this;
    }

    public int getCurrentTheme() {
        return currentTheme;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.currentTheme);
    }

    private InstagramSelectionConfig() {
    }

    private InstagramSelectionConfig(Parcel in) {
        this.currentTheme = in.readInt();
    }

    public static final Parcelable.Creator<InstagramSelectionConfig> CREATOR = new Parcelable.Creator<InstagramSelectionConfig>() {
        @Override
        public InstagramSelectionConfig createFromParcel(Parcel source) {
            return new InstagramSelectionConfig(source);
        }

        @Override
        public InstagramSelectionConfig[] newArray(int size) {
            return new InstagramSelectionConfig[size];
        }
    };
}
