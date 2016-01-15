package com.example.ypavshl.lib;

import com.example.ypavshl.lib.parcel.SubscriberKey;
import com.example.ypavshl.lib.parcel.RemoteItem;
import com.example.ypavshl.lib.parcel.RemoteThrowable;

interface IObservableCallback {
    void onNext(in SubscriberKey key, in RemoteItem item);
    void onStart(in SubscriberKey key);
    void onComplete(in SubscriberKey key);
    void onError(in SubscriberKey key, in RemoteThrowable throwable);
}
