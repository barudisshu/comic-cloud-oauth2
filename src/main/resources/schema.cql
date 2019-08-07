DROP KEYSPACE IF EXISTS comic;
CREATE KEYSPACE comic WITH REPLICATION = { 'class' : 'org.apache.cassandra.locator.NetworkTopologyStrategy', 'datacenter1': '1' } AND DURABLE_WRITES = true;

CREATE TABLE comic.account
(
    id         timeuuid,
    created_at timestamp,
    email      text,
    "password" text,
    phone      text,
    salt       text,
    username   text,
    PRIMARY KEY (id)
);

CREATE TABLE comic.account_by_username
(
    username   text,
    id         timeuuid,
    created_at timestamp,
    email      text,
    "password" text,
    phone      text,
    salt       text,
    PRIMARY KEY (username, id)
) WITH CLUSTERING ORDER BY (id DESC);

CREATE TABLE comic.client
(
    appid        timeuuid,
    owner_id     timeuuid,
    appkey       timeuuid,
    created_at   timestamp,
    grant_type   text,
    redirect_uri text,
    PRIMARY KEY (appid, owner_id)
) WITH CLUSTERING ORDER BY (owner_id DESC);

CREATE TABLE comic.code
(
    code         text,
    account_id   timeuuid,
    appid        timeuuid,
    created_at   timestamp,
    redirect_uri text,
    PRIMARY KEY (code, account_id, appid)
) WITH CLUSTERING ORDER BY (account_id DESC, appid DESC);

CREATE TABLE comic.token_by_access
(
    access_token  text,
    account_id    timeuuid,
    refresh_token text,
    appid         timeuuid,
    created_at    timestamp,
    expired_at    timestamp,
    PRIMARY KEY (access_token, account_id, refresh_token)
) WITH CLUSTERING ORDER BY (account_id DESC, refresh_token DESC);

CREATE TABLE comic.token_by_refresh
(
    refresh_token text,
    account_id    timeuuid,
    access_token  text,
    appid         timeuuid,
    created_at    timestamp,
    expired_at    timestamp,
    PRIMARY KEY (refresh_token, account_id, access_token)
) WITH CLUSTERING ORDER BY (account_id DESC, access_token DESC);
