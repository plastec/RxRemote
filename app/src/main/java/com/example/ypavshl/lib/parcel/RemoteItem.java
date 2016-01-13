package com.example.ypavshl.lib.parcel;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This a simple item for remote Rx emission.
 *
 * How to generate Parcelable quickly:
 * http://codentrick.com/quickly-create-parcelable-class-in-android-studio/
 * http://www.parcelabler.com/
 *
 */
public class RemoteItem<T extends Parcelable> implements Parcelable {

    public final T item;

    public RemoteItem(T p) {
        item = p;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.item, 0);
    }

    protected RemoteItem(Parcel in) {
        this.item = in.readParcelable(RemoteItem.class.getClassLoader());
    }

    public static final Creator<RemoteItem> CREATOR = new Creator<RemoteItem>() {
        public RemoteItem createFromParcel(Parcel source) {
            return new RemoteItem(source);
        }

        public RemoteItem[] newArray(int size) {
            return new RemoteItem[size];
        }
    };

    @Override
    public String toString() {
        return "RemoteItem{" +
                "item=" + item +
                '}';
    }
}
