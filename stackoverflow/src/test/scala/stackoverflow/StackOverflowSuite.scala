package stackoverflow

import org.scalatest.{FunSuite, BeforeAndAfterAll}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import java.io.File

@RunWith(classOf[JUnitRunner])
class StackOverflowSuite extends FunSuite with BeforeAndAfterAll {

  val conf: SparkConf = new SparkConf().setMaster("local").setAppName("StackOverflow")
  val sc: SparkContext = new SparkContext(conf)

  lazy val testObject = new StackOverflow {
    override val langs =
      List(
        "JavaScript", "Java", "PHP", "Python", "C#", "C++", "Ruby", "CSS",
        "Objective-C", "Perl", "Scala", "Haskell", "MATLAB", "Clojure", "Groovy")
    override def langSpread = 50000
    override def kmeansKernels = 45
    override def kmeansEta: Double = 20.0D
    override def kmeansMaxIterations = 120
  }


  override def afterAll(): Unit = {
    sc.stop()
  }

  test("testObject can be instantiated") {
    val instantiatable = try {
      testObject
      true
    } catch {
      case _: Throwable => false
    }
    assert(instantiatable, "Can't instantiate a StackOverflow object")
  }
  trait TestsPostings {
    val q1: Question = Posting(1, 0, Some(2), None, 5, Some("Scala"))
    val a1: Answer = Posting(2, 2, None, Some(0), 5, None)
    val a2: Answer = Posting(2, 3, None, Some(0), 6, None)
    val q2: Question = Posting(1, 4, None, None, 5, Some("Scala"))
    val a3: Answer = Posting(2, 5, None, Some(4), 8, None)
    val q3: Question = Posting(2, 6, None, None, -1, Some("Haskell"))

    val posts = sc.parallelize(Seq(q1,a1,a2,q2,a3))
  }
  test("should group questions and answers together"){
    new TestsPostings {
      val groupedPostings = testObject.groupedPostings(posts).collect()
      assert(groupedPostings.length == 2)
      assert(groupedPostings.contains((q1.id, Iterable((q1, a1), (q1, a2)))))
      assert(groupedPostings.contains((q2.id, Iterable((q2, a3)))))
    }
  }

  test("should compute scores and return the highest score"){
    new TestsPostings {
      val groupedAnswers = testObject.groupedPostings(posts)
      val highestScores = testObject.scoredPostings(groupedAnswers).collect()
      assert(highestScores.length == 2)
      assert(highestScores contains((q1, 6)))
      assert(highestScores contains((q2, 8)))
    }
  }

  test("should create vecotrs for clustering") {
    new TestsPostings {
      val groupedAnswers = testObject.groupedPostings(posts)
      val scored = testObject.scoredPostings(groupedAnswers)
      val vectors = testObject.vectorPostings(scored).collect()
      assert(vectors.length == 2)
      assert(vectors contains((500000, 8)))
      assert(vectors contains((500000, 6)))
//      assert(vectors contains())
    }
  }
}
