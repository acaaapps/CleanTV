package com.gathin.tvhome;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RecentsManager {
    private static final int MAX_RECENTS = 5;
    private static final String PREFS_NAME = "bg_apps_prefs";
    private static final String PREF_CLOSED = "closed_apps_set";

    private RecentsManager() {}

    private static Set<String> getClosedPackages(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return new HashSet<>(sp.getStringSet(PREF_CLOSED, Collections.emptySet()));
    }

    public static void closeApp(Context context, String pkg) {
        if (pkg == null) return;
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                am.killBackgroundProcesses(pkg);
            }
        } catch (Exception ignored) {}

        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(sp.getStringSet(PREF_CLOSED, Collections.emptySet()));
        set.add(pkg);
        sp.edit().putStringSet(PREF_CLOSED, set).apply();

        try {
            context.getSharedPreferences("last_opened", Context.MODE_PRIVATE)
                    .edit().remove(pkg).apply();
        } catch (Exception ignored) {}
    }

    public static void onAppOpened(Context context, String pkg) {
        if (pkg == null) return;
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(sp.getStringSet(PREF_CLOSED, Collections.emptySet()));
        if (set.remove(pkg)) {
            sp.edit().putStringSet(PREF_CLOSED, set).apply();
        }
    }

    public static List<String> getRecentPackages(Context context) {
        LinkedHashMap<String, Long> packageTimes = new LinkedHashMap<>();
        Set<String> closed = getClosedPackages(context);

        // 1. Query UsageStatsManager (when PACKAGE_USAGE_STATS is granted)
        try {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            if (usm != null) {
                long now = System.currentTimeMillis();
                List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - 86400000L * 7, now);
                if (stats != null && !stats.isEmpty()) {
                    for (UsageStats u : stats) {
                        String pkg = u.getPackageName();
                        if (u.getLastTimeUsed() > 0 && !context.getPackageName().equals(pkg) && !closed.contains(pkg)) {
                            Long cur = packageTimes.get(pkg);
                            long t = u.getLastTimeUsed();
                            if (cur == null || t > cur) {
                                packageTimes.put(pkg, t);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        // 2. Query LastOpened SharedPreferences
        try {
            SharedPreferences prefs = context.getSharedPreferences("last_opened", Context.MODE_PRIVATE);
            Map<String, ?> all = prefs.getAll();
            if (all != null) {
                for (Map.Entry<String, ?> e : all.entrySet()) {
                    String pkg = e.getKey();
                    if (!context.getPackageName().equals(pkg) && !closed.contains(pkg) && e.getValue() instanceof Long) {
                        long t = (Long) e.getValue();
                        Long cur = packageTimes.get(pkg);
                        if (cur == null || t > cur) {
                            packageTimes.put(pkg, t);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        // Sort by timestamp descending
        List<Map.Entry<String, Long>> sorted = new ArrayList<>(packageTimes.entrySet());
        Collections.sort(sorted, (a, b) -> Long.compare(b.getValue(), a.getValue()));

        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : sorted) {
            result.add(entry.getKey());
            if (result.size() >= MAX_RECENTS) break;
        }

        return result;
    }
}
