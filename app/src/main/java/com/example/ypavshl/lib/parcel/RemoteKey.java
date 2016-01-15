package com.example.ypavshl.lib.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by ypavshl on 29.12.15.
 */
public final class RemoteKey<T> implements Parcelable, Serializable {

    private final String id;

    public final Class<T> type;

    public RemoteKey(Class<T> t) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        id = elements[3].toString();
        type = t;
    }

    @Override
    public final int hashCode() {
        return id.hashCode();
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
        return id.equals(lhs.id);
    }

    @Override
    public String toString() {
        return "RemoteKey{" +
                "id='" + id + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeSerializable(this.type);
    }

    protected RemoteKey(Parcel in) {
        this.id = in.readString();
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
