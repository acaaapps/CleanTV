package com.gathin.tvhome;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Settings activity for the screensaver (ClockDream), launched by Android TV's system
 * settings when configured as the DreamService settingsActivity, or from launcher settings.
 */
public final class ScreensaverSettingsActivity extends Activity {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LauncherLocale.wrap(base));
    }

    private static final String[] MODES = {"nature", "art", "mix", "clock"};
    private final TextView[] modeViews = new TextView[MODES.length];

    private static final String[] TIMEOUTS = {"30s", "1m", "5m"};
    private final TextView[] timeoutViews = new TextView[TIMEOUTS.length];

    private SharedPreferences prefs;
    private String currentMode;
    private String currentTimeout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screensaver_settings);

        prefs = getSharedPreferences(Config.SETTINGS_NAME, MODE_PRIVATE);
        currentMode = prefs.getString("screensaver_mode", "nature");
        currentTimeout = prefs.getString("screensaver_timeout", "1m");

        int[] modeIds = {R.id.modeNature, R.id.modeArt, R.id.modeMix, R.id.modeClock};
        for (int i = 0; i < modeIds.length; i++) {
            final String m = MODES[i];
            modeViews[i] = findViewById(modeIds[i]);
            modeViews[i].setOnClickListener(v -> {
                if (m.equals(currentMode)) return;
                currentMode = m;
                prefs.edit().putString("screensaver_mode", m).apply();
                refresh();
            });
        }

        View startNow = findViewById(R.id.screensaverStartNow);
        if (startNow != null) {
            startNow.setOnClickListener(v -> {
                startActivity(new Intent(this, ClockActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            });
        }

        int[] intIds = {R.id.interval30s, R.id.interval1m, R.id.interval5m};
        for (int i = 0; i < intIds.length; i++) {
            final String timeout = TIMEOUTS[i];
            timeoutViews[i] = findViewById(intIds[i]);
            timeoutViews[i].setOnClickListener(v -> {
                if (timeout.equals(currentTimeout)) return;
                currentTimeout = timeout;
                prefs.edit().putString("screensaver_timeout", timeout).apply();
                refresh();
            });
        }

        refresh();
    }

    private void refresh() {
        for (int i = 0; i < modeViews.length; i++) {
            modeViews[i].setSelected(MODES[i].equals(currentMode));
        }
        for (int i = 0; i < timeoutViews.length; i++) {
            timeoutViews[i].setSelected(TIMEOUTS[i].equals(currentTimeout));
        }
    }
}
