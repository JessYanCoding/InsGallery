package com.luck.picture.lib.widget.instagram;

import android.os.Parcel;
import android.os.Parcelable;

import com.luck.picture.lib.config.PictureSelectionConfig;

public class InstagramSelectionConfig extends PictureSelectionConfig implements Parcelable {

    public static final Creator<InstagramSelectionConfig> CREATOR = new Creator<InstagramSelectionConfig>() {
        @Override
        public InstagramSelectionConfig createFromParcel(Parcel source) {
            return new InstagramSelectionConfig(source);
        }

        @Override
        public InstagramSelectionConfig[] newArray(int size) {
            return new InstagramSelectionConfig[size];
        }
    };
    public boolean isInstagramStyle;

    public InstagramSelectionConfig() {
    }

    public InstagramSelectionConfig(Parcel in) {
        this.isInstagramStyle = in.readByte() != 0;
    }

    public static InstagramSelectionConfig getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public static InstagramSelectionConfig getCleanInstance() {
        InstagramSelectionConfig selectionSpec = getInstance();
        selectionSpec.initDefaultValue();
        return selectionSpec;
    }

    @Override
    protected void initDefaultValue() {
        super.initDefaultValue();
        isInstagramStyle = false;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(this.isInstagramStyle ? (byte) 1 : (byte) 0);
    }

    private static final class InstanceHolder {
        private static final InstagramSelectionConfig INSTANCE = new InstagramSelectionConfig();
    }

}
