package ru.yandex.music.rxremote;

import ru.yandex.music.rxremote.parcel.RemoteKey;

/**
 * Created by ypavshl on 29.1.16.
 */

interface OnObservableAwailabilityListener {
    void onObservableOffered(RemoteKey key);
    void onObservableDismissed(RemoteKey key);
}
