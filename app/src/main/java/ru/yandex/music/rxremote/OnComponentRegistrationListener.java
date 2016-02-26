package ru.yandex.music.rxremote;

import android.content.ComponentName;

/**
 * Created by ypavshl on 12.1.16.
 */
public interface OnComponentRegistrationListener {
    void onComponentRegistered(ComponentName component);
    void onComponentUnregistered(ComponentName component);
}
