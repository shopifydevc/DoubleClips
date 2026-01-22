package com.vanvatcorporation.doubleclips.activities.template;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.slider.RangeSlider;
import com.vanvatcorporation.doubleclips.R;
import com.vanvatcorporation.doubleclips.activities.editing.BaseEditSpecificAreaScreen;

public class TemplateExportPropertiesAreaScreen extends BaseEditSpecificAreaScreen {

    public ShapeableImageView previewImage;
    public TextView titleText, dataInfoText, lengthText;
    public RangeSlider trimRangeSlider;


    public TemplateExportPropertiesAreaScreen(Context context) {
        super(context);
    }

    public TemplateExportPropertiesAreaScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TemplateExportPropertiesAreaScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TemplateExportPropertiesAreaScreen(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void init()
    {
        super.init();
        previewImage = findViewById(R.id.previewImage);
        titleText = findViewById(R.id.titleText);
        dataInfoText = findViewById(R.id.dataInfoText);
        lengthText = findViewById(R.id.lengthText);
        trimRangeSlider = findViewById(R.id.trimRangeSlider);


        onClose.add(() -> {
//            textEditContent.clearFocus();
//            textSizeContent.clearFocus();
        });

        animationScreen = AnimationScreen.ToBottom;
    }


}
