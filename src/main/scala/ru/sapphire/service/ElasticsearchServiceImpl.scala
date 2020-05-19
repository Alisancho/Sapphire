package ru.sapphire.service

import akka.stream.Materializer
import akka.stream.alpakka.elasticsearch.ElasticsearchSourceSettings
import akka.stream.alpakka.elasticsearch.scaladsl.ElasticsearchSource
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.scalalogging.LazyLogging
import monix.eval.Task
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.client.RestClient
import ru.sapphire.service.KafkaConsumerToElastic.SearchPayment

import scala.concurrent.Future
import scala.concurrent.duration._

object ElasticsearchServiceImpl {
  def getElasticSearchClient(host: String, port: Int, login: String, pass: String): Task[RestClient] = Task {
    val credentialsProvider: CredentialsProvider = new BasicCredentialsProvider()
    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, pass))
    RestClient
      .builder(new HttpHost(host, port))
      .setHttpClientConfigCallback(p => {
        p.disableAuthCaching
        p.setDefaultCredentialsProvider(credentialsProvider)
      })
      .build
  }
}

class ElasticsearchTask(implicit materializer: Materializer,
                        client: RestClient) extends LazyLogging {

  private val searchFunction: (String, String, String) => Future[Seq[SearchPayment]] = (index, searchField, rqid) =>
    ElasticsearchSource.typed[SearchPayment](
      index,
      None,
      s""" {"match": {"$searchField":{"query": "$rqid"}} } """,
      ElasticsearchSourceSettings()).map(_.source
    ).runWith(Sink.seq)

  val getValueFromElastic2: (String, String, String) => Task[Option[String]] = (index, searchField, rqid) => Task.fromFuture {
    Source(Seq.range(0, 40)).flatMapConcat { _ =>
      Source.single(rqid).delay(1.seconds)
    }
      .mapAsync(parallelism = 1)(_ => searchFunction(index, searchField, rqid))
      .filter(_.nonEmpty)
      .map(_.head.value)
      .take(1)
      .runWith(Sink.headOption)
  }
}
