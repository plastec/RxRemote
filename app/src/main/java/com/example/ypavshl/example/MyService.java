package com.example.ypavshl.example;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.*;
import android.support.annotation.Nullable;
import android.util.Log;


import ru.yandex.music.rxremote.RxBridge;
import ru.yandex.music.rxremote.RxSingleBridge;
import ru.yandex.music.rxremote.parcel.RemoteKey;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;

/**
 * Created by ypavshl on 24.12.15.
 *
 * How to generate Parcelable quickly:
 * http://codentrick.com/quickly-create-parcelable-class-in-android-studio/
 * http://www.parcelabler.com/
 *
 */
public class MyService extends Service {
    private static final String TAG = MyService.class.getSimpleName();

    public static final RemoteKey<ColorItem> COLOR_OBSERVABLE_KEY
            = new RemoteKey<>("COLOR_OBSERVABLE", ColorItem.class);

    private RxBridge mBridge;
    private BehaviorSubject<ColorItem> mColorObservable = BehaviorSubject.create();
    private Thread mRoutine;

    private volatile int mCounter;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mCounter = 0;

            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
                mColorObservable.onNext(new ColorItem("item: " + ++mCounter  + " " + MyService.this,
                        ColorGenerator.generate()));
            }
        }
    };

//    private Observable.OnSubscribe<Integer> mOnSubscribe = new Observable.OnSubscribe<Integer>() {
//        @Override
//        public void call(Subscriber<? super Integer> subscriber) {
//            Log.i("AAAA", "call subscriber = " + subscriber + " " + this);
//        }
//    };

    @Override
    public void onCreate() {
        super.onCreate();
        mRoutine = new Thread(mRunnable);
        mRoutine.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        RxSingleBridge bridge = intent.getParcelableExtra("result_bridge");
        bridge.offerObservable(COLOR_OBSERVABLE_KEY, mColorObservable);

//        Observable<Integer> observable = Observable.create(mOnSubscribe);
//        observable.subscribe(integer -> {});
//        observable.subscribe(integer -> {});
//        observable.subscribe(integer -> {});

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mBridge = new RxBridge(this);
        mBridge.offerObservable(COLOR_OBSERVABLE_KEY, mColorObservable);

        Log.i(TAG + " REMOTE", "mBridge.get().subscribe");
        mBridge.observe(MyActivity.BUTTON_OBSERVABLE_KEY)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(colorItem -> {
                mCounter = 0;
                mColorObservable.onNext(new ColorItem("item: " + mCounter , Color.WHITE));
            });
//        mRoutine = new Thread(mRunnable);
//        mRoutine.start();
        return mBridge;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mBridge.dismissObservables();
        mRoutine.interrupt();
        return super.onUnbind(intent);
    }
}
