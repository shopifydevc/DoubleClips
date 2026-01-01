package com.vanvatcorporation.doubleclips.activities;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.vanvatcorporation.doubleclips.R;
import com.vanvatcorporation.doubleclips.activities.main.MainAreaScreen;
import com.vanvatcorporation.doubleclips.activities.main.TemplateAreaScreen;
import com.vanvatcorporation.doubleclips.externalUtils.Random;
import com.vanvatcorporation.doubleclips.helper.DateHelper;
import com.vanvatcorporation.doubleclips.helper.NumberHelper;
import com.vanvatcorporation.doubleclips.impl.AppCompatActivityImpl;
import com.vanvatcorporation.doubleclips.manager.LoggingManager;

import java.io.IOException;

public class TemplatePreviewActivity extends AppCompatActivityImpl {
    TemplateAreaScreen.TemplateData data;

    MediaPlayer mediaPlayer;
    SurfaceView surfaceView;
    TextView usernameText, replacementClipCount, durationClipCount;
    TextView heartCount, commentCount, bookmarkCount;

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

        usernameText.setText("@" + "viet2007ht"); // data.username
        replacementClipCount.setText("" + Random.Range(1, 50)); // data.clipCount

        heartCount.setText("" + NumberHelper.abbreviateNumber(Random.Range(1, 80000000))); // data.clipCount
        commentCount.setText("" + NumberHelper.abbreviateNumber(Random.Range(1, 300000))); // data.clipCount
        bookmarkCount.setText("" + NumberHelper.abbreviateNumber(Random.Range(1, 500000))); // data.clipCount


        setupMediaPlayer(data.getTemplateVideoLink());

    }


    void setupMediaPlayer(String httpsPath) {
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                Context audioAttributionContext = (Build.VERSION.SDK_INT >= 30) ?
                        createAttributionContext("audioPlayback") :
                        TemplatePreviewActivity.this;


                mediaPlayer = MediaPlayer.create(audioAttributionContext, Uri.parse(httpsPath), holder);
                try {
                    mediaPlayer.setOnCompletionListener(MediaPlayer::start);
                    mediaPlayer.setOnPreparedListener(mp -> {
                        mp.start();
                        durationClipCount.setText(DateHelper.convertTimestampToMMSSFormat(mp.getDuration()));
                    });


                } catch (Exception e) {
                    LoggingManager.LogExceptionToNoteOverlay(TemplatePreviewActivity.this, e);
                }
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
