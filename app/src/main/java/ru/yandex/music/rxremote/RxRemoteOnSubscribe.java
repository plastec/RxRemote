package ru.yandex.music.rxremote;

import android.content.ComponentName;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import java.util.HashMap;
import java.util.HashSet;

import ru.yandex.music.rxremote.parcel.RemoteKey;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

/**
 * Created by ypavshl on 9.3.16.
 */
class RxRemoteOnSubscribe implements Observable.OnSubscribe<RxRemote> {

    private static final String TAG = RxRemoteOnSubscribe.class.getSimpleName();

    private HashMap<Pair<RemoteKey, ComponentName>, RxRemote> mObservables
            = new HashMap<>();

    private HashSet<Subscriber<? super RxRemote>> mSubscribers = new HashSet<>();

    @Override
    public synchronized void call(Subscriber<? super RxRemote> subscriber) {
        mSubscribers.add(subscriber);
        subscriber.add(Subscriptions.create(() -> {
            synchronized (subscriber) {
                mSubscribers.remove(subscriber);
            }
        }));

        synchronized (mObservables) {
            for (RxRemote observable : mObservables.values()) {
                subscriber.onNext(observable);
            }
        }
    }

    synchronized void onObservable(@NonNull final IConnector remote,
                                   @NonNull final RemoteKey key,
                                   @NonNull final ComponentName name) {
        Observable observable = Observable.create(new AidlOnSubscribe<>(remote, key));
        onObservable(observable, key, name);
    }

    synchronized void onObservable(@NonNull final Observable observable,
                                   @NonNull final RemoteKey key,
                                   @NonNull final ComponentName name) {
        RxRemote observableHolder = new RxRemote(observable, key, name);
        synchronized (mObservables) {
            mObservables.put(Pair.create(key, name), observableHolder);
            for (Subscriber subscriber : mSubscribers) {
                subscriber.onNext(observableHolder);
            }
        }
    }

    synchronized void onObservableDismissed(@NonNull final RemoteKey key,
                                            @NonNull final ComponentName name) {
        RxRemote observableHolder = new RxRemote(null, key, name);
        synchronized (mObservables) {
            mObservables.remove(Pair.create(key, name));
            for (Subscriber subscriber : mSubscribers) {
                subscriber.onNext(observableHolder);
            }
        }
    }
}
