package com.ce.game.screenlocker.util;

import android.view.View;

/**
 * Created by KyleCe on 2016/5/13.
 *
 * @author: KyleCe
 */
final public class ScreenModeHelper {
    public static void requestImmersiveFullScreen(View view) {
        if(view == null) return;
        view.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    public static void unsetImmersiveFullScreen(View view) {
        if(view == null) return;
        view.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

}
