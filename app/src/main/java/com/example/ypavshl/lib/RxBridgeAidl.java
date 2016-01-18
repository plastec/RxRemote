package com.example.ypavshl.lib;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

import com.example.ypavshl.lib.parcel.ComponentKey;
import com.example.ypavshl.lib.parcel.RemoteItem;
import com.example.ypavshl.lib.parcel.RemoteKey;
import com.example.ypavshl.lib.parcel.RemoteThrowable;
import com.example.ypavshl.lib.parcel.SubscriberKey;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private Map<ComponentKey, RemoteObservable> mBoundObservables = new ConcurrentHashMap<>();

    public RxBridgeAidl(Context context) {
        mContext = context;
        mComponentName = new ComponentName(context, context.getClass());
    }

    public RxBridgeAidl(Context context, OnComponentRegistrationListener listener) {
        this(context);
        mRegistrationListener = listener;
    }

    protected void registerOn(IBinder service, ComponentName compName) throws RemoteException {
        if (service instanceof RxBridgeAidl) {
            RxBridgeAidl bridge = (RxBridgeAidl)service;
            mLocals.put(bridge, compName);
            bridge.mLocals.put(this, getComponentName());
            bridge.fireRegistered(getComponentName());
        } else {
            IRxRemote remote = IRxRemote.Stub.asInterface(service);
            mRemotes.put(remote, compName);
            (IRxRemote.Stub.asInterface(service)).register(this);
        }
    }

    protected void unregisterFrom(IBinder service, ComponentName compName) throws RemoteException {
        unbindObservables();
        if (service instanceof RxBridgeAidl) {
            RxBridgeAidl bridge = mLocals.remove(compName);
            bridge.fireUnregistered(compName);
        } else {
            IRxRemote remote = IRxRemote.Stub.asInterface(service);
            remote.unregister(this);
        }
    }

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

    private void fireRegistered(ComponentName component) {
        onComponentRegistered(component);
        if (mRegistrationListener != null)
                mRegistrationListener.onComponentRegistered(component);
    }

    @Override
    public void unregister(IRxRemote remote) throws RemoteException {
        Log.i(TAG + " RemoteRx", "unregisterRemote " + remote + " " + remote.getComponentName());
        ComponentName component = mRemotes.remove(remote);
        fireRegistered(component);
    }

    private void fireUnregistered(ComponentName component) {
        onComponentUnregistered(component);
        if (mRegistrationListener != null)
            mRegistrationListener.onComponentRegistered(component);
    }

    @Override
    public void subscribe(IObservableCallback callback, RemoteKey remoteKey, SubscriberKey subscriberKey) throws RemoteException {
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

    @Override
    public synchronized <T> void offerObservable(RemoteKey<T> remoteKey, Observable<T> observable) {
        Log.i(TAG + " RemoteRx", "unsubscribe(key)");
        if (observable == null)
            throw new IllegalArgumentException("Observable can't be null");

        mOfferedObservables.put(remoteKey, observable);
    }

    @Override
    public <T> void dismissObservable(RemoteKey<T> remoteKey) {
        synchronized (remoteKey) {
            mOfferedObservables.remove(remoteKey);
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

    public <T> Observable<T> bindObservable(RemoteKey<T> key, Class clazz) {
        return bindObservable(key, new ComponentName(mContext, clazz));
    }

    @Override
    public <T> Observable<T> bindObservable(RemoteKey<T> key, ComponentName componentName) {
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
            RemoteOnSubscribe<T> onSubscribe
                    = new RemoteOnSubscribe<>(iRemote, compKey.remoteKey);
            RemoteObservable<T> observable = new RemoteObservable<>(onSubscribe);
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
    @Override
    public <T> void unbindObservable(RemoteKey<T> key, ComponentName componentName) {
        ComponentKey compKey = new ComponentKey(key, componentName); // TODO make cache for remote component key
        unbindObservable(compKey);
    }

    public <T> void unbindObservable(RemoteKey<T> key, Class clazz) {
        unbindObservable(key, new ComponentName(mContext, clazz));
    }

    private <T> void unbindObservable(ComponentKey<T> compKey) {
        synchronized (compKey) {
            RemoteObservable observable = mBoundObservables.remove(compKey);
            Log.i(TAG + " RxRemote", "unbindObservable " + observable);
//            if (observable != null)
//                observable.complete();
        }
    }

    @Override
    public void unbindObservables() {
        Log.i(TAG + " RxRemote", "unbindObservables()");
        for(ComponentKey compKey : mBoundObservables.keySet())
            unbindObservable(compKey);
    }

}
