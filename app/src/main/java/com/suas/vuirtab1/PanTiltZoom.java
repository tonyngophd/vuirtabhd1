package com.suas.vuirtab1;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class PanTiltZoom {
    private ScaleGestureDetector mScaleGestureDetector;
    public static float mScaleFactor = 1.0f;

    public boolean onTouchEvent(MotionEvent motionEvent) {
        mScaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

    public static class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));
            return true;
        }
    }
}
