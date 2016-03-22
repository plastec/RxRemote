package com.example.ypavshl.example;

import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.ypavshl.rxservice.R;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import ru.yandex.music.rxremote.RxSingleBridge;
import ru.yandex.music.rxremote.utils.ConnectionPool;
import ru.yandex.music.rxremote.parcel.RemoteKey;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;


public class MyActivity extends RxAppCompatActivity{
    private static final String TAG = MyActivity.class.getSimpleName();

    public static final RemoteKey<ButtonItem> BUTTON_OBSERVABLE_KEY
            = new RemoteKey<>("BUTTON_OBSERVABLE", ButtonItem.class);

    private TextView mTextView;
    private FloatingActionButton mButton;

    private BehaviorSubject<ButtonItem> mButtonObservable = BehaviorSubject.create();

    private ConnectionPool mPool = new ConnectionPool() {
        @NonNull
        @Override
        public Intent buildIntent(@NonNull Context context) {
            return new Intent(context, MyService.class);
        }

        @Override
        public int buildFlags(@NonNull Context context) {
            return Context.BIND_AUTO_CREATE;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        mPool.get(this)
//            .flatMap(rxBridgeAidl -> {
//                rxBridgeAidl.offerObservable(BUTTON_OBSERVABLE_KEY, mButtonObservable);
//                return rxBridgeAidl.observe(MyService.COLOR_OBSERVABLE_KEY);
//            })
//            .compose(this.bindToLifecycle())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(colorItem -> {
//                mTextView.setText(colorItem.getText());
//                mTextView.setBackgroundColor(colorItem.getColor());
//            });

        Intent i = new Intent(this, MyService.class);
        RxSingleBridge bridge = new RxSingleBridge();
        i.putExtra("result_bridge", bridge);
        startService(i);

        Log.i(TAG + " REMOTE", "onCreate subscribe()");
        bridge.observe(MyService.COLOR_OBSERVABLE_KEY)
                .compose(this.bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(colorItem -> {
                    mTextView.setText(colorItem.getText());
                    mTextView.setBackgroundColor(colorItem.getColor());
                });

        mTextView = (TextView) findViewById(R.id.text);

        mButton = (FloatingActionButton) findViewById(R.id.fab);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mButtonObservable.onNext(new ButtonItem(0));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
