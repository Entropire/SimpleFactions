package com.entropire.simplefactions.database;

public interface SchemaStatus {

    record Ready() implements SchemaStatus {}

    record Incompatible(int foundSchemaVersion, String createdByPluginVersion) implements SchemaStatus {}
}
