import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree
import edu.stanford.nlp.util.CoreMap
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.elasticsearch.hadoop.cfg.ConfigurationOptions

import scala.collection.JavaConverters._

class KafkaConsumer extends Config with TextAnalysis {

  def consume(): Unit = {

    val batchSize = 2

    val sparkSession: SparkSession = SparkSession
      .builder()
      .config(ConfigurationOptions.ES_NET_HTTP_AUTH_USER, "")
      .config(ConfigurationOptions.ES_NET_HTTP_AUTH_PASS, "")
      .config(ConfigurationOptions.ES_BATCH_SIZE_ENTRIES, 50)
      .config(ConfigurationOptions.ES_NODES, "localhost")
      .config(ConfigurationOptions.ES_PORT, "9200")
      .config(ConfigurationOptions.ES_INDEX_AUTO_CREATE, "true")
      .master("local[*]")
      .appName("analysis")
      .getOrCreate()

    val checkpointDir = "./checkpoint"

    val streamingContext = new StreamingContext(sparkSession.sparkContext, Seconds(batchSize))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> brokers,
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "test_id",
      "auto.offset.reset" -> "earliest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array(topic)

    val stream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      streamingContext,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    val analysedStream: DStream[ESEntry] = stream.transform(r => r.map { e =>

      val nlpProps = {
        val props = new Properties()
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment")
        props
      }

      val message = e.value()
      val pipeline = new StanfordCoreNLP(nlpProps)
      val doc = new Annotation(message)

      pipeline.annotate(doc)
      val sentences: List[CoreMap] = doc.get(classOf[CoreAnnotations.SentencesAnnotation]).asScala.toList

      val result = sentences
        .map(sentence => (sentence, sentence.get(classOf[SentimentAnnotatedTree])))
        .foldLeft(0.0, 0) {
          case ((acc1, acc2), (sentence, tree)) =>
            val s = RNNCoreAnnotations.getPredictedClass(tree).toDouble
            (acc1 + sentence.toString.length * s, acc2 + sentence.toString.length)
        }

      val weightedResult = result._1 / result._2

      val output = weightedResult match {
        case s if s <= 0.0 => NoComments
        case s if s < 1.0 => VeryNegative
        case s if s < 2.0 => Negative
        case s if s < 3.0 => Neutral
        case s if s < 4.0 => Positive
        case s if s < 5.0 => VeryPositive
        case s if s > 5.0 => NoComments
      }

      val components = e.key().split(",")
      val time = components(0).toLong
      val lat: Double = (components(1).toDouble * 100.0).toInt / 100.0
      val lon: Double = (components(2).toDouble * 100.0).toInt / 100.0
      ESEntry(time, output.toString, s"$lat,$lon", weightedResult / 5.0, e.value())

    })

    analysedStream.foreachRDD { rdd =>
      val df = sparkSession.sqlContext.createDataFrame(rdd)
      import org.elasticsearch.spark.sql._
      df.saveToEs("tweet/_doc")
    }

    streamingContext.remember(Minutes(1))
    streamingContext.checkpoint(checkpointDir)

    streamingContext.start()
    streamingContext.awaitTerminationOrTimeout(5000)
  }
}
