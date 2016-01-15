package com.example.ypavshl.lib;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.ypavshl.lib.parcel.RemoteKey;

import java.util.Arrays;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by ypavshl on 14.1.16.
 */
public class RxContract implements Parcelable {

    private final String id;

    public final RemoteKey[] service;
    public final RemoteKey[] client;

    public RxContract(RemoteKey[] s, RemoteKey[] c){
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        id = elements[3].toString();
        service = s;
        client = c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RxContract)) return false;

        RxContract that = (RxContract) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(service, that.service)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(client, that.client);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (service != null ? Arrays.hashCode(service) : 0);
        result = 31 * result + (client != null ? Arrays.hashCode(client) : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeParcelableArray(this.service, 0);
        dest.writeParcelableArray(this.client, 0);
    }

    protected RxContract(Parcel in) {
        this.id = in.readString();
        this.service = (RemoteKey[]) in.readParcelableArray(RemoteKey.class.getClassLoader());
        this.client = (RemoteKey[]) in.readParcelableArray(RemoteKey.class.getClassLoader());
    }

    public static final Creator<RxContract> CREATOR = new Creator<RxContract>() {
        public RxContract createFromParcel(Parcel source) {
            return new RxContract(source);
        }

        public RxContract[] newArray(int size) {
            return new RxContract[size];
        }
    };
}
