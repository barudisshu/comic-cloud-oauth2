package io.comiccloud.rest

import java.sql.Timestamp
import java.util.Date

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

import scala.concurrent.duration.FiniteDuration

/**
  * Root json protocol class for others to extend from
  */
trait ServiceProtocol extends SprayJsonSupport with DefaultJsonProtocol {

  import reflect._

  private val PASS1       = """([A-Z]+)([A-Z][a-z])""".r
  private val PASS2       = """([a-z\d])([A-Z])""".r
  private val REPLACEMENT = "$1_$2"
  override protected def extractFieldNames(classTag: ClassTag[_]): Array[String] = {
    import java.util.Locale
    def snakify(name: String) =
      PASS2.replaceAllIn(PASS1.replaceAllIn(name, REPLACEMENT), REPLACEMENT).toLowerCase(Locale.US)

    super.extractFieldNames(classTag).map { snakify }
  }
  implicit object DateFormat extends JsonFormat[Date] {
    def write(date: Date): JsValue = JsNumber(date.getTime)
    def read(json: JsValue): Date = json match {
      case JsNumber(epoch) => new Date(epoch.toLong)
      case unknown         => deserializationError(s"Expected JsString, got $unknown")
    }
  }
  implicit object TimestampFormat extends JsonFormat[Timestamp] {
    override def write(obj: Timestamp): JsValue = JsNumber(obj.getTime)
    override def read(json: JsValue): Timestamp = json match {
      case JsNumber(epoch) => new Timestamp(epoch.toLong)
      case unknown         => deserializationError(s"Expected JsString, got $unknown")
    }
  }
  implicit object AnyJsonFormat extends JsonFormat[Any] {
    def write(x: Any): JsValue = x match {
      case n: Int           => JsNumber(n)
      case s: String        => JsString(s)
      case b: Boolean if b  => JsTrue
      case b: Boolean if !b => JsFalse
    }
    def read(value: JsValue): Any = value match {
      case JsNumber(n) => n.intValue()
      case JsString(s) => s
      case JsTrue      => true
      case JsFalse     => false
      case _           => serializationError(s"serialization error")
    }
  }
  implicit object FiniteDurationJsonFormat extends RootJsonFormat[FiniteDuration] {
    def write(dur: FiniteDuration) = JsObject(
      "length" -> JsNumber(dur.length),
      "unit"   -> JsString(dur.unit.toString)
    )
    def read(value: JsValue): FiniteDuration = {
      value.asJsObject.getFields("length", "unit") match {
        case Seq(JsNumber(length), JsString(unit)) => FiniteDuration(length.toLong, unit.toLowerCase)
        case _                                     => deserializationError("FiniteDuration expected")
      }
    }
  }
}

object ServiceProtocol extends ServiceProtocol
