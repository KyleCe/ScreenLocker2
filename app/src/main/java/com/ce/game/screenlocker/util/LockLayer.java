package com.ce.game.screenlocker.util;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.ce.game.screenlocker.common.DU;


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


        mLockViewLayoutParams = new LayoutParams();
        mLockViewLayoutParams.width = LayoutParams.MATCH_PARENT;
        mLockViewLayoutParams.height = LayoutParams.MATCH_PARENT;
        //实现关键
        mLockViewLayoutParams.type = LayoutParams.TYPE_SYSTEM_ERROR;

        // 此行代码有时在主界面键按下情况下会出现无法显示和退出，暂时去掉，去掉之后按下主界面键会直接返回主界面

        mLockViewLayoutParams.flags = LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | LayoutParams.FLAG_DISMISS_KEYGUARD
                | LayoutParams.FLAG_KEEP_SCREEN_ON;

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
            removeViewAndExitFullscreen();
        }
        bIsLocked = false;
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

    private void removeViewAndExitFullscreen() {
        mWindowManager.removeView(mLockView);
        ScreenModeHelper.unsetImmersiveFullScreen(mLockView);
    }

    public synchronized void removeLockView() {
        if (mWindowManager == null || mLockView == null) return;

        removeViewAndExitFullscreen();
    }

    public synchronized void setLockView(View v) {
        mLockView = v;
    }

    public boolean isbIsLocked() {
        return bIsLocked ? true : false;
    }
}
