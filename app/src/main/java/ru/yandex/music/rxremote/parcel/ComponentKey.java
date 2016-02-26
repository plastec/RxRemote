package ru.yandex.music.rxremote.parcel;

import android.content.ComponentName;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ypavshl on 13.1.16.
 */
public final class ComponentKey<T> implements Parcelable {

    public final RemoteKey<T> remoteKey;
    public final ComponentName component;

    public ComponentKey(RemoteKey<T> key, Context context) {
        this(key, new ComponentName(context, context.getClass()));
    }

    public ComponentKey(RemoteKey<T> key, ComponentName component) {
        if (key == null || component == null)
            throw new IllegalArgumentException("The arguments can't be null!");
        remoteKey = key;
        this.component = component;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComponentKey that = (ComponentKey) o;

        if (!remoteKey.equals(that.remoteKey)) return false;
        return component.equals(that.component);

    }

    @Override
    public int hashCode() {
        int result = remoteKey.hashCode();
        result = 31 * result + component.hashCode();
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.remoteKey, 0);
        dest.writeParcelable(this.component, 0);
    }

    protected ComponentKey(Parcel in) {
        this.remoteKey = in.readParcelable(RemoteKey.class.getClassLoader());
        this.component = in.readParcelable(ComponentName.class.getClassLoader());
    }

    public static final Creator<ComponentKey> CREATOR = new Creator<ComponentKey>() {
        public ComponentKey createFromParcel(Parcel source) {
            return new ComponentKey(source);
        }

        public ComponentKey[] newArray(int size) {
            return new ComponentKey[size];
        }
    };
}
