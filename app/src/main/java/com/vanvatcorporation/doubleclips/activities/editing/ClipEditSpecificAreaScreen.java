package com.vanvatcorporation.doubleclips.activities.editing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.TooltipCompat;

import com.vanvatcorporation.doubleclips.R;
import com.vanvatcorporation.doubleclips.activities.EditingActivity;

public class ClipEditSpecificAreaScreen extends BaseEditSpecificAreaScreen {

    public TextView totalDurationText;
    public EditText clipNameField, durationContent, positionXField, positionYField, rotationField, scaleXField, scaleYField, opacityField, speedField, hueField;
    public SeekBar saturationSeekbar, brightnessSeekbar;
    public CheckBox muteAudioCheckbox, lockMediaForTemplateCheckbox;
    public LinearLayout keyframeScrollFrame;
    public Button clearKeyframeButton;
    public ArrayAdapter<EditingActivity.EasingType> easingTypeArrayAdapter;
    public Spinner easingSpinner;


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
        hueField = findViewById(R.id.hueField);
        saturationSeekbar = findViewById(R.id.saturationSeekbar);
        brightnessSeekbar = findViewById(R.id.brightnessSeekbar);
        muteAudioCheckbox = findViewById(R.id.muteAudioCheckbox);
        lockMediaForTemplateCheckbox = findViewById(R.id.lockMediaForTemplateCheckbox);

        easingSpinner = findViewById(R.id.easingContent);
        easingTypeArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, EditingActivity.EasingType.values());
        easingSpinner.setAdapter(easingTypeArrayAdapter);

        saturationSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                // TODO: Ugly
                //seekBar.setThumb(getThumb(progressValue));
                TooltipCompat.setTooltipText(seekBar, String.valueOf(progressValue));


//                yourTextView.setText(progress + " miles");
//                // Get the thumb bound and get its left value
//                int x = seekBar.getThumb().getBounds().left;
//                // set the left value to textview x value
//                yourTextView.setX(x);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }
        });
        brightnessSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                // TODO: Ugly
                //seekBar.setThumb(getThumb(progressValue));
                TooltipCompat.setTooltipText(seekBar, String.valueOf(progressValue));


//                yourTextView.setText(progress + " miles");
//                // Get the thumb bound and get its left value
//                int x = seekBar.getThumb().getBounds().left;
//                // set the left value to textview x value
//                yourTextView.setX(x);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }
        });





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
            hueField.clearFocus();
        });
    }

    private Drawable getThumb(int progress) {
        View thumbView = LayoutInflater.from(getContext()).inflate(R.layout.seekbar_tv, null, false);
        ((TextView) thumbView.findViewById(R.id.tvProgress)).setText(progress + "");
        thumbView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(thumbView.getMeasuredWidth(), thumbView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        thumbView.layout(0, 0, thumbView.getMeasuredWidth(), thumbView.getMeasuredHeight());
        thumbView.draw(canvas);
        return new BitmapDrawable(getResources(), bitmap);
    }

    public void createKeyframeElement(EditingActivity.Clip clip, EditingActivity.Keyframe keyframe, Runnable onClickKeyframe, Runnable onLongClickKeyframe)
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
            onLongClickKeyframe.run();

            keyframeScrollFrame.removeView(layout);
            return true;
        });

        layout.addView(text);

        keyframeScrollFrame.addView(layout);
    }

}
