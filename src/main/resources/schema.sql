-- Applied on every start (spring.sql.init.mode=always), so every statement must be idempotent.
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(50) NOT NULL UNIQUE,
    mmr INTEGER,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    tag VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    is_account_non_expired BOOLEAN NOT NULL,
    is_account_non_locked BOOLEAN NOT NULL,
    is_credentials_non_expired BOOLEAN NOT NULL,
    is_enabled BOOLEAN NOT NULL
);

-- Profile settings (/me).
ALTER TABLE users ADD COLUMN IF NOT EXISTS first_name VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_name VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS birthday DATE;
-- JSON array of profile links (URLs).
ALTER TABLE users ADD COLUMN IF NOT EXISTS links TEXT;
-- Key of a preset profile background (see ProfileBackgrounds).
ALTER TABLE users ADD COLUMN IF NOT EXISTS background VARCHAR(32);

-- Per-game player analytics, written by bmatch-analytics (same definition as there); read for profiles.
CREATE TABLE IF NOT EXISTS game_analytics (
    game_uuid UUID NOT NULL,
    player_id INTEGER NOT NULL,
    username VARCHAR(50),
    metrics JSONB NOT NULL,
    performance REAL,
    mmr_before INTEGER,
    mmr_delta INTEGER NOT NULL,
    rated BOOLEAN NOT NULL,
    model_version SMALLINT NOT NULL,
    computed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    applied_at TIMESTAMPTZ,
    PRIMARY KEY (game_uuid, player_id)
);
