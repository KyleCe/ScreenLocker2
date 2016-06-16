package com.ce.game.screenlocker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
    }

    private void startScreenLockService() {
        Intent intent = new Intent(mContext, LockScreenService.class);
        intent.setAction(LockScreenService.class.getPackage().getName() + LockScreenService.class.getSimpleName());
        mContext.startService(intent);
    }

}
