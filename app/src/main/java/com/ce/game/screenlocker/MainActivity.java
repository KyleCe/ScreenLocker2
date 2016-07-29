package com.ce.game.screenlocker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.ce.game.screenlocker.util.DU;
import com.ce.game.screenlocker.util.LockAffairs;

public class MainActivity extends Activity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        findViewById(R.id.start_locker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        final Button button = (Button) findViewById(R.id.set_password);

        mCountDownTimer = new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                button.setText("remaining " + millisUntilFinished / 1000);
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                button.setText("done!");
            }

        }.start();

        mConatainerXY = (ImageView) findViewById(R.id.pic_container_xy);
        mConatainerCenter = (ImageView) findViewById(R.id.pic_container_center);
        mConatainerCrop = (ImageView) findViewById(R.id.pic_container_center_crop);
        mConatainerCenterInside = (ImageView) findViewById(R.id.pic_container_center_inside);
        mConatainerFitCenter = (ImageView) findViewById(R.id.pic_container_fit_center);

        requestPermissionOrStartServiceDirectly();

        DU.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);

                LockAffairs affairs = new LockAffairs();

                Bitmap toSetBg = affairs.parseCustomBackground(mContext);

                if (toSetBg == null)
                    toSetBg = BitmapFactory.decodeStream(
                            mContext.getResources().openRawResource(+R.drawable.general_bg));

                sDefaultBG = new BitmapDrawable(toSetBg);

                sBlurredBG = affairs.generateBlurredDrawable(mContext, toSetBg);

                Message msg = new Message();
                msg.obj = sDefaultBG;
                mHandler.sendMessage(msg);
            }
        });
    }

    private Drawable sDefaultBG;
    private Drawable sBlurredBG;
    private ImageView mConatainerCenter ;
    private ImageView mConatainerCrop ;
    private ImageView mConatainerCenterInside;
    private ImageView mConatainerFitCenter ;
    private ImageView mConatainerXY;

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Object obj = msg.obj;
            if(obj instanceof Drawable){
                Drawable drawable = (Drawable) msg.obj;
                mConatainerXY.setImageDrawable(drawable);
                mConatainerCenter .setImageDrawable(drawable);
                mConatainerCrop .setImageDrawable(drawable);
                mConatainerCenterInside.setImageDrawable(drawable);
                mConatainerFitCenter .setImageDrawable(drawable);
            }
        }
    };

    private void requestPermissionOrStartServiceDirectly() {
        if (Build.VERSION.SDK_INT >= 23 && noReadWriteExternalStorage())
            ActivityCompat.requestPermissions(MainActivity.this
                    , PERMISSION_REQUEST_STRINGS, REQUEST_PERMISSION);
        else startScreenLockService();
    }

    private boolean noReadWriteExternalStorage() {
        return isPermissionNotGranted(mContext, READ_PERMISSION) ||
                isPermissionNotGranted(mContext, WRITE_PERMISSION);
    }

    private boolean isPermissionNotGranted(Context c, final String p) {
        return ActivityCompat.checkSelfPermission(c, p) != PackageManager.PERMISSION_GRANTED;
    }

    CountDownTimer mCountDownTimer;
    private final int REQUEST_PERMISSION = 268;
    private final String READ_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String WRITE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String[] PERMISSION_REQUEST_STRINGS = new String[]{READ_PERMISSION, WRITE_PERMISSION};


    private void startScreenLockService() {
        Intent intent = new Intent(mContext, LockScreenService.class);
        intent.setAction(LockScreenService.class.getPackage().getName() + LockScreenService.class.getSimpleName());
        mContext.startService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        startScreenLockService();
    }
}
