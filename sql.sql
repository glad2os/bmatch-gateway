CREATE TABLE IF NOT EXISTS users (
                                     id SERIAL PRIMARY KEY,
                                     username VARCHAR(50) NOT NULL,
                                     email VARCHAR(50) NOT NULL,
                                     password VARCHAR(255) NOT NULL,
                                     role VARCHAR(50) NOT NULL,
                                     tag VARCHAR(50),
                                     status VARCHAR(50) NOT NULL,
                                     is_account_non_expired BOOLEAN NOT NULL,
                                     is_account_non_locked BOOLEAN NOT NULL,
                                     is_credentials_non_expired BOOLEAN NOT NULL,
                                     is_enabled BOOLEAN NOT NULL
);

