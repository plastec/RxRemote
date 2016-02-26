package ru.yandex.music.rxremote;

import rx.Observable;

/**
 * Created by ypavshl on 15.1.16.
 */
class AidlObservable<T> extends Observable<T>{

    final AidlOnSubscribe<T> onSubscribe;

    AidlObservable(final AidlOnSubscribe<T> onSubscribe) {
        super(onSubscribe);
        this.onSubscribe = onSubscribe;
    }

//    void complete() {
//        onSubscribe.complete();
//    }
}
