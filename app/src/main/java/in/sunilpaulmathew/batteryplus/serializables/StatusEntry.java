package in.sunilpaulmathew.batteryplus.serializables;

import android.content.Context;
import android.graphics.Color;

import java.io.Serializable;
import java.util.Objects;

import in.sunilpaulmathew.batteryplus.R;
import in.sunilpaulmathew.batteryplus.utils.Settings;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on Sept. 07, 2026
 */
public class StatusEntry implements Serializable {

    private final int iconRes, value;
    private final String des, title;

    public StatusEntry(int iconRes, String title, String des) {
        this.iconRes = iconRes;
        this.title = title;
        this.value = -1;
        this.des = des;
    }

    public StatusEntry(int iconRes, int value) {
        this.iconRes = iconRes;
        this.value = value;
        this.title = null;
        this.des = null;
    }

    public StatusEntry(String title, String des) {
        this.iconRes = -1;
        this.value = -1;
        this.title = title;
        this.des = des;
    }

    public StatusEntry(String des) {
        this.iconRes = -1;
        this.value = -1;
        this.title = null;
        this.des = des;
    }

    public int getIcon() {
        return this.iconRes;
    }

    public int getIconTintAttribute(Context context) {
        return getIconColor(context);
    }

    public int getValue() {
        return this.value;
    }

    public String getDescription() {
        return this.des;
    }

    public String getTitle() {
        return this.title;
    }

    public int getIconColor(Context context) {
        boolean dark = Settings.isDarkTheme(context);
        String hexColor;
        if (this.iconRes == R.drawable.ic_clock) {
            hexColor = dark ? "#82B1FF" : "#2962FF";
        } else if (this.iconRes == R.drawable.ic_current) {
            hexColor = dark ? "#4DB6AC" : "#00897B";
        } else if (this.iconRes == R.drawable.ic_voltage) {
            hexColor = dark ? "#FFB74D" : "#E65100";
        } else if (this.iconRes == R.drawable.ic_power) {
            hexColor = dark ? "#FFEE58" : "#F57F17";
        } else if (this.iconRes == R.drawable.ic_temperature) {
            hexColor = dark ? "#FF8A65" : "#D84315";
        } else if (this.iconRes == R.drawable.ic_technology) {
            hexColor = dark ? "#B0BEC5" : "#455A64";
        } else if (this.iconRes == R.drawable.ic_heart) {
            hexColor = dark ? "#CE93D8" : "#7B1FA2";
        } else {
            hexColor = dark ? "#FFFFFF" : "#000000";
        }
        return Color.parseColor(hexColor);
    }

    public int getExtraDes(Context context) {
        if (this.iconRes == R.drawable.ic_clock) {
            return Objects.requireNonNull(this.title).equalsIgnoreCase(context.getString(R.string.discharging)) ? R.string.discharging_summary : R.string.charging_summary;
        } else if (this.iconRes == R.drawable.ic_current) {
            return R.string.current_summary;
        } else if (this.iconRes == R.drawable.ic_voltage) {
            return R.string.voltage_summary;
        } else if (this.iconRes == R.drawable.ic_power) {
            return R.string.power_summary;
        } else if (this.iconRes == R.drawable.ic_temperature) {
            return R.string.temperature_summary;
        } else if (this.iconRes == R.drawable.ic_technology) {
            return R.string.technology_summary;
        } else if (this.iconRes == R.drawable.ic_heart) {
            return R.string.health_summary;
        } else {
            return R.string.capacity_summary;
        }
    }

}