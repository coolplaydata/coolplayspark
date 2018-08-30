package com.open.spark.basic.rdd

import org.apache.spark.sql.SparkSession

/**
 * Created by huchao on 2018-08-08.
 */
object CollectRddExample {

  def main(args: Array[String]) {
    val spark = SparkSession.builder()
      .appName("DatasetExchangeDriver")
      .master("local[*]")
      .getOrCreate()

    val a = spark.sparkContext.parallelize(1 to 20, 3)

  }


}
