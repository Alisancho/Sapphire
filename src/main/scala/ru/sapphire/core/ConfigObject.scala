package ru.sapphire.core
import com.typesafe.config.{Config, ConfigFactory}
object ConfigObject {
  lazy val gsConfig: Config = ConfigFactory.load()

  lazy val GS_HOST: String         = gsConfig.getString("S_HOST")
  lazy val GS_PORT: Int            = gsConfig.getInt("S_PORT")

  lazy val KAFKA_HOST: String      = gsConfig.getString("KAFKA_HOST")
  lazy val KAFKA_PORT: Int         = gsConfig.getInt("KAFKA_PORT")
  lazy val KAFKA_SETTINGS: Config  = gsConfig.getConfig("akka.kafka.producer")

  lazy val MQ_HOST: String          = gsConfig.getString("MQ_HOST")
  lazy val MQ_PORT: Int             = gsConfig.getInt("MQ_PORT")
  lazy val MQ_QMANAGER_NAME: String = gsConfig.getString("QMANAGER_NAME")
  lazy val MQ_CHANNEL: String       = gsConfig.getString("MQ_CHANNEL")

  lazy val ELASTIC_LOGIN: String    = gsConfig.getString("ELASTIC.LOGIN")
  lazy val ELASTIC_PASS: String     = gsConfig.getString("ELASTIC.PASS")
  lazy val ELASTIC_HOST: String     = gsConfig.getString("ELASTIC.HOST")
  lazy val ELASTIC_PORT: Int        = gsConfig.getInt("ELASTIC.PORT")
  lazy val ELASTIC_INDEX: String    = gsConfig.getString("ELASTIC.INDEX")

  lazy val KAFKA_TOPIK: String    = gsConfig.getString("monitoringApiRs")
}
