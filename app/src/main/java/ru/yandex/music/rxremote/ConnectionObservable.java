package ru.yandex.music.rxremote;

import android.content.Context;

import rx.Observable;

/**
 * TODO make bind to lifecyrcle
 *
 *
 * Created by ypavshl on 20.1.16.
 */
public class ConnectionObservable<T> extends Observable<Observable<T>>{

    private OnConnect mOnSubscribeConnection;
    private Context mContext;

    public ConnectionObservable(Context context, OnConnect<T> onSubscribe) {
        super(onSubscribe);
        mOnSubscribeConnection = onSubscribe;
        mContext = context;
    }

    public void unbind() {
        mContext.unbindService(mOnSubscribeConnection);
    }
}
