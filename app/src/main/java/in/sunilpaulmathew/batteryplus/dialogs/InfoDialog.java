package in.sunilpaulmathew.batteryplus.dialogs;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.batteryplus.R;
import in.sunilpaulmathew.batteryplus.serializables.StatusEntry;
import in.sunilpaulmathew.batteryplus.serializables.DataEntry;
import in.sunilpaulmathew.batteryplus.utils.Battery;
import in.sunilpaulmathew.batteryplus.utils.Utils;
import in.sunilpaulmathew.batteryplus.utils.XYPlot;
import in.sunilpaulmathew.batteryplus.utils.Tracker;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on Sept. 07, 2026
 */
public class InfoDialog extends BottomSheetDialog {

    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final MaterialButton reset;
    private final MaterialTextView status;
    private final Runnable updateRunnable;
    private final StatusEntry statusEntry;
    private final XYPlot plotView;
    private String prefix = null;

    public InfoDialog(StatusEntry statusEntry, Context context) {
        super(context);
        this.context = context;
        this.statusEntry = statusEntry;

        View rootView = View.inflate(context, R.layout.layout_dialog_info, null);

        AppCompatImageButton icon = rootView.findViewById(R.id.icon);
        plotView = rootView.findViewById(R.id.discharge_plot);
        MaterialButton cancel = rootView.findViewById(R.id.cancel);
        reset = rootView.findViewById(R.id.reset);
        status = rootView.findViewById(R.id.status);
        MaterialTextView title = rootView.findViewById(R.id.title);
        MaterialTextView text = rootView.findViewById(R.id.text);

        if (statusEntry.getIcon() == R.drawable.ic_clock) {
            prefix = "battery_level";
            plotView.setUnit("%");
        } else if (statusEntry.getIcon() == R.drawable.ic_temperature) {
            prefix = "temperature";
            plotView.setUnit("°C");
        } else if (statusEntry.getIcon() == R.drawable.ic_power) {
            prefix = "power";
            plotView.setUnit("W");
        } else if (statusEntry.getIcon() == R.drawable.ic_voltage) {
            prefix = "voltage";
            plotView.setUnit("V");
        } else if (statusEntry.getIcon() == R.drawable.ic_current) {
            prefix = "current";
            plotView.setUnit("mA");
        } else if (statusEntry.getIcon() == R.drawable.ic_battery_full) {
            prefix = "capacity";
            plotView.setUnit("mAh");
        }

        refreshData();

        icon.setImageResource(statusEntry.getIcon());
        icon.setColorFilter(statusEntry.getIconColor(context));

        String extraDesc = context.getString(statusEntry.getExtraDes(context));
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, getTextSize(extraDesc.length()));
        text.setText(extraDesc);

        title.setText(statusEntry.getTitle());
        title.setTextColor(statusEntry.getIconColor(context));

        rootView.post(() -> {
            FrameLayout bottomSheet = findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setFitToContents(true);
            }
        });

        setOnDismissListener(DialogInterface::dismiss);
        setContentView(rootView);

        reset.setOnClickListener(v -> {
            Tracker.clearHistory(prefix, context);
            dismiss();
        });

        if (status.getText().toString().trim().equalsIgnoreCase(context.getString(R.string.capacity_design_input_message))
                || statusEntry.getIcon() == R.drawable.ic_battery_full && Battery.getDesignCapacityAsInt(context) != (int) Math.round(Battery.getDesignCapacity(context))) {
            status.setOnClickListener(v -> new InputValueDialog(R.drawable.ic_battery_full, Battery.getDesignCapacityAsInt(context), context.getString(R.string.capacity_design_input_title), context) {
                @Override
                public void onValueEntered(int value) {
                    if (value > 0) {
                        Utils.saveInt("designCapacity", value, context);
                        refreshData();
                    }
                }
            });
        }

        cancel.setOnClickListener(v -> dismiss());

        updateRunnable = new Runnable() {
            @Override
            public void run() {
                refreshData();
                handler.postDelayed(this, 5000);
            }
        };

        show();
    }

    private int getTextSize(int length) {
        if (length >= 100) {
            return 15;
        } else if (length >= 50) {
            return 17;
        } else {
            return 20;
        }
    }

    private void refreshData() {
        if (prefix == null) return;

        List<DataEntry> history = Tracker.getHistory(prefix, context);
        if (!history.isEmpty()) {
            plotView.setData(history);
            plotView.setVisibility(VISIBLE);

            if (prefix != null) {
                reset.setVisibility(VISIBLE);
            } else {
                reset.setVisibility(GONE);
            }

            if (statusEntry.getIcon() == R.drawable.ic_clock) {
                String statusValue = statusEntry.getDescription().trim().replace("~", "");
                if (!statusValue.isEmpty()) {
                    status.setText(statusEntry.getTitle().equalsIgnoreCase(context.getString(R.string.discharging)) ?
                            context.getString(R.string.time_to_empty_summary, statusValue) : context.getString(R.string.time_to_full_charge_summary, statusValue));
                    status.setVisibility(VISIBLE);
                } else {
                    status.setVisibility(GONE);
                }
            } else if (statusEntry.getIcon() == R.drawable.ic_battery_full) {
                int design = Battery.getDesignCapacityAsInt(context);
                if (design > 0) {
                    int actual = Integer.parseInt(statusEntry.getDescription().replace(" mAh", ""));
                    int percentage = (actual * 100) / design;
                    boolean valueEntered = design != (int) Math.round(Battery.getDesignCapacity(context));
                    String statusTxt = context.getString(R.string.capacity_design_summary, percentage + "%") +
                            (valueEntered ? " (" + context.getString(R.string.tap_to_modify_status) + ")" : "");
                    status.setText(statusTxt);
                } else {
                    status.setText(context.getString(R.string.capacity_design_input_message));
                }
                status.setVisibility(VISIBLE);
            }
        } else {
            plotView.setVisibility(GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (updateRunnable != null) {
            handler.post(updateRunnable);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
    }

}