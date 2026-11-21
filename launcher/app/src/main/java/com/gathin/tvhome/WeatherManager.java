package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Open-Meteo weather widget for A Coruña, Galicia.
 * Updates at most once every 30 minutes, caches data in SharedPreferences,
 * and gracefully falls back to the last cached forecast if offline.
 */
public final class WeatherManager {
    private static final String PREFS_NAME = "tvhome_weather_cache";
    private static final String KEY_LAST_FETCH = "last_fetch";
    private static final String KEY_CACHED_NOW = "cached_now";
    private static final String KEY_CACHED_FORECAST = "cached_forecast";

    private static final long UPDATE_INTERVAL_MS = 30 * 60 * 1000L; // 30 minutes
    private static final int TIMEOUT_MS = 6000;

    private static final String CITY_NAME = "A Coruña";
    private static final String API_URL =
            "https://api.open-meteo.com/v1/forecast?latitude=43.3713&longitude=-8.3960"
            + "&current=temperature_2m,weather_code,is_day"
            + "&daily=weather_code,temperature_2m_max,temperature_2m_min"
            + "&timezone=Europe%2FMadrid&forecast_days=4";

    private final Context context;
    private final SharedPreferences prefs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean fetching = false;

    public WeatherManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static String getCachedNow(Context context) {
        if (context == null) return null;
        SharedPreferences p = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return p.getString(KEY_CACHED_NOW, null);
    }

    public void update(View weatherBlock, TextView weatherNow, TextView weatherForecast) {
        if (weatherBlock == null || weatherNow == null || weatherForecast == null) return;

        // 1. Immediately display cached data if available
        String cachedNow = prefs.getString(KEY_CACHED_NOW, null);
        String cachedForecast = prefs.getString(KEY_CACHED_FORECAST, null);
        if (cachedNow != null && cachedForecast != null) {
            weatherNow.setText(cachedNow);
            weatherForecast.setText(cachedForecast);
            weatherBlock.setVisibility(View.VISIBLE);
        }

        // 2. Check if update is needed (< 30 minutes since last successful fetch)
        long lastFetch = prefs.getLong(KEY_LAST_FETCH, 0L);
        long now = System.currentTimeMillis();
        if (now - lastFetch < UPDATE_INTERVAL_MS) {
            return;
        }

        // 3. Prevent duplicate concurrent fetches
        if (fetching) return;
        fetching = true;

        executor.execute(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "cleanTV/3.3");

                int status = conn.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    try (InputStream in = conn.getInputStream()) {
                        byte[] chunk = new byte[2048];
                        int n;
                        while ((n = in.read(chunk)) != -1) {
                            buffer.write(chunk, 0, n);
                        }
                    }
                    String jsonStr = buffer.toString(StandardCharsets.UTF_8.name());
                    JSONObject root = new JSONObject(jsonStr);

                    // Parse current
                    JSONObject current = root.getJSONObject("current");
                    int curTemp = (int) Math.round(current.getDouble("temperature_2m"));
                    int curCode = current.getInt("weather_code");
                    boolean isDay = current.optInt("is_day", 1) == 1;

                    String curIcon = wmoIcon(curCode, isDay);
                    String curDesc = wmoDesc(curCode, isDay);
                    String nowText = CITY_NAME + "  ·  " + curTemp + "°C " + curIcon + "  " + curDesc;

                    // Parse daily forecast
                    JSONObject daily = root.getJSONObject("daily");
                    JSONArray timeArr = daily.getJSONArray("time");
                    JSONArray codeArr = daily.getJSONArray("weather_code");
                    JSONArray maxArr = daily.getJSONArray("temperature_2m_max");
                    JSONArray minArr = daily.getJSONArray("temperature_2m_min");

                    StringBuilder forecastBuilder = new StringBuilder();
                    Locale esLocale = new Locale("es", "ES");
                    int count = Math.min(4, timeArr.length());

                    for (int i = 0; i < count; i++) {
                        String dateStr = timeArr.getString(i);
                        int dayCode = codeArr.getInt(i);
                        int maxT = (int) Math.round(maxArr.getDouble(i));
                        int minT = (int) Math.round(minArr.getDouble(i));
                        String dayIcon = wmoIcon(dayCode, true);

                        String dayLabel;
                        if (i == 0) {
                            dayLabel = "Hoy";
                        } else {
                            try {
                                LocalDate date = LocalDate.parse(dateStr);
                                String dayShort = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, esLocale);
                                if (dayShort.endsWith(".")) dayShort = dayShort.substring(0, dayShort.length() - 1);
                                dayLabel = dayShort.substring(0, 1).toUpperCase(esLocale) + dayShort.substring(1);
                            } catch (Exception e) {
                                dayLabel = dateStr;
                            }
                        }

                        if (i > 0) {
                            forecastBuilder.append("   ·   ");
                        }
                        forecastBuilder.append(dayLabel).append(" ").append(maxT).append("°/").append(minT).append("° ").append(dayIcon);
                    }

                    String forecastText = forecastBuilder.toString();

                    // Save to SharedPreferences cache
                    prefs.edit()
                            .putLong(KEY_LAST_FETCH, System.currentTimeMillis())
                            .putString(KEY_CACHED_NOW, nowText)
                            .putString(KEY_CACHED_FORECAST, forecastText)
                            .apply();

                    // Update UI on main thread
                    mainHandler.post(() -> {
                        weatherNow.setText(nowText);
                        weatherForecast.setText(forecastText);
                        weatherBlock.setVisibility(View.VISIBLE);
                    });
                }
                conn.disconnect();
            } catch (Exception ignored) {
                // Network error or timeout: if cache was shown, it stays shown.
            } finally {
                fetching = false;
            }
        });
    }

    public void destroy() {
        executor.shutdownNow();
    }

    private static String wmoIcon(int code, boolean isDay) {
        switch (code) {
            case 0:
                return isDay ? "☀️" : "🌙";
            case 1:
                return isDay ? "🌤️" : "🌤️";
            case 2:
                return "⛅";
            case 3:
                return "☁️";
            case 45:
            case 48:
                return "🌫️";
            case 51:
            case 53:
            case 55:
                return "🌦️";
            case 56:
            case 57:
            case 61:
            case 63:
            case 65:
            case 80:
            case 81:
            case 82:
                return "🌧️";
            case 66:
            case 67:
            case 71:
            case 73:
            case 75:
            case 77:
            case 85:
            case 86:
                return "🌨️";
            case 95:
            case 96:
            case 99:
                return "⛈️";
            default:
                return "⛅";
        }
    }

    private static String wmoDesc(int code, boolean isDay) {
        switch (code) {
            case 0:
                return "Despejado";
            case 1:
                return "Mayormente despejado";
            case 2:
                return "Parcialmente nublado";
            case 3:
                return "Nublado";
            case 45:
            case 48:
                return "Niebla";
            case 51:
            case 53:
            case 55:
                return "Llovizna";
            case 56:
            case 57:
                return "Llovizna helada";
            case 61:
                return "Lluvia ligera";
            case 63:
                return "Lluvia moderada";
            case 65:
                return "Lluvia fuerte";
            case 66:
            case 67:
                return "Lluvia helada";
            case 71:
            case 73:
            case 75:
                return "Nieve";
            case 77:
                return "Granizo";
            case 80:
            case 81:
            case 82:
                return "Chubascos";
            case 85:
            case 86:
                return "Chubascos de nieve";
            case 95:
            case 96:
            case 99:
                return "Tormenta";
            default:
                return "Nublado";
        }
    }
}
