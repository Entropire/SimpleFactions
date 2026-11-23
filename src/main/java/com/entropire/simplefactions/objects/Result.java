package com.entropire.simplefactions.objects;

public sealed interface Result<T>
        permits Result.Success, Result.Failure, Result.Error {

    record Success<T>(T value) implements Result<T> {}

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
}