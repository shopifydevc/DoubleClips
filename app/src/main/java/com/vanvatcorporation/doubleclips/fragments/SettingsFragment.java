package com.vanvatcorporation.doubleclips.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.vanvatcorporation.doubleclips.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey);
    }
}
