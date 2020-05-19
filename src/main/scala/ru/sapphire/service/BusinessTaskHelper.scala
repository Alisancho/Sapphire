package ru.sapphire.service

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.kafka.ProducerSettings
import akka.stream.Materializer
import com.typesafe.scalalogging.LazyLogging
import monix.eval.Task
import org.elasticsearch.client.RestClient
import ru.sapphire.core.loggerfunction.LoggerMesseng.ERROR_XML
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import cats.implicits._
import ru.sapphire.core.loggerfunction.SapphireError
trait BusinessTaskHelper extends LazyLogging{
  implicit val system: ActorSystem
  implicit val materializer: Materializer
  implicit val ec: ExecutionContextExecutor
  implicit val client: RestClient
  val producerSettings: ProducerSettings[String, String]

  lazy val kafkaTasks = new KafkaStream(producerSettings)
  lazy val elasticTasks = new ElasticsearchTask()
  implicit val wd: languageFeature.postfixOps = scala.language.postfixOps

  val requestIDGen: Task[String] = Task {
    "G-" + UUID.randomUUID().toString.substring(24)
  }

  val getHeader: (HttpRequest, String) => Task[String] = (httpRequest, name) => Task {
    httpRequest.headers.find(_.name() == name).get.value()
  }

  val getBody: HttpRequest => Task[String] = httpRequest => Task.fromFuture(
    httpRequest.entity
      .toStrict(3 seconds)
      .map(_.data.utf8String)
  )
  val getRqUID:(String, String) => Task[String] = (body,requestID) => Task {
    (scala.xml.XML.loadString(body) \\ "RqUID").text
  }.onErrorHandle(w => throw SapphireError(
    requestId = requestID.some,
    errorDescription = ERROR_XML.some,
    errorCode = w.getMessage.some)
  )
}
