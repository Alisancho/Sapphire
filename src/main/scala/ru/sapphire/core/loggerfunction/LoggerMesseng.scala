package ru.sapphire.core.loggerfunction

import com.softwaremill.tagging._

object LoggerMesseng {
  sealed trait ErrorInfoMesseng
  val ERROR_XML: String @@ ErrorInfoMesseng = "Ошибка парсинга XML".taggedWith[ErrorInfoMesseng]
  val ERROR_TOPIK:String @@ ErrorInfoMesseng = "Ошибка у указании имени топика".taggedWith[ErrorInfoMesseng]
  val ERROR_NO_RQUID: String @@ ErrorInfoMesseng = "В теле запроса нет поля RqUID".taggedWith[ErrorInfoMesseng]
  val ERROR_TOPEK: String @@ ErrorInfoMesseng = "Не указан параметр Topik".taggedWith[ErrorInfoMesseng]
  val ERROR_ELASTIC: String @@ ErrorInfoMesseng = "Не найден ответ в базе".taggedWith[ErrorInfoMesseng]
  val ERROR_KAFKA_PRODUCER: String @@ ErrorInfoMesseng = "Кафка не доступна".taggedWith[ErrorInfoMesseng]
  val ERROR_HOST_OR_PORT:String @@ ErrorInfoMesseng = "Ошибка у указании настройка Кафки".taggedWith[ErrorInfoMesseng]
}