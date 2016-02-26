package ru.yandex.music.rxremote;

import android.os.Parcelable;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import ru.yandex.music.rxremote.parcel.RemoteItem;
import ru.yandex.music.rxremote.parcel.RemoteKey;
import ru.yandex.music.rxremote.parcel.RemoteThrowable;
import rx.Subscriber;

/**
 * Created by ypavshl on 11.3.16.
 */
final class RemoteSubscriber extends Subscriber {

    private final IObservableCallback mCallback;
    private final RemoteKey mKey;

    RemoteSubscriber(@NonNull final IObservableCallback callback, @NonNull final RemoteKey key) {
        mCallback = callback;
        mKey = key;
    }

    @Override
    public void onCompleted() {
        try {
            mCallback.onComplete();
        } catch (RemoteException e) {
            // ignore
        }
    }

    @Override
    public void onError(Throwable e) {
        try {
            mCallback.onError(new RemoteThrowable(e));
        } catch (RemoteException re) {
            // ignore
        }
    }

    @Override
    public void onNext(Object o) {
        try {
            mCallback.onNext(new RemoteItem((Parcelable)mKey.getmType().cast(o)));
        } catch (RemoteException e) {
            // ignore
        }
    }
}
