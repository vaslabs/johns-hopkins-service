package hopkins.database.github

import java.time.LocalDateTime

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class DateFormatterSpec extends AnyFlatSpec with Matchers{

  "dates" must "be parseable" in {
    List(
      "2/1/2020 1:52",
      "12/1/2020 1:52",
      "12/10/2020 1:52",
      "2/10/2020 1:52",
      "2/10/2020 14:52",
      "2/10/20 14:52",
      "2020-02-02T23:43:02"
    ).map(DateFormatter.parseLocalDateTimeUnsafe) mustBe List(
      LocalDateTime.of(2020, 2, 1, 1, 52),
      LocalDateTime.of(2020, 12, 1,1,52),
      LocalDateTime.of(2020, 12, 10, 1, 52),
      LocalDateTime.of(2020, 2, 10, 1, 52),
      LocalDateTime.of(2020, 2, 10, 14, 52),
      LocalDateTime.of(2020, 2, 10, 14, 52),
      LocalDateTime.of(2020, 2, 2, 23, 43, 2)
    )
  }
}
