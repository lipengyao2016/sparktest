package com.bigdata

import org.apache.spark.SparkConf
import org.apache.spark.streaming.flume.FlumeUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object FlumeFileTest {
  def main(args: Array[String]): Unit = {
     var sparkConf = new SparkConf()
    sparkConf.setAppName("flumeFile").setMaster("local[4]");
     var streamCtxt = new StreamingContext(sparkConf,Seconds(5))
     var flumeStream = FlumeUtils.createStream(streamCtxt,"hadoop001",8888)
    flumeStream.map(line => new String(line.event.getBody().array()).trim).print()
    streamCtxt.start()
    streamCtxt.awaitTermination()
  }
}
