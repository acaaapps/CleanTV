package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages loading, filtering, rotating, and displaying quotes and poems in Home.
 * Distinguishes poetry (serif font + vertical accent bar) from quotes (italic).
 * Fully offline, zero network access.
 */
public final class QuotesManager {

    public static class Entry {
        public final String id;
        public final String category;
        public final String type; // "quote" or "poem"
        public final String text;
        public final String author;
        public final String source;
        public final String edition;

        public Entry(String id, String category, String type, String text, String author, String source, String edition) {
            this.id = id;
            this.category = category;
            this.type = type;
            this.text = text;
            this.author = author;
            this.source = source;
            this.edition = edition;
        }
    }

    private static final String JSON_PATH = "/sdcard/Android/data/com.gathin.tvhome/files/quotes.json";
    private static final long POEM_DURATION_MS = 50_000L;
    private static final long QUOTE_DURATION_MS = 25_000L;

    private final Context context;
    private final SharedPreferences prefs;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private View container;
    private View accentLine;
    private TextView quoteText;
    private TextView quoteAuthor;

    private final List<Entry> masterList = new ArrayList<>();
    private final List<Entry> activePool = new ArrayList<>();
    private int poolIndex = 0;
    private boolean running = false;
    private Entry forcedEntry = null;

    private final Runnable rotateRunnable = new Runnable() {
        @Override
        public void run() {
            advance();
        }
    };

    public QuotesManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(Config.SETTINGS_NAME, Context.MODE_PRIVATE);
        loadDatabase();
    }

    public void bindViews(View container, View accentLine, TextView quoteText, TextView quoteAuthor) {
        this.container = container;
        this.accentLine = accentLine;
        this.quoteText = quoteText;
        this.quoteAuthor = quoteAuthor;
    }

    public void start() {
        if (container == null || quoteText == null) return;
        boolean enabled = prefs.getBoolean("quotes_enabled", true);
        if (!enabled) {
            container.setVisibility(View.GONE);
            return;
        }

        if (forcedEntry != null) {
            running = true;
            container.setAlpha(1f);
            container.setVisibility(View.VISIBLE);
            showEntry(forcedEntry);
            return;
        }

        rebuildPool();
        if (activePool.isEmpty()) {
            container.setVisibility(View.GONE);
            return;
        }

        running = true;
        container.setAlpha(1f);
        container.setVisibility(View.VISIBLE);
        showEntry(activePool.get(poolIndex));
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(rotateRunnable);
        if (container != null) {
            container.animate().cancel();
        }
    }

    private void advance() {
        if (!running || activePool.isEmpty()) return;

        poolIndex++;
        if (poolIndex >= activePool.size()) {
            poolIndex = 0;
            Collections.shuffle(activePool);
        }

        final Entry next = activePool.get(poolIndex);
        if (container != null) {
            container.animate().alpha(0f).setDuration(500).withEndAction(() -> {
                showEntry(next);
                if (container != null) {
                    container.animate().alpha(1f).setDuration(700).start();
                }
            }).start();
        } else {
            showEntry(next);
        }
    }

    private void showEntry(Entry entry) {
        if (quoteText == null || quoteAuthor == null) return;

        quoteText.setGravity(android.view.Gravity.END | android.view.Gravity.RIGHT);
        quoteAuthor.setGravity(android.view.Gravity.END | android.view.Gravity.RIGHT);

        boolean isPoem = "poem".equals(entry.type);
        if (accentLine != null) {
            accentLine.setVisibility(isPoem ? View.VISIBLE : View.GONE);
        }

        if (isPoem) {
            quoteText.setTypeface(Typeface.SERIF, Typeface.NORMAL);
            quoteText.setText(entry.text);
            quoteAuthor.setText("— " + entry.source + " · " + entry.author);
        } else {
            quoteText.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
            quoteText.setText("«" + entry.text + "»");
            quoteAuthor.setText("— " + entry.author + " (" + entry.source + ")");
        }

        long delay;
        String speed = prefs.getString("quotes_speed", "normal");
        if ("fast".equals(speed)) {
            delay = isPoem ? 30_000L : 15_000L;
        } else if ("slow".equals(speed)) {
            delay = isPoem ? 90_000L : 45_000L;
        } else {
            delay = isPoem ? POEM_DURATION_MS : QUOTE_DURATION_MS;
        }
        handler.removeCallbacks(rotateRunnable);
        if (running) {
            handler.postDelayed(rotateRunnable, delay);
        }
    }

    public void showSpecific(String id) {
        for (Entry e : masterList) {
            if (e.id.equals(id)) {
                forcedEntry = e;
                if (container != null) {
                    container.setVisibility(View.VISIBLE);
                    container.setAlpha(1f);
                }
                showEntry(e);
                handler.removeCallbacks(rotateRunnable);
                return;
            }
        }
    }

    public void rebuildPool() {
        activePool.clear();
        String filter = prefs.getString("quotes_type_filter", "all"); // "all", "quotes_only", "poems_only"

        for (Entry e : masterList) {
            if ("quotes_only".equals(filter) && "poem".equals(e.type)) continue;
            if ("poems_only".equals(filter) && "quote".equals(e.type)) continue;

            // Check author toggle (default: true)
            boolean authorEnabled = prefs.getBoolean("quote_author_" + e.category, true);
            if (!authorEnabled) continue;

            activePool.add(e);
        }

        Collections.shuffle(activePool);
        poolIndex = 0;
    }

    private void loadDatabase() {
        masterList.clear();
        File extDir = context.getExternalFilesDir(null);
        File file = extDir != null ? new File(extDir, "quotes.json") : new File(JSON_PATH);
        if (!file.exists()) {
            file = new File(JSON_PATH);
        }
        if (!file.exists()) {
            file = new File(context.getFilesDir(), "quotes.json");
        }
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONArray arr = new JSONArray(sb.toString());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                masterList.add(new Entry(
                        obj.optString("id", ""),
                        obj.optString("category", ""),
                        obj.optString("type", "quote"),
                        obj.optString("text", ""),
                        obj.optString("author", ""),
                        obj.optString("source", ""),
                        obj.optString("edition", "")
                ));
            }
        } catch (Exception ignored) {}
    }
}
