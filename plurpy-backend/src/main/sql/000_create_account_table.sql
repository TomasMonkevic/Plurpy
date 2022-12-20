CREATE TABLE account (
  id UUID PRIMARY KEY,
  date_created TIMESTAMP NOT NULL,
  date_updated TIMESTAMP NOT NULL,
  revision  INT NOT NULL,
  name  CHAR(25) NOT NULL,
  password_hash CHAR(60) NOT NULL
);