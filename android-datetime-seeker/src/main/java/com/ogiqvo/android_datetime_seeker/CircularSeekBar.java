/**
 * @author Raghav Sood
 * @version 1
 * @date 26 January, 2013
 */
package com.ogiqvo.android_datetime_seeker;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

/**
 * The Class CircularSeekBar.
 */
public class CircularSeekBar extends View {
    /** The context */
    private Context mContext;

    /** The listener to listen for changes */
    private Collection<SeekChangeListener> seekChangeListeners;

    private Collection<BarHoldListener> barHoldListeners;

    /** The color of the progress ring */
    private Paint circleColor;
    private Paint grayColor;

    /** The start angle (12 O'clock */
    static final private int START_ANGLE = 270;

    /** The maximum progress amount */
    private int maxProgress = Integer.MAX_VALUE;

    /** The current progress */
    private int progress;

    /** The radius of the outer circle */
    private float radius;

    /**
     * The adjustment factor. This adds an adjustment of the specified size to
     * both sides of the progress bar, allowing touch events to be processed
     * more user friendly (yes, I know that's not a word)
     */
    private float outerAdjustmentFactor = 60;
    private float innerAdjustmentFactor = 60;

    /** The progress mark when the view isn't being progress modified */
    private Bitmap progressMark;

    /** The progress mark when the view is being progress modified. */
    private Bitmap progressMarkPressed;

    /** The flag to see if view is pressed */
    private boolean isPressed = false;

    /** The rectangle containing our circles and arcs. */
    private RectF viewBoundingRectangle = new RectF();

    public void setReverseMode(boolean reverseMode) {
        isReverseMode = reverseMode;
    }

    private boolean isReverseMode = true;

    /**
     * Instantiates a new circular seek bar.
     *
     * @param context
     *            the context
     * @param attrs
     *            the attrs
     * @param defStyle
     *            the def style
     */
    public CircularSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;

        init();
    }

    /**
     * Instantiates a new circular seek bar.
     *
     * @param context
     *            the context
     * @param attrs
     *            the attrs
     */
    public CircularSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        seekChangeListeners = new HashSet<>();
        barHoldListeners = new HashSet<>();

        init();
    }

    /**
     * Instantiates a new circular seek bar.
     *
     * @param context
     *            the context
     */
    public CircularSeekBar(Context context) {
        super(context);
        mContext = context;

        init();
    }

    /**
     * Inits the drawable.
     */
    private void init() {
        AssetManager assetManager = mContext.getAssets();

        try {
            progressMark = BitmapFactory.decodeStream(assetManager.open("scrubber_control_normal_holo.png"));
            progressMarkPressed = BitmapFactory.decodeStream(assetManager.open("scrubber_control_pressed_holo.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        circleColor = new Paint();
        circleColor.setColor(Color.parseColor("#ff33b5e5")); // Set default
        circleColor.setAntiAlias(true);
        circleColor.setStrokeWidth(10);
        circleColor.setStyle(Paint.Style.STROKE);

        grayColor = new Paint();
        grayColor.setColor(Color.GRAY);// Set default background color to Gray
        grayColor.setAntiAlias(true);
        grayColor.setStrokeWidth(5);
        grayColor.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onLayout (boolean changed,
                   int left,
                   int top,
                   int right,
                   int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int width = right-left; // Get View Width
        int height = bottom-top;// Get View Height

        if(width <= 0 || height <= 0){
            requestLayout();
            invalidate();
            return;
        }

        int maxMarkWidth = Math.max(progressMark.getWidth(), progressMarkPressed.getWidth());
        int maxMarkHeight = Math.max(progressMark.getHeight(), progressMarkPressed.getHeight());
        int minWidth = width-maxMarkWidth;
        int minHeight = height-maxMarkHeight;

        int scalingSize = Math.min(minWidth, minHeight); // Choose the smaller between width and height to make a square
        radius = scalingSize / 2; // Radius of the outer circle

        float cx = width / 2.0f; // Center X for circle
        float cy = height / 2.0f; // Center Y for circle
        float bleft = cx - radius; // Calculate left bound of our rect
        float bright = cx + radius;// Calculate right bound of our rect
        float btop = cy - radius;// Calculate top bound of our rect
        float bbottom = cy + radius;// Calculate bottom bound of our rect
        viewBoundingRectangle.set(bleft, btop, bright, bbottom); // assign size to rect
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        int width = getWidth(); // Get View Width
        int height = getHeight();// Get View Height
        float cx = width / 2.0f; // Center X for circle
        float cy = height / 2.0f; // Center Y for circle

        float angleDegree = (float) ((double) progress / (double) maxProgress * 360.0f);
        canvas.drawArc(viewBoundingRectangle, START_ANGLE, START_ANGLE + 360, false, grayColor);
        if(isReverseMode) {
            canvas.drawArc(viewBoundingRectangle, angleDegree-90, START_ANGLE-angleDegree+90, false, circleColor);
        }else{
            canvas.drawArc(viewBoundingRectangle, START_ANGLE, angleDegree, false, circleColor);
        }

        float angleRadian = (float) Math.toRadians(angleDegree + START_ANGLE);
        float dx = (float) (Math.cos(angleRadian) * radius);
        float dy = (float) (Math.sin(angleRadian) * radius);
        float x = dx + cx;
        float y = dy + cy;
        Bitmap usingBitmap;
        if (isPressed) {
            usingBitmap = progressMarkPressed;
        } else {
            usingBitmap = progressMark;
        }
        canvas.drawBitmap(usingBitmap, x - usingBitmap.getWidth() / 2, y - usingBitmap.getHeight() / 2, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View#onTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int width = getWidth(); // Get View Width
        int height = getHeight();// Get View Height
        float cx = width / 2.0f; // Center X for circle
        float cy = height / 2.0f; // Center Y for circle
        float x = event.getX();
        float y = event.getY();
        float dx = x - cx;
        float dy = y - cy;

        float touchRadius2 = dx * dx + dy * dy;
        boolean isRequiredToUpdate = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                isPressed = false;
                if(barHoldListeners != null){
                    for(BarHoldListener l : barHoldListeners) {
                        l.onBarReleased();
                    }
                }
                break;

            case MotionEvent.ACTION_DOWN:
                double touchRadius = Math.sqrt(touchRadius2);
                isPressed =
                        touchRadius < radius + innerAdjustmentFactor &&
                                touchRadius > radius - outerAdjustmentFactor;
                isRequiredToUpdate = isPressed;
                if(isPressed && barHoldListeners != null){
                    for(BarHoldListener l : barHoldListeners) {
                        l.onBarHold();
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                isRequiredToUpdate = isPressed;
                break;
        }

        if (!isPressed || !isRequiredToUpdate) {
            invalidate();
            return false; // Done! It's over! Finish!
        }
        double radianAngleAbs = Math.atan2(dy, dx);
        double degreeAngleAbs = Math.toDegrees(radianAngleAbs);
        double degreeAngle = degreeAngleAbs - START_ANGLE;
        while (degreeAngle < 0.0) {
            degreeAngle += 360.0;
        }
        while(degreeAngle > 360.0){
            degreeAngle -= 360.0;
        }

        int newProgress = (int) Math.ceil(degreeAngle / 360.0 * maxProgress);
        this.setProgress(newProgress,true);

        invalidate();
        return true;
    }

    /**
     * Sets the seek bar change listener.
     *
     * @param listener
     *            the new seek bar change listener
     */
    public void addSeekBarChangeListener(SeekChangeListener listener) {
        seekChangeListeners.add(listener);
    }

    public void addBarHoldListener(BarHoldListener listener) {
        barHoldListeners.add(listener);
    }

    /**
     * Gets the max progress.
     *
     * @return the max progress
     */
    public int getMaxProgress() {
        return maxProgress;
    }

    /**
     * Sets the max progress.
     *
     * @param maxProgress
     *            the new max progress
     */
    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
        if (this.progress >= this.maxProgress) {
            this.progress = this.maxProgress - 1;
        }
    }

    /**
     * Gets the progress.
     *
     * @return the progress
     */
    public int getProgress() {
        return this.progress;
    }

    /**
     * Sets the progress.
     *
     * @param progress
     *            the new progress
     */
    public void setProgress(int progress,boolean enableOverflows) {
        int previousProgress = this.progress;

        // Update progress
        this.progress = progress;
        if (this.progress >= maxProgress) {
            this.progress = 0;
        }
        if (this.progress <= -1) {
            this.progress = this.maxProgress - 1;
        }
        int progressDelta = this.progress - previousProgress;
        if (progressDelta == 0) {
            return;
        }
        int overflowThreshold = maxProgress / 4 * 3;
        OverflowType overflowType = OverflowType.NONE;
        if (enableOverflows) {
            if (progressDelta < 0 && progressDelta < -overflowThreshold) {
                overflowType = OverflowType.OVERFLOWED;
            } else if (progressDelta > 0 && progressDelta > overflowThreshold) {
                overflowType = OverflowType.UNDERFLOWED;
            }
        }

        if (seekChangeListeners != null) {
            for(SeekChangeListener l : seekChangeListeners) {
                l.onProgressChange(this, this.progress, overflowType);
            }
        }
        invalidate(); // Repaint UI
    }

    /**
     * Sets the progress color.
     *
     * @param color
     *            the new progress color
     */
    public void setProgressColor(int color) {
        circleColor.setColor(color);
    }

    public void setOuterAdjustmentFactor(float outerAdjustmentFactor) {
        this.outerAdjustmentFactor = outerAdjustmentFactor;
    }

    public void setInnerAdjustmentFactor(float innerAdjustmentFactor) {
        this.innerAdjustmentFactor = innerAdjustmentFactor;
    }

    /**
     * The listener interface for receiving onSeekChange events. The class that
     * is interested in processing a onSeekChange event implements this
     * interface, and the object created with that class is registered with a
     * component using the component's
     * <code>setSeekBarChangeListener(OnSeekChangeListener)<code> method. When
     * the onSeekChange event occurs, that object's appropriate
     * method is invoked.
     */
    public interface SeekChangeListener {
        public void onProgressChange(CircularSeekBar view, int newProgress, OverflowType overflowType);
    }

    public interface BarHoldListener {
        public void onBarHold();
        public void onBarReleased();
    }

    public enum OverflowType {
        NONE, OVERFLOWED, UNDERFLOWED
    }
}
