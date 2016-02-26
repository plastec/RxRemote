package ru.yandex.music.rxremote;

import android.content.ComponentName;

import ru.yandex.music.rxremote.parcel.RemoteKey;
import rx.Observable;

/**
 * Created by ypavshl on 13.1.16.
 */
public interface RxConnector {
    <T> Observable<T> bindObservable(RemoteKey<T> key, ComponentName componentName);
    <T> void unbindObservable(RemoteKey<T> key, ComponentName componentName);
    void unbindObservables();
}
