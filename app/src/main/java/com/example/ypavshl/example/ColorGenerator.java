package com.example.ypavshl.example;

import java.util.Random;

/**
 * Created by ypavshl on 13.1.16.
 */
public class ColorGenerator extends Random {
    private static final ColorGenerator sInstance = new ColorGenerator();
    public static int generate() {
        int red = sInstance.next();
        int green = sInstance.next();
        int blue = sInstance.next();
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }
    public int next() {
        return next(1)*255;
    };
}
