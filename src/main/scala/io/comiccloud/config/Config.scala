package io.comiccloud.config

import com.typesafe.config.{Config => AkkaConfig, ConfigFactory => AkkaConfigFactory}

trait Config {

  protected lazy val config: AkkaConfig = AkkaConfigFactory.load()


  lazy val httpInterface: String = config.getString("service.http.interface")
  lazy val httpPort: Int = config.getInt("service.http.port")

  lazy val accessTokenLifeInSecond: Int = config.getInt("service.authentication.access-token-life-in-seconds")

}
