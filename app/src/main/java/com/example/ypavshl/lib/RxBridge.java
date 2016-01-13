package com.example.ypavshl.lib;

import android.content.ComponentName;

import com.example.ypavshl.lib.parcel.RemoteKey;

import rx.Observable;

/**
 * Created by ypavshl on 6.1.16.
 */
public interface RxBridge {
    <T> void offerObservable(RemoteKey<T> key, Observable<T> observable);
    <T> void dismissObservable(RemoteKey<T> key);
    void dismissObservables();

    <T> Observable<T> bindObservable(RemoteKey<T> key, ComponentName componentName);
    <T> void unbindObservable(RemoteKey<T> key, ComponentName componentName);
    void unbindObservables();
}
