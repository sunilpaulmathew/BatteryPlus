package in.sunilpaulmathew.batteryplus.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.batteryplus.BuildConfig;
import in.sunilpaulmathew.batteryplus.R;
import in.sunilpaulmathew.batteryplus.adapters.PolicyAdapter;
import in.sunilpaulmathew.batteryplus.serializables.StatusEntry;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on Sept. 07, 2026
 */
public class PolicyDialog extends BottomSheetDialog {

    public PolicyDialog(Context context) {
        super(context);

        View root = View.inflate(context, R.layout.layout_privacy_policy, null);
        MaterialButton cancel = root.findViewById(R.id.cancel);
        MaterialTextView title = root.findViewById(R.id.title);
        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);

        String titleString = context.getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME;
        title.setText(titleString);
        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new PolicyAdapter(getPolicyData()));

        cancel.setOnClickListener(v -> dismiss());

        root.post(() -> {
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
        setContentView(root);
        show();
    }

    public static List<StatusEntry> getPolicyData() {
        List<StatusEntry> mData = new ArrayList<>();
        mData.add(new StatusEntry("Introduction", "Battery Plus is developed by one main developer, sunilpaulmathew, leveraging code from various open-source projects. This Privacy Policy outlines how we handle user privacy."));
        mData.add(new StatusEntry("Scope", "This policy applies exclusively to the original version of Battery Plus published by the developer on Google Play, and GitHub."));
        mData.add(new StatusEntry("Personal Information", "We do not collect, store, or share any personal information about our users. User identities remain anonymous. If we inadvertently receive any personal information, we will not disclose or share it with third parties."));
        mData.add(new StatusEntry("Contact Us", "If you have questions or concerns about this Privacy Policy, please contact us at: smartpack.org@gmail.com"));
        mData.add(new StatusEntry("Changes to This Policy", "We may update this policy from time to time. Changes will be posted here."));
        return mData;
    }

}