package hopkins.covid.endpoints
import java.time.LocalDate

import hopkins.covid.model.{Confirmed, Country, CountryStats, Deaths, Province}
import sttp.tapir._
import io.circe.generic.auto._
import hopkins.covid.json.circe._
import sttp.model.StatusCode
import sttp.tapir.json.circe._
object CountryStats {

  implicit val countryCodec = Codec.stringPlainCodecUtf8.map(Country)(_.name)
  import documentation._


  val country = endpoint
    .get
    .in("country" / path[Country]("country"))
    .in(query[LocalDate]("from"))
    .in(query[LocalDate]("to"))
    .out(jsonBody[List[CountryStats]])

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
  implicit val recoveredSchema: Schema[Confirmed] = Schema(SchemaType.SInteger)
}
