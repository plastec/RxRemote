package com.example.ypavshl.lib;

import android.content.ComponentName;

import com.example.ypavshl.lib.parcel.RemoteKey;

import rx.Observable;

/**
 * Created by ypavshl on 13.1.16.
 */
public interface RxConnector {
    <T> Observable<T> bindObservable(RemoteKey<T> key, ComponentName componentName);
    <T> void unbindObservable(RemoteKey<T> key, ComponentName componentName);
    void unbindObservables();
}
