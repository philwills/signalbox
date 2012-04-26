import github.DateTimeSerializer
import org.joda.time.{DateTimeZone, DateTime}
import org.specs2.mutable.Specification

import net.liftweb.json._

class DateParsingSpec extends Specification {
  
  "JSON parser should recognise non-UTC date" in  {
    implicit val formats = DefaultFormats + DateTimeSerializer()

    val json = """{"date": "2010-11-18T04:25:23-08:00"}"""
    val expectedDateTime = new DateTime(2010, 11, 18, 4, 25, 23, DateTimeZone.forID("-08:00"))
    (parse(json) \ "date").extract[DateTime] must be equalTo expectedDateTime
  }
}
