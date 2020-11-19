package com.bigdata

import com.bigdata.FlumeFileTest.updateFunction
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.flume.FlumeUtils
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent

object KafkaSparkTest {
  def main(args: Array[String]): Unit = {
    var sparkConf = new SparkConf()
    sparkConf.setAppName("kafkaSparkTest").setMaster("local[4]");
    var streamCtxt = new StreamingContext(sparkConf,Seconds(5))
//    streamCtxt.checkpoint("hdfs://hadoop001:8020/spark-streaming")

    var kafkaConfig = Map[String,Object](
      "zk.connect"-> "47.112.111.193:2181",
       "bootstrap.servers" -> "47.112.111.193:9092",
      "key.deserializer"  -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "user_mingixi_group",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (true:java.lang.Boolean)
    );
    var topic = Array("user_mingixi_mq")
    var kafkaStream = KafkaUtils.createDirectStream(streamCtxt,
      PreferConsistent,Subscribe[String,String](topic,kafkaConfig))

    kafkaStream.map(record=>(record.key,record.value)).print()


    streamCtxt.start()
    streamCtxt.awaitTermination()
  }

}
