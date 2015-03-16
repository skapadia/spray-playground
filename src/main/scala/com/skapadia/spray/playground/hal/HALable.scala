package com.skapadia.spray.playground.hal

import spray.json._
import spray.httpx.SprayJsonSupport._
import DefaultJsonProtocol._

abstract class HALable[A](implicit writer: JsonWriter[A]) {
  def mainResource: A
  def writeMainResource: JsObject = mainResource.toJson.asJsObject
  def write: JsObject = new JsObject(writeMainResource.fields + ("embedded" -> writeEmbedded.toJson))
  def embedded: Map[String, HALable[_]]
  def writeEmbedded = embedded.foldLeft(Map[String, JsValue]()) { (map, pair) => map + (pair._1 -> pair._2.write) }
}

case class HALResponse[A](
  override val mainResource: A,
  override val embedded: Map[String, HALable[_]])(implicit writer: JsonWriter[A]) extends HALable[A]

object HALable {

  implicit object halableWriter extends RootJsonWriter[HALable[_]] {
    def write(halObj: HALable[_]): JsObject = halObj.write
  }

  implicit def halableMarshallerConverter[T <: HALable[_]](implicit writer: RootJsonWriter[HALable[_]], printer: JsonPrinter = PrettyPrinter) =
    sprayJsonMarshaller[HALable[_]](writer, printer)
}

