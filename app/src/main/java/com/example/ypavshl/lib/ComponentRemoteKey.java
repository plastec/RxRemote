package com.example.ypavshl.lib;

import android.content.ComponentName;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.ypavshl.lib.parcel.RemoteKey;

/**
 * Created by ypavshl on 13.1.16.
 */
class ComponentRemoteKey<T> implements Parcelable {

    final RemoteKey<T> remoteKey;
    final ComponentName mComponent;

    ComponentRemoteKey(RemoteKey<T> key, Context context) {
        this(key, new ComponentName(context, context.getClass()));
    }

    ComponentRemoteKey(RemoteKey<T> key, ComponentName component) {
        if (key == null || component == null)
            throw new IllegalArgumentException("The arguments can't be null!");
        remoteKey = key;
        mComponent = component;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComponentRemoteKey that = (ComponentRemoteKey) o;

        if (!remoteKey.equals(that.remoteKey)) return false;
        return mComponent.equals(that.mComponent);

    }

    @Override
    public int hashCode() {
        int result = remoteKey.hashCode();
        result = 31 * result + mComponent.hashCode();
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.remoteKey, 0);
        dest.writeParcelable(this.mComponent, 0);
    }

    protected ComponentRemoteKey(Parcel in) {
        this.remoteKey = in.readParcelable(RemoteKey.class.getClassLoader());
        this.mComponent = in.readParcelable(ComponentName.class.getClassLoader());
    }

    public static final Creator<ComponentRemoteKey> CREATOR = new Creator<ComponentRemoteKey>() {
        public ComponentRemoteKey createFromParcel(Parcel source) {
            return new ComponentRemoteKey(source);
        }

        public ComponentRemoteKey[] newArray(int size) {
            return new ComponentRemoteKey[size];
        }
    };
}
