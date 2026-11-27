package com.entropire.simplefactions.testharness.framework;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

public final class SimpleFactionsPlugin {

    public static final String NAME = "SimpleFactions";
    public static final String DATABASE_FILE = "Simple-Faction.db";

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private SimpleFactionsPlugin() {}

    public static Plugin plugin() {
        return Bukkit.getPluginManager().getPlugin(NAME);
    }

    public static PluginCommand factionCommand() {
        PluginCommand command = Bukkit.getPluginCommand("faction");
        Check.that(command != null, "/faction is not registered");
        return command;
    }

    public static void run(CommandSender sender, String arguments) {
        factionCommand().execute(sender, "f", split(arguments));
    }

    public static void run(FakePlayer player, String arguments) {
        run(player.player(), arguments);
    }

    public static List<String> tabComplete(FakePlayer player, String arguments) {
        return factionCommand().tabComplete(player.player(), "f", arguments.split(" ", -1));
    }

    public static FakePlayer joinedPlayer(String name) throws ReflectiveOperationException {
        FakePlayer player = new FakePlayer(name + COUNTER.incrementAndGet());
        Events.join(player);
        return player;
    }

    public static String uniqueFactionName(String base) {
        return base + COUNTER.incrementAndGet();
    }

    public static void addMember(String factionName, FakePlayer player) throws Exception {
        execute("INSERT INTO faction_memberships (player_uuid, faction_uuid, role) "
                + "SELECT ?, uuid, 'MEMBER' FROM factions WHERE name = ?", player.uuid().toString(), factionName);
        Check.equal(factionName, factionOf(player), "faction of " + player.name() + " after adding them as a member");
    }

    public static String factionOf(FakePlayer player) throws Exception {
        return query("SELECT f.name FROM faction_memberships m JOIN factions f ON f.uuid = m.faction_uuid "
                + "WHERE m.player_uuid = ?", player.uuid().toString());
    }

    public static File databaseFile() {
        return new File(plugin().getDataFolder(), DATABASE_FILE);
    }

    public static String query(String sql, Object... params) throws Exception {
        try (Connection conn = openDatabase(); PreparedStatement stmt = prepare(conn, sql, params);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    public static void execute(String sql, Object... params) throws Exception {
        try (Connection conn = openDatabase(); PreparedStatement stmt = prepare(conn, sql, params)) {
            stmt.executeUpdate();
        }
    }

    private static Connection openDatabase() throws Exception {
        Driver driver = (Driver) Class.forName("org.sqlite.JDBC", true, plugin().getClass().getClassLoader())
                .getDeclaredConstructor().newInstance();
        Connection conn = driver.connect("jdbc:sqlite:" + databaseFile().getPath(), new Properties());
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute("PRAGMA busy_timeout = 5000");
        }
        return conn;
    }

    private static PreparedStatement prepare(Connection conn, String sql, Object... params) throws Exception {
        PreparedStatement stmt = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
        return stmt;
    }

    private static String[] split(String arguments) {
        return arguments.isEmpty() ? new String[0] : arguments.split(" ");
    }
}
