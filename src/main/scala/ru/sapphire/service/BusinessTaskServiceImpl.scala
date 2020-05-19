package ru.sapphire.service
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.kafka.ProducerSettings
import akka.stream.Materializer
import monix.eval.Task
import org.elasticsearch.client.RestClient
import ru.sapphire.core.ObjectFotJSON.ResponseJSON
import ru.sapphire.core.loggerfunction.{LoggerMesseng, SapphireError}
import spray.json._
import cats.implicits._
import ru.sapphire.core.ConfigObject._
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import ru.sapphire.core.loggerfunction.LoggerMesseng._
class BusinessTaskServiceImpl(val producerSettings: ProducerSettings[String, String])(implicit val system: ActorSystem,
                                                                                      val materializer: Materializer,
                                                                                      val ec: ExecutionContextExecutor,
                                                                                      val client: RestClient) extends BusinessTaskHelper {

  val addMessageToKafka:HttpRequest => Task[String] = httpRequest =>
    for {
      requestID <- requestIDGen
      body      <- getBody(httpRequest)
      topik     <- getHeader(httpRequest, "Topik").onErrorHandle(_ => "")
      _         = if (topik == "") throw SapphireError(requestID.some, errorDescription = ERROR_TOPIK.some)
      _         <- Task {logger.info(s"requestID=$requestID ${httpRequest.headers.toString()}  boby=$body")}
      q         <- kafkaTasks.setKafka(httpRequest.headers.find(_.name() == "Topik").get.value(), body).timeoutWith(40 seconds, SapphireError(requestID.some,errorDescription = ERROR_KAFKA_PRODUCER.some))
    } yield ResponseJSON(requestID.some, success = true, result = "The message is delivered to Kafka".some).toJson.toString()

  val addMessageToKafkaMod:HttpRequest => Task[String] = httpRequest =>
    for {
      requestID <- requestIDGen
      body      <- getBody(httpRequest)
      topik     <- getHeader(httpRequest, "Topik").onErrorHandle(_ => "")
      _         = if (topik == "") throw SapphireError(requestID.some, errorDescription = ERROR_TOPIK.some)
      host      <- getHeader(httpRequest, "KafkaHost").onErrorHandle(_ => "")
      port      <- getHeader(httpRequest, "KafkaPort").onErrorHandle(_ => "")
      _         <- Task {logger.info(s"requestID=$requestID ${httpRequest.headers.toString()}  boby=$body")}
      _         = if (host == "" || port == "") throw SapphireError(requestID.some, errorDescription = ERROR_HOST_OR_PORT.some)
      sett      = KafkaServiceImpl.getProducerSettings(host,port.toInt)
      q         <- kafkaTasks.setKafkaCastom(httpRequest.headers.find(_.name() == "Topik").get.value(), body,sett).timeoutWith(40 seconds, SapphireError(requestID.some,errorDescription = ERROR_KAFKA_PRODUCER.some))
    } yield ResponseJSON(requestID.some, success = true, result = "The message is delivered to Kafka".some).toJson.toString()

  val addMessageToMQ: HttpRequest => Task[String] = httpRequest =>
    for {
      requestID <- requestIDGen
      body      <- getBody(httpRequest)
      _         <- Task {logger.info(s"${httpRequest.headers.toString}  boby=$body")}
      topik     <- getHeader(httpRequest, "Topik")
      _         <-  MQServiceImpl.monexTaskAddForMQ(body,topik)
    } yield ResponseJSON(requestID.some, success = true, result = "The message is delivered to MQ".some).toJson.toString()

  val searshInElastic: HttpRequest => Task[String] = httpRequest => for {
    requestID <- requestIDGen
    body      <- getBody(httpRequest)
    _         <- Task {logger.info(s"SEARCH_REQUEST requestId=$requestID boby=$body")}
    rqUID     <- getRqUID(body, requestID)
    _         = if (rqUID == "") throw SapphireError(requestID.some, errorDescription = ERROR_NO_RQUID.some)
    _         <- kafkaTasks.setKafka("monitoringApi", body).timeoutWith(40 seconds, SapphireError(requestID.some, errorDescription = ERROR_KAFKA_PRODUCER.some))
    l         <- elasticTasks.getValueFromElastic2(ELASTIC_INDEX, "rqUID", rqUID)
  } yield l.getOrElse(ResponseJSON(requestId = requestID.some, errorDescription = ERROR_ELASTIC.some, success = false).toJson.toString())

  //  val searshInElastic2: HttpRequest => Task[Either[Throwable, String]] = httpRequest => (for {
  //    requestID <- requestIDGen
  //    body      <- getBody(httpRequest)
  //    _         <- Task {logger.info(s"SEARCH_REQUEST requestId=$requestID boby=$body")}
  //    rqUID     <- getRqUID(body, requestID)
  //    _         = if (rqUID == "")   GenesisError(Option.apply(requestID), success = false,errorDescription = Option.apply(ERROR_NO_RQUID))
  //    _         <- kafkaTasks.setKafka("monitoringApi", body).timeoutWith(40 seconds, GenesisError(Option.apply(requestID), success = false,errorDescription = Option.apply(ERROR_KAFKA_PRODUCER)))
  //    l         <- elasticTasks.getValueFromElastic2(ELASTIC_INDEX, "rqUID", rqUID)
  //  } yield Right(l.getOrElse(ResponseJSON(requestId = Option.apply(requestID),
  //    errorDescription = Option.apply(ERROR_ELASTIC),
  //    success = false).toJson.toString())))
  //    .onErrorHandle( error => Left(error))

}
