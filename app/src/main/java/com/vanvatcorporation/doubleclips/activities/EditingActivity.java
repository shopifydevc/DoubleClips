package com.vanvatcorporation.doubleclips.activities;

import static com.vanvatcorporation.doubleclips.FFmpegEdit.runAnyCommand;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.arthenica.ffmpegkit.Log;
import com.arthenica.ffmpegkit.Statistics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import com.vanvatcorporation.doubleclips.FFmpegEdit;
import com.vanvatcorporation.doubleclips.FXCommandEmitter;
import com.vanvatcorporation.doubleclips.R;
import com.vanvatcorporation.doubleclips.activities.editing.BaseEditSpecificAreaScreen;
import com.vanvatcorporation.doubleclips.activities.editing.ClipEditSpecificAreaScreen;
import com.vanvatcorporation.doubleclips.activities.editing.ClipsEditSpecificAreaScreen;
import com.vanvatcorporation.doubleclips.activities.editing.EffectEditSpecificAreaScreen;
import com.vanvatcorporation.doubleclips.activities.editing.TextEditSpecificAreaScreen;
import com.vanvatcorporation.doubleclips.activities.editing.TransitionEditSpecificAreaScreen;
import com.vanvatcorporation.doubleclips.activities.editing.VideoPropertiesEditSpecificAreaScreen;
import com.vanvatcorporation.doubleclips.constants.Constants;
import com.vanvatcorporation.doubleclips.helper.DateHelper;
import com.vanvatcorporation.doubleclips.helper.IOHelper;
import com.vanvatcorporation.doubleclips.helper.IOImageHelper;
import com.vanvatcorporation.doubleclips.helper.ImageHelper;
import com.vanvatcorporation.doubleclips.helper.MathHelper;
import com.vanvatcorporation.doubleclips.helper.ParserHelper;
import com.vanvatcorporation.doubleclips.helper.StringFormatHelper;
import com.vanvatcorporation.doubleclips.impl.AppCompatActivityImpl;
import com.vanvatcorporation.doubleclips.impl.ImageGroupView;
import com.vanvatcorporation.doubleclips.impl.NavigationIconLayout;
import com.vanvatcorporation.doubleclips.impl.TrackFrameLayout;
import com.vanvatcorporation.doubleclips.impl.java.RunnableImpl;
import com.vanvatcorporation.doubleclips.manager.LoggingManager;
import com.vanvatcorporation.doubleclips.utils.TimelineUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditingActivity extends AppCompatActivityImpl {


    //List<Track> trackList = new ArrayList<>();
    Timeline timeline = new Timeline();
    MainActivity.ProjectData properties;
    VideoSettings settings;

    private LinearLayout timelineTracksContainer, rulerContainer, trackInfoLayout;
    private RelativeLayout timelineWrapper, editingZone, previewZone, editingToolsZone, outerPreviewViewGroup;
    private HorizontalScrollView timelineScroll, rulerScroll;
    private ScrollView timelineVerticalScroll, trackInfoVerticalScroll;
    private TextView currentTimePosText, durationTimePosText, textCanvasControllerInfo;
    private ImageButton addNewTrackButton;
    private FrameLayout previewViewGroup;
    private ImageButton playPauseButton, backButton, settingsButton;
    private Button exportButton;
    private TimelineRenderer timelineRenderer;

    private TrackFrameLayout addNewTrackBlankTrackSpacer;

    private TextEditSpecificAreaScreen textEditSpecificAreaScreen;
    private EffectEditSpecificAreaScreen effectEditSpecificAreaScreen;
    private TransitionEditSpecificAreaScreen transitionEditSpecificAreaScreen;
    private ClipsEditSpecificAreaScreen clipsEditSpecificAreaScreen;
    private ClipEditSpecificAreaScreen clipEditSpecificAreaScreen;
    private VideoPropertiesEditSpecificAreaScreen videoPropertiesEditSpecificAreaScreen;



    private HorizontalScrollView toolbarDefault, toolbarClips, toolbarTrack;



    private int trackCount = 0;
    private final int TRACK_HEIGHT = 100;
    private static final float MIN_CLIP_DURATION = 0.1f; // in seconds
    public static int pixelsPerSecond = 100;

    public static int previewAvailableWidth, previewAvailableHeight;

    public static int centerOffset;
    int currentTimelineEnd = 0; // Keep a variable that tracks the furthest X position of any clip


    float currentTime = 0f; // seconds


    static TransitionClip selectedKnot = null;
    static Clip selectedClip = null;
    static Track selectedTrack = null;

    static ArrayList<Clip> selectedClips = new ArrayList<>();
    boolean isClipSelectMultiple;



    private ScaleGestureDetector scaleDetector;
    private float scaleFactor = 1f;
    private int basePixelsPerSecond = 100;
    private float currentTimeBeforeScrolling = -1;




    Handler playheadHandler = new Handler(Looper.getMainLooper());
    Runnable updatePlayhead = new Runnable() {
        @Override
        public void run() {
//            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//                int currentPositionMs = mediaPlayer.getCurrentPosition();
//                float currentSeconds = currentPositionMs / 1000f;
//
//                int newScrollX = (int) (currentSeconds * pixelsPerSecond);
//                timelineScroll.scrollTo(newScrollX, 0);
//
//                playheadHandler.postDelayed(this, 16); // ~60fps
//            }
        }
    };
    private boolean isPlaying = false;
    private float frameInterval = 1f / 30f; // 30fps
    private Handler playbackHandler = new Handler(Looper.getMainLooper());
    private Runnable playbackLoop;



    FFmpegEdit.FfmpegRenderQueue previewRenderQueue = new FFmpegEdit.FfmpegRenderQueue();


    @SuppressLint("Range")
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
    int getCurrentTimeInX()
    {
        return getTimeInX(currentTime);
    }
    int getTimeInX(float time)
    {
        return (int) (centerOffset + time * pixelsPerSecond);
    }

    private ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    // Check if multiple files are selected

                    float offsetTime = 0;

                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri fileUri = data.getClipData().getItemAt(i).getUri();
                            // Process each file URI
                            offsetTime = parseFileIntoWorkPathAndAddToTrack(fileUri, offsetTime);
                        }
                    } else if (data.getData() != null) {
                        // Single file selected
                        Uri fileUri = data.getData();
                        // Process the file URI
                        parseFileIntoWorkPathAndAddToTrack(fileUri, offsetTime);
                    }
                }
            }
    );

    float parseFileIntoWorkPathAndAddToTrack(Uri uri, float offsetTime)
    {

        if(uri == null) return offsetTime;
        if(selectedTrack == null) return offsetTime;
        String filename = getFileName(uri);
        String clipPath = IOHelper.CombinePath(properties.getProjectPath(), Constants.DEFAULT_CLIP_DIRECTORY, filename);
        String previewClipPath = IOHelper.CombinePath(properties.getProjectPath(), Constants.DEFAULT_PREVIEW_CLIP_DIRECTORY, filename);
        IOHelper.writeToFileAsRaw(this, clipPath, IOHelper.readFromFileAsRaw(this, getContentResolver(), uri));

        float duration = 3f; // fallback default if needed
        String mimeType = getContentResolver().getType(uri);

        if(mimeType == null) return offsetTime;


        ClipType type;

        if (mimeType.startsWith("audio/")) type = ClipType.AUDIO;
        else if (mimeType.startsWith("image/")) type = ClipType.IMAGE;
        else if (mimeType.startsWith("video/")) type = ClipType.VIDEO;
        else type = ClipType.EFFECT; // if effect or unknown



        if(type == ClipType.VIDEO || type == ClipType.AUDIO)
            try {
                MediaExtractor extractor = new MediaExtractor();
                extractor.setDataSource(clipPath);



                for (int i = 0; i < extractor.getTrackCount(); i++) {
                    MediaFormat format = extractor.getTrackFormat(i);
                    String trackMime = format.getString(MediaFormat.KEY_MIME);
                    if (trackMime != null) {
                        // For MOV type, it has 2 track, so before this version, this applied to the below ClipType, which turn MOV to audio type
                        //mimeType = trackMime;

                        if (format.containsKey(MediaFormat.KEY_DURATION)) {
                            long d = format.getLong(MediaFormat.KEY_DURATION);
                            duration = d / 1_000_000f; // microseconds to seconds
                            break;
                        }
                    }
                }

                extractor.release();
            } catch (Exception e) {
                LoggingManager.LogExceptionToNoteOverlay(this, e);
            }


        boolean isVideoHasAudio = false;
        int width = 0, height = 0;
        // Video check
        if(type == ClipType.VIDEO)
        {
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(clipPath);

                width = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)));
                height = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));

                isVideoHasAudio = "yes".equals(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO));
                retriever.close();
            } catch (IOException e) {
                LoggingManager.LogExceptionToNoteOverlay(this, e);
            }
        }
        if(type == ClipType.IMAGE)
        {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true; // Don't load the full bitmap
            BitmapFactory.decodeFile(clipPath, options);
            width = options.outWidth;
            height = options.outHeight;
        }


        Clip newClip = new Clip(filename, currentTime + offsetTime, duration, selectedTrack.trackIndex, type, isVideoHasAudio, width, height);


        addClipToTrack(selectedTrack, newClip);

        offsetTime += duration;


        previewRenderQueue.enqueue(new FFmpegEdit.FfmpegRenderQueue.FfmpegRenderQueueInfo("Preview Generation",
                () -> {
                    processingPreview(newClip, clipPath, previewClipPath);
                }));

        if(type == ClipType.VIDEO && isVideoHasAudio)
        {
            previewRenderQueue.enqueue(new FFmpegEdit.FfmpegRenderQueue.FfmpegRenderQueueInfo("Preview Generation",
                    () -> {
                        // Extract audio from Video if it has audio
                        Clip audioClip = new Clip(newClip);
                        audioClip.type = ClipType.AUDIO;
                        processingPreview(audioClip, clipPath, previewClipPath);
                    }));
        }



        return offsetTime;
    }

    void processingPreview(Clip clip, String originalClipPath, String previewClipPath)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate your custom layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.popup_processing_preview, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        builder.setNegativeButton(getText(R.string.alert_processing_without_preview_confirmation),
                (dialog, which) -> {
            // This initial listener might be simple or a placeholder
                    dialog.dismiss();
        });

        // Get references to the EditText and Buttons in your custom layout
        TextView processingText = dialogView.findViewById(R.id.adsDescriptionText);
        TextView processingDescription = dialogView.findViewById(R.id.processingDescription);
        ProgressBar previewProgressBar = dialogView.findViewById(R.id.previewProgressBar);
        TextView processingPercent = dialogView.findViewById(R.id.processingPercent);

        // Create the AlertDialog
        AlertDialog dialog = builder.create();

        // Already prevent using the setCancelable above
        //dialog.setCanceledOnTouchOutside(false);
        // Show the dialog
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);


        processingText.setText(getString(R.string.processing) + " " + "\"" + clip.clipName + "\"");



        String previewGeneratedVideoCmd = "-i \"" + originalClipPath +
                "\" -vf \"scale=1280:-2\" -c:v libx264 -preset ultrafast -crf 32 -x264-params keyint=1 -an -y \"" + previewClipPath + "\"";
        String previewGeneratedAudioCmd = "-i \"" + originalClipPath +
                "\" -vn -ac 1 -ar 22050 -c:a pcm_s16le -y \"" + previewClipPath.substring(0, previewClipPath.lastIndexOf('.')) + ".wav\"";


        processingDescription.setTextColor(0xFF00AA00);
        if(clip.type == ClipType.AUDIO)
        {
            runAnyCommand(this, previewGeneratedAudioCmd, "Exporting Preview Audio",
                    () -> EditingActivity.this.runOnUiThread(() -> {

                        dialog.dismiss();
                        previewRenderQueue.taskCompleted();
                    }), () -> {
                        processingDescription.post(() -> {
                            processingDescription.setTextColor(0xFFFF0000);

                            dialog.setCancelable(true);
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
                        });
                    }
                    , new RunnableImpl() {
                        @Override
                        public <T> void runWithParam(T param) {
                            Log log = (Log) param;
                            processingDescription.post(() -> {
                                processingDescription.setText(log.getMessage());
                            });
                        }
                    }, new RunnableImpl() {
                        @Override
                        public <T> void runWithParam(T param) {
                            double duration = clip.duration * 1000;

                            Statistics statistics = (Statistics) param;
                            {
                                if (statistics.getTime() > 0) {
                                    int progress = (int) ((statistics.getTime() * 100) / (int) duration);
                                    previewProgressBar.setMax(100);
                                    previewProgressBar.setProgress(progress);
                                    processingPercent.setText(progress + "%");
                                }
                            }
                        }
                    });
        }
        else if(clip.type == ClipType.VIDEO)
        {
            runAnyCommand(this, previewGeneratedVideoCmd, "Exporting Preview Video",
                    () -> EditingActivity.this.runOnUiThread(() -> {
                        dialog.dismiss();
                        previewRenderQueue.taskCompleted();
                    }), () -> {
                        processingDescription.post(() -> {
                            processingDescription.setTextColor(0xFFFF0000);

                            dialog.setCancelable(true);
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
                        });
                }
                    , new RunnableImpl() {
                        @Override
                        public <T> void runWithParam(T param) {
                            Log log = (Log) param;
                            processingDescription.post(() -> {
                                processingDescription.setText(log.getMessage());
                            });
                        }
                    }, new RunnableImpl() {
                        @Override
                        public <T> void runWithParam(T param) {
                            double duration = clip.duration * 1000;

                            Statistics statistics = (Statistics) param;
                            {
                                if (statistics.getTime() > 0) {
                                    int progress = (int) ((statistics.getTime() * 100) / (int) duration);
                                    previewProgressBar.setMax(100);
                                    previewProgressBar.setProgress(progress);
                                    processingPercent.setText(progress + "%");
                                }
                            }
                        }
                    });







        }
        else {
            // Any other type should be drop
            dialog.dismiss();
        }
    }


    void pickingContent()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow multiple
        filePickerLauncher.launch(Intent.createChooser(intent, "Select Media"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        properties = (MainActivity.ProjectData) createrBundle.getSerializable("ProjectProperties");

        //settings = new VideoSettings(1280, 720, 30, 30, VideoSettings.FfmpegPreset.MEDIUM, VideoSettings.FfmpegTune.ZEROLATENCY);
        assert properties != null;
        settings = VideoSettings.loadSettings(this, properties);

        if(settings == null)
        {
            settings = new VideoSettings(1366, 768, 30, 30, VideoSettings.FfmpegPreset.MEDIUM, VideoSettings.FfmpegTune.ZEROLATENCY);
        }

        pixelsPerSecond = basePixelsPerSecond;


        setContentView(R.layout.layout_editing);
        timelineTracksContainer = findViewById(R.id.timeline_tracks_container);
        trackInfoLayout = findViewById(R.id.trackInfoLayout);

        timelineWrapper = findViewById(R.id.timeline_wrapper);


        editingZone = findViewById(R.id.editingZone);
        previewZone = findViewById(R.id.previewZone);
        editingToolsZone = findViewById(R.id.editingToolsZone);

        currentTimePosText = findViewById(R.id.currentTimePosText);
        durationTimePosText = findViewById(R.id.durationTimePosText);
        textCanvasControllerInfo = findViewById(R.id.textCanvasControllerInfo);

        // Example: Add button to add more tracks
        addNewTrackButton = findViewById(R.id.addTrackButton);
        addNewTrackButton.setOnClickListener(v -> {
            Track track = addNewTrack();
            track.viewRef.trackInfo = track;
        });
        //timelineTracksContainer.addView(addTrackButton);

        previewViewGroup = findViewById(R.id.previewViewGroup);

        ViewGroup.LayoutParams previewViewGroupParams = previewViewGroup.getLayoutParams();
        previewViewGroupParams.width = settings.videoWidth;
        previewViewGroupParams.height = settings.videoHeight;
        previewViewGroup.setLayoutParams(previewViewGroupParams);


        outerPreviewViewGroup = findViewById(R.id.outerPreviewViewGroup);

        outerPreviewViewGroup.post(() -> {
            previewAvailableWidth = outerPreviewViewGroup.getWidth();
            previewAvailableHeight = outerPreviewViewGroup.getHeight();
        });

        playPauseButton = findViewById(R.id.playPauseButton);
        playPauseButton.setOnClickListener(v -> {

            isPlaying = !isPlaying;

            if (isPlaying) {
                startPlayback();
                playPauseButton.setImageResource(R.drawable.baseline_pause_circle_24);
            } else {
                stopPlayback();
            }
        });
        exportButton = findViewById(R.id.exportButton);
        exportButton.setOnClickListener(v -> {
            Timeline.saveTimeline(this, timeline, properties, settings);
            Intent intent = new Intent(this, ExportActivity.class);
            intent.putExtra("ProjectProperties", properties);
            intent.putExtra("ProjectSettings", settings);
            intent.putExtra("ProjectTimeline", timeline);
            startActivity(intent);
        });
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
        });
        settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            videoPropertiesEditSpecificAreaScreen.open();
        });




        // Time ruler
        rulerContainer = findViewById(R.id.ruler_container);

        timelineScroll = findViewById(R.id.trackHorizontalScrollView);
        rulerScroll = findViewById(R.id.ruler_scroll);
        timelineScroll.post(() -> {

            centerOffset = timelineScroll.getWidth() / 2;
            timelineScroll.scrollTo(centerOffset, 0);

            // Add initial track after centerOffset is taken
            //addNewTrack();



            timeline = Timeline.loadTimeline(this, this, properties);
        });
        timelineScroll.getViewTreeObserver().addOnScrollChangedListener(() -> {
            rulerScroll.scrollTo(timelineScroll.getScrollX(), 0);

            if(!isPlaying)
                currentTime = (timelineScroll.getScrollX()) / (float) pixelsPerSecond;

            // Get time (- centerOffset mean remove the start spacer)
            //float totalSeconds = (timelineScroll.getScrollX()) / (float) pixelsPerSecond;
            currentTimePosText.post(() -> currentTimePosText.setText(DateHelper.convertTimestampToMMSSFormat((long) (currentTime * 1000L)) + String.format(".%02d", ((long)((currentTime % 1) * 100)))));

            timelineRenderer.updateTime(currentTime, !isPlaying);
        });


        timelineVerticalScroll = findViewById(R.id.trackVerticalScrollView);
        trackInfoVerticalScroll = findViewById(R.id.trackInfoScroll);


        timelineVerticalScroll.getViewTreeObserver().addOnScrollChangedListener(() -> {
            trackInfoVerticalScroll.scrollTo(0, timelineVerticalScroll.getScrollY());
        });



        addNewTrackBlankTrackSpacer = new TrackFrameLayout(this);
        addNewTrackBlankTrackSpacer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,//ViewGroup.LayoutParams.MATCH_PARENT,
                TRACK_HEIGHT
        ));
        addNewTrackBlankTrackSpacer.setBackgroundColor(Color.parseColor("#222222"));
        addNewTrackBlankTrackSpacer.setPadding(4, 4, 4, 4);
        handleTrackInteraction(addNewTrackBlankTrackSpacer);





        setupPreview();

        setupTimelinePinchAndZoom();

        setupSpecificEdit();

        setupToolbars();

        handleEditZoneInteraction(timelineScroll);
    }
    private void editingMultiple() {
        clipsEditSpecificAreaScreen.open();
    }
    private void editingSpecific(ClipType type) {
        switch (type)
        {
            case TEXT:
                textEditSpecificAreaScreen.open();
                break;
            case EFFECT:
                effectEditSpecificAreaScreen.open();
                break;
            case TRANSITION:
                transitionEditSpecificAreaScreen.open();
                break;
            case VIDEO:
            case IMAGE:
                clipEditSpecificAreaScreen.open();
                break;
        }
    }

    // Bottom Navigation Bar
    private void setupToolbars()
    {
        // ===========================       CRITICAL ZONE       ====================================

        toolbarDefault = (HorizontalScrollView) LayoutInflater.from(this).inflate(R.layout.view_toolbar_default, null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        editingToolsZone.addView(toolbarDefault, params);

        toolbarTrack = (HorizontalScrollView) LayoutInflater.from(this).inflate(R.layout.view_toolbar_track, null);
        RelativeLayout.LayoutParams paramsTrack = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsTrack.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        editingToolsZone.addView(toolbarTrack, paramsTrack);
        toolbarTrack.setVisibility(View.GONE);

        toolbarClips = (HorizontalScrollView) LayoutInflater.from(this).inflate(R.layout.view_toolbar_clips, null);
        RelativeLayout.LayoutParams paramsClips = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsClips.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        editingToolsZone.addView(toolbarClips, paramsClips);
        toolbarClips.setVisibility(View.GONE);

        // ===========================       CRITICAL ZONE       ====================================


        // ===========================       DEFAULT ZONE       ====================================


        toolbarDefault.findViewById(R.id.splitMediaButton).setOnClickListener(v -> {
            List<Clip> affectedClips = timeline.getClipAtCurrentTime(currentTime);
            if(selectedClip != null && affectedClips.contains(selectedClip)) {
                selectedClip.splitClip(this, timeline, currentTime);
            }
            else {
                for (Clip clip : affectedClips) {
                    clip.splitClip(this, timeline, currentTime);
                }

            }
        });


        // ===========================       DEFAULT ZONE       ====================================



        // ===========================       TRACK ZONE       ====================================


        toolbarTrack.findViewById(R.id.addMediaButton).setOnClickListener(v -> {
            if(selectedTrack != null)
                pickingContent();
            else new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a track first!").show();
        });
        toolbarTrack.findViewById(R.id.deleteTrackButton).setOnClickListener(v -> {
            if(selectedTrack != null) {
                selectedTrack.delete(timeline, timelineTracksContainer, trackInfoLayout, this);
                updateCurrentClipEnd();
            }
            else new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a clip first!").show();

        });
        toolbarTrack.findViewById(R.id.splitMediaButton).setOnClickListener(v -> {
            List<Clip> affectedClips = timeline.getClipAtCurrentTime(currentTime);
            if(selectedClip != null && affectedClips.contains(selectedClip)) {
                selectedClip.splitClip(this, timeline, currentTime);
            }
            else {
                for (Clip clip : affectedClips) {
                    clip.splitClip(this, timeline, currentTime);
                }

            }
        });

        toolbarTrack.findViewById(R.id.addTextButton).setOnClickListener(v -> {

            if(selectedTrack == null) {
                new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a track first!").show();
                return;
            }

            float duration = 3f; // fallback default if needed
            ClipType type = ClipType.TEXT; // if effect or unknown



            Clip newClip = new Clip("TEXT", currentTime, duration, selectedTrack.trackIndex, type, false, 0, 0);
            newClip.textContent = "Simple text";
            newClip.fontSize = 30;
            addClipToTrack(selectedTrack, newClip);
        });
        toolbarTrack.findViewById(R.id.addEffectButton).setOnClickListener(v -> {

            if(selectedTrack == null) {
                new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a track first!").show();
                return;
            }

            float duration = 3f; // fallback default if needed
            ClipType type = ClipType.EFFECT; // if effect or unknown

            Clip newClip = new Clip("EFFECT", currentTime, duration, selectedTrack.trackIndex, type, false, 0, 0);
            newClip.effect = new EffectTemplate("glitch-pulse", 1.2, 4.0);

            addClipToTrack(selectedTrack, newClip);
        });
        toolbarTrack.findViewById(R.id.selectAllButton).setOnClickListener(v -> {
            // Todo: Not fully implemented yet. The idea is to remake the whole thing, get the "array" of selected clip is completed
            // now if one clip is move then the whole array move along. Also ghost will be as well


            if(selectedTrack != null) {
                isClipSelectMultiple = true;
                ((NavigationIconLayout)toolbarClips.findViewById(R.id.selectMultipleButton)).getIconView().setColorFilter(0xFFFF0000, PorterDuff.Mode.SRC_ATOP);

                deselectingClip();

                for (Clip clip : selectedTrack.clips) {
                    selectingClip(clip);
                }
            }
            else new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a track first!").show();
        });
        toolbarTrack.findViewById(R.id.autoSnapButton).setOnClickListener(v -> {
            if(selectedTrack != null) {
                selectedTrack.sortClips();
                List<Clip> clips = selectedTrack.clips;
                for (int i = 1; i < clips.size(); i++) {
                    Clip clip = clips.get(i);
                    Clip prevClip = clips.get(i - 1);

                    clip.startTime = prevClip.startTime + prevClip.duration;
                }

                updateClipLayouts();
                updateCurrentClipEnd();
            }
            else new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a track first!").show();
        });


        // ===========================       TRACK ZONE       ====================================





        // ===========================       CLIPS ZONE       ====================================


        toolbarClips.findViewById(R.id.deleteMediaButton).setOnClickListener(v -> {
            if(selectedClip != null) {
                selectedClip.deleteClip(timeline, this);
                updateCurrentClipEnd();
            }
            if(selectedClips != null) {
                for (Clip selectedClip : selectedClips) {
                    selectedClip.deleteClip(timeline, this);
                }
                updateCurrentClipEnd();
            }

            else new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a clip first!").show();

        });
        toolbarClips.findViewById(R.id.splitMediaButton).setOnClickListener(v -> {
            List<Clip> affectedClips = timeline.getClipAtCurrentTime(currentTime);
            if(selectedClip != null && affectedClips.contains(selectedClip)) {
                selectedClip.splitClip(this, timeline, currentTime);
            }
            else new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a clip first!").show();
        });
        toolbarClips.findViewById(R.id.editMediaButton).setOnClickListener(v -> {
            if(selectedClip != null) {
                editingSpecific(selectedClip.type);
            }
            else if(!selectedClips.isEmpty())
            {
                editingMultiple();
            }
            else new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a clip first!").show();
        });
        toolbarClips.findViewById(R.id.addKeyframeButton).setOnClickListener(v -> {
            if(selectedClip != null) {
                addKeyframe(selectedClip, currentTime);
            }
            else new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a clip first!").show();
        });
        toolbarClips.findViewById(R.id.selectMultipleButton).setOnClickListener(v -> {
            // Todo: Not fully implemented yet. The idea is to remake the whole thing, get the "array" of selected clip is completed
            // now if one clip is move then the whole array move along. Also ghost will be as well

            isClipSelectMultiple = !isClipSelectMultiple;

            ((NavigationIconLayout)toolbarClips.findViewById(R.id.selectMultipleButton)).getIconView().setColorFilter((isClipSelectMultiple ? 0xFFFF0000 : 0xFFFFFFFF), PorterDuff.Mode.SRC_ATOP);
        });
        toolbarClips.findViewById(R.id.applyKeyframeToAllClip).setOnClickListener(v -> {

            if(selectedTrack != null && selectedClip != null) {

                List<Clip> clips = selectedTrack.clips;
                for (Clip clip : clips) {
                    if(clip != selectedClip)
                    {
                        clip.keyframes.keyframes.clear();
                        clip.keyframes.keyframes.addAll(selectedClip.keyframes.keyframes);
                        //TODO: Fetch the addKeyframesUi again to match the preview
                    }
                }
            }
            else new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a track first!").show();

        });
        toolbarClips.findViewById(R.id.restateButton).setOnClickListener(v -> {
            if(selectedClip != null) {
                selectedClip.restate();

                // TODO: Find a way to specifically build only the edited clip. Not entire timeline
                //  this is just for testing. Resource-consuming asf.
                regeneratingTimelineRenderer();



            }
            else new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a clip first!").show();
        });

        // ===========================       CLIPS ZONE       ====================================
    }

    private void setupSpecificEdit()
    {
        // ===========================       TEXT ZONE       ====================================
        textEditSpecificAreaScreen = (TextEditSpecificAreaScreen) LayoutInflater.from(this).inflate(R.layout.view_edit_specific_text, null);
        editingZone.addView(textEditSpecificAreaScreen);
        // ===========================       TEXT ZONE       ====================================


        // ===========================       EFFECT ZONE       ====================================
        effectEditSpecificAreaScreen = (EffectEditSpecificAreaScreen) LayoutInflater.from(this).inflate(R.layout.view_edit_specific_effect, null);
        editingZone.addView(effectEditSpecificAreaScreen);
        // ===========================       EFFECT ZONE       ====================================


        // ===========================       TRANSITION ZONE       ====================================
        transitionEditSpecificAreaScreen = (TransitionEditSpecificAreaScreen) LayoutInflater.from(this).inflate(R.layout.view_edit_specific_transition, null);
        editingZone.addView(transitionEditSpecificAreaScreen);
        // ===========================       TRANSITION ZONE       ====================================

        // ===========================       MULTIPLE CLIPS ZONE       ====================================
        clipsEditSpecificAreaScreen = (ClipsEditSpecificAreaScreen) LayoutInflater.from(this).inflate(R.layout.view_edit_multiple_clips, null);
        editingZone.addView(clipsEditSpecificAreaScreen);
        // ===========================       MULTIPLE CLIPS ZONE       ====================================

        // ===========================       CLIP ZONE       ====================================
        clipEditSpecificAreaScreen = (ClipEditSpecificAreaScreen) LayoutInflater.from(this).inflate(R.layout.view_edit_specific_clip, null);
        editingZone.addView(clipEditSpecificAreaScreen);
        // ===========================       CLIP ZONE       ====================================


        // ===========================       VIDEO PROPERTIES ZONE       ====================================
        videoPropertiesEditSpecificAreaScreen = (VideoPropertiesEditSpecificAreaScreen) LayoutInflater.from(this).inflate(R.layout.view_edit_specific_video_properties, null);
        previewZone.addView(videoPropertiesEditSpecificAreaScreen);
        // ===========================       VIDEO PROPERTIES ZONE       ====================================






        // ===========================       TEXT ZONE       ====================================

        textEditSpecificAreaScreen.onClose.add(() -> {
            if(selectedClip != null)
            {
                selectedClip.textContent = textEditSpecificAreaScreen.textEditContent.getText().toString();
                selectedClip.fontSize = ParserHelper.TryParse(textEditSpecificAreaScreen.textSizeContent.getText().toString(), 28f);
            }
        });
        textEditSpecificAreaScreen.onOpen.add(() -> {
            textEditSpecificAreaScreen.textEditContent.setText(selectedClip.textContent);
            textEditSpecificAreaScreen.textSizeContent.setText(String.valueOf(selectedClip.fontSize));
        });

        // ===========================       TEXT ZONE       ====================================


        // ===========================       EFFECT ZONE       ====================================


        effectEditSpecificAreaScreen.onClose.add(() -> {
            if(selectedClip != null)
            {
                selectedClip.effect = new EffectTemplate((String) FXCommandEmitter.FXRegistry.effectsFXMap.keySet().toArray()[effectEditSpecificAreaScreen.effectEditContent.getSelectedItemPosition()], ParserHelper.TryParse(effectEditSpecificAreaScreen.effectDurationContent.getText().toString(), 1), 1);
            }
        });
        effectEditSpecificAreaScreen.onOpen.add(() -> {
            List<String> stringEffects = Arrays.asList(FXCommandEmitter.FXRegistry.effectsFXMap.values().toArray(new String[0]));
            effectEditSpecificAreaScreen.effectEditContent.setSelection(stringEffects.indexOf(FXCommandEmitter.FXRegistry.effectsFXMap.get(selectedClip.effect.style)));
            effectEditSpecificAreaScreen.effectDurationContent.setText(String.valueOf(selectedClip.effect.duration));
        });

        // ===========================       EFFECT ZONE       ====================================


        // ===========================       TRANSITION ZONE       ====================================



        transitionEditSpecificAreaScreen.onClose.add(() -> {
            if(selectedKnot != null)
            {
                for (int i = 0; i < timeline.tracks.get(selectedKnot.trackIndex).transitions.size(); i++)
                {
                    TransitionClip clip = timeline.tracks.get(selectedKnot.trackIndex).transitions.get(i);
                    if(clip == selectedKnot)
                    {
                        clip.duration = ParserHelper.TryParse(transitionEditSpecificAreaScreen.transitionDurationContent.getText().toString(), 0.5f);
                        clip.effect.style = (String) FXCommandEmitter.FXRegistry.transitionFXMap.keySet().toArray()[transitionEditSpecificAreaScreen.transitionEditContent.getSelectedItemPosition()];
                        clip.effect.duration = ParserHelper.TryParse(transitionEditSpecificAreaScreen.transitionDurationContent.getText().toString(), 0.5f);
                        clip.mode = TransitionClip.TransitionMode.values()[transitionEditSpecificAreaScreen.transitionModeEditContent.getSelectedItemPosition()];
//
//                        for (TransitionClip clip2 : timeline.tracks.get(selectedKnot.trackIndex).transitions)
//                            System.err.println(clip2.duration);
                        selectedKnot = null;
                        break;
                    }
                }
            }
        });
        transitionEditSpecificAreaScreen.applyAllTransitionButton.setOnClickListener(v -> {
            transitionEditSpecificAreaScreen.animateLayout(BaseEditSpecificAreaScreen.AnimationType.Close);
            if(selectedKnot != null)
            {
                for (int i = 0; i < timeline.tracks.get(selectedKnot.trackIndex).transitions.size(); i++)
                {
                    TransitionClip clip = timeline.tracks.get(selectedKnot.trackIndex).transitions.get(i);
                    clip.duration = ParserHelper.TryParse(transitionEditSpecificAreaScreen.transitionDurationContent.getText().toString(), 0.5f);
                    clip.effect.style = (String) FXCommandEmitter.FXRegistry.transitionFXMap.keySet().toArray()[transitionEditSpecificAreaScreen.transitionEditContent.getSelectedItemPosition()];
                    clip.effect.duration = ParserHelper.TryParse(transitionEditSpecificAreaScreen.transitionDurationContent.getText().toString(), 0.5f);
                    clip.mode = TransitionClip.TransitionMode.values()[transitionEditSpecificAreaScreen.transitionModeEditContent.getSelectedItemPosition()];
                }
//                for (TransitionClip clip : timeline.tracks.get(selectedKnot.trackIndex).transitions)
//                    System.err.println(clip.duration);
                selectedKnot = null;

            }
        });
        transitionEditSpecificAreaScreen.onOpen.add(() -> {
            List<String> stringTransition = Arrays.asList(FXCommandEmitter.FXRegistry.transitionFXMap.values().toArray(new String[0]));
            transitionEditSpecificAreaScreen.transitionEditContent.setSelection(stringTransition.indexOf(FXCommandEmitter.FXRegistry.transitionFXMap.get(selectedKnot.effect.style)));
            transitionEditSpecificAreaScreen.transitionDurationContent.setText(String.valueOf(selectedKnot.effect.duration));
            transitionEditSpecificAreaScreen.transitionModeEditContent.setSelection(selectedKnot.mode.ordinal());
        });

        // ===========================       TRANSITION ZONE       ====================================



        // ===========================       MULTIPLE CLIPS ZONE       ====================================

        clipsEditSpecificAreaScreen.onClose.add(() -> {
            if(!selectedClips.isEmpty())
            {
                for (Clip clip : selectedClips) {
                    float defaultValue = clip.duration;
                    clip.duration = ParserHelper.TryParse(clipsEditSpecificAreaScreen.clipsDurationContent.getText().toString(), defaultValue);
                }
                updateClipLayouts();
                updateCurrentClipEnd();
            }
        });
        clipsEditSpecificAreaScreen.onOpen.add(() -> {
            // TODO: Didn't change following the endClipTrim/startClipTrim rule yet.
            clipsEditSpecificAreaScreen.clipsDurationContent.setText(String.valueOf(selectedClips.get(0).duration));
        });


        // ===========================       MULTIPLE CLIPS ZONE       ====================================



        // ===========================       CLIP ZONE       ====================================

        clipEditSpecificAreaScreen.onClose.add(() -> {
            if(selectedClip != null)
            {
                selectedClip.clipName = clipEditSpecificAreaScreen.clipNameField.getText().toString();
                selectedClip.duration = ParserHelper.TryParse(clipEditSpecificAreaScreen.durationContent.getText().toString(), selectedClip.duration);
                selectedClip.videoProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.positionXField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.PosX)), VideoProperties.ValueType.PosX);
                selectedClip.videoProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.positionYField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.PosY)), VideoProperties.ValueType.PosY);
                selectedClip.videoProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.rotationField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.Rot)), VideoProperties.ValueType.Rot);
                selectedClip.videoProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.scaleXField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.ScaleX)), VideoProperties.ValueType.ScaleX);
                selectedClip.videoProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.scaleYField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.ScaleY)), VideoProperties.ValueType.ScaleY);
                selectedClip.videoProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.opacityField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.Opacity)), VideoProperties.ValueType.Opacity);
                selectedClip.videoProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.speedField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.Speed)), VideoProperties.ValueType.Speed);

                selectedClip.isMute = clipEditSpecificAreaScreen.muteAudioCheckbox.isChecked();

                updateClipLayouts();
                updateCurrentClipEnd();
                // TODO: Find a way to specifically build only the edited clip. Not entire timeline
                //  this is just for testing. Resource-consuming asf.
                regeneratingTimelineRenderer();


                clipEditSpecificAreaScreen.keyframeScrollFrame.removeAllViews();
            }
        });
        clipEditSpecificAreaScreen.onOpen.add(() -> {
            clipEditSpecificAreaScreen.totalDurationText.setText(String.valueOf(selectedClip.originalDuration));
            clipEditSpecificAreaScreen.clipNameField.setText(String.valueOf(selectedClip.clipName));
            clipEditSpecificAreaScreen.durationContent.setText(String.valueOf(selectedClip.duration));
            clipEditSpecificAreaScreen.positionXField.setText(String.valueOf(selectedClip.videoProperties.getValue(VideoProperties.ValueType.PosX)));
            clipEditSpecificAreaScreen.positionYField.setText(String.valueOf(selectedClip.videoProperties.getValue(VideoProperties.ValueType.PosY)));
            clipEditSpecificAreaScreen.rotationField.setText(String.valueOf(selectedClip.videoProperties.getValue(VideoProperties.ValueType.Rot)));
            clipEditSpecificAreaScreen.scaleXField.setText(String.valueOf(selectedClip.videoProperties.getValue(VideoProperties.ValueType.ScaleX)));
            clipEditSpecificAreaScreen.scaleYField.setText(String.valueOf(selectedClip.videoProperties.getValue(VideoProperties.ValueType.ScaleY)));
            clipEditSpecificAreaScreen.opacityField.setText(String.valueOf(selectedClip.videoProperties.getValue(VideoProperties.ValueType.Opacity)));
            clipEditSpecificAreaScreen.speedField.setText(String.valueOf(selectedClip.videoProperties.getValue(VideoProperties.ValueType.Speed)));

            clipEditSpecificAreaScreen.muteAudioCheckbox.setChecked(selectedClip.isMute);


            for(Keyframe keyframe : selectedClip.keyframes.keyframes)
            {
                clipEditSpecificAreaScreen.createKeyframeElement(keyframe, () -> {
                    setCurrentTime(keyframe.time);
                });
            }

            clipEditSpecificAreaScreen.clearKeyframeButton.setOnClickListener(v -> {
                if(selectedClip != null)
                {
                    selectedClip.keyframes.keyframes.clear();
                }
            });
        });


        // ===========================       CLIP ZONE       ====================================



        // ===========================       VIDEO PROPERTIES ZONE       ====================================

        videoPropertiesEditSpecificAreaScreen.onClose.add(() -> {
            settings.videoWidth = ParserHelper.TryParse(videoPropertiesEditSpecificAreaScreen.resolutionXField.getText().toString(), 1366);
            settings.videoHeight = ParserHelper.TryParse(videoPropertiesEditSpecificAreaScreen.resolutionYField.getText().toString(), 768);
            settings.crf = ParserHelper.TryParse(videoPropertiesEditSpecificAreaScreen.bitrateField.getText().toString(), 30);



            ViewGroup.LayoutParams previewViewGroupParams = previewViewGroup.getLayoutParams();
            previewViewGroupParams.width = settings.videoWidth;
            previewViewGroupParams.height = settings.videoHeight;
            previewViewGroup.setLayoutParams(previewViewGroupParams);
        });
        videoPropertiesEditSpecificAreaScreen.onOpen.add(() -> {
            videoPropertiesEditSpecificAreaScreen.resolutionXField.setText(String.valueOf(settings.getVideoWidth()));
            videoPropertiesEditSpecificAreaScreen.resolutionYField.setText(String.valueOf(settings.getVideoHeight()));
            videoPropertiesEditSpecificAreaScreen.bitrateField.setText(String.valueOf(settings.getCRF()));
        });


        // ===========================       VIDEO PROPERTIES ZONE       ====================================


    }
    public void setupTimelinePinchAndZoom() {
        scaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                if(currentTimeBeforeScrolling == -1)
                {
                    currentTimeBeforeScrolling = currentTime;
                }

                scaleFactor *= detector.getScaleFactor();

                // Clamp scale factor
                scaleFactor = Math.max(0.05f, Math.min(scaleFactor, 8.0f));

                pixelsPerSecond = (int) (basePixelsPerSecond * scaleFactor);
                updateTimelineZoom();
                return true;
            }
        });

    }

    private void startPlayback() {

        timelineRenderer.startPlayAt(currentTime);
        playbackLoop = new Runnable() {
            @Override
            public void run() {
                if (!isPlaying) return;
                currentTime += frameInterval;

                timelineRenderer.updateTime(currentTime, false);

                int newScrollX = (int) (currentTime * pixelsPerSecond);
                timelineScroll.scrollTo(newScrollX, 0);

                if (currentTime >= timeline.duration) {
                    isPlaying = false;
                    currentTime = 0f;
                    stopPlayback();
                }


                playbackHandler.postDelayed(this, (long)(frameInterval * 1000));
            }
        };
        playbackHandler.post(playbackLoop);
    }

    private void stopPlayback() {
        isPlaying = false;

        playbackHandler.removeCallbacks(playbackLoop);
        playPauseButton.setImageResource(R.drawable.baseline_play_circle_24);

        regeneratingTimelineRenderer();
    }
    private void regeneratingTimelineRenderer()
    {

        LoggingManager.LogToToast(this, "Begin prepare for preview!");
        //refreshPreviewClip();

        // TODO: Tested for dragging back and forth clips. They're doing fine with the extractor SYNC_EXACT
        //  Limit the time of refreshing entire timeline like this.
        //  lmao she said that she absolutely can't :(
        timelineRenderer.buildTimeline(timeline, properties, settings, this, previewViewGroup, textCanvasControllerInfo);
    }
    private void setCurrentTime(float value)
    {
        currentTime = value;
        timelineScroll.scrollTo((int) (currentTime * pixelsPerSecond), 0);
    }

    void setupPreview()
    {
        timelineRenderer = new TimelineRenderer(this);


        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenHeight = metrics.heightPixels;
        editingZone.getLayoutParams().height = (int) (screenHeight * 0.35);
        editingZone.requestLayout();



        regeneratingTimelineRenderer();
    }
    void reloadPreviewClip()
    {
//        mediaPlayer.reset();
//        try {
//            mediaPlayer.setDataSource(IOHelper.CombinePath(properties.getProjectPath(), Constants.DEFAULT_PREVIEW_CLIP_FILENAME));
//        } catch (IOException e) {
//            LoggingManager.LogExceptionToNoteOverlay(this, e);
//        }
//        mediaPlayer.prepareAsync();
//
//        mediaPlayer.setOnPreparedListener(mp -> {
//            playPauseButton.setOnClickListener(v -> {
//                if (mediaPlayer.isPlaying()) {
//                    mediaPlayer.pause();
//                    playPauseButton.setImageResource(R.drawable.baseline_play_circle_24);
//                } else {
//                    mediaPlayer.start();
//                    playheadHandler.post(updatePlayhead); // Start syncing
//                    playPauseButton.setImageResource(R.drawable.baseline_pause_circle_24);
//                }
//            });
//        });
//
//        mediaPlayer.setOnCompletionListener(mp -> {
//            playPauseButton.setImageResource(R.drawable.baseline_play_circle_24);
//            playPauseButton.setOnClickListener(v -> {
//                mediaPlayer.start();
//                playheadHandler.post(updatePlayhead); // Start syncing
//                playPauseButton.setImageResource(R.drawable.baseline_pause_circle_24);
//            });
//        });


    }

    @Override
    public void finish() {
        super.finish();

        timelineRenderer.release();
        Timeline.saveTimeline(this, timeline, properties, settings);
    }
    @Override
    public void onPause() {
        super.onPause();

        timelineRenderer.release();
        Timeline.saveTimeline(this, timeline, properties, settings);
    }

    private Track addNewTrack() {
        Track trackInfo = new Track(trackCount, addNewTrackUi());
        timeline.addTrack(trackInfo);

        return trackInfo;
        // Add a sample clip to the new track
//        addClipToTrack(trackInfo, "/storage/emulated/0/DoubleClips/sample.mp4", 0, Random.Range(1, 6));
    }

    private TrackFrameLayout addNewTrackUi() {
        TrackFrameLayout track = new TrackFrameLayout(this);
        track.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,//ViewGroup.LayoutParams.MATCH_PARENT,
                TRACK_HEIGHT
        ));
        track.setBackgroundColor(Color.parseColor("#222222"));
        track.setPadding(4, 4, 4, 4);

        // 👻 Add spacer to align 0s with center playhead
        View startSpacer = new View(this);
        TrackFrameLayout.LayoutParams spacerParams = new TrackFrameLayout.LayoutParams(
                centerOffset,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        startSpacer.setLayoutParams(spacerParams);
        track.addView(startSpacer); // Add spacer before any clips


        View endSpacer = new View(this);
        TrackFrameLayout.LayoutParams endParams = new TrackFrameLayout.LayoutParams(
                centerOffset,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        endSpacer.setLayoutParams(endParams);
        track.addView(endSpacer); // Add this after all clips


        TextView trackInfoView = new TextView(this);
        LinearLayout.LayoutParams trackInfoParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                TRACK_HEIGHT
        );
        trackInfoView.setLayoutParams(trackInfoParams);
        trackInfoView.setGravity(Gravity.CENTER);
        trackInfoView.setText("Track " + trackCount);



        timelineTracksContainer.removeView(addNewTrackBlankTrackSpacer);
        timelineTracksContainer.addView(track, trackCount);
        timelineTracksContainer.addView(addNewTrackBlankTrackSpacer);
        trackCount++;

        trackInfoLayout.removeView(addNewTrackButton);
        trackInfoLayout.addView(trackInfoView);
        trackInfoLayout.addView(addNewTrackButton);

        handleTrackInteraction(track);

        return track;
    }

    public void addClipToTrack(Track track, Clip data) {
        addClipToTrackUi(track.viewRef, data);

        track.addClip(data);
    }
    private void addClipToTrackUi(TrackFrameLayout trackLayout, Clip data)
    {
        ImageGroupView clipView = new ImageGroupView(this);
        TrackFrameLayout.LayoutParams params = new TrackFrameLayout.LayoutParams(
                (int) (data.duration * pixelsPerSecond),
                TRACK_HEIGHT - 4 // 16
        );
        //params.leftMargin = getTimeInX(data.startTime);
        //params.topMargin = 4; // 8
        clipView.setX(getTimeInX(data.startTime));
        clipView.setLayoutParams(params);
        clipView.setFilledImageBitmap(combineThumbnails(extractThumbnail(this, data.getAbsolutePreviewPath(properties), data)));
        clipView.setTag(data);


        data.registerClipHandle(clipView, this, timelineScroll);




        clipView.post(() -> {
            updateCurrentClipEnd();

            for (Keyframe keyframe : data.keyframes.keyframes) {
                addKeyframeUi(data, keyframe);
            }
        });




        trackLayout.addView(clipView);
        handleClipInteraction(clipView);
    }
    public void addKnotTransition(TransitionClip clip, Clip clipA)
    {
        View knotView = new View(this);
        knotView.setBackgroundColor(Color.RED);
        knotView.setVisibility(View.VISIBLE);

        knotView.setTag(R.id.transition_knot_tag, clip);
        knotView.setTag(R.id.clip_knot_tag, clipA);
        // Position it between clips
        int width = 50;
        int height = 50;

        knotView.setPivotX((float) width /2);
        knotView.setPivotY((float) height /2);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        //params.leftMargin = clipB.getLeft() - (width / 2); // center between clips
        params.topMargin = clipA.viewRef.getTop() + (clipA.viewRef.getHeight() / 2) - (height / 2);
        knotView.setX(clipA.viewRef.getX() + (clipA.duration * pixelsPerSecond) - (width / 2));
        timeline.tracks.get(clip.trackIndex).viewRef.addView(knotView, params);

        handleKnotInteraction(knotView);
    }

    public void addKeyframe(Clip clip, float keyframeTime)
    {
        addKeyframe(clip, new Keyframe(keyframeTime - clip.startTime, new VideoProperties(
                clip.videoProperties.getValue(VideoProperties.ValueType.PosX), clip.videoProperties.getValue(VideoProperties.ValueType.PosY),
                clip.videoProperties.getValue(VideoProperties.ValueType.Rot),
                clip.videoProperties.getValue(VideoProperties.ValueType.ScaleX), clip.videoProperties.getValue(VideoProperties.ValueType.ScaleY),
                clip.videoProperties.getValue(VideoProperties.ValueType.Opacity), clip.videoProperties.getValue(VideoProperties.ValueType.Speed)
        ), EasingType.LINEAR));
    }
    public void addKeyframe(Clip clip, Keyframe keyframe)
    {
        addKeyframeUi(clip, keyframe);

        clip.keyframes.keyframes.add(keyframe);
    }
    public void addKeyframeUi(Clip clip, Keyframe keyframe)
    {
        View knotView = new View(this);
        knotView.setBackgroundColor(Color.BLUE);
        knotView.setVisibility(View.VISIBLE);

        knotView.setTag(R.id.keyframe_knot_tag, keyframe);
        knotView.setTag(R.id.clip_knot_tag, clip);
        // Position it between clips
        int width = 12;
        int height = clip.viewRef.getHeight();

        knotView.setPivotX((float) width /2);
        knotView.setPivotY((float) height /2);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        //params.leftMargin = getCurrentTimeInX();
        //params.topMargin = clip.viewRef.getTop() + (clip.viewRef.getHeight() / 2);
        params.topMargin = clip.viewRef.getTop() + (clip.viewRef.getHeight() / 2) - (height / 2);
        knotView.setX(getTimeInX(clip.startTime + keyframe.time));
        timeline.tracks.get(clip.trackIndex).viewRef.addView(knotView, params);



        handleKeyframeInteraction(knotView);
    }
    public void revalidationClipView(Clip data)
    {
        ImageGroupView clipView = data.viewRef;
        clipView.getLayoutParams().width = (int) (data.duration * pixelsPerSecond);


    }
    private void handleEditZoneInteraction(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_MOVE:
                    if(isPlaying)
                        stopPlayback();
                    break;
                // ACTION_UP is the action that invoke only if we clicked
                // that's mean its invoke if we didn't ACTION_MOVE
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    break;
                // ACTION_CANCEL is when you accidentally click something and
                // drag it somewhere so it doesn't recognize that click anymore
                case MotionEvent.ACTION_CANCEL:
                    break;
            }
            return false;
        });
    }
    private void handleKeyframeInteraction(View view) {
        view.setOnClickListener(v -> {
            if(view.getTag(R.id.keyframe_knot_tag) instanceof Keyframe)
            {
                Keyframe data = (Keyframe) view.getTag(R.id.keyframe_knot_tag);
            }

        });
    }
    private void handleKnotInteraction(View view) {
        view.setOnClickListener(v -> {
            if(view.getTag(R.id.transition_knot_tag) instanceof TransitionClip)
            {
                selectingKnot((TransitionClip) view.getTag(R.id.transition_knot_tag));
                editingSpecific(ClipType.TRANSITION);
            }

        });
    }
    private void handleTrackInteraction(View view) {
        view.setOnClickListener(v -> {
            if(view instanceof TrackFrameLayout)
            {
                selectingTrack(((TrackFrameLayout) view).trackInfo);
            }
        });

        view.setOnTouchListener((v, event) -> {
            scaleDetector.onTouchEvent(event);
            switch (event.getAction())
            {
                case MotionEvent.ACTION_MOVE:
                    if(event.getPointerCount() == 2) {
                        timelineScroll.requestDisallowInterceptTouchEvent(true);
                    }
                    break;
                    // ACTION_UP is the action that invoke only if we clicked
                    // that's mean its invoke if we didn't ACTION_MOVE
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    currentTimeBeforeScrolling = -1;
                    timelineScroll.requestDisallowInterceptTouchEvent(false);
                    break;
                    // ACTION_CANCEL is when you accidentally click something and
                    // drag it somewhere so it doesn't recognize that click anymore
                case MotionEvent.ACTION_CANCEL:
                    currentTimeBeforeScrolling = -1;
                    timelineScroll.requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return true;
        });

    }
    private void handleClipInteraction(View view) {
        DragContext dragContext = new DragContext();
        view.setOnClickListener(v -> {
            Clip clip = (Clip) view.getTag(); // Already stored

            selectingClip(clip);
        });


        view.setOnLongClickListener(v -> {

            Clip clip = (Clip) view.getTag(); // Already stored
            Track track = timeline.tracks.get(clip.trackIndex);



            timelineScroll.requestDisallowInterceptTouchEvent(true);

            // 👻 Create ghost
            ImageGroupView ghost = new ImageGroupView(v.getContext());
            ghost.setLayoutParams(new TrackFrameLayout.LayoutParams(v.getWidth(), v.getHeight()));
            ghost.setFilledImageBitmap(((ImageGroupView)v).getFilledImageBitmap());
            ghost.setAlpha(0.5f);




            track.viewRef.addView(ghost);
            ghost.setX(view.getX());
            ghost.setY(view.getY());

            v.setVisibility(View.INVISIBLE); // Hide original

            dragContext.ghost = ghost;
            dragContext.currentTrack = track;
            dragContext.clip = clip;

            return true;
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            float dX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Clip data = (Clip) v.getTag();


                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        //dY = v.getY() - event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (dragContext.ghost != null) {
                            float newX = event.getRawX() + dX;
                            if (newX < centerOffset) newX = centerOffset; // ⛔ Prevent going past 0s

                            float ghostWidth = dragContext.ghost.getWidth();
                            float ghostStart = newX;
                            float ghostEnd = newX + ghostWidth;


                            // 🧲 Check for snapping
                            // Snap the playhead
                            float playheadX = (timelineScroll.getScrollX() + centerOffset) - 2;

                            if (Math.abs(ghostStart - playheadX) < Constants.TRACK_CLIPS_SNAP_THRESHOLD_PIXEL) {
                                newX = playheadX;
                            }
                            if (Math.abs(ghostEnd - playheadX) < Constants.TRACK_CLIPS_SNAP_THRESHOLD_PIXEL) {
                                newX = playheadX - ghostWidth;
                            }



                            // Snap the other track
                            for (int i = 0; i < dragContext.currentTrack.viewRef.getChildCount(); i++) {
                                View other = dragContext.currentTrack.viewRef.getChildAt(i);
                                if (other == dragContext.ghost || other == v) continue;

                                float otherStart = other.getX();
                                float otherEnd = other.getX() + other.getWidth();

                                // Snap ghost start to other end
                                if (Math.abs(ghostStart - otherEnd) <= Constants.TRACK_CLIPS_SNAP_THRESHOLD_PIXEL) {
                                    newX = otherEnd;
                                    break;
                                }

                                // Snap ghost end to other start
                                if (Math.abs(ghostEnd - otherStart) <= Constants.TRACK_CLIPS_SNAP_THRESHOLD_PIXEL) {
                                    newX = otherStart - ghostWidth;
                                    break;
                                }

                                // Optional: Snap start-to-start or end-to-end
                                // Todo: Pending removal as no sense of letting clips overlapping each other in the same track in the near future.
                                if (Math.abs(ghostStart - otherStart) <= Constants.TRACK_CLIPS_SNAP_THRESHOLD_PIXEL) {
                                    newX = otherStart;
                                    break;
                                }
                                if (Math.abs(ghostEnd - otherEnd) <= Constants.TRACK_CLIPS_SNAP_THRESHOLD_PIXEL) {
                                    newX = otherEnd - ghostWidth;
                                    break;
                                }
                            }
                            dragContext.ghost.setX(newX);

                            // 🔍 Detect track under finger
                            Track targetTrack = null;
                            float touchY = event.getRawY();


                            for (Track track : timeline.tracks) {
                                TrackFrameLayout trackRef = track.viewRef;

                                int[] loc = new int[2];
                                trackRef.getLocationOnScreen(loc);
                                float top = loc[1];
                                float bottom = top + trackRef.getHeight();
                                // 🧲 Move ghost to new track if needed
                                if (touchY >= top && touchY <= bottom && track != dragContext.currentTrack) {
                                    dragContext.currentTrack.viewRef.removeView(dragContext.ghost);
                                    track.viewRef.addView(dragContext.ghost);
                                    dragContext.ghost.setY(4);
                                    dragContext.currentTrack = track;
                                    break;
                                }
                            }
                            return true;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        timelineScroll.requestDisallowInterceptTouchEvent(false);

                        if (dragContext.ghost != null) {
                            float finalX = dragContext.ghost.getX();
                            if (finalX < centerOffset) finalX = centerOffset; // ⛔ Clamp again for safety


                            float finalX1 = finalX;

                            v.post(() -> {
                                // Move original to new track and position
                                ViewGroup oldParent = (ViewGroup) v.getParent();
                                oldParent.removeView(v);

                                // Add to new track
                                dragContext.currentTrack.viewRef.addView(v);
                                v.setX(finalX1);
                                v.setVisibility(View.VISIBLE);

                                // Update metadata
                                float newStartTime = (finalX1 - centerOffset) / pixelsPerSecond;
                                dragContext.clip.startTime = Math.max(0, newStartTime); // Clamp to 0
                                timeline.tracks.get(dragContext.clip.trackIndex).removeClip(dragContext.clip);
                                dragContext.clip.trackIndex = dragContext.currentTrack.trackIndex;
                                timeline.tracks.get(dragContext.clip.trackIndex).addClip((dragContext.clip));


                                updateCurrentClipEnd();


                                // Remove ghost
                                dragContext.currentTrack.viewRef.removeView(dragContext.ghost);
                                dragContext.ghost = null;




                                timeline.tracks.get(dragContext.clip.trackIndex).sortClips();
                            });

                        }
                        break;
                }
                return false;
            }
        });
    }



    void updateCurrentClipEnd()
    {
        updateCurrentClipEnd(true);
    }
    void updateCurrentClipEnd(boolean updateRuler)
    {
        int newTimelineEnd = 0;
        // 🧠 Recalculate max right edge of all clips in all tracks
        for (Track trackCpn : timeline.tracks) {
            for (int i = 0; i < trackCpn.viewRef.getChildCount(); i++) {
                View child = trackCpn.viewRef.getChildAt(i);
                if (child.getTag() != null && child.getTag() instanceof Clip) { // It's a clip
                    int right = (int) (child.getX() + child.getWidth());
                    if (right > newTimelineEnd) newTimelineEnd = right;
                }
            }
        }
        currentTimelineEnd = newTimelineEnd;

        // Get time (- centerOffset mean remove the start spacer)
        float totalSeconds = (currentTimelineEnd - centerOffset) / (float) pixelsPerSecond;
        durationTimePosText.post(() -> durationTimePosText.setText(DateHelper.convertTimestampToMMSSFormat((long) (totalSeconds * 1000L))));
        timeline.duration = totalSeconds;
        if(updateRuler)
            updateRuler(totalSeconds, currentRulerInterval);
        // 🧱 Expand end spacer
        for (Track trackCpn : timeline.tracks) {
            updateEndSpacer(trackCpn);
            updateTrackWidth(trackCpn);
            updateTransitionKnot(trackCpn);
        }



        //refreshPreviewClip();
    }
    void updateTransitionKnot(Track track)
    {

        //TODO: Use endTransition from clipA and toggle visibility and on/off of the transition
        ArrayList<Clip> snappedClipStart = new ArrayList<>();
        ArrayList<Clip> snappedClipEnd = new ArrayList<>();


//                              ArrayList<View> sortedTrackClips = IntStream.range(0, dragContext.currentTrack.viewRef.getChildCount()).mapToObj(i -> dragContext.currentTrack.viewRef.getChildAt(i)).filter(clipView -> clipView.getTag() instanceof Clip).collect(Collectors.toCollection(ArrayList::new));

        track.clearTransition();

        // Check for snapped track
        for (int i = 1; i < track.clips.size(); i++) {
            Clip at = track.clips.get(i - 1);
            Clip other = track.clips.get(i);
            // Snap ghost start to other end
            if (Math.abs(at.startTime - (other.startTime + other.duration)) <= Constants.TRACK_CLIPS_SNAP_THRESHOLD_PIXEL / pixelsPerSecond) {
                snappedClipEnd.add(at);
                snappedClipStart.add(other);
            }
            // Snap ghost end to other start
            if (Math.abs((at.startTime + at.duration) - other.startTime) <= Constants.TRACK_CLIPS_SNAP_THRESHOLD_PIXEL / pixelsPerSecond) {
                snappedClipEnd.add(other);
                snappedClipStart.add(at);
            }
        }

        for (int i = 0; i < snappedClipStart.size(); i++) {
            addTransitionBridge(snappedClipStart.get(i), snappedClipEnd.get(i), 0.2f);
        }
    }
    void updateTrackWidth(Track track)
    {
        track.viewRef.setLayoutParams(new LinearLayout.LayoutParams(
                centerOffset + currentTimelineEnd, // End spacer = centerOffset
                TRACK_HEIGHT
        ));
    }
    private void updateEndSpacer(Track track) {
        View existingSpacer = track.viewRef.findViewWithTag("end_spacer");
        if (existingSpacer != null) {
            track.viewRef.removeView(existingSpacer);
        }

        View endSpacer = new View(this);
        endSpacer.setTag("end_spacer");
        TrackFrameLayout.LayoutParams params = new TrackFrameLayout.LayoutParams(
                centerOffset, // Always half screen
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        endSpacer.setLayoutParams(params);
        track.viewRef.addView(endSpacer);
    }
    private void updateRuler(float totalSeconds, float interval) {
        rulerContainer.removeAllViews();



        // 👻 Add spacer to align 0s with center playhead
        View startSpacerRuler = new View(this);
        LinearLayout.LayoutParams spacerRulerParams = new LinearLayout.LayoutParams(
                centerOffset,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        startSpacerRuler.setLayoutParams(spacerRulerParams);
        rulerContainer.addView(startSpacerRuler); // Add spacer before any clips

        for (float i = 0; i <= totalSeconds; i += interval) {
            TextView tick = new TextView(this);
            tick.setText(StringFormatHelper.smartRound(i, 1, true) + "s");
            //tick.setTextColor(Color.BLACK);
            tick.setTextSize(12);
            tick.setGravity(Gravity.START | Gravity.BOTTOM);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) (pixelsPerSecond * interval), ViewGroup.LayoutParams.MATCH_PARENT
            );
            tick.setLayoutParams(params);
            rulerContainer.addView(tick);
        }

        // Add end spacer to ruler
        View rulerEndSpacer = new View(this);
        rulerEndSpacer.setLayoutParams(new LinearLayout.LayoutParams(
                centerOffset, ViewGroup.LayoutParams.MATCH_PARENT
        ));
        rulerContainer.addView(rulerEndSpacer);
    }
    float currentRulerInterval = 1f;
    float changedRulerInterval = 1f;

    private void updateRulerEfficiently() {
        if(pixelsPerSecond < 25 && pixelsPerSecond > 10)
        {
            changedRulerInterval = 8f;
        }
        if(pixelsPerSecond < 50 && pixelsPerSecond > 25)
        {
            changedRulerInterval = 4f;
        }
        if(pixelsPerSecond < 100 && pixelsPerSecond > 50)
        {
            changedRulerInterval = 2f;
        }
        if(pixelsPerSecond < 200 && pixelsPerSecond > 100)
        {
            changedRulerInterval = 1f;
        }
        if(pixelsPerSecond < 500 && pixelsPerSecond > 200)
        {
            changedRulerInterval = 0.5f;
        }
        if(pixelsPerSecond < 1000 && pixelsPerSecond > 500)
        {
            changedRulerInterval = 0.2f;
        }
        if(pixelsPerSecond < 2000 && pixelsPerSecond > 1000)
        {
            changedRulerInterval = 0.1f;
        }
        if(pixelsPerSecond < 5000 && pixelsPerSecond > 2000)
        {
            changedRulerInterval = 0.05f;
        }
        // Recently updated. Above are tested.
        if(pixelsPerSecond < 10000 && pixelsPerSecond > 5000)
        {
            changedRulerInterval = 0.02f;
        }
        if(pixelsPerSecond < 20000 && pixelsPerSecond > 10000)
        {
            changedRulerInterval = 0.01f;
        }
        if(pixelsPerSecond < 50000 && pixelsPerSecond > 20000)
        {
            changedRulerInterval = 0.005f;
        }


        for (int i = 0; i < rulerContainer.getChildCount(); i++) {
            View tick = rulerContainer.getChildAt(i);
            if(!(tick instanceof TextView)) continue;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tick.getLayoutParams();
            params.width = (int) (pixelsPerSecond * changedRulerInterval); // or pixelsPerSecond / 2 for 0.5s ticks
            tick.setLayoutParams(params);
        }
    }

    private void updateClipLayouts() {
        for (Track track : timeline.tracks) {
            for (int i = 0; i < track.viewRef.getChildCount(); i++) {
                View clip = track.viewRef.getChildAt(i);
                if (clip.getTag() instanceof Clip) {
                    Clip data = (Clip) clip.getTag();

                    TrackFrameLayout.LayoutParams params = new TrackFrameLayout.LayoutParams(
                            (int) (data.duration * pixelsPerSecond),
                            TRACK_HEIGHT - 4);
                    //params.leftMargin = (int) (centerOffset + data.startTime * pixelsPerSecond);
                    //params.topMargin = 4;\
                    clip.setX(getTimeInX(data.startTime));
                    clip.setLayoutParams(params);
                }
                if(clip.getTag(R.id.transition_knot_tag) instanceof TransitionClip) {
                    TransitionClip data = (TransitionClip) clip.getTag(R.id.transition_knot_tag);
                    //clip.setX(getTimeInX(data.time));
                }
                if(clip.getTag(R.id.keyframe_knot_tag) instanceof Keyframe) {
                    Keyframe data = (Keyframe) clip.getTag(R.id.keyframe_knot_tag);
                    Clip data2 = (Clip) clip.getTag(R.id.clip_knot_tag);

                    clip.setX(getTimeInX(data2.startTime + data.time));
                }
            }
        }
    }
    private void updateEndSpacers()
    {
        for (Track track : timeline.tracks) {
            updateEndSpacer(track);
        }
    }

    private void updateTimelineZoom() {
        updateRulerEfficiently();
        boolean rulerChanged = false;
        if(changedRulerInterval != currentRulerInterval)
        {
            currentRulerInterval = changedRulerInterval;
            rulerChanged = true;
            updateRuler(timeline.duration, currentRulerInterval);
        }

        updateClipLayouts();
        updateCurrentClipEnd(false);
        //updateEndSpacers(); // updateCurrentClipEnd did it
        //updateEndSpacers(); // Optional: recalculate based on new width

        // Let playhead froze when scrolling
        setCurrentTime(currentTimeBeforeScrolling);


        //Todo: Still resource-intensive, recode it by cache the created thumbnail, then insert/remove accordingly.
        if(rulerChanged)
        {
            for (Track track : timeline.tracks) {
                for (Clip clip : track.clips) {
                    //clip.viewRef.setFilledImageBitmap(combineThumbnails(extractThumbnail(this, clip.filePath, clip.type)));
                }
            }
        }
    }
    private void addTransitionBridge(Clip clipA, Clip clipB, float transitionDuration)
    {
        //if (clipA.startTime + clipA.duration == clipB.startTime)
        {
            TransitionClip transition = new TransitionClip();
            transition.trackIndex = clipA.trackIndex;
            transition.fromClip = clipA;
            transition.toClip = clipB;
            transition.startTime = clipB.startTime - transitionDuration / 2;
            transition.duration = transitionDuration;
            transition.effect = new EffectTemplate("fade", transitionDuration, transition.startTime);
            transition.mode = TransitionClip.TransitionMode.OVERLAP;
            timeline.tracks.get(clipA.trackIndex).addTransition(transition);

            addTransitionBridgeUi(transition, clipA);
        }

    }
    private void addTransitionBridgeUi(TransitionClip transitionClip, Clip clip)
    {
        addKnotTransition(transitionClip, clip);
    }





    private void selectingClip(Clip selectedClip)
    {
        if(isClipSelectMultiple)
        {
            this.selectedClip = null;
            if(selectedClips.contains(selectedClip))
            {
                selectedClips.remove(selectedClip);
                selectedClip.deselect();
            }
            else
            {
                selectedClips.add(selectedClip);
                selectedClip.select();
                toolbarClips.setVisibility(View.VISIBLE);
            }
        }
        else {
            deselectingClip();

            if(this.selectedClip != null && this.selectedClip == selectedClip){
                this.selectedClip = null;
            }
            else {
                selectingTrack(timeline.getTrackFromClip(selectedClip));

                selectedClip.select();
                this.selectedClip = selectedClip;
                toolbarClips.setVisibility(View.VISIBLE);
            }
            if(currentTime < selectedClip.startTime)
                setCurrentTime(selectedClip.startTime);
        }

    }
    private void selectingTrack(Track selectedTrack)
    {
        deselectingTrack();

        if(selectedTrack == null) {
            deselectingClip();
            this.selectedClip = null;
            return;
        }
        if(this.selectedTrack != null && this.selectedTrack == selectedTrack){
            this.selectedTrack = null;
            deselectingClip();
            this.selectedClip = null;
        }
        else {
            deselectingClip();
            this.selectedClip = null;
            selectedTrack.select();
            this.selectedTrack = selectedTrack;
            toolbarTrack.setVisibility(View.VISIBLE);
        }
    }
    private void selectingKnot(TransitionClip selectedKnot)
    {
        this.selectedKnot = selectedKnot;
    }
    private void deselectingClip()
    {
        toolbarClips.setVisibility(View.GONE);
        if(isClipSelectMultiple)
            selectedClips.clear();

        for (Track track : timeline.tracks) {
            for (Clip clip : track.clips) {
                clip.deselect();
            }
        }
    }
    private void deselectingTrack()
    {
        toolbarTrack.setVisibility(View.GONE);
        for (Track track : timeline.tracks) {
            track.deselect();
        }
    }




    private void refreshPreviewClip()
    {
        //FFmpegEdit.generatePreviewVideo(this, timeline, settings, properties, this::reloadPreviewClip);
    }


    // Native Android user only
    private static List<Bitmap> extractThumbnail(Context context, String filePath, Clip clip)
    {
        return extractThumbnail(context, filePath, clip, -1);
    }
    private static List<Bitmap> extractThumbnail(Context context, String filePath, Clip clip, int frameCountOverride)
    {
        List<Bitmap> thumbnails = new ArrayList<>();
        if(clip.type == null)
            clip.type = ClipType.EFFECT;
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_launcher_background, null);
        switch (clip.type)
        {
            case VIDEO:
                try {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(filePath);

                    long durationMs = (long) (clip.duration * 1000);
                    int frameCount = frameCountOverride;
                    // Every 1s will have a thumbnail
                    // Math.ceil will make sure 0.1 will be 1, and clamp to 1 using Math.max to make sure not 0 or below
                    // TODO: Convert it to other thread in order to prevent lag
                    if(frameCountOverride == -1) frameCount = (int) Math.max(1, Math.ceil((double) durationMs / 1000));
                    int originalWidth = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)));
                    int originalHeight = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));

                    int desiredWidth = originalWidth / Constants.SAMPLE_SIZE_PREVIEW_CLIP;
                    int desiredHeight = originalHeight / Constants.SAMPLE_SIZE_PREVIEW_CLIP;

                    for (int i = 0; i < frameCount; i++) {
                        long timeUs = (long) (clip.startClipTrim * 1_000_000) +  ((durationMs * 1000L * i) / frameCount);
                        Bitmap frame;
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                            frame = retriever.getScaledFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, desiredWidth, desiredHeight);
                        }
                        else {
                            frame = Bitmap.createScaledBitmap(
                                    Objects.requireNonNull(retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)),
                                    desiredWidth, desiredHeight, true);

                        }
                        if (frame != null) {
                            thumbnails.add(frame);
                        }
                    }
                    retriever.release();
                    retriever.close();
                } catch (Exception e) {
                    LoggingManager.LogExceptionToNoteOverlay(context, e);
                }
                break;
            case IMAGE:
                thumbnails.add(IOImageHelper.LoadFileAsPNGImage(context, filePath, Constants.SAMPLE_SIZE_PREVIEW_CLIP));
                break;
            case TEXT:
                drawable.setColorFilter(0xAAFF0000, PorterDuff.Mode.SRC_ATOP);
                thumbnails.add(ImageHelper.createBitmapFromDrawable(drawable));
                break;
            case AUDIO:

                drawable.setColorFilter(0xAA0000FF, PorterDuff.Mode.SRC_ATOP);
                thumbnails.add(ImageHelper.createBitmapFromDrawable(drawable));

                //TODO: Failed...Visualizer isn't good
//                Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//
//                PlayerVisualizerUtils.drawVisualizer(context, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), IOHelper.readFromFileAsRaw(context, filePath), new Canvas(bitmap));
//
//                thumbnails.add(bitmap);


                break;
            case EFFECT:
                drawable.setColorFilter(0xAAFFFF00, PorterDuff.Mode.SRC_ATOP);
                thumbnails.add(ImageHelper.createBitmapFromDrawable(drawable));
                break;

        }


        return thumbnails;

    }

    private static Bitmap combineThumbnails(List<Bitmap> frames)
    {
        int totalWidth = frames.size() * frames.get(0).getWidth();
        int height = frames.get(0).getHeight();

        Bitmap combined = Bitmap.createBitmap(totalWidth, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(combined);

        int x = 0;
        for (Bitmap bmp : frames) {
            canvas.drawBitmap(bmp, x, 0, null);
            x += bmp.getWidth();
        }

        return combined;
    }




    // End Native Android




    public static int previewToRenderConversionX(float previewX, float renderResolutionX)
    {
        return (int) ((previewX / Math.min(previewAvailableWidth, renderResolutionX)) * renderResolutionX);
    }
    public static int previewToRenderConversionY(float previewY, float renderResolutionY)
    {
        return (int) ((previewY / Math.min(previewAvailableHeight, renderResolutionY)) * renderResolutionY);
    }

    public static int renderToPreviewConversionX(float renderX, float renderResolutionX)
    {
        return (int) ((renderX * Math.min(previewAvailableWidth, renderResolutionX)) / renderResolutionX);
    }
    public static int renderToPreviewConversionY(float renderY, float renderResolutionY)
    {
        return (int) ((renderY * Math.min(previewAvailableHeight, renderResolutionY)) / renderResolutionY);
    }

    // TODO: Using the same ratio system like below because multiplication and division is in the same order, no plus and subtract
    //  the matrix of the preview clip are not using the previewAvailable ratio system yet, so 1366 width
    //  in the 1080px screen the movement will be jittered

    public static float previewToRenderConversionScalingX(float clipScaleX, float renderResolutionX)
    {
        return clipScaleX / getRenderRatio(previewAvailableWidth, renderResolutionX);
    }
    public static float previewToRenderConversionScalingY(float clipScaleY, float renderResolutionY)
    {
        return clipScaleY / getRenderRatio(previewAvailableHeight, renderResolutionY);
    }

    public static float renderToPreviewConversionScalingX(float clipScaleX, float renderResolutionX)
    {
        return clipScaleX * getRenderRatio(previewAvailableWidth, renderResolutionX);
    }
    public static float renderToPreviewConversionScalingY(float clipScaleY, float renderResolutionY)
    {
        return clipScaleY * getRenderRatio(previewAvailableHeight, renderResolutionY);
    }

    public static float getRenderRatio(float previewAvailable, float renderResolution)
    {
        return Math.min(previewAvailable, renderResolution) / renderResolution;
    }

    // TODO: For the scaling. When passing the previewAvailableWidth/Height. We get
    //  the previewAvailable / renderResolution for the ratio. And we divide the render scale by the ratio to
    //  get the preview












    public static class Timeline implements Serializable {
        @Expose
        public List<Track> tracks = new ArrayList<>();
        @Expose
        public float duration;

        public void addTrack(Track track) {
            tracks.add(track);
        }

        public void removeTrack(Track track) {
            tracks.remove(track);
        }

        public List<Clip> getClipAtCurrentTime(float playheadTime) {
            List<Clip> clips = new ArrayList<>();
            for (Track track : tracks) {
                for (Clip clip : track.clips) {
                    if (playheadTime >= clip.startTime && playheadTime < clip.startTime + clip.duration) {
                        clips.add(clip);
                    }
                }
            }
            return clips; // No clip at this time
        }
        public Track getTrackFromClip(Clip selectedClip) {
            for (Track track : tracks) {
                if(track.clips.contains(selectedClip))
                    return track;
            }
            return null;
        }



        public static void saveTimeline(Context context, Timeline timeline, MainActivity.ProjectData data, VideoSettings settings)
        {
            float max = 0;
            for (Track trackCpn : timeline.tracks) {
                float endTime = trackCpn.getTrackEndTime();
                if(endTime > max) max = endTime;

                trackCpn.sortClips();
            }
            timeline.duration = max;

            Clip nearestClip = null;
            for (Track trackCpn : timeline.tracks) {
                for (Clip c : trackCpn.clips)
                {
                    nearestClip = c;
                    break;
                }
                if(nearestClip != null) break;
            }
            if(nearestClip != null)
                IOImageHelper.SaveFileAsPNGImage(context, IOHelper.CombinePath(data.getProjectPath(), "preview.png"), extractThumbnail(context, nearestClip.getAbsolutePreviewPath(data), nearestClip, 1).get(0), 25);

            data.setProjectTimestamp(new Date().getTime());
            data.setProjectDuration((long) (timeline.duration * 1000));
            data.setProjectSize(IOHelper.getFileSize(context, data.getProjectPath()));


            String jsonTimeline = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(timeline); // Save


            data.savePropertiesAtProject(context);
            settings.saveSettings(context, data);
            IOHelper.writeToFile(context, IOHelper.CombinePath(data.getProjectPath(), Constants.DEFAULT_TIMELINE_FILENAME), jsonTimeline);


        }
        public static Timeline loadRawTimeline(Context context, MainActivity.ProjectData data)
        {
            String json = IOHelper.readFromFile(context, IOHelper.CombinePath(data.getProjectPath(), Constants.DEFAULT_TIMELINE_FILENAME));
            return new Gson().fromJson(json, Timeline.class);
        }
        public static Timeline loadTimeline(Context context, EditingActivity instance, MainActivity.ProjectData data)
        {
            return loadTimeline(context, instance, loadRawTimeline(context, data));
        }
        public static Timeline loadTimeline(Context context, EditingActivity instance, Timeline timeline)
        {
            if(timeline == null) return new Timeline();
            for (Track track : timeline.tracks) {
                track.viewRef = instance.addNewTrackUi();
                track.viewRef.trackInfo = track;

                for (Clip clip : track.clips) {
                    clip.filterNullAfterLoad();
                    instance.addClipToTrackUi(track.viewRef, clip);
                }
            }

            return timeline;
        }
    }

    public static class Track implements Serializable {
        @Expose
        public int trackIndex;
        @Expose
        public List<Clip> clips = new ArrayList<>();
        @Expose
        public List<TransitionClip> transitions = new ArrayList<>();

        //Not serializing
        public transient TrackFrameLayout viewRef;



        public Track(int trackIndex, TrackFrameLayout viewRef) {
            this.trackIndex = trackIndex;
            this.viewRef = viewRef;
        }

        public void addClip(Clip clip) {
            clips.add(clip);
        }

        public void removeClip(Clip clip) {
            clips.remove(clip);
        }
        public void sortClips()
        {
            clips.sort((o1, o2) -> (Float.compare(o1.startTime, o2.startTime)));
        }

        public float getTrackEndTime() {
            float max = 0f;
            for (Clip clip : clips) {
                float end = clip.startTime + clip.duration;
                if (end > max) max = end;
            }
            return max;
        }

        public void select() {
            viewRef.setBackgroundColor(0xFFAAAAAA);
        }
        public void deselect() {
            viewRef.setBackgroundColor(0xFF222222);
        }

        public void addTransition(TransitionClip transition) {
            transitions.add(transition);
        }
        public void removeTransitionUi(TransitionClip transition) {

            for (int i = 0; i < viewRef.getChildCount(); i++) {
                View targetView = viewRef.getChildAt(i);
                System.err.println(i);

                if (targetView != null && targetView.getTag(R.id.transition_knot_tag) != null) {
                    viewRef.removeView(targetView);
                    break;
                }
            }
        }
        public void removeTransition(TransitionClip transition) {
            transitions.remove(transition);
            removeTransitionUi(transition);
        }
        public void clearTransition() {
            for (TransitionClip clip : transitions) {
                removeTransitionUi(clip);
            }
            transitions.clear();
        }
        public void removeTransitionByClip(Clip clip) {
            List<TransitionClip> removalQueue = new ArrayList<>();
            for (TransitionClip transitionClip : transitions) {
                if(transitionClip.fromClip == clip || transitionClip.toClip == clip)
                    removalQueue.add(transitionClip);
                if(removalQueue.size() == 2) break;
            }

            for (TransitionClip clip2 : removalQueue) {
                removeTransitionUi(clip2);
            }
            transitions.removeAll(removalQueue);
        }

        public void delete(Timeline timeline, ViewGroup trackContainer, ViewGroup trackInfo, EditingActivity activity) {
            activity.deselectingTrack();

            timeline.removeTrack(timeline.tracks.get(trackIndex));

            // Lower the higher indexes track by 1 to fill up the remove one.
            // When removed, trackIndex element become the next element
            List<Track> tracks = timeline.tracks;
            for (int i = trackIndex; i < tracks.size(); i++) {
                Track higherTrack = tracks.get(i);
                higherTrack.trackIndex--;
            }

            trackContainer.removeView(viewRef);

            // Since the track #n is following the pattern, we just need to delete the last track # text and it does the job
            // -1 for the count to index, like count is 4 but index should be 3
            // -1 for the index for the button
            trackInfo.removeView(trackInfo.getChildAt(trackInfo.getChildCount() - 2));

            // Decrease the trackIndex from the global scope
            activity.trackCount--;
        }
    }

    public static class Clip implements Serializable {
        @Expose
        public ClipType type;
        @Expose
        public String clipName;
        @Expose
        public float startTime; // in seconds
        @Expose
        public float duration;  // in seconds
        @Expose
        public float startClipTrim; // in seconds
        @Expose
        public float endClipTrim; // in seconds
        @Expose
        public float originalDuration;  // in seconds
        @Expose
        public int trackIndex;

        @Expose
        public int width;
        @Expose
        public int height;

        @Expose
        public VideoProperties videoProperties;


        @Expose
        public AnimatedProperty keyframes = new AnimatedProperty();

        // FX support (for EFFECT type)
        @Expose
        public EffectTemplate effect; // for EFFECT type
        @Expose
        public String textContent;    // for TEXT type
        @Expose
        public float fontSize;    // for TEXT type


        // TODO: End transition for clip later that attached to the end of this clip.
        //  this will make more sense than begin Transition as no one would merge the beginning of the clip and call it a transition.
        //  Save this endTransition alongside with this clip.
        @Expose
        public TransitionClip endTransition = null;

        /**
         * For VIDEO type.
         * When import, check whether the clip has audio or not
         * When export, decide to include audio stream or not to prevent "match no stream" ffmpeg error.
         */
        @Expose
        public boolean isVideoHasAudio;    // for VIDEO type

        /**
         * For VIDEO type.
         * Can use this to mute video when export
         */
        @Expose
        public boolean isMute;    // for VIDEO type


        //Not serializing
        public transient View leftHandle, rightHandle;
        public transient ImageGroupView viewRef;


        public Clip(String clipName, float startTime, float duration, int trackIndex, ClipType type, boolean isVideoHasAudio, int width, int height) {
            this.clipName = clipName;
            this.startTime = startTime;
            this.startClipTrim = 0;
            this.endClipTrim = 0;
            this.duration = duration;
            this.originalDuration = duration;
            this.trackIndex = trackIndex;
            this.type = type;
            this.isVideoHasAudio = isVideoHasAudio;

            this.width = width;
            this.height = height;

            this.videoProperties = new VideoProperties(0, 0, 0, 1, 1, 1, 1);
            this.isMute = false;
        }

        public Clip(Clip clip) {
            this.clipName = clip.clipName;
            this.startTime = clip.startTime;
            this.startClipTrim = clip.startClipTrim;
            this.endClipTrim = clip.endClipTrim;
            this.duration = clip.duration;
            this.originalDuration = clip.originalDuration;
            this.trackIndex = clip.trackIndex;
            this.type = clip.type;
            this.isVideoHasAudio = clip.isVideoHasAudio;

            this.width = clip.width;
            this.height = clip.height;

            this.videoProperties = new VideoProperties(clip.videoProperties);
            this.isMute = clip.isMute;


            if(clip.type == ClipType.TEXT)
            {
                this.textContent = clip.textContent;
                this.fontSize = clip.fontSize;
            }
            if(clip.type == ClipType.EFFECT)
            {
                this.effect = clip.effect;
            }
        }


        /**
         * Full transfer to this new Clip.
         * Safe enough to filter the null when the new update rolled out.
         * @param loadedClip Clip that loaded from JSON and are potentially contains null variables
         */
        public void safeLoad(Clip loadedClip)
        {
            // ? WTF am I writing this for. Make no sense!
        }


        public void filterNullAfterLoad()
        {
            if(type == null) type = ClipType.VIDEO;
            if(videoProperties == null) videoProperties = new VideoProperties();
            if(keyframes == null) keyframes = new AnimatedProperty();
            if(keyframes.keyframes == null) keyframes.keyframes = new ArrayList<>();
        }


        public void registerClipHandle(ImageGroupView clipView, EditingActivity activity, HorizontalScrollView timelineScroll) {
            viewRef = clipView;

            leftHandle = new View(clipView.getContext());
            leftHandle.setBackgroundColor(Color.WHITE);
            RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(35, ViewGroup.LayoutParams.MATCH_PARENT);
            leftParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            //leftParams.setMarginStart(-35);   for rendering the part outside of clip to match Capcut
            leftHandle.setLayoutParams(leftParams);

            rightHandle = new View(clipView.getContext());
            rightHandle.setBackgroundColor(Color.WHITE);
            RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(35, ViewGroup.LayoutParams.MATCH_PARENT);
            rightParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            //rightParams.setMarginEnd(-35);   for rendering the part outside of clip to match Capcut
            rightHandle.setLayoutParams(rightParams);

            leftHandle.setOnTouchListener(
                    new View.OnTouchListener() {
                        float dX;
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            Clip clip = (Clip) clipView.getTag();
                            int minWidth = (int) (MIN_CLIP_DURATION * pixelsPerSecond);
                            int maxWidth = (int) (clip.originalDuration * pixelsPerSecond);
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    dX = event.getRawX();
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    timelineScroll.requestDisallowInterceptTouchEvent(true);

                                    float deltaX = event.getRawX() - dX;
                                    dX = event.getRawX();


                                    // Clamping only for video and audio as these type has limited duration
                                    if (type == ClipType.VIDEO || type == ClipType.AUDIO)
                                    {
                                        deltaX = (Math.min(-deltaX, clip.startClipTrim * pixelsPerSecond));
                                        deltaX = -deltaX;


                                        int newWidth = clipView.getWidth() - (int) deltaX;
                                        float newStartTime = (clipView.getX() + deltaX - centerOffset) / pixelsPerSecond;

                                        // Clamping
                                        if (newWidth < minWidth) return true;
                                        // Clamp to prevent going before 0s
                                        // TODO: In capcut it doesn't clamp. Instead after pointer up, it set the
                                        //  start time to 0 and push the rest of the before 0s to the right.
                                        if (newStartTime < 0) return true;


                                        newWidth = Math.max(minWidth, Math.min(newWidth, maxWidth));

                                        clipView.getLayoutParams().width = newWidth;
                                        clipView.setX(clipView.getX() + deltaX);
                                        clipView.requestLayout();

                                        clip.startTime = (clipView.getX() - centerOffset) / pixelsPerSecond;
                                        clip.startClipTrim += (deltaX) / pixelsPerSecond;
                                        clip.duration = clip.originalDuration - clip.endClipTrim - clip.startClipTrim;//Math.max(MIN_CLIP_DURATION, newWidth / (float) pixelsPerSecond);
                                    }
                                    else {
                                        deltaX = -deltaX;

                                        int newWidth = clipView.getWidth() - (int) deltaX;

                                        clipView.getLayoutParams().width = newWidth;
                                        clipView.setX(clipView.getX() + deltaX);
                                        clipView.requestLayout();

                                        clip.startTime = (clipView.getX() - centerOffset) / pixelsPerSecond;
                                        clip.startClipTrim += (deltaX) / pixelsPerSecond;
                                        clip.duration = clip.originalDuration - clip.endClipTrim - clip.startClipTrim;
                                    }
                                    break;

                                case MotionEvent.ACTION_UP:
                                case MotionEvent.ACTION_CANCEL:
                                    timelineScroll.requestDisallowInterceptTouchEvent(false);

                                    activity.updateCurrentClipEnd();
                                    break;

                            }
                            return true;
                        }
                    }
            );

            rightHandle.setOnTouchListener(
                    new View.OnTouchListener() {
                        float dX;
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            Clip clip = (Clip) clipView.getTag();
                            int minWidth = (int) (MIN_CLIP_DURATION * pixelsPerSecond);
                            int maxWidth = (int) (clip.originalDuration * pixelsPerSecond);
                            switch (event.getAction())
                            {
                                case MotionEvent.ACTION_DOWN:
                                    dX = event.getRawX();
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    timelineScroll.requestDisallowInterceptTouchEvent(true);

                                    float deltaX = event.getRawX() - dX;
                                    dX = event.getRawX();

                                    // Clamping only for video and audio as these type has limited duration
                                    if(type == ClipType.VIDEO || type == ClipType.AUDIO)
                                    {
                                        deltaX = Math.min(deltaX, clip.endClipTrim * pixelsPerSecond);

                                        int newWidth = clipView.getWidth() + (int) deltaX;

                                        // Clamping
                                        if (newWidth < minWidth) return true;

                                        newWidth = Math.max(minWidth, Math.min(newWidth, maxWidth));

                                        clipView.getLayoutParams().width = newWidth;
                                        clipView.requestLayout();

                                        clip.endClipTrim -= (deltaX) / pixelsPerSecond;
                                        clip.duration = clip.originalDuration - clip.endClipTrim - clip.startClipTrim;//Math.max(MIN_CLIP_DURATION, newWidth / (float) pixelsPerSecond);
                                    }
                                    else {
                                        int newWidth = clipView.getWidth() + (int) deltaX;

                                        clipView.getLayoutParams().width = newWidth;
                                        clipView.requestLayout();

                                        clip.endClipTrim -= (deltaX) / pixelsPerSecond;
                                        clip.duration = clip.originalDuration - clip.endClipTrim - clip.startClipTrim;//Math.max(MIN_CLIP_DURATION, newWidth / (float) pixelsPerSecond);
                                    }


                                    break;

                                case MotionEvent.ACTION_UP:
                                case MotionEvent.ACTION_CANCEL:
                                    timelineScroll.requestDisallowInterceptTouchEvent(false);

                                    activity.updateCurrentClipEnd();
                                    break;

                            }

                            return true;
                        }
                    }
            );

            clipView.addView(leftHandle);
            clipView.addView(rightHandle);

            deselect();
        }

        public void select() {
            viewRef.getFilledImageView().setColorFilter(0x77AAAAAA);

            leftHandle.setVisibility(View.VISIBLE);
            rightHandle.setVisibility(View.VISIBLE);
        }
        public void deselect() {
            viewRef.getFilledImageView().setColorFilter(0x00000000);

            leftHandle.setVisibility(View.GONE);
            rightHandle.setVisibility(View.GONE);
        }



        public void deleteClip(Timeline timeline, EditingActivity activity)
        {
            activity.deselectingClip();

            Track currentTrack = timeline.tracks.get(trackIndex);

            currentTrack.removeClip(this);
            currentTrack.viewRef.removeView(viewRef);
        }

        public void splitClip(EditingActivity activity, Timeline timeline, float currentGlobalTime)
        {
            Track currentTrack = timeline.tracks.get(trackIndex);

            float translatedLocalCurrentTime = getLocalClipTime(currentGlobalTime);

            Clip secondaryClip = new Clip(this);

            secondaryClip.startTime = currentGlobalTime;

            float oldEndClipTrim = endClipTrim;
            float oldStartClipTrim = startClipTrim;

            // Primary clip
            endClipTrim = originalDuration - (translatedLocalCurrentTime + oldStartClipTrim);
            duration = originalDuration - endClipTrim - startClipTrim;

            // Secondary clip
            secondaryClip.startClipTrim = translatedLocalCurrentTime + oldStartClipTrim;
            secondaryClip.endClipTrim = oldEndClipTrim;
            secondaryClip.duration = secondaryClip.originalDuration - secondaryClip.endClipTrim - secondaryClip.startClipTrim;

            activity.addClipToTrack(currentTrack, secondaryClip);
            activity.revalidationClipView(this);
        }
        public void restate() {
            videoProperties = new VideoProperties(0, 0, 0, 1, 1, 1, 1);
        }

        public void mergingVideoPropertiesFromSingleKeyframe() {
            if(hasOnlyOneAnimatedProperties())
            {
                VideoProperties videoProperties = keyframes.keyframes.get(0).value;

                applyPropertiesToClip(videoProperties);
            }
        }
        public void applyPropertiesToClip(VideoProperties properties) {
            this.videoProperties = new VideoProperties(properties);
        }




        /**
         * To ensure rendering keyframe correctly, there must have more than 2 keyframe to form a line for
         * lerping back and forth.
         * 2 points create 1 line, 3 points create 2 lines, 4 points create 3 lines, etc...
         * @return true if keyframe list has more than 2 element, false otherwise.
         */
        public boolean hasAnimatedProperties() {
            return keyframes.keyframes.size() > 1;
        }

        /**
         * If there's not enough keyframe to render, then we just merging the video properties to
         * the main video.
         * @return true if keyframe list has only 1 element, false otherwise.
         */
        public boolean hasOnlyOneAnimatedProperties() {
            return keyframes.keyframes.size() == 1;
        }


        /**
         * Used for FFmpeg and other output that requires original quality.
         *
         * @param properties The Project Data.
         * @return The path for original file.
         */
        public String getAbsolutePath(MainActivity.ProjectData properties) {
            return getAbsolutePath(properties.getProjectPath());
        }
        public String getAbsolutePath(String projectPath) {
            return IOHelper.CombinePath(projectPath, Constants.DEFAULT_CLIP_DIRECTORY, clipName);
        }
        /**
         * Used for EditingActivity in which didn't need high quality video. Fit for real-time preview.
         *
         * @param properties The Project Data.
         * @return The path for preview file.
         */
        public String getAbsolutePreviewPath(MainActivity.ProjectData properties) {
            return getAbsolutePreviewPath(properties.getProjectPath());
        }
        public String getAbsolutePreviewPath(String projectPath) {
            String path = IOHelper.CombinePath(projectPath, Constants.DEFAULT_PREVIEW_CLIP_DIRECTORY, clipName);
            // Fallback if not available yet.
            // TODO: Temporary fix for the soon preview loading. Consider block main thread for preview to have time to load first
            if(!IOHelper.isFileExist(path))
                path = getAbsolutePath(projectPath);
            return path;
        }
        /**
         * Used for EditingActivity in which didn't need high quality video. Fit for real-time preview.
         *
         * @param properties The Project Data.
         * @return The path for preview file.
         */
        public String getAbsolutePreviewPath(MainActivity.ProjectData properties, String previewExtension) {
            return getAbsolutePreviewPath(properties.getProjectPath(), previewExtension);
        }
        public String getAbsolutePreviewPath(String projectPath, String previewExtension) {
            String path = IOHelper.CombinePath(projectPath, Constants.DEFAULT_PREVIEW_CLIP_DIRECTORY, clipName.substring(0, clipName.lastIndexOf('.')) + previewExtension);
            // Fallback if not available yet.
            // TODO: Temporary fix for the soon preview loading. Consider block main thread for preview to have time to load first
            if(!IOHelper.isFileExist(path))
                path = getAbsolutePath(projectPath);
            return path;
        }
        public float getLocalClipTime(float playheadTime) {
            return playheadTime - startTime;
        }

        public float getTrimmedLocalTime(float localClipTime) {
            return localClipTime + startClipTrim;
        }

        public float getCutoutDuration() {
            return duration - startClipTrim - endClipTrim;
        }

    }




    public enum ClipType {
        VIDEO,
        AUDIO,
        IMAGE,
        TEXT,
        TRANSITION,
        EFFECT
    }
    public static class EffectTemplate implements Serializable {
        @Expose
        public String type; // "transition", "overlay", etc
        @Expose
        public String style; // "fade", "zoom", "glitch"
        @Expose
        public double duration;
        @Expose
        public double offset;
        @Expose
        public Map<String, Object> params;

        public EffectTemplate(String style, double duration, double offset)
        {
            this.style = style;
            this.duration = duration;
            this.offset = offset;
        }

    }


    static class DragContext {
        View ghost;
        Track currentTrack;
        Clip clip;
    }

    public static class TransitionClip implements Serializable {
        @Expose
        public int trackIndex;
        @Expose
        public float startTime;
        @Expose
        public float duration;

        @Expose
        public Clip fromClip;
        @Expose
        public Clip toClip;

        @Expose
        public EffectTemplate effect; // e.g. xfade, zoom, etc.

        @Expose
        public TransitionMode mode;


        public enum TransitionMode {
            END_FIRST,
            OVERLAP,
            BEGIN_SECOND
        }
    }



    public static class VideoSettings implements Serializable {
        int videoWidth;
        int videoHeight;
        int frameRate;
        int crf;
        String preset;
        String tune;
        public VideoSettings(int videoWidth, int videoHeight, int frameRate, int crf, String preset, String tune)
        {
            this.videoWidth = videoWidth;
            this.videoHeight = videoHeight;
            this.frameRate = frameRate;
            this.crf = crf;
            this.preset = preset;
            this.tune = tune;
        }

        public int getVideoWidth() {
            return videoWidth;
        }
        public int getVideoHeight() {
            return videoHeight;
        }
        public int getFrameRate() {
            return frameRate;
        }
        public int getCRF() {
            return crf;
        }
        public String getPreset() {
            return preset;
        }
        public String getTune() {
            return tune;
        }

        public void saveSettings(Context context, MainActivity.ProjectData data) {
            IOHelper.writeToFile(context, IOHelper.CombinePath(data.getProjectPath(), Constants.DEFAULT_VIDEO_SETTINGS_FILENAME), new Gson().toJson(this));
        }
        public void loadSettingsFromProject(Context context, MainActivity.ProjectData data)
        {
            VideoSettings loadSettings = loadSettings(context, data);
            this.videoWidth = loadSettings.videoWidth;
            this.videoHeight = loadSettings.videoHeight;
            this.frameRate = loadSettings.frameRate;
            this.crf = loadSettings.crf;
            this.preset = loadSettings.preset;
            this.tune = loadSettings.tune;
        }
        public static VideoSettings loadSettings(Context context, MainActivity.ProjectData data) {
            return new Gson().fromJson(IOHelper.readFromFile(context, IOHelper.CombinePath(data.getProjectPath(), Constants.DEFAULT_VIDEO_SETTINGS_FILENAME)), VideoSettings.class);
        }

        /*
        // Low
videoWidth = 640;
videoHeight = 360;
frameRate = 24;

// Medium
videoWidth = 1280;
videoHeight = 720;
frameRate = 30;

// High
videoWidth = 1920;
videoHeight = 1080;
frameRate = 60;

         */


        public class FfmpegPreset
        {
            public static final String PLACEBO = "placebo";
            public static final String VERYSLOW = "veryslow";
            public static final String SLOWER = "slower";
            public static final String SLOW = "slow";
            public static final String MEDIUM = "medium";
            public static final String FAST = "fast";
            public static final String FASTER = "faster";
            public static final String VERYFAST = "veryfast";
            public static final String SUPERFAST = "superfast";
            public static final String ULTRAFAST = "ultrafast";
        }

        public class FfmpegTune
        {
            public static final String FILM = "film";
            public static final String ANIMATION = "animation";
            public static final String GRAIN = "grain";
            public static final String STILLIMAGE = "stillimage";
            public static final String FASTDECODE = "fastdecode";
            public static final String ZEROLATENCY = "zerolatency";
        }

    }
    public static class VideoProperties implements Serializable {
        @Expose
        public float valuePosX;
        @Expose
        public float valuePosY;
        @Expose
        public float valueRot;
        @Expose
        public float valueScaleX;
        @Expose
        public float valueScaleY;
        @Expose
        public float valueOpacity;
        @Expose
        public float valueSpeed;

        public VideoProperties()
        {
            this.valuePosX = 0;
            this.valuePosY = 0;
            this.valueRot = 0;
            this.valueScaleX = 1;
            this.valueScaleY = 1;
            this.valueOpacity = 1;
            this.valueSpeed = 1;
        }
        public VideoProperties(float valuePosX, float valuePosY,
                               float valueRot,
                               float valueScaleX, float valueScaleY,
                               float valueOpacity, float valueSpeed)
        {
            this.valuePosX = valuePosX;
            this.valuePosY = valuePosY;
            this.valueRot = valueRot;
            this.valueScaleX = valueScaleX;
            this.valueScaleY = valueScaleY;
            this.valueOpacity = valueOpacity;
            this.valueSpeed = valueSpeed;
        }

        public VideoProperties(VideoProperties properties)
        {
            this.valuePosX = properties.valuePosX;
            this.valuePosY = properties.valuePosY;
            this.valueRot = properties.valueRot;
            this.valueScaleX = properties.valueScaleX;
            this.valueScaleY = properties.valueScaleY;
            this.valueOpacity = properties.valueOpacity;
            this.valueSpeed = properties.valueSpeed;
        }

        public float getValue(ValueType valueType) {

            switch (valueType)
            {
                case PosX:
                    return valuePosX;
                case PosY:
                    return valuePosY;
                case Rot:
                    return valueRot;
                case RotInRadians:
                    return (float) Math.toRadians(valueRot);
                case ScaleX:
                    return valueScaleX;
                case ScaleY:
                    return valueScaleY;
                case Opacity:
                    return valueOpacity;
                case Speed:
                    return valueSpeed;
                default:
                    return 1;

            }
        }

        public void setValue(float v, ValueType valueType) {

            switch (valueType)
            {
                case PosX:
                    valuePosX = v;
                    break;
                case PosY:
                    valuePosY = v;
                    break;
                case Rot:
                    valueRot = v;
                    break;
                case ScaleX:
                    valueScaleX = v;
                    break;
                case ScaleY:
                    valueScaleY = v;
                    break;
                case Opacity:
                    valueOpacity = v;
                    break;
                case Speed:
                    valueSpeed = v;
                    break;

            }
        }

        public enum ValueType {
            PosX, PosY, Rot, RotInRadians, ScaleX, ScaleY, Opacity, Speed
        }
    }
    public static class Keyframe implements Serializable {
        @Expose
        public float time; // seconds
        @Expose
        public VideoProperties value;
        @Expose
        public EasingType easing = EasingType.LINEAR;


        public Keyframe(float time, VideoProperties value, EasingType easing)
        {
            this.time = time;
            this.value = value;
            this.easing = easing;
        }
    }
    public static class AnimatedProperty implements Serializable {

        @Expose
        public List<Keyframe> keyframes = new ArrayList<>();

        public float getValueAtTime(float playheadTime, VideoProperties.ValueType valueType) {
            if (keyframes.isEmpty()) return 0f;

            Keyframe prev = keyframes.get(0);
            for (Keyframe next : keyframes) {
                if (playheadTime < next.time) {
                    float t = (playheadTime - prev.time) / (next.time - prev.time);
                    t = Math.max(0f, Math.min(1f, t));

                    float prevValue = prev.value.getValue(valueType);
                    float nextValue = next.value.getValue(valueType);


                    return lerp(prevValue, nextValue, ease(t, next.easing)); // linear interpolation
                }
                prev = next;

//                if (playheadTime <= keyframes.get(0).time) return keyframes.get(0).value;
//                if (playheadTime >= keyframes.get(keyframes.size() - 1).time) return keyframes.get(keyframes.size() - 1).value;

            }
            return keyframes.get(keyframes.size() - 1).value.getValue(valueType);
        }
        public float getValueAtPoint(int keyframeIndex, VideoProperties.ValueType valueType) {
            if (keyframes.isEmpty()) return 0f;

            return keyframes.get(keyframeIndex).value.getValue(valueType);
        }

        private float lerp(float a, float b, float t) {
            return a + (b - a) * t;
        }

        private float ease(float t, EasingType type) {
            switch (type) {
                case LINEAR: return t;
                case EASE_IN: return t * t;
                case EASE_OUT: return 1 - (1 - t) * (1 - t);
                case EXPONENTIAL: return (float)Math.pow(2, 10 * (t - 1));
                case EASE_IN_OUT:
                    return t < 0.5f ? 2 * t * t : 1 - (float)Math.pow(-2 * t + 2, 2) / 2;
                case QUADRATIC:
                    return t * t; // same as EASE_IN, but you can customize later
                case SPRING:
                    MathHelper.spring(t, 1, 12, 0.5f);
//                    SpringProperty spring = new SpringProperty();
//                    return spring.getValueAt(t);
                // Add more...
                default: return t;
            }
        }

    }
//    public static class SpringProperty implements Serializable {
//        public float mass = 1f;
//        public float stiffness = 12f;
//        public float damping = 0.5f;
//
//        public float getValueAt(float t) {
//            // Critically damped spring motion
//            return 1 - (float)Math.exp(-damping * t) * (1 + damping * t);
//        }
//    }

    public enum EasingType {
        LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT, EXPONENTIAL, SPRING, QUADRATIC
    }

    //Todo: When real-time preview is back to working, consider using this stuff
    protected void useKeyFrameInRealtimeRendering(Clip clip, float playheadTime)
    {
        float x = clip.keyframes.getValueAtTime(playheadTime, VideoProperties.ValueType.PosX);
        float y = clip.keyframes.getValueAtTime(playheadTime, VideoProperties.ValueType.PosY);
        float rotation = clip.keyframes.getValueAtTime(playheadTime, VideoProperties.ValueType.Rot);
        float scaleX = clip.keyframes.getValueAtTime(playheadTime, VideoProperties.ValueType.ScaleX);
        float scaleY = clip.keyframes.getValueAtTime(playheadTime, VideoProperties.ValueType.ScaleY);

        // Apply transform to canvas or OpenGL

    }


    public static class ClipRenderer {
        public final Clip clip;

        private MediaExtractor videoExtractor;
        private MediaCodec videoDecoder;

        private MediaExtractor audioExtractor;
        private AudioTrack audioTrack;
        public boolean isPlaying;

        private TextureView textureView;
        private Context context;

        private ExecutorService renderThreadExecutorAudio = Executors.newFixedThreadPool(1);
        private ExecutorService renderThreadExecutorVideo = Executors.newFixedThreadPool(1);



        private Matrix matrix = new Matrix();
        private float scaleX = 1, scaleY = 1;
        private float rot = 0;
        private float posX = 0, posY = 0;


        private float scaleMatrixX = 1, scaleMatrixY = 1;
        private float rotMatrix = 0;
        private float posMatrixX = 0, posMatrixY = 0;


        public ClipRenderer(Context context, Clip clip, MainActivity.ProjectData data, VideoSettings settings, EditingActivity editingActivity, FrameLayout previewViewGroup, TextView textCanvasControllerInfo) {
            this.context = context;
            this.clip = clip;

            try
            {

                switch (clip.type)
                {
                    case VIDEO:
                    {

                        // VIDEO






                        textureView = new TextureView(context);
                        RelativeLayout.LayoutParams textureViewLayoutParams =
                                new RelativeLayout.LayoutParams(clip.width, clip.height);
                        previewViewGroup.addView(textureView, textureViewLayoutParams);


                        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                            @Override
                            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
                                try {
                                    Surface surface = new Surface(surfaceTexture);

                                    // Step 1: Extractor setup
                                    videoExtractor = new MediaExtractor();
                                    videoExtractor.setDataSource(clip.getAbsolutePreviewPath(data));

                                    int trackIndex = TimelineUtils.findVideoTrackIndex(videoExtractor);
                                    videoExtractor.selectTrack(trackIndex);

                                    MediaFormat format = videoExtractor.getTrackFormat(trackIndex);

                                    // Step 2: Codec setup
                                    videoDecoder = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME));
                                    videoDecoder.configure(format, surface, null, 0);
                                    videoDecoder.start();


                                    surfaceTexture.setDefaultBufferSize(clip.width, clip.height); // or your target resolution

                                    posX = (EditingActivity.renderToPreviewConversionX(clip.videoProperties.getValue(VideoProperties.ValueType.PosX), settings.videoWidth));
                                    posY = (EditingActivity.renderToPreviewConversionY(clip.videoProperties.getValue(VideoProperties.ValueType.PosY), settings.videoHeight));
                                    scaleX = (EditingActivity.renderToPreviewConversionScalingX(clip.videoProperties.getValue(VideoProperties.ValueType.ScaleX), settings.videoWidth));
                                    scaleY = (EditingActivity.renderToPreviewConversionScalingY(clip.videoProperties.getValue(VideoProperties.ValueType.ScaleY), settings.videoHeight));
                                    rot = (clip.videoProperties.getValue(VideoProperties.ValueType.Rot));


                                    applyTransformation();
                                    applyPostTransformation();

                                } catch (Exception e) {
                                    LoggingManager.LogExceptionToNoteOverlay(context, e);
                                }
                            }

                            @Override
                            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                                // Handle resize if needed
                            }

                            @Override
                            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                                return true; // release resources if needed
                            }

                            @Override
                            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
                                // Called every frame
                            }
                        });












                        // AUDIO

                        audioExtractor = new MediaExtractor();
                        audioExtractor.setDataSource(clip.getAbsolutePreviewPath(data, ".wav"));

                        int audioTrackIndex = TimelineUtils.findVideoTrackIndex(audioExtractor);
                        audioExtractor.selectTrack(audioTrackIndex);

                        MediaFormat audioFormat = audioExtractor.getTrackFormat(audioTrackIndex);

                        int sampleRate = audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                        int channelConfig = (audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT) == 1) ?
                                AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
                        int audioFormatPCM = AudioFormat.ENCODING_PCM_16BIT;
                        int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormatPCM);

                        audioTrack = new AudioTrack(
                                AudioManager.STREAM_MUSIC,
                                sampleRate,
                                channelConfig,
                                audioFormatPCM,
                                minBufferSize,
                                AudioTrack.MODE_STREAM
                        );
                        audioTrack.play();


                        break;
                    }
                    case IMAGE:
                    {
                        textureView = new TextureView(context);
                        RelativeLayout.LayoutParams textureViewLayoutParams =
                                new RelativeLayout.LayoutParams(clip.width, clip.height);
                        previewViewGroup.addView(textureView, textureViewLayoutParams);

                        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                            @Override
                            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
                                // Create a Surface from the TextureView

                                // For IMAGE rendering
                                // TODO: WTF sample size 1? Yes for rendering. We don't know how to forcefully extend the 8 old sampleSize
                                Bitmap image = IOImageHelper.LoadFileAsPNGImage(context, clip.getAbsolutePreviewPath(data), 1);

                                // Resize TextureView to match bitmap size
                                ViewGroup.LayoutParams params = textureView.getLayoutParams();
                                params.width = clip.width;
                                params.height = clip.height;
                                textureView.setLayoutParams(params);

                                // Draw the bitmap onto the TextureView’s canvas
                                Canvas canvas = textureView.lockCanvas();
                                if (canvas != null) {
                                    //canvas.drawColor(Color.BLACK); // optional background
                                    canvas.drawBitmap(image, 0, 0, null); // draw at top-left
                                    textureView.unlockCanvasAndPost(canvas);
                                }


                                surfaceTexture.setDefaultBufferSize(clip.width, clip.height); // or your target resolution

                                posX = (EditingActivity.renderToPreviewConversionX(clip.videoProperties.getValue(VideoProperties.ValueType.PosX), settings.videoWidth));
                                posY = (EditingActivity.renderToPreviewConversionY(clip.videoProperties.getValue(VideoProperties.ValueType.PosY), settings.videoHeight));
                                scaleX = (EditingActivity.renderToPreviewConversionScalingX(clip.videoProperties.getValue(VideoProperties.ValueType.ScaleX), settings.videoWidth));
                                scaleY = (EditingActivity.renderToPreviewConversionScalingY(clip.videoProperties.getValue(VideoProperties.ValueType.ScaleY), settings.videoHeight));
                                rot = (clip.videoProperties.getValue(VideoProperties.ValueType.Rot));

                                applyTransformation();
                                applyPostTransformation();

                            }

                            @Override
                            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {}

                            @Override
                            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                                return true;
                            }

                            @Override
                            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}
                        });


                        break;
                    }
                    case AUDIO:
                    {

                        audioExtractor = new MediaExtractor();
                        audioExtractor.setDataSource(clip.getAbsolutePreviewPath(data, ".wav"));

                        int audioTrackIndex = TimelineUtils.findVideoTrackIndex(audioExtractor);
                        audioExtractor.selectTrack(audioTrackIndex);

                        MediaFormat audioFormat = audioExtractor.getTrackFormat(audioTrackIndex);

                        int sampleRate = audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                        int channelConfig = (audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT) == 1) ?
                                AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
                        int audioFormatPCM = AudioFormat.ENCODING_PCM_16BIT;
                        int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormatPCM);

                        audioTrack = new AudioTrack(
                                AudioManager.STREAM_MUSIC,
                                sampleRate,
                                channelConfig,
                                audioFormatPCM,
                                minBufferSize,
                                AudioTrack.MODE_STREAM
                        );
                        audioTrack.play();

                        break;
                    }
                }


                if(textureView != null)
                {
                    setPivot();
                    attachGestureControls(textureView, clip, settings, editingActivity, textCanvasControllerInfo);
                }


            }
            catch (Exception e)
            {
                LoggingManager.LogExceptionToNoteOverlay(context, e);
            }
        }


        public boolean isVisible(float playheadTime) {
            return playheadTime >= clip.startTime &&
                    playheadTime <= clip.startTime + clip.duration;
        }

        public void renderFrame(float playheadTime, boolean isSeekingOnly) {
            if (!isVisible(playheadTime)) {
//                if(surfaceView != null)
//                {
//                    Canvas canvas = surfaceHolder.lockCanvas();
//                    if (canvas != null) {
//                        canvas.drawColor(Color.BLACK); // Fill canvas with black
//                        surfaceHolder.unlockCanvasAndPost(canvas);
//                    }
//                }
                return;
            }
//            if(isPlaying && !isSeekingOnly) return;

            startPlayingAt(playheadTime, isSeekingOnly);
        }

        private void pumpDecoderVideoSeek(float playheadTime) {
            if(videoDecoder == null) return;
            if(textureView == null) return;
            if(textureView.getVisibility() == View.GONE) return;
            float clipTime = playheadTime - clip.startTime + clip.startClipTrim;
            long ptsUs = (long)(clipTime * 1_000_000); // override presentation timestamp
            int inputIndex = videoDecoder.dequeueInputBuffer(0);
            if (inputIndex >= 0) {
                ByteBuffer inputBuffer = videoDecoder.getInputBuffer(inputIndex);
                int sampleSize = videoExtractor.readSampleData(inputBuffer, 0);

                if (sampleSize >= 0) {
                    videoExtractor.seekTo(ptsUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
                    videoDecoder.queueInputBuffer(inputIndex, 0, sampleSize, ptsUs, 0);

                } else {
                    videoDecoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                }
            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputIndex = videoDecoder.dequeueOutputBuffer(bufferInfo, 0);
            if (outputIndex >= 0) {
                videoDecoder.releaseOutputBuffer(outputIndex, true); // true = render to surface
            }
        }
        private void pumpDecoderAudioSeek(float playheadTime) {

            float clipTime = playheadTime - clip.startTime + clip.startClipTrim;
            long ptsUs = (long)(clipTime * 1_000_000); // override presentation timestamp
            audioExtractor.seekTo(ptsUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);

            ByteBuffer buffer = ByteBuffer.allocate(32768);

            int sampleSize = audioExtractor.readSampleData(buffer, 0);
            if (sampleSize < 0) return; // End of stream

            byte[] chunk = new byte[sampleSize];
            buffer.get(chunk, 0, sampleSize);
            buffer.clear();

            audioTrack.write(chunk, 0, chunk.length, AudioTrack.WRITE_NON_BLOCKING);

        }





        public void startPlayingAt(float playheadTime, boolean isSeekingOnly) {
            if (!isVisible(playheadTime)) {
//                Canvas canvas = surfaceHolder.lockCanvas();
//                if (canvas != null) {
//                    canvas.drawColor(Color.BLACK); // Fill canvas with black
//                    surfaceHolder.unlockCanvasAndPost(canvas);
//                }
                return;
            }


            try {

                switch (clip.type)
                {
                    case VIDEO:
                    {

//                        if(clip.getLocalClipTime(playheadTime) * 1000 > 0 && clip.getLocalClipTime(playheadTime) * 1000 < clip.duration)
//                        {
//                            videoPlayer.seekTo((long) (clip.getTrimmedLocalTime(clip.getLocalClipTime(playheadTime)) * 1000000));
//                            System.err.println(videoPlayer.getCurrentPosition());
//                            if(!isSeekingOnly)
//                            {
//                                videoPlayer.start();
//                                isPlaying = true;
//                            }
//                        }
                        renderThreadExecutorVideo.execute(() -> pumpDecoderVideoSeek(playheadTime));

                        if(clip.isVideoHasAudio)
                            renderThreadExecutorAudio.execute(() -> pumpDecoderAudioSeek(playheadTime));
                        break;
                    }
                    case AUDIO:
                    {
//                        if(clip.getLocalClipTime(playheadTime) * 1000 > 0 && clip.getLocalClipTime(playheadTime) * 1000 < clip.duration)
//                        {
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                                audioPlayer.seekTo((int) (clip.getTrimmedLocalTime(clip.getLocalClipTime(playheadTime)) * 1000), android.media.MediaPlayer.SEEK_CLOSEST);
//                            }
//                            else {
//                                audioPlayer.seekTo((int) (clip.getTrimmedLocalTime(clip.getLocalClipTime(playheadTime)) * 1000));
//                            }
//                            if(!isSeekingOnly)
//                            {
//                                audioPlayer.start();
//                                isPlaying = true;
//                            }
//                        }


                        renderThreadExecutorAudio.execute(() -> pumpDecoderAudioSeek(playheadTime));
                        break;
                    }

                }



            } catch (Exception e) {
                LoggingManager.LogExceptionToNoteOverlay(context, e);
            }
        }





        private void setPivot() {
            textureView.post(() -> {
                // Not affecting the translation pos when scaling
                textureView.setPivotX(0);
                textureView.setPivotY(0);
            });

        }


        EditMode currentMode = EditMode.NONE;


        private void attachGestureControls(TextureView tv, Clip clip, VideoSettings settings, EditingActivity editingActivity, TextView textCanvasControllerInfo) {
            final GestureDetector tapDrag = new GestureDetector(tv.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override public boolean onDown(MotionEvent e) { return true; } // must return true to receive events
                @Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {

                    if(currentMode != EditMode.NONE && currentMode != EditMode.MOVE) return true;
                    currentMode = EditMode.MOVE;

                    // Move
                    posX -= dx;
                    posY -= dy;
                    posMatrixX -= dx / clip.videoProperties.getValue(VideoProperties.ValueType.ScaleX);
                    posMatrixY -= dy / clip.videoProperties.getValue(VideoProperties.ValueType.ScaleY);
                    applyTransformation();

                    // Sync model
                    clip.videoProperties.setValue(EditingActivity.previewToRenderConversionX(posX, settings.videoWidth), VideoProperties.ValueType.PosX);
                    clip.videoProperties.setValue(EditingActivity.previewToRenderConversionY(posY, settings.videoHeight), VideoProperties.ValueType.PosY);

                    textCanvasControllerInfo.setText("Pos X: " + clip.videoProperties.getValue(VideoProperties.ValueType.PosX) + " | Pos Y: " + clip.videoProperties.getValue(VideoProperties.ValueType.PosY));
                    return true;
                }
                @Override public boolean onSingleTapUp(MotionEvent e) {
                    editingActivity.selectingClip(clip);
                    return true;
                }
            });

            final ScaleGestureDetector scaler = new ScaleGestureDetector(tv.getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override public boolean onScale(ScaleGestureDetector detector) {

                    if(currentMode != EditMode.NONE && currentMode != EditMode.SCALE && currentMode != EditMode.ROTATE) return true;
                    currentMode = EditMode.SCALE;

                    scaleX *= detector.getScaleFactor();
                    scaleY *= detector.getScaleFactor();
                    scaleMatrixX *= detector.getScaleFactor();
                    scaleMatrixY *= detector.getScaleFactor();

                    applyTransformation();

                    // TODO: Before we going further. Let calculate first the aspect ratio of video
                    //  only after that we based on the width and height of the following aspect ratio
                    //  and use it for preview scaling inside the screen that smaller than the video. (Clamping)
                    // Sync model
                    clip.videoProperties.setValue(EditingActivity.previewToRenderConversionScalingX(scaleX, settings.videoWidth), VideoProperties.ValueType.ScaleX);
                    clip.videoProperties.setValue(EditingActivity.previewToRenderConversionScalingY(scaleY, settings.videoHeight), VideoProperties.ValueType.ScaleY);

                    setPivot();

                    textCanvasControllerInfo.setText(
                                    "Scale X: " + clip.videoProperties.getValue(VideoProperties.ValueType.ScaleX) +
                                    " | Scale Y: " + clip.videoProperties.getValue(VideoProperties.ValueType.ScaleY) +
                                    "\n" + "Rot: " + clip.videoProperties.getValue(VideoProperties.ValueType.Rot)
                    );

                    return true;
                }
            });

            // Simple rotation detector
            final float[] lastAngle = { Float.NaN };
            tv.setOnTouchListener((v, event) -> {
                tapDrag.onTouchEvent(event);
                scaler.onTouchEvent(event);


                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount() >= 2) {

                            if(currentMode != EditMode.NONE && currentMode != EditMode.ROTATE && currentMode != EditMode.SCALE) break;
                            currentMode = EditMode.ROTATE;

                            float ax = event.getX(0), ay = event.getY(0);
                            float bx = event.getX(1), by = event.getY(1);
                            float angle = (float) Math.toDegrees(Math.atan2(by - ay, bx - ax)); // [-180,180]
                            if (Float.isNaN(lastAngle[0])) {
                                lastAngle[0] = angle;
                            } else {
                                float delta = normalizeAngle(angle - lastAngle[0]);
                                rot += delta;
                                rotMatrix += delta;

                                // Normalize to [-360, 360]
                                rot = ((rot + 360f) % 720f) - 360f;
                                rotMatrix = ((rotMatrix + 360f) % 720f) - 360f;

                                // Snap to nearest multiple of 90
                                float nearest = Math.round(rot / Constants.CANVAS_ROTATE_SNAP_DEGREE) * Constants.CANVAS_ROTATE_SNAP_DEGREE;
                                if (Math.abs(rot - nearest) <= Constants.CANVAS_ROTATE_SNAP_THRESHOLD_DEGREE) {
                                    rot = nearest;
                                }
                                nearest = Math.round(rotMatrix / Constants.CANVAS_ROTATE_SNAP_DEGREE) * Constants.CANVAS_ROTATE_SNAP_DEGREE;
                                if (Math.abs(rotMatrix - nearest) <= Constants.CANVAS_ROTATE_SNAP_THRESHOLD_DEGREE) {
                                    rotMatrix = nearest;
                                }

                                applyTransformation();
                                clip.videoProperties.setValue(rot, VideoProperties.ValueType.Rot);

                                textCanvasControllerInfo.setText(
                                        "Scale X: " + clip.videoProperties.getValue(VideoProperties.ValueType.ScaleX) +
                                                " | Scale Y: " + clip.videoProperties.getValue(VideoProperties.ValueType.ScaleY) +
                                                "\n" + "Rot: " + clip.videoProperties.getValue(VideoProperties.ValueType.Rot)
                                );

                                lastAngle[0] = angle;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        lastAngle[0] = Float.NaN;
                        currentMode = EditMode.NONE;
                        applyPostTransformation();
                        break;
                }
                return true;
            });
        }

        // TODO: Isn't handling scale properly yet.
        private void applyTransformation() {
            matrix.reset();
            matrix.postScale(scaleMatrixX, scaleMatrixY);
            matrix.postRotate(rotMatrix);
            matrix.postTranslate(posMatrixX, posMatrixY);

            textureView.setTransform(matrix);
            textureView.invalidate();
        }
        private void applyPostTransformation() {
            textureView.post(() -> {
                // Reset the matrix state for next drag
                scaleMatrixX = 1;
                scaleMatrixY = 1;
                rotMatrix = 0;
                posMatrixX = 0;
                posMatrixY = 0;
                matrix.reset();

                textureView.setTransform(matrix);
                textureView.invalidate();


                textureView.setTranslationX(posX);
                textureView.setTranslationY(posY);
                textureView.setScaleX(scaleX);
                textureView.setScaleY(scaleY);
                textureView.setRotation(rot);
            });
        }

        private float normalizeAngle(float a) {
            // Map to [-180, 180] to avoid jump
            while (a > 180f) a -= 360f;
            while (a < -180f) a += 360f;
            return a;
        }












        public enum EditMode {
            MOVE, SCALE, ROTATE, NONE
        }


        public void release() {
            if (audioExtractor != null) {
                audioExtractor.release();
            }
            if(videoDecoder != null) {
                videoDecoder.release();
            }
            if(videoExtractor != null) {
                videoExtractor.release();
            }
            if(renderThreadExecutorAudio != null) {
                renderThreadExecutorAudio.shutdownNow();
            }
            if(renderThreadExecutorVideo != null) {
                renderThreadExecutorVideo.shutdownNow();
            }

        }
    }


    public static class TimelineRenderer {
        private final Context context;
        private List<List<ClipRenderer>> trackLayers = new ArrayList<>();

        public TimelineRenderer(Context context) {
            this.context = context;
        }

        public void buildTimeline(Timeline timeline, MainActivity.ProjectData properties, VideoSettings settings, EditingActivity editingActivity, FrameLayout previewViewGroup, TextView textCanvasControllerInfo)
        {
            for (List<ClipRenderer> trackRenderer : trackLayers) {
                for (ClipRenderer clipRenderer : trackRenderer) {
                    if(clipRenderer != null)
                    {
                        clipRenderer.release();
                    }
                }
            }
//            for (int i = 0; i < previewViewGroup.getChildCount(); i++) {
//                SurfaceView view = (SurfaceView) previewViewGroup.getChildAt(i);
//                view.release?
//            }

            previewViewGroup.removeAllViews();

            // Black box for blank video
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            View blackBox = new View(context);
            blackBox.setBackgroundColor(Color.BLACK);
            previewViewGroup.addView(blackBox, params);

            trackLayers = new ArrayList<>();

            for (Track track : timeline.tracks) {
                List<ClipRenderer> renderers = new ArrayList<>();
                for (Clip clip : track.clips) {
                    switch (clip.type)
                    {
                        case VIDEO:
                        case AUDIO:
                        case IMAGE:
                            ClipRenderer clipRenderer = new ClipRenderer(context, clip, properties, settings, editingActivity, previewViewGroup, textCanvasControllerInfo);
                            renderers.add(clipRenderer);
                            break;
                    }
                }
                trackLayers.add(renderers);
            }
        }
        public void updateTime(float time, boolean isSeekingOnly)
        {
            for (List<ClipRenderer> trackRenderer : trackLayers) {
                for (ClipRenderer clipRenderer : trackRenderer) {
                    if(clipRenderer != null)
                    {
                        if(clipRenderer.isVisible(time))
                        {
                            if(clipRenderer.textureView != null)
                                clipRenderer.textureView.setVisibility(View.VISIBLE);
                        }
                        else {
                            if(clipRenderer.textureView != null)
                                clipRenderer.textureView.setVisibility(View.GONE);
                            clipRenderer.isPlaying = false;
                        }
                        clipRenderer.renderFrame(time, isSeekingOnly);
                    }
                }
            }
        }


        public void startPlayAt(float playheadTime) {

            boolean renderedAny = false;

            for (List<ClipRenderer> track : trackLayers) {
                for (ClipRenderer clipRenderer : track) {
                    if (clipRenderer.isVisible(playheadTime)) {
                        clipRenderer.renderFrame(playheadTime, false);
                        renderedAny = true;
                    }
                }
            }

            if (!renderedAny) {
                renderSolidBlack();
            }
        }

        private void renderSolidBlack() {
//            GLES20.glClearColor(0f, 0f, 0f, 1f);
//            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        }

        public void release() {
            for (List<ClipRenderer> track : trackLayers) {
                for (ClipRenderer cr : track) cr.release();
            }
        }
    }



}



