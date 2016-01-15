// IRxBridge.aidl
package com.example.ypavshl.lib;

import com.example.ypavshl.lib.parcel.RemoteKey;
import com.example.ypavshl.lib.parcel.SubscriberKey;
//import com.example.ypavshl.lib.parcel.SubscriberKeyDeprecatedDeprecated;
//import com.example.ypavshl.lib.parcel.RemoteItem;
//import com.example.ypavshl.lib.parcel.RemoteThrowable;
//import com.example.ypavshl.lib.IObservable;
import com.example.ypavshl.lib.IObservableCallback;

interface IRxRemote {
//    void onNext(in SubscriberKeyDeprecated key, in RemoteItem item);
//    void onStart(in SubscriberKeyDeprecated key);
//    void onComplete(in SubscriberKeyDeprecated key);
//    void onError(in SubscriberKeyDeprecated key, in RemoteThrowable throwable);

    void register(in IRxRemote remote);
    void unregister(in IRxRemote remote);

    void subscribe(in IObservableCallback observable, in RemoteKey remoteKey, in SubscriberKey subscriberKey);
    void unsubscribe(in RemoteKey remoteKey, in SubscriberKey subscriberKey);

    ComponentName getComponentName();
}
