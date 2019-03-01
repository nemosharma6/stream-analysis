object Runner extends App {

  val twitterFeedObj = new TwitterFeed
  twitterFeedObj.readPersist()

  val kafkaConsumer = new KafkaConsumer
  kafkaConsumer.consume()

}
