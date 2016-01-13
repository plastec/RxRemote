package com.example.ypavshl.lib;

import android.content.ComponentName;

/**
 * Created by ypavshl on 12.1.16.
 */
public interface OnRxRemoteRegistrationListener {
    void onRemoteRegistered(IRxRemote remote, ComponentName component);
    void onRemoteUnregistered(IRxRemote remote, ComponentName component);
}
