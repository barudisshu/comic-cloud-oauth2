package io.comiccloud

import akka.actor.ActorSystem
import io.comiccloud.config.DbConfiguration
import io.comiccloud.repository.AccountsRepository
import io.comiccloud.rest.{BasicRoutesDefinition, Bootstrap}
import io.comiccloud.servcie.accounts.{AccountAssociate, AccountRouters}

class ComicBoot extends Bootstrap with DbConfiguration {
  override def bootup(system: ActorSystem): List[BasicRoutesDefinition] = {
    import system.dispatcher

    val accountRepo = new AccountsRepository(config)
    val accountRef = system.actorOf(AccountAssociate.props(accountRepo), AccountAssociate.Name)


    List(new AccountRouters(accountRef))
  }
}