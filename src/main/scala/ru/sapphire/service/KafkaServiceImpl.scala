package ru.sapphire.service

import akka.kafka.ConsumerSettings

import org.apache.kafka.clients.consumer.ConsumerConfig

import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer, StringSerializer}
import ru.sapphire.core.ConfigObject.KAFKA_SETTINGS
import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import monix.eval.Task
import org.apache.kafka.clients.producer.ProducerRecord
object KafkaServiceImpl {
  def getProducerSettings(host: String,
                          port: Int): ProducerSettings[String, String] =
    ProducerSettings(KAFKA_SETTINGS, new StringSerializer, new StringSerializer)
      .withBootstrapServers(host + ":" + port)

  def getConsumerSettings(host: String, port: Int, groupId: String)(
    implicit system: ActorSystem
  ): ConsumerSettings[Array[Byte], String] =
    ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers(host + ":" + port)
      .withGroupId(groupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
}

class KafkaStream(producerSettings: ProducerSettings[String, String])(implicit system: ActorSystem,
                                                                      materializer: Materializer) {
  val setKafka: (String, String) => Task[Done] = (topic, body) => Task.fromFuture {
    Source(1 to 1)
      .map(_ => new ProducerRecord[String, String](topic, body))
      .runWith(Producer.plainSink(producerSettings))
  }

  val setKafkaCastom: (String, String, ProducerSettings[String, String]) => Task[Done] = (topic, body, producerSettingsT) => Task.fromFuture {
    Source(1 to 1)
      .map(_ => new ProducerRecord[String, String](topic, body))
      .runWith(Producer.plainSink(producerSettingsT))
  }

}