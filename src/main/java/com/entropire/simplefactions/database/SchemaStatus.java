package com.entropire.simplefactions.database;

public sealed interface SchemaStatus {

    record Ready() implements SchemaStatus {}

    record Incompatible(int foundSchemaVersion, String createdByPluginVersion) implements SchemaStatus {}
}
