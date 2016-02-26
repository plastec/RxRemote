package ru.yandex.music.rxremote;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;

import java.util.HashSet;

import ru.yandex.music.rxremote.parcel.RemoteKey;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

/**
 * Binding to local service returns original Observable
 * and binding to remote service returns a mirrored copy.
 * One copy per connection.
 *
 * Created by ypavshl on 20.1.16.
 */
public class OnConnect<T> extends RxServiceConnectionAidl
        implements Observable.OnSubscribe<Observable<T>>
{
    private static final String TAG = OnConnect.class.getSimpleName();

    private HashSet<Subscriber<? super Observable<T>>> mSubscribers = new HashSet<>();
    private RemoteKey<T> mKey;
    private Observable<T> mObservable;

    public OnConnect(Context context, RemoteKey<T> key) {
        super(context);
        mKey = key;
    }

    @Override
    public synchronized void onServiceConnected(ComponentName className, IBinder service) {
        super.onServiceConnected(className, service);
        mObservable = bindObservable(mKey);
        for (Subscriber<? super Observable<T>> s : mSubscribers)
            s.onNext(mObservable);
    }
    @Override
    public synchronized void onServiceDisconnected(ComponentName name) {
        super.onServiceDisconnected(name);
        for (Subscriber<? super Observable<T>> s : mSubscribers)
            s.onCompleted();
    }

    @Override
    public synchronized void call(Subscriber<? super Observable<T>> subscriber) {
        if (mObservable != null) {
            subscriber.onNext(mObservable);
        }
        mSubscribers.add(subscriber);
        subscriber.add(Subscriptions.create(() -> mSubscribers.remove(subscriber)));
    }
}
