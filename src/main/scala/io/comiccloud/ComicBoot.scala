package io.comiccloud

import akka.actor.ActorSystem
import io.comiccloud.rest._
import io.comiccloud.service.accounts.{AccountAssociate, AccountRouters}
import io.comiccloud.service.clients.{ClientAssociate, ClientRouters}
import io.comiccloud.service.codes.{CodeAssociate, CodeRouters}
import io.comiccloud.service.resources.{ResourceAssociate, ResourceRouters}
import io.comiccloud.service.tokens.{TokenAssociate, TokenRouters}

class ComicBoot extends Bootstrap {
  override def bootup(system: ActorSystem): List[BasicRoutesDefinition] = {
    import system.dispatcher

    val accountRef  = system.actorOf(AccountAssociate.props(), AccountAssociate.Name)
    val clientRef   = system.actorOf(ClientAssociate.props(), ClientAssociate.Name)
    val codeRef     = system.actorOf(CodeAssociate.props(), CodeAssociate.Name)
    val tokenRef    = system.actorOf(TokenAssociate.props(), TokenAssociate.Name)
    val resourceRef = system.actorOf(ResourceAssociate.props(), ResourceAssociate.Name)

    List(new AccountRouters(accountRef),
         new ClientRouters(clientRef),
         new CodeRouters(codeRef),
         new TokenRouters(tokenRef),
         new ResourceRouters(resourceRef))
  }
}
