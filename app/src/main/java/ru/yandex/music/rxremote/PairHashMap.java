package ru.yandex.music.rxremote;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

class PairHashMap<F,S> {

    private HashMap<Object, Pair<F, S>> mValues = new HashMap<>();
    private HashSet<F> mFirsts = new HashSet<>();
    private HashSet<S> mSeconds = new HashSet<>();

    @Nullable
    Pair<F,S> put(F f, S s) {
        Pair<F,S> current = new Pair<>(f, s);
        mValues.put(f, current);
        mValues.put(s, current);
        mFirsts.add(f);
        mSeconds.add(s);
        return mValues.put(s, current);
    }

    @Nullable
    Pair<F,S> get(Object o) {
        return mValues.get(o);
    }

    boolean contains(Object o) {
        return mValues.containsKey(o);
    }

    @Nullable
    Pair<F,S> remove(Object o) {
        Pair<F, S> p = mValues.remove(o);
        if (p != null) {
            mValues.remove(p.second);
            mFirsts.remove(o);
            mSeconds.remove(p.second);
        }

        return p;
    }

    @NonNull
    Set<F> firsts() {
        return mFirsts;
    }


    @NonNull
    Set<S> seconds() {
        return mSeconds;
    }
}
