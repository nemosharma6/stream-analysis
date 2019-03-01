import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree
import edu.stanford.nlp.util.CoreMap

import scala.collection.JavaConverters._

trait TextAnalysis extends Config {

  def detect(message: String): String = {

    val nlpProps = {
      val props = new Properties()
      props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment")
      props
    }

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

    output.toString
  }

  def detectStub(msg: String): String = {
    if(msg.length % 2 == 0) Positive.toString else Negative.toString
  }
}
