package in.sunilpaulmathew.batteryplus.adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.batteryplus.R;
import in.sunilpaulmathew.batteryplus.serializables.SettingsEntry;
import in.sunilpaulmathew.batteryplus.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 07, 2026
 */
public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    private final List<SettingsEntry> data;
    private final OnItemClickListener clickListener;

    public SettingsAdapter(List<SettingsEntry> data, OnItemClickListener clickListener) {
        this.data = data;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_settings, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SettingsEntry entry = data.get(position);
        Context context = holder.itemView.getContext();
        int color = entry.getIconColor(context);

        holder.title.setText(entry.geTitle());

        if (entry.getDescription() != null) {
            holder.description.setText(entry.getDescription());
            holder.description.setVisibility(VISIBLE);
        } else {
            holder.description.setVisibility(GONE);
        }

        if (entry.getIcon() != Integer.MIN_VALUE) {
            holder.icon.setImageResource(entry.getIcon());
            if (color != Integer.MIN_VALUE && entry.getIcon() != R.mipmap.ic_launcher) {
                holder.icon.setColorFilter(color);
            } else {
                holder.icon.clearColorFilter();
            }
            holder.icon.setVisibility(View.VISIBLE);
        } else {
            holder.icon.clearColorFilter();
            holder.icon.setVisibility(View.GONE);
        }

        if (entry.isSwitch()) {
            holder.checkBox.setVisibility(VISIBLE);
            holder.checkBox.setChecked(entry.isEnabled());
        } else {
            holder.checkBox.setVisibility(GONE);
        }

        if (entry.getID() == 0) {
            holder.title.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            holder.title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            holder.divider.setVisibility(VISIBLE);
        } else {
            holder.title.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            holder.divider.setVisibility(GONE);
        }

        Utils.setSlideInAnimation(holder.icon, position);
    }

    public void updateData(List<SettingsEntry> newData) {
        final List<SettingsEntry> oldList = new ArrayList<>(this.data);
        final List<SettingsEntry> newList = (newData != null) ? new ArrayList<>(newData) : Collections.emptyList();

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(oldList.get(oldItemPosition),
                        newList.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(oldList.get(oldItemPosition), newList.get(newItemPosition));
            }
        });

        this.data.clear();
        this.data.addAll(newList);

        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final AppCompatImageButton icon;
        private final MaterialCheckBox checkBox;
        private final MaterialTextView description, title;
        private final View divider;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.icon = view.findViewById(R.id.icon);
            this.checkBox = view.findViewById(R.id.checkbox);
            this.title = view.findViewById(R.id.title);
            this.description = view.findViewById(R.id.description);
            this.divider = view.findViewById(R.id.divider);
        }

        @Override
        public void onClick(View view) {
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;
            SettingsEntry settingsEntry = data.get(position);
            if (settingsEntry.getUrl() != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(settingsEntry.getUrl()));
                try {
                    view.getContext().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Utils.toast("No suitable app found...", view.getContext()).show();
                }
            } else {
                clickListener.onItemClick(settingsEntry.getID());
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int id);
    }

}