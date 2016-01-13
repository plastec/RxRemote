package com.example.ypavshl.lib.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by ypavshl on 29.12.15.
 */
public final class RemoteKey<T> implements Parcelable, Serializable {

    private final String mName;

    public RemoteKey() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        mName = elements[3].toString(); // TODO + application package name
    }

    public final String getName() {
        return mName;
    }

    @Override
    public final int hashCode() {
        return mName.hashCode();
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
        return mName.equals(lhs.mName);
    }

    @Override
    public String toString() {
        return "RemoteKey{" +
                "mName='" + mName + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mName);
    }

    protected RemoteKey(Parcel in) {
        this.mName = in.readString();
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
