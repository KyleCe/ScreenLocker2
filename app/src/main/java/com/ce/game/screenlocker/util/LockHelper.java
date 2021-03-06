package com.ce.game.screenlocker.util;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;

import com.ce.game.screenlocker.view.LockView;
import com.ce.game.screenlocker.view.PinCodeView;
import com.ce.game.screenlocker.view.SwipeWithAnimListener;


/**
 * Created by KyleCe on 2016/6/1.
 *
 * @author: KyleCe
 */
public enum LockHelper implements SwipeEvent {
    INSTANCE;

    private static Context mContext;

    private final int UNLOCK = 830;
    private final int UNLOCK_WITH_PASSWORD = 831;
    private final int SWITCH_TO_GUEST = 345;

    public static final String INIT_VIEW_FILTER = "init view";
    public static final String START_SUPERVISE = "start supervise";
    public static final String STOP_SUPERVISE = "stop supervise";
    public static final String SHOW_SCREEN_LOCKER = "show screen locker";

    private static LockView mLockView;
    private static LockLayer mLockLayer;

    public void initialize(Context context) {
        initContextViewAndLayer(context);

        loadLockView(context);
    }


    /**
     * @throws NullPointerException if not init
     */
    public static LockView getLockView() {
        if (mLockView == null)
            throw new NullPointerException("init first");
        return mLockView;
    }

    /**
     * @throws NullPointerException if not init
     */
    public static LockLayer getLockLayer() {
        if (mLockLayer == null)
            throw new NullPointerException("init first");

        return mLockLayer;
    }

    /**
     * @throws NullPointerException if context == null
     */
    public void initLockViewInBackground(final Context context) {
        if (context == null)
            throw new NullPointerException("context == null, assign first");

        if (mLockView == null || mLockLayer == null)
            initContextViewAndLayer(context);
    }

    public void initContextViewAndLayer(Context context) {
        if (mContext == null)
            synchronized (this) {
                if (mContext == null)
                    mContext = context;
            }

        //init layout view
        if (mLockView == null)
            synchronized (this) {
                if (mLockView == null)
                    mLockView = new LockView(context);
            }

        //init lock layer
        if (mLockLayer == null)
            synchronized (this) {
                if (mLockLayer == null)
                    mLockLayer = LockLayer.getInstance(context, mLockView);
            }
    }

    private volatile boolean mIsInitialized = false;

    public void loadLockView(Context context) {
        mLockView.showLockHome();

        if( !mIsInitialized){
            mLockView.assignSwipeEvent(this);

            mLockView.assignDirectionOperator(new SwipeWithAnimListener.DirectionOperator() {
                @Override
                public void up() {
                }

                @Override
                public void down() {

                }

                @Override
                public void left() {
                    if(!mLockView.leftSlidable()) return;

                    mHandler.sendEmptyMessage(UNLOCK);
                }

                @Override
                public void right() {
                    if(!mLockView.rightSlidable()) return;

                    mHandler.sendEmptyMessage(UNLOCK);
                }
            });

            mLockView.assignPinCodeRuler(new PinCodeView.UnlockInterface() {
                @Override
                public void onUnlock(String password) {

                    Message msg = new Message();
                    msg.what = UNLOCK_WITH_PASSWORD;
                    msg.obj = password;
                    mHandler.sendMessage(msg);
                }

                @Override
                public void onBack() {
                    mLockView.switchBackToCenterFromBottom();
                }
            });

            mIsInitialized = true;
        }

        mLockLayer.lock();
        showLockLayer();
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case UNLOCK:
                    DU.sd("handler", "unlock");

                    unlock();
                    break;

                case UNLOCK_WITH_PASSWORD:

                    if (!(msg.obj instanceof String)) break;
                    String password = (String) msg.obj;
                    switchUserIfExistOrAlertUser(password);

                    break;

                default:
                    break;
            }
        }
    };

    private void unlock() {
        mLockLayer.unlock();
        mLockView.stopShimmer();

        mContext.sendBroadcast(new Intent(LockHelper.STOP_SUPERVISE));

        CameraHelper.refreshImageIfFileExist(mContext);

        mContext.sendBroadcast(new Intent(CoreIntent.ACTION_SCREEN_LOCKER_UNLOCK));
    }

    private void switchUserIfExistOrAlertUser(String password) {
        if (TextUtils.isEmpty(password)) {
            wrong();
            return;
        }

        if (!password.equals("1234")) {
            wrong();
            return;
        }

        unlockScreenAndResetPinCode();
    }
    private void unlockScreenAndResetPinCode() {
        unlock();
        mLockView.resetPinCodeView();
    }

    private void wrong() {

    }


    public static final String INTENT_KEY_WITH_SECURE = "with_secure";

    @Override
    public <S, T> void onSwipe(S s, T t) {


        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LockHelper.INSTANCE.getLockLayer().removeLockView();
            }
        }, 1000);

        triggerCameraWithSecure(mContext, !(t instanceof Boolean) || (Boolean) t);
    }

    private void triggerCameraWithSecure(Context context, boolean withSecure) {
        if (!CameraHelper.hasCameraHardware(context)) return;

        try {
            CameraHelper.cameraStrategy(context, withSecure);
        } catch (Exception e) {
            // may cannot open
            e.printStackTrace();
            showLockLayer();
        }

        context.sendBroadcast(new Intent(LockHelper.START_SUPERVISE));
    }


    private void showLockLayer() {

        mLockView.showLockHome();
        mLockLayer.bringBackLockView();
    }

    public void vibrate(long milliseconds) {
        if (mContext == null) return;
        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(milliseconds == 0 ? 500 : milliseconds);
    }

}
