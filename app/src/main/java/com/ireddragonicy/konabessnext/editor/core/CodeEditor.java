package com.ireddragonicy.konabessnext.editor.core;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Code editor for DTS files.
 * Extends VirtualizedCodeEditor for high-performance rendering with large
 * files.
 */
public class CodeEditor extends VirtualizedCodeEditor {

    public CodeEditor(Context context) {
        super(context);
    }

    public CodeEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CodeEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
