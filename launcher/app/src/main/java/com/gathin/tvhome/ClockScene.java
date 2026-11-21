package com.gathin.tvhome;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Screensaver scene (layout dream_clock):
 * - Nature or Art modes: rotated CC0/public-domain 1080p pictures with smooth crossfades,
 *   top-left time + weather, bottom-right artwork/photo title and author/location.
 * - Clock mode: classic drifting clock and date on black over a slow ambient glow.
 */
final class ClockScene {

    private static final long MOVE_MS = 60_000L;
    private static final long ROTATION_MS = 60_000L;
    private static final int GLOW_STEPS = 16;
    private static final long GLOW_STEP_MS = 500L;
    private static final int[] PALETTE = {0xFF2B5CFF, 0xFF00A39A, 0xFF8A3FFC, 0xFFE0661B, 0xFFD6336C};

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final Context context;
    private final View content;
    private final ClockRefresh clockRefresh;
    private final ImageView ambient;
    private final ImageView ambientNext;
    private final View topLeft;
    private final TextView dreamWeather;
    private final View bottomRight;
    private final TextView artworkTitle;
    private final TextView artworkSubtitle;

    private final GradientDrawable glow;
    private int colorIndex;
    private int glowColor;
    private boolean wallpaperShown;

    private File wallpaperFile;
    private int wallpaperShade;
    private Resources wallpaperRes;
    private int wallpaperGeneration;
    private List<File> rotation;
    private long rotationMs;
    private long shownMs;
    private String order;
    private Map<String, Set<String>> moods;
    private String latitude, longitude;

    // Custom screensaver mode fields
    static class Item {
        final File file;
        final String title;
        final String subtitle;
        Item(File file, String title, String subtitle) {
            this.file = file;
            this.title = title;
            this.subtitle = subtitle;
        }
    }

    private String screensaverMode = "nature";
    private final List<Item> screensaverItems = new ArrayList<>();
    private int screensaverIndex = 0;
    private Bitmap currentBmp = null;
    private Bitmap nextBmp = null;
    private int itemGeneration = 0;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            moveClock();
            shiftGlow();
            advanceWallpaper();
            handler.postDelayed(this, MOVE_MS);
        }
    };

    private final Runnable screensaverRotate = new Runnable() {
        @Override
        public void run() {
            advanceScreensaverImage();
        }
    };

    ClockScene(View root) {
        context = root.getContext();
        content = root.findViewById(R.id.dreamContent);
        clockRefresh = new ClockRefresh(content);
        ambient = root.findViewById(R.id.dreamAmbient);
        ambientNext = root.findViewById(R.id.dreamAmbientNext);
        topLeft = root.findViewById(R.id.dreamTopLeft);
        dreamWeather = root.findViewById(R.id.dreamWeather);
        bottomRight = root.findViewById(R.id.dreamBottomRight);
        artworkTitle = root.findViewById(R.id.dreamArtworkTitle);
        artworkSubtitle = root.findViewById(R.id.dreamArtworkSubtitle);

        glow = new GradientDrawable();
        glow.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        ambient.setBackground(glow);
        ambient.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) ->
                glow.setGradientRadius((r - l) * 0.55f));
        colorIndex = random.nextInt(PALETTE.length);
        setGlow(PALETTE[colorIndex]);
    }

    void start() {
        screensaverMode = context.getSharedPreferences(Config.SETTINGS_NAME, Context.MODE_PRIVATE)
                .getString("screensaver_mode", "nature");

        if ("nature".equals(screensaverMode) || "art".equals(screensaverMode) || "mix".equals(screensaverMode)) {
            loadScreensaverItems(screensaverMode);
            if (!screensaverItems.isEmpty()) {
                startImageScreensaver();
                return;
            }
        }

        // Fallback or "clock" mode
        startClockScreensaver();
    }

    private void startClockScreensaver() {
        if (topLeft != null) topLeft.setVisibility(View.GONE);
        if (bottomRight != null) bottomRight.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);
        content.setAlpha(0f);
        content.post(() -> {
            placeClock();
            content.animate().alpha(1f).setDuration(1200).start();
        });
        handler.removeCallbacks(tick);
        handler.postDelayed(tick, MOVE_MS);
        clockRefresh.start();
        if (wallpaperFile != null && !wallpaperShown) loadWallpaper();
    }

    private void startImageScreensaver() {
        content.setVisibility(View.GONE);
        ambient.setBackground(null);
        if (topLeft != null) {
            topLeft.setVisibility(View.VISIBLE);
            updateWeatherText();
        }
        if (bottomRight != null) {
            bottomRight.setVisibility(View.VISIBLE);
            bottomRight.setAlpha(0f);
        }

        screensaverIndex = 0;
        showFirstScreensaverImage();
    }

    private void updateWeatherText() {
        if (dreamWeather == null) return;
        String w = WeatherManager.getCachedNow(context);
        if (w != null && !w.isEmpty()) {
            dreamWeather.setText(w);
            dreamWeather.setVisibility(View.VISIBLE);
        } else {
            dreamWeather.setVisibility(View.GONE);
        }
    }

    private void loadScreensaverItems(String mode) {
        screensaverItems.clear();
        if ("mix".equals(mode)) {
            loadDirectoryItems(new File("/sdcard/Android/data/com.gathin.tvhome/files/screensaver/nature"));
            loadDirectoryItems(new File("/sdcard/Android/data/com.gathin.tvhome/files/screensaver/art"));
        } else {
            loadDirectoryItems(new File("/sdcard/Android/data/com.gathin.tvhome/files/screensaver/" + mode));
        }
        if (!screensaverItems.isEmpty()) {
            Collections.shuffle(screensaverItems, random);
        }
    }

    private void loadDirectoryItems(File dir) {
        if (!dir.exists() || !dir.isDirectory()) return;

        Map<String, String[]> captions = new HashMap<>();
        File capFile = new File(dir, "captions.txt");
        if (capFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(capFile), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    String[] parts = line.split("\t");
                    if (parts.length >= 2) {
                        String sub = parts.length >= 3 ? parts[2] : "";
                        captions.put(parts[0], new String[]{parts[1], sub});
                    }
                }
            } catch (Exception ignored) {}
        }

        File[] files = dir.listFiles((d, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
        });
        if (files != null) {
            for (File f : files) {
                String[] meta = captions.get(f.getName());
                if (meta != null) {
                    screensaverItems.add(new Item(f, meta[0], meta[1]));
                } else {
                    screensaverItems.add(new Item(f, f.getName(), ""));
                }
            }
        }
    }

    private void showFirstScreensaverImage() {
        if (screensaverItems.isEmpty()) return;
        Item item = screensaverItems.get(screensaverIndex);
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int w = dm.widthPixels, h = dm.heightPixels;
        int gen = ++itemGeneration;

        new Thread(() -> {
            Bitmap bmp = decode(item.file, w, h);
            if (bmp == null) return;
            handler.post(() -> {
                if (gen != itemGeneration) {
                    bmp.recycle();
                    return;
                }
                ambient.setImageBitmap(bmp);
                ambient.setAlpha(0f);
                ambient.animate().alpha(1f).setDuration(1200).start();
                currentBmp = bmp;

                if (artworkTitle != null) artworkTitle.setText(item.title);
                if (artworkSubtitle != null) artworkSubtitle.setText(item.subtitle);
                if (bottomRight != null) {
                    bottomRight.animate().alpha(1f).setDuration(1200).start();
                }

                handler.removeCallbacks(screensaverRotate);
                handler.postDelayed(screensaverRotate, ROTATION_MS);
            });
        }, "screensaver-decode").start();
    }

    private void advanceScreensaverImage() {
        if (screensaverItems.size() < 2) {
            handler.postDelayed(screensaverRotate, ROTATION_MS);
            return;
        }
        int next = screensaverIndex + 1;
        if (next >= screensaverItems.size()) {
            Collections.shuffle(screensaverItems, random);
            next = 0;
        }
        final int nextIndex = next;
        Item nextItem = screensaverItems.get(nextIndex);
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int w = dm.widthPixels, h = dm.heightPixels;
        int gen = ++itemGeneration;

        new Thread(() -> {
            Bitmap bmp = decode(nextItem.file, w, h);
            if (bmp == null) {
                handler.postDelayed(screensaverRotate, ROTATION_MS);
                return;
            }
            handler.post(() -> {
                if (gen != itemGeneration) {
                    bmp.recycle();
                    return;
                }
                nextBmp = bmp;
                ambientNext.setImageBitmap(nextBmp);
                ambientNext.setAlpha(0f);
                ambientNext.animate().alpha(1f).setDuration(1400).withEndAction(() -> {
                    ambient.setImageBitmap(nextBmp);
                    ambient.setAlpha(1f);
                    if (currentBmp != null && !currentBmp.isRecycled()) {
                        currentBmp.recycle();
                    }
                    currentBmp = nextBmp;
                    nextBmp = null;
                    ambientNext.setImageDrawable(null);
                    ambientNext.setAlpha(0f);
                    screensaverIndex = nextIndex;
                    updateWeatherText();
                    handler.removeCallbacks(screensaverRotate);
                    handler.postDelayed(screensaverRotate, ROTATION_MS);
                }).start();

                if (bottomRight != null) {
                    bottomRight.animate().alpha(0f).setDuration(400).withEndAction(() -> {
                        if (artworkTitle != null) artworkTitle.setText(nextItem.title);
                        if (artworkSubtitle != null) artworkSubtitle.setText(nextItem.subtitle);
                        bottomRight.animate().alpha(1f).setDuration(800).start();
                    }).start();
                }
            });
        }, "screensaver-decode").start();
    }

    void stop() {
        handler.removeCallbacksAndMessages(null);
        clockRefresh.stop();
        content.animate().cancel();
        ambient.animate().cancel();
        ambientNext.animate().cancel();
        if (bottomRight != null) bottomRight.animate().cancel();
        wallpaperGeneration++;
        itemGeneration++;

        if (currentBmp != null && !currentBmp.isRecycled()) {
            currentBmp.recycle();
            currentBmp = null;
        }
        if (nextBmp != null && !nextBmp.isRecycled()) {
            nextBmp.recycle();
            nextBmp = null;
        }
        ambient.setImageDrawable(null);
        ambientNext.setImageDrawable(null);
    }

    void useHomeWallpaper(Context ctx) {
        String mode = ctx.getSharedPreferences(Config.SETTINGS_NAME, Context.MODE_PRIVATE)
                .getString("screensaver_mode", "nature");
        if (!"clock".equals(mode)) return;

        Config cfg;
        try {
            cfg = Config.load(ctx);
        } catch (RuntimeException e) {
            return;
        }
        if (!cfg.clockWallpaper) {
            if (!cfg.clockGlow) {
                wallpaperShown = true;
                ambient.setBackground(null);
            }
            return;
        }
        if (cfg.wallpapers.isEmpty()) {
            wallpaperFile = null;
        } else {
            rotation = new ArrayList<>(cfg.wallpapers);
            String shown = ctx.getSharedPreferences("wallpaper_rotation", Context.MODE_PRIVATE)
                    .getString("image", "");
            File current = WallpaperRotation.find(rotation, shown);
            wallpaperFile = current != null ? current : rotation.get(0);
            rotationMs = cfg.wallpaperRotationMinutes * 60_000L;
            order = cfg.wallpaperOrder;
            moods = cfg.wallpaperMoods;
            latitude = moods.isEmpty() ? "" : ctx.getString(R.string.latitude);
            longitude = moods.isEmpty() ? "" : ctx.getString(R.string.longitude);
        }
        wallpaperShade = (cfg.clockWallpaperDim * 255 / 100) << 24;
        wallpaperRes = ctx.getResources();
    }

    private void loadWallpaper() {
        File file = wallpaperFile;
        int shade = wallpaperShade;
        Resources res = wallpaperRes;
        DisplayMetrics dm = res.getDisplayMetrics();
        int w = dm.widthPixels, h = dm.heightPixels;
        int generation = ++wallpaperGeneration;
        new Thread(() -> {
            Bitmap bmp = decode(file, w, h);
            if (bmp == null) return;
            handler.post(() -> {
                if (generation != wallpaperGeneration || wallpaperShown) {
                    bmp.recycle();
                    return;
                }
                wallpaperShown = true;
                ambient.setBackground(null);
                ambient.setImageDrawable(new LayerDrawable(new Drawable[]{
                        new BitmapDrawable(res, bmp), new ColorDrawable(shade)}));
            });
        }, "clock-wallpaper").start();
    }

    private static Bitmap decode(File file, int screenW, int screenH) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getPath(), options);
            int width = options.outWidth, height = options.outHeight;
            if (width <= 0 || height <= 0) return null;
            int sample = WallpaperSize.sampleSize(width, height, screenW, screenH);
            if (!WallpaperSize.accepted(width / sample, height / sample)) return null;
            options.inJustDecodeBounds = false;
            options.inSampleSize = sample;
            int[] scale = WallpaperSize.coverScale(width / sample, height / sample, screenW, screenH);
            if (scale != null) {
                options.inScaled = true;
                options.inDensity = scale[0];
                options.inTargetDensity = scale[1];
            }
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            return BitmapFactory.decodeFile(file.getPath(), options);
        } catch (Exception | OutOfMemoryError failure) {
            return null;
        }
    }

    private void advanceWallpaper() {
        if (rotation == null || rotationMs <= 0 || rotation.size() < 2) return;
        shownMs += MOVE_MS;
        if (shownMs < rotationMs) return;
        shownMs = 0;
        File next = WallpaperRotation.pick(playable(), wallpaperFile.getName(), order, random);
        if (next.equals(wallpaperFile)) return;
        wallpaperFile = next;
        wallpaperShown = false;
        loadWallpaper();
    }

    private List<File> playable() {
        if (moods.isEmpty()) return rotation;
        LocalDateTime now = LocalDateTime.now();
        int[] sun = WallpaperMood.sun(now.toLocalDate(), latitude, longitude, ZoneId.systemDefault());
        return WallpaperMood.select(rotation, moods,
                WallpaperMood.timeOfDay(now.getHour() * 60 + now.getMinute(), sun[0], sun[1]));
    }

    private void moveClock() {
        content.animate().alpha(0f).setDuration(900).withLayer().withEndAction(() -> {
            placeClock();
            content.animate().alpha(1f).setDuration(1200).withLayer().start();
        }).start();
    }

    private void placeClock() {
        View parent = (View) content.getParent();
        int maxX = Math.max(0, parent.getWidth() - content.getWidth());
        int maxY = Math.max(0, parent.getHeight() - content.getHeight());
        float x = maxX * (0.1f + 0.8f * random.nextFloat());
        float y = maxY * (0.1f + 0.8f * random.nextFloat());
        content.setX(x);
        content.setY(y);
        if (parent.getWidth() > 0 && parent.getHeight() > 0) {
            glow.setGradientCenter((x + content.getWidth() / 2f) / parent.getWidth(),
                    (y + content.getHeight() / 2f) / parent.getHeight());
        }
    }

    private void shiftGlow() {
        if (wallpaperShown) return;
        int from = glowColor;
        colorIndex = (colorIndex + 1) % PALETTE.length;
        int to = PALETTE[colorIndex];
        ArgbEvaluator mix = new ArgbEvaluator();
        for (int i = 1; i <= GLOW_STEPS; i++) {
            float f = (float) i / GLOW_STEPS;
            handler.postDelayed(() -> setGlow((int) mix.evaluate(f, from, to)), i * GLOW_STEP_MS);
        }
    }

    private void setGlow(int color) {
        glowColor = color;
        int c = color & 0x00FFFFFF;
        glow.setColors(new int[]{0x40000000 | c, 0x14000000 | c, 0x00000000 | c});
    }
}
