DROP KEYSPACE IF EXISTS comic;
CREATE KEYSPACE comic WITH REPLICATION = { 'class' : 'org.apache.cassandra.locator.NetworkTopologyStrategy', 'datacenter1': '1' } AND DURABLE_WRITES = true;

CREATE TABLE comic.account
(
    id         timeuuid,
    username   text,
    "password" text,
    email      text,
    phone      text,
    salt       text,
    created_at timestamp,
    PRIMARY KEY ((id, username))
);

CREATE TABLE comic.client
(
    appid        timeuuid,
    owner_id     timeuuid,
    appkey       timeuuid,
    grant_type   text,
    redirect_uri text,
    created_at   timestamp,
    PRIMARY KEY ((appid, owner_id))
);

CREATE TABLE comic.code
(
    account_id   timeuuid,
    appid        timeuuid,
    code         text,
    redirect_uri text,
    created_at   timestamp,
    PRIMARY KEY (account_id, appid)
);


CREATE TABLE comic.access_token
(
    account_id    timeuuid,
    appid         timeuuid,
    access_token  text,
    refresh_token text,
    created_at    timestamp,
    PRIMARY KEY ((account_id, appid), access_token, refresh_token)
) with clustering order by (access_token desc, refresh_token desc);
