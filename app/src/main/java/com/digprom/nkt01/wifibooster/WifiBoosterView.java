package com.digprom.nkt01.wifibooster;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

/**
 * Created by nkt01 on 02.01.2017. WifiBoosterView
 */

public class WifiBoosterView extends View implements Closeable {

    private static final int resFile = R.drawable.wifi_signal;

    private static int numAnimations;
    private static final long ANIMATION_TIME = TimeUnit.SECONDS.toMillis(2);
    private static final int FPS = 30;
    private static final long REFRESH = ANIMATION_TIME / FPS;
    private static final int MAX_SIGNAL = 100;

    private final int BMP_COLUMNS = 5;
    private final int BMP_ROWS = 100;

    private Context context;

    private int width;
    private int height;
    private int imHeight;
    private int heightDif;

    private int currentBMP = 0;
    private int currentBMPHeight = 0;
    private Bitmap image;

    private Rect src;
    private Rect dst;
    private Runnable animator;

    private boolean isEnabled;

    private OnAnimationEndListener listener;
    private int endValueAnimation;


    public WifiBoosterView(Context context) {
        super(context);
        init(context);
    }

    public WifiBoosterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WifiBoosterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        this.context = context;

        image = BitmapFactory.decodeResource(context.getResources(), resFile);

        imHeight = image.getHeight();

        width = image.getWidth() / BMP_COLUMNS;
        height = image.getHeight() / BMP_ROWS;
        heightDif = 1;

        src = new Rect();
        dst = new Rect();

        animator = new RunnableAnimator();

        isEnabled = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minW = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minW, widthMeasureSpec, 1);

        int h = resolveSizeAndState(MeasureSpec.getSize(w) - image.getWidth(), heightMeasureSpec, 0);

        setMeasuredDimension(w, h);
    }

    public void setSignal(float level) {
        if (level > 100 || level < 0) return;

//        Log.d("TAG", "setSignal() called with: level = [" + level + "]");
        currentBMP = Math.round((BMP_COLUMNS-1) * level / MAX_SIGNAL);
        currentBMPHeight = (int) level;

        invalidate();
        requestLayout();
    }

    public int getCurrentSignal() {
        return currentBMP;
    }

    private void update() {
        currentBMP = ++currentBMP % BMP_COLUMNS;
        currentBMPHeight = currentBMPHeight + heightDif;
//        Log.d("TAG", "onStartCommand: h = " + currentBMPHeight);
    }

    public void startAnimation(int toValue) {
        removeCallbacks(animator);

//        Log.d("TAG", "startAnimation() called with: toValue = [" + toValue + "]");
        this.endValueAnimation = Math.round((BMP_COLUMNS-1) * toValue / MAX_SIGNAL);

        if (isEnabled) heightDif = 1;
        else heightDif = -1;

        this.endValueAnimation = Math.abs(toValue - currentBMPHeight);

        post(animator);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(boolean bool) {
        isEnabled = bool;
    }

    public void setListener(OnAnimationEndListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        int x = currentBMP * width;
//        src.set(x, 0, x+width, imHeight);
//        int padding = convertDpToPixels(16, context);
//        dst.set(16, 16, getMeasuredWidth() - padding, getMeasuredHeight() - padding);

        int y = height * currentBMPHeight;
        src.set(0, imHeight-y, image.getWidth(), image.getHeight());
        int padding = convertDpToPixels(16, context);
        int scaled = currentBMPHeight * getMeasuredHeight() / BMP_ROWS;
        dst.set(padding, getMeasuredHeight() - scaled, getMeasuredWidth() - padding, getMeasuredHeight());

        canvas.drawBitmap(image, src, dst, null);
    }

    private int convertDpToPixels(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    @Override
    public void close() {
    }

    private class RunnableAnimator implements Runnable {

        private int counter = 0;

        RunnableAnimator() {
        }

        @Override
        public void run() {

//            boolean scheduleNewFrame = counter++ < 6;
            boolean scheduleNewFrame = counter++ <= endValueAnimation;

//            Log.d("TAG", "run: " + scheduleNewFrame);
//            Log.d("TAG", "run: counter " + counter);
//            Log.d("TAG", "run: to " + (endValueAnimation));

            if (scheduleNewFrame) {
                postDelayed(this, REFRESH);

                update();
                invalidate();
            } else {
                if (listener != null) listener.onAnimationEnd();
                counter = 0;
            }
        }
    }

    public interface OnAnimationEndListener {
        void onAnimationEnd();
    }
}