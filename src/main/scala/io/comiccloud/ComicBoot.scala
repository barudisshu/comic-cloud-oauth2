package io.comiccloud

import akka.actor.ActorSystem
import io.comiccloud.config.DbConfiguration
import io.comiccloud.repository._
import io.comiccloud.rest._
import io.comiccloud.service.accounts._
import io.comiccloud.service.clients._

class ComicBoot extends Bootstrap with DbConfiguration {
  override def bootup(system: ActorSystem): List[BasicRoutesDefinition] = {
    import system.dispatcher

    val accountRepo = new AccountsRepository(config)
    val accountRef = system.actorOf(AccountAssociate.props(accountRepo), AccountAssociate.Name)

    val clientRepo = new ClientsRepository(config)
    val clientRef = system.actorOf(ClientAssociate.props(clientRepo, accountRepo), ClientAssociate.Name)

    List(new AccountRouters(accountRef), new ClientRouters(clientRef))
  }
}