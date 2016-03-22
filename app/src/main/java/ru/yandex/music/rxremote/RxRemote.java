package ru.yandex.music.rxremote;

import android.content.ComponentName;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.yandex.music.rxremote.parcel.RemoteKey;
import rx.Observable;

/**
 * Created by ypavshl on 29.1.16.
 */
public final class RxRemote {

    private final Observable mObservable;
    private final RemoteKey mKey;
    private final ComponentName mComponent;

    protected <T> RxRemote(@Nullable Observable<T> o,
                           @NonNull RemoteKey<T> k,
                           @Nullable ComponentName c) {
        if (k == null)
            throw new IllegalArgumentException("RemoteKey == null");

        mObservable = o;
        mKey = k;
        mComponent = c;
    }

    public <T> Observable<T> getObservable(RemoteKey<T> key) {
        return (Observable<T>)mObservable;
    }

    public RemoteKey getRemoteKey() {
        return mKey;
    }

    public ComponentName getComponentName() {
        return mComponent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RxRemote rxRemote = (RxRemote) o;

        if (mObservable != null ? !mObservable.equals(rxRemote.mObservable) : rxRemote.mObservable != null)
            return false;
        if (!mKey.equals(rxRemote.mKey)) return false;
        return !(mComponent != null ? !mComponent.equals(rxRemote.mComponent) : rxRemote.mComponent != null);

    }

    @Override
    public int hashCode() {
        int result = mObservable != null ? mObservable.hashCode() : 0;
        result = 31 * result + mKey.hashCode();
        result = 31 * result + (mComponent != null ? mComponent.hashCode() : 0);
        return result;
    }

    //    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//         RxRemote rxRemote = (RxRemote) o;
//
//        if (!mObservable.equals(rxRemote.mObservable)) return false;
//        if (!mKey.equals(rxRemote.mKey)) return false;
//        return mComponent.equals(rxRemote.mComponent);
//
//    }
//
//    @Override
//    public int hashCode() {
//        int result = mObservable.hashCode();
//        result = 31 * result + mKey.hashCode();
//        result = 31 * result + mComponent.hashCode();
//        return result;
//    }
}
