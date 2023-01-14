CREATE TABLE IF NOT EXISTS account (
  id UUID PRIMARY KEY,
  password_hash CHAR(60) NOT NULL,
  date_created TIMESTAMP NOT NULL,
  date_updated TIMESTAMP NOT NULL,
  revision INT NOT NULL,
  account_info jsonb NOT NULL
);