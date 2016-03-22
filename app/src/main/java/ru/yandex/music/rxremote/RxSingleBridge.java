package ru.yandex.music.rxremote;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import ru.yandex.music.rxremote.parcel.RemoteKey;
import ru.yandex.music.rxremote.parcel.SubscriberKey;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by ypavshl on 22.3.16.
 */
public class RxSingleBridge implements Parcelable{

    private static final String TAG = RxSingleBridge.class.getSimpleName();

    private final Map<RemoteKey, Map<SubscriberKey, Subscriber>> mSubscribers = new HashMap<>();
    private final Map<RemoteKey, Observable> mObservables = new HashMap<>();

    private final RxRemoteOnSubscribe mBridgeOnSubscribe
            = new RxRemoteOnSubscribe();
    private final Observable<? extends RxRemote> mBridgeObservable
            = Observable.create(mBridgeOnSubscribe);

    private ISingleConnector mRemoteConnector;
    private ISingleConnector mLocalConnector;

    private static class SubscribeAdapterImpl implements AidlOnSubscribe.SubscribeAdapter {

        private ISingleConnector mConnector;

        SubscribeAdapterImpl(@NonNull final ISingleConnector connector) {
            mConnector = connector;
        }

        public void subscribe(@NonNull final IObservableCallback callback,
                              @NonNull final RemoteKey remoteKey,
                              @NonNull final SubscriberKey subscriberKey)
                 throws RemoteException {
            mConnector.subscribe(callback, remoteKey, subscriberKey);
        }

        public void unsubscribe(@NonNull final RemoteKey remoteKey,
                                @NonNull final SubscriberKey subscriberKey)
                 throws RemoteException {
            mConnector.unsubscribe(remoteKey, subscriberKey);
        }
    }

    public RxSingleBridge() {};

    private class RemoteConnector extends ISingleConnector.Stub {

        @Override
        public void onObservableOffered(@NonNull final ISingleConnector remote,
                                        @NonNull final RemoteKey key) {
            Log.i(TAG + " REMOTE", "onObservableOffered " + key);
            mBridgeOnSubscribe.onObservable(new SubscribeAdapterImpl(remote), key, null);
        }

        @Override
        public void onObservableDismissed(@NonNull final RemoteKey key) {
            Log.i(TAG + " REMOTE", "onObservableDismissed " + key);
            mBridgeOnSubscribe.onObservableDismissed(key, null);
        }

        @Override
        public void subscribe(@NonNull final IObservableCallback callback,
                              @NonNull final RemoteKey remoteKey,
                              @NonNull final SubscriberKey subscriberKey) {}

        @Override
        public void unsubscribe(@NonNull final RemoteKey remoteKey,
                                @NonNull final SubscriberKey subscriberKey) {}
    }


    private class LocalConnector extends ISingleConnector.Stub {

        @Override
        public void onObservableOffered(@NonNull final ISingleConnector remote,
                                        @NonNull final RemoteKey key) {}

        @Override
        public void onObservableDismissed(@NonNull final RemoteKey key) {}

        @Override
        public synchronized void subscribe(@NonNull final IObservableCallback callback,
                                           @NonNull final RemoteKey remoteKey,
                                           @NonNull final SubscriberKey subscriberKey) {
            Observable observable = mObservables.get(remoteKey);
            Subscriber subscriber = new RemoteSubscriber(callback, remoteKey);
            observable.subscribe(subscriber);

            if (!mSubscribers.containsKey(remoteKey)) {
                mSubscribers.put(remoteKey, new HashMap<>());
            }
            mSubscribers.get(remoteKey).put(subscriberKey, subscriber);
        }

        @Override
        public synchronized void unsubscribe(@NonNull final RemoteKey remoteKey,
                                             @NonNull final SubscriberKey subscriberKey) {
            mSubscribers.get(remoteKey).get(subscriberKey).unsubscribe();
        }
    }

    public synchronized <T> void offerObservable(RemoteKey<T> key, Observable<T> observable) {
        if (observable == null)
            throw new IllegalArgumentException("Observable can't be null");

        Log.i(TAG + " REMOTE", "offerObservable " + key);
        mObservables.put(key, observable);
        Log.i(TAG + " REMOTE", "offerObservable observable = " + observable);

        if (mRemoteConnector != null) {
            try {
                mRemoteConnector.onObservableOffered(mLocalConnector, key);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized <T> void dismissObservable(RemoteKey<T> key) {
        mObservables.remove(key);
        if (mRemoteConnector != null) {
            try {
                mRemoteConnector.onObservableDismissed(key);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void dismissObservables() {
        for (RemoteKey remoteKey : mObservables.keySet()) {
            dismissObservable(remoteKey);
        }
    }

    public synchronized Observable<? extends RxRemote> observe() {
        return mBridgeObservable;
    }

    public synchronized <T> Observable<T> observe(@NonNull final RemoteKey<T> key) {
        return Observable.create(new OnRemoteSubscribe<T>(mBridgeObservable, key, null));
    }


    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        synchronized (this) {
            if (mRemoteConnector == null) {
                mRemoteConnector = new RemoteConnector();
                mLocalConnector = new LocalConnector();
            }
            out.writeStrongBinder(mRemoteConnector.asBinder());
        }
    }

    RxSingleBridge(Parcel in) {
        mRemoteConnector = ISingleConnector.Stub.asInterface(in.readStrongBinder());
        mLocalConnector = new LocalConnector();
    }

    public static final Parcelable.Creator<RxSingleBridge> CREATOR
            = new Parcelable.Creator<RxSingleBridge>() {
        public RxSingleBridge createFromParcel(Parcel in) {
            return new RxSingleBridge(in);
        }
        public RxSingleBridge[] newArray(int size) {
            return new RxSingleBridge[size];
        }
    };
}
