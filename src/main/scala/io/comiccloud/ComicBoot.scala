package io.comiccloud

import akka.actor.ActorSystem
import io.comiccloud.rest._
import io.comiccloud.service.accounts.{AccountAssociate, AccountRouters}

class ComicBoot extends Bootstrap {
  override def bootup(system: ActorSystem): List[BasicRoutesDefinition] = {
    import system.dispatcher

    val accountRef = system.actorOf(AccountAssociate.props(), AccountAssociate.Name)
    List(new AccountRouters(accountRef))
  }
}
