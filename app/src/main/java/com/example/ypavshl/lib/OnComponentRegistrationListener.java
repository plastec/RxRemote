package com.example.ypavshl.lib;

import android.content.ComponentName;

/**
 * Created by ypavshl on 12.1.16.
 */
public interface OnComponentRegistrationListener {
    void onComponentRegistered(ComponentName component);
    void onComponentUnregistered(ComponentName component);
}
