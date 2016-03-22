package ru.yandex.music.rxremote;

import android.os.RemoteException;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ru.yandex.music.rxremote.parcel.RemoteItem;
import ru.yandex.music.rxremote.parcel.RemoteKey;
import ru.yandex.music.rxremote.parcel.RemoteThrowable;
import ru.yandex.music.rxremote.parcel.SubscriberKey;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

/**
 *
 * Created by ypavshl on 15.1.16.
 */
class AidlOnSubscribe<T> implements Observable.OnSubscribe<T> {

    private final SubscribeAdapter mConnector;
    private final RemoteKey<T> mRemoteKey;
    private final Map<Subscriber<? super T>, ObservableCallback<T>> mCallbacks = new HashMap<>();

    interface SubscribeAdapter {
        void subscribe(IObservableCallback callback, RemoteKey remoteKey, SubscriberKey subscriberKey)
                throws RemoteException;
        void unsubscribe(RemoteKey remoteKey, SubscriberKey subscriberKey)
                throws RemoteException;
    }

    private static class ObservableCallback<T1> extends IObservableCallback.Stub {

        private final Subscriber<? super T1> mSubscriber;

        private ObservableCallback(Subscriber<? super T1> subscriber) {
            mSubscriber = subscriber;
        }

        @Override
        public void onNext(RemoteItem item) throws RemoteException {
            mSubscriber.onNext((T1) item.getItem());
        }

        @Override
        public void onStart() throws RemoteException {
            mSubscriber.onStart();
        }

        @Override
        public void onComplete() throws RemoteException {
            mSubscriber.onCompleted();
        }

        @Override
        public void onError(RemoteThrowable throwable) throws RemoteException {
            mSubscriber.onError(throwable.throwable);
        }
    }

    AidlOnSubscribe(@NonNull final SubscribeAdapter connector,
                    @NonNull final RemoteKey<T> remoteKey) {
        mConnector = connector;
        mRemoteKey = remoteKey;
    }

    @Override
    public void call(@NonNull final Subscriber<? super T> subscriber) {
        final SubscriberKey subscriberKey = new SubscriberKey();
        final ObservableCallback callback = new ObservableCallback(subscriber);

        synchronized (subscriberKey) {
            mCallbacks.put(subscriber, callback);
            try {
                mConnector.subscribe(callback, mRemoteKey, subscriberKey);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }

        subscriber.add(Subscriptions.create(() -> {
            synchronized (subscriberKey) {
                mCallbacks.remove(subscriber);
                try {
                    mConnector.unsubscribe(mRemoteKey, subscriberKey);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }));
    }
}
