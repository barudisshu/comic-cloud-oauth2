package io.comiccloud.rest

import akka.Done
import akka.actor._
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import io.comiccloud.config.{Config => HttpConfig}

import scala.concurrent.Future

class Server(boot: Bootstrap, service: String) extends HttpConfig {

  import akka.http.scaladsl.server.Directives._

  implicit val system: ActorSystem = ActorSystem()
  implicit val mater: ActorMaterializer = ActorMaterializer()

  import system.dispatcher


  //Boot up each service module from the config and get the routes
  val routes: List[Route] = boot.bootup(system).map(_.routes)
  val definedRoutes: Route = routes.reduce(_ ~ _)

  val finalRoutes: Route = pathPrefix("api")(definedRoutes)

  val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
    Http().bind(interface = httpInterface, port = httpPort)


  val log = Logging(system.eventStream, "Server")

  log.info("Starting up on port {} and ip {}", httpPort, httpInterface)

  val sink: Sink[Http.IncomingConnection, Future[Done]] = Sink.foreach[Http.IncomingConnection](_.handleWith(finalRoutes))

  serverSource.to(sink).run
}
