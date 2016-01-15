package com.example.ypavshl.lib;

import rx.Observable;

/**
 * Created by ypavshl on 15.1.16.
 */
class RemoteObservable<T> extends Observable<T>{

    final RemoteOnSubscribe<T> onSubscribe;

    RemoteObservable(final RemoteOnSubscribe<T> onSubscribe) {
        super(onSubscribe);
        this.onSubscribe = onSubscribe;
    }

//    void complete() {
//        onSubscribe.complete();
//    }
}
