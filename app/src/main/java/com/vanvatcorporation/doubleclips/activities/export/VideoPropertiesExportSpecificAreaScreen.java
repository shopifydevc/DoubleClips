package com.vanvatcorporation.doubleclips.activities.export;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.vanvatcorporation.doubleclips.R;
import com.vanvatcorporation.doubleclips.activities.EditingActivity;
import com.vanvatcorporation.doubleclips.activities.editing.BaseEditSpecificAreaScreen;

public class VideoPropertiesExportSpecificAreaScreen extends BaseEditSpecificAreaScreen {


    public ArrayAdapter<String> presetAdapter, tuneAdapter;

    public Spinner presetSpinner, tuneSpinner;
    public EditText resolutionXField, resolutionYField, frameRateText, crfText, clipCapText;
    public CheckBox stretchToFullCheckbox;




    public VideoPropertiesExportSpecificAreaScreen(Context context) {
        super(context);
    }

    public VideoPropertiesExportSpecificAreaScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoPropertiesExportSpecificAreaScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public VideoPropertiesExportSpecificAreaScreen(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public void init() {
        super.init();

        resolutionXField = findViewById(R.id.resolutionXField);
        resolutionYField = findViewById(R.id.resolutionYField);
        frameRateText = findViewById(R.id.exportFrameRate);
        crfText = findViewById(R.id.exportCRF);
        clipCapText = findViewById(R.id.exportClipCap);

        presetSpinner = findViewById(R.id.exportPreset);
        presetAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, new String[]{
                EditingActivity.VideoSettings.FfmpegPreset.PLACEBO,
                EditingActivity.VideoSettings.FfmpegPreset.VERYSLOW,
                EditingActivity.VideoSettings.FfmpegPreset.SLOWER,
                EditingActivity.VideoSettings.FfmpegPreset.SLOW,
                EditingActivity.VideoSettings.FfmpegPreset.MEDIUM,
                EditingActivity.VideoSettings.FfmpegPreset.FAST,
                EditingActivity.VideoSettings.FfmpegPreset.FASTER,
                EditingActivity.VideoSettings.FfmpegPreset.VERYFAST,
                EditingActivity.VideoSettings.FfmpegPreset.SUPERFAST,
                EditingActivity.VideoSettings.FfmpegPreset.ULTRAFAST
        });
        presetSpinner.setAdapter(presetAdapter);
        presetSpinner.setSelection(9); // ULTRAFAST
        tuneSpinner = findViewById(R.id.exportTune);
        tuneAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, new String[]{
                EditingActivity.VideoSettings.FfmpegTune.FILM,
                EditingActivity.VideoSettings.FfmpegTune.ANIMATION,
                EditingActivity.VideoSettings.FfmpegTune.GRAIN,
                EditingActivity.VideoSettings.FfmpegTune.STILLIMAGE,
                EditingActivity.VideoSettings.FfmpegTune.FASTDECODE,
                EditingActivity.VideoSettings.FfmpegTune.ZEROLATENCY
        });
        tuneSpinner.setAdapter(tuneAdapter);
        tuneSpinner.setSelection(5); // ZEROLATENCY

        stretchToFullCheckbox = findViewById(R.id.stretchToFullCheckbox);


        onClose.add(() -> {
            resolutionXField.clearFocus();
            resolutionYField.clearFocus();
            frameRateText.clearFocus();
            crfText.clearFocus();
            clipCapText.clearFocus();
            presetSpinner.clearFocus();
            tuneSpinner.clearFocus();
        });

        animationScreen = AnimationScreen.ToBottom;
    }
}
