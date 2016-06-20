package com.ce.game.screenlocker.util;

import android.os.Build;

import java.lang.reflect.Method;

/**
 * Created by KyleCe on 2016/6/16.
 *
 * @author: KyleCe
 */
public class AboutPhoneHelper {

    public static boolean notFlyMe() {
        try {
            final Method method = Build.class.getMethod("hasSmartBar");
            return method == null;
        } catch (final Exception e) {
            return true;
        }
    }

    public static boolean sdkNoLessThanM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
    public static boolean sdkNoLessThan19() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean sdkBelow18() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2;
    }
}
