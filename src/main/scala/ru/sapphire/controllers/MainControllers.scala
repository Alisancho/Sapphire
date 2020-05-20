package ru.sapphire.controllers
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives.{complete, path, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.kafka.ProducerSettings
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import org.elasticsearch.client.RestClient
import ru.sapphire.AppStart
import ru.sapphire.service.BusinessTaskServiceImpl

import scala.concurrent.ExecutionContextExecutor


class MainControllers(producerSettings: ProducerSettings[String, String])(implicit system: ActorSystem,
                                                                   materializer: ActorMaterializer,
                                                                   ec: ExecutionContextExecutor,
                                                                   client: RestClient) extends LazyLogging {

  private val businessTaskServiceImpl = new BusinessTaskServiceImpl(producerSettings)

  val routApiV1: Route = pathPrefix("api" / "v1") {
    path("version") {
      get {
        getFromResource("htmlres/index.html")
      }
    } ~ pathPrefix("createMessage") {
      path("kafka") {
        post {
          entity(as[HttpRequest]) { mess =>
            complete(businessTaskServiceImpl
              .addMessageToKafka(mess)
              .onErrorHandle(throwableToResponseJSONObject)
              .runToFuture(AppStart.scheduler))
          }
        }
      } ~ path("kafkaMod") {
        post{
          entity(as[HttpRequest]) { mess =>
            complete(businessTaskServiceImpl
              .addMessageToKafkaMod(mess)
              .onErrorHandle(throwableToResponseJSONObject)
              .runToFuture(AppStart.scheduler))
          }
        }~ path("mq") {
          post{
            entity(as[HttpRequest]) { mess =>
              complete(businessTaskServiceImpl
                .addMessageToMQ(mess)
                .onErrorHandle(throwableToResponseJSONObject)
                .runToFuture(AppStart.scheduler))
            }
          }
        }
      }
    } ~ pathPrefix("inapi") {
      path("search") {
        post{
          entity(as[HttpRequest]) { mess =>
            complete(businessTaskServiceImpl
              .searshInElastic(mess)
              .onErrorHandle(throwableToResponseJSONObject)
              .runToFuture(AppStart.schedulerForAsyncKafka))
          }
        }
      }
    }
  }
}