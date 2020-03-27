package hopkins.database.github

import java.time.LocalDate

import akka.actor.typed.{ActorRef, Scheduler}
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.adapter._
import akka.stream.Materializer
import akka.stream.typed.scaladsl.ActorSink
import akka.util.Timeout
import hopkins.covid.model.{Country, CountryStats}
import hopkins.database.github.CountryStatsAggregation.Protocol
import hopkins.database.github.CountryStatsAggregation.Protocol._
import hopkins.database.github.Downloader.ProvinceRow

import scala.concurrent.Future


object GithubDatabase {

  def start(implicit actorContext: ActorContext[_]): ActorRef[CountryStatsAggregation.Protocol] = {
    implicit val materializer: Materializer = Materializer(actorContext)
    val worldStatsAggregation: ActorRef[CountryStatsAggregation.Protocol] =
      actorContext.spawn(WorldStatsAggregation.behaviour, "WorldStats")

    val sink = ActorSink.actorRefWithBackpressure[ProvinceRow, Protocol, Ack] (
      worldStatsAggregation,
      (ackingActor, row) => AddCountryStats(row, ackingActor),
      Start.apply,
      Ack,
      Completed,
      _ => Completed
    )

    Downloader.gatherStats(actorContext.system.toClassic)
      .runWith(sink)

    worldStatsAggregation

  }

  object api {
    import akka.actor.typed.scaladsl.AskPattern._
    implicit final class CountryStatsAggregationOps(actorRef: ActorRef[CountryStatsAggregation.Protocol]) {
      def getData(country: Country, from: LocalDate, to: LocalDate)
                 (implicit timeout: Timeout, scheduler: Scheduler): Future[List[CountryStats]] =
        actorRef ?  (replyTo => GetCountryStats(country, from, to, replyTo))
    }
  }
}
