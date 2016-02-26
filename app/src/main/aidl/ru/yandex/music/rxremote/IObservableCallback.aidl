package ru.yandex.music.rxremote;

import ru.yandex.music.rxremote.parcel.SubscriberKey;
import ru.yandex.music.rxremote.parcel.RemoteItem;
import ru.yandex.music.rxremote.parcel.RemoteThrowable;

interface IObservableCallback {
    void onNext(in SubscriberKey key, in RemoteItem item);
    void onStart(in SubscriberKey key);
    void onComplete(in SubscriberKey key);
    void onError(in SubscriberKey key, in RemoteThrowable throwable);
}
