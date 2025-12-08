package com.ireddragonicy.konabessnext.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.ireddragonicy.konabessnext.R;

public class GpuBinAdapter extends RecyclerView.Adapter<GpuBinAdapter.ViewHolder> {

    public static class BinItem {
        public final String title;
        public final String subtitle;

        public BinItem(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
        }
    }

    public interface OnItemClickListener {
        void onBinClick(int position);
    }

    private final List<BinItem> items;
    private OnItemClickListener clickListener;

    public GpuBinAdapter(List<BinItem> items) {
        this.items = items;
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
