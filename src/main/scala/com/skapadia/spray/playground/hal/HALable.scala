package com.skapadia.spray.playground.hal

import spray.json._
import spray.httpx.SprayJsonSupport._
import DefaultJsonProtocol._

// TODO: HAL Links

abstract class HALable[A](implicit writer: JsonWriter[A]) {
  def mainResource: A
  def embedded: Option[Map[String, Seq[HALable[_]]]]

  private def write: JsObject = new JsObject(writeMainResource.fields ++ (embeddedFields))
  private def writeMainResource = mainResource.toJson.asJsObject
  private def writeEmbedded = embedded map { _.foldLeft(Map[String, Seq[JsValue]]()) { (map, pair) => map + (pair._1 -> pair._2.map(_.write)) } }
  private def embeddedFields = writeEmbedded map { jsValuesMap => Map("embedded" -> jsValuesMap.toJson) } getOrElse Map[String, JsValue]()
}

case class HALResponse[A](
  override val mainResource: A,
  override val embedded: Option[Map[String, Seq[HALable[_]]]] = None)(implicit writer: JsonWriter[A]) extends HALable[A]

object HALable {

  implicit object halableWriter extends RootJsonWriter[HALable[_]] {
    def write(halObj: HALable[_]): JsObject = halObj.write
  }

  implicit def halableMarshallerConverter[T <: HALable[_]](implicit writer: RootJsonWriter[HALable[_]], printer: JsonPrinter = PrettyPrinter) =
    sprayJsonMarshaller[HALable[_]](writer, printer)
}

/* Alternate working implementation with no abstract class
case class HALResponse[A](mainResource: A, embedded: Map[String, HALResponse[_]])(implicit writer: JsonWriter[A]) {
  def writeMainResource: JsObject = mainResource.toJson.asJsObject
  def write: JsObject = new JsObject(writeMainResource.fields + ("embedded" -> writeEmbedded.toJson))
  def writeEmbedded = embedded.foldLeft(Map[String, JsValue]()) { (map, pair) => map + (pair._1 -> pair._2.write) }
}

object HALResponse {

  implicit object halableWriter extends RootJsonWriter[HALResponse[_]] {
    def write(halObj: HALResponse[_]): JsObject = halObj.write
  }

  implicit def halableMarshallerConverter[T <: HALResponse[_]](implicit writer: RootJsonWriter[HALResponse[_]], printer: JsonPrinter = PrettyPrinter) =
    sprayJsonMarshaller[HALResponse[_]](writer, printer)
}
*/
