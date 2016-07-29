package com.ce.game.screenlocker;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;

import com.ce.game.screenlocker.inter.PhoneStateChange;
import com.ce.game.screenlocker.util.CoreIntent;
import com.ce.game.screenlocker.util.LockHelper;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by KyleCe on 2016/5/25.
 *
 * @author: KyleCe
 */

final public class LockScreenService extends Service implements PhoneStateChange {
    public static final String TAG = LockScreenService.class.getSimpleName();

    private LockBroadcastReceiver mReceiver = null;
    private Intent rebootIntent = null;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mIsPhonePickUpWhileLocked.set(false);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_SHUTDOWN);
        filter.addAction("android.intent.action.PHONE_STATE");
        filter.addAction(LockHelper.INIT_VIEW_FILTER);
        filter.addAction(LockHelper.SHOW_SCREEN_LOCKER);
        filter.addAction(LockHelper.START_SUPERVISE);
        filter.addAction(CoreIntent.ACTION_SCREEN_LOCKER_UNLOCK);

        mReceiver = new LockBroadcastReceiver();

        mReceiver.assignPhoneStateChangeCallback(this);

        registerReceiver(mReceiver, filter);
        sendBroadcast(new Intent(LockHelper.INIT_VIEW_FILTER));

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("KeyguardLock");
        keyguardLock.disableKeyguard();

        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        rebootIntent = intent;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mIsPhonePickUpWhileLocked.set(false);

        unregisterReceiver(mReceiver);

        if (rebootIntent != null) {
            startService(rebootIntent);
        }
        super.onDestroy();
    }

    private AtomicBoolean mIsPhonePickUpWhileLocked = new AtomicBoolean(false);

    @Override
    public void ringing() {
        if (LockHelper.INSTANCE.getLockLayer().isLocked())
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    LockHelper.INSTANCE.getLockLayer().unlock();
                    mIsPhonePickUpWhileLocked.set(true);
                }
            },800);
    }

    @Override
    public void offHook() {

    }

    @Override
    public void idle() {
        if (mIsPhonePickUpWhileLocked.get()){
            mIsPhonePickUpWhileLocked.set(false);
            LockHelper.INSTANCE.getLockLayer().lock();
        }
    }
}
