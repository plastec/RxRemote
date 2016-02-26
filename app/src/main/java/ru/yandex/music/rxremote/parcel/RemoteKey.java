package ru.yandex.music.rxremote.parcel;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by ypavshl on 29.12.15.
 */
public final class RemoteKey<T> implements Parcelable, Serializable {

    private static final String TAG = RemoteKey.class.getSimpleName();

    private final String mName;
    private final Class<T> mType;

    public RemoteKey(@NonNull final String n, @NonNull final Class<T> t) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        mName = n;
        mType = t;
    }

    public Class<T> getmType() {
        return mType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteKey<?> remoteKey = (RemoteKey<?>) o;

        if (!mName.equals(remoteKey.mName)) return false;
        return mType.equals(remoteKey.mType);
    }

    @Override
    public int hashCode() {
        int result = mName.hashCode();
        result = 31 * result + mType.hashCode();
        return result;
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
        dest.writeSerializable(this.mType);
    }

    protected RemoteKey(Parcel in) {
        this.mName = in.readString();
        this.mType = (Class<T>) in.readSerializable();
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
