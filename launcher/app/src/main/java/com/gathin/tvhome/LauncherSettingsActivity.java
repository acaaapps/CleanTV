package com.gathin.tvhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Remote-friendly settings owned by the launcher; system settings stay one row away. */
public final class LauncherSettingsActivity extends Activity {
    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LauncherLocale.wrap(base));   // locale
    }

    static final String EXTRA_REORDER = "com.gathin.tvhome.extra.REORDER_FROM_SETTINGS";
    private SharedPreferences choices;
    private final TextView[] columnChoices = new TextView[4];
    private final TextView[] rowChoices = new TextView[4];
    private static final int[] DOCK_OPACITIES = {100, 70, 50, 30};
    private final TextView[] dockChoices = new TextView[DOCK_OPACITIES.length];
    private static final String[] CLOCK_FORMATS = {"system", "12", "24"};
    private final TextView[] clockChoices = new TextView[CLOCK_FORMATS.length];
    private static final String[] SCREENSAVER_MODES = {"nature", "art", "mix", "clock"};
    private final TextView[] screensaverChoices = new TextView[SCREENSAVER_MODES.length];
    private static final String[] SCREENSAVER_TIMEOUTS = {"30s", "1m", "5m"};
    private final TextView[] screensaverTimeoutChoices = new TextView[SCREENSAVER_TIMEOUTS.length];
    private TextView screensaverStartNow;
    private static final String[] QUOTES_TYPES = {"all", "quotes_only", "poems_only"};
    private final TextView[] quotesTypeChoices = new TextView[QUOTES_TYPES.length];
    private static final String[] QUOTES_SPEEDS = {"fast", "normal", "slow"};
    private final TextView[] quotesSpeedChoices = new TextView[QUOTES_SPEEDS.length];

    private static final String[] AUTHOR_KEYS = {
            "biblia", "poesia", "gracian", "seneca", "marco_aurelio",
            "maquiavelo", "epicteto", "sun_tzu", "franklin", "kempis", "tracy", "kiyosaki"
    };
    private static final String[] AUTHOR_NAMES = {
            "Biblia (Reina-Valera 1909)",
            "Poesía de dominio público",
            "Baltasar Gracián",
            "Séneca",
            "Marco Aurelio",
            "Nicolás Maquiavelo",
            "Epicteto",
            "Sun Tzu",
            "Benjamin Franklin",
            "Tomás de Kempis",
            "Brian Tracy",
            "Robert Kiyosaki"
    };

    private TextView quotesToggleChoice;
    private TextView quotesAuthorsChoice;
    private String screensaverTimeout;
    private boolean quotesEnabled;
    private String quotesTypeFilter;
    private String quotesSpeed;
    private TextView wallpaperChoice;
    private TextView animationsChoice;
    private TextView advancedChoice;
    private TextView followThemeChoice;
    private TextView openOnStartChoice;
    private int columns;
    private int rowsBelowDock;
    private int dockOpacity;
    private String clockFormat;
    private String screensaverMode;
    private boolean animations;
    private boolean advancedEffects;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_launcher_settings);
        choices = getSharedPreferences(Config.SETTINGS_NAME, MODE_PRIVATE);
        Config config = Config.load(this);
        View reorderChoice = findViewById(R.id.reorderChoice);
        reorderChoice.setVisibility(config.launcherSettingsButton ? View.VISIBLE : View.GONE);
        reorderChoice.setOnClickListener(v -> {
            setResult(RESULT_OK, new Intent().putExtra(EXTRA_REORDER, true));
            finish();
        });
        columns = config.columns;
        rowsBelowDock = config.rowsBelowDock;
        dockOpacity = config.dockOpacity;
        clockFormat = config.clockFormat;
        animations = config.animations;
        advancedEffects = config.animations && config.advancedEffects;

        int[] ids = {R.id.columns4, R.id.columns5, R.id.columns6, R.id.columns7};
        for (int i = 0; i < ids.length; i++) {
            final int value = i + 4;
            columnChoices[i] = findViewById(ids[i]);
            columnChoices[i].setOnClickListener(v -> {
                if (columns == value) return;
                columns = value;
                choices.edit().putInt("columns", value).apply();
                changed();
                refresh();
            });
        }
        int[] rowIds = {R.id.rows0, R.id.rows1, R.id.rows2, R.id.rows3};
        for (int i = 0; i < rowIds.length; i++) {
            final int value = i;
            rowChoices[i] = findViewById(rowIds[i]);
            rowChoices[i].setOnClickListener(v -> {
                if (rowsBelowDock == value) return;
                rowsBelowDock = value;
                choices.edit().putInt(Config.ROWS_BELOW_DOCK_KEY, value).apply();
                changed();
                refresh();
            });
        }
        int[] dockIds = {R.id.dock100, R.id.dock70, R.id.dock50, R.id.dock30};
        for (int i = 0; i < dockIds.length; i++) {
            final int value = DOCK_OPACITIES[i];
            dockChoices[i] = findViewById(dockIds[i]);
            dockChoices[i].setOnClickListener(v -> {
                if (dockOpacity == value) return;
                dockOpacity = value;
                choices.edit().putInt(Config.DOCK_OPACITY_KEY, value).apply();
                changed();
                refresh();
            });
        }
        int[] clockIds = {R.id.clockSystem, R.id.clock12, R.id.clock24};
        for (int i = 0; i < clockIds.length; i++) {
            final String value = CLOCK_FORMATS[i];
            clockChoices[i] = findViewById(clockIds[i]);
            clockChoices[i].setOnClickListener(v -> {
                if (value.equals(clockFormat)) return;
                clockFormat = value;
                choices.edit().putString(Config.CLOCK_FORMAT_KEY, value).apply();
                changed();
                refresh();
            });
        }
        screensaverMode = choices.getString("screensaver_mode", "nature");
        int[] screensaverIds = {R.id.screensaverNature, R.id.screensaverArt, R.id.screensaverMix, R.id.screensaverClock};
        for (int i = 0; i < screensaverIds.length; i++) {
            final String value = SCREENSAVER_MODES[i];
            screensaverChoices[i] = findViewById(screensaverIds[i]);
            screensaverChoices[i].setOnClickListener(v -> {
                if (value.equals(screensaverMode)) return;
                screensaverMode = value;
                choices.edit().putString("screensaver_mode", value).apply();
                changed();
                refresh();
            });
        }
        screensaverTimeout = choices.getString("screensaver_timeout", "1m");
        screensaverStartNow = findViewById(R.id.screensaverStartNow);
        if (screensaverStartNow != null) {
            screensaverStartNow.setOnClickListener(v -> {
                startActivity(new Intent(this, ClockActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            });
        }
        int[] screensaverTimeoutIds = {R.id.screensaverTimeout30s, R.id.screensaverTimeout1m, R.id.screensaverTimeout5m};
        for (int i = 0; i < screensaverTimeoutIds.length; i++) {
            final String value = SCREENSAVER_TIMEOUTS[i];
            screensaverTimeoutChoices[i] = findViewById(screensaverTimeoutIds[i]);
            screensaverTimeoutChoices[i].setOnClickListener(v -> {
                if (value.equals(screensaverTimeout)) return;
                screensaverTimeout = value;
                choices.edit().putString("screensaver_timeout", value).apply();
                changed();
                refresh();
            });
        }

        quotesEnabled = choices.getBoolean("quotes_enabled", true);
        quotesToggleChoice = findViewById(R.id.quotesToggleChoice);
        quotesToggleChoice.setOnClickListener(v -> {
            quotesEnabled = !quotesEnabled;
            choices.edit().putBoolean("quotes_enabled", quotesEnabled).apply();
            changed();
            refresh();
        });

        quotesTypeFilter = choices.getString("quotes_type_filter", "all");
        int[] quotesTypeIds = {R.id.quotesTypeAll, R.id.quotesTypeQuotes, R.id.quotesTypePoems};
        for (int i = 0; i < quotesTypeIds.length; i++) {
            final String value = QUOTES_TYPES[i];
            quotesTypeChoices[i] = findViewById(quotesTypeIds[i]);
            quotesTypeChoices[i].setOnClickListener(v -> {
                if (value.equals(quotesTypeFilter)) return;
                quotesTypeFilter = value;
                choices.edit().putString("quotes_type_filter", value).apply();
                changed();
                refresh();
            });
        }

        quotesSpeed = choices.getString("quotes_speed", "normal");
        int[] quotesSpeedIds = {R.id.quotesSpeedFast, R.id.quotesSpeedNormal, R.id.quotesSpeedSlow};
        for (int i = 0; i < quotesSpeedIds.length; i++) {
            final String value = QUOTES_SPEEDS[i];
            quotesSpeedChoices[i] = findViewById(quotesSpeedIds[i]);
            quotesSpeedChoices[i].setOnClickListener(v -> {
                if (value.equals(quotesSpeed)) return;
                quotesSpeed = value;
                choices.edit().putString("quotes_speed", value).apply();
                changed();
                refresh();
            });
        }

        quotesAuthorsChoice = findViewById(R.id.quotesAuthorsChoice);
        quotesAuthorsChoice.setOnClickListener(v -> showAuthorsDialog());
        // Mirror text alignment without globally opting the launcher into RTL. Numeric rows
        // still need explicit ordering in Android's no-supportsRtl compatibility mode.
        if (getResources().getBoolean(R.bool.rtl_menu_bar))
            findViewById(android.R.id.content).setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        if (getResources().getBoolean(R.bool.rtl_app_grid)) {
            reverseRow(columnChoices);
            reverseRow(rowChoices);
            reverseRow(dockChoices);
            reverseRow(clockChoices);
            reverseRow(screensaverChoices);
            reverseRow(screensaverTimeoutChoices);
        }
        wallpaperChoice = findViewById(R.id.wallpaperChoice);
        wallpaperChoice.setOnClickListener(v -> showWallpaperPicker());
        animationsChoice = findViewById(R.id.animationsChoice);
        animationsChoice.setOnClickListener(v -> {
            animations = !animations;
            if (!animations) advancedEffects = false;
            choices.edit().putBoolean("animations", animations)
                    .putBoolean("advanced_effects", advancedEffects).apply();
            changed();
            refresh();
        });
        advancedChoice = findViewById(R.id.advancedChoice);
        advancedChoice.setOnClickListener(v -> {
            if (advancedEffects) {
                setAdvanced(false);
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.advanced_effects)
                        .setMessage(R.string.advanced_effects_warning)
                        .setPositiveButton(R.string.enable, (dialog, which) -> setAdvanced(true))
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });
        openOnStartChoice = findViewById(R.id.openOnStartChoice);
        if (config.openOnStartChoice) {
            openOnStartChoice.setVisibility(View.VISIBLE);
            openOnStartChoice.setOnClickListener(v -> showOpenOnStartPicker());
        }
        followThemeChoice = findViewById(R.id.followThemeChoice);
        followThemeChoice.setOnClickListener(v -> {
            if (!hasOnTvChoices()) return;
            choices.edit().remove("columns").remove(Config.ROWS_BELOW_DOCK_KEY).remove("animations")
                    .remove("advanced_effects").remove(Config.WALLPAPER_CHOICE_KEY)
                    .remove(Config.DOCK_OPACITY_KEY).remove(Config.CLOCK_FORMAT_KEY)
                    .remove("screensaver_mode").remove("screensaver_timeout")
                    .remove("quotes_enabled").remove("quotes_type_filter").remove("quotes_speed")
                    .remove(OpenOnStart.KEY).commit();
            Config current = Config.load(this);
            columns = current.columns;
            rowsBelowDock = current.rowsBelowDock;
            dockOpacity = current.dockOpacity;
            clockFormat = current.clockFormat;
            screensaverMode = choices.getString("screensaver_mode", "nature");
            screensaverTimeout = choices.getString("screensaver_timeout", "1m");
            quotesEnabled = choices.getBoolean("quotes_enabled", true);
            quotesTypeFilter = choices.getString("quotes_type_filter", "all");
            quotesSpeed = choices.getString("quotes_speed", "normal");
            animations = current.animations;
            advancedEffects = current.animations && current.advancedEffects;
            changed();
            refresh();
        });
        findViewById(R.id.defaultHomeChoice).setOnClickListener(v ->
                open(new Intent(this, HomeSettingsActivity.class)));
        findViewById(R.id.tvSettingsChoice).setOnClickListener(v ->
                open(SystemSettings.intent(this, Settings.ACTION_SETTINGS)));
        refresh();
        if (config.launcherSettingsButton) reorderChoice.requestFocus();
        else columnChoices[Math.max(0, Math.min(columns - 4, 3))].requestFocus();
    }

    private void reverseRow(TextView[] views) {
        LinearLayout row = (LinearLayout) views[0].getParent();
        row.removeAllViews();
        for (int i = views.length - 1; i >= 0; i--) row.addView(views[i]);
    }

    private void showWallpaperPicker() {
        File dir = Config.dir(this);
        List<File> images = new java.util.ArrayList<>(WallpaperRotation.images(
                dir == null ? null : new File(dir, "wallpapers")));
        // A theme with a single wallpaper.jpg offers that picture too (Config maps the choice back to it).
        File single = dir == null ? null : new File(dir, Config.WALLPAPER_NAME);
        if (single != null && single.isFile()) images.add(0, single);
        String[] labels = new String[images.size() + 1];
        for (int i = 0; i < images.size(); i++) labels[i] = images.get(i).getName();
        labels[images.size()] = getString(R.string.wallpaper_none);
        String current = choices.getString(Config.WALLPAPER_CHOICE_KEY, null);
        int checked = "none".equals(current) ? images.size()
                : indexOf(images, current);
        new AlertDialog.Builder(this)
                .setTitle(R.string.wallpaper_label)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    dialog.dismiss();
                    if (which == images.size()) {
                        choices.edit().putString(Config.WALLPAPER_CHOICE_KEY, "none").apply();
                    } else {
                        choices.edit().putString(Config.WALLPAPER_CHOICE_KEY,
                                images.get(which).getName()).apply();
                    }
                    changed();
                    Toast.makeText(this, R.string.wallpaper_applied, Toast.LENGTH_SHORT).show();
                    refresh();
                })
                .setNeutralButton(R.string.wallpaper_theme_default, (dialog, which) -> {
                    choices.edit().remove(Config.WALLPAPER_CHOICE_KEY).apply();
                    changed();
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /** Off, then every app with a TV entry point by name. Saved at once; it is read on the next boot. */
    private void showOpenOnStartPicker() {
        PackageManager pm = getPackageManager();
        List<String[]> apps = new ArrayList<>();
        Intent tv = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        for (ResolveInfo r : pm.queryIntentActivities(tv, 0)) {
            String pkg = r.activityInfo.packageName;
            if (pkg.equals(getPackageName())) continue;
            boolean seen = false;
            for (String[] a : apps) seen |= a[1].equals(pkg);
            if (!seen) apps.add(new String[]{r.loadLabel(pm).toString(), pkg});
        }
        Collections.sort(apps, (a, b) -> a[0].compareToIgnoreCase(b[0]));
        String[] labels = new String[apps.size() + 1];
        labels[0] = getString(R.string.off);
        String current = choices.getString(OpenOnStart.KEY, "");
        int checked = 0;
        for (int i = 0; i < apps.size(); i++) {
            labels[i + 1] = apps.get(i)[0];
            if (apps.get(i)[1].equals(current)) checked = i + 1;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.open_on_start_label)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    dialog.dismiss();
                    if (which == 0) choices.edit().remove(OpenOnStart.KEY).apply();
                    else {
                        OpenOnStart.skipThisBoot(this);
                        choices.edit().putString(OpenOnStart.KEY, apps.get(which - 1)[1]).apply();
                    }
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /** The picked app's name, its package when it is gone, or Off. */
    private String openOnStartLabel() {
        String pkg = choices.getString(OpenOnStart.KEY, "");
        if (pkg.isEmpty()) return getString(R.string.off);
        try {
            PackageManager pm = getPackageManager();
            return pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return pkg;
        }
    }

    private static int indexOf(List<File> images, String name) {
        if (name == null) return -1;
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).getName().equals(name)) return i;
        }
        return -1;
    }

    private void showAuthorsDialog() {
        boolean[] checked = new boolean[AUTHOR_KEYS.length];
        for (int i = 0; i < AUTHOR_KEYS.length; i++) {
            checked[i] = choices.getBoolean("quote_author_" + AUTHOR_KEYS[i], true);
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.quotes_authors_dialog_title)
                .setMultiChoiceItems(AUTHOR_NAMES, checked, (dialog, which, isChecked) -> {
                    choices.edit().putBoolean("quote_author_" + AUTHOR_KEYS[which], isChecked).apply();
                    changed();
                })
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    /**
     * A choice was saved. Home rebuilds when it next resumes because the change count moved, whether these
     * settings are left with Back or with the remote's Home button (which cancels the activity result).
     */
    private void changed() {
        setResult(RESULT_OK);
        choices.edit().putLong(Config.SETTINGS_REVISION_KEY,
                choices.getLong(Config.SETTINGS_REVISION_KEY, 0) + 1).apply();
    }

    private void setAdvanced(boolean enabled) {
        advancedEffects = enabled;
        choices.edit().putBoolean("advanced_effects", enabled).apply();
        changed();
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < columnChoices.length; i++) {
            columnChoices[i].setSelected(columns == i + 4);
        }
        for (int i = 0; i < rowChoices.length; i++) {
            rowChoices[i].setSelected(rowsBelowDock == i);
        }
        for (int i = 0; i < dockChoices.length; i++) {
            dockChoices[i].setSelected(dockOpacity == DOCK_OPACITIES[i]);
        }
        for (int i = 0; i < clockChoices.length; i++) {
            clockChoices[i].setSelected(CLOCK_FORMATS[i].equals(clockFormat));
        }
        for (int i = 0; i < screensaverChoices.length; i++) {
            screensaverChoices[i].setSelected(SCREENSAVER_MODES[i].equals(screensaverMode));
        }
        for (int i = 0; i < screensaverTimeoutChoices.length; i++) {
            screensaverTimeoutChoices[i].setSelected(SCREENSAVER_TIMEOUTS[i].equals(screensaverTimeout));
        }
        quotesToggleChoice.setText(getString(quotesEnabled ? R.string.quotes_enabled_on : R.string.quotes_enabled_off));
        for (int i = 0; i < quotesTypeChoices.length; i++) {
            quotesTypeChoices[i].setSelected(QUOTES_TYPES[i].equals(quotesTypeFilter));
        }
        for (int i = 0; i < quotesSpeedChoices.length; i++) {
            quotesSpeedChoices[i].setSelected(QUOTES_SPEEDS[i].equals(quotesSpeed));
        }
        animationsChoice.setText(getString(R.string.animations_state,
                getString(animations ? R.string.on : R.string.off)));
        advancedChoice.setText(getString(R.string.advanced_effects_state,
                getString(advancedEffects ? R.string.on : R.string.off)));
        advancedChoice.setEnabled(animations);
        advancedChoice.setAlpha(animations ? 1f : 0.5f);
        String wallpaperCurrent = choices.getString(Config.WALLPAPER_CHOICE_KEY, null);
        wallpaperChoice.setText(getString(R.string.wallpaper_state,
                "none".equals(wallpaperCurrent) ? getString(R.string.wallpaper_none)
                        : wallpaperCurrent != null ? wallpaperCurrent
                        : getString(R.string.wallpaper_theme_default)));
        openOnStartChoice.setText(getString(R.string.open_on_start_state, openOnStartLabel()));
        followThemeChoice.setSelected(!hasOnTvChoices());
    }

    private boolean hasOnTvChoices() {
        return choices.contains("columns") || choices.contains(Config.ROWS_BELOW_DOCK_KEY)
                || choices.contains("animations") || choices.contains("advanced_effects")
                || choices.contains(Config.WALLPAPER_CHOICE_KEY) || choices.contains(Config.DOCK_OPACITY_KEY)
                || choices.contains(Config.CLOCK_FORMAT_KEY) || choices.contains("screensaver_mode")
                || choices.contains("screensaver_timeout") || choices.contains("quotes_enabled")
                || choices.contains("quotes_type_filter") || choices.contains("quotes_speed")
                || choices.contains(OpenOnStart.KEY);
    }

    private void open(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException e) {
            Toast.makeText(this, R.string.not_available, Toast.LENGTH_LONG).show();
        }
    }
}
