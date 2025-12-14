package com.ireddragonicy.konabessnext.utils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.ireddragonicy.konabessnext.ui.adapters.GpuFreqAdapter;

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final GpuFreqAdapter adapter;

    public ItemTouchHelperCallback(GpuFreqAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true; // Enable drag by long-pressing anywhere on the card
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false; // No swipe to dismiss
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
            @NonNull RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();

        if (fromPosition < 0 || toPosition < 0 || fromPosition >= adapter.getItems().size()
                || toPosition >= adapter.getItems().size()) {
            return false;
        }

        GpuFreqAdapter.FreqItem fromItem = adapter.getItems().get(fromPosition);
        GpuFreqAdapter.FreqItem toItem = adapter.getItems().get(toPosition);

        if (fromItem.isHeader || fromItem.isFooter || toItem.isHeader || toItem.isFooter) {
            return false;
        }

        adapter.onItemMove(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Not used
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            // Item is being dragged or swiped
            if (viewHolder != null) {
                viewHolder.itemView.setAlpha(0.7f);
                viewHolder.itemView.setScaleX(1.05f);
                viewHolder.itemView.setScaleY(1.05f);
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        viewHolder.itemView.setAlpha(1.0f);
        viewHolder.itemView.setScaleX(1.0f);
        viewHolder.itemView.setScaleY(1.0f);
    }
}



