package com.bigdata

import org.apache.spark.SparkConf
import org.apache.spark.streaming.flume.FlumeUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.flume._

object FlumeFileTest {
  def main(args: Array[String]): Unit = {
     var sparkConf = new SparkConf()
    sparkConf.setAppName("flumeFile").setMaster("local[4]");
     var streamCtxt = new StreamingContext(sparkConf,Seconds(5))
    streamCtxt.checkpoint("hdfs://hadoop001:8020/spark-streaming")
//     var flumeStream = FlumeUtils.createStream(streamCtxt,"hadoop001",8888)
    var flumeStream = FlumeUtils.createPollingStream(streamCtxt,"hadoop001",8888)
//    flumeStream.map(line => new String(line.event.getBody().array()).trim).print()
//    flumeStream.flatMap(line => new String(line.event.getBody().array()).split(" ")).map(x=>(x,1)).reduceByKey(_+_).print()
    flumeStream.flatMap(line => new String(line.event.getBody().array()).split(" ")).map(x=>(x,1))
        .updateStateByKey[Int](updateFunction _).print()
    streamCtxt.start()
    streamCtxt.awaitTermination()
  }

  def updateFunction(currentValues: Seq[Int], preValues: Option[Int]): Option[Int] = {
    val current = currentValues.sum
    val pre = preValues.getOrElse(0)
    Some(current + pre)
  }
}
