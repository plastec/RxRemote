package ru.yandex.music.rxremote;

import android.os.RemoteException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ru.yandex.music.rxremote.parcel.RemoteItem;
import ru.yandex.music.rxremote.parcel.RemoteKey;
import ru.yandex.music.rxremote.parcel.RemoteThrowable;
import ru.yandex.music.rxremote.parcel.SubscriberKey;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

/**
 *
 * TODO synchronize
 *
 * Created by ypavshl on 15.1.16.
 */
class AidlOnSubscribe<T> implements Observable.OnSubscribe<T> {

    private Map<SubscriberKey, Subscriber<? super T>> mSubscribers = new ConcurrentHashMap<>();
    private final IRxRemote mRxRemote;
    private final RemoteKey<T> mRemoteKey;

    private IObservableCallback.Stub mCallback = new IObservableCallback.Stub() {

        @Override
        public void onNext(SubscriberKey key, RemoteItem item) throws RemoteException {
            mSubscribers.get(key).onNext((T)item.item);
        }

        @Override
        public void onStart(SubscriberKey key) throws RemoteException {
            mSubscribers.get(key).onStart();
        }

        @Override
        public void onComplete(SubscriberKey key) throws RemoteException {
            mSubscribers.get(key).onCompleted();
            mSubscribers.remove(key);
        }

        @Override
        public void onError(SubscriberKey key, RemoteThrowable throwable) throws RemoteException {
            mSubscribers.get(key).onError(throwable.throwable);
            mSubscribers.remove(key);
        }
    };

    AidlOnSubscribe(final IRxRemote rxRemote, final RemoteKey<T> remoteKey) {
        mRxRemote = rxRemote;
        mRemoteKey = remoteKey;
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {
        SubscriberKey key = new SubscriberKey();
        mSubscribers.put(key, subscriber);
        try {
            mRxRemote.subscribe(mCallback, mRemoteKey, key);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        subscriber.add(Subscriptions.create(() -> mSubscribers.remove(key)));
    }

//    void complete() {
//        for (Map.Entry<SubscriberKey, Subscriber<? super T>> subscriber : mSubscribers.entrySet()) {
//            try {
//                mRxRemote.unsubscribe(mRemoteKey, subscriber.getKey());
//            } catch (RemoteException e) {
//                // looks like nothing to do here
//            }
//            subscriber.getValue().unsubscribe();
//        }
//    }
}