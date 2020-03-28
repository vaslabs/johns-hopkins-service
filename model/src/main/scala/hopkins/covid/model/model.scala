package hopkins.covid.model

import java.time.LocalDateTime

case class Country(name: String)
case class Province(name: String)
case class Confirmed(value: Int)
case class Deaths(value: Int)
case class Recovered(value: Int)

case class CountryStats(provinceStats: Map[Province, List[ProvinceStats]])

case class ProvinceStats(
    lastUpdate: LocalDateTime,
    confirmed: Confirmed,
    deaths: Deaths,
    recovered: Recovered
)