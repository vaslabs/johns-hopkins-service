package hopkins.covid.service

import akka.actor.typed.Scheduler
import akka.actor.typed.ActorRef
import akka.util.Timeout
import hopkins.covid.endpoints.Stats
import hopkins.database.github.CountryStatsAggregation
import sttp.tapir.server.akkahttp._
import sttp.tapir.docs.openapi._
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.swagger.akkahttp.SwaggerAkka

import scala.concurrent.Future
object Routes {
  import hopkins.database.github.GithubDatabase.api._

  def countryStats(worldStats: ActorRef[CountryStatsAggregation.Protocol])(implicit
                     timeout: Timeout, scheduler: Scheduler) =
    Stats.country.toRoute {
      case (country, from, to) =>
        worldStats.getData(country, from , to)
    }

  def allCountries(worldStats: ActorRef[CountryStatsAggregation.Protocol])
                  (implicit
                   timeout: Timeout, scheduler: Scheduler) =
    Stats.allCountries.toRoute {
      _ => worldStats.getAllCountries
    }

  def health = Stats.health.toRoute {
    _ => Future.successful(Right(()))
  }

  val openApi = List(Stats.country, Stats.allCountries).toOpenAPI("Hopkins daily stats", "v1.0")

  val docs = new SwaggerAkka(openApi.toYaml).routes

}
