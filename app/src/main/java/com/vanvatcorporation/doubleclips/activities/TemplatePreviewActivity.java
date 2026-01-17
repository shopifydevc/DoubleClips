package com.vanvatcorporation.doubleclips.activities;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.vanvatcorporation.doubleclips.R;
import com.vanvatcorporation.doubleclips.activities.main.TemplateAreaScreen;
import com.vanvatcorporation.doubleclips.externalUtils.Random;
import com.vanvatcorporation.doubleclips.helper.AlgorithmHelper;
import com.vanvatcorporation.doubleclips.helper.DateHelper;
import com.vanvatcorporation.doubleclips.helper.NumberHelper;
import com.vanvatcorporation.doubleclips.impl.AppCompatActivityImpl;
import com.vanvatcorporation.doubleclips.manager.LoggingManager;

import java.io.IOException;
import java.util.concurrent.Executors;

public class TemplatePreviewActivity extends AppCompatActivityImpl {
    TemplateAreaScreen.TemplateData data;

    MediaPlayer mediaPlayer;
    SurfaceView surfaceView;
    TextView usernameText, replacementClipCount, durationClipCount;
    TextView heartCount, commentCount, bookmarkCount;
    ProgressBar mediaLoadingIcon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_template_preview);

        data = (TemplateAreaScreen.TemplateData) createrBundle.getSerializable("TemplateData");

        surfaceView = findViewById(R.id.previewSurfaceView);
        usernameText = findViewById(R.id.usernameText);
        replacementClipCount = findViewById(R.id.replacementClipCount);
        durationClipCount = findViewById(R.id.durationClipCount);
        heartCount = findViewById(R.id.heartCount);
        commentCount = findViewById(R.id.commentCount);
        bookmarkCount = findViewById(R.id.bookmarkCount);

        mediaLoadingIcon = findViewById(R.id.mediaLoadingIcon);

        usernameText.setText("@" + data.getTemplateAuthor());
        replacementClipCount.setText("" + data.getTemplateClipCount());

        heartCount.setText("" + NumberHelper.abbreviateNumber(Random.Range(1, 80000000))); // data.clipCount
        commentCount.setText("" + NumberHelper.abbreviateNumber(Random.Range(1, 300000))); // data.clipCount
        bookmarkCount.setText("" + NumberHelper.abbreviateNumber(Random.Range(1, 500000))); // data.clipCount


        setupMediaPlayer(data.getTemplateVideoLink());

        findViewById(R.id.useTemplateButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, TemplateExportActivity.class);
            intent.putExtra("TemplateData", data);
            startActivity(intent);
        });

    }


    void adjustMaximumAspectRatio(int width, int height)
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // Extract width and height in pixels
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;

        int[] res = AlgorithmHelper.calculateTargetRatio(displayWidth, displayHeight, width, height);

        ViewGroup.LayoutParams surfaceParams = surfaceView.getLayoutParams();
        surfaceParams.width = res[0];
        surfaceParams.height = res[1];
        surfaceView.setLayoutParams(surfaceParams);
    }
    void setupMediaPlayer(String httpsPath) {
        mediaLoadingIcon.setVisibility(View.VISIBLE);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Executors.newSingleThreadExecutor().execute(() -> {

                    // Handle network task.

                    try {
                        Context audioAttributionContext = (Build.VERSION.SDK_INT >= 30) ?
                                createAttributionContext("audioPlayback") :
                                TemplatePreviewActivity.this;


                        mediaPlayer = MediaPlayer.create(audioAttributionContext, Uri.parse(httpsPath), holder);
                        mediaPlayer.setOnCompletionListener(mp -> {
                            runOnUiThread(mp::start);
                        });
                        mediaPlayer.setOnPreparedListener(mp -> {
                            runOnUiThread(() -> {
                                mp.start();
                                durationClipCount.setText(DateHelper.convertTimestampToMMSSFormat(mp.getDuration()));
                                data.setTemplateDuration(mp.getDuration());

                                mediaLoadingIcon.setVisibility(View.GONE);
                            });
                        });
                        mediaPlayer.setOnVideoSizeChangedListener((mp, width, height) -> {
                            runOnUiThread(() -> {
                                adjustMaximumAspectRatio(width, height);
                            });
                        });


                    } catch (Exception e) {
                        LoggingManager.LogExceptionToNoteOverlay(TemplatePreviewActivity.this, e);
                    }

                });
            }

            @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
            @Override public void surfaceDestroyed(SurfaceHolder holder) {
                if (mediaPlayer != null) mediaPlayer.setDisplay(null);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null) mediaPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
