package com.entropire.simplefactions.testsupport;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

import com.entropire.simplefactions.database.DataBaseContext;
import com.entropire.simplefactions.faction.FactionRepository;
import com.entropire.simplefactions.faction.FactionService;
import com.entropire.simplefactions.faction.membership.FactionMembershipRepository;
import com.entropire.simplefactions.faction.membership.FactionMembershipService;
import com.entropire.simplefactions.objects.Result;
import com.entropire.simplefactions.player.FactionPlayerRepository;
import com.entropire.simplefactions.player.FactionPlayerService;

public final class TestDatabase {

    public final DataBaseContext db;

    public final FactionRepository factionRepository = new FactionRepository();
    public final FactionMembershipRepository membershipRepository = new FactionMembershipRepository();
    public final FactionPlayerRepository playerRepository = new FactionPlayerRepository();

    public final FactionService factionService;
    public final FactionMembershipService membershipService;
    public final FactionPlayerService playerService;

    public final Given given;

    private TestDatabase(DataBaseContext db) {
        this.db = db;
        this.factionService = new FactionService(db, factionRepository, membershipRepository);
        this.membershipService = new FactionMembershipService(db, membershipRepository);
        this.playerService = new FactionPlayerService(db, playerRepository);
        this.given = new Given(this);
    }

    public static TestDatabase create(Path folder) {
        DataBaseContext db = new DataBaseContext(folder.resolve("Simple-Faction.db").toString());
        Results.assertSuccess(db.initSchema("test"));
        return new TestDatabase(db);
    }

    public int count(String table) {
        return Integer.parseInt(query("SELECT COUNT(*) FROM " + table));
    }

    public void execute(String sql, Object... params) {
        Results.assertSuccess(db.withConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
                stmt.executeUpdate();
                return new Result.Success<>(true);
            }
        }));
    }

    public String query(String sql, Object... params) {
        return Results.assertSuccess(db.withConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
                try (ResultSet rs = stmt.executeQuery()) {
                    return new Result.Success<>(Optional.ofNullable(rs.next() ? rs.getString(1) : null));
                }
            }
        })).orElse(null);
    }
}
