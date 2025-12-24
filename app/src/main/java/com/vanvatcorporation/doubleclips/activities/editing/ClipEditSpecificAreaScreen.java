package com.vanvatcorporation.doubleclips.activities.editing;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.vanvatcorporation.doubleclips.R;
import com.vanvatcorporation.doubleclips.activities.EditingActivity;

public class ClipEditSpecificAreaScreen extends BaseEditSpecificAreaScreen {

    public TextView totalDurationText;
    public EditText clipNameField, durationContent, positionXField, positionYField, rotationField, scaleXField, scaleYField, opacityField, speedField;
    public CheckBox muteAudioCheckbox;
    public LinearLayout keyframeScrollFrame;
    public Button clearKeyframeButton;


    public ClipEditSpecificAreaScreen(Context context) {
        super(context);
    }

    public ClipEditSpecificAreaScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClipEditSpecificAreaScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ClipEditSpecificAreaScreen(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void init()
    {
        super.init();

        totalDurationText = findViewById(R.id.totalDurationText);
        clipNameField = findViewById(R.id.clipNameField);
        durationContent = findViewById(R.id.durationContent);
        keyframeScrollFrame = findViewById(R.id.keyframeScrollFrame);
        clearKeyframeButton = findViewById(R.id.clearKeyframeButton);
        positionXField = findViewById(R.id.positionXField);
        positionYField = findViewById(R.id.positionYField);
        rotationField = findViewById(R.id.rotationField);
        scaleXField = findViewById(R.id.scaleXField);
        scaleYField = findViewById(R.id.scaleYField);
        opacityField = findViewById(R.id.opacityField);
        speedField = findViewById(R.id.speedField);
        muteAudioCheckbox = findViewById(R.id.muteAudioCheckbox);

        // Clear focus after edit
        onClose.add(() -> {
            clipNameField.clearFocus();
            durationContent.clearFocus();
            positionXField.clearFocus();
            positionYField.clearFocus();
            rotationField.clearFocus();
            scaleXField.clearFocus();
            scaleYField.clearFocus();
            opacityField.clearFocus();
            speedField.clearFocus();
        });
    }

    public void createKeyframeElement(EditingActivity.Clip clip, EditingActivity.Keyframe keyframe, Runnable onClickKeyframe)
    {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView text = new TextView(getContext());
        text.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        text.setText("Keyframe | Local clip time: " + keyframe.getLocalTime());
        text.setOnClickListener(v -> {
            text.setBackgroundColor(getResources().getColor(R.color.colorHighlightedButton, null));
            onClickKeyframe.run();
        });
        text.setOnLongClickListener(v -> {
            clip.keyframes.keyframes.remove(keyframe);
            keyframeScrollFrame.removeView(layout);
            return true;
        });

        layout.addView(text);

        keyframeScrollFrame.addView(layout);
    }

}
