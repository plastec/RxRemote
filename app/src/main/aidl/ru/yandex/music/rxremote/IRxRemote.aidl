package ru.yandex.music.rxremote;

import ru.yandex.music.rxremote.parcel.RemoteKey;
import ru.yandex.music.rxremote.parcel.SubscriberKey;
import ru.yandex.music.rxremote.IObservableCallback;

interface IRxRemote {
    void register(in IRxRemote remote);
    void unregister(in IRxRemote remote);
    void subscribe(in IObservableCallback observable, in RemoteKey remoteKey, in SubscriberKey subscriberKey);
    void unsubscribe(in RemoteKey remoteKey, in SubscriberKey subscriberKey);
    void onObservableOffered(in RemoteKey remoteKey);
    void onObservableDismissed(in RemoteKey remoteKey);
    ComponentName getComponentName();
}
