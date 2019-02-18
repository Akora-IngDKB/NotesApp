package com.akoraingdkb.notesapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity implements Constants {
    private static String versionName;
    private static SharedPreferences mySharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mySharedPreferences = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        boolean usingNightTheme = mySharedPreferences.getBoolean(THEME_KEY, true);

        if (usingNightTheme) setTheme(R.style.NightTheme);
        else setTheme(R.style.DayTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set default preference values
        PreferenceManager.setDefaultValues(this, R.xml.preference, false);

        // Replace content of activity with preference fragment

        getFragmentManager().beginTransaction()
                .replace(R.id.settings_frame, new SettingsFragment())
                .commit();

        try {
            versionName = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * This is the preference fragment which will be added to the SettingsActivity
     */
    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        public SettingsFragment() {
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preference xml
            addPreferencesFromResource(R.xml.preference);

            Preference aboutPref = findPreference(PREF_KEY_ABOUT);
            aboutPref.setSummary("Version: " + versionName);
            aboutPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    return false;
                }
            });

            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            /* Handle preference change actions */
            if (key.equals(PREF_KEY_THEME)) {
                boolean value = sharedPreferences.getBoolean(key, true);
                mySharedPreferences.edit().putBoolean(THEME_KEY, value).apply();
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }
    }
}
