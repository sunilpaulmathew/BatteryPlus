package in.sunilpaulmathew.batteryplus.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import in.sunilpaulmathew.batteryplus.R;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on Sept. 23, 2026
 */
public abstract class InputValueDialog extends MaterialAlertDialogBuilder {

    public InputValueDialog(int iconRes, int valueCurrent, String title, Context context) {
        super(context);

        LinearLayoutCompat layout = new LinearLayoutCompat(context);
        layout.setPadding(75, 75, 75, 75);
        final AppCompatEditText editText = new AppCompatEditText(context);
        editText.setGravity(Gravity.CENTER);
        editText.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (valueCurrent > 0) {
            editText.setText(String.valueOf(valueCurrent));
        }
        layout.addView(editText);

        if (title != null) {
            setTitle(title);
            if (iconRes != -1) {
                setIcon(iconRes);
            } else {
                setIcon(R.mipmap.ic_launcher);
            }
        }
        setView(layout);
        setOnDismissListener(DialogInterface::dismiss);
        setNeutralButton(R.string.cancel, (dialog, id) -> {
        });
        setPositiveButton(R.string.apply, (dialog, id) -> {
            if (editText.getText() != null && !editText.getText().toString().trim().isEmpty()) {
                int newValue = Integer.parseInt(editText.getText().toString().trim());
                if (newValue > 0 && newValue != valueCurrent) {
                    onValueEntered(newValue);
                }
            }
        });

        show();
    }

    public abstract void onValueEntered(int value);

}