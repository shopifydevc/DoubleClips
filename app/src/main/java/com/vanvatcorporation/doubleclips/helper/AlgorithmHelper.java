package com.vanvatcorporation.doubleclips.helper;

import android.os.Build;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;


public class AlgorithmHelper {
    public static int getRatio(int a, int b) {
        return a / b;
    }

    public static int getRatioToDivideDiv(int div, int a, int b) {
        return div / getRatio(a, b);
    }


    public static int[] calculateTargetRatio(int targetWidth, int targetHeight, int width, int height) {

        if (targetWidth < width) {
            float aspectRatio = (float) height / width;
            width = targetWidth;
            height = Math.round(width * aspectRatio);
        }
        if (targetHeight < height) {
            float aspectRatio = (float) width / height;
            height = targetHeight;
            width = Math.round(height * aspectRatio);
        }

        // 🔼 Enlarge if too small
        if (width < targetWidth && height < targetHeight) {
            // Compare scaling factors for width and height
            float scaleX = (float) targetWidth / width;
            float scaleY = (float) targetHeight / height;

            // Use the smaller scale to fit inside display
            float scale = Math.min(scaleX, scaleY);
            width = Math.round(width * scale);
            height = Math.round(height * scale);
        }
        return new int[] {width, height};
    }
    public static int[] scaleByWidth(int targetWidth, int width, int height) {
        float scale = (float) targetWidth / width;
        int newWidth = targetWidth;
        int newHeight = Math.round(height * scale);
        return new int[] { newWidth, newHeight };
    }
    public static int[] scaleByHeight(int targetHeight, int width, int height) {
        float scale = (float) targetHeight / height;
        int newHeight = targetHeight;
        int newWidth = Math.round(width * scale);
        return new int[] { newWidth, newHeight };
    }




}
