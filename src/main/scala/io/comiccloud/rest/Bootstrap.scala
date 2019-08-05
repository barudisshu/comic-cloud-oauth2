package io.comiccloud.rest

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}

trait Bootstrap {

  def bootup(system: ActorSystem): List[BasicRoutesDefinition]

  def startSingleton(system: ActorSystem,
                     props: Props,
                     managerName: String,
                     terminationMessage: Any = PoisonPill): ActorRef = {

    system.actorOf(
      ClusterSingletonManager.props(singletonProps = props,
                                    terminationMessage = terminationMessage,
                                    settings = ClusterSingletonManagerSettings(system)),
      managerName
    )
  }
}
