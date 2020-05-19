package ru.sapphire.service
import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.alpakka.elasticsearch.WriteMessage
import akka.stream.alpakka.elasticsearch.scaladsl.ElasticsearchSink
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.elasticsearch.client.RestClient
import ru.sapphire.service.KafkaConsumerToElastic.SearchPayment
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.Future
import scala.xml.XML
object KafkaConsumerToElastic extends LazyLogging {

  case class SearchPayment(UID: String, value: String)


  lazy implicit val formatSearchPayment: JsonFormat[SearchPayment] = jsonFormat2(SearchPayment)

  val funFromAstartConsumer: Either[Throwable, _] => Unit = {
    case Right(_) =>
    case Left(err) => logger.error("ERROR_KAFKA=" + err.getMessage)
  }
}

class KafkaConsumerToElastic(topic: String, consumerSettings:ConsumerSettings[Array[Byte], String])
                            (elasticIndex: String, elasticTypeDoc: String)
                            (implicit system: ActorSystem, materialize: ActorMaterializer, client: RestClient) {


  private def getID: String = "API-" + UUID.randomUUID().toString.substring(24)

  def startConsumer: Future[Done] =
    Consumer.plainSource(consumerSettings, Subscriptions.topics(topic)).map { message =>
      try {
        SearchPayment((XML.loadString(message.value()) \\ "UID").text, message.value())
      } catch {
        case _:Throwable => SearchPayment("", "")
      }
    }
      .filter(_.UID != "")
      .map { objectMess =>
        WriteMessage.createUpsertMessage(id = getID, source = objectMess)
      }.runWith(ElasticsearchSink.create[SearchPayment](elasticIndex, elasticTypeDoc))



  def iwdwdo[T](message: ConsumerRecord[Array[Byte], String], twdwd: T):SearchPayment= {
    try {
      SearchPayment((XML.loadString(message.value()) \\ "UID").text, message.value())
    } catch {
      case _:Throwable => SearchPayment("", "")
    }
  }


  def startConsumer2[T](topic:String,consumerSettings:ConsumerSettings[Array[Byte], String]): Future[Done] =
    Consumer.plainSource(consumerSettings, Subscriptions.topics(topic)).map {o =>iwdwdo(o,"")}
      .filter(_.UID != "")
      .map { objectMess =>
        WriteMessage.createUpsertMessage(id = getID, source = objectMess)
      }.runWith(ElasticsearchSink.create[SearchPayment](elasticIndex, elasticTypeDoc))
}
