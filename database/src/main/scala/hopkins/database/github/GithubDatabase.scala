package hopkins.database.github

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.adapter._
import akka.stream.Materializer
import akka.stream.typed.scaladsl.ActorSink
import hopkins.database.github.CountryStatsAggregation.Protocol
import hopkins.database.github.CountryStatsAggregation.Protocol._
import hopkins.database.github.Downloader.ProvinceRow


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
}
