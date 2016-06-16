package com.ce.game.screenlocker.util;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.ce.game.screenlocker.common.DU;
import com.ce.game.screenlocker.common.PhoneOemHelper;


/**
 * Created by KyleCe on 2016/5/25.
 *
 * @author: KyleCe
 */

final public class LockLayer {
    private WindowManager mWindowManager;
    private View mLockView;
    private LayoutParams mLockViewLayoutParams;
    private static LockLayer mLockLayer;
    private volatile boolean bIsLocked;
    private Context mContext;

    public static LockLayer getInstance(Context ctx, View view) {
        if (ctx == null || view == null) throw new NullPointerException("Nonnull");
        if (mLockLayer == null)
            synchronized (ctx) {
                if (mLockLayer == null) {
                    mLockLayer = new LockLayer(ctx, view);
                }
            }
        return mLockLayer;
    }

    private LockLayer(Context ctx, View v) {
        mContext = ctx;
        mLockView = v;
        init();
    }

    private void init() {
        bIsLocked = false;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        int type = 0;
        if (PhoneOemHelper.notFlyMe()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                type = LayoutParams.TYPE_SYSTEM_ERROR;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                type = LayoutParams.TYPE_TOAST;
            } else {
                type = LayoutParams.TYPE_PHONE;
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                type = LayoutParams.TYPE_TOAST;
            } else {
                type = LayoutParams.TYPE_SYSTEM_ALERT;
            }
        }

        mLockViewLayoutParams = new LayoutParams();
        mLockViewLayoutParams.width = LayoutParams.MATCH_PARENT;
        mLockViewLayoutParams.height = LayoutParams.MATCH_PARENT;
        mLockViewLayoutParams.type = type;

        mLockViewLayoutParams.flags = LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | LayoutParams.FLAG_DISMISS_KEYGUARD
                | LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | LayoutParams.FLAG_KEEP_SCREEN_ON;
        mLockViewLayoutParams.format = PixelFormat.TRANSLUCENT;

        mLockViewLayoutParams.gravity = Gravity.TOP;

        mLockViewLayoutParams.alpha = 1f;
        mLockViewLayoutParams.screenOrientation = 1;
    }

    public synchronized void lock() {
        if (mLockView != null && !bIsLocked) {
            removeIfAttachedAlready();

            addViewAndSetFullscreen();
        }
        bIsLocked = true;
    }

    public synchronized void unlock() {
        if (mWindowManager != null && bIsLocked) {
            bIsLocked = false;
            removeViewAndExitFullscreen();
        }
    }

    public synchronized void requestFullScreen() {
        ScreenModeHelper.requestImmersiveFullScreen(mLockView);
    }

    public synchronized void addLockView() {
        if (mLockView != null) {
            ScreenModeHelper.requestImmersiveFullScreen(mLockView);

            if (mLockView.getWindowToken() != null)
                mWindowManager.updateViewLayout(mLockView, mLockViewLayoutParams);
            else
                mWindowManager.addView(mLockView, mLockViewLayoutParams);
        }
        bIsLocked = true;
    }


    public synchronized void bringBackLockView() {
        if (mLockView == null) return;

        addViewAndSetFullscreen();
    }


    private void removeIfAttachedAlready() {
        if (mLockView.getWindowToken() != null || mLockView.getParent() != null) {
            DU.sd("window token", mLockView.getWindowToken());

            removeViewAndExitFullscreen();
        }
    }

    private void addViewAndSetFullscreen() {
        ScreenModeHelper.requestImmersiveFullScreen(mLockView);
        try {
            mWindowManager.addView(mLockView, mLockViewLayoutParams);
        } catch (Exception e) {
            // already attach to window, ignore exception
        }
    }

    private synchronized void removeViewAndExitFullscreen() {
        // order limited, unset must before remove, or problems occur
        ScreenModeHelper.unsetImmersiveFullScreen(mLockView);
        mWindowManager.removeView(mLockView);
    }

    public synchronized void removeLockView() {
        if (mWindowManager == null || mLockView == null) return;

        removeViewAndExitFullscreen();
    }

    public synchronized void setLockView(View v) {
        mLockView = v;
    }

    public boolean isLocked() {
        return bIsLocked ? true : false;
    }
}
