/**
 * 
 */
package com.chickling.kmanager.test.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

/**
 * @author lvswe
 *
 */
public class ConsumerGroupExample {
  private final ConsumerConnector consumer;
  private final String topic;
  private ExecutorService executor;
  private String zookeeper;
  private String groupId;
  private String url;

  public ConsumerGroupExample(String zookeeper, String groupId, String topic, String url) {
    this.topic = topic;
    this.zookeeper = zookeeper;
    this.groupId = groupId;
    this.url = url;
    consumer = kafka.consumer.Consumer.createJavaConsumerConnector(new ConsumerConfig(createConsumerConfig()));
  }

  private Properties createConsumerConfig() {
    Properties props = new Properties();
    props.put("zookeeper.connect", this.zookeeper);
    props.put("group.id", this.groupId);
    props.put("auto.commit.enable", "true");
    props.put("auto.offset.reset", "smallest");
    props.put("offsets.storage", "kafka");
    // props.put("schema.registry.url", url);

    return props;
  }

  public void run(int numThreads) {
    Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
    topicCountMap.put(topic, numThreads);

    Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
    List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

    // Launch all the threads
    executor = Executors.newFixedThreadPool(streams.size());

    // Create ConsumerLogic objects and bind them to threads
    int threadNumber = 0;
    for (final KafkaStream<byte[], byte[]> stream : streams) {
      executor.submit(new ConsumerLogic(stream, threadNumber));
      threadNumber++;
    }
  }

  public void shutdown() {
    if (consumer != null) {
      consumer.shutdown();
    }
    if (executor != null) {
      executor.shutdown();
    }
    try {
      if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
        System.out.println("Timed out waiting for consumer threads to shut down, exiting uncleanly");
      }
    } catch (InterruptedException e) {
      System.out.println("Interrupted during shutdown, exiting uncleanly");
    }
  }

  public static void main(String[] args) {
    /*
     * if (args.length != 5) { System.out.println( "Please provide command line arguments: " +
     * "zookeeper groupId topic threads schemaRegistryUrl"); System.exit(-1); }
     */

    // String zooKeeper = "c7003.luva.h:2181,c7001.luva.h:2181,c7002.luva.h:2181";
    String zooKeeper = "localhost:2181";
    // String groupId = args[0];
    // String topic = args[1];
    String groupId = "luva";
    String topic = "test";
    int threads = 1;
    String url = "";

    ConsumerGroupExample example = new ConsumerGroupExample(zooKeeper, groupId, topic, url);
    example.run(threads);

    // try {
    // Thread.sleep(100000);
    // } catch (InterruptedException ie) {
    //
    // }
    // example.shutdown();
  }
}
