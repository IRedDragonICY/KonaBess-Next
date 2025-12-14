package com.ireddragonicy.konabessnext.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.ireddragonicy.konabessnext.R;

public class GpuBinAdapter extends RecyclerView.Adapter<GpuBinAdapter.ViewHolder> {

    public static class BinItem {
        public final String title;
        public final String subtitle;

        public BinItem(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            BinItem binItem = (BinItem) o;
            return Objects.equals(title, binItem.title) &&
                    Objects.equals(subtitle, binItem.subtitle);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, subtitle);
        }
    }

    public static class BinDiffCallback extends DiffUtil.Callback {
        private final List<BinItem> oldList;
        private final List<BinItem> newList;

        public BinDiffCallback(List<BinItem> oldList, List<BinItem> newList) {
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
            // Items are the same if they have the same title (bin ID)
            return Objects.equals(oldList.get(oldItemPosition).title,
                    newList.get(newItemPosition).title);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }

    public void updateData(List<BinItem> newItems) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new BinDiffCallback(this.items, newItems));
        this.items.clear();
        this.items.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    public interface OnItemClickListener {
        void onBinClick(int position);
    }

    private List<BinItem> items;
    private OnItemClickListener clickListener;

    public GpuBinAdapter(List<BinItem> items) {
        // Make items list mutable if it isn't already
        if (items instanceof ArrayList) {
            this.items = items;
        } else {
            this.items = new ArrayList<>(items);
        }
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bin_item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BinItem item = items.get(position);
        holder.title.setText(item.title);
        if (item.subtitle == null || item.subtitle.isEmpty()) {
            holder.subtitle.setVisibility(View.GONE);
        } else {
            holder.subtitle.setVisibility(View.VISIBLE);
            holder.subtitle.setText(item.subtitle);
        }
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onBinClick(holder.getBindingAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View card;
        TextView title;
        TextView subtitle;
        ImageView icon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView;
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            icon = itemView.findViewById(R.id.icon);
        }
    }
}




