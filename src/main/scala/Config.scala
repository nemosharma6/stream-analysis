import com.typesafe.config.ConfigFactory

trait Config {

  val config: com.typesafe.config.Config = ConfigFactory.load("secret.conf")
  val consumerKey: String = config.getString("consumer-key")
  val consumerSecret: String = config.getString("consumer-secret")
  val accessToken: String = config.getString("access-token")
  val accessSecret: String = config.getString("access-secret")

  val brokers: String = config.getString("brokers")
  val topic: String = config.getString("topic")

  val keyword: String = config.getString("tweet.keyword")
  val language: String = config.getString("tweet.language")

  val index: String = config.getString("elasticsearch.index")
  val indexType: String = config.getString("elasticsearch.type")

}
