package in.sunilpaulmathew.batteryplus.dialogs;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import in.sunilpaulmathew.batteryplus.R;
import in.sunilpaulmathew.batteryplus.utils.Settings;
import in.sunilpaulmathew.batteryplus.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on Nov. 25, 2026
 */
public abstract class AppThemeDialog extends MaterialAlertDialogBuilder {

    public AppThemeDialog(Context context) {
        super(context);

        int currentTheme = Settings.getAppTheme(context);

        setIcon(R.drawable.ic_theme);
        setTitle(R.string.app_theme);
        setSingleChoiceItems(new CharSequence[] {
                context.getString(R.string.app_theme_auto),
                context.getString(R.string.app_theme_dark),
                context.getString(R.string.app_theme_light)
        }, currentTheme, null);
        setNeutralButton(R.string.cancel, null);
        setOnDismissListener(DialogInterface::dismiss);
        setPositiveButton(R.string.apply, (dialog, id) -> {
            int selectedIndex = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
            if (selectedIndex == currentTheme) return;

            Utils.saveInt("appTheme", selectedIndex, context);
            AppCompatDelegate.setDefaultNightMode(getModes()[selectedIndex]);

            onApply();
        });
        show();
    }

    private static int[] getModes() {
        return new int[] {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                AppCompatDelegate.MODE_NIGHT_YES,
                AppCompatDelegate.MODE_NIGHT_NO
        };
    }

    public abstract void onApply();

}