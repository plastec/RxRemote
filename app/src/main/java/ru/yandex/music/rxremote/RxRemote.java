package ru.yandex.music.rxremote;

import ru.yandex.music.rxremote.parcel.RemoteKey;
import rx.Observable;

/**
 * Created by ypavshl on 29.1.16.
 */
public class RxRemote<T>{

    public final RemoteKey<T> key;
    public final Observable<T> observable;

    RxRemote(Observable<T> o, RemoteKey k) {
        observable = o;
        key = k;
    }

}
