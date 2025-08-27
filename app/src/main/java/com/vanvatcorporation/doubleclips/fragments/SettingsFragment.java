package com.vanvatcorporation.doubleclips.fragments;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.vanvatcorporation.doubleclips.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey);

        ListPreference themePref = findPreference("theme_mode");
        if (themePref != null) {
            themePref.setOnPreferenceChangeListener((preference, newValue) -> {
                String value = newValue.toString();
                switch (value) {
                    case "dark":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                    case "light":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                    case "system":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        break;
                }

                // Recreate the activity to apply the theme immediately
                if (getActivity() != null) {
                    getActivity().recreate();
                }

                return true;
            });
        }
    }
}
