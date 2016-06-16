package com.ce.game.screenlocker;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;

import com.ce.game.screenlocker.common.DU;
import com.ce.game.screenlocker.util.LockHelper;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by KyleCe on 2016/5/25.
 *
 * @author: KyleCe
 */

final public class LockBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = LockBroadcastReceiver.class.getSimpleName();

    private volatile boolean bInterruptSupervisor = false;

    private ScheduledThreadPoolExecutor mExecutor;
    private FutureRunnable mSupervisorRunnable;

    private ScheduledFuture<?> mBatteryCheckFuture;

    @Override
    public void onReceive(Context context, Intent intent) {
        String mAction = intent.getAction();
        DU.sd("broadcast -----The Intent Action is: ", "" + mAction);

        switch (mAction) {
            case LockHelper.INIT_VIEW_FILTER:
                LockHelper.INSTANCE.initLockViewInBackground(context);
                break;
            case Intent.ACTION_SCREEN_ON:
                refreshBatteryInfo();
                break;
            case LockHelper.START_SUPERVISE:
                bInterruptSupervisor = false;
                supervise(context.getApplicationContext());
                break;
            case LockHelper.STOP_SUPERVISE:
                bInterruptSupervisor = true;
                break;
            case LockHelper.SHOW_SCREEN_LOCKER:
                DU.sd("broadcast", "locker received");
            case Intent.ACTION_SCREEN_OFF:
                LockHelper.INSTANCE.initialize(context);
                LockHelper.INSTANCE.getLockLayer().lock();
                bInterruptSupervisor = true;
                break;
            case Intent.ACTION_POWER_CONNECTED:
                LockHelper.INSTANCE.getLockView().batteryChargingAnim();
                break;
            case Intent.ACTION_POWER_DISCONNECTED:
                LockHelper.INSTANCE.getLockView().batteryChargingAnim();
                break;
            case Intent.ACTION_SHUTDOWN:
                break;
            default:
                break;
        }
    }

    abstract class FutureRunnable implements Runnable {

        private Future<?> future;

        public Future<?> getFuture() {
            return future;
        }

        public void setFuture(Future<?> future) {
            this.future = future;
        }
    }

    public void supervise(final Context context) {
        DU.sd("service", "supervise");

        initScheduleExecutor();

        if (mSupervisorRunnable == null)
            mSupervisorRunnable = new FutureRunnable() {
                public void run() {
                    if (bInterruptSupervisor)
                        getFuture().cancel(true);


                    boolean cameraRunning = false;
                    Camera _camera = null;
                    try {
                        _camera = Camera.open();
                        cameraRunning = _camera == null;
                    } catch (Exception e) {
                        // fail to open camera, secure to ignore exception
                        DU.sd("camera exception on supervise");
                        cameraRunning = true;
                    } finally {
                        if (_camera != null) {
                            _camera.release();
                            getFuture().cancel(true);
                            context.sendBroadcast(new Intent(LockHelper.SHOW_SCREEN_LOCKER));
                        }
                    }

                    if (!cameraRunning)
                        context.sendBroadcast(new Intent(LockHelper.SHOW_SCREEN_LOCKER));
                }

            };
        Future<?> future = mExecutor.scheduleAtFixedRate(mSupervisorRunnable, 2000, 500, TimeUnit.MILLISECONDS);
        mSupervisorRunnable.setFuture(future);
    }

    private void refreshBatteryInfo() {
        initScheduleExecutor();
        mBatteryCheckFuture = mExecutor.scheduleAtFixedRate(new BatteryCheckRunnable(), 2, 2, TimeUnit.MINUTES);
    }

    private class BatteryCheckRunnable implements Runnable {
        public void run() {
            LockHelper.INSTANCE.getLockView().refreshBattery();

            if (!LockHelper.INSTANCE.getLockLayer().isbIsLocked())
                mBatteryCheckFuture.cancel(false);
        }
    }

    private void initScheduleExecutor() {
        if (mExecutor == null)
            synchronized (this) {
                if (mExecutor == null)
                    mExecutor = new ScheduledThreadPoolExecutor(2);
            }
    }

    public void shutdownScheduleExecutor() {
        if (mExecutor != null)
            mExecutor.shutdown();
    }

    private String topActivityPackageName(Context context) {
        ComponentName info = getComponentName(context);
        return info == null ? "" : info.getPackageName();
    }

    private ComponentName getComponentName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        if (taskInfo == null || taskInfo.size() == 0) return null;

        ComponentName componentInfo = taskInfo.get(0).topActivity;

        // main launcher::: com.blablaapp.launcher/com.blablaapp.launcher.activity.LauncherActivity

        DU.sd("componentInfo", componentInfo, "package::::" + componentInfo.getPackageName());
        return componentInfo;
    }
}
