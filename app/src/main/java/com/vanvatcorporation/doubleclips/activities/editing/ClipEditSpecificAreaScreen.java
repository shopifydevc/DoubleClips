package com.vanvatcorporation.doubleclips.activities.editing;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

import com.vanvatcorporation.doubleclips.R;

public class ClipEditSpecificAreaScreen extends BaseEditSpecificAreaScreen {

    public TextView totalDurationText;
    public EditText clipNameField, durationContent, positionXField, positionYField, rotationField, scaleXField, scaleYField, opacityField;


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
        positionXField = findViewById(R.id.positionXField);
        positionYField = findViewById(R.id.positionYField);
        rotationField = findViewById(R.id.rotationField);
        scaleXField = findViewById(R.id.scaleXField);
        scaleYField = findViewById(R.id.scaleYField);
        opacityField = findViewById(R.id.opacityField);
    }

}
