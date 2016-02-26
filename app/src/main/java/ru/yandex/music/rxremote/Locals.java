package ru.yandex.music.rxremote;

import android.content.ComponentName;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.Set;

/**
 * Created by ypavshl on 18.1.16.
 */
class Locals {

    private PairHashMap<RxBridgeAidl, ComponentName> mMap = new PairHashMap<>();

    void put(@NonNull RxBridgeAidl bridge,
             @NonNull ComponentName name) {
        mMap.put(bridge, name);
    }

    @Nullable
    RxBridgeAidl get(@NonNull ComponentName name) {
        return mMap.get(name).first;
    }

    @Nullable
    ComponentName get(@NonNull RxBridgeAidl bridge) {
        return mMap.get(bridge).second;
    }

    boolean contains(@NonNull RxBridgeAidl bridge) {
        return mMap.contains(bridge);
    }

    boolean contains(@NonNull ComponentName name) {
        return mMap.contains(name);
    }

    @Nullable
    RxBridgeAidl remove(@NonNull ComponentName name) {
        Pair<RxBridgeAidl, ComponentName> pair = mMap.remove(name);
        return pair != null ? pair.first : null;
    }

    @Nullable
    ComponentName remove(@NonNull RxBridgeAidl bridge) {
        Pair<RxBridgeAidl, ComponentName> pair = mMap.remove(bridge);
        return pair != null ? pair.second : null;
    }

    @NonNull
    Set<RxBridgeAidl> all() {
        return mMap.firsts();
    }
}
