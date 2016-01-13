package com.example.ypavshl.lib;

import android.content.ComponentName;
import android.content.Context;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

import com.example.ypavshl.lib.parcel.RemoteItem;
import com.example.ypavshl.lib.parcel.RemoteKey;
import com.example.ypavshl.lib.parcel.RemoteThrowable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

// TODO consider spliting this class into two separately logical Classes
/**
 * Created by ypavshl on 6.1.16.
 */
class RxBridgeAidl extends IRxRemote.Stub implements RxBridge {

    private static final String TAG = RxBridgeAidl.class.getSimpleName();

    // Local side
    private final Context mContext;
    private ComponentName mComponentName;
    private OnRxRemoteRegistrationListener mRegistrationListener;
    private Map<ComponentRemoteKey, IRxRemote> mRemoteCallbacks = new HashMap<>();
    private Map<ComponentRemoteKey, Subscription> mSubscriptions = new HashMap<>();
    private Map<ComponentRemoteKey, Observable> mOfferedObservables = new ConcurrentHashMap<>();

    // Remote side
    protected Remotes mRemotes = new Remotes();
    private Map<ComponentRemoteKey, Subject> mBoundObservables = new ConcurrentHashMap<>();

    public RxBridgeAidl(Context context) {
        mContext = context;
        mComponentName = new ComponentName(context, context.getClass());
    }

    public RxBridgeAidl(Context context, OnRxRemoteRegistrationListener listener) {
        this(context);
        mRegistrationListener = listener;
    }

    @Override
    public void onNext(ComponentRemoteKey key, RemoteItem item) throws RemoteException {
        mBoundObservables.get(key).onNext(item.item);
    }

    @Override
    public void onStart(ComponentRemoteKey key) throws RemoteException {
        // пока что хз
        // может и не нужен будет совсем
    }

    @Override
    public void onComplete(ComponentRemoteKey key) throws RemoteException {
        mBoundObservables.get(key).onCompleted();
    }

    @Override
    public void onError(ComponentRemoteKey key, RemoteThrowable t) throws RemoteException {
        mBoundObservables.get(key).onError(t.throwable);
    }

    protected void registerOn(IRxRemote remote) throws RemoteException {
        remote.registerRemote(this);
    }

    protected void unregisterFrom(IRxRemote remote) throws RemoteException {
        unbindObservables();
        remote.unregisterRemote(this);
    }

    @Override
    public void registerRemote(IRxRemote remote) {
        try {
            ComponentName component = remote.getComponentName();
            Log.i(TAG + " RemoteRx", "registerRemote " + remote + " " + component);
            if (mRemotes.contains(component))
                return;

            remote.registerRemote(this);
            mRemotes.put(remote, remote.getComponentName());
            onRxRemoteRegistered(remote, mRemotes.get(remote));
            if (mRegistrationListener != null)
                mRegistrationListener.onRemoteRegistered(remote, component);
        } catch (RemoteException e) {
            // no need to do anything here
        }
    }

    @Override
    public void unregisterRemote(IRxRemote remote) throws RemoteException {

        Log.i(TAG + " RemoteRx", "unregisterRemote " + remote + " " + remote.getComponentName());
        ComponentName component = mRemotes.remove(remote);
        onRxRemoteUnregistered(remote, component);
        if (mRegistrationListener != null)
            mRegistrationListener.onRemoteUnregistered(remote, component);
    }

    /**
     * Called when a remote bridge is regirested to this bridge
     * @param remote
     * @param name
     */
    protected void onRxRemoteRegistered(IRxRemote remote, ComponentName name) {}

    /**
     * Called when a remote bridge is unregirested to this bridge
     * @param remote
     * @param name
     */
    protected void onRxRemoteUnregistered(IRxRemote remote, ComponentName name) {}

    @Override
    public boolean registerKey(IRxRemote remote, ComponentRemoteKey key) {
        Log.i(TAG + " RemoteRx", "registerCallback " + remote);
        synchronized (key) {
            if (mOfferedObservables.containsKey(key)) {
                    mRemoteCallbacks.put(key, remote);
                if (!mSubscriptions.containsKey(key))
                    subscribe(key);
                return true;
            } else {
                return false;
            }
        }
    }


    @Override
    public boolean unregisterKey(IRxRemote remote, ComponentRemoteKey key) {
        Log.i(TAG + " RemoteRx", "unregisterKey");
        synchronized (key) {
            if (mRemoteCallbacks.remove(key) != null){
                unsubscribe(key);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public ComponentName getComponentName() throws RemoteException {
        if (mComponentName.getPackageName().isEmpty())
            mComponentName = new ComponentName(mContext, mContext.getClass());

        if (mComponentName.getPackageName().isEmpty())
            throw new RemoteException("Can't get package name for the component!");

        return mComponentName;
    }

    @Override
    public synchronized <T> void offerObservable(RemoteKey<T> key, Observable<T> observable) {
        Log.i(TAG + " RemoteRx", "unsubscribe(key)");
        if (observable == null)
            throw new IllegalArgumentException("observable can't be null");

        ComponentRemoteKey compKey = new ComponentRemoteKey(key, mContext); // TODO make cache
        mOfferedObservables.put(compKey, observable);
    }

    @Override
    public <T> void dismissObservable(RemoteKey<T> key) {
        ComponentRemoteKey compKey = new ComponentRemoteKey(key, mContext); // TODO make cache of local component key
        dismissObservable(compKey);
    }

    @Override
    public void dismissObservables() {
        for (ComponentRemoteKey compKey : mOfferedObservables.keySet())
            dismissObservable(compKey);
    }


    private void dismissObservable(ComponentRemoteKey compKey) {
        synchronized (compKey.remoteKey) {
            mOfferedObservables.remove(compKey);
            unsubscribe(compKey);
        }
    }

    public <T> Observable<T> bindObservable(RemoteKey<T> key, Class clazz) {
        return bindObservable(key, new ComponentName(mContext, clazz));
    }

    @Override
    public <T> Observable<T> bindObservable(RemoteKey<T> key, ComponentName componentName) {
        ComponentRemoteKey<T> compKey = new ComponentRemoteKey(key, componentName); // TODO make cache for remote component key
        return bindObservable(key, compKey, componentName);
    }

    private <T> Observable<T> bindObservable(RemoteKey<T> key, ComponentRemoteKey<T> compKey, ComponentName componentName) {
        synchronized (key) {
            if (mBoundObservables.containsKey(compKey)) {
                return mBoundObservables.get(compKey);
            }

            BehaviorSubject<T> subject = null;

            try {
                if (mRemotes.get(componentName).registerKey(this, compKey)) {
                    subject = BehaviorSubject.create();
                    mBoundObservables.put(compKey, subject);
                }
            } catch (RemoteException e) {
                // no need to do anything here
            }

            return subject;
        }
    }

    @Override
    public <T> void unbindObservable(RemoteKey<T> key, ComponentName componentName) {
        ComponentRemoteKey compKey = new ComponentRemoteKey(key, componentName); // TODO make cache for remote component key
        unbindObservable(compKey);
    }

    public <T> void unbindObservable(RemoteKey<T> key, Class clazz) {
        unbindObservable(key, new ComponentName(mContext, clazz));
    }

    private <T> void unbindObservable(ComponentRemoteKey<T> compKey) {
        synchronized (compKey.remoteKey) {
            Subject s = mBoundObservables.remove(compKey);
            if (s != null){
                s.onCompleted();
            }

            try {
                mRemotes.get(compKey.component).unregisterKey(this, compKey);
            } catch (RemoteException e) {
                // no need to do anything here
            }
        }
    }

    @Override
    public void unbindObservables() {
        for(ComponentRemoteKey compKey : mBoundObservables.keySet())
            unbindObservable(compKey);
    }

    private <T> void subscribe(final ComponentRemoteKey<T> key) {
        synchronized (key) {
            if (mSubscriptions.containsKey(key))
                return;

            final IRxRemote remote = mRemoteCallbacks.get(key);

            final Action1<T> onNext = new Action1<T>() {
                @Override
                public void call(T item) {
                    synchronized (key) {
                        try {
                            remote.onNext(key, new RemoteItem((Parcelable)item));
                        } catch (RemoteException e) {
                            // ignore
                        }
                    }
                }
            };

            final Action1<Throwable> onError = new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    synchronized (key) {
                        try {
                            remote.onError(key, new RemoteThrowable(throwable));
                        } catch (RemoteException e) {
                            // ignore
                        }
                    }
                }
            };

            final Action0 onComplete =  new Action0() {
                @Override
                public void call() {
                    synchronized (key) {
                        try {
                            remote.onComplete(key);
                        } catch (RemoteException e) {
                            // ignore
                        }
                    }
                }
            };

            Subscription subscription = mOfferedObservables.get(key).subscribe(onNext, onError, onComplete);
            mSubscriptions.put(key, subscription);
        }
    }

    private void unsubscribe(ComponentRemoteKey key) {
        synchronized (key) {
            Subscription s = mSubscriptions.remove(key);
            if (s != null)
                s.unsubscribe();
        }
    }
}
