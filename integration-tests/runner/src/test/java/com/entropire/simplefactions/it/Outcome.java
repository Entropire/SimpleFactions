package com.entropire.simplefactions.it;

import java.nio.file.Path;
import java.util.List;

/** What happened on one server: either the harness' results, or why there are none. */
record Outcome(ServerTarget target, List<HarnessResult> results, String error, Path logFile) {

    static Outcome finished(ServerTarget target, List<HarnessResult> results, Path logFile) {
        return new Outcome(target, results, null, logFile);
    }

    static Outcome failed(ServerTarget target, String error, Path logFile) {
        return new Outcome(target, List.of(), error, logFile);
    }
}
