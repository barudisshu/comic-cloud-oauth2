package io.comiccloud.util

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.Cluster
import akka.cluster.ddata.{DistributedData, LWWMap, LWWMapKey}

object ReplicatedCache {

  def props: Props = Props[ReplicatedCache]

  private final case class Request(key: String, replyTo: ActorRef)

  final case class PutInCache(key: String, value: Any)
  final case class GetFromCache(key: String)
  final case class Cached(key: String, value: Option[Any])
  final case class Evict(key: String)
}

class ReplicatedCache extends Actor {
  import ReplicatedCache._
  import akka.cluster.ddata.Replicator._

  val replicator: ActorRef      = DistributedData(context.system).replicator
  implicit val cluster: Cluster = Cluster(context.system)

  def dataKey(entryKey: String): LWWMapKey[String, Any] =
    LWWMapKey("cache-" + math.abs(entryKey.hashCode) % 100)

  def receive: Receive = {
    case PutInCache(key, value) =>
      replicator ! Update(dataKey(key), LWWMap(), WriteLocal)(_ + (key -> value))
    case Evict(key) =>
      replicator ! Update(dataKey(key), LWWMap(), WriteLocal)(_ - key)
    case GetFromCache(key) =>
      replicator ! Get(dataKey(key), ReadLocal, Some(Request(key, sender())))
    case g @ GetSuccess(LWWMapKey(_), Some(Request(key, replyTo))) =>
      g.dataValue match {
        case data: LWWMap[_, _] =>
          data.asInstanceOf[LWWMap[String, Any]].get(key) match {
            case Some(value) => replyTo ! Cached(key, Some(value))
            case None        => replyTo ! Cached(key, None)
          }
      }
    case NotFound(_, Some(Request(key, replyTo))) =>
      replyTo ! Cached(key, None)
    case _: UpdateResponse[_] => // ok
  }

}
