package ru.yandex.music.rxremote;

import ru.yandex.music.rxremote.parcel.RemoteKey;
import ru.yandex.music.rxremote.parcel.SubscriberKey;
import ru.yandex.music.rxremote.IObservableCallback;

//
// The IDL communitation for providing mirrored observables between processes.
//

interface IConnector {

    void register(in IConnector remote);
    void unregister(in IConnector remote);

    void onObservableOffered(in IConnector remote, in RemoteKey remoteKey,
                             in ComponentName componentName);
    void onObservableDismissed(in RemoteKey remoteKey, in ComponentName componentName);

    void subscribe(in IObservableCallback observable, in RemoteKey remoteKey,
                   in SubscriberKey subscriberKey);
    void unsubscribe(in RemoteKey remoteKey, in SubscriberKey subscriberKey);

    ComponentName getComponentName();
}
