CREATE TABLE IF NOT EXISTS public.users (
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
