package com.example.ypavshl.lib;

import android.content.ComponentName;
import android.content.Context;
import android.os.Binder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

import com.example.ypavshl.lib.parcel.RemoteItem;
import com.example.ypavshl.lib.parcel.RemoteKey;
import com.example.ypavshl.lib.parcel.RemoteThrowable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

// TODO make me hot

/**
 * Created by ypavshl on 6.1.16.
 */
class RxBridgeLocal extends Binder implements RxBridge {

    private static final String TAG = RxBridgeLocal.class.getSimpleName();


    @Override
    public <T> Observable<T> bindObservable(RemoteKey<T> key, ComponentName componentName) {
        return null;
    }

    @Override
    public <T> void unbindObservable(RemoteKey<T> key, ComponentName componentName) {

    }

    @Override
    public void unbindObservables() {

    }

    @Override
    public <T> void offerObservable(RemoteKey<T> key, Observable<T> observable) {

    }

    @Override
    public <T> void dismissObservable(RemoteKey<T> key) {

    }

    @Override
    public void dismissObservables() {

    }
}
