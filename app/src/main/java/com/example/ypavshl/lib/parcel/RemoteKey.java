package com.example.ypavshl.lib.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by ypavshl on 29.12.15.
 */
public final class RemoteKey<T> implements Parcelable, Serializable {

    private static final String TAG = RemoteKey.class.getSimpleName();

    private final String name;

    public final Class<T> type;

    public RemoteKey(String n, Class<T> t) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        name = n;
        type = t;
    }

    @Override
    public final int hashCode() {
        return name.hashCode();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RemoteKey)) {
            return false;
        }
        RemoteKey lhs = (RemoteKey) o;
        return name.equals(lhs.name);
    }

    @Override
    public String toString() {
        return "RemoteKey{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeSerializable(this.type);
    }

    protected RemoteKey(Parcel in) {
        this.name = in.readString();
        this.type = (Class<T>) in.readSerializable();
    }

    public static final Creator<RemoteKey> CREATOR = new Creator<RemoteKey>() {
        public RemoteKey createFromParcel(Parcel source) {
            return new RemoteKey(source);
        }

        public RemoteKey[] newArray(int size) {
            return new RemoteKey[size];
        }
    };
}
