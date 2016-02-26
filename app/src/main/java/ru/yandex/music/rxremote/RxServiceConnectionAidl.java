package ru.yandex.music.rxremote;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import ru.yandex.music.rxremote.parcel.RemoteKey;
import rx.Observable;

/**
 * TODO implement multiple services support
 *
 * Created by ypavshl on 6.1.16.
 */
public class RxServiceConnectionAidl extends RxBridgeAidl implements ServiceConnection {

    private static final String TAG = RxServiceConnectionAidl.class.getSimpleName();

    private ComponentName mServiceComponentName;
    private IBinder mService;

    public RxServiceConnectionAidl(Context context) {
        super(context);
    }

    public void onServiceConnected(ComponentName className, IBinder service) {
        // This is called when the connection with the service has been
        // established, giving us the service object we can use to
        // interact with the service.  We are communicating with our
        // service through an IDL interface, so get a client-side
        // representation of that from the raw service object.
        Log.i(TAG + " RxRemote", "onServiceConnected " + service);
        mServiceComponentName = className;
        mService = service;

        try {
            registerOn(service, className);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void onServiceDisconnected(ComponentName className) {
        Log.i(TAG + " RemoteRx", "onServiceDisconnected " + this);
        // This is called when the connection with the service has been
        // unexpectedly disconnected -- that is, its process crashed.
        try {
            unregisterFrom(mService, mServiceComponentName);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

}