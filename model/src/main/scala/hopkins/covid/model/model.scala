package hopkins.covid.model

import java.time.{LocalDate, LocalDateTime}

case class DailyStats(countryStats: Map[Country, CountryStats], time: LocalDate)
case class Country(name: String)
case class Province(name: String)
case class Confirmed(value: Int)
case class Deaths(value: Int)
case class Recovered(value: Int)
case class CountryStats(provinceStats: Map[Province, ProvinceStats])
case class ProvinceStats(lastUpdate: LocalDateTime, confirmed: Confirmed, deaths: Deaths, recovered: Recovered)
