package com.bigdata;

import com.bigdata.utils.RedisUtil;
import org.apache.hadoop.yarn.webapp.hamlet.HamletSpec;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.Seconds;
import org.apache.spark.streaming.StreamingContext;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws InterruptedException {
        System.out.println( "Hello World!" );
        SparkConf conf = new SparkConf()
                .setAppName("myapplication") .setMaster("local[4]");
        JavaStreamingContext stream = new JavaStreamingContext(conf, Durations.seconds(10));
        stream.checkpoint("hdfs://hadoop001:8020/spark-streaming");
        System.out.println( "init ok!" );

        /*创建文本输入流,并进行词频统计*/
        JavaReceiverInputDStream<String> lines = stream.socketTextStream("hadoop001", 9999);

        FlatMapFunction<String, String> sf = new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                String[] lineArray = s.split(" ");
                return Arrays.asList(lineArray).iterator();
        }
        };

//        lines.flatMap( (FlatMapFunction<String, String>)s->{
//           String[] lineArray = s.split(" ");
//           return Arrays.asList(lineArray).iterator();
//        });
        JavaDStream<String> flatStream  = lines.flatMap(sf);
        System.out.println( "flatMap ok!" );



        PairFunction<String,String,Integer> pairFunction = new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<>(s,1);
            }
        };

        JavaPairDStream<String,Integer> pairList = flatStream.mapToPair(pairFunction);

//        JavaPairDStream<String, Integer> reducePair =  pairList.reduceByKey(
//                (Function2<Integer,Integer,Integer>)(v1,v2)->v1+v2);
//        System.out.println( "reduce ok!" );
//        reducePair.print();
//        System.out.println( "reduce print!" );

        JavaPairDStream<String, Integer> updateStatePair =  pairList.updateStateByKey(new Function2<List<Integer>, Optional<Integer>, Optional<Integer>>() {
            @Override
            public Optional<Integer> call(List<Integer> integers, Optional<Integer> optional) throws Exception {
                Integer clickCount = 0;
                if (optional.isPresent())
                {
                    clickCount = optional.get();
                }
                for (int v: integers) {
                    clickCount += v;
                }
                return Optional.of(clickCount);
            }
        });
        System.out.println( "updateStatePair ok!" );
        updateStatePair.print();
        System.out.println( "updateStatePair print!" );
        updateStatePair.foreachRDD(new VoidFunction<JavaPairRDD<String, Integer>>() {
            @Override
            public void call(JavaPairRDD<String, Integer> stringIntegerJavaPairRDD) throws Exception {

                stringIntegerJavaPairRDD.foreachPartition(new VoidFunction<Iterator<Tuple2<String, Integer>>>() {
                    @Override
                    public void call(Iterator<Tuple2<String, Integer>> tuple2Iterator) throws Exception {

                        for (Iterator tu2 = tuple2Iterator;tu2.hasNext();) {
                            Tuple2<String, Integer> t1  = (Tuple2<String, Integer>) tu2.next();
                            RedisUtil.addHash("wordCntCount",t1._1,String.valueOf(t1._2));
                        }

                    }
                });
            }
        });

        stream.start();
        System.out.println( "stream start!" );

        stream.awaitTermination();
        System.out.println( "stream end!" );

//        lines.flatMap(HamletSpec._.split(" ")).map(x => (x, 1)).reduceByKey(_ + _).print()
//        /*启动服务*/
//        ssc.start()
//        /*等待服务结束*/
//        ssc.awaitTermination()
    }
}
