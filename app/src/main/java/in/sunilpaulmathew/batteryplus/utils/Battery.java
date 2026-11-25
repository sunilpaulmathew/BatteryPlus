package in.sunilpaulmathew.batteryplus.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Build;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import in.sunilpaulmathew.batteryplus.R;
import in.sunilpaulmathew.batteryplus.serializables.StatusEntry;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on Sept. 07, 2026
 */
public class Battery {

    public static List<StatusEntry> getData(Intent batteryStatus, Context context) {
        List<StatusEntry> data = new CopyOnWriteArrayList<>();
        if (batteryStatus == null) {
            data.add(new StatusEntry(context.getString(R.string.battery_status_unavailable_message)));
        }
        boolean isPresent = Objects.requireNonNull(batteryStatus).getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);
        if (!isPresent) {
            data.add(new StatusEntry(context.getString(R.string.battery_not_found_message)));
        }

        BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);

        // 1. Level & Percentage
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

        boolean isPowerConnected = (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL);

        if (scale > 0 && level >= 0) {
            int percent = (level * 100) / scale;

            data.add(new StatusEntry(getBatteryRes(status == BatteryManager.BATTERY_STATUS_CHARGING, percent), percent));

            if (isPowerConnected) {
                String remainingTime = Battery.calculateTimeRemaining("charge", percent, context);
                long msRemaining = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    msRemaining = bm.computeChargeTimeRemaining();
                }
                if (msRemaining > 0) {
                    data.add(new StatusEntry(R.drawable.ic_clock, decodePlugged(chargePlug, context), "~" + computeChargeTimeRemaining(msRemaining, context)));
                } else if (!remainingTime.equals(context.getString(R.string.battery_status_unknown))) {
                    data.add(new StatusEntry(R.drawable.ic_clock, decodePlugged(chargePlug, context), "~" + remainingTime));
                }
            } else {
                String remainingTime = Battery.calculateTimeRemaining("discharge", percent, context);
                if (remainingTime != null && !remainingTime.equals(context.getString(R.string.battery_status_unknown))) {
                    data.add(new StatusEntry(R.drawable.ic_clock, decodePlugged(chargePlug, context), "~" + remainingTime));
                }
            }
        }

        // 2. Current, Voltage, & Power
        int currentUa = (bm != null) ? bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) : 0;
        int currentMa = currentUa / 1000;
        int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
        long now = System.currentTimeMillis();
        float voltageV;
        if (voltage > 0) {
            voltageV = voltage / 1000f;
            Tracker.appendHistoryPoint("voltage", now, voltageV, context);
            data.add(new StatusEntry(R.drawable.ic_voltage, context.getString(R.string.voltage), String.format(Locale.getDefault(), "%.2f V", voltageV)));

            if (currentUa != Integer.MIN_VALUE) {
                Tracker.appendHistoryPoint("current", now, currentMa, context);
                data.add(new StatusEntry(R.drawable.ic_current, context.getString(R.string.current), currentMa + " mA"));

                if (voltageV > 0) {
                    float powerWatts = voltageV * (currentMa / 1000f);
                    double watts = (voltage / 1000.0) * (currentUa / 1_000_000.0);
                    Tracker.appendHistoryPoint("power", now, powerWatts, context);
                    data.add(new StatusEntry(R.drawable.ic_power, context.getString(R.string.power), String.format(Locale.getDefault(), "%.1f W", watts)));
                }
            }
        }

        // 3. Capacity
        long totalCapacityMah = getCapacityMah(bm, level, scale, Objects.requireNonNull(bm).getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER));
        if (totalCapacityMah > 0) {
            String capacityString = totalCapacityMah + " mAh";
            if (!capacityString.trim().equalsIgnoreCase("0.0 mAh") && !capacityString.trim().equalsIgnoreCase("mAh")) {
                Tracker.appendHistoryPoint("capacity", now, totalCapacityMah, context);
                data.add(new StatusEntry(R.drawable.ic_battery_full, context.getString(R.string.capacity), capacityString));
            }
        }

        // 4. Temperature
        int temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        if (temperature != -1) {
            float tempC = temperature / 10.0f;
            Tracker.appendHistoryPoint("temperature", now, tempC, context);
            data.add(new StatusEntry(R.drawable.ic_temperature, context.getString(R.string.temperature), String.format(Locale.getDefault(), "%.1f°C", tempC)));
        }

        // 5. Technology & Health
        String tech = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
        int health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        if (tech == null || tech.trim().isEmpty()) {
            tech = context.getString(R.string.battery_status_unknown);
        }
        data.add(new StatusEntry(R.drawable.ic_technology, context.getString(R.string.technology), tech));
        data.add(new StatusEntry(R.drawable.ic_heart, context.getString(R.string.health), decodeHealth(health, context)));

        return data;
    }

    public static Bitmap createBatteryIconBitmap(int percentage, Context context) {
        int size = 96;
        int color = getProgressColor(percentage, context);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
        RectF rect = new RectF(10, 10, size - 10, size - 10);
        canvas.drawArc(rect, 0, 360, false, paint);

        paint.setColor(color);
        float sweepAngle = (percentage / 100f) * 360f;
        canvas.drawArc(rect, -90, sweepAngle, false, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(25f);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(color);

        Paint.FontMetrics fm = paint.getFontMetrics();
        float textY = (canvas.getHeight() - (fm.descent + fm.ascent)) / 2f;

        canvas.drawText(percentage + "%", canvas.getWidth() / 2f, textY, paint);
        return bitmap;
    }

    public static double getDesignCapacity(Context context) {
        try {
            @SuppressLint("PrivateApi")
            Class<?> powerProfileClass = Class.forName("com.android.internal.os.PowerProfile");
            Object powerProfile = powerProfileClass.getConstructor(Context.class).newInstance(context);
            Method getBatteryCapacity = powerProfileClass.getMethod("getAveragePower", String.class);
            Double capacityDouble = (Double) getBatteryCapacity.invoke(powerProfile, "battery.capacity");
            return capacityDouble != null && capacityDouble > 0 ? capacityDouble : -1;
        } catch (Exception e) {
            return  -1;
        }
    }

    public static int getBatteryRes(boolean charging, int per) {
        if (charging) {
            if (per >= 90) {
                return R.drawable.ic_charging_high;
            } else if (per >= 60) {
                return R.drawable.ic_charging_medium;
            } else {
                return R.drawable.ic_charging_low;
            }
        } else {
            if (per == 100) {
                return R.drawable.ic_battery_full;
            } else if (per >= 90) {
                return R.drawable.ic_battery_high;
            } else if (per >= 60) {
                return R.drawable.ic_battery_medium;
            } else {
                return R.drawable.ic_battery_low;
            }
        }
    }

    public static int getDesignCapacityAsInt(Context context) {
        return Utils.getInt("designCapacity", (int) Math.round(Battery.getDesignCapacity(context)), context);
    }

    public static int getLowBatteryThreshold(Context context) {
        return Utils.getInt("battery_low_threshold", 15, context);
    }

    public static long getCapacityMah(BatteryManager batteryManager, int level, int scale, long chargeCounter) {
        try {
            if (batteryManager != null && chargeCounter > 0 && level > 0 && scale > 0) {
                float batteryPct = level / (float) scale;
                long remainingMah = chargeCounter / 1000L;
                return (long) (remainingMah / batteryPct);
            }
        } catch (Exception ignored) {}
        return -1;
    }

    public static int getProgressColor(int percentValue, Context context) {
        boolean dark = Settings.isDarkTheme(context);
        String hexColor;

        if (percentValue <= 10) {
            // Critical: High-visibility Red
            hexColor = dark ? "#EF5350" : "#D32F2F";
        } else if (percentValue <= 20) {
            // Warning: Bright Orange / Amber
            hexColor = dark ? "#FFA726" : "#E65100";
        } else {
            // Normal: Safe Green
            hexColor = dark ? "#81C784" : "#2E7D32";
        }

        return Color.parseColor(hexColor);
    }

    public static String calculateTimeRemaining(String prefPrefix, int currentPercent, Context context) {
        boolean isCharging = prefPrefix.equals("charge");

        int remainingPercent = isCharging ? (100 - currentPercent) : currentPercent;

        if (isCharging && currentPercent >= 100) return context.getString(R.string.battery_status_unknown);
        if (!isCharging && currentPercent <= 0) return context.getString(R.string.battery_status_unknown);

        long now = System.currentTimeMillis();
        String prefLevel = prefPrefix + "_last_level";
        String prefTime = prefPrefix + "_last_timestamp";
        String prefMsPerPct = prefPrefix + "_ms_per_pct";

        int lastLevel = (int) Utils.getLong(prefLevel, -1, context);
        long lastTime = Utils.getLong(prefTime, -1, context);
        long rollingMsPerPct = Utils.getLong(prefMsPerPct, -1, context);

        if (lastLevel == -1 || lastTime == -1) {
            Utils.saveLong(prefLevel, currentPercent, context);
            Utils.saveLong(prefTime, now, context);

            Tracker.appendHistoryPoint("battery_level", now, currentPercent, context);

            return (rollingMsPerPct > 0) ? Battery.computeChargeTimeRemaining(remainingPercent * rollingMsPerPct, context) : context.getString(R.string.status_analyzing);
        }

        int delta = isCharging ? (currentPercent - lastLevel) : (lastLevel - currentPercent);

        if (delta >= 1) {
            long elapsedMs = now - lastTime;
            long newMsPerPct = elapsedMs / delta;

            if (newMsPerPct >= 10_000L && newMsPerPct <= 2_700_000L) {
                if (rollingMsPerPct <= 0) {
                    rollingMsPerPct = newMsPerPct;
                } else {
                    rollingMsPerPct = (long) ((rollingMsPerPct * 0.70) + (newMsPerPct * 0.30));
                }
                Utils.saveLong(prefMsPerPct, rollingMsPerPct, context);
            }

            Utils.saveLong(prefLevel, currentPercent, context);
            Utils.saveLong(prefTime, now, context);

            Tracker.appendHistoryPoint("battery_level", now, currentPercent, context);
        } else if (delta < 0) {
            Utils.saveLong(prefLevel, currentPercent, context);
            Utils.saveLong(prefTime, now, context);
        }

        if (rollingMsPerPct > 0) {
            long totalRemainingMs = remainingPercent * rollingMsPerPct;
            return Battery.computeChargeTimeRemaining(totalRemainingMs, context);
        }

        return context.getString(R.string.battery_status_unknown);
    }

    public static String computeChargeTimeRemaining(long millis, Context context) {
        if (millis <= 0) {
            return context.getString(R.string.battery_status_unknown);
        }

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;

        if (hours > 0) {
            String hrUnit = (hours == 1) ? "hr" : "hrs";
            if (minutes > 0) {
                String minUnit = (minutes == 1) ? "min" : "mins";
                return String.format(Locale.getDefault(), "%d %s %d %s", hours, hrUnit, minutes, minUnit);
            } else {
                return String.format(Locale.getDefault(), "%d %s", hours, hrUnit);
            }
        }

        if (minutes > 0) {
            String minUnit = (minutes == 1) ? "min" : "mins";
            return String.format(Locale.getDefault(), "%d %s", minutes, minUnit);
        }

        return "a min";
    }

    public static String decodeHealth(int health, Context context) {
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD:
                return context.getString(R.string.battery_status_good);
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                return context.getString(R.string.battery_status_overheat);
            case BatteryManager.BATTERY_HEALTH_DEAD:
                return context.getString(R.string.battery_status_dead);
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                return context.getString(R.string.battery_status_overvoltage);
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                return context.getString(R.string.battery_status_unspecified);
            case BatteryManager.BATTERY_HEALTH_COLD:
                return context.getString(R.string.battery_status_cold);
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
            default:
                return context.getString(R.string.battery_status_unknown);
        }
    }

    public static String decodePlugged(int plugged, Context context) {
        switch (plugged) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                return context.getString(R.string.charging_status_ac);
            case BatteryManager.BATTERY_PLUGGED_USB:
                return context.getString(R.string.charging_status_usb);
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                return context.getString(R.string.charging_status_wireless);
            case 0:
                return context.getString(R.string.discharging);
            default:
                return context.getString(R.string.battery_status_unknown);
        }
    }

}