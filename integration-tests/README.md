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

```
gradlew integrationTest                                  # every server in servers.csv
gradlew integrationTest -Pservers=paper                  # only Paper servers
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
- The HTML report groups the results per server (for example `paper-1.21.4`):
  `integration-tests/runner/build/reports/tests/integrationTest/index.html`
- Each server's full log: `integration-tests/runner/build/server-logs/<server>.log`
- BuildTools logs: `integration-tests/runner/build/server-logs/buildtools-<version>.log`

Every server reports:

- **server starts and runs the test harness**: fails if the server couldn't be downloaded,
  crashed, or the harness never reported back. The failure shows the end of the server log.
- **environment: ...**: the exact server software, version and Java version that ran, so
  you can see it really tested what you think it tested.
- One result per in-server test. When **plugin loads and enables** fails, the failure
  includes the server log lines about SimpleFactions, which contain the actual reason.
  The tests that need a working plugin are then skipped.

## Adding tests

The tests live in
`harness/src/main/java/com/entropire/simplefactions/testharness/InServerTests.java`.
Add a method that throws on failure (use `Check.that(...)`) and register it in `runAll()`.
They run on the server thread, inside a real server, so they can use the real Bukkit API.

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

- **Real players.** All tests use the console or a fake non-player sender. Anything that needs a
  player (creating a faction, kicking a member, join/quit events) needs a bot client such as
  Mineflayer that joins the server. Bot clients often lag behind new Minecraft versions, so
  check that one supports 26.x before relying on it.
- **Folia's region threads.** On Folia the tests run on the global region thread. Commands typed
  by a player run on that player's region thread, which only bot-based tests can exercise.
