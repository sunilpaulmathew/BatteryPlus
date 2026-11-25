package in.sunilpaulmathew.batteryplus.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.service.notification.StatusBarNotification;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Locale;

import in.sunilpaulmathew.batteryplus.MainActivity;
import in.sunilpaulmathew.batteryplus.R;
import in.sunilpaulmathew.batteryplus.serializables.StatusEntry;
import in.sunilpaulmathew.batteryplus.utils.Battery;
import in.sunilpaulmathew.batteryplus.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on Sept. 07, 2026
 */
public class BatteryMonitorService extends Service {

    private final Handler alertHandler = new Handler(Looper.getMainLooper());
    private NotificationManager notificationManager;
    private Runnable repeatAlertRunnable;
    public static final String ACTION_UPDATE_PREFERENCES = "com.example.batteryplus.ACTION_UPDATE_PREFERENCES";
    public static final String ACTION_STOP_SERVICE = "com.example.batteryplus.ACTION_STOP_SERVICE";
    private static final String CHANNEL_ID_SILENT = "battery_status_silent_channel";
    private static final String CHANNEL_ID_ALERT = "battery_status_alert_channel";
    private static final String CHANNEL_ID_CRITICAL_ALERTS = "battery_alerts_channel";
    private static final int NOTIFICATION_ID_LOW = 2001;
    private static final int NOTIFICATION_ID_FULL = 2002;
    private static final int NOTIFICATION_ID = 1001;

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                updateNotification(intent);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannels();

        Notification initialNotification = buildNotification(
                R.drawable.ic_launcher_foreground,
                getString(R.string.app_name),
                getString(R.string.initializing_Status),
                false
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                    NOTIFICATION_ID,
                    initialNotification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            );
        } else {
            startForeground(NOTIFICATION_ID, initialNotification);
        }

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(batteryReceiver, filter);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
                Utils.saveBoolean("show_notification", false, this);
                stopSelf();
                return START_NOT_STICKY;
            } else if (ACTION_UPDATE_PREFERENCES.equals(intent.getAction())) {
                IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent currentBatteryIntent = registerReceiver(null, filter);
                if (currentBatteryIntent != null) {
                    updateNotification(currentBatteryIntent);
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopRepeatingAlert();
        super.onDestroy();
        try {
            unregisterReceiver(batteryReceiver);
        } catch (IllegalArgumentException ignored) {
        }
        stopForeground(STOP_FOREGROUND_REMOVE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updateNotification(Intent batteryIntent) {
        if (batteryIntent == null) return;

        boolean isPresent = batteryIntent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);
        if (!isPresent) {
            Notification emptyNotification = buildNotification(
                    R.drawable.ic_battery_alert,
                    getString(R.string.app_name),
                    getString(R.string.battery_not_found_message),
                    false
            );
            notificationManager.notify(NOTIFICATION_ID, emptyNotification);
            return;
        }

        int iconRes = R.drawable.ic_launcher_foreground;
        int percent = -1;
        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        String statusCharging = getString(R.string.status_analyzing);
        boolean isPowerConnected = (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL);

        StringBuilder sb = new StringBuilder();

        boolean isFullAlertSent = Utils.getBoolean("isFullAlertSent", false, this);
        boolean isLowAlertSent = Utils.getBoolean("isLowAlertSent", false, this);

        for (StatusEntry statusEntry : Battery.getData(batteryIntent, this)) {
            if (statusEntry.getValue() != -1) {
                percent = statusEntry.getValue();
                iconRes = statusEntry.getIcon();

                isFullAlertSent = handleFullBatteryAlert(percent, isPowerConnected, isFullAlertSent);
                isLowAlertSent = handleLowBatteryAlert(percent, isPowerConnected, isLowAlertSent);
            } else {
                appendStatusDescription(sb, statusEntry);
                if (statusEntry.getIcon() == R.drawable.ic_clock) {
                    statusCharging = statusEntry.getTitle();
                }
            }
        }

        boolean alertUser = Utils.getBoolean("isFirstUpdate", true, this);
        if (alertUser) {
            Utils.saveBoolean("isFirstUpdate", false, this);
        }

        Notification notification = buildNotification(iconRes, String.format(Locale.getDefault(), "%s: %d%% • %s",
                getString(R.string.battery_level), percent, statusCharging), sb.toString().trim(), alertUser);

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void appendStatusDescription(StringBuilder sb, StatusEntry statusEntry) {
        int icon = statusEntry.getIcon();
        String description = statusEntry.getDescription();

        if (icon == R.drawable.ic_clock && Utils.getBoolean("pref_show_remaining_time", true, this)) {
            String timeRemaining = description.trim().replace("~", "");
            if (!timeRemaining.equalsIgnoreCase(getString(R.string.battery_status_unknown))) {
                String statusCharging = statusEntry.getTitle();
                if (statusCharging != null && statusCharging.equalsIgnoreCase(getString(R.string.discharging))) {
                    sb.append(getString(R.string.time_to_empty_summary, timeRemaining)).append("\n");
                } else {
                    sb.append(getString(R.string.time_to_full_charge_summary, timeRemaining)).append("\n");
                }
            }
        } else if (icon == R.drawable.ic_voltage && Utils.getBoolean("pref_show_voltage", true, this)) {
            sb.append(getString(R.string.voltage)).append(": ").append(description).append("\n");
        } else if (icon == R.drawable.ic_current && Utils.getBoolean("pref_show_current", true, this)) {
            sb.append(getString(R.string.current)).append(": ").append(description).append("\n");
        } else if (icon == R.drawable.ic_power && Utils.getBoolean("pref_show_power", true, this)) {
            sb.append(getString(R.string.power)).append(": ").append(description).append("\n");
        } else if (icon == R.drawable.ic_battery_full && Utils.getBoolean("pref_show_capacity", true, this)) {
            sb.append(getString(R.string.capacity)).append(": ").append(description).append("\n");
        } else if (icon == R.drawable.ic_temperature && Utils.getBoolean("pref_show_temperature", true, this)) {
            sb.append(getString(R.string.temperature)).append(": ").append(description).append("\n");
        } else if (icon == R.drawable.ic_technology && Utils.getBoolean("pref_show_technology", true, this)) {
            sb.append(getString(R.string.technology)).append(": ").append(description).append("\n");
        } else if (icon == R.drawable.ic_heart && Utils.getBoolean("pref_show_health", true, this)) {
            sb.append(getString(R.string.health)).append(": ").append(description).append("\n");
        }
    }

    private void createNotificationChannels() {
        NotificationChannel silentChannel = new NotificationChannel(
                CHANNEL_ID_SILENT,
                "Ongoing Battery Status",
                NotificationManager.IMPORTANCE_LOW
        );
        silentChannel.setDescription("Continuous silent updates of battery metrics");
        silentChannel.setShowBadge(false);

        NotificationChannel alertChannel = new NotificationChannel(
                CHANNEL_ID_ALERT,
                "Service Activated Alert",
                NotificationManager.IMPORTANCE_HIGH
        );
        alertChannel.setDescription("Heads-up alert when monitoring is first enabled");
        alertChannel.setShowBadge(false);

        Uri soundUri = Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/" + R.raw.notification
        );

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build();

        NotificationChannel alertsChannel = new NotificationChannel(
                CHANNEL_ID_CRITICAL_ALERTS,
                "Battery Warnings & Alerts",
                NotificationManager.IMPORTANCE_HIGH
        );
        alertsChannel.setDescription("Alerts for critical low battery and 100% full charge");
        alertsChannel.setSound(soundUri, audioAttributes);
        alertsChannel.enableVibration(true);

        notificationManager.createNotificationChannel(silentChannel);
        notificationManager.createNotificationChannel(alertChannel);
        notificationManager.createNotificationChannel(alertsChannel);
    }

    private void postAlertNotification(int id, int iconRes, String title, String text) {
        Intent openAppIntent = new Intent(this, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                id,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_CRITICAL_ALERTS)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(iconRes)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setOngoing(false)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(id, notification);
    }

    private Notification buildNotification(int drawable, String title, String expandedBody, boolean alertUser) {
        Intent openAppIntent = new Intent(this, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent stopIntent = new Intent(this, BatteryMonitorService.class);
        stopIntent.setAction(ACTION_STOP_SERVICE);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this,
                1,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String channelId = alertUser ? CHANNEL_ID_ALERT : CHANNEL_ID_SILENT;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(getString(R.string.tap_to_full_metrics))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(expandedBody))
                .setSmallIcon(drawable)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.stop), stopPendingIntent);

        if (alertUser) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setOnlyAlertOnce(false);
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOnlyAlertOnce(true);
        }

        return builder.build();
    }

    private boolean handleFullBatteryAlert(int percent, boolean isPowerConnected, boolean isFullAlertSent) {
        if (percent >= Battery.getHighBatteryThreshold(this) && isPowerConnected) {
            if (!isFullAlertSent) {
                startRepeatingFullAlert();
                Utils.saveBoolean("isFullAlertSent", true, this);
                return true;
            }
        } else if (isFullAlertSent) {
            stopRepeatingAlert();
            Utils.saveBoolean("isFullAlertSent", false, this);
            notificationManager.cancel(NOTIFICATION_ID_FULL);
            return false;
        }
        return isFullAlertSent;
    }

    private boolean handleLowBatteryAlert(int percent, boolean isPowerConnected, boolean isLowAlertSent) {
        boolean isLow = percent <= Battery.getLowBatteryThreshold(this);
        if (isLow && !isPowerConnected) {
            if (!isLowAlertSent) {
                startRepeatingLowAlert(percent);
                Utils.saveBoolean("isLowAlertSent", true, this);
                return true;
            }
        } else if (isLowAlertSent) {
            stopRepeatingAlert();
            Utils.saveBoolean("isLowAlertSent", false, this);
            notificationManager.cancel(NOTIFICATION_ID_LOW);
            return false;
        }
        return isLowAlertSent;
    }

    private boolean isNotificationVisible(int notificationId) {
        if (notificationManager == null) return false;
        for (StatusBarNotification sbn : notificationManager.getActiveNotifications()) {
            if (sbn.getId() == notificationId) {
                return true;
            }
        }
        return false;
    }

    private void stopRepeatingAlert() {
        if (repeatAlertRunnable != null) {
            alertHandler.removeCallbacks(repeatAlertRunnable);
            repeatAlertRunnable = null;
        }
    }

    private void startRepeatingFullAlert() {
        stopRepeatingAlert();

        repeatAlertRunnable = new Runnable() {
            int count = 0;

            @Override
            public void run() {
                if (count > 0 && !isNotificationVisible(NOTIFICATION_ID_FULL)) {
                    return;
                }

                postAlertNotification(
                        NOTIFICATION_ID_FULL,
                        R.drawable.ic_battery_full,
                        getString(R.string.battery_full_title),
                        getString(R.string.battery_full_summary)
                );

                count++;
                if (count < 3) {
                    alertHandler.postDelayed(this, 15000L);
                }
            }
        };

        alertHandler.post(repeatAlertRunnable);
    }

    private void startRepeatingLowAlert(int percent) {
        stopRepeatingAlert();

        repeatAlertRunnable = new Runnable() {
            int count = 0;

            @Override
            public void run() {
                if (count > 0 && !isNotificationVisible(NOTIFICATION_ID_LOW)) {
                    return;
                }

                postAlertNotification(
                        NOTIFICATION_ID_LOW,
                        R.drawable.ic_battery_alert,
                        getString(R.string.battery_low_title, percent + "%"),
                        getString(R.string.battery_low_summary)
                );

                count++;
                if (count < 3) {
                    alertHandler.postDelayed(this, 15000L);
                }
            }
        };

        alertHandler.post(repeatAlertRunnable);
    }

}