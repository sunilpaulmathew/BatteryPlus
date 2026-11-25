package in.sunilpaulmathew.batteryplus.serializables;

import android.content.Context;
import android.graphics.Color;

import java.io.Serializable;

import in.sunilpaulmathew.batteryplus.R;
import in.sunilpaulmathew.batteryplus.utils.Settings;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 07, 2026
 */
public class SettingsEntry implements Serializable {

    private final boolean isEnabled, isSwitch;
    private final int icon, id;
    private final String description, title, url;

    public SettingsEntry(String title) {
        this.id = 0;
        this.icon = Integer.MIN_VALUE;
        this.title = title;
        this.description = null;
        this.url = null;
        this.isSwitch = false;
        this.isEnabled = false;
    }

    public SettingsEntry(int id, int icon, String title, String description) {
        this.id = id;
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.url = null;
        this.isSwitch = false;
        this.isEnabled = false;
    }

    public SettingsEntry(int id, int icon, String title, String description, String url) {
        this.id = id;
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.url = url;
        this.isSwitch = false;
        this.isEnabled = false;
    }

    public SettingsEntry(int id, int icon, String title, String description, boolean isSwitch, boolean isEnabled) {
        this.id = id;
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.url = null;
        this.isSwitch = isSwitch;
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public boolean isSwitch() {
        return this.isSwitch;
    }

    public int getIcon() {
        return this.icon;
    }

    public int getID() {
        return this.id;
    }

    public int getIconColor(Context context) {
        boolean dark = Settings.isDarkTheme(context);
        String hexColor;
        if (this.icon == R.drawable.ic_notification) {
            hexColor = dark ? "#9FA8DA" : "#3F51B5";
        } else if (this.icon == R.drawable.ic_on_boot) {
            hexColor = dark ? "#81C784" : "#2E7D32";
        } else if (this.icon == R.drawable.ic_github ||
                this.icon == R.drawable.ic_theme) {
            hexColor = dark ? "#58A6FF" : "#0969DA";
        } else if (this.icon == R.drawable.ic_email) {
            hexColor = dark ? "#F48FB1" : "#C2185B";
        } else if (this.icon == R.drawable.ic_tune) {
            hexColor = dark ? "#FFB74D" : "#EF6C00";
        } else if (this.icon == R.drawable.ic_privacy) {
            hexColor = dark ? "#90A4AE" : "#455A64";
        } else if (this.icon == R.drawable.ic_battery_alert) {
            hexColor = dark ? "#F28B82" : "#C5221F";
        } else if (this.icon == R.drawable.ic_charging_high) {
            hexColor = dark ? "#81C995" : "#137333";
        } else if (this.icon == R.drawable.ic_amoled_theme) {
            hexColor = dark ? "#FFB74D" : "#BF360C";
        } else if (this.icon == R.drawable.ic_translate) {
            hexColor = dark ? "#4DB6AC" : "#00796B";
        } else if (this.icon == R.mipmap.ic_launcher) {
            return Integer.MIN_VALUE;
        } else {
            hexColor = dark ? "#FFFFFF" : "#000000";
        }
        return Color.parseColor(hexColor);
    }

    public String geTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getUrl() {
        return this.url;
    }

}