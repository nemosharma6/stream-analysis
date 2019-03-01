import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import twitter4j._
import twitter4j.conf.ConfigurationBuilder

class TwitterFeed extends Config {

  def readPersist(): Unit = {

    val configBuilder = new ConfigurationBuilder()
    configBuilder
      .setOAuthConsumerKey(consumerKey)
      .setOAuthConsumerSecret(consumerSecret)
      .setOAuthAccessToken(accessToken)
      .setOAuthAccessTokenSecret(accessSecret)

    val twitterStream = new TwitterStreamFactory(configBuilder.build()).getInstance()

    val properties = new Properties()
    properties.put("bootstrap.servers", brokers)
    properties.put("client.id", "TwitterProducer")
    properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](properties)

    twitterStream.addListener(new StatusListener {
      override def onStatus(status: Status): Unit = {
        status.getGeoLocation match {
          case null => Unit
          case _ =>
            val data = new ProducerRecord[String, String](topic, s"${status.getCreatedAt.getTime}" +
              s",${status.getGeoLocation.getLatitude},${status.getGeoLocation.getLongitude}", status.getText)
            producer.send(data)
        }
      }

      override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = None

      override def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = None

      override def onScrubGeo(userId: Long, upToStatusId: Long): Unit = None

      override def onStallWarning(warning: StallWarning): Unit = None

      override def onException(ex: Exception): Unit = println("error : " + ex.getMessage)
    })

    val twitterFilterQuery = new FilterQuery()
    twitterFilterQuery.track(keyword)
    twitterFilterQuery.language(language)

    twitterStream.filter(twitterFilterQuery)
  }

}
