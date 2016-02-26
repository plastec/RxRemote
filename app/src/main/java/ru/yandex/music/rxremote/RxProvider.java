package ru.yandex.music.rxremote;

import ru.yandex.music.rxremote.parcel.RemoteKey;
import rx.Observable;

/**
 * Created by ypavshl on 13.1.16.
 */
public interface RxProvider {
    <T> void offerObservable(RemoteKey<T> key, Observable<T> observable);
    <T> void dismissObservable(RemoteKey<T> key);
    void dismissObservables();
}
