import sbtassembly.MergeStrategy

name := "Sapphire"

version := "0.1"

scalaVersion := "2.13.2"

scalacOptions += "-feature"
lazy val akkaVersion = "2.5.26"
lazy val akkaHTTP = "10.1.10"
lazy val akkaKafka = "1.0.5"
lazy val swaggerVersion = "2.0.9"
lazy val logback = "1.2.3"

val circeVersion      = "0.11.0"
val circeDerVersion   = "0.11.0-M1"
val catsVersion       = "1.6.0"
val catsEffectVersion = "1.1.0"
val fs2Version        = "1.0.3"
lazy val softwaremill      = "2.3.3"
libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-http" % akkaHTTP,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHTTP % Test,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-distributed-data" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHTTP,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % akkaKafka,
  "org.slf4j" % "slf4j-api" % "1.7.27",
  "net.logstash.logback" % "logstash-logback-encoder" % "6.1",
  "ch.qos.logback" % "logback-core" % logback,
  "ch.qos.logback" % "logback-classic" % logback,
  "com.rabbitmq" % "amqp-client" % "5.3.0",
  "com.typesafe.play" %% "play-json" % "2.7.4",
  "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
  "commons-logging" % "commons-logging" % "1.2",
  "com.lightbend.akka" %% "akka-stream-alpakka-elasticsearch" % "1.1.2",
  "io.monix" %% "monix" % "3.0.0",
)
libraryDependencies += "org.typelevel" %% "cats-core" % "2.0.0"

libraryDependencies ++= Seq(
  "com.softwaremill.macwire" %% "macrosakka" % softwaremill % "provided",
  "com.softwaremill.macwire" %% "macros"     % softwaremill % "provided",
  "com.softwaremill.macwire" %% "proxy"      % softwaremill,
  "com.softwaremill.macwire" %% "util"       % softwaremill
)


lazy val commonSettings = Seq(
  test in assembly :={}
)

val defaultMergeStrategy: String => MergeStrategy = {
  case x if Assembly.isConfigFile(x) =>
    MergeStrategy.concat
  case PathList(ps @ _*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
    MergeStrategy.rename
  case PathList("META-INF", xs @ _*) =>
    (xs map {_.toLowerCase}) match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
        MergeStrategy.discard
      case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
        MergeStrategy.discard
      case "plexus" :: xs =>
        MergeStrategy.discard
      case "services" :: xs =>
        MergeStrategy.filterDistinctLines
      case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
        MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.deduplicate
    }
  case _ => MergeStrategy.deduplicate
}