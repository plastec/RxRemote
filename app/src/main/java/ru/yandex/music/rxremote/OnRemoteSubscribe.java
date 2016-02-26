package ru.yandex.music.rxremote;

import android.content.ComponentName;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.yandex.music.rxremote.parcel.RemoteKey;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by ypavshl on 11.3.16.
 */
class OnRemoteSubscribe<T> implements Observable.OnSubscribe<T> {

    private final Observable<? extends RxRemote> mObservable;
    private final RemoteKey<T> mKey;
    private final ComponentName mName;


    public OnRemoteSubscribe(@NonNull final Observable<? extends RxRemote> observable,
                             @NonNull final RemoteKey<T> key,
                             @Nullable final ComponentName name) {
        mObservable = observable;
        mKey = key;
        mName = name;
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {
        mObservable.subscribe(remoteRx -> {
                if (remoteRx.getRemoteKey().equals(mKey)
                        && (mName == null || remoteRx.getComponentName().equals(mName))) {
                    remoteRx.getObservable(mKey).subscribe(subscriber);
                }
            }
        );
    }
}
