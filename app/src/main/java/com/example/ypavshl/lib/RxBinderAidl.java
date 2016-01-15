package com.example.ypavshl.lib;

import android.content.Context;

/**
 * Created by ypavshl on 6.1.16.
 */
public class RxBinderAidl extends RxBridgeAidl {
    private static final String TAG = RxBinderAidl.class.getSimpleName();

    public RxBinderAidl(Context context) {
        super(context);
    }

    public RxBinderAidl(Context context, OnComponentRegistrationListener listener) {
        super(context, listener);
    }
}
