package io.comiccloud.modeling.connector

import com.outworkers.phantom.connectors.{CassandraConnection, ContactPoints}
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

object Connector {
  private val config         : Config = ConfigFactory.load()
  private val cassandraConfig: Config = config.getConfig("cassandra")

  private val contactPoints    = cassandraConfig.getStringList("contact-points").asScala
  private val port    : Int    = cassandraConfig.getInt("port")
  private val keyspace: String = cassandraConfig.getString("key-space")

  /**
   * Create a connector with the ability to connects to multiple hosts in a cluster
   *
   * If you need to connect to a secure cluster, use:
   * {{{
   * ContactPoints(hosts)
   *   .withClusterBuilder(_.withCredentials(username, password))
   *   .keySpace(keyspace)
   * }}}
   *
   */
  lazy val connector: CassandraConnection = ContactPoints(contactPoints, port).keySpace(keyspace)
}
