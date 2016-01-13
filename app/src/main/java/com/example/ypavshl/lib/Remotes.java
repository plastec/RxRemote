package com.example.ypavshl.lib;

import android.content.ComponentName;

import java.util.HashMap;

public class Remotes {
    HashMap mMap = new HashMap();

    public void put(IRxRemote binder, ComponentName name) {
        mMap.put(binder, name);
        mMap.put(name, binder);
    }

    public IRxRemote get(ComponentName name) {
        return (IRxRemote) mMap.get(name);
    }

    public ComponentName get(IRxRemote binder) {
        return (ComponentName) mMap.get(binder);
    }

    public boolean contains(IRxRemote binder) {
        return mMap.containsKey(binder);
    }

    public boolean contains(ComponentName name) {
        return mMap.containsKey(name);
    }

    public IRxRemote remove(ComponentName name) {
        IRxRemote binder = (IRxRemote)mMap.remove(name);
        if (binder != null)
            mMap.remove(binder);

        return binder;
    }

    public ComponentName remove(IRxRemote binder) {
        ComponentName name = (ComponentName) mMap.remove(binder);
        if (binder != null)
            mMap.remove(name);

        return name;
    }
}
