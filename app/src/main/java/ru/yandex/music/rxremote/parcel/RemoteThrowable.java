package ru.yandex.music.rxremote.parcel;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ypavshl on 30.12.15.
 */
public final class RemoteThrowable implements Parcelable {

    public final Throwable throwable;

    public RemoteThrowable(Throwable t) {
        throwable = t;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.throwable);
    }

    protected RemoteThrowable(Parcel in) {
        this.throwable = (Throwable) in.readSerializable();
    }

    public static final Creator<RemoteThrowable> CREATOR = new Creator<RemoteThrowable>() {
        public RemoteThrowable createFromParcel(Parcel source) {
            return new RemoteThrowable(source);
        }

        public RemoteThrowable[] newArray(int size) {
            return new RemoteThrowable[size];
        }
    };
}
