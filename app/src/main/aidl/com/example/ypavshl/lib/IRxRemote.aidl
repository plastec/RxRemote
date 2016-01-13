// IRxBridge.aidl
package com.example.ypavshl.lib;

import com.example.ypavshl.lib.ComponentRemoteKey;
import com.example.ypavshl.lib.parcel.RemoteItem;
import com.example.ypavshl.lib.parcel.RemoteThrowable;

interface IRxRemote {
    void onNext(in ComponentRemoteKey key, in RemoteItem item);
    void onStart(in ComponentRemoteKey key);
    void onComplete(in ComponentRemoteKey key);
    void onError(in ComponentRemoteKey key, in RemoteThrowable throwable);

    void registerRemote(in IRxRemote binder);
    void unregisterRemote(in IRxRemote binder);
    boolean registerKey(in IRxRemote binder, in ComponentRemoteKey key);
    boolean unregisterKey(in IRxRemote binder, in ComponentRemoteKey key);

    ComponentName getComponentName();
}
