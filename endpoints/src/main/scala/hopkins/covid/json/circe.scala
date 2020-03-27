package hopkins.covid.json

import hopkins.covid.model.{Confirmed, Country, Deaths, Province, Recovered}
import io.circe.{Codec, KeyEncoder}
import io.circe.generic.extras.semiauto._
object circe {

  implicit val countryCodec: Codec[Country] = deriveUnwrappedCodec
  implicit val deathsCodec: Codec[Deaths] = deriveUnwrappedCodec
  implicit val recoveriesCodec: Codec[Recovered] = deriveUnwrappedCodec
  implicit val confirmedCodec: Codec[Confirmed] = deriveUnwrappedCodec
  implicit val provinceCodec: Codec[Province] = deriveUnwrappedCodec
  implicit val provinceKeyCodec: KeyEncoder[Province] = KeyEncoder.encodeKeyString.contramap(_.name)

}
