package com.example.ypavshl.example;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.*;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.ypavshl.lib.OnComponentRegistrationListener;
import com.example.ypavshl.lib.parcel.RemoteKey;
import com.example.ypavshl.lib.RxBinderAidl;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by ypavshl on 24.12.15.
 *
 * How to generate Parcelable quickly:
 * http://codentrick.com/quickly-create-parcelable-class-in-android-studio/
 * http://www.parcelabler.com/
 *
 */
public class MyService extends Service implements OnComponentRegistrationListener {
    private static final String TAG = MyService.class.getSimpleName();

    public static final RemoteKey<ColorItem> COLOR_OBSERVABLE_KEY = new RemoteKey<>(ColorItem.class);

    private RxBinderAidl mBinder;
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mBinder = new RxBinderAidl(this, this);
        mBinder.offerObservable(COLOR_OBSERVABLE_KEY, mColorObservable);

        mRoutine = new Thread(mRunnable);
        mRoutine.start();

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mBinder.unbindObservables();
        mBinder.dismissObservables();
        mRoutine.interrupt();
        return super.onUnbind(intent);
    }

    // TODO try to substitute this callback with Observable
    @Override
    public void onComponentRegistered(ComponentName component) {
        if (MyActivity.class.getName().equals(component.getClassName())) {
            Observable<ButtonItem> observable = mBinder.bindObservable(MyActivity.BUTTON_OBSERVABLE_KEY, MyActivity.class);
            observable.subscribe(buttonItem -> {
                    mCounter = 0;
                    mColorObservable.onNext(new ColorItem("item: " + mCounter , Color.WHITE));
                });
        }
    }

    @Override
    public void onComponentUnregistered(ComponentName component) {}
}
