package github

import net.liftweb.json.{CustomSerializer, DefaultFormats}
import net.liftweb.json.JsonAST.{JString,JNull}
import net.liftweb.json.ext.DateParser
import org.joda.time.{DateTimeZone, DateTime}

case class DateTimeSerializer extends CustomSerializer[DateTime](format => (
  {
    case JString(s) => {
      s match {
        case TimeZoneFormats.offsetPattern(dateTime, timeZone) => {
          new DateTime(DateParser.parse(dateTime, TimeZoneFormats.dateTimeOnly)).withZoneRetainFields(DateTimeZone.forID(timeZone))
        }
        case TimeZoneFormats.utcPattern(dateTime, timeZone) =>
          new DateTime(DateParser.parse(dateTime, TimeZoneFormats.dateTimeOnly)).withZone(DateTimeZone.forID("Z"))
      }
    }
    case JNull => null
  },
  {
    case d: DateTime => JString(format.dateFormat.format(d.toDate))
  })
)

class ThreadLocal[A](init: => A) extends java.lang.ThreadLocal[A] with (() => A) {
  override def initialValue = init
  def apply = get
}

object TimeZoneFormats extends DefaultFormats {
  val dateTimeNoZone = new ThreadLocal(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"))

  val offsetPattern = """^(.*)([+-]\d\d:\d\d)$""".r
  val utcPattern = """^(.*)Z$""".r

  def dateTimeOnly = new DefaultFormats {
    override def dateFormatter = dateTimeNoZone()
  }
}