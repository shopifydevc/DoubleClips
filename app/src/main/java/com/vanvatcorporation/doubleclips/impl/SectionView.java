package com.vanvatcorporation.doubleclips.impl;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.vanvatcorporation.doubleclips.R;

public class SectionView extends LinearLayout {

    private static final int CHILD_STAGGER_SPEED_MS = 150;
    private static final int ANIMATION_DURATION = 300;

    private int contentHeight = 0;
    private boolean isOpenAtStart;
    LinearLayout sectionHeader;
    LinearLayout sectionContent;
    ImageView sectionArrow;

    public SectionView(Context context) {
        super(context);
        preInit(context, null);
    }

    public SectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        preInit(context, attrs);
    }

    public SectionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        preInit(context, attrs);
    }

    public SectionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        preInit(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // After all children are added and measured
        if(!isOpenAtStart)
            post(() -> {
                sectionContent.setVisibility(View.GONE);
                System.err.println(contentHeight);
            });

    }

    // Move all the child from the XML into the sectionContent
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (sectionContent == null) {
            super.addView(child, index, params); // before init
        } else {
            // put user-declared children into sectionContent
            sectionContent.addView(child, index, params);

            child.post(() -> {
                // Random 25 px number if child couldn't measured
                contentHeight += child.getHeight() <= 0 ? 25 : child.getHeight();
            });
        }
    }


    void preInit(Context context, @Nullable AttributeSet attrs)
    {
        setOrientation(VERTICAL);

        inflate(context, R.layout.merge_section_view, this); // inject base layout

        sectionHeader = findViewById(R.id.sectionHeader);
        sectionContent = findViewById(R.id.sectionContent);
        sectionArrow = findViewById(R.id.sectionArrow);


        TextView titleView = findViewById(R.id.sectionTitle);
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SectionView);
            String title = ta.getString(R.styleable.SectionView_sectionTitle);
            isOpenAtStart = ta.getBoolean(R.styleable.SectionView_isOpenAtStart, false);
            if (title != null) {
                titleView.setText(title);
            }
            ta.recycle();
        }


        sectionHeader.setOnClickListener(v -> {
            boolean isVisible = sectionContent.getVisibility() == View.VISIBLE;
            AutoTransition transition = new AutoTransition();
            transition.setDuration(ANIMATION_DURATION); // smooth animation
            TransitionManager.beginDelayedTransition((ViewGroup) sectionHeader.getParent(), transition);
            if (isVisible) {
                sectionArrow.setImageResource(R.drawable.baseline_keyboard_arrow_down_24);
                collapse(sectionContent);
            } else {
                sectionArrow.setImageResource(R.drawable.baseline_keyboard_arrow_down_24); // Up
                expand(sectionContent); // target height in dp
            }

            sectionArrow.animate()
                    .rotation(isVisible ? 0 : 180)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new android.view.animation.OvershootInterpolator())
                    .start();

            if (!isVisible)
                for (int i = 0; i < sectionContent.getChildCount(); i++) {
                    View child = sectionContent.getChildAt(i);
                    child.setAlpha(0f);
                    child.setTranslationY(50f);

                    child.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setStartDelay((long) i * CHILD_STAGGER_SPEED_MS) // stagger by milliseconds
                            .setDuration(ANIMATION_DURATION)
                            .start();
                }


        });


    }

    private void expand(final View view) {
        view.setVisibility(View.VISIBLE);
        // Measure the content’s natural height

        final int targetHeight = contentHeight;//view.getMeasuredHeight();

        ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
        animator.setDuration(ANIMATION_DURATION);
        animator.addUpdateListener(animation -> {
            int value = (Integer) animation.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = value;
            view.setLayoutParams(layoutParams);
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                view.setLayoutParams(layoutParams);
            }
        });
        animator.start();
    }

    private void collapse(final View view) {
        final int initialHeight = view.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
        animator.setDuration(ANIMATION_DURATION);
        animator.addUpdateListener(animation -> {
            int value = (Integer) animation.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = value;
            view.setLayoutParams(layoutParams);
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
        animator.start();
    }
}
