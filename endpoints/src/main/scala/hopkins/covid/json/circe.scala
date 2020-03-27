package hopkins.covid.json

import hopkins.covid.model.{Confirmed, Country, CountryStats, Deaths, Province, Recovered}
import io.circe.{Codec, KeyDecoder, KeyEncoder}
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec
import io.circe.generic.semiauto._
import io.circe.generic.auto._
object circe {

  implicit val countryCodec: Codec[Country] = deriveUnwrappedCodec
  implicit val deathsCodec: Codec[Deaths] = deriveUnwrappedCodec
  implicit val recoveriesCodec: Codec[Recovered] = deriveUnwrappedCodec
  implicit val confirmedCodec: Codec[Confirmed] = deriveUnwrappedCodec
  implicit val provinceCodec: Codec[Province] = deriveUnwrappedCodec
  implicit val provinceKeyEncoder: KeyEncoder[Province] = KeyEncoder.encodeKeyString.contramap(_.name)
  implicit val provinceKeyDecoder: KeyDecoder[Province] = KeyDecoder.decodeKeyString.map(Province)

  implicit val countryStats: Codec[CountryStats] = deriveCodec
}
