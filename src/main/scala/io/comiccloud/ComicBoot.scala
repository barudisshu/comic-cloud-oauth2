package io.comiccloud

import akka.actor.ActorSystem
import io.comiccloud.config.DbConfiguration
import io.comiccloud.repository._
import io.comiccloud.rest._
import io.comiccloud.service.accounts._
import io.comiccloud.service.clients._
import io.comiccloud.service.codes._
import io.comiccloud.service.tokens.{TokenAssociate, TokenRouters}

class ComicBoot extends Bootstrap with DbConfiguration {
  override def bootup(system: ActorSystem): List[BasicRoutesDefinition] = {
    import system.dispatcher

    val accountRepo = new AccountsRepository(config)
    val clientRepo = new ClientsRepository(config)

    val accountRef = system.actorOf(AccountAssociate.props(accountRepo), AccountAssociate.Name)
    val clientRef = system.actorOf(ClientAssociate.props(accountRepo, clientRepo), ClientAssociate.Name)
    val codeRef = system.actorOf(CodeAssociate.props(accountRepo, clientRepo), CodeAssociate.Name)
    val tokenRef = system.actorOf(TokenAssociate.props(accountRepo, clientRepo), TokenAssociate.Name)

    List(new AccountRouters(accountRef), new ClientRouters(clientRef), new CodeRouters(codeRef), new TokenRouters(tokenRef))
  }
}