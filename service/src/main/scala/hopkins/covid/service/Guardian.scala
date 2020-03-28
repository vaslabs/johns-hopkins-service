package hopkins.covid.service

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{Behavior, PostStop}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import akka.util.Timeout
import hopkins.database.github.GithubDatabase

import scala.concurrent.duration._
object Guardian {
  sealed trait GuardianProtocol

  def behavior: Behavior[GuardianProtocol] = Behaviors.setup {
    ctx =>
      implicit val actorSystem = ctx.system.toClassic
      val worldStats = GithubDatabase.start(ctx)
      val routes =
        Routes.countryStats(worldStats)(Timeout(3 seconds), ctx.system.scheduler) ~
          Routes.allCountries(worldStats)(Timeout(2 seconds), ctx.system.scheduler) ~
          Routes.health ~
          Routes.docs
      implicit val materializer = Materializer(ctx)
      val server = Http().bindAndHandle(routes, "0.0.0.0", 8080)
      Behaviors.receiveMessage[GuardianProtocol] (
        _ => Behaviors.ignore
      ).receiveSignal {
        case (ctx, PostStop) =>
          import ctx.executionContext
          server.flatMap(_.terminate(15 seconds))
          Behaviors.same
      }
  }

}
