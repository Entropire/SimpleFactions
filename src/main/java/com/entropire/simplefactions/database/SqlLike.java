package com.entropire.simplefactions.database;

public final class SqlLike {

    private SqlLike() {}

    public static String startsWith(String prefix) {
        if (prefix == null) prefix = "";
        return prefix.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }
}
