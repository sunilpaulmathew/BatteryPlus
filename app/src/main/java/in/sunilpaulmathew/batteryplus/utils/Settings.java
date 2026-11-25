package in.sunilpaulmathew.batteryplus.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.color.DynamicColors;

import in.sunilpaulmathew.batteryplus.MainActivity;
import in.sunilpaulmathew.batteryplus.R;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on Nov. 25, 2026
 */
public class Settings {

    public static boolean isAmoledBlackEnabled(Context context) {
        return isDarkTheme(context) && Utils.getBoolean("amoledTheme", false, context);
    }

    public static boolean isDarkTheme(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static int getAppTheme(Context context) {
        return Utils.getInt("appTheme", 0, context);
    }

    public static String getAppThemeDescription(Context context) {
        int appTheme = Utils.getInt("appTheme", 0, context);
        switch (appTheme) {
            case 2:
                return context.getString(R.string.app_theme_light);
            case 1:
                return context.getString(R.string.app_theme_dark);
            default:
                return context.getString(R.string.app_theme_auto);
        }
    }

    public static void initializeAppTheme(Activity activity) {
        DynamicColors.applyToActivityIfAvailable(activity);
        int appTheme = Utils.getInt("appTheme", 0, activity);
        switch (appTheme) {
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
        activity.getTheme().applyStyle(Settings.isAmoledBlackEnabled(activity) ? R.style.AppTheme_Amoled : R.style.AppTheme, true);
    }

    public static void restartApp(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finishAffinity();
    }

}