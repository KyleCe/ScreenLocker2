package com.ce.game.screenlocker.common;

import android.view.View;

/**
 * Created by KyleCe on 2015/10/15.
 */
public class ViewU {
    public static void hide(View... views) {
        for (View v : views) if (v != null) v.setVisibility(View.GONE);
    }

    public static void invisible(View... views) {
        for (View v : views) if (v != null) v.setVisibility(View.INVISIBLE);
    }

    public static void show(View... views) {
        for (View v : views) if (v != null) v.setVisibility(View.VISIBLE);
    }

    public static void showAndHide(View sv, View hv) {
        show(sv);
        hide(hv);
    }

    public static void hideAndShow(View hv, View sv) {
        showAndHide(sv, hv);
    }

    public static void enClick(View... views) {
        for (View v : views) if (v != null) v.setClickable(true);
    }

    public static void disClick(View... views) {
        for (View v : views) if (v != null) v.setClickable(false);
    }

    public static void clearClickListener(View... views) {
        for (View v : views) if (v != null) v.setOnClickListener(null);
    }


    public static void setClickListener(View.OnClickListener listener, View... views) {
        for (View v : views) if (v != null) v.setOnClickListener(listener);
    }

    /**
     * set background color by color int
     *
     * @param views views you want to set background color
     * @param color the color that user want to set
     */
    public static void setBackgroundColor(int color, View... views) {
        for (View v : views) if (v != null) v.setBackgroundColor(color);
    }

    /**
     * set click listener for views
     *
     * @param listener the click listener to assign
     * @param views    views
     */
    public static void clickListen(View.OnClickListener listener, View... views) {
        for (View v : views) if (v != null && listener != null) v.setOnClickListener(listener);
    }
}
