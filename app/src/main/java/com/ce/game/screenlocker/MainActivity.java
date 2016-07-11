package com.ce.game.screenlocker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;

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

        startScreenLockService();

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
    }

    CountDownTimer mCountDownTimer;

    private void startScreenLockService() {
        Intent intent = new Intent(mContext, LockScreenService.class);
        intent.setAction(LockScreenService.class.getPackage().getName() + LockScreenService.class.getSimpleName());
        mContext.startService(intent);
    }

}
