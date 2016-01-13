package com.example.ypavshl.lib;

import android.content.ComponentName;

/**
 * Created by ypavshl on 12.1.16.
 */
public interface OnRxRemoteRegistrationListener {
    void onRemoteRegirested(IRxRemote remote, ComponentName component);
    void onRemoteUnregirested(IRxRemote remote, ComponentName component);
}
