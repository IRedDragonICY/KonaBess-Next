package com.ireddragonicy.konabessnext.viewmodel;

/**
 * Sealed class pattern for representing UI states.
 * Used with LiveData to communicate loading, success, and error states to the
 * UI layer.
 *
 * @param <T> The type of data held by the Success state
 */
public abstract class UiState<T> {

    private UiState() {
        // Private constructor to prevent external instantiation
    }

    /**
     * Represents a loading state.
     */
    public static final class Loading<T> extends UiState<T> {
        private static final Loading<?> INSTANCE = new Loading<>();

        private Loading() {
        }

        @SuppressWarnings("unchecked")
        public static <T> Loading<T> getInstance() {
            return (Loading<T>) INSTANCE;
        }
    }

    /**
     * Represents a successful state with data.
     */
    public static final class Success<T> extends UiState<T> {
        private final T data;

        public Success(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }
    }

    /**
     * Represents an error state with a message.
     */
    public static final class Error<T> extends UiState<T> {
        private final String message;
        private final Throwable cause;

        public Error(String message) {
            this(message, null);
        }

        public Error(String message, Throwable cause) {
            this.message = message;
            this.cause = cause;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getCause() {
            return cause;
        }
    }

    // Convenience factory methods
    public static <T> UiState<T> loading() {
        return Loading.getInstance();
    }

    public static <T> UiState<T> success(T data) {
        return new Success<>(data);
    }

    public static <T> UiState<T> error(String message) {
        return new Error<>(message);
    }

    public static <T> UiState<T> error(String message, Throwable cause) {
        return new Error<>(message, cause);
    }

    // State checking methods
    public boolean isLoading() {
        return this instanceof Loading;
    }

    public boolean isSuccess() {
        return this instanceof Success;
    }

    public boolean isError() {
        return this instanceof Error;
    }

    /**
     * Get data if this is a Success state, otherwise return null.
     */
    public T getDataOrNull() {
        if (this instanceof Success) {
            return ((Success<T>) this).getData();
        }
        return null;
    }

    /**
     * Get error message if this is an Error state, otherwise return null.
     */
    public String getErrorMessageOrNull() {
        if (this instanceof Error) {
            return ((Error<T>) this).getMessage();
        }
        return null;
    }
}



