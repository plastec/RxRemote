package com.example.ypavshl.example;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.ypavshl.lib.RxServiceConnectionAidl;
import com.example.ypavshl.lib.parcel.RemoteKey;
import com.example.ypavshl.rxservice.R;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;


public class MyActivity extends AppCompatActivity {
    private static final String TAG = MyActivity.class.getSimpleName();

    public static final RemoteKey<ButtonItem> BUTTON_OBSERVABLE_KEY = new RemoteKey<>();

    private TextView mTextView;
    private FloatingActionButton mButton;

    private Observable<ColorItem> mRemoteObservable;
    private BehaviorSubject<ButtonItem> mButtonObservable = BehaviorSubject.create();
//    private RxServiceConnectionAidl mRxConnection;
    private RxServiceConnectionAidl mRxConnection = new RxServiceConnectionAidl(this) {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            super.onServiceConnected(className, service);
            mRemoteObservable = mRxConnection.bindObservable(MyService.COLOR_OBSERVABLE_KEY);
            mRemoteObservable
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
        Log.i(TAG + " RemoteRx", "onCreate");
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
        Log.i(TAG + " RemoteRx", "onResume");
        Intent intent = new Intent(this, MyService.class);
//        mRxConnection = new RxServiceConnectionAidl(this) {
//            @Override
//            public void onServiceConnected(ComponentName className, IBinder service) {
//                super.onServiceConnected(className, service);
//                mRemoteObservable = mRxConnection.bindObservable(MyService.COLOR_OBSERVABLE_KEY);
//                mRemoteObservable
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(item -> {
//                            mTextView.setText(item.getText());
//                            mTextView.setBackgroundColor(item.getColor());
//                        });
//            }
//        };

        mRxConnection.offerObservable(BUTTON_OBSERVABLE_KEY, mButtonObservable);
        bindService(intent, mRxConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        Log.i(TAG + " RemoteRx", "onPause");
        mRxConnection.unbindObservables();
        unbindService(mRxConnection);
        super.onPause();
    }
}
