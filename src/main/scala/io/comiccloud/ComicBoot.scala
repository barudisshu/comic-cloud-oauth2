package io.comiccloud

import akka.actor.ActorSystem
import io.comiccloud.config.DbConfiguration
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}
import io.comiccloud.rest.{BasicRoutesDefinition, Bootstrap}
import io.comiccloud.service.accounts.{AccountActor, AccountRouters}
import io.comiccloud.service.clients.{ClientActor, ClientRouters}
import io.comiccloud.service.codes.{CodeAssociate, CodeRouters}
import io.comiccloud.service.resources.{ResourceActor, ResourceRouters}
import io.comiccloud.service.tokens.{TokenActor, TokenRouters}

class ComicBoot extends Bootstrap with DbConfiguration {
  override def bootup(system: ActorSystem): List[BasicRoutesDefinition] = {
    import system.dispatcher

    val accountRepo = new AccountsRepository(config)
    val clientRepo  = new ClientsRepository(config)

    val accountRef  = system.actorOf(AccountActor.props(accountRepo), AccountActor.Name)
    val clientRef   = system.actorOf(ClientActor.props(clientRepo, accountRepo), ClientActor.Name)
    val codeRef     = system.actorOf(CodeAssociate.props(accountRepo, clientRepo), CodeAssociate.Name)
    val tokenRef    = system.actorOf(TokenActor.props(accountRepo, clientRepo, codeRef), TokenActor.Name)
    val resourceRef = system.actorOf(ResourceActor.props(), ResourceActor.Name)

    List(new AccountRouters(accountRef),
         new ClientRouters(clientRef),
         new CodeRouters(codeRef),
         new TokenRouters(tokenRef),
         new ResourceRouters(resourceRef))
  }
}
