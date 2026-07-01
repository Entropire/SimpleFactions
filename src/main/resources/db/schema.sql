PRAGMA foreign_keys = ON;

-- =============================================================================
-- FACTIONS TABLE
-- =============================================================================
CREATE TABLE IF NOT EXISTS factions (
    uuid TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    color TEXT NOT NULL,
    owner_uuid TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active INTEGER DEFAULT 1,
    last_activity TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_factions_name ON factions(name);
CREATE INDEX IF NOT EXISTS idx_factions_owner ON factions(owner_uuid);
CREATE INDEX IF NOT EXISTS idx_factions_active ON factions(is_active);

-- =============================================================================
-- PLAYERS TABLE
-- =============================================================================
CREATE TABLE IF NOT EXISTS players (
    uuid TEXT PRIMARY KEY,
    username TEXT,
    chat_mode TEXT CHECK(chat_mode IN ('PUBLIC', 'FACTION')) DEFAULT 'PUBLIC',
    last_seen TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_players_username ON players(username);
CREATE INDEX IF NOT EXISTS idx_players_last_seen ON players(last_seen);

-- =============================================================================
-- FACTION MEMBERSHIPS TABLE
-- =============================================================================
CREATE TABLE IF NOT EXISTS faction_memberships (
    player_uuid TEXT NOT NULL,
    faction_uuid TEXT NOT NULL,
    role TEXT CHECK(role IN ('OWNER', 'MEMBER')) NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (player_uuid, faction_uuid),

    FOREIGN KEY (player_uuid) REFERENCES players(uuid)
    ON DELETE CASCADE,

    FOREIGN KEY (faction_uuid) REFERENCES factions(uuid)
    ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_memberships_faction ON faction_memberships(faction_uuid);
CREATE INDEX IF NOT EXISTS idx_memberships_role ON faction_memberships(role);

-- =============================================================================
-- JOIN REQUESTS TABLE
-- =============================================================================
CREATE TABLE IF NOT EXISTS join_requests (
    uuid TEXT PRIMARY KEY,
    player_uuid TEXT NOT NULL,
    faction_uuid TEXT NOT NULL,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,

    UNIQUE (player_uuid, faction_uuid),

    FOREIGN KEY (player_uuid) REFERENCES players(uuid)
    ON DELETE CASCADE,

    FOREIGN KEY (faction_uuid) REFERENCES factions(uuid)
    ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_join_requests_player ON join_requests(player_uuid);
CREATE INDEX IF NOT EXISTS idx_join_requests_faction ON join_requests(faction_uuid);
CREATE INDEX IF NOT EXISTS idx_join_requests_expires ON join_requests(expires_at);

-- =============================================================================
-- FACTION INVITES TABLE
-- =============================================================================
CREATE TABLE IF NOT EXISTS faction_invites (
    uuid TEXT PRIMARY KEY,
    faction_uuid TEXT NOT NULL,
    player_uuid TEXT NOT NULL,
    invited_by TEXT NOT NULL,
    invited_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,

    UNIQUE (faction_uuid, player_uuid),

    FOREIGN KEY (faction_uuid) REFERENCES factions(uuid)
    ON DELETE CASCADE,

    FOREIGN KEY (player_uuid) REFERENCES players(uuid)
    ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_invites_faction ON faction_invites(faction_uuid);
CREATE INDEX IF NOT EXISTS idx_invites_player ON faction_invites(player_uuid);
CREATE INDEX IF NOT EXISTS idx_invites_expires ON faction_invites(expires_at);