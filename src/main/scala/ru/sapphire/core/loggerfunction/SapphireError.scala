package ru.sapphire.core.loggerfunction

import cats.implicits._
import ru.sapphire.core.ObjectFotJSON.ResponseJSON

case class SapphireError(requestId: Option[String] = None,
                        success: Boolean = false,
                        errorId: Option[String] = None,
                        errorCode: Option[String] = None,
                        errorDescription: Option[String] = None) extends RuntimeException {
}
object SapphireError{
  import spray.json._

  implicit lazy val throwableToResponseJSONObject: Throwable => String = {
    case error: SapphireError => ResponseJSON(
      requestId = error.requestId,
      errorId = error.errorId,
      success = error.success,
      errorDescription = error.errorDescription,
      errorCode = error.errorCode).toJson.toString()
    case err@(_: Throwable) => ResponseJSON(
      success = false,
      errorDescription = err.getMessage.some).toJson.toString()
  }
}