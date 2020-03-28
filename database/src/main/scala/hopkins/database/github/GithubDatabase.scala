package hopkins.database.github

import java.time.LocalDate

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ActorRef, Scheduler}
import akka.stream.typed.scaladsl.ActorSink
import akka.stream.{ActorAttributes, Materializer, Supervision}
import akka.util.Timeout
import hopkins.covid.model.{Country, CountryStats, DetectedCountries}
import hopkins.database.github.CountryStatsAggregation.Protocol
import hopkins.database.github.CountryStatsAggregation.Protocol._
import hopkins.database.github.Downloader.ProvinceRow

import scala.concurrent.Future


object GithubDatabase {

  def start(implicit actorContext: ActorContext[_]): ActorRef[CountryStatsAggregation.Protocol] = {
    implicit val materializer: Materializer = Materializer(actorContext)
    val worldStatsAggregation: ActorRef[CountryStatsAggregation.Protocol] =
      actorContext.spawn(WorldStatsAggregation.behaviour(), "WorldStats")

    reload(worldStatsAggregation, actorContext.system.toClassic)

    worldStatsAggregation

  }

  def reload(
              worldStatsAggregation: ActorRef[CountryStatsAggregation.Protocol],
              actorSystem: ActorSystem)(implicit materializer: Materializer) = {
    val sink = ActorSink.actorRefWithBackpressure[ProvinceRow, Protocol, Ack](
      worldStatsAggregation,
      (ackingActor, row) => AddCountryStats(row, ackingActor),
      Start.apply,
      Ack,
      Completed,
      t => Failure(t)
    ).withAttributes(ActorAttributes.supervisionStrategy(decider))

    Downloader.gatherStats(actorSystem)
      .runWith(sink)
  }

  private val decider: Supervision.Decider = {
    case _: RuntimeException => Supervision.Resume
    case _                      => Supervision.Stop
  }

  object api {
    import akka.actor.typed.scaladsl.AskPattern._
    implicit final class CountryStatsAggregationOps(actorRef: ActorRef[CountryStatsAggregation.Protocol]) {
      def getData(country: Country, from: LocalDate, to: LocalDate)
                 (implicit timeout: Timeout, scheduler: Scheduler): Future[Either[Unit, Map[LocalDate, CountryStats]]] =
        actorRef ? (replyTo => GetCountryStats(country, from, to, replyTo))

      def getAllCountries(implicit timeout: Timeout, scheduler: Scheduler): Future[Either[Unit, DetectedCountries]] =
        actorRef ? (GetAllCountries.apply)

    }
  }
}
