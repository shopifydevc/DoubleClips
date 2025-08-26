package com.vanvatcorporation.doubleclips.activities;

import android.os.Bundle;

import com.vanvatcorporation.doubleclips.R;
import com.vanvatcorporation.doubleclips.fragments.SettingsFragment;
import com.vanvatcorporation.doubleclips.impl.AppCompatActivityImpl;

public class SettingsActivity extends AppCompatActivityImpl {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_settings);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, new SettingsFragment())
                    .commit();
        }
    }
}
