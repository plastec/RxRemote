package com.example.ypavshl.example;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.*;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.ypavshl.lib.IRxRemote;
import com.example.ypavshl.lib.OnRxRemoteRegistrationListener;
import com.example.ypavshl.lib.parcel.RemoteKey;
import com.example.ypavshl.lib.RxBinderAidl;

import java.util.Random;

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
public class MyService extends Service implements OnRxRemoteRegistrationListener {
    private static final String TAG = MyService.class.getSimpleName();

    public static final RemoteKey<ColorItem> COLOR_OBSERVABLE_KEY = new RemoteKey<>();

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
    public void onRemoteRegistered(/*ncdot used*/IRxRemote remote, ComponentName component) {
        if (MyActivity.class.getName().equals(component.getClassName())) {
            mBinder.bindObservable(MyActivity.BUTTON_OBSERVABLE_KEY, MyActivity.class)
                .subscribe(buttonItem -> {
                    mCounter = 0;
                    mColorObservable.onNext(new ColorItem("item: " + mCounter , Color.WHITE));
                });
        }
    }

    @Override
    public void onRemoteUnregistered(IRxRemote remote, ComponentName component) {
    }
}
