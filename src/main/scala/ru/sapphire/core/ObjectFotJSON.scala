package ru.sapphire.core

import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol._
import spray.json._

object ObjectFotJSON {
  lazy implicit val formatResponseJSON: RootJsonFormat[ResponseJSON] = jsonFormat6(ResponseJSON)

  case class ResponseJSON(requestId       : Option[String] = None,
                          success         : Boolean,
                          result          : Option[String] = None,
                          errorId         : Option[String] = None,
                          errorCode       : Option[String] = None,
                          errorDescription: Option[String] = None) {
  }

}