# Testing plan: one run that proves every feature on every supported server

Goal: `gradlew verify` runs everything, and the end result is one report that says, per
feature and per server (Spigot, CraftBukkit, Paper, Purpur, Folia, 1.17 – 26.3), whether
it works.

## 1. Where we are today

| Suite | Location | What it covers | Gaps |
|---|---|---|---|
| Unit / DB tests | `src/test` (3 classes) | Schema versioning and reset, concurrent DB access, SQLite driver compatibility (`testSqlite1_17`, `testSqlite1_18` run them against older bundled drivers) | No tests for services' business rules, `FactionName`, commands, tab completion, chat formatting |
| Cross-version harness | `integration-tests/harness` + `runner` | Starts 45 real servers in Docker, checks the plugin loads, DB file exists, `/f` is registered, console `/f create` is rejected, `/f help`, tab completion | Console only (no player features), one monolithic `InServerTests` class, no persistence/restart or safe-mode checks, no summary matrix |
| Manual servers | `runServerX_Y`, `runSpigotX_Y` tasks, `run/` | Hand testing | Not automated |

### Problems to fix first

1. **`/f help` test will crash on every server.** Commands send through
   `sender.spigot().sendMessage(BaseComponent...)` (`ChatMessage.java:48`), but
   `RecordingSender` returns `null` from `spigot()`. It needs to return a recording
   `CommandSender.Spigot` and turn `BaseComponent`s into plain text (`toPlainText()`).
2. **Java 16 is claimed but never tested.** The plugin is built for Java 16 and the README
   says 1.17 runs on Java 16, but the harness is built for Java 17 and `servers.csv` runs
   1.17.1 on Java 17. Build the harness for Java 16 and add a `1.17.1` row on `java16`.
3. **`servers.csv` has a "CHECK THESE" note.** Confirm 1.21.11 / 26.1.1 / 26.3 and the Folia
   versions exist before the first full run, or those rows fail for the wrong reason.

## 2. Target structure: four layers, one command

```
             ┌───────────────────────────────┐
  slow, few  │ L4  In-server, every server   │  integration-tests/  (Docker, fake players)
             ├───────────────────────────────┤
             │ L3  Command tests (JVM)       │  src/test/.../command   (mocked Player, real SQLite)
             ├───────────────────────────────┤
             │ L2  Service + repository      │  src/test/.../faction, player, database
             │     ("backend" tests)         │  (real SQLite in a temp folder)
             ├───────────────────────────────┤
 fast, many  │ L1  Pure unit tests           │  src/test/...  (no DB, no server)
             └───────────────────────────────┘
```

Each layer tests what only it can test. Business rules are checked thoroughly in L2
(milliseconds). L4 doesn't re-check every rule on 45 servers; it checks that each feature
**works end to end on that server's API and threading**.

### L1 – Pure unit tests (`src/test`, plain JUnit)

- `FactionName`: valid and invalid names, case-insensitivity, length limits.
- `Result`, `Pageable` (page clamping), `Role`.
- `ChatMessage` / `ListComponent` / `TitleComponent` / `MinecraftFont`: the components
  they build (text, colors, padding width).
- `CommandNode`: routing to subcommands, `help` after a subcommand, tab completion filtering.

### L2 – Service and repository tests ("backend" style)

Real SQLite in a `@TempDir`, real repositories, no mocks. This is where every rule lives.

Shared fixtures in `src/test/.../testsupport/`:
- `TestDatabase`: makes a fresh `DataBaseContext` with the schema, one per test.
- `Given`: small builders, e.g. `given.player("Alice")`, `given.faction("Red").ownedBy(alice).withMembers(bob)`.

**Rule: small tests (L1–L3) test what must not be possible; the L4 flow tests are the happy
paths.** No "creates a faction" or "accepts valid names" tests in L1–L3: the `Given` setup
already runs the real create/join code in every test, and L4 tests every flow end to end.
This is a soft rule. The exception is a hidden side effect a player can't see, such as a
deleted faction not leaving invites or join requests behind. Those get a small test,
written as "does not ...".

One test class per service, one test per rule:

| Class | Cases |
|---|---|
| `FactionServiceTest` | create rejects: empty/invalid names, invalid colors, taken names, players already in a faction · delete: only the owner, not without a faction, doesn't touch other factions, doesn't leave invites/join requests behind · pages that don't exist show the nearest page · completion doesn't treat `_` as a wildcard · owner/members reject unknown factions and players without one, don't list other factions' members |
| `FactionMembershipServiceTest` | leave: owner can't, not without a faction · kick: only the owner, not yourself, not other factions' members or owners, unknown names, not without a faction · completion doesn't suggest other factions' members or treat `_` as a wildcard |
| `FactionPlayerServiceTest` | rejoining doesn't create a second player or reset chat mode · leaving without joining creates nothing · same names aren't merged |
| Existing `DataBaseContextTest`, `DataBaseConcurrencyTest`, `SqliteCompatibilityTest` | Keep; move shared setup into `TestDatabase` |

Every L2 test also runs against the older SQLite drivers through the existing
`testSqlite*` tasks, which already reuse the whole test source set.

### L3 – Command tests in the JVM

Commands run with Mockito `Player` mocks wired to real services on a temp SQLite DB. A
`FakeChat` helper captures both `sendMessage(String)` and `spigot().sendMessage(BaseComponent)`
as plain text. Covers each command's branches and messages: create, delete, kick, leave,
list, members, owner, help, tab completion per argument, and `DatabaseResetCommand`
(safe mode, `reset`, `reset confirm`).

`Bukkit.getPlayer(...)` (used by delete and kick to notify online members) is static, so
either wrap it in a small `OnlinePlayers` interface that the commands receive, or leave the
"notify online member" check to L4. Recommendation: the interface, it keeps commands
testable without a server.

### L4 – In-server tests on every server (existing harness, restructured)

**Fake players.** `Player` is an interface, so a dynamic proxy can implement it on every
server type and version, just like `RecordingSender` does for `CommandSender`. A
`FakePlayer` has a fixed UUID and name, passes `instanceof Player`, records messages
(including `spigot()`), and has all permissions. With it the harness can run every player
command through the real command map:

```java
FakePlayer alice = players.create("Alice");
alice.run("f create Red");
check(alice.lastMessage()).contains("Created faction Red");
```

Limits: it isn't in `Bukkit.getOnlinePlayers()` / `Bukkit.getPlayer()`, so "notify online
member" is covered in L3 through `OnlinePlayers`. Fake players can't click GUIs or send real
chat packets; see section 5.

**Harness layout.** Split `InServerTests` into feature suites with a small registry:

```
harness/src/main/java/.../testharness/
  TestHarnessPlugin.java      runs suites, writes results (unchanged contract)
  framework/                  TestRegistry, Check, TestResult, FakePlayer, RecordingSender, Scheduling
  suites/
    PluginLifecycleSuite      loads + enables, DB file, plugin.yml (folia-supported, api-version)
    CommandRegistrationSuite  /faction + alias, executor, tab completer, console rejections
    FactionLifecycleSuite     create → list → owner → members → delete, as fake players
    MembershipSuite           kick, leave, owner-can't-leave, tab completion of members
    PlayerTrackingSuite       fire PlayerJoinEvent / PlayerQuitEvent with fake players, check DB
    HelpAndChatSuite          /f help, /f <sub> help, component formatting reaches the sender
    SafeModeSuite             (second boot, see below)
```

Each suite declares what it needs (`requires(PLUGIN_LOADS)`), so one root failure gives one
FAIL and clear SKIPs, as today. Tests isolate themselves with unique names and UUIDs
instead of wiping the DB between tests.

**Scenarios that need more than one boot.** The runner starts the same server a second time
with the same data folder:
- *Persistence:* boot 1 creates factions, boot 2 checks they're still there and the schema
  check says Ready.
- *Safe mode:* the runner puts an incompatible `Simple-Faction.db` in place before boot;
  the harness checks the plugin enters safe mode, `/f` explains it, and
  `f database reset confirm` brings it back to working.

Add `phase` (`fresh`, `restart`, `incompatible-db`) to the harness via an environment
variable; the runner runs the phases in order per server.

**Folia.** The harness already runs on the global region thread. Run every command suite
there too, so the DB lock and any accidental `Bukkit.getScheduler()` use show up as failures
on Folia only.

## 3. The runner: one run, one answer

- **Tiers** in `servers.csv` (4th column): `smoke` (Paper oldest + newest, Spigot newest,
  Folia newest: ~4 servers, a few minutes), `default` (one per minor version per type),
  `full` (everything). `-Pmatrix=smoke|default|full`, `-Pservers=` stays as a filter.
- **Compatibility matrix report** written at the end to
  `integration-tests/runner/build/reports/compat-matrix.html` (and `.md`): rows are tests,
  columns are servers, cells are pass/fail/skip with a link to the server log. This is the
  "do I know everything works" page.
- **One entry point** in the root build:
  - `gradlew verify` → `test` + `testSqlite*` + `integrationTest -Pmatrix=smoke`
  - `gradlew verify -Pmatrix=full` → before a release

## 4. Order of work

| # | Step | Outcome |
|---|---|---|
| 1 | Fix `RecordingSender.spigot()`, harness on Java 16, add `java16` row, confirm versions in `servers.csv` | Current harness green; baseline full run |
| 2 | `TestDatabase` + `Given` fixtures; L2 service tests for all three services | Every business rule tested in ms |
| 3 | L1 tests (`FactionName`, `CommandNode`, chat components) | Logic outside the DB covered |
| 4 | `OnlinePlayers` seam + L3 command tests | Every command branch and message covered |
| 5 | `FakePlayer` + split harness into suites; player feature suites | Every feature on every server |
| 6 | Multi-boot phases: persistence + safe mode | Upgrade/restart behaviour on every server |
| 7 | Tiers, `verify` task, compatibility matrix report | One command, one report |
| 8 | (optional) GitHub Actions: L1–L3 + smoke on PRs, full matrix nightly/before release, cache `.cache/` | Regressions caught without remembering to run |

### Full automation: making "green" mean "works"

| # | Step | Outcome |
|---|---|---|
| 9 | `full` tier tests **every** patch version (1.17.1 … 26.3) per server type, not only oldest/newest per minor | No version silently skipped |
| 10 | JaCoCo agent inside the test server containers; merge coverage across servers into one report | Proof of which plugin code actually ran on real servers; untested paths are visible |
| 11 | Missing-API check: scan the built jar against each server's API jar (e.g. Animal Sniffer-style signatures) | Catches `NoSuchMethodError` even in code no test executes |
| 12 | CI gates: L1–L3 + smoke on every PR, `full` nightly and required before a release | No release without a green full run |
| 13 | Weekly job queries Paper/Purpur/Folia/Spigot for new versions, adds them to `servers.csv` (PR) and runs the tests | New Minecraft versions tested before users hit them |
| 14 | Small Mineflayer bot smoke test on oldest + newest Paper (real join, a few commands; later GUI and chat) | Covers what fake players can't: real connections and packets |

Known limits even when everything is green: interaction with other plugins (optionally add one
run with LuckPerms/EssentialsX installed) and hybrid servers stay "expected to work".

**Rule for new features** (add to the project board's definition of done): a feature isn't
done until it has L2/L3 tests for everything that must not be possible, and an L4 flow test
where it works. Every feature must be in at least one L4 flow test: the small tests only
expect a "no", so a feature that always says "no" would pass them.

Happy paths the L4 flow tests must cover (moved out of the small tests): create, delete
(members end up without a faction), kick and leave, list and pages, owner and members
(owner first, then alphabetical), name completion, and join/quit updating the player's
name and last seen.

## 5. Not covered by this plan (on purpose)

- **GUIs and real chat** (backlog items: GUI framework, faction chat, prefixes in tab list).
  Fake players can't click inventories or send chat packets. When those land, add a small
  Mineflayer bot smoke test on the newest Paper only, after checking it supports 26.x.
- **Hybrid servers** (Mohist, Arclight): README says soft-supported and not tested. Could be
  added later as `expected-to-work` rows that don't fail the run.
