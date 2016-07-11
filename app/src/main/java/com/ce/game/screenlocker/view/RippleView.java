package com.ce.game.screenlocker.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.ce.game.screenlocker.R;
import com.ce.game.screenlocker.inter.RippleAnimationListener;


/**
 * Created by KyleCe on 2016/5/25.
 *
 * @author: KyleCe
 */

public class RippleView extends RelativeLayout {
    private static final String TAG = RippleView.class.getSimpleName();
    private int WIDTH;
    private int HEIGHT;
    private int FRAME_RATE = 10;
    private int DURATION = 400;
    private int PAINT_ALPHA = 90;
    private Handler canvasHandler;
    private float radiusMax = 0;
    private boolean animationRunning = false;
    private int timer = 0;
    private float x = -1;
    private float y = -1;
    private Boolean isCentered;
    private Integer rippleType;
    private Paint paint;
    private Bitmap originBitmap;
    private int rippleColor;
    private int ripplePadding;
    private GestureDetector gestureDetector;
    private RippleAnimationListener mAnimationListener;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

    public RippleView(Context context) {
        super(context);
    }

    public RippleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RippleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attrs) {
        if (isInEditMode())
            return;

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleView);
        rippleColor = typedArray.getColor(R.styleable.RippleView_rv_color, getResources().getColor(R.color.rippleColor));
        rippleType = typedArray.getInt(R.styleable.RippleView_rv_type, 0);
        isCentered = typedArray.getBoolean(R.styleable.RippleView_rv_centered, false);
        DURATION = typedArray.getInteger(R.styleable.RippleView_rv_rippleDuration, DURATION);
        FRAME_RATE = typedArray.getInteger(R.styleable.RippleView_rv_framerate, FRAME_RATE);
        PAINT_ALPHA = typedArray.getInteger(R.styleable.RippleView_rv_alpha, PAINT_ALPHA);
        ripplePadding = typedArray.getDimensionPixelSize(R.styleable.RippleView_rv_ripplePadding, 0);
        canvasHandler = new Handler();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(rippleColor);
        paint.setAlpha(PAINT_ALPHA);
        this.setWillNotDraw(false);

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent event) {
                super.onLongPress(event);
                animateRipple(event);
                sendClickEvent(true);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return true;
            }
        });

        this.setDrawingCacheEnabled(true);
        this.setClickable(true);
    }

    @Override
    public void draw(Canvas canvas) {
        try {
            super.draw(canvas);
            if (animationRunning) {
                if (DURATION <= timer * FRAME_RATE) {
                    if (mAnimationListener != null) {
                        mAnimationListener.onRippleAnimationEnd();
                    }
                    animationRunning = false;
                    timer = 0;
                    canvas.restore();
                    invalidate();
                    return;
                } else
                    canvasHandler.postDelayed(runnable, FRAME_RATE);

                if (timer == 0)
                    canvas.save();

                canvas.drawCircle(x, y, (radiusMax * (((float) timer * FRAME_RATE) / DURATION)), paint);

                timer++;
            }
        } catch (RuntimeException e) {
            if (mAnimationListener != null) {
                mAnimationListener.onRippleAnimationEnd();
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        WIDTH = w;
        HEIGHT = h;
    }

    public void animateRipple(MotionEvent event) {
        createAnimation(event.getX(), event.getY());
    }

    private void createAnimation(final float x, final float y) {
        animationRunning = false;
        timer = 0;
        this.clearAnimation();

        radiusMax = Math.max(WIDTH, HEIGHT);

        if (rippleType != 2)
            radiusMax /= 2;

        radiusMax -= ripplePadding;

        if (isCentered || rippleType == 1) {
            this.x = getMeasuredWidth() / 2;
            this.y = getMeasuredHeight() / 2;
        } else {
            this.x = x;
            this.y = y;
        }

        animationRunning = true;

        if (rippleType == 1 && originBitmap == null)
            originBitmap = getDrawingCache(true);

        invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            animateRipple(event);
            sendClickEvent(false);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        this.onTouchEvent(event);
        return super.onInterceptTouchEvent(event);
    }

    private void sendClickEvent(final Boolean isLongClick) {
        if (getParent() instanceof ListView) {
            final int position = ((ListView) getParent()).getPositionForView(this);
            final long id = ((ListView) getParent()).getItemIdAtPosition(position);
            if (isLongClick) {
                if (((ListView) getParent()).getOnItemLongClickListener() != null)
                    ((ListView) getParent()).getOnItemLongClickListener().onItemLongClick(((ListView) getParent()), this, position, id);
            } else {
                if (((ListView) getParent()).getOnItemClickListener() != null)
                    ((ListView) getParent()).getOnItemClickListener().onItemClick(((ListView) getParent()), this, position, id);
            }
        }
    }

    public void setRippleAnimationListener(RippleAnimationListener rippleAnimationListener) {
        this.mAnimationListener = rippleAnimationListener;
    }
}

