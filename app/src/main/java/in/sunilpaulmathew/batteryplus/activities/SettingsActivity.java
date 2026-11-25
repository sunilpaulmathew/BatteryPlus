package in.sunilpaulmathew.batteryplus.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.batteryplus.BuildConfig;
import in.sunilpaulmathew.batteryplus.R;
import in.sunilpaulmathew.batteryplus.adapters.SettingsAdapter;
import in.sunilpaulmathew.batteryplus.dialogs.AppThemeDialog;
import in.sunilpaulmathew.batteryplus.dialogs.InputValueDialog;
import in.sunilpaulmathew.batteryplus.dialogs.NotificationContentDialog;
import in.sunilpaulmathew.batteryplus.dialogs.PolicyDialog;
import in.sunilpaulmathew.batteryplus.serializables.SettingsEntry;
import in.sunilpaulmathew.batteryplus.services.BatteryMonitorService;
import in.sunilpaulmathew.batteryplus.utils.Battery;
import in.sunilpaulmathew.batteryplus.utils.Settings;
import in.sunilpaulmathew.batteryplus.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on Sept. 07, 2026
 */
public class SettingsActivity extends BaseActivity {

    private SettingsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings, R.id.layout_main);

        LinearLayoutCompat layoutMain = findViewById(R.id.layout_main);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        ViewGroup.LayoutParams params = layoutMain.getLayoutParams();
        params.height = (Utils.isTablet(this) || Utils.isLandscape(this)) ? ViewGroup.LayoutParams.WRAP_CONTENT : ViewGroup.LayoutParams.MATCH_PARENT;
        layoutMain.setLayoutParams(params);

        adapter = new SettingsAdapter(getData(), (id) -> {
            switch (id) {
                case 9:
                    new PolicyDialog(this);
                    break;
                case 7:
                    Utils.saveBoolean("startOnBoot", !Utils.getBoolean("startOnBoot", false, this), this);
                    notifyChangesAndReturn();
                    adapter.updateData(getData());
                    break;
                case 6:
                    new InputValueDialog(R.drawable.ic_battery_alert, Battery.getLowBatteryThreshold(this), getString(R.string.battery_low_threshold) + " (%)", this) {
                        @Override
                        public void onValueEntered(int value) {
                            Utils.saveInt("battery_low_threshold", value, SettingsActivity.this);
                            adapter.updateData(getData());
                        }
                    };
                    break;
                case 5:
                    new NotificationContentDialog(this);
                    break;
                case 4:
                    if (Utils.hasNotificationGranted(this) && Utils.getBoolean("showNotification", false, this)) {
                        stopBatteryService();
                    } else {
                        if (Utils.hasNotificationGranted(this)) {
                            startBatteryService();
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                requestPermissions(new String[]{
                                        Manifest.permission.POST_NOTIFICATIONS
                                }, 101);
                            }
                        }
                    }
                    break;
                case 3:
                    Utils.saveBoolean("amoledTheme", !Utils.getBoolean("amoledTheme", false, this), this);
                    Settings.restartApp(this);
                    break;
                case 2:
                    new AppThemeDialog(this) {
                        @Override
                        public void onApply() {
                            adapter.updateData(getData());
                        }
                    };
                    break;
                default:
                    Intent settings = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                    settings.setData(uri);
                    startActivity(settings);
                    break;
            }
        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, getSpanCount()) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private int getSpanCount() {
        boolean isLandscape = Utils.isLandscape(this);
        if (Utils.isTablet(this)) {
            return isLandscape ? 3 : 2;
        } else {
            return isLandscape ? 2 : 1;
        }
    }

    private List<SettingsEntry> getData() {
        List<SettingsEntry> mData = new ArrayList<>();
        mData.add(new SettingsEntry(1, R.mipmap.ic_launcher, getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME, getString(R.string.copyright_text)));

        mData.add(new SettingsEntry(getString(R.string.user_interface)));
        mData.add(new SettingsEntry(2, R.drawable.ic_theme, getString(R.string.app_theme), Settings.getAppThemeDescription(this)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && Settings.isDarkTheme(this)) {
            mData.add(new SettingsEntry(3, R.drawable.ic_amoled_theme, getString(R.string.app_theme_amoled), getString(R.string.app_theme_amoled_description), true, Settings.isAmoledBlackEnabled(this)));
        }

        mData.add(new SettingsEntry(getString(R.string.general)));

        if (Utils.hasNotificationGranted(this) && Utils.getBoolean("showNotification", false, this)) {
            mData.add(new SettingsEntry(4, R.drawable.ic_notification, getString(R.string.notification_start), getString(R.string.notification_start_description), true, true));
            mData.add(new SettingsEntry(5, R.drawable.ic_tune, getString(R.string.notification_content), getString(R.string.notification_content_description)));
            mData.add(new SettingsEntry(6, R.drawable.ic_battery_alert, getString(R.string.battery_low_threshold), getString(R.string.battery_low_threshold_description, Battery.getLowBatteryThreshold(this) + "%")));
            mData.add(new SettingsEntry(7, R.drawable.ic_on_boot, getString(R.string.start_on_boot), getString(R.string.start_on_boot_description), true, Utils.getBoolean("startOnBoot", false, this)));
        } else {
            mData.add(new SettingsEntry(4, R.drawable.ic_notification, getString(R.string.notification_start), getString(R.string.notification_start_description), true, false));
        }

        mData.add(new SettingsEntry(getString(R.string.miscellaneous)));
        mData.add(new SettingsEntry(8, R.drawable.ic_github, getString(R.string.source_code), getString(R.string.source_code_description), "https://github.com/sunilpaulmathew/BatteryPlus"));
        mData.add(new SettingsEntry(9, R.drawable.ic_privacy, getString(R.string.privacy_policy), getString(R.string.privacy_policy_description)));
        mData.add(new SettingsEntry(10, R.drawable.ic_email, getString(R.string.developer_contact), getString(R.string.developer_contact_description), "mailto:smartpack.org@gmail.com"));
        return mData;
    }

    private void notifyChangesAndReturn() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("STATUS_CHANGE", true);
        setResult(Activity.RESULT_OK, resultIntent);
    }

    private void startBatteryService() {
        Utils.saveBoolean("showNotification", true, this);
        Intent serviceIntent = new Intent(this, BatteryMonitorService.class);
        startForegroundService(serviceIntent);
        notifyChangesAndReturn();
        adapter.updateData(getData());
    }

    private void stopBatteryService() {
        Utils.saveBoolean("showNotification", false, this);
        Intent serviceIntent = new Intent(this, BatteryMonitorService.class);
        stopService(serviceIntent);
        notifyChangesAndReturn();
        adapter.updateData(getData());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Utils.saveBoolean("showNotification", true, this);
            startBatteryService();
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.permission_denied_title)
                    .setMessage(R.string.permission_denied_notification)
                    .setPositiveButton(R.string.cancel, (dialog, id) -> {
                    }).show();
        }
    }

}