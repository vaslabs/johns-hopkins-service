package hopkins.database.github

import java.net.URI
import java.time.{LocalDate, LocalDateTime}
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.scaladsl.Source
import akka.util.ByteString
import cats.effect.IO
import github4s.Github
import github4s.domain.Content
import io.circe.Encoder

import scala.concurrent.ExecutionContext
import scala.util.Try
import akka.http.scaladsl.model._
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import hopkins.covid.model._

object Downloader {

  implicit val contextShift = IO.contextShift(ExecutionContext.global)
  import ExecutionContext.Implicits._

  def gatherStats(implicit actorSystem: ActorSystem): Source[ProvinceRow, NotUsed] =
    Source.future(dailyReports.unsafeToFuture())
    .mapConcat(identity)
    .mapAsync(4) {
      downloadable =>
        println("downloading " + downloadable.uri)
        Http().singleRequest(HttpRequest(uri = downloadable.uri.toString))
    }.flatMapConcat {
      extractEntityData(_)
        .via(CsvParsing.lineScanner())
        .via(CsvToMap.toMapAsStrings())
        .map(toProvinceRow)
        .collect {
          case Some(o) => o
        }
    }



  private def extractEntityData(response: HttpResponse): Source[ByteString, _] =
    response match {
      case HttpResponse(StatusCodes.OK, _, entity, _) => entity.dataBytes
      case notOkResponse =>
        Source.failed(new RuntimeException(s"illegal response $notOkResponse"))
    }

  implicit val uriEncoder: Encoder[URI] = Encoder.encodeString.contramap(_.toString)
  case class Downloadable private(fileDate: LocalDate, uri: URI)

  case class ProvinceRow private(country: Country, province: Province, provinceStats: ProvinceStats)

  def toProvinceRow(values: Map[String, String]): Option[ProvinceRow] = {

    val lastUpdateOpt = values.get("Last Update").filterNot(_ == "Last Update").map(
      DateFormatter.parseLocalDateTimeUnsafe
    )
    lastUpdateOpt.map {
      lastUpdate =>
        val countryName = values.getOrElse("Country/Region", "")
        val provinceName = values.getOrElse("Province/State", "")

        val confirmed = Confirmed(values.get("Confirmed").filterNot(_.isEmpty).map(_.toInt).getOrElse(0))
        val deaths = Deaths(values.get("Deaths").filterNot(_.isEmpty).map(_.toInt).getOrElse(0))
        val recoveries = Recovered(values.get("Recovered").filterNot(_.isEmpty).map(_.toInt).getOrElse(0))
        ProvinceRow(
          Country(countryName),
          Province(provinceName),
          ProvinceStats(
            lastUpdate,
            confirmed,
            deaths,
            recoveries
          )
        )
    }

  }

  val getContents =
    Github[IO](None).repos
      .getContents(
        "CSSEGISandData",
        "COVID-19",
        "csse_covid_19_data/csse_covid_19_daily_reports", Some("master")
      ).map(_.result).flatMap(IO.fromEither)

  val dailyReports = getContents.map(_.map(toDownloadableCsv).collect {
    case Some(o) =>
      o
  })

  val dateParsing: String => LocalDate =
    LocalDate.parse(_, DateTimeFormatter.ofPattern("MM-dd-yyyy"))

  private def toDownloadableCsv(content: Content): Option[Downloadable] = for {
    fileDate <- Try(dateParsing(content.name.substring(0, 10))).toOption
    downloadUrl <- content.download_url
  } yield Downloadable (fileDate, URI.create(downloadUrl))



}

object DateFormatter {
  val formatter = new DateTimeFormatterBuilder()
    .appendOptional(DateTimeFormatter.ofPattern("M/d/yyyy H:mm"))
    .appendOptional(DateTimeFormatter.ofPattern("M/d/yy H:mm"))
    .toFormatter()


  val parseLocalDateTimeUnsafe: String => LocalDateTime = date =>
    (Try(LocalDateTime.parse(date, formatter)) orElse
      Try(LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME))).get
}