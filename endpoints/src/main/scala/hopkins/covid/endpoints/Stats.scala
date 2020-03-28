package hopkins.covid.endpoints
import java.time.LocalDate

import hopkins.covid.model.{Confirmed, Country, CountryStats, Deaths, Province, Recovered}
import sttp.tapir._
import hopkins.covid.json.circe._
import sttp.model.StatusCode
import sttp.tapir.json.circe._
object Stats {

  implicit val countryCodec = Codec.stringPlainCodecUtf8.map(Country)(_.name)
  import documentation._

  val country = endpoint
    .get
    .in("country" / path[Country]("country"))
    .in(query[LocalDate]("from").example(LocalDate.now().minusMonths(2)))
    .in(query[LocalDate]("to").example(LocalDate.now()))
    .out(jsonBody[Map[LocalDate, CountryStats]])

  val allCountries = endpoint
    .get
    .in("countries")
    .out(jsonBody[List[Country]])

  val health = endpoint
    .get
    .in("health")
    .out(statusCode(StatusCode.Ok))

  val allEndpoints = List(country, health)
}

object documentation {
  implicit val countrySchema: Schema[Country] = Schema(SchemaType.SString)
  implicit val provinceSchema: Schema[Province] = Schema(SchemaType.SString)
  implicit val deathsSchema: Schema[Deaths] = Schema(SchemaType.SInteger)
  implicit val confirmedSchema: Schema[Confirmed] = Schema(SchemaType.SInteger)
  implicit val recoveredSchema: Schema[Recovered] = Schema(SchemaType.SInteger)

  implicit val countryValidation: Validator[Country] = Validator.maxLength(128).contramap(_.name)
  implicit val provinceValidation: Validator[Province] = Validator.maxLength(128).contramap(_.name)
  implicit val deathsValidation: Validator[Deaths] = Validator.min(0).contramap(_.value)
  implicit val confirmedValidation: Validator[Confirmed] = Validator.min(0).contramap(_.value)
  implicit val recoveredValidation: Validator[Recovered] = Validator.min(0).contramap(_.value)

  implicit val countryStatsValidation: Validator[CountryStats] = Validator.pass
}
