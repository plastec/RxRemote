package ru.yandex.music.rxremote;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import ru.yandex.music.rxremote.parcel.RemoteKey;
import ru.yandex.music.rxremote.parcel.SubscriberKey;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by ypavshl on 6.1.16.
 */
public class RxBridgeAidl extends IConnector.Stub implements RxBridge {

    private static final String TAG = RxBridgeAidl.class.getSimpleName();

    private final Context mContext;
    private ComponentName mComponentName;

    private final Map<RemoteKey, Map<SubscriberKey, Subscriber>> mSubscribers = new HashMap<>();
    private final Map<RemoteKey, Observable> mObservables = new HashMap<>();
    private final PairHashMap<IConnector, ComponentName> mRemotes = new PairHashMap<>();
    private final PairHashMap<RxBridgeAidl, ComponentName> mLocals = new PairHashMap<>();


    private final RxRemoteOnSubscribe mBridgeOnSubscribe
            = new RxRemoteOnSubscribe();
    private final Observable<? extends RxRemote> mBridgeObservable
            = Observable.create(mBridgeOnSubscribe);

    /**
     * Used when this calls acts as {@code ServiceConnection}
     */
    private ComponentName mServiceComponentName;
    /**
     * Used when this calls acts as {@code ServiceConnection}
     */
    private IBinder mService;


    public RxBridgeAidl(Context context) {
        mContext = context;
        mComponentName = new ComponentName(context, context.getClass());
    }

    @Override
    public synchronized void onServiceConnected(ComponentName name, IBinder service) {
        Log.i(TAG + " REMOTE", "onServiceConnected  " + name);
        mServiceComponentName = name;
        mService = service;
        try {
            if (service instanceof RxBridgeAidl) {
                // Service is running in our process.
                // Register it locally.
                RxBridgeAidl bridge = (RxBridgeAidl)service;
                bridge.doRegistrationForLocal(this);
            } else {
                // Service is remote.
                IConnector remote = IConnector.Stub.asInterface(service);
                doRegistrationForRemote(remote, name);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void onServiceDisconnected(ComponentName className) {
        Log.i(TAG + " REMOTE", "onServiceDisconnected  " + className);
        try {
            if (mService instanceof RxBridgeAidl) {
                RxBridgeAidl bridge = mLocals.remove(mServiceComponentName).first;
                bridge.unregisterLocally(this);
            } else {
                IConnector remote = IConnector.Stub.asInterface(mService);
                mRemotes.remove(mServiceComponentName);
                remote.unregister(this);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * IConnector implementation. Don't call it directly.
     *
     * @param remote
     */
    @Override
    public synchronized void register(IConnector remote) {
        Log.i(TAG + " REMOTE", "register " + remote);
        try {
            ComponentName name = remote.getComponentName();
            doRegistrationForRemote(remote, name);
        } catch (RemoteException e) {
            // no need to do anything here
        }
    }

    /**
     * Remote registration is performed here.
     *
     * @param remote
     * @param name
     * @throws RemoteException
     */
    private void doRegistrationForRemote(@NonNull final IConnector remote,
                                         @NonNull final ComponentName name) throws RemoteException {
        Log.i(TAG + " REMOTE", "doRegistrationForRemote " + name);
        if (mRemotes.contains(name))
            return;

        mRemotes.put(remote, name);
        onComponentRegistered(name);
        for (Map.Entry<RemoteKey, Observable> entry : mObservables.entrySet()) {
            remote.onObservableOffered(this, entry.getKey(), mComponentName);
        }

        remote.register(this);
    }

    /**
     * Called from the other instances of {@code RxBridgeAidl} for registration.
     *
     * @param local to be registered
     */
    private synchronized void doRegistrationForLocal(@NonNull final RxBridgeAidl local) {
        Log.i(TAG + " REMOTE", "doRegistrationForLocal " + local);
        if(mLocals.contains(local))
            return;

        final ComponentName name = local.mComponentName;
        mLocals.put(local, name);
        onComponentRegistered(name);
        for (Map.Entry<RemoteKey, Observable> e : mObservables.entrySet()) {
            local.mBridgeOnSubscribe.onObservable(e.getValue(), e.getKey(), name);
        }

        local.doRegistrationForLocal(this);
    }

    /**
     * IConnector implementation. Don't call it directly.
     *
     * @param remote
     */
    @Override
    public synchronized void unregister(IConnector remote) throws RemoteException {
        Log.i(TAG + " REMOTE", "unregister " + remote);
        ComponentName name = mRemotes.remove(remote).second;
        onComponentUnregistered(name);
    }

    /**
     * Call from the other instances of {@code RxBridgeAidl}.
     *
     * @param local
     */
    public synchronized void unregisterLocally(@NonNull final RxBridgeAidl local) {
        Log.i(TAG + " REMOTE", "unregisterLocally");
        final ComponentName name = local.mComponentName;
        mLocals.remove(name);
        onComponentUnregistered(name);
    }

    /**
     * IConnector implementation. Don't call it directly.
     *
     * @param callback
     * @param remoteKey
     * @param subscriberKey
     */
    @Override
    public synchronized void subscribe(@NonNull IObservableCallback callback,
                                       @NonNull RemoteKey remoteKey,
                                       @NonNull SubscriberKey subscriberKey) {
        Log.i(TAG + " REMOTE", "subscribe " + remoteKey);
        Observable observable = mObservables.get(remoteKey);
        Subscriber subscriber = new RemoteSubscriber(callback, remoteKey);
        observable.subscribe(subscriber);

        if (!mSubscribers.containsKey(remoteKey)) {
            mSubscribers.put(remoteKey, new HashMap<>());
        }
        mSubscribers.get(remoteKey).put(subscriberKey, subscriber);
    }

    @Override
    public synchronized void unsubscribe(RemoteKey remoteKey, SubscriberKey subscriberKey) {
        mSubscribers.get(remoteKey).get(subscriberKey).unsubscribe();
    }

    /**
     * IConnector implementation. Don't call it directly.
     *
     * @param key
     * @throws RemoteException
     */
    @Override
    public void onObservableOffered(@NonNull final IConnector remote,
                                    @NonNull final RemoteKey key,
                                    @NonNull final ComponentName name) {
        Log.i(TAG + " REMOTE", "onObservableOffered " + key);
        mBridgeOnSubscribe.onObservable(remote, key, name);
    }

    /**
     * IConnector implementation. Don't call it directly.
     *
     * @param key
     * @param name
     */
    @Override
    public void onObservableDismissed(@NonNull final RemoteKey key,
                                      @NonNull final ComponentName name) {
        Log.i(TAG + " REMOTE", "onObservableDismissed " + key);
        mBridgeOnSubscribe.onObservableDismissed(key, name);
    }

    /**
     * Called when a observe bridge is regirested to this bridge
     * @param name
     */
    protected void onComponentRegistered(ComponentName name) {}

    /**
     * Called when a observe bridge is unregirested to this bridge
     * @param name
     */
    protected void onComponentUnregistered(ComponentName name) {}

    @Override
    public ComponentName getComponentName() throws RemoteException {
        if (mComponentName.getPackageName().isEmpty())
            mComponentName = new ComponentName(mContext, mContext.getClass());

        if (mComponentName.getPackageName().isEmpty())
            throw new RemoteException("Can't get package name for the component!");

        return mComponentName;
    }

    /**
     * RxBridge implementation.
     *
     * @param key
     * @param observable
     * @param <T>
     */
    @Override
    public synchronized <T> void offerObservable(RemoteKey<T> key, Observable<T> observable) {
        if (observable == null)
            throw new IllegalArgumentException("Observable can't be null");

        Log.i(TAG + " REMOTE", "offerObservable " + key);
        mObservables.put(key, observable);
        for (IConnector remote : mRemotes.firsts()) {
            try {
                remote.onObservableOffered(this, key, mComponentName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        for (RxBridgeAidl local : mLocals.firsts()) {
            local.mBridgeOnSubscribe.onObservable(observable, key, mComponentName);
        }
    }

    @Override
    public synchronized <T> void dismissObservable(RemoteKey<T> key) {
        synchronized (key) {
            mObservables.remove(key);
            for (IConnector remote : mRemotes.firsts()) {
                try {
                    remote.onObservableDismissed(key, mComponentName);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            for (RxBridgeAidl local : mLocals.firsts()) {
                local.onObservableDismissed(key, mComponentName);
            }
            Map<SubscriberKey, Subscriber> map = mSubscribers.get(key);
            if (map != null) {
                for (Subscriber subscriber : map.values()){
                    subscriber.unsubscribe();
                }
            }
        }
    }

    @Override
    public void dismissObservables() {
        for (RemoteKey remoteKey : mObservables.keySet()) {
            dismissObservable(remoteKey);
        }
    }

    @Override
    public synchronized Observable<? extends RxRemote> observe() {
        return mBridgeObservable;
    }

    @Override
    public synchronized <T> Observable<T> observe(@NonNull final RemoteKey<T> key,
                                                  @Nullable final ComponentName name) {
        return Observable.create(new OnRemoteSubscribe<T>(mBridgeObservable, key, name));
    }

    @Override
    public synchronized <T> Observable<T> observe(@NonNull final RemoteKey<T> key) {
        return Observable.create(new OnRemoteSubscribe<T>(mBridgeObservable, key, null));
    }
}
