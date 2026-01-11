package com.vanvatcorporation.doubleclips.helper;

import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

import androidx.recyclerview.widget.RecyclerView;

public class EdgeScrollHelper {
    private final View view; // RecyclerView or HorizontalScrollView
    private final int edgeThreshold; // px distance from edge
    private final int maxSpeedPxPerTick; // scroll speed
    private final long tickMs; // interval in ms

    private boolean running = false;
    private int velocity = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            view.scrollBy(velocity, 0);
            handler.postDelayed(this, tickMs);
        }
    };

    public EdgeScrollHelper(View view, int edgeThreshold, int maxSpeedPxPerTick, long tickMs) {
        this.view = view;
        this.edgeThreshold = edgeThreshold;
        this.maxSpeedPxPerTick = maxSpeedPxPerTick;
        this.tickMs = tickMs;
    }

    public void attach() {
        System.err.println("attach");
        view.setOnTouchListener((v, event) -> {
            System.err.println("touch");
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    float x = event.getX();
                    int width = v.getWidth();
                    float leftDist = x;
                    float rightDist = width - x;

                    if (leftDist < edgeThreshold) {
                        float t = (edgeThreshold - leftDist) / (float) edgeThreshold;
                        velocity = (int) (-maxSpeedPxPerTick * t);
                    } else if (rightDist < edgeThreshold) {
                        float t = (edgeThreshold - rightDist) / (float) edgeThreshold;
                        velocity = (int) (maxSpeedPxPerTick * t);
                    } else {
                        velocity = 0;
                    }
                    System.err.println(velocity);

                    if (velocity != 0 && !running) start();
                    if (velocity == 0 && running) stop();
                    break;

                default:
                    stop();
                    break;
            }
            return false; // allow normal scrolling
        });
    }

    private void start() {
        running = true;
        handler.post(tick);
    }

    private void stop() {
        running = false;
        handler.removeCallbacks(tick);
        velocity = 0;
    }
}
