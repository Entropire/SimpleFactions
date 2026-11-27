# Cross-version integration tests

These tests start real servers of every type and version in `runner/servers.csv`
(Paper, Purpur, Folia, Spigot and CraftBukkit, from 1.17.1 up to 26.x), install
SimpleFactions plus a test plugin in each, and run the tests inside the server.
Everything runs locally in Docker; nothing is installed on your PC except Docker itself.

## What you need

- **Docker Desktop**, running. Check with `docker version` in a terminal.
- **Disk space**: roughly 10-15 GB for the full list (server jars, Mojang files, built
  Spigot/CraftBukkit jars and the Docker images for Java 16, 17, 21 and 25).
- **Memory**: each server uses about 1.5 GB, and `-Pparallel=2` (the default) runs two at once.

## Running the tests

Windows uses `gradlew.bat`, macOS and Linux `./gradlew`.

**One command for everything**, from the project root:

```
gradlew verify                      # unit tests + smoke servers: a few minutes, while you code
gradlew verify -Pmatrix=default     # unit tests + one server per Java era per server type
gradlew verify -Pmatrix=full        # unit tests + every server in servers.csv: before a release
```

`verify` runs the unit tests first (also against the SQLite versions old servers ship) and only
starts servers when they pass. The last column of `servers.csv` says which run size includes
each server.

Only the server tests:

```
gradlew integrationTest                                  # the default run size
gradlew integrationTest -Pmatrix=full                    # every server in servers.csv
gradlew integrationTest -Pservers=paper                  # only Paper servers (any run size)
gradlew integrationTest -Pservers=1.21.4                 # every server type on 1.21.4
gradlew integrationTest -Pservers=paper-1.17.1,folia     # one server plus all Folia servers
gradlew integrationTest -Pparallel=4                     # four servers at a time
gradlew integrationTest -PstartupTimeoutMinutes=40       # more time for a slow first run
```

The **first full run is slow**: every server jar is downloaded, and Spigot and CraftBukkit
are built with BuildTools for each version, which takes several minutes per version. All of
that is cached in `integration-tests/.cache`, so later runs only start the servers.

## Reading the results

- The console shows each server as it starts and finishes, then every test result.
- **The compatibility report** has every test as a row and every server as a column, with
  ✓, ✗ or – (skipped) in each cell. Hover a ✗ for the failure, a server for its exact version
  and Java version; click a server for its log:
  `integration-tests/runner/build/reports/compatibility/index.html`
  (and `compatibility.md` next to it, for pasting into a pull request or issue)
- The Gradle test report groups the results per server (for example `paper-1.21.4`):
  `integration-tests/runner/build/reports/tests/integrationTest/index.html`
- Each server's full log: `integration-tests/runner/build/server-logs/<server>.log`, plus
  `<server>-restart.log` and `<server>-incompatible-db.log` for the later boots
- BuildTools logs: `integration-tests/runner/build/server-logs/buildtools-<version>.log`

Every server is started three times, keeping its data between the boots:

1. **fresh**: all feature tests, then a faction with a member is saved for the next boot.
2. **restart** (results start with "after a restart:"): checks the faction, members and
   players are still there, then marks the database as made by a newer SimpleFactions.
3. **incompatible-db** (results start with "with an incompatible database:"): checks the
   plugin starts in safe mode, the log explains the options, and `f database reset confirm`
   from the console brings it back.

Every server reports:

- **server starts and runs the test harness**: fails if the server couldn't be downloaded,
  crashed, or the harness never reported back. The failure shows the end of the server log.
- **environment: ...**: the exact server software, version and Java version that ran, so
  you can see it really tested what you think it tested.
- One result per in-server test. When **plugin loads and enables** fails, the failure
  includes the server log lines about SimpleFactions, which contain the actual reason.
  The tests that need a working plugin are then skipped.

## Adding tests

The tests live in `harness/src/main/java/com/entropire/simplefactions/testharness/`:

- `suites/`: one class per feature (`FactionLifecycleSuite`, `MembershipSuite`, ...). These are
  the **happy-path flow tests**; what must not be possible is tested in the plugin's own unit
  tests in `src/test`.
- `framework/`: the runner and the helpers the suites use:
  - `SimpleFactionsPlugin.joinedPlayer("Alice")` makes a `FakePlayer` and fires a real
    `PlayerJoinEvent` for it, so the plugin registers the player.
  - `SimpleFactionsPlugin.run(alice, "create red")` runs `/f create red` as that player;
    `alice.allText()` is everything the player was sent.
  - `factionOf(player)`, `addMember(faction, player)`, `query(...)` and `execute(...)` read and
    write the plugin's database directly, for checks and for setup the commands can't do yet.
  - `Check.contains(...)`, `Check.equal(...)` and friends fail the test with a clear message.

To add a test, add a method to a suite and list it in its `tests()`. For a new feature, add a
new suite and register it for its phase in `SUITES_PER_PHASE` in `TestHarnessPlugin`. To pass
data to the next boot, use `PhaseData.save(...)` and `PhaseData.load()`. Give factions and players unique
names (`uniqueFactionName("red")`, `joinedPlayer` does this itself), because all tests share one
server and one database. The tests run on the server thread, inside a real server, so they can
use the real Bukkit API.

Keep in mind that the harness is compiled against the **Spigot 1.17.1 API** on purpose, so
it works on every server in the list. Paper-only APIs need reflection (see `Scheduling` and
`AdventureText` for examples).

## Running a test server by hand

To look around on a test server, start one container yourself with the same settings and
`SF_TEST_SHUTDOWN=false`, so the harness doesn't stop the server after the tests. For example:

```
docker run -it --rm -p 25565:25565 -e EULA=TRUE -e TYPE=PAPER -e VERSION=1.21.4 ^
  -e ONLINE_MODE=FALSE -e SF_TEST_SHUTDOWN=false ^
  -v "%cd%\integration-tests\.cache\plugins-under-test:/plugins:ro" itzg/minecraft-server:java21
```

(On macOS/Linux, replace `^` with `\` and `%cd%` with `$(pwd)`.)

## Cleaning up

- Delete `integration-tests/.cache` to start completely fresh. Delete only
  `.cache/buildtools-jars/<file>` to rebuild one Spigot or CraftBukkit version.
- If a run was killed hard and left containers behind, remove them with
  `docker rm -f $(docker ps -aq --filter name=sf-it-)` (or by hand in Docker Desktop).
- On Linux, if cleanup fails with a permissions error, delete `.cache/servers` once with sudo.

## What these tests don't cover yet

- **Real players.** Fake players run commands and fire join/quit events, but they never connect.
  They aren't in `Bukkit.getPlayer(...)`, so "notify an online member" is covered by the unit
  tests instead. GUIs, real chat and anything packet-based need a bot client such as Mineflayer.
  Bot clients often lag behind new Minecraft versions, so check that one supports 26.x first.
- **Folia's region threads.** On Folia the tests run on the global region thread. Commands typed
  by a player run on that player's region thread, which only bot-based tests can exercise.
