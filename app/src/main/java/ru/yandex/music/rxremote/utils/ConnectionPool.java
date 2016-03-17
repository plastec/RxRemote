package ru.yandex.music.rxremote.utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ru.yandex.music.rxremote.RxBridgeAidl;
import rx.Observable;

// TODO synchronize

/**
 * Created by ypavshl on 17.3.16.
 */
public abstract class ConnectionPool {

    private final Map<Context, Observable<RxBridgeAidl>> mPool = new HashMap<>();

    public Observable<RxBridgeAidl> get(@NonNull final Context context) {
        if (mPool.containsKey(context))
            return mPool.get(context);

        Observable.OnSubscribe<RxBridgeAidl> onSubscribe
                = new ConnectionOnSubscribe(context, buildIntent(context), buildFlags(context));
        Observable<RxBridgeAidl> observable = Observable.create(onSubscribe);
        mPool.put(context, observable);

        return observable;
    }

    @NonNull
    public abstract Intent buildIntent(@NonNull final Context context);

    public abstract int buildFlags(@NonNull final Context context);
}
