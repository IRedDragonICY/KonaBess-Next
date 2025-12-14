package com.ireddragonicy.konabessnext.viewmodel;

/**
 * Single-event wrapper for LiveData.
 * Used to represent one-time UI events such as navigation, toasts, or dialogs.
 * Prevents re-emission of events on configuration changes.
 *
 * @param <T> The type of content held by this event
 */
public class Event<T> {
    private final T content;
    private boolean hasBeenHandled = false;

    public Event(T content) {
        this.content = content;
    }

    /**
     * Returns the content and prevents its use again.
     * Use this for handling one-time events like navigation or toasts.
     *
     * @return The content if not already handled, null otherwise
     */
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        }
        hasBeenHandled = true;
        return content;
    }

    /**
     * Returns the content, even if it has already been handled.
     * Use this for peeking at the event content without consuming it.
     *
     * @return The content
     */
    public T peekContent() {
        return content;
    }

    /**
     * Check if this event has been handled.
     *
     * @return true if the event has been handled
     */
    public boolean hasBeenHandled() {
        return hasBeenHandled;
    }

    // Convenience factory method
    public static <T> Event<T> of(T content) {
        return new Event<>(content);
    }
}



