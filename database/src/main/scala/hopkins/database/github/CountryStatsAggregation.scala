package hopkins.database.github

import java.time.LocalDate

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import cats.Monoid
import hopkins.covid.model.{CountryStats}
import cats.implicits._
import hopkins.database.github.Downloader.ProvinceRow
import cats.derived.auto._

object CountryStatsAggregation {

  implicit val monoid: Monoid[Map[LocalDate, CountryStats]] = implicitly

  def behaviour(timeSeries: Map[LocalDate, CountryStats]): Behavior[Protocol] =
    Behaviors.receiveMessage {
      case Protocol.AddCountryStats(row, replyTo) =>
        replyTo ! Protocol.Ack
        behaviour(timeSeries |+| toTimeSeries(row))
    }

  def toTimeSeries(row: ProvinceRow): Map[LocalDate, CountryStats] =
    Map(
      row.provinceStats.lastUpdate.toLocalDate ->
      CountryStats(
        Map(
          row.province -> row.provinceStats
        )
      )
    )

  sealed trait Protocol
  object Protocol {
    sealed trait Ack
    case object Ack extends Ack
    case class AddCountryStats(countryStats: ProvinceRow, replyTo: ActorRef[Ack]) extends Protocol
    case object Completed extends Protocol
    case class Start(replyTo: ActorRef[Ack]) extends Protocol
  }
}