package com.ce.game.screenlocker.common;

import android.os.Build;

import java.lang.reflect.Method;

/**
 * Created by KyleCe on 2016/6/16.
 *
 * @author: KyleCe
 */
public class PhoneOemHelper {

    public static boolean notFlyMe() {
        try {
            final Method method = Build.class.getMethod("hasSmartBar");
            return method == null;
        } catch (final Exception e) {
            return true;
        }
    }
}
