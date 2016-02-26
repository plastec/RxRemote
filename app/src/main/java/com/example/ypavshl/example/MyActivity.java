package com.example.ypavshl.example;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.ypavshl.rxservice.R;

import ru.yandex.music.rxremote.RxServiceConnectionAidl;
import ru.yandex.music.rxremote.parcel.RemoteKey;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;


public class MyActivity extends AppCompatActivity {
    private static final String TAG = MyActivity.class.getSimpleName();

    public static final RemoteKey<ButtonItem> BUTTON_OBSERVABLE_KEY
            = new RemoteKey<>("BUTTON_OBSERVABLE", ButtonItem.class);

    private TextView mTextView;
    private FloatingActionButton mButton;

    private BehaviorSubject<ButtonItem> mButtonObservable = BehaviorSubject.create();

    private RxServiceConnectionAidl mRxConnection = new RxServiceConnectionAidl(this) {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            super.onServiceConnected(className, service);
            mRxConnection.bindObservable(MyService.COLOR_OBSERVABLE_KEY)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(item -> {
                    mTextView.setText(item.getText());
                    mTextView.setBackgroundColor(item.getColor());
            });
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
    protected void onResume() {
        super.onResume();
        mRxConnection.offerObservable(BUTTON_OBSERVABLE_KEY, mButtonObservable);
        Intent intent = new Intent(this, MyService.class);
        bindService(intent, mRxConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        mRxConnection.unbindObservables();
        mRxConnection.dismissObservables();
        unbindService(mRxConnection);
        super.onPause();
    }
}
