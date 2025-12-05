package com.vanvatcorporation.doubleclips.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.vanvatcorporation.doubleclips.R;

public class PlayerVisualizerUtils {

    /**
     * constant value for Height of the bar
     */
    public static final int VISUALIZER_HEIGHT = 28;


    public static int dp(Context context, float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(context.getResources().getDisplayMetrics().density * value);
    }



    public static Canvas drawVisualizer(Context context, int measuredWidth, int measuredHeight, byte[] bytes, Canvas canvas) {
        Paint playedStatePainting = new Paint();
        Paint notPlayedStatePainting = new Paint();

        playedStatePainting.setStrokeWidth(1f);
        playedStatePainting.setAntiAlias(true);
        playedStatePainting.setColor(ContextCompat.getColor(context, R.color.gray));
        notPlayedStatePainting.setStrokeWidth(1f);
        notPlayedStatePainting.setAntiAlias(true);
        notPlayedStatePainting.setColor(ContextCompat.getColor(context, R.color.colorPrimaryBackground));



        float denseness = 0;


        if (bytes == null || measuredWidth == 0) {
            return canvas;
        }
        float totalBarsCount = (float) measuredWidth / dp(context, 3);

        if (totalBarsCount <= 0.1f) {
            return canvas;
        }
        byte value;
        int samplesCount = (bytes.length * 8 / 5);
        float samplesPerBar = samplesCount / totalBarsCount;
        float barCounter = 0;
        int nextBarNum = 0;

        int y = (measuredHeight - dp(context, VISUALIZER_HEIGHT)) / 2;
        int barNum = 0;
        int lastBarNum;
        int drawBarCount;

        for (int a = 0; a < samplesCount; a++) {
            if (a != nextBarNum) {
                continue;
            }
            drawBarCount = 0;
            lastBarNum = nextBarNum;
            while (lastBarNum == nextBarNum) {
                barCounter += samplesPerBar;
                nextBarNum = (int) barCounter;
                drawBarCount++;
            }
            System.err.println(a);

            int bitPointer = a * 5;
            int byteNum = bitPointer / Byte.SIZE;
            int byteBitOffset = bitPointer - byteNum * Byte.SIZE;
            int currentByteCount = Byte.SIZE - byteBitOffset;
            int nextByteRest = 5 - currentByteCount;
            value = (byte) ((bytes[byteNum] >> byteBitOffset) & ((2 << (Math.min(5, currentByteCount) - 1)) - 1));
            if (nextByteRest > 0) {
                value <<= nextByteRest;
                value |= bytes[byteNum + 1] & ((2 << (nextByteRest - 1)) - 1);
            }

            for (int b = 0; b < drawBarCount; b++) {
                int x = barNum * dp(context, 3);
                float left = x;
                float top = y + dp(context, VISUALIZER_HEIGHT - Math.max(1, VISUALIZER_HEIGHT * value / 31.0f));
                float right = x + dp(context, 2);
                float bottom = y + dp(context, VISUALIZER_HEIGHT);
                if (x < denseness && x + dp(context, 2) < denseness) {
                    canvas.drawRect(left, top, right, bottom, notPlayedStatePainting);
                } else {
                    canvas.drawRect(left, top, right, bottom, playedStatePainting);
                    if (x < denseness) {
                        canvas.drawRect(left, top, right, bottom, notPlayedStatePainting);
                    }
                }
                barNum++;
            }
        }

        return canvas;
    }
}
