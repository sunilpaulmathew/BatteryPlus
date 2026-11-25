package in.sunilpaulmathew.batteryplus.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on Sept. 07, 2026
 */
public class Utils {

    public static boolean getBoolean(String name, boolean defaults, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(name, defaults);
    }

    public static boolean hasNotificationGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public static boolean isLandscape(Context context) {
        Configuration config = context.getResources().getConfiguration();
        return config.orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static int getInt(String name, int defaults, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(name, defaults);
    }

    public static long getLong(String name, long defaults, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(name, defaults);
    }

    public static String getString(String name, String defaults, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(name, defaults);
    }

    public static Toast toast(String message, Context context) {
        return Toast.makeText(context, message, Toast.LENGTH_SHORT);
    }

    public static void saveBoolean(String name, boolean value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(name, value).apply();
    }

    public static void saveInt(String name, int value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(name, value).apply();
    }

    public static void saveLong(String name, long value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(name, value)
                .apply();
    }

    public static void saveString(String name, String value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(name, value).apply();
    }

    public static void setSlideInAnimation(final View viewToAnimate, int position) {
        // Only animate items appearing for the first time
        if (position > -1) {
            viewToAnimate.setTranslationY(50f);
            viewToAnimate.setAlpha(0f);

            viewToAnimate.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(150)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        } else {
            // Reset properties to ensure recycled views are displayed correctly
            viewToAnimate.setTranslationY(0f);
            viewToAnimate.setAlpha(1f);
        }
    }

}