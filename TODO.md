# SimpleFactions TODO

Compared against the old version (https://github.com/Entropire/SimpleFactions_Old) on 2026-11-23.
Everything in "BackLog → Parity with old version" is needed before the new version can do everything the old one could.

## BackLog

### Parity with old version: commands
- Implement members command with pages (`/f members <FactionName> [page]`)
- Implement invite command (`/f invite <PlayerName>`, tab-complete online players without a faction)
- Implement join request command (old: `/f join <FactionName>`, new: `/f request <FactionName>`, tab-complete faction names)
- Implement accept command for invites and join requests
- Implement decline command for invites and join requests (notify the other side)
- Implement expiry of invites/join requests (old: 30 seconds; use `expires_at` + a cleanup task)
- Implement leave command (owner cannot leave)
- Implement kick command (owner only, cannot kick self, notify kicked player if online, tab-complete members)
- Implement modify command group (`/f modify name|color|owner`)
- Implement modify name command (same validation as create)
- Implement modify color command (tab-complete colors; decide between old named colors and new hex colors)
- Implement modify owner command (tab-complete members, update membership roles + factions.owner_uuid)
- Make `/f owner` without a faction name show the owner of your own faction
- Make `/f` without arguments open the GUI (falls back to help when the GUI is disabled)

### Parity with old version: faction rules
- Validate faction names on create (old: letters only, stored lowercase)
- Add banned faction names list (old: `bannedNames.yml`; README: `blacklist.yml`) with a setting to turn it on/off

### Parity with old version: chat & display
- Implement custom chat system (public chat shows `[Faction] Player: message` in faction color)
- Implement faction chat (message only sent to online faction members)
- Implement default chat command (old: `/chat <public|faction>`, README: `/f chat <public|faction>`)
- Implement quick chat chosing with prefix (old: `/cp` and `/cf` shortcuts)
- Show faction prefix in display name, tab list and join message (on join, create, join faction, rename, recolor)
- Remove faction prefix when a player leaves, is kicked or the faction is deleted

### Parity with old version: GUI
- Implement GUI framework (button/click handling)
- Implement main GUI for players without a faction (Create / Join)
- Implement create faction GUI (old used AnvilGUI for name input)
- Implement faction list GUI with pages, and faction info GUI with "request to join"
- Implement own-faction GUI (name, color, owner, members, invite, delete/leave)
- Implement change name GUI and change color GUI (owner only)
- Implement members list GUI with pages, and member info GUI (promote / kick)
- Implement invite player list GUI with pages, and invite confirm GUI

### Parity with old version: configuration
- Implement config file (old `settings.yml`: `enable-gui`, `enable-bannedNames`)

### Bugs found in current code
- `/f owner <unknown name>` crashes: `getOwner` returns `Success(null)` and `OwnerCommand` calls `owner.username()` → return a `Failure("There is no faction named X")` instead
- `JoinRequestRepository.save` calls `setString(2, ...)` twice and never sets parameter 3 → every join request insert fails (second one should be `setString(3, factionUuid)`)
- `ListCommand` / help command crash on a non-number page argument (`Integer.parseInt`)
- Help command pagination: `maxPage` uses `size / 2` but shows 10 per page, and `subList` end is clamped to `size - 1` so the last command is never shown
- `schema.sql` changes don't reach existing databases (`CREATE TABLE IF NOT EXISTS` skips existing tables) → delete the dev `Simple-Faction.db` after schema changes, or add migrations before release
- `FactionPlayerRepository.saveIfNotExists` uses `ON CONFLICT(uuid) DO NOTHING`, so a player who changes their Minecraft name keeps the old `username` → use `DO UPDATE SET username = excluded.username`
- `FactionRepository.save` checks for a unique-constraint error on `factions.owner_uuid`, but the schema has no such constraint → add the constraint or remove the check
- `gradle/wrapper/gradle-wrapper.jar` is missing, so `./gradlew` does not work (regenerate with `gradle wrapper`)

### New features (in README, not in old version)
- Implement info command (`/f info <FactionName>`)
- Implement reload command (`/f reload`)
- Implement `max-members-per-faction` setting
- Implement admin interface
- Implement developer API
- Implement chasing for Faction table
- Implement cashing for Player table
- Implement cashing for FactionMembership table
- Implement cashing for FactionInvite table
- Implement cashing for JoinRequist table

## Doing
- Implement members command

## Done
- Create database structure
- Implement Faction object, repository and service
- Implement Player object, repository and service
- Implement FactionMembership object, repository and service
- Implement FactionInvite object, repository and service
- Implement JoinRequist object, repository and service
- Implement command structure
- Implement help command
- Implement create command
- Implement delete command
- Implement list command
- Implement owner command
- Implement chat formating structure
- Update help command to use new chat formating structure
- Update list command to use new chat formating structure
- Implement improved help command (where when you put help after a faction command it explains the command and its children)
- Implement tab completion for Owner command
- Register players and track last seen on join/leave
- Rename master command to `/faction` (`/f`)
- Fix `/f delete` crashing on offline members, comparing UUIDs with `==` and players without a faction
- `/f list` clamps the page number to the existing pages
- Refactor database code to return `Result` instead of throwing exceptions (repositories, services, `DataBaseContext`, `FactionApplication`, commands, `PlayerJoinListener`, `initSchema`; removed the custom exception classes and `objects/Error.java`)
- Plugin disables itself when the database schema can't be created
- Fix `getFactionNames` SQL (`name contains ?` → `name LIKE ? || '%'`) so owner tab completion works
- Add `UNIQUE (player_uuid)` to `faction_memberships` so a player can only be in one faction

## Never Going To Implement
