package com.example.ypavshl.lib;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.ypavshl.lib.parcel.RemoteKey;

import rx.Observable;

/**
 * Created by ypavshl on 6.1.16.
 */
public class RxServiceConnectionAidl extends RxBridgeAidl implements ServiceConnection {

    private static final String TAG = RxServiceConnectionAidl.class.getSimpleName();

    private IRxRemote mServiceRxRemote;
    private ComponentName mServiceComponentName;

    public RxServiceConnectionAidl(Context context) {
        super(context);
    }

    public void onServiceConnected(ComponentName className, IBinder service) {
        // This is called when the connection with the service has been
        // established, giving us the service object we can use to
        // interact with the service.  We are communicating with our
        // service through an IDL interface, so get a client-side
        // representation of that from the raw service object.
        Log.i(TAG + " RemoteRx", "onServiceConnected " + this);
        mServiceComponentName = className;
        mServiceRxRemote = IRxRemote.Stub.asInterface(service);

        try {
            mRemotes.put(mServiceRxRemote, mServiceComponentName);
            registerOn(mServiceRxRemote);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void onServiceDisconnected(ComponentName className) {
        Log.i(TAG + " RemoteRx", "onServiceDisconnected " + this);
        // This is called when the connection with the service has been
        // unexpectedly disconnected -- that is, its process crashed.
        try {
            unregisterFrom(mServiceRxRemote);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> Observable<T> bindObservable(RemoteKey<T> key) {
        return bindObservable(key, mServiceComponentName);
    }

    @Override
    public <T> void unbindObservable(RemoteKey<T> key, ComponentName componentName) {
        unbindObservable(key, mServiceComponentName);
    }


}
