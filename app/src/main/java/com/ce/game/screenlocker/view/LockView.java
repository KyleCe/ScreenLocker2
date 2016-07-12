package com.ce.game.screenlocker.view;

import android.animation.Animator;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Process;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ce.game.screenlocker.R;
import com.ce.game.screenlocker.common.AnimatorU;
import com.ce.game.screenlocker.common.DU;
import com.ce.game.screenlocker.common.ViewU;
import com.ce.game.screenlocker.inter.DirectionSlidability;
import com.ce.game.screenlocker.util.BlurStrategy;
import com.ce.game.screenlocker.util.CameraHelper;
import com.ce.game.screenlocker.util.PhoneStateHelper;
import com.ce.game.screenlocker.util.SwipeEvent;


/**
 * Created by KyleCe on 2016/5/25.
 *
 * @author: KyleCe
 */
public class LockView extends FrameLayout implements DirectionSlidability {

    private volatile ShowingItem mShowingItem = ShowingItem.center;
    private static final int DEFAULT_ANIM_DURATION = 793;
    private GestureDetector mGestureDetector;

    private View mCenterItem;
    private View mTopItem;
    private PinCodeView mBottomItem;
    private View mLeftItem;
    private View mRightItem;

    private View nextView[][] = new View[5][4];

    private EssentialAnimFactors mFirstEAF = new EssentialAnimFactors();
    private EssentialAnimFactors mSecondEAF = new EssentialAnimFactors();

    float dX, dY;
    float oldX;
    float oldY;

    private SwipeWithAnimListener.DirectionOperator mDirectionOperator;

    private FrameLayout mWholeParent;

    private static Drawable sDefaultBG = null;
    private static Drawable sBlurredBG = null;

    private ImageView mCameraIcon;

    private View mBottomViewCover;

    private Context mContext;

    private SwipeEvent mSwipeEvent;

    private TextView mBatteryInfo;
    private ProgressBar mBatteryIndicator;
    private ImageView mBatteryCharging;
    private ImageView mUnlockIcon;

    public LockView(Context context) {
        this(context, null);
    }

    public LockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    public void assignSwipeEvent(SwipeEvent event) {
        mSwipeEvent = event;
    }

    public void assignDirectionOperator(SwipeWithAnimListener.DirectionOperator operator) {
        this.mDirectionOperator = operator;
    }

    public void assignPinCodeRuler(PinCodeView.UnlockInterface unlockInterface) {
        if (mBottomItem != null)
            mBottomItem.assignUnlockInterface(unlockInterface);
    }

    public void showLockHome() {
        ViewU.invisible(mTopItem, mBottomItem, mLeftItem, mRightItem);
        ViewU.show(mCenterItem);
        mShowingItem = ShowingItem.center;

        // bring center item back to center
        if (mCenterItem != null)
            mCenterItem.animate()
                    .x(oldX)
                    .y(oldY)
                    .setDuration(0)
                    .start();

        setBackground();

        resetPinCodeView();
        startShimmer();
        mUnlockIcon.startAnimation(mRollingAnim);
//        ViewU.hide(mBottomViewCover);
    }

    public void switchBackToCenterFromBottom() {
        if (mShowingItem != ShowingItem.bottom) return;

        animationDelegate(ShowingItem.bottom, OnSwipeListener.Direction.down);
    }

    @Override
    public boolean leftSlidable() {
        return !mShowingItem.equals(ShowingItem.bottom);
    }

    @Override
    public boolean rightSlidable() {
        return !mShowingItem.equals(ShowingItem.bottom);
    }

    @Override
    public boolean upSlidable() {
        return true;
    }

    @Override
    public boolean downSlidable() {
        return true;
    }

    enum ShowingItem {
        center(0), top(1), bottom(2), left(3), right(4);

        private int index;

        ShowingItem(int i) {
            this.index = i;
        }

        public int getIndex() {
            return index;
        }

        public boolean cannotSwipe(OnSwipeListener.Direction d) {
            return !canSwipe(d);
        }

        public boolean canSwipe(OnSwipeListener.Direction d) {
            switch (this) {
                case center:
                    return true;
                case top:
                    return d != OnSwipeListener.Direction.down;
                case bottom:
                    return d != OnSwipeListener.Direction.up;
                case left:
                    return d != OnSwipeListener.Direction.right;
                case right:
                    return d != OnSwipeListener.Direction.left;
                default:
                    return false;
            }
        }
    }

    enum ActionObject {
        normal, camera
    }

    private static ActionObject sActionObject = ActionObject.normal;

    private void init(final Context context) {
        inflate(context, R.layout.screen_lock, this);

        this.mContext = context;

        mWholeParent = (FrameLayout) findViewById(R.id.whole_parent);

        backgroundStrategy();


        mCenterItem = ((ViewStub) findViewById(R.id.lock_home)).inflate();
        setCenterItemDetail();

//        mTopItem = ((ViewStub) findViewById(R.id.viewStubTop)).inflate();
//        mBottomItem = ((ViewStub) findViewById(R.id.lock_password)).inflate();
//        mLeftItem = ((ViewStub) findViewById(R.id.viewStubLeft)).inflate();
//        mRightItem = ((ViewStub) findViewById(R.id.viewStubRight)).inflate();

        mBottomItem = (PinCodeView) findViewById(R.id.password_view);

        mBottomViewCover = findViewById(R.id.black_cover);

        findViewById(R.id.center).setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                sActionObject = ActionObject.normal;
                ViewU.hide(mBottomViewCover);
                DU.sd("touch", "normal");
                return false;
            }
        });

        setCamera(context);

        ViewU.invisible(mTopItem, mBottomItem, mLeftItem, mRightItem);

        View nextView[][] = {/*up, down,left,right*/
            /*center*/ {mBottomItem, mTopItem, mRightItem, mLeftItem},
            /*top   */ {mCenterItem, mCenterItem, mRightItem, mLeftItem},
            /*bottom*/ {mCenterItem, mCenterItem, mRightItem, mLeftItem},
            /*left  */ {mBottomItem, mTopItem, mCenterItem, mCenterItem},
            /*right */ {mBottomItem, mTopItem, mCenterItem, mCenterItem},
        };

        this.nextView = nextView;

        oldX = mCenterItem.getX();
        oldY = mCenterItem.getY();

        SwipeWithAnimListener swipeWithAnimListener = new SwipeWithAnimListener(new SwipeWithAnimListener.DirectionOperator() {
            @Override
            public void up() {
                animationDelegate(mShowingItem, OnSwipeListener.Direction.up);
                if (mDirectionOperator != null) mDirectionOperator.up();
            }

            @Override
            public void down() {
                animationDelegate(mShowingItem, OnSwipeListener.Direction.down);
                if (mDirectionOperator != null) mDirectionOperator.down();
            }

            @Override
            public void left() {
                animationDelegate(mShowingItem, OnSwipeListener.Direction.left);
                if (mDirectionOperator != null) mDirectionOperator.left();
            }

            @Override
            public void right() {
                animationDelegate(mShowingItem, OnSwipeListener.Direction.right);
                if (mDirectionOperator != null) mDirectionOperator.right();
            }
        });

        mGestureDetector = new GestureDetector(context, swipeWithAnimListener);
    }


    private void setCamera(Context context) {
        mCameraIcon = (ImageView) findViewById(R.id.camera_icon);

        if (!CameraHelper.hasCameraHardware(context))
            ViewU.hide(mCameraIcon);
        else
            mCameraIcon.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    DU.sd("touch", "camera");
                    sActionObject = ActionObject.camera;
                    ViewU.show(mBottomViewCover);
                    return false;
                }
            });
    }

//    private ShimmerFrameLayout mShimmerContainer;

    private void setCenterItemDetail() {
        mBatteryIndicator = (ProgressBar) mCenterItem.findViewById(R.id.battery_indicator);
        mBatteryInfo = (TextView) mCenterItem.findViewById(R.id.battery_info);
        mBatteryCharging = (ImageView) mCenterItem.findViewById(R.id.battery_charging);
        mUnlockIcon = (ImageView) mCenterItem.findViewById(R.id.unlock_icon);
//        mShimmerContainer = (FrameLayout) mCenterItem.findViewById(R.id.shimmer_container);

        refreshBatteryInfo(mBatteryIndicator, mBatteryInfo);

        batteryChargingAnim();

        unlockAnim();
    }

    public void refreshBattery(){
        refreshBatteryInfo(mBatteryIndicator,mBatteryInfo);
    }

    private void refreshBatteryInfo(ProgressBar percentage, TextView num) {
        if (percentage == null || num == null) return;

        int batteryPercentage = (int) PhoneStateHelper.getBatteryLevel(mContext);

        if (0 <= batteryPercentage && batteryPercentage <= 100)
            percentage.setProgress(batteryPercentage);

        String info = String.format("%d%%", batteryPercentage);
        if (DU.notEmpty(info))
            num.setText(info);
    }

    public void startShimmer() {
//        if (mShimmerContainer != null)
//            mShimmerContainer.startShimmerAnimation();
    }

    public void stopShimmer() {
//        if (mShimmerContainer != null)
//            mShimmerContainer.stopShimmerAnimation();
    }

    public void batteryChargingAnim() {
        if (PhoneStateHelper.isPowerConnected(mContext)) {
            final ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(mBatteryCharging, View.ALPHA, 0.2f, 1, 1);
            alphaAnimation.setRepeatCount(ValueAnimator.INFINITE);
            alphaAnimation.setRepeatMode(ValueAnimator.REVERSE);
            alphaAnimation.setEvaluator(new FloatEvaluator());
            alphaAnimation.setDuration(1500);
            alphaAnimation.start();
            ViewU.show(mBatteryCharging);
        } else {
            ViewU.hide(mBatteryCharging);
            cancelAnimation(mBatteryCharging);
        }
    }

    private TranslateAnimation mRollingAnim;

    private void unlockAnim() {

        final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mUnlockIcon, View.ALPHA, 0.2f, 1, 1);
        alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
        alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
        alphaAnimator.setEvaluator(new FloatEvaluator());
        alphaAnimator.setDuration(1500);
        alphaAnimator.start();

        if (mRollingAnim == null) {
            mRollingAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
                    0, Animation.RELATIVE_TO_SELF, -0.3f);
            mRollingAnim.setRepeatCount(Animation.INFINITE);
            mRollingAnim.setRepeatMode(Animation.REVERSE);
            mRollingAnim.setDuration(1500);
        }
        mUnlockIcon.startAnimation(mRollingAnim);
    }

    private void backgroundStrategy() {
        DU.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);

                long start = System.currentTimeMillis();

                DU.sd("blur time take", "1", start);
                Bitmap bgBitmap = BitmapFactory.decodeStream(
                        mContext.getResources().openRawResource(+R.drawable.lock_background));
                sDefaultBG = new BitmapDrawable(mContext.getResources(), bgBitmap);

                DU.sd("blur time take", 2, System.currentTimeMillis() - start);

                BitmapFactory.Options option = new BitmapFactory.Options();
                option.inJustDecodeBounds = false;
//                option.inPreferredConfig = Bitmap.Config.ARGB_8888;
                option.inSampleSize = 2;
                /*
                * 8 takes about 140ms on my Mi3
                * 4 takes about 280ms on my Mi3
                * 2 takes about 320ms on my Mi3
                * */

                Bitmap defaultBip = BitmapFactory.decodeResource(getContext().getResources(),
                        R.drawable.lock_background
                        , option
                );
                DU.sd("blur time take", 3, System.currentTimeMillis() - start);


                if (defaultBip == null) {
                    DU.sd("blur exception", "null default");
                    return;
                }

                sBlurredBG = BlurStrategy.getBlurBitmap(getContext(), defaultBip);
            }
        });
    }

    private class EssentialAnimFactors {
        View view;
        String propertyName;
        float fromValue;
        float toValue;
        ActionObject action;

        public EssentialAnimFactors() {
        }

        public void setParams(View view, String propertyName, float fromValue, float toValue) {
            this.view = view;
            this.propertyName = propertyName;
            this.fromValue = fromValue;
            this.toValue = toValue;
        }

        public void setAction(ActionObject action) {
            this.action = action;
        }
    }


    private void cancelAnimation(View view) {
        if (view.getAnimation() == null) return;
        view.getAnimation().cancel();
        view.getAnimation().setAnimationListener(null);
        view.clearAnimation();
        view.setAnimation(null);
    }

    public void stopAnimation() {
        View[] views = {mCenterItem, mTopItem, mBottomItem, mLeftItem, mRightItem};
        for (View v : views) {
            if (v != null)
                cancelAnimation(v);
        }
    }


    private View pairView(ShowingItem item) {
        switch (item) {
            case center:
                return mCenterItem;
            case top:
                return mTopItem;
            case bottom:
                return mBottomItem;
            case left:
                return mLeftItem;
            case right:
                return mRightItem;
            default:
                return mCenterItem;
        }
    }

    private ShowingItem pairItem(View view) {
        if (view.equals(mCenterItem))
            return ShowingItem.center;
        if (view.equals(mTopItem))
            return ShowingItem.top;
        if (view.equals(mBottomItem))
            return ShowingItem.bottom;
        if (view.equals(mLeftItem))
            return ShowingItem.left;
        if (view.equals(mRightItem))
            return ShowingItem.right;

        return ShowingItem.center;
    }

    private synchronized void animationDelegate(final ShowingItem item, OnSwipeListener.Direction d) {
        if (item.cannotSwipe(d)) return;

        View firstView = pairView(item);
        final View secondView = nextView[item.getIndex()][d.getIndex()];

        if (firstView == null || secondView == null) return;

        switch (d) {
            case up:
                mFirstEAF.setParams(firstView, "translationY", firstView.getY(), -firstView.getHeight());
                mSecondEAF.setParams(secondView, "translationY", secondView.getHeight(), 0);

                mSecondEAF.setAction(sActionObject);
                break;
            case down:
                mFirstEAF.setParams(firstView, "translationY", firstView.getY(), firstView.getHeight());
                mSecondEAF.setParams(secondView, "translationY", -secondView.getHeight(), 0);
                break;
            case left:
                mFirstEAF.setParams(firstView, "translationX", firstView.getX(), -firstView.getWidth());
                mSecondEAF.setParams(secondView, "translationX", secondView.getWidth(), 0);
                break;
            case right:
                mFirstEAF.setParams(firstView, "translationX", firstView.getX(), firstView.getWidth());
                mSecondEAF.setParams(secondView, "translationX", -secondView.getWidth(), 0);
                break;
            default:
                break;
        }

        mShowingItem = pairItem(mSecondEAF.view);

        ObjectAnimator animator = ObjectAnimator.ofFloat(mFirstEAF.view, mFirstEAF.propertyName,
                mFirstEAF.fromValue, mFirstEAF.toValue).setDuration(DEFAULT_ANIM_DURATION);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                cancelAnimation(mSecondEAF.view);

                parseSecondEAFAction(mContext, mSecondEAF.action);

                ObjectAnimator anim = ObjectAnimator.ofFloat(mSecondEAF.view, mSecondEAF.propertyName,
                        mSecondEAF.fromValue, mSecondEAF.toValue).setDuration(DEFAULT_ANIM_DURATION);

                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        ViewU.show(mSecondEAF.view);

                    }
                });
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        setBackground();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                anim.start();
                ViewU.show(mFirstEAF.view, mSecondEAF.view);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!pairView(mShowingItem).equals(mFirstEAF.view))
                    ViewU.invisible(mFirstEAF.view);

                // reverse the view position
                ObjectAnimator.ofFloat(mFirstEAF.view, mFirstEAF.propertyName,
                        mFirstEAF.toValue, mFirstEAF.fromValue).setDuration(1).start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animator.start();
    }

    private void parseSecondEAFAction(Context context, ActionObject action) {
        switch (action) {
            case camera:
                DU.sd("action", "camera");
                AnimatorU.alphaOut(mBottomItem);
                if (mSwipeEvent != null) mSwipeEvent.onSwipe(context, true);
                break;
            case normal:
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("NewApi")
    private void setBackground() {
        final Drawable drawable = pairView(mShowingItem).equals(mBottomItem) ? sBlurredBG : sDefaultBG;

        if (!pairView(mShowingItem).equals(mBottomItem))
            resetPinCodeView();

        if (drawable == null) return;


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {

            mWholeParent.setBackgroundDrawable(drawable);
        } else {

            mWholeParent.setBackground(drawable);
        }
    }

    public void resetPinCodeView() {
        mBottomItem.resetPinCodeAndView();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);

        View view = pairView(mShowingItem);

        strategyAnim(event, view);

        return mGestureDetector.onTouchEvent(event);
    }


    private static final int UNSET = 584;
    private static final int SWIPE_X = 456;
    private static final int SWIPE_Y = 416;

    private void strategyAnim(MotionEvent event, View view) {
        float startX = 0f;
        float startY = 0f;

        int mSwipeType = UNSET;// 0 for x; 1 for y

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();

                startX = event.getRawX();
                startY = event.getRawY();
                mSwipeType = UNSET;

                break;

            case MotionEvent.ACTION_MOVE:
                if (mSwipeType == UNSET) {
                    float alterX = event.getRawX() - startX;
                    float alterY = event.getRawY() - startY;

                    mSwipeType = Math.abs(alterX) >= Math.abs(alterY)
                            ? SWIPE_X : SWIPE_Y;
                }


                if (mSwipeType == SWIPE_X)
                    view.animate()
                            .x(event.getRawX() + dX)
                            .setDuration(0)
                            .start();
                else if (mSwipeType == SWIPE_Y)
                    view.animate()
                            .y(event.getRawY() + dY)
                            .setDuration(0)
                            .start();
                break;
            case MotionEvent.ACTION_UP:
                view.animate()
                        .x(oldX)
                        .y(oldY)
                        .setDuration(500)
                        .start();
                break;
        }
    }
}

