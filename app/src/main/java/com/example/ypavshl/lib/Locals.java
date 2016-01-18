package com.example.ypavshl.lib;

import android.content.ComponentName;

import java.util.HashMap;

/**
 * Created by ypavshl on 18.1.16.
 */
public class Locals {
    HashMap mMap = new HashMap();

    public void put(RxBridgeAidl bridge, ComponentName name) {
        mMap.put(bridge, name);
        mMap.put(name, bridge);
    }

    public RxBridgeAidl get(ComponentName name) {
        return (RxBridgeAidl) mMap.get(name);
    }

    public ComponentName get(RxBridgeAidl bridge) {
        return (ComponentName) mMap.get(bridge);
    }

    public boolean contains(RxBridgeAidl bridge) {
        return mMap.containsKey(bridge);
    }

    public boolean contains(ComponentName name) {
        return mMap.containsKey(name);
    }

    public RxBridgeAidl remove(ComponentName name) {
        RxBridgeAidl bridge = (RxBridgeAidl)mMap.remove(name);
        if (bridge != null)
            mMap.remove(bridge);

        return bridge;
    }

    public ComponentName remove(RxBridgeAidl bridge) {
        ComponentName name = (ComponentName) mMap.remove(bridge);
        if (bridge != null)
            mMap.remove(name);

        return name;
    }
}
