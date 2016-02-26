package ru.yandex.music.rxremote;

import android.content.ComponentName;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.Set;

/**
 * Created by ypavshl on 29.1.16.
 */
class Remotes {

    PairHashMap<IRxRemote, ComponentName> mMap = new PairHashMap<>();

    void put(@NonNull IRxRemote bridge,
             @NonNull ComponentName name) {
        mMap.put(bridge, name);
    }

    @Nullable
    IRxRemote get(@NonNull ComponentName name) {
        return mMap.get(name).first;
    }

    @Nullable
    ComponentName get(@NonNull IRxRemote bridge) {
        return mMap.get(bridge).second;
    }

    boolean contains(@NonNull IRxRemote bridge) {
        return mMap.contains(bridge);
    }

    boolean contains(@NonNull ComponentName name) {
        return mMap.contains(name);
    }

    @Nullable
    IRxRemote remove(ComponentName name) {
        Pair<IRxRemote, ComponentName> pair = mMap.remove(name);
        return pair != null ? pair.first : null;
    }

    @Nullable
    ComponentName remove(@NonNull IRxRemote bridge) {
        Pair<IRxRemote, ComponentName> pair = mMap.remove(bridge);
        return pair != null ? pair.second : null;
    }

    @NonNull
    Set<IRxRemote> all() {
        return mMap.firsts();
    }
}
