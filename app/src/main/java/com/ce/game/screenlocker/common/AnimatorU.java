package com.ce.game.screenlocker.common;

import android.os.SystemClock;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;


/**
 * Created by KyleCe on 2015/9/24.
 */
public class AnimatorU {

    private static final long ANIM_DURATION = 500;// milliseconds

    /**
     * 从控件所在位置移动到控件的底部
     *
     * @return translate animation
     */
    public static TranslateAnimation moveToViewBottomAnim(long duration) {
        TranslateAnimation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        anim.setDuration(duration);
        return anim;
    }

    /**
     * 从控件的底部移动到控件所在位置
     *
     * @return
     */
    public static TranslateAnimation moveToViewLocationFromBottomAnim(long duration) {
        TranslateAnimation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        anim.setDuration(duration);
        return anim;
    }

    /**
     * 从控件的顶部移动到控件所在位置
     *
     * @return
     */
    public static TranslateAnimation moveToViewLocationFromTopAnim(long duration) {
        TranslateAnimation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        anim.setDuration(duration);
        return anim;
    }

    public static void hideToBottom(View view, long duration) {
        if (view == null) return;
        view.setVisibility(View.GONE);
        view.setAnimation(AnimatorU.moveToViewBottomAnim(duration));
    }

    /**
     * @see #moveToLocation(View, long)
     */
    public static void moveToLocation(View view) {
        moveToLocation(view, ANIM_DURATION);
    }

    /**
     * 从控件的底部移动到控件所在位置
     */
    public static void moveToLocation(View view, long duration) {
        if (view == null) return;
        view.setVisibility(View.VISIBLE);
        view.setAnimation(moveToViewLocationFromBottomAnim(duration));
    }

    /**
     * @see #moveToLocationFromViewTop(View, long)
     */
    public static void moveToLocationFromViewTop(View view) {
        moveToLocationFromViewTop(view, ANIM_DURATION);
    }

    /**
     * 从控件的顶部移动到控件所在位置
     */
    public static void moveToLocationFromViewTop(View view, long duration) {
        if (view == null) return;
        view.setVisibility(View.VISIBLE);
        view.setAnimation(moveToViewLocationFromTopAnim(duration));
    }

    /**
     * get the scale from view center for duration
     *
     * @param duration duration for anim
     * @return the scale anim
     */
    private static ScaleAnimation scaleToLocationAnimFromCenter(long duration) {
        ScaleAnimation anim = new ScaleAnimation(1, 1, /* from x to x*/
                .5f, 1,/*from y to y*/
                Animation.RELATIVE_TO_SELF, 1, /*pivotXType,pivotXValue */
                Animation.RELATIVE_TO_PARENT, 0.5f /*pivotYType,pivotYValue */);
        anim.setDuration(duration);
        return anim;
    }


    /**
     * @see #scaleToLocationFromCenter(View, long)
     */
    public static void scaleToLocationFromCenter(View view) {
        scaleToLocationFromCenter(view, ANIM_DURATION);
    }

    /**
     * scale to location from view self center
     *
     * @param view     view to scale
     * @param duration duration for anim unit: milliseconds, 1second is good for a whole screen view
     */
    public static void scaleToLocationFromCenter(View view, long duration) {
        if (view == null) return;
        view.setVisibility(View.VISIBLE);
        view.startAnimation(scaleToLocationAnimFromCenter(duration));
        DU.sd("anim log", SystemClock.elapsedRealtime());
    }

    /**
     * get the alpha anim
     *
     * @param duration duration for anim
     * @param from     from alpha
     * @param to       to alpha
     * @return the alpha anim
     */
    private static AlphaAnimation alphaAnim(float from, float to, long duration) {
        AlphaAnimation anim = new AlphaAnimation(from, to);
        anim.setDuration(duration);
        return anim;
    }


    /**
     * @see #alphaOut(View, long)
     */
    public static void alphaOut(View view) {
        alphaOut(view, ANIM_DURATION);
    }

    /**
     * alpha out view
     *
     * @param view     view to scale
     * @param duration duration for anim unit: milliseconds
     */
    public static void alphaOut(View view, long duration) {
        alphaOut(view, duration, null);
    }

    /**
     * alpha out view
     *
     * @param view     view to scale
     * @param duration duration for anim unit: milliseconds
     * @param listener anim listener to set
     */
    public static void alphaOut(View view, long duration, Animation.AnimationListener listener) {
        if (view == null) return;
        view.setVisibility(View.VISIBLE);
        AlphaAnimation anim = alphaAnim(1, 0, duration);
        if (listener != null) anim.setAnimationListener(listener);

        // should use the start instead of set fun, otherwise the listener may not take effect
        view.startAnimation(anim);
    }

    /**
     * @see #alphaIn(View, long)
     */
    public static void alphaIn(View view) {
        alphaIn(view, ANIM_DURATION);
    }

    /**
     * alpha out view
     *
     * @param view     view to scale
     * @param duration duration for anim unit: milliseconds
     */
    public static void alphaIn(View view, long duration) {
        if (view == null) return;
        view.setVisibility(View.VISIBLE);
        view.setAnimation(alphaAnim(0, 1, duration));
    }


    /**
     * @see #moveFromTo(float, float, float, float, long)
     */
    public static TranslateAnimation moveFromTo(float fromX, float toX, float fromY, float toY) {
        return moveFromTo(fromX, toX, fromY, toY, ANIM_DURATION);
    }

    /**
     * @return
     */
    private static TranslateAnimation moveFromTo(float fromX, float toX, float fromY, float toY, long duration) {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, fromX,
                Animation.RELATIVE_TO_SELF, toX, Animation.RELATIVE_TO_SELF,
                fromY, Animation.RELATIVE_TO_SELF, toY);
        mHiddenAction.setDuration(duration);
        return mHiddenAction;
    }

    public static void hideFromLeftToRight(View view) {
        if (view == null) return;
        view.setAnimation(moveFromTo(0, 1, 0, 0));
        view.setVisibility(View.INVISIBLE);
    }

    /**
     * @see #showFromRightToLeft(View, Animation.AnimationListener)
     */
    public static void showFromRightToLeft(View view) {
        showFromRightToLeft(view, null);
    }

    public static void showFromRightToLeft(View view, Animation.AnimationListener listener) {
        showFromRightToLeft(view, 300, listener);
    }

    /**
     * show from screen right to left
     *
     * @param view     view to show
     * @param dura     duration
     * @param listener animator listener to attach
     */
    public static void showFromRightToLeft(View view, long dura, Animation.AnimationListener listener) {
        if (view == null) return;
        view.setVisibility(View.VISIBLE);
        TranslateAnimation tran = moveFromTo(1, 0, 0, 0);
        tran.setDuration(dura);
        if (listener != null) tran.setAnimationListener(listener);

        view.startAnimation(tran);
    }

    public static void hideFromRightToLeft(View view) {
        if (view == null) return;
        view.setAnimation(moveFromTo(0, -1, 0, 0));
        view.setVisibility(View.INVISIBLE);
    }

    //    new on 2016-5-24 18:24:54
    private static final int DEFAULT_DURA = 426;

    /*
    * */
    public static void showFromTopToCenter(View view) {
        showFromTopToCenter(view, DEFAULT_DURA);
    }

    public static void showFromTopToCenter(View view, long dura) {
        showFromTopToCenter(view, dura, null);
    }

    public static void showFromTopToCenter(View view, long dura, Animation.AnimationListener listener) {
        showViewWithAnim(view, moveFromTo(0, 0, 1, 0), dura, listener);
    }

    /*
    * */
    public static void showFromBottomToCenter(View view) {
        showFromBottomToCenter(view, DEFAULT_DURA);
    }

    public static void showFromBottomToCenter(View view, long dura) {
        showFromBottomToCenter(view, dura, null);
    }

    public static void showFromBottomToCenter(View view, long dura, Animation.AnimationListener listener) {
        showViewWithAnim(view, moveFromTo(0, 0, 1, 0), dura, listener);
    }

    /*
    * */
    public static void showFromLeftToCenter(View view) {
        showFromLeftToCenter(view, DEFAULT_DURA);
    }

    public static void showFromLeftToCenter(View view, long dura) {
        showFromLeftToCenter(view, dura, null);
    }

    public static void showFromLeftToCenter(View view, long dura, Animation.AnimationListener listener) {
        showViewWithAnim(view, moveFromTo(0, 0, 1, 0), dura, listener);
    }

    /*
    * */
    public static void showFromRightToCenter(View view) {
        showFromRightToCenter(view, DEFAULT_DURA);
    }

    public static void showFromRightToCenter(View view, long dura) {
        showFromRightToCenter(view, dura, null);
    }

    public static void showFromRightToCenter(View view, long dura, Animation.AnimationListener listener) {
        showViewWithAnim(view, moveFromTo(0, 0, 1, 0), dura, listener);
    }

    /*
    * */
    public static void showViewWithAnim(final View view, TranslateAnimation tran) {
        showViewWithAnim(view, tran, DEFAULT_DURA);
    }

    public static void showViewWithAnim(final View view, TranslateAnimation tran, long dura) {
        showViewWithAnim(view, tran, dura, null);
    }

    public static void showViewWithAnim(final View view, TranslateAnimation tran, long dura, final Animation.AnimationListener listener) {
        if (view == null) return;
        view.setVisibility(View.VISIBLE);
        tran.setDuration(dura);
        if (listener != null)
            tran.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    view.setVisibility(View.VISIBLE);
                    listener.onAnimationStart(animation);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.setVisibility(View.VISIBLE);
                    listener.onAnimationEnd(animation);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    listener.onAnimationRepeat(animation);
                }
            });

        view.startAnimation(tran);
    }


    /*
    * */
    public static void hideFromCenterToTop(final View view) {
        hideFromCenterToTop(view, DEFAULT_DURA);
    }

    public static void hideFromCenterToTop(final View view, long dura) {
        hideFromCenterToTop(view, dura, null);
    }


    public static void hideFromCenterToTop(final View view, long dura, final Animation.AnimationListener listener) {
        hideViewWithAnim(view, moveFromTo(0, 0, 0, 1), dura, listener);
    }

    /*
    * */
    public static void hideFromCenterToBottom(final View view) {
        hideFromCenterToBottom(view, DEFAULT_DURA);
    }

    public static void hideFromCenterToBottom(final View view, long dura) {
        hideFromCenterToBottom(view, dura, null);
    }


    public static void hideFromCenterToBottom(final View view, long dura, final Animation.AnimationListener listener) {
        hideViewWithAnim(view, moveFromTo(0, 0, 0, 1), dura, listener);
    }

    /*
    * */
    public static void hideFromCenterToLeft(final View view) {
        hideFromCenterToLeft(view, DEFAULT_DURA);
    }

    public static void hideFromCenterToLeft(final View view, long dura) {
        hideFromCenterToLeft(view, dura, null);
    }


    public static void hideFromCenterToLeft(final View view, long dura, final Animation.AnimationListener listener) {
        hideViewWithAnim(view, moveFromTo(0, -1, 0, 0), dura, listener);
    }

    /*
    * */
    public static void hideFromCenterToRight(final View view) {
        hideFromCenterToRight(view, DEFAULT_DURA);
    }

    public static void hideFromCenterToRight(final View view, long dura) {
        hideFromCenterToRight(view, dura, null);
    }

    public static void hideFromCenterToRight(final View view, long dura, final Animation.AnimationListener listener) {
        hideViewWithAnim(view, moveFromTo(0, 1, 0, 0), dura, listener);
    }

    /**/
    public static void hideViewWithAnim(final View view, TranslateAnimation tran) {
        hideViewWithAnim(view, tran, DEFAULT_DURA);
    }

    public static void hideViewWithAnim(final View view, TranslateAnimation tran, long dura) {
        hideViewWithAnim(view, tran, dura, null);
    }

    public static void hideViewWithAnim(final View view, TranslateAnimation tran, long dura, final Animation.AnimationListener listener) {
        if (view == null) return;
        view.setVisibility(View.VISIBLE);
        tran.setDuration(dura);
        if (listener != null)
            tran.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    view.setVisibility(View.VISIBLE);
                    listener.onAnimationStart(animation);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.setVisibility(View.INVISIBLE);
                    listener.onAnimationEnd(animation);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    listener.onAnimationRepeat(animation);
                }
            });

        view.startAnimation(tran);
    }


}
