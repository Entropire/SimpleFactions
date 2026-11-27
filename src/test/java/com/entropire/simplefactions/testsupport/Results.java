package com.entropire.simplefactions.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.entropire.simplefactions.objects.Result;

public final class Results {

    private Results() {}

    public static <T> T assertSuccess(Result<T> result) {
        if (result instanceof Result.Success<T> success) return success.value();
        return fail("Expected a Success but got " + describe(result));
    }

    public static String assertFailure(Result<?> result) {
        if (result instanceof Result.Failure<?> failure) return failure.message();
        return fail("Expected a Failure but got " + describe(result));
    }

    public static void assertFailure(Result<?> result, String expectedMessage) {
        assertEquals(expectedMessage, assertFailure(result));
    }

    private static String describe(Result<?> result) {
        if (result instanceof Result.Error<?> error && error.exception() != null) {
            return result + " caused by " + error.exception();
        }
        return String.valueOf(result);
    }
}
