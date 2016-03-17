package ru.yandex.music.rxremote.utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import ru.yandex.music.rxremote.RxBridgeAidl;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

/**
 * Created by ypavshl on 17.3.16.
 */
class ConnectionOnSubscribe implements Observable.OnSubscribe<RxBridgeAidl> {

    private static final String TAG = ConnectionOnSubscribe.class.getSimpleName();

    private final Context mContext;
    private final Intent mIntent;
    private int mFlags;
    private RxBridgeAidl mBridge;
    private int mSubscribers;

    ConnectionOnSubscribe(@NonNull final Context context,
                          @NonNull final Intent intent,
                          final int flags) {
        mContext = context;
        mIntent = intent;
        mFlags = flags;
    }

    @Override
    public void call(Subscriber<? super RxBridgeAidl> subscriber) {
        if (mBridge == null) {
            mBridge = new RxBridgeAidl(mContext);
        }

        synchronized (this) {
            if (mSubscribers == 0) {
                Log.i(TAG + " AAA", "mContext.bindService(mIntent, mBridge, mFlags);");
                mContext.bindService(mIntent, mBridge, mFlags);
            }
            mSubscribers++;
        }

        subscriber.onNext(mBridge);

        subscriber.add(Subscriptions.create(() -> {
            synchronized (ConnectionOnSubscribe.this) {
                mSubscribers--;
                if (mSubscribers == 0) {
                    Log.i(TAG + " AAA", "mContext.unbindService(mBridge);");
                    mBridge.dismissObservables();
                    mContext.unbindService(mBridge);
                    mBridge = null;
                }
            }
        }));
    }
}
