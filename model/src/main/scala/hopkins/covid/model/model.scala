package hopkins.covid.model

import java.time.LocalDateTime

case class Country(value: String)
case class Province(value: String)
case class Confirmed(value: Int)
case class Deaths(value: Int)
case class Recovered(value: Int)

case class CountryStats(provinceStats: Map[Province, Set[ProvinceStats]])

case class ProvinceStats(
    lastUpdate: LocalDateTime,
    confirmed: Confirmed,
    deaths: Deaths,
    recovered: Recovered
)

case class DetectedCountries(found: Set[Country])