package hopkins.database.github

import java.time.LocalDate

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import cats.implicits._
import cats.kernel.Semigroup
import hopkins.covid.model.{Country, CountryStats, DetectedCountries}
import hopkins.database.github.Downloader.ProvinceRow

object CountryStatsAggregation {


  implicit val semigroupCountryStats: Semigroup[CountryStats] = (x: CountryStats, y: CountryStats) =>
    CountryStats(x.provinceStats |+| y.provinceStats)


  def behaviour(timeSeries: Map[LocalDate, CountryStats]): Behavior[Protocol] = Behaviors.setup { _ =>
    Behaviors.receiveMessage {
      case Protocol.AddCountryStats(row, replyTo) =>
        val newTimeSeries = toTimeSeries(row)
        replyTo ! Protocol.Ack
        behaviour(timeSeries |+| newTimeSeries)
      case Protocol.GetCountryStats(_, from, to, replyTo) =>
        val stats = timeSeries.view.filterKeys(d =>
          d.compareTo(from) >= 0 && d.compareTo(to) <= 0
        )

        replyTo ! Right(stats.toMap)
        Behaviors.same
      case _ =>
        Behaviors.same
    }
  }

  def toTimeSeries(row: ProvinceRow): Map[LocalDate, CountryStats] =
    Map(
      row.provinceStats.lastUpdate.toLocalDate -> CountryStats(
        Map(
          row.province -> Set(row.provinceStats)
        )
      )
    )

  sealed trait Protocol
  object Protocol {
    sealed trait Ack
    case object Ack extends Ack
    case class AddCountryStats(countryStats: ProvinceRow, replyTo: ActorRef[Ack]) extends Protocol
    case object Completed extends Protocol
    case class Failure(throwable: Throwable) extends Protocol
    case class Start(replyTo: ActorRef[Ack]) extends Protocol

    sealed trait Query extends Protocol
    case class GetCountryStats(
      country: Country,
      from: LocalDate,
      to: LocalDate,
      replyTo: ActorRef[Either[Unit, Map[LocalDate, CountryStats]]]
    ) extends Query

    case class GetAllCountries(replyTo: ActorRef[Either[Unit, DetectedCountries]]) extends Query

  }

}
