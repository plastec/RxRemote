package ru.yandex.music.rxremote;

import ru.yandex.music.rxremote.parcel.SubscriberKey;
import ru.yandex.music.rxremote.parcel.RemoteItem;
import ru.yandex.music.rxremote.parcel.RemoteThrowable;

interface IObservableCallback {
    void onStart();
    void onNext(in RemoteItem item);
    void onError(in RemoteThrowable throwable);
    void onComplete();
}
