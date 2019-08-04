drop keyspace if exists comic;
CREATE KEYSPACE IF NOT EXISTS comic WITH replication = {'class': 'NetworkTopologyStrategy', 'datacenter1': '1'} AND durable_writes = true;

use comic;

drop table if exists account;
create table account
(
    id         uuid,
    username   varchar,
    password   varchar,
    salt       varchar,
    email      varchar,
    phone      varchar,
    created_at date,
    primary key ( id, created_at )
) with clustering order by (created_at DESC)
      and caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
      and compaction = { 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy' };

drop table if exists client;
create table client
(
    id           uuid,
    owner_id     uuid,
    appid        varchar,
    appkey       varchar,
    redirect_uri varchar,
    grant_type   varchar,
    created_at   date,
    primary key ( id, created_at )
) with clustering order by (created_at DESC)
      and caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
      and compaction = { 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy' };

drop table if exists access_token;
create table access_token
(
    id            uuid,
    account_id    uuid,
    appid         varchar,
    access_token  varchar,
    refresh_token varchar,
    created_at    date,
    primary key ( id, created_at )
) with clustering order by (created_at DESC)
      and caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
      and compaction = { 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy' };

drop table if exists authorization_code;
create table authorization_code
(
    id           uuid,
    account_id   uuid,
    appid        varchar,
    code         varchar,
    redirect_uri varchar,
    created_at   date,
    primary key ( id, created_at )
) with clustering order by (created_at DESC)
      and caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
      and compaction = { 'class': 'org.apache.cassandra.db.compaction.LeveledCompactionStrategy' };


