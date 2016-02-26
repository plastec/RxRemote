package com.example.ypavshl.example;

import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.ypavshl.rxservice.R;

import ru.yandex.music.rxremote.RxBridge;
import ru.yandex.music.rxremote.RxBridgeAidl;
import ru.yandex.music.rxremote.parcel.RemoteKey;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;


public class MyActivity extends AppCompatActivity {
    private static final String TAG = MyActivity.class.getSimpleName();

    public static final RemoteKey<ButtonItem> BUTTON_OBSERVABLE_KEY
            = new RemoteKey<>("BUTTON_OBSERVABLE", ButtonItem.class);

    private TextView mTextView;
    private FloatingActionButton mButton;

    private BehaviorSubject<ButtonItem> mButtonObservable = BehaviorSubject.create();

    private RxBridge mBridge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mBridge = new RxBridgeAidl(this);
        mBridge.offerObservable(BUTTON_OBSERVABLE_KEY, mButtonObservable);
        Intent intent = new Intent(this, MyService.class);
        bindService(intent, mBridge, Context.BIND_AUTO_CREATE);
        Log.i(TAG + " REMOTE", "mBridge.observe().subscribe(remoteObservable");
        mBridge.observe(MyService.COLOR_OBSERVABLE_KEY)
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
        unbindService(mBridge);
        super.onDestroy();
    }
}
