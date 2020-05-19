package ru.sapphire.service

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, ProducerSettings}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{
  ByteArrayDeserializer,
  StringDeserializer,
  StringSerializer
}
import ru.sapphire.core.ConfigObject.KAFKA_SETTINGS

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
