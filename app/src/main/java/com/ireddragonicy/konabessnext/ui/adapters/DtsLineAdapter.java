package com.ireddragonicy.konabessnext.ui.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ireddragonicy.konabessnext.R;

import java.util.ArrayList;

/**
 * RecyclerView adapter for the virtualized DTS text editor.
 * Each item represents one line of the file.
 */
public class DtsLineAdapter extends RecyclerView.Adapter<DtsLineAdapter.ViewHolder> {

    private ArrayList<String> lines;
    private final Context context;
    private OnLineChangedListener listener;
    private int focusedPosition = -1;
    private int highlightedPosition = -1;

    public interface OnLineChangedListener {
        /**
         * Called when line content changes.
         */
        void onLineChanged(int position, String newContent);

        /**
         * Called when Enter is pressed, requesting a new line.
         * 
         * @param position     Current line position
         * @param beforeCursor Text before cursor position
         * @param afterCursor  Text after cursor position
         */
        void onNewLineRequested(int position, String beforeCursor, String afterCursor);

        /**
         * Called when Backspace is pressed on an empty line or at line start.
         * 
         * @param position Current line position
         */
        void onLineMergeRequested(int position);

        /**
         * Called when focus changes between lines.
         */
        void onFocusChanged(int oldPosition, int newPosition);
    }

    public DtsLineAdapter(Context context, ArrayList<String> lines) {
        this.context = context;
        this.lines = lines;
    }

    public void setOnLineChangedListener(OnLineChangedListener listener) {
        this.listener = listener;
    }

    public void setLines(ArrayList<String> lines) {
        this.lines = lines;
        notifyDataSetChanged();
    }

    public void highlightLine(int position) {
        int oldHighlight = highlightedPosition;
        highlightedPosition = position;
        if (oldHighlight >= 0) {
            notifyItemChanged(oldHighlight);
        }
        if (position >= 0 && position < getItemCount()) {
            notifyItemChanged(position);
        }
    }

    public void clearHighlight() {
        int oldHighlight = highlightedPosition;
        highlightedPosition = -1;
        if (oldHighlight >= 0) {
            notifyItemChanged(oldHighlight);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dts_line, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // CRITICAL: Remove TextWatcher BEFORE setting text to prevent recycling bugs
        if (holder.textWatcher != null) {
            holder.lineContent.removeTextChangedListener(holder.textWatcher);
        }

        // Set line number (1-indexed)
        holder.lineNumber.setText(String.valueOf(position + 1));

        // Set content
        String content = lines.get(position);
        holder.lineContent.setText(content);

        // Apply highlight if this is the searched line
        if (position == highlightedPosition) {
            holder.itemView.setBackgroundColor(context.getColor(R.color.md_theme_light_primaryContainer));
        } else {
            holder.itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }

        // Store position in tag for callbacks
        holder.lineContent.setTag(position);

        // Create new TextWatcher for this binding
        holder.textWatcher = new TextWatcher() {
            private int currentPosition = position;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Get actual position from tag (in case of recycling)
                Object tag = holder.lineContent.getTag();
                if (tag instanceof Integer) {
                    int pos = (Integer) tag;
                    if (pos >= 0 && pos < lines.size()) {
                        String newContent = s.toString();
                        // Only update if actually changed
                        if (!newContent.equals(lines.get(pos))) {
                            lines.set(pos, newContent);
                            if (listener != null) {
                                listener.onLineChanged(pos, newContent);
                            }
                        }
                    }
                }
            }
        };

        // Add TextWatcher AFTER setting text
        holder.lineContent.addTextChangedListener(holder.textWatcher);

        // Handle focus changes
        holder.lineContent.setOnFocusChangeListener((v, hasFocus) -> {
            Object tag = holder.lineContent.getTag();
            if (tag instanceof Integer) {
                int pos = (Integer) tag;
                if (hasFocus) {
                    int oldFocus = focusedPosition;
                    focusedPosition = pos;
                    if (listener != null && oldFocus != pos) {
                        listener.onFocusChanged(oldFocus, pos);
                    }
                }
            }
        });

        // Handle Enter key for new line
        holder.lineContent.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                Object tag = holder.lineContent.getTag();
                if (tag instanceof Integer && listener != null) {
                    int pos = (Integer) tag;
                    int cursorPos = holder.lineContent.getSelectionStart();
                    String text = holder.lineContent.getText().toString();
                    String before = text.substring(0, cursorPos);
                    String after = text.substring(cursorPos);
                    listener.onNewLineRequested(pos, before, after);
                    return true;
                }
            }
            return false;
        });

        // Handle Backspace on empty line or at start
        holder.lineContent.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                Object tag = holder.lineContent.getTag();
                if (tag instanceof Integer && listener != null) {
                    int pos = (Integer) tag;
                    int cursorPos = holder.lineContent.getSelectionStart();
                    String text = holder.lineContent.getText().toString();

                    // If at the beginning of line (or line is empty) and not first line
                    if (cursorPos == 0 && pos > 0) {
                        listener.onLineMergeRequested(pos);
                        return true;
                    }
                }
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return lines != null ? lines.size() : 0;
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        // Clean up listeners to prevent memory leaks
        if (holder.textWatcher != null) {
            holder.lineContent.removeTextChangedListener(holder.textWatcher);
        }
        holder.lineContent.setOnFocusChangeListener(null);
        holder.lineContent.setOnEditorActionListener(null);
        holder.lineContent.setOnKeyListener(null);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView lineNumber;
        EditText lineContent;
        TextWatcher textWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            lineNumber = itemView.findViewById(R.id.line_number);
            lineContent = itemView.findViewById(R.id.line_content);
        }
    }
}




