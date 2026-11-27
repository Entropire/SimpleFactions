package com.entropire.simplefactions.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.entropire.simplefactions.faction.Faction;
import com.entropire.simplefactions.faction.FactionRepository;
import com.entropire.simplefactions.faction.FactionService;
import com.entropire.simplefactions.faction.membership.FactionMembership;
import com.entropire.simplefactions.faction.membership.FactionMembershipRepository;
import com.entropire.simplefactions.faction.membership.FactionMembershipService;
import com.entropire.simplefactions.faction.membership.Role;
import com.entropire.simplefactions.objects.Result;
import com.entropire.simplefactions.player.FactionPlayerRepository;
import com.entropire.simplefactions.player.FactionPlayerService;

class DataBaseConcurrencyTest {

    private static final int THREADS = 16;

    @TempDir
    Path tempDir;

    private DataBaseContext db;
    private FactionService factionService;
    private FactionMembershipService membershipService;
    private FactionPlayerService playerService;
    private final FactionMembershipRepository membershipRepository = new FactionMembershipRepository();
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        db = new DataBaseContext(tempDir.resolve("Simple-Faction.db").toString());
        assertInstanceOf(Result.Success.class, db.initSchema("test"));

        factionService = new FactionService(db, new FactionRepository(), membershipRepository);
        membershipService = new FactionMembershipService(db, membershipRepository);
        playerService = new FactionPlayerService(db, new FactionPlayerRepository());
        executor = Executors.newFixedThreadPool(THREADS);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        executor.shutdownNow();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS), "worker threads did not stop");
    }

    private <T> List<T> runAtOnce(IntFunction<Callable<T>> task) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        List<Future<T>> futures = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            Callable<T> work = task.apply(i);
            futures.add(executor.submit(() -> {
                start.await();
                return work.call();
            }));
        }
        start.countDown();

        List<T> results = new ArrayList<>();
        for (Future<T> future : futures) {
            results.add(future.get(60, TimeUnit.SECONDS));
        }
        return results;
    }

    private static void assertAllSucceeded(List<? extends Result<?>> results, String what) {
        List<String> problems = new ArrayList<>();
        for (Result<?> result : results) {
            if (!result.isSuccess()) problems.add(result.toString());
        }
        assertTrue(problems.isEmpty(), what + " failed " + problems.size() + "/" + results.size() + " times: " + problems);
    }

    private int count(String table) {
        Result<Integer> result = db.withConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM " + table);
                 ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return new Result.Success<>(rs.getInt(1));
            }
        });
        if (!(result instanceof Result.Success<Integer> success)) throw new AssertionError("count failed: " + result);
        return success.value();
    }

    @Test
    void parallelJoinsCreatesAndLeavesAllSucceed() throws Exception {
        UUID[] owners = new UUID[THREADS];
        UUID[] members = new UUID[THREADS];
        for (int i = 0; i < THREADS; i++) {
            owners[i] = UUID.randomUUID();
            members[i] = UUID.randomUUID();
        }

        assertAllSucceeded(runAtOnce(i -> () -> playerService.registerJoin(owners[i], "owner" + i)), "owner join");
        assertAllSucceeded(runAtOnce(i -> () -> playerService.registerJoin(members[i], "member" + i)), "member join");

        List<Result<Faction>> created = runAtOnce(i -> () -> factionService.createFaction("faction_" + i, "#ffffff", owners[i]));
        assertAllSucceeded(created, "create faction");

        assertAllSucceeded(runAtOnce(i -> () -> {
            if (!(created.get(i) instanceof Result.Success<Faction> faction)) throw new AssertionError("no faction for " + i);
            return db.transaction(conn -> membershipRepository.save(conn,
                    new FactionMembership(members[i], faction.value().uuid(), Role.MEMBER, null)));
        }), "join faction");

        List<Result<?>> mixed = runAtOnce(i -> () -> i % 2 == 0
                ? membershipService.leaveFaction(members[i])
                : playerService.registerJoin(owners[i], "owner" + i));
        assertAllSucceeded(mixed, "leave or rejoin");

        assertEquals(THREADS, count("factions"));
        assertEquals(2 * THREADS, count("players"));

        assertEquals(THREADS + THREADS / 2, count("faction_memberships"));
    }

    @Test
    void parallelCreatesWithTheSameNameHaveExactlyOneWinner() throws Exception {
        UUID[] owners = new UUID[THREADS];
        for (int i = 0; i < THREADS; i++) owners[i] = UUID.randomUUID();
        assertAllSucceeded(runAtOnce(i -> () -> playerService.registerJoin(owners[i], "owner" + i)), "owner join");

        List<Result<Faction>> results = runAtOnce(i -> () -> factionService.createFaction("contested", "#ffffff", owners[i]));

        int successes = 0;
        List<String> unexpected = new ArrayList<>();
        for (Result<Faction> result : results) {
            if (result.isSuccess()) successes++;
            else if (!(result instanceof Result.Failure<Faction>)) unexpected.add(result.toString());
        }
        assertTrue(unexpected.isEmpty(), "losing creates should fail cleanly, got: " + unexpected);
        assertEquals(1, successes, "exactly one create should win");
        assertEquals(1, count("factions"));
    }
}
