// ISingleConnector.aidl
package ru.yandex.music.rxremote;

import ru.yandex.music.rxremote.parcel.RemoteKey;
import ru.yandex.music.rxremote.parcel.SubscriberKey;
import ru.yandex.music.rxremote.IObservableCallback;

interface ISingleConnector {
    void onObservableOffered(in ISingleConnector remote, in RemoteKey remoteKey);
    void onObservableDismissed(in RemoteKey remoteKey);

    void subscribe(in IObservableCallback callback, in RemoteKey remoteKey,
                   in SubscriberKey subscriberKey);
    void unsubscribe(in RemoteKey remoteKey, in SubscriberKey subscriberKey);
}
