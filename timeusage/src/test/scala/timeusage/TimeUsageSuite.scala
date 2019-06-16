package timeusage

import org.apache.spark.sql.{Column, ColumnName, DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.apache.spark.sql.functions.col
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.util.Random


case class TimeSeriesData(telfs: Double, tesex: Double, teage: Double,
                          te1: Double, te2: Double, te3: Double,
                          te4: Double, te5: Double
                         )
//case class SummarisedTimeUsageData(working: String, sex: String, age: String,
//                                   primaryNeeds: Double, work: Double, other: Double)

@RunWith(classOf[JUnitRunner])
class TimeUsageSuite extends FunSuite with BeforeAndAfterAll {

  val spark = SparkSession.builder().appName("TimeUsage").master("local").getOrCreate()

  import spark.implicits._

  override protected def afterAll(): Unit = {
    spark.stop()
  }

  trait RawTimeUsageRows {
    val data: Seq[TimeSeriesData] = Seq(
      TimeSeriesData(4, 1, 21, 60, 60, 30, 30, 60),
      TimeSeriesData(1, 0, 30, 60, 30, 30, 30, 60),
      TimeSeriesData(2, 0, 75, 60, 60, 30, 30, 60),
      TimeSeriesData(5, 0, 75, 60, 60, 30, 30, 60),
      TimeSeriesData(0, 1, 5, 60, 60, 30, 30, 60)
    )
    val df: DataFrame = data.toDF()

    val primCols: List[Column] = List(col("te1"), col("te2"))
    val workCols: List[Column] = List(col("te3"), col("te4"))
    val otherCols : List[Column] = List(col("te5"))
    df.show()
  }

  trait TimeUsageRows {
    val data: Seq[TimeUsageRow] = Seq(
      TimeUsageRow("not working", "male", "elder", 2, 2, 2),
      TimeUsageRow("not working", "male", "elder", 2, 2, 2),
      TimeUsageRow("not working", "male", "elder", 3, 3, 3),
      TimeUsageRow("working", "male", "young", 2, 2, 2),
      TimeUsageRow("working", "male", "active", 2, 2, 2),
      TimeUsageRow("not working", "female", "young", 2, 2, 2),
      TimeUsageRow("working", "female", "elder", 2, 2, 2),
      TimeUsageRow("working", "male", "young", 2, 2, 2),
      TimeUsageRow("working", "female", "active", 2, 2, 2),
      TimeUsageRow("working", "female", "active", 2, 2, 2)
    )

    val df: DataFrame = spark.createDataFrame(data)
    df.show()

  }

  test("dfSchema: should convert column names into a StructType") {
    val schema: StructType = TimeUsage.dfSchema(List("col1", "col2", "col3"))
    val expectedSchema: StructType = StructType(
      List(
        StructField("col1", StringType, nullable = false),
        StructField("col2", DoubleType, nullable = false),
        StructField("col3", DoubleType, nullable = false)
      )
    )
    assert(schema === expectedSchema)
  }

  test("row: should parse record that conforms with schema") {
    val expectedSchema: StructType = StructType(
      List(
        StructField("col1", StringType, nullable = false),
        StructField("col2", DoubleType, nullable = false),
        StructField("col3", DoubleType, nullable = false)
      )
    )

    val row = TimeUsage.row(List("foo", "1", "4"))

    val df = spark.createDataFrame(spark.sparkContext.parallelize(Seq(row)), expectedSchema)
    assert(df.head().schema == expectedSchema)


  }

  test("classifiedColumns: split the dataset into a manageable dataset") {
    val primaryNeedsCols = List("t01434", "t03234", "t11543", "t180187", "t180398")
    val workingActivitiesCols = List("t0509", "t180512")
    val otherActivitiesCols = List("t02966", "t04445", "t06", "t07", "t08", "t09", "t10", "t12", "t13", "t14", "t15", "t16", "t18")
    val irrelevantActivites = List("t45846", "t46546")

    val (actualPrimary, actualWorking, actualOther) = TimeUsage
      .classifiedColumns(
        primaryNeedsCols ++
          workingActivitiesCols ++
          otherActivitiesCols ++
          irrelevantActivites
      )

    assert(primaryNeedsCols.length == actualPrimary.length)
    assert(workingActivitiesCols.length == actualWorking.length)
    assert(otherActivitiesCols.length == actualOther.length)
  }

  test("timeUsageSummary: return a dataframe that is easier to work with") {
    new RawTimeUsageRows {
      val actualDf: DataFrame = TimeUsage.timeUsageSummary(primCols, workCols, otherCols, df)
      actualDf.show()

      val actualColumns: Array[String] = actualDf.columns

      assert(actualColumns.contains("working"))
      assert(actualColumns.contains("sex"))
      assert(actualColumns.contains("age"))
      assert(actualColumns.contains("primaryNeeds"))
      assert(actualColumns.contains("work"))
      assert(actualColumns.contains("other"))

      val actualRows: Array[Row] = actualDf.collect()

      assert(actualRows.length == 4)

      assert(actualRows(0)(0) == "not working")
      assert(actualRows(0)(1) == "male")
      assert(actualRows(0)(2) == "young")
      assert(actualRows(0)(3) == 2)
      assert(actualRows(0)(4) == 1)
      assert(actualRows(0)(5) == 1)

      assert(actualRows(1)(0) == "working")
      assert(actualRows(1)(1) == "female")
      assert(actualRows(1)(2) == "active")
      assert(actualRows(1)(3) == 1.5)
      assert(actualRows(1)(4) == 1)
      assert(actualRows(1)(5) == 1)

      assert(actualRows(2)(0) == "working")
      assert(actualRows(2)(1) == "female")
      assert(actualRows(2)(2) == "elder")
      assert(actualRows(2)(3) == 2)
      assert(actualRows(2)(4) == 1)
      assert(actualRows(2)(5) == 1)

      assert(actualRows(3)(0) == "not working")
      assert(actualRows(3)(1) == "male")
      assert(actualRows(3)(2) == "elder")
      assert(actualRows(3)(3) == 2)
      assert(actualRows(3)(4) == 1)
      assert(actualRows(3)(5) == 1)
    }


  }

  test("timeUsageGrouped: aggregate and calcualte the average time spent on activities"){
    new TimeUsageRows {

      val actualDf: DataFrame = TimeUsage.timeUsageGrouped(df)

      actualDf.show()

      val actualColumns: Array[String] = actualDf.columns

      assert(actualColumns.contains("working"))
      assert(actualColumns.contains("sex"))
      assert(actualColumns.contains("age"))
      assert(actualColumns.contains("primaryNeeds"))
      assert(actualColumns.contains("work"))
      assert(actualColumns.contains("other"))

      val actualRows: Array[Row] = actualDf.collect()

      assert(actualRows(0)(0) == "not working")
      assert(actualRows(0)(1) == "female")
      assert(actualRows(0)(2) == "young")

      assert(actualRows(2)(0) == "working")
      assert(actualRows(2)(1) == "female")
      assert(actualRows(2)(2) == "active")

      assert(actualRows(3)(0) == "working")
      assert(actualRows(3)(1) == "female")
      assert(actualRows(3)(2) == "elder")

      assert(actualRows(4)(0) == "working")
      assert(actualRows(4)(1) == "male")
      assert(actualRows(4)(2) == "active")

      assert(actualRows(1)(0) == "not working")
      assert(actualRows(1)(1) == "male")
      assert(actualRows(1)(2) == "elder")
      assert(actualRows(1)(3) == 2.3)
      assert(actualRows(1)(4) == 2.3)
      assert(actualRows(1)(5) == 2.3)

      assert(actualRows(5)(0) == "working")
      assert(actualRows(5)(1) == "male")
      assert(actualRows(5)(2) == "young")
    }

  }

  test("timeUsageGroupedSql: aggregate and calculate the time spent with sql"){
    new TimeUsageRows {
      val actualDf: DataFrame = TimeUsage.timeUsageGroupedSql(df)
      actualDf.show()

      val actualColumns: Array[String] = actualDf.columns

      assert(actualColumns.contains("working"))
      assert(actualColumns.contains("sex"))
      assert(actualColumns.contains("age"))
      assert(actualColumns.contains("primaryNeeds"))
      assert(actualColumns.contains("work"))
      assert(actualColumns.contains("other"))

      val actualRows: Array[Row] = actualDf.collect()

      assert(actualRows(0)(0) == "not working")
      assert(actualRows(0)(1) == "female")
      assert(actualRows(0)(2) == "young")

      assert(actualRows(2)(0) == "working")
      assert(actualRows(2)(1) == "female")
      assert(actualRows(2)(2) == "active")

      assert(actualRows(3)(0) == "working")
      assert(actualRows(3)(1) == "female")
      assert(actualRows(3)(2) == "elder")

      assert(actualRows(4)(0) == "working")
      assert(actualRows(4)(1) == "male")
      assert(actualRows(4)(2) == "active")

      assert(actualRows(1)(0) == "not working")
      assert(actualRows(1)(1) == "male")
      assert(actualRows(1)(2) == "elder")
      assert(actualRows(1)(3) == 2.3)
      assert(actualRows(1)(4) == 2.3)
      assert(actualRows(1)(5) == 2.3)

      assert(actualRows(5)(0) == "working")
      assert(actualRows(5)(1) == "male")
      assert(actualRows(5)(2) == "young")
    }
  }

  test("timeUsageSummaryTyped: Using datasets api create the summary data frame"){
    new TimeUsageRows {
      val actualDs: Dataset[TimeUsageRow] = TimeUsage.timeUsageSummaryTyped(df)
      actualDs.show()

      val actualColumns: Array[String] = actualDs.columns

      assert(actualColumns.contains("working"))
      assert(actualColumns.contains("sex"))
      assert(actualColumns.contains("age"))
      assert(actualColumns.contains("primaryNeeds"))
      assert(actualColumns.contains("work"))
      assert(actualColumns.contains("other"))

      val actualRows: Array[TimeUsageRow] = actualDs.collect()

      assert(actualRows.length == 10)

      assert(actualRows(0).working == "not working")
      assert(actualRows(0).sex == "male")
      assert(actualRows(0).age == "elder")
      assert(actualRows(0).primaryNeeds == 2)
      assert(actualRows(0).work == 2)
      assert(actualRows(0).other == 2)

    }

  }

  test("timeUsageGroupedTyped: Perform the aggregation logic already applied on the sql and DF tests"){
    new TimeUsageRows {
      val actualDs: Dataset[TimeUsageRow] = TimeUsage.timeUsageGroupedTyped(df.as[TimeUsageRow])
      actualDs.show()

      val actualColumns: Array[String] = actualDs.columns

      assert(actualColumns.contains("working"))
      assert(actualColumns.contains("sex"))
      assert(actualColumns.contains("age"))
      assert(actualColumns.contains("primaryNeeds"))
      assert(actualColumns.contains("work"))
      assert(actualColumns.contains("other"))

      val actualRows: Array[TimeUsageRow] = actualDs.collect()

      assert(actualRows(0).working == "not working")
      assert(actualRows(0).sex == "female")
      assert(actualRows(0).age == "young")

      assert(actualRows(2).working == "working")
      assert(actualRows(2).sex == "female")
      assert(actualRows(2).age == "active")

      assert(actualRows(3).working == "working")
      assert(actualRows(3).sex == "female")
      assert(actualRows(3).age == "elder")

      assert(actualRows(4).working == "working")
      assert(actualRows(4).sex == "male")
      assert(actualRows(4).age == "active")

      assert(actualRows(1).working == "not working")
      assert(actualRows(1).sex == "male")
      assert(actualRows(1).age == "elder")
      assert(actualRows(1).primaryNeeds == 2.3)
      assert(actualRows(1).work == 2.3)
      assert(actualRows(1).other == 2.3)

      assert(actualRows(5).working == "working")
      assert(actualRows(5).sex == "male")
      assert(actualRows(5).age == "young")

    }
  }
}
