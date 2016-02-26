package ru.yandex.music.rxremote.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

/**
 * Created by ypavshl on 15.1.16.
 */
public final class SubscriberKey implements Parcelable {

    final private UUID mUuid;

    public SubscriberKey() {
        mUuid = UUID.randomUUID();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriberKey)) return false;

        SubscriberKey that = (SubscriberKey) o;

        return !(mUuid != null ? !mUuid.equals(that.mUuid) : that.mUuid != null);

    }

    @Override
    public int hashCode() {
        return mUuid != null ? mUuid.hashCode() : 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.mUuid);
    }

    protected SubscriberKey(Parcel in) {
        this.mUuid = (UUID) in.readSerializable();
    }

    public static final Creator<SubscriberKey> CREATOR = new Creator<SubscriberKey>() {
        public SubscriberKey createFromParcel(Parcel source) {
            return new SubscriberKey(source);
        }

        public SubscriberKey[] newArray(int size) {
            return new SubscriberKey[size];
        }
    };
}
