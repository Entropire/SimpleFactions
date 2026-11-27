package com.entropire.simplefactions.it;

enum Phase {
    FRESH("fresh", ""),
    RESTART("restart", "after a restart: "),
    INCOMPATIBLE_DB("incompatible-db", "with an incompatible database: ");

    final String id;
    final String label;

    Phase(String id, String label) {
        this.id = id;
        this.label = label;
    }
}
