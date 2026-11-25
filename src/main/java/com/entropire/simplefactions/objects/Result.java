package com.entropire.simplefactions.objects;

import java.util.Objects;
import java.util.Optional;

public sealed interface Result<T>
        permits Result.Success, Result.Failure, Result.Error {

    record Success<T>(T value) implements Result<T> {
        public Success {
            Objects.requireNonNull(value, "Success value cannot be null");
        }
    }

    record Failure<T>(String message)
            implements Result<T> {}

    record Error<T>(String message, Exception exception)
            implements Result<T> {}

    default boolean isSuccess() {
        return this instanceof Success;
    }

    @SuppressWarnings("unchecked")
    default <U> Result<U> propagate() {
        if (this instanceof Success) throw new IllegalStateException("Cannot propagate a Success");
        return (Result<U>) this;
    }

    static <T> Result<T> require(Result<Optional<T>> result, String messageIfMissing) {
        if (!(result instanceof Success<Optional<T>> success)) return result.propagate();

        return success.value().<Result<T>>map(Success::new).orElseGet(() -> new Failure<>(messageIfMissing));
    }
}
