# SimpleFactions TODO

Compared against the old version (https://github.com/Entropire/SimpleFactions_Old) on 2026-11-23.
Everything in "BackLog → Parity with old version" is needed before the new version can do everything the old one could.

## BackLog

### Parity with old version: commands
- Implement invite command (`/f invite <PlayerName>`, tab-complete online players without a faction)
- Implement join request command (old: `/f join <FactionName>`, new: `/f request <FactionName>`, tab-complete faction names)
- Implement accept command for invites and join requests
- Implement decline command for invites and join requests (notify the other side)
- Implement expiry of invites/join requests (old: 30 seconds; use `expires_at` + a cleanup task)
- Implement kick command (owner only, cannot kick self, notify kicked player if online, tab-complete members)
- Implement modify command group (`/f modify name|color|owner`)
- Implement modify name command (same validation as create)
- Implement modify color command (tab-complete colors; decide between old named colors and new hex colors)
- Implement modify owner command (tab-complete members, update membership roles + factions.owner_uuid)
- Make `/f` without arguments open the GUI (falls back to help when the GUI is disabled)

### Parity with old version: faction rules
- Validate faction names on create (old: letters only; names are already case-insensitive)
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
- None known right now

### Database versions
- Bump `DataBaseContext.SCHEMA_VERSION` only for actual releases, when `schema.sql` changed in a way the previous release's database files can't use
- Between releases, delete the dev `Simple-Faction.db` after changing `schema.sql` (the version check won't catch it)
- Add migrations (schema version N → N+1) so upgrading the plugin keeps factions instead of needing `f database reset`

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
- Implement leave command (owner cannot leave)

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
- Fix help command pagination (last command was never shown, page numbers were 0-based) and crash on a non-number page
- Fix `/f list` crashing on a non-number page argument
- Fix `/f owner <unknown name>` crashing (`getOwner` now returns a `Failure` when no faction has that name)
- Fix `JoinRequestRepository.save` setting parameter 2 twice instead of parameter 3
- Update a player's `username` on join so name changes are picked up
- Add `UNIQUE (owner_uuid)` to `factions` so a player can only own one faction
- Track the schema version and plugin version in the database file (`plugin_meta` table, `DataBaseContext.SCHEMA_VERSION`)
- Incompatible database file: console warning with 2 options (use the plugin version that made it, or reset), plugin waits in safe mode
- Console command `f database reset [confirm]` that moves the old database to a backup and starts with an empty one
- Add unit tests for the database version check and reset
- Add the missing Gradle wrapper jar so `./gradlew` works
- Implement members command with pages (`/f members <faction name> [page]`, owner listed first, tab-complete faction names)
- Fix tab completion: `CommandNode.onTabComplete` now calls each command's `tabComplete` (owner and members completion work)
- Faction names are case-insensitive (`COLLATE NOCASE`): `/f members RED` finds "Red" and "Red"/"RED" can't both exist
- `/f members` shows the faction's real name (new `getByName` lookup and `FactionMembersPage`)
- Make `/f owner` without a faction name show the owner of your own faction
- Make `/f member` without a faction name show the members of your own faction

## Never Going To Implement
