package ru.yandex.music.rxremote.utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ru.yandex.music.rxremote.RxBridge;
import rx.Observable;

// TODO synchronize

/**
 * Created by ypavshl on 17.3.16.
 */
public abstract class ConnectionPool {

    private final Map<Context, Observable<RxBridge>> mPool = new HashMap<>();

    public Observable<RxBridge> get(@NonNull final Context context) {
        if (mPool.containsKey(context))
            return mPool.get(context);

        Observable.OnSubscribe<RxBridge> onSubscribe
                = new ConnectionOnSubscribe(context, buildIntent(context), buildFlags(context));
        Observable<RxBridge> observable = Observable.create(onSubscribe);
        mPool.put(context, observable);

        return observable;
    }

    @NonNull
    public abstract Intent buildIntent(@NonNull final Context context);

    public abstract int buildFlags(@NonNull final Context context);
}
