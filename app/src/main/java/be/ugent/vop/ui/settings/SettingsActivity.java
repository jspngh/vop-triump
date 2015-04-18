package be.ugent.vop.ui.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;

import be.ugent.vop.R;
import be.ugent.vop.ui.main.MainActivity;
import be.ugent.vop.utils.PrefUtils;

import java.util.List;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        boolean darkTheme = PrefUtils.getDarkTheme(this);
        if(darkTheme) setTheme(R.style.AppTheme_Dark);
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PrefUtils.registerOnSharedPreferenceChangeListener(this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PrefUtils.unregisterOnSharedPreferenceChangeListener(this, this);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PrefUtils.THEME_DARK)) {
            recreate();
        }
    }

    @Override
    public void onBackPressed()
    {
        //Say fuck you to the user, it's the easiest way
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
