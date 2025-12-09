package com.ireddragonicy.konabessnext.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

import java.util.Collections;
import java.util.List;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.widget.ImageButton;

import androidx.recyclerview.widget.DiffUtil;

import com.ireddragonicy.konabessnext.R;
import com.ireddragonicy.konabessnext.SettingsActivity;

public class GpuFreqAdapter extends RecyclerView.Adapter<GpuFreqAdapter.ViewHolder> {

    public static class FreqItem {
        public enum ActionType {
            NONE,
            BACK,
            ADD_TOP,
            ADD_BOTTOM,
            DUPLICATE
        }

        public String title;
        public String subtitle;
        public boolean isHeader;
        public boolean isFooter;
        public int originalPosition;
        public ActionType actionType;
        public int targetPosition; // For duplicate action, stores the target frequency position
        public boolean isHighlighted; // For visual feedback on long-press

        // Spec details for frequency items
        public String busMax;
        public String busMin;
        public String busFreq;
        public String voltageLevel;
        public long frequencyHz = -1L;

        public FreqItem(String title, String subtitle, ActionType actionType) {
            this.title = title;
            this.subtitle = subtitle;
            this.actionType = actionType;
            this.isHeader = actionType == ActionType.BACK || actionType == ActionType.ADD_TOP;
            this.isFooter = actionType == ActionType.ADD_BOTTOM;
            this.originalPosition = -1;
            this.targetPosition = -1;
        }

        public FreqItem(String title, String subtitle) {
            this(title, subtitle, ActionType.NONE);
        }

        public boolean isLevelItem() {
            return actionType == ActionType.NONE;
        }

        public boolean isActionItem() {
            return actionType == ActionType.ADD_TOP || actionType == ActionType.ADD_BOTTOM;
        }

        public boolean isDuplicateItem() {
            return actionType == ActionType.DUPLICATE;
        }

        public boolean hasSpecs() {
            return busMax != null || busMin != null || busFreq != null || voltageLevel != null;
        }

        public boolean hasFrequencyValue() {
            return frequencyHz >= 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            FreqItem freqItem = (FreqItem) o;
            return isHeader == freqItem.isHeader &&
                    isFooter == freqItem.isFooter &&
                    originalPosition == freqItem.originalPosition &&
                    targetPosition == freqItem.targetPosition &&
                    isHighlighted == freqItem.isHighlighted &&
                    frequencyHz == freqItem.frequencyHz &&
                    actionType == freqItem.actionType &&
                    java.util.Objects.equals(title, freqItem.title) &&
                    java.util.Objects.equals(subtitle, freqItem.subtitle) &&
                    java.util.Objects.equals(busMax, freqItem.busMax) &&
                    java.util.Objects.equals(busMin, freqItem.busMin) &&
                    java.util.Objects.equals(busFreq, freqItem.busFreq) &&
                    java.util.Objects.equals(voltageLevel, freqItem.voltageLevel);
        }
    }

    private List<FreqItem> items;
    private Context context;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;
    private OnDeleteClickListener deleteClickListener;
    private OnStartDragListener dragStartListener;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    public static class FreqDiffCallback extends DiffUtil.Callback {
        private final List<FreqItem> oldList;
        private final List<FreqItem> newList;

        public FreqDiffCallback(List<FreqItem> oldList, List<FreqItem> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

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
            // For simple lists, we can rely on object identity or a unique ID if we had
            // one.
            // Here, relying on content equality for "sameness" in absence of IDs might be
            // tricky if duplicates are allowed,
            // but assuming position/content combination matters.
            // Better to check if they represent the same logical item.
            // Since we don't have stable IDs, we'll try to match by properties.
            FreqItem oldItem = oldList.get(oldItemPosition);
            FreqItem newItem = newList.get(newItemPosition);
            return oldItem.actionType == newItem.actionType &&
                    java.util.Objects.equals(oldItem.title, newItem.title) &&
                    oldItem.frequencyHz == newItem.frequencyHz;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }

    public void updateData(List<FreqItem> newItems) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new FreqDiffCallback(this.items, newItems));
        this.items.clear();
        this.items.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public GpuFreqAdapter(List<FreqItem> items, Context context) {
        this.items = items;
        // Make items list mutable if it isn't already, though usually passed as
        // ArrayList
        if (!(items instanceof java.util.ArrayList)) {
            this.items = new java.util.ArrayList<>(items);
        }
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public void setOnStartDragListener(OnStartDragListener listener) {
        this.dragStartListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.gpu_freq_item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FreqItem item = items.get(position);

        holder.title.setText(item.title);
        holder.subtitle.setText(item.subtitle);

        boolean isLevel = item.isLevelItem();
        boolean isAction = item.isActionItem();
        boolean isDuplicate = item.isDuplicateItem();
        boolean isBack = item.actionType == FreqItem.ActionType.BACK;

        // Reset visibility defaults
        holder.subtitle.setVisibility(item.subtitle == null || item.subtitle.isEmpty() ? View.GONE : View.VISIBLE);

        if (isLevel) {
            if (item.hasFrequencyValue()) {
                String formatted = SettingsActivity.formatFrequency(item.frequencyHz, context);
                if (!formatted.equals(item.title)) {
                    item.title = formatted;
                }
                holder.title.setText(formatted);
            }
            int baseColor = MaterialColors.getColor(holder.card,
                    com.google.android.material.R.attr.colorSurface);
            int highlightColor = MaterialColors.getColor(holder.card,
                    com.google.android.material.R.attr.colorSurfaceVariant);

            holder.card.setCardBackgroundColor(item.isHighlighted ? highlightColor : baseColor);
            int onSurface = MaterialColors.getColor(holder.card,
                    com.google.android.material.R.attr.colorOnSurface);
            int onSurfaceVariant = MaterialColors.getColor(holder.card,
                    com.google.android.material.R.attr.colorOnSurfaceVariant);

            holder.title.setTextColor(onSurface);
            holder.subtitle.setTextColor(onSurfaceVariant);
            holder.dragHandle.setVisibility(View.GONE); // Drag by long-press on card
            holder.deleteIcon.setVisibility(View.VISIBLE);
            holder.copyButton.setVisibility(View.VISIBLE);

            // Show spec details if available
            if (item.hasSpecs()) {
                holder.subtitle.setVisibility(View.GONE);
                holder.specsContainer.setVisibility(View.VISIBLE);

                // Populate spec values
                if (item.busMax != null) {
                    holder.busMaxValue.setText(item.busMax);
                    holder.busMaxValue.setVisibility(View.VISIBLE);
                } else {
                    holder.busMaxValue.setVisibility(View.GONE);
                }

                if (item.busMin != null) {
                    holder.busMinValue.setText(item.busMin);
                    holder.busMinValue.setVisibility(View.VISIBLE);
                } else {
                    holder.busMinValue.setVisibility(View.GONE);
                }

                if (item.busFreq != null) {
                    holder.busFreqValue.setText(item.busFreq);
                    holder.busFreqValue.setVisibility(View.VISIBLE);
                } else {
                    holder.busFreqValue.setVisibility(View.GONE);
                }

                if (item.voltageLevel != null) {
                    holder.voltageValue.setText(item.voltageLevel);
                    holder.voltageValue.setVisibility(View.VISIBLE);
                } else {
                    holder.voltageValue.setVisibility(View.GONE);
                }
            } else {
                holder.specsContainer.setVisibility(View.GONE);
            }
        } else if (isAction) {
            int container = MaterialColors.getColor(holder.card,
                    com.google.android.material.R.attr.colorSecondaryContainer);
            int onContainer = MaterialColors.getColor(holder.card,
                    com.google.android.material.R.attr.colorOnSecondaryContainer);

            holder.card.setCardBackgroundColor(container);
            holder.title.setTextColor(onContainer);
            holder.subtitle.setTextColor(onContainer);
            holder.dragHandle.setVisibility(View.VISIBLE);
            holder.dragHandle.setImageResource(item.actionType == FreqItem.ActionType.ADD_TOP
                    ? R.drawable.ic_arrow_upward
                    : R.drawable.ic_arrow_downward);
            holder.dragHandle.setImageTintList(ColorStateList.valueOf(onContainer));
            holder.deleteIcon.setVisibility(View.GONE);
            holder.copyButton.setVisibility(View.GONE);
            holder.specsContainer.setVisibility(View.GONE);
        } else if (isDuplicate) {
            // Duplicate action with tertiary color scheme
            int container = MaterialColors.getColor(holder.card,
                    com.google.android.material.R.attr.colorTertiaryContainer);
            int onContainer = MaterialColors.getColor(holder.card,
                    com.google.android.material.R.attr.colorOnTertiaryContainer);

            holder.card.setCardBackgroundColor(container);
            holder.title.setTextColor(onContainer);
            holder.subtitle.setTextColor(onContainer);
            holder.dragHandle.setVisibility(View.VISIBLE);
            holder.dragHandle.setImageResource(R.drawable.ic_arrow_downward);
            holder.dragHandle.setImageTintList(ColorStateList.valueOf(onContainer));
            holder.deleteIcon.setVisibility(View.GONE);
            holder.copyButton.setVisibility(View.GONE);
            holder.specsContainer.setVisibility(View.GONE);
        } else if (isBack) {
            int surfaceVariant = MaterialColors.getColor(holder.card,
                    com.google.android.material.R.attr.colorSurfaceVariant);
            int onSurfaceVariant = MaterialColors.getColor(holder.card,
                    com.google.android.material.R.attr.colorOnSurfaceVariant);

            holder.card.setCardBackgroundColor(surfaceVariant);
            holder.title.setTextColor(onSurfaceVariant);
            holder.subtitle.setTextColor(onSurfaceVariant);
            holder.dragHandle.setVisibility(View.GONE);
            holder.deleteIcon.setVisibility(View.GONE);
            holder.copyButton.setVisibility(View.GONE);
            holder.specsContainer.setVisibility(View.GONE);
        }

        boolean isInteractive = isLevel;

        // Set click listeners
        holder.mainContent.setOnClickListener(v -> {
            if (clickListener != null)
                clickListener.onItemClick(position);
        });

        holder.mainContent.setOnLongClickListener(v -> {
            if (longClickListener != null && isInteractive) {
                longClickListener.onItemLongClick(position);
                return true;
            }
            return false;
        });

        holder.deleteIcon.setOnClickListener(v -> {
            if (deleteClickListener != null && isInteractive) {
                deleteClickListener.onDeleteClick(position);
            }
        });

        holder.copyButton.setOnClickListener(v -> {
            if (item.hasFrequencyValue()) {
                String label = item.title;
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Frequency", label);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    android.widget.Toast.makeText(context, "Frequency copied", android.widget.Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        // Drag is now handled by long-pressing anywhere on the card (ItemTouchHelperCallback)
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        sharedPreferences = context.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        preferenceChangeListener = (prefs, key) -> {
            if (SettingsActivity.KEY_FREQ_UNIT.equals(key)) {
                refreshFrequencyUnits();
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        refreshFrequencyUnits();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (sharedPreferences != null && preferenceChangeListener != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        }
        preferenceChangeListener = null;
        sharedPreferences = null;
    }

    private void refreshFrequencyUnits() {
        for (int i = 0; i < items.size(); i++) {
            FreqItem item = items.get(i);
            if (item.hasFrequencyValue()) {
                item.title = SettingsActivity.formatFrequency(item.frequencyHz, context);
                notifyItemChanged(i);
            }
        }
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < 0 || toPosition < 0 || fromPosition >= items.size() || toPosition >= items.size()) {
            return;
        }

        // Prevent moving header or footer items
        FreqItem fromItem = items.get(fromPosition);
        FreqItem toItem = items.get(toPosition);

        if (fromItem.isHeader || fromItem.isFooter || toItem.isHeader || toItem.isFooter) {
            return;
        }

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    public List<FreqItem> getItems() {
        return items;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        View mainContent;
        ImageView dragHandle;
        TextView title;
        TextView subtitle;
        ImageButton deleteIcon;
        ImageButton copyButton;

        // Spec details views
        View specsContainer;
        TextView busMaxValue;
        TextView busMinValue;
        TextView busFreqValue;
        TextView voltageValue;

        ViewHolder(View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            mainContent = itemView.findViewById(R.id.main_content);
            dragHandle = itemView.findViewById(R.id.drag_handle);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
            copyButton = itemView.findViewById(R.id.btn_copy);

            // Spec details
            specsContainer = itemView.findViewById(R.id.specs_container);
            busMaxValue = itemView.findViewById(R.id.bus_max_value);
            busMinValue = itemView.findViewById(R.id.bus_min_value);
            busFreqValue = itemView.findViewById(R.id.bus_freq_value);
            voltageValue = itemView.findViewById(R.id.voltage_value);
        }
    }
}