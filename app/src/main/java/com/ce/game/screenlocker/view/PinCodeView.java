package com.ce.game.screenlocker.view;

import android.content.Context;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ce.game.screenlocker.R;
import com.ce.game.screenlocker.inter.Clickable;
import com.ce.game.screenlocker.inter.KeyboardButtonClickedListener;
import com.ce.game.screenlocker.view.KeyboardButtonView.KeyType;


/**
 * Created by KyleCe on 2016/5/25.
 *
 * @author: KyleCe
 */
public class PinCodeView extends RelativeLayout implements View.OnTouchListener
        , KeyboardButtonClickedListener,Clickable {

    private Context mContext;


    private static final int DEFAULT_PIN_LENGTH = 4;

    protected TextView mPasswordHint;
    protected PinCodeRoundView mPinCodeRoundView;
    protected KeyboardView mKeyboardView;
    protected ImageView mFingerprintImageView;
    protected TextView mFingerprintTextView;

    protected volatile int mAttemptCount = 0;

    private static final int ATTEMPT_TIMES_ALLOWED = Integer.MAX_VALUE;

    protected String mPinCode;

    protected String mOldPinCode;

    private boolean isCodeSuccessful = false;
    private volatile int mPinLength;

    private UnlockInterface mUnlockRuler;

    private volatile boolean mClickable = true;

    public PinCodeView(Context context) {
        this(context, null);
    }

    public PinCodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PinCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.mContext = context.getApplicationContext();
        initializeView();
    }

    private void initializeView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.password_pin_code, this);

        setUpView();
    }

    public interface UnlockInterface {
        void onUnlock(String string);

        void onBack();
    }

    public void assignUnlockInterface(UnlockInterface unlockInterface) {
        mUnlockRuler = unlockInterface;
    }


    private void setUpView() {
        mPinLength = DEFAULT_PIN_LENGTH;

        mPinCode = "";
        mOldPinCode = "";

        mPinCodeRoundView = (PinCodeRoundView) this.findViewById(R.id.pin_code_round_view);
        mPinCodeRoundView.setPinLength(this.getPinLength());
        mPasswordHint = (TextView) this.findViewById(R.id.pin_code_password_hint);
        mPasswordHint.setOnTouchListener(this);
        mKeyboardView = (KeyboardView) this.findViewById(R.id.pin_code_keyboard_view);
        mKeyboardView.setKeyboardButtonClickedListener(this);
        ((KeyboardView) this.findViewById(R.id.pin_code_keyboard_view)).assignClickableController(this);

        mCountDownTimer = new CountDownTimer(COUNT_DOWN_MILLISECONDS, COUNT_DOWN_INTERNAL) {

            @Override
            public void onTick(long millisUntilFinished) {
                mPasswordHint.setText(
                        String.format(getResources().getConfiguration().locale,
                                getResources().getString(millisUntilFinished / 1000 != 1 ?
                                        R.string.pin_code_retry_after_certain_period :
                                        R.string.pin_code_retry_after_certain_period_one_second),
                                millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                mPasswordHint.setText("");
                mClickable = true;
                mAttemptCount = 0;
            }

        };
    }

    private CountDownTimer mCountDownTimer;
    private static final int COUNT_DOWN_MILLISECONDS = 30000;
    private static final int COUNT_DOWN_INTERNAL = 1000;

    public void resetPinCodeAndView() {
        mPinCode = "";
        mOldPinCode = "";
        if (mPasswordHint != null && mAttemptCount != ATTEMPT_TIMES_ALLOWED)
            mPasswordHint.setText(R.string.pin_code_password_hint);

        if (mPinCodeRoundView != null)
            mPinCodeRoundView.refresh(0);

        if (shouldAnimPinCodeRoundView(mContext.getString(R.string.pin_code_password_incorrect)))
            initLeftToRightAnimation(mPinCodeRoundView);
    }

    private boolean shouldAnimPinCodeRoundView(String hint) {
        return !TextUtils.isEmpty(hint) && hint.equals(mContext.getString(R.string.pin_code_password_incorrect));
    }

    private TranslateAnimation mLeftRightAnimation;

    float mTranslateCellDistance = 0.05f;

    private float[][] mTranAnimPoints = new float[][]{
            {0, -mTranslateCellDistance},
            {-mTranslateCellDistance, mTranslateCellDistance},
            {mTranslateCellDistance, 0}
    };

    private static final int CELL_DURATION = 123;

    private void initLeftToRightAnimation(final View view) {

        if (mLeftRightAnimation == null) {
            mLeftRightAnimation = getTranAnim(mTranAnimPoints[0][0], mTranAnimPoints[0][1]);
            mLeftRightAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                    TranslateAnimation secondAnim = getTranAnim(mTranAnimPoints[1][0], mTranAnimPoints[1][1]);
                    secondAnim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            TranslateAnimation thirdAnim = getTranAnim(mTranAnimPoints[2][0], mTranAnimPoints[2][1]);
                            thirdAnim.setDuration(CELL_DURATION);
                            view.startAnimation(thirdAnim);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    secondAnim.setDuration(CELL_DURATION << 1);
                    secondAnim.setInterpolator(new LinearInterpolator());
                    view.startAnimation(secondAnim);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mLeftRightAnimation.setDuration(CELL_DURATION);
        }

        view.startAnimation(mLeftRightAnimation);
    }

    protected TranslateAnimation getTranAnim(float startX, float startY) {
        return new TranslateAnimation(Animation.RELATIVE_TO_SELF, startX,
                Animation.RELATIVE_TO_SELF, startY, Animation.RELATIVE_TO_SELF,
                0, Animation.RELATIVE_TO_SELF, 0);
    }

    @Override
    public void onKeyboardClick(@KeyType int keyType) {
        if (mPinCode.length() >= this.getPinLength()) return;

        if (keyType == KeyType.K_BACKSPACE)
            setPinCode(!mPinCode.isEmpty() ? mPinCode.substring(0, mPinCode.length() - 1) : "");
        else if (keyType == KeyType.K_BACK) mUnlockRuler.onBack();
        else setPinCode(mPinCode + keyType);
    }

    @Override
    public void onRippleAnimationEnd() {



        // TODO: 2016/5/25 check rule 
        if (mPinCode.length() != getPinLength()) return;

        if (mAttemptCount++ == ATTEMPT_TIMES_ALLOWED) {
            mClickable = false;
            mCountDownTimer.start();
        }

        mUnlockRuler.onUnlock(mPinCode);

//        if (mPinCode.equals("1234")) {
//            mUnlockRuler.onUnlock("1234");
//        } else {
//            DU.t(mContext, getResources().getString(R.string.pin_code_password_incorrect));
//            resetPinCodeAndView();
//            mPasswordHint.setText(R.string.pin_code_password_incorrect);
//        }
    }

    /**
     */
    public void setPinCode(String pinCode) {
        mPinCode = pinCode;
        mPinCodeRoundView.refresh(mPinCode.length());
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (v.getId()) {
            case R.id.pin_code_password_hint:

                break;
            default:
                break;
        }

        return false;
    }

    public int getPinLength() {
        return mPinLength;
    }

    @Override
    public boolean clickable() {
        return mClickable;
    }
}
