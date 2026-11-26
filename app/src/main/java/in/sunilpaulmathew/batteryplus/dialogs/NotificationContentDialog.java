package in.sunilpaulmathew.batteryplus.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import in.sunilpaulmathew.batteryplus.R;
import in.sunilpaulmathew.batteryplus.services.BatteryMonitorService;
import in.sunilpaulmathew.batteryplus.utils.Settings;
import in.sunilpaulmathew.batteryplus.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on Sept. 07, 2026
 */
public class NotificationContentDialog extends MaterialAlertDialogBuilder {

    public NotificationContentDialog(Context context) {
        super(context);

        String[] keys = getKeys();

        boolean[] checkedItems = new boolean[keys.length];
        for (int i = 0; i < keys.length; i++) {
            checkedItems[i] = Utils.getBoolean(keys[i], true, context);
        }

        setIcon(getColoredIcon(context));
        setTitle(R.string.notification_content);
        setMultiChoiceItems(getNotificationOptions(context), checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked);
        setOnDismissListener(DialogInterface::dismiss);
        setNeutralButton(android.R.string.cancel, null);
        setPositiveButton(R.string.apply, (dialog, which) -> {
            for (int i = 0; i < keys.length; i++) {
                Utils.saveBoolean(keys[i], checkedItems[i], context);
            }
            Intent updateIntent = new Intent(context, BatteryMonitorService.class);
            updateIntent.setAction(BatteryMonitorService.ACTION_UPDATE_PREFERENCES);
            context.startService(updateIntent);
        });
        show();
    }

    private static Drawable getColoredIcon(Context context) {
        Drawable drawable = AppCompatResources.getDrawable(context, R.drawable.ic_tune);
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable).mutate();
            DrawableCompat.setTint(drawable, Color.parseColor(Settings.isDarkTheme(context) ? "#FFB74D" : "#EF6C00"));
        }
        return drawable;
    }

    private static String[] getKeys() {
        return new String[] {
                "pref_show_remaining_time",
                "pref_show_voltage",
                "pref_show_current",
                "pref_show_power",
                "pref_show_capacity",
                "pref_show_temperature",
                "pref_show_technology",
                "pref_show_health"
        };
    }

    private static String[] getNotificationOptions(Context context) {
        return new String[] {
                context.getString(R.string.remaining_time),
                context.getString(R.string.voltage),
                context.getString(R.string.current),
                context.getString(R.string.power),
                context.getString(R.string.capacity),
                context.getString(R.string.temperature),
                context.getString(R.string.technology),
                context.getString(R.string.health)
        };
    }

}