package ru.yandex.music.rxremote;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ru.yandex.music.rxremote.parcel.ComponentKey;
import ru.yandex.music.rxremote.parcel.RemoteItem;
import ru.yandex.music.rxremote.parcel.RemoteKey;
import ru.yandex.music.rxremote.parcel.RemoteThrowable;
import ru.yandex.music.rxremote.parcel.SubscriberKey;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by ypavshl on 6.1.16.
 */
class RxBridgeAidl extends IRxRemote.Stub implements RxBridge {

    private static final String TAG = RxBridgeAidl.class.getSimpleName();

    // Local side
    private final Context mContext;
    private ComponentName mComponentName;
    private OnComponentRegistrationListener mRegistrationListener;
    private Map<RemoteKey, Map<SubscriberKey, Subscriber>> mSubscribers = new HashMap<>();
    private Map<RemoteKey, Observable> mOfferedObservables = new ConcurrentHashMap<>();

    // Remote side
    protected Remotes mRemotes = new Remotes();
    protected Locals mLocals = new Locals();
    private Map<ComponentKey, AidlObservable> mBoundObservables = new ConcurrentHashMap<>();

    public RxBridgeAidl(Context context) {
        mContext = context;
        mComponentName = new ComponentName(context, context.getClass());
    }

    public RxBridgeAidl(Context context, OnComponentRegistrationListener listener) {
        this(context);
        mRegistrationListener = listener;
    }

    /**
     * The enter registration point. The ServerConnection starts registration
     * process here.
     *
     * @param service
     * @param compName
     * @throws RemoteException
     */
    // TODO try make this private. Inplements ServiceConnection.
    protected void registerOn(IBinder service, ComponentName compName) throws RemoteException {
        if (service instanceof RxBridgeAidl) {
            // Service is running our process.
            // Register it locally.
            RxBridgeAidl bridge = (RxBridgeAidl)service;
            mLocals.put(bridge, compName);
            bridge.registerLocally(this);
        } else {
            // Service is remote one.
            // Register it on remote side.
            IRxRemote remote = IRxRemote.Stub.asInterface(service);
            mRemotes.put(remote, compName);
            remote.register(this);
        }
    }

    /**
     * IRxRemote implementation. Don't call it directly.
     *
     * @param remote
     */
    @Override
    public void register(IRxRemote remote) {
        try {
            ComponentName component = remote.getComponentName();
            Log.i(TAG + " RemoteRx", "registerRemote " + remote + " " + component);
            if (mRemotes.contains(component))
                return;


            remote.register(this);
            mRemotes.put(remote, component);
            fireRegistered(component);
        } catch (RemoteException e) {
            // no need to do anything here
        }
    }

    /**
     * Call from the other instances of {@code RxBridgeAidl}.
     *
     * @param local
     */
    private void registerLocally(@NonNull final RxBridgeAidl local) {
        try {
            final ComponentName name = local.getComponentName();
            mLocals.put(local, name);
            fireRegistered(name);
        } catch (RemoteException e) {
            // Local execution. No RemoteException won't be thrown
        }
    }

    /**
     * Notification.
     * @param component
     */
    private void fireRegistered(ComponentName component) {
        onComponentRegistered(component);
        if (mRegistrationListener != null)
            mRegistrationListener.onComponentRegistered(component);
    }


    /**
     * The ServerConnection uregister here.
     *
     * @param service
     * @param compName
     * @throws RemoteException
     */
    // TODO try make this private. Inplements ServiceConnection.
    protected void unregisterFrom(IBinder service, ComponentName compName) throws RemoteException {
        unbindObservables();
        if (service instanceof RxBridgeAidl) {
            RxBridgeAidl bridge = mLocals.remove(compName);
            mLocals.remove(compName);
            bridge.unregisterLocally(this);
        } else {
            IRxRemote remote = IRxRemote.Stub.asInterface(service);
            mRemotes.remove(compName);
            remote.unregister(this);
        }
    }

    /**
     * IRxRemote implementation. Don't call it directly.
     *
     * @param remote
     */
    @Override
    public void unregister(IRxRemote remote) throws RemoteException {
        Log.i(TAG + " RemoteRx", "unregisterRemote " + remote + " " + remote.getComponentName());
        ComponentName component = mRemotes.remove(remote);
        fireRegistered(component);
    }

    /**
     * Call from the other instances of {@code RxBridgeAidl}.
     *
     * @param local
     */
    public void unregisterLocally(@NonNull final RxBridgeAidl local) {
        try {
            final ComponentName name = local.getComponentName();
            mLocals.remove(name);
            fireUnregistered(name);
        } catch (RemoteException e) {
            // Local execution. No RemoteException won't be thrown
        }
    }

    /**
     * Notification.
     * @param component
     */
    private void fireUnregistered(ComponentName component) {
        onComponentUnregistered(component);
        if (mRegistrationListener != null)
            mRegistrationListener.onComponentRegistered(component);
    }

    /**
     * IRxRemote implementation. Don't call it directly.
     *
     * @param callback
     * @param remoteKey
     * @param subscriberKey
     */
    @Override
    public void subscribe(IObservableCallback callback,
                          RemoteKey remoteKey,
                          SubscriberKey subscriberKey)
    {
        synchronized (remoteKey) { // TODO no-no. Should be map with remote key lock

            Observable observable = mOfferedObservables.get(remoteKey); //TODO introduce getType() in remote key

            Subscriber subscriber = new Subscriber() {
                @Override
                public void onCompleted() {
                    try {
                        callback.onComplete(subscriberKey);
                    } catch (RemoteException e) {
                        // ignore
                    }
                }

                @Override
                public void onError(Throwable e) {
                    try {
                        callback.onError(subscriberKey, new RemoteThrowable(e));
                    } catch (RemoteException re) {
                        // ignore
                    }
                }

                @Override
                public void onNext(Object o) {
                    try {
                        callback.onNext(subscriberKey,
                                new RemoteItem((Parcelable)remoteKey.type.cast(o)));
                    } catch (RemoteException e) {
                        // ignore
                    }
                }
            };

            if (!mSubscribers.containsKey(remoteKey))
                mSubscribers.put(remoteKey, new HashMap<>());

            mSubscribers.get(remoteKey).put(subscriberKey, subscriber);
            observable.subscribe(subscriber);
        }
    }

    @Override
    public void unsubscribe(RemoteKey remoteKey, SubscriberKey subscriberKey) throws RemoteException {
        synchronized (remoteKey) { // TODO no-no. Should be map with remote key lock
            mSubscribers.get(remoteKey).get(subscriberKey).unsubscribe();
        }
    }

    @Override
    public void onObservableOffered(RemoteKey remoteKey) throws RemoteException {
        закончил здесь
    }

    @Override
    public void onObservableOfferedLocally(Observable observable) {
        закончил здесь
    }

    @Override
    public void onObservableDismissed(RemoteKey remoteKey) throws RemoteException {
        закончил здесь
    }

    /**
     * Called when a remote bridge is regirested to this bridge
     * @param name
     */
    protected void onComponentRegistered(ComponentName name) {}

    /**
     * Called when a remote bridge is unregirested to this bridge
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
     * @param remoteKey
     * @param observable
     * @param <T>
     */
    @Override
    public synchronized <T> void offerObservable(RemoteKey<T> remoteKey, Observable<T> observable) {
        Log.i(TAG + " RemoteRx", "unsubscribe(key)");
        if (observable == null)
            throw new IllegalArgumentException("Observable can't be null");

        mOfferedObservables.put(remoteKey, observable);
        for (IRxRemote remote : mRemotes.all()) {
            try {
                remote.onObservableOffered(remoteKey);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        for (RxBridgeAidl local : mLocals.all()) {
            try {
                local.onObservableOffered(remoteKey);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public <T> void dismissObservable(RemoteKey<T> remoteKey) {
        synchronized (remoteKey) {
            mOfferedObservables.remove(remoteKey);
            for (IRxRemote remote : mRemotes.all()) {
                try {
                    remote.onObservableDismissed(remoteKey);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            for (RxBridgeAidl local : mLocals.all()) {
                try {
                    local.onObservableDismissed(remoteKey);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            Map<SubscriberKey, Subscriber> map = mSubscribers.get(remoteKey);
            if (map != null) {
                for (Subscriber subscriber : map.values()){
                    subscriber.unsubscribe();
                }
            }
        }
    }

    @Override
    public void dismissObservables() {
        for (RemoteKey remoteKey : mOfferedObservables.keySet())
            dismissObservable(remoteKey);
    }

    private Observable<? extends RxRemote> mRxRemoteObservable;
    @Override
    public Observable<? extends RxRemote> remote() {
        return mRxRemoteObservable;
    }

    public <T> Observable<T> bindObservable(RemoteKey<T> key, Class clazz) {
        return bindObservable(key, new ComponentName(mContext, clazz));
    }

    private <T> Observable<T> bindObservable(RemoteKey<T> key, ComponentName componentName) {
        ComponentKey<T> compKey = new ComponentKey(key, componentName); // TODO make cache for remote component key

        Observable<T> observable;
        if (mLocals.contains(componentName)) {
            observable = mLocals.get(componentName).mOfferedObservables.get(key);
        } else {
            observable = bindRemoteObservable(compKey);
        }
        return observable;
    }

    private <T> Observable<T> bindRemoteObservable(ComponentKey<T> compKey) {
        synchronized (compKey) {
            if (mBoundObservables.containsKey(compKey))
                return mBoundObservables.get(compKey);

            IRxRemote iRemote = mRemotes.get(compKey.component);
            AidlOnSubscribe<T> onSubscribe
                    = new AidlOnSubscribe<>(iRemote, compKey.remoteKey);
            AidlObservable<T> observable = new AidlObservable<>(onSubscribe);
            mBoundObservables.put(compKey, observable);
            return observable;
        }
    }

    /**
     * TODO probably we bon't need unbind at all !!!!!
     * because it's not obvious what to do with observable
     * without corrupting it's nature.
     *
     * @param key
     * @param componentName
     * @param <T>
     */
    private <T> void unbindObservable(RemoteKey<T> key, ComponentName componentName) {
        ComponentKey compKey = new ComponentKey(key, componentName); // TODO make cache for remote component key
        unbindObservable(compKey);
    }

    private <T> void unbindObservable(RemoteKey<T> key, Class clazz) {
        unbindObservable(key, new ComponentName(mContext, clazz));
    }

    private <T> void unbindObservable(ComponentKey<T> compKey) {
        synchronized (compKey) {
            AidlObservable observable = mBoundObservables.remove(compKey);
            Log.i(TAG + " RxRemote", "unbindObservable " + observable);
//            if (observable != null)
//                observable.complete();
        }
    }

    private void unbindObservables() {
        Log.i(TAG + " RxRemote", "unbindObservables()");
        for(ComponentKey compKey : mBoundObservables.keySet())
            unbindObservable(compKey);
    }

}
