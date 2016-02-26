package ru.yandex.music.rxremote;

import android.content.ComponentName;
import android.content.ServiceConnection;

import ru.yandex.music.rxremote.parcel.RemoteKey;
import rx.Observable;

/**
 * Created by ypavshl on 6.1.16.
 */
public interface RxBridge extends ServiceConnection {
    <T> void offerObservable(RemoteKey<T> key, Observable<T> observable);
    <T> void dismissObservable(RemoteKey<T> key);
    void dismissObservables();
    Observable<? extends RxRemote> observe();
    <T> Observable<T> observe(RemoteKey<T> key, ComponentName name);
    <T> Observable<T> observe(RemoteKey<T> key);
}
