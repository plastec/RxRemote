package ru.yandex.music.rxremote;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by ypavshl on 21.1.16.
 */
public class OnRemoteSubscribe<T> implements Observable.OnSubscribe<T> {

    private ConnectionObservable<T> mConnectionObservable;

    public OnRemoteSubscribe(ConnectionObservable<T> observable) {
        mConnectionObservable = observable;
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {
        mConnectionObservable.subscribe(observable -> observable.subscribe(subscriber));
    }
}
