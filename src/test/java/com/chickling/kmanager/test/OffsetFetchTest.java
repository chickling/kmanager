/**
 * 
 */
package com.chickling.kmanager.test;

import java.util.Properties;
import java.nio.ByteBuffer;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStreamBuilder;

import kafka.common.OffsetAndMetadata;
import kafka.coordinator.BaseKey;
import kafka.coordinator.GroupMetadataKey;
import kafka.coordinator.GroupMetadataManager;
import kafka.coordinator.OffsetKey;

/**
 * @author Hulva Luva.H
 * @since 2018年4月21日
 *
 */
public class OffsetFetchTest {
  private final static String OFFSET_TOPIC = "__consumer_offsets";
  private final static String BOOTSTRAP_SERVERS = "10.16.238.101:8092,10.16.238.102:8092";
  private final static String OUTPUT_TOPIC = "__consumer_offsets_output";

  public static void main(String[] args) {
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "consumer-offsets-consumer-app");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass());
    props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass());
    props.put("exclude.internal.topics", "false"); // necessary to consume __consumer_offsets

    KStreamBuilder builder = new KStreamBuilder();
    builder.stream(Serdes.ByteArray(), Serdes.ByteArray(), OFFSET_TOPIC)
        .map((k, v) -> new KeyValue<BaseKey, byte[]>(GroupMetadataManager.readMessageKey(ByteBuffer.wrap(k)), v))
        .filter((k, v) -> {
          if (k instanceof OffsetKey) {
            OffsetKey offsetKey = (OffsetKey) k;
            return offsetKey.key().topicPartition().topic() != OUTPUT_TOPIC;
          }
          // if (k instanceof GroupMetadataKey) {
          // GroupMetadataKey key = (GroupMetadataKey) k;
          // System.out.println(key.toString());
          // }
          return false;
        })
        .map((k, v) -> {
          System.out.println(k.toString());
          // v 有可能是 null
          if (v == null) {
            return new KeyValue<OffsetKey, OffsetAndMetadata>((OffsetKey) k, null);
          }
          return new KeyValue<OffsetKey, OffsetAndMetadata>((OffsetKey) k, GroupMetadataManager.readOffsetMessageValue(ByteBuffer.wrap(v)));
        })
        .foreach((key, offsetAndMetadata) -> {
          System.out.println(String.format("key: {}, offsetAndMetadata: {}", key, offsetAndMetadata == null ? null : offsetAndMetadata.toString()));
        });
    
    KafkaStreams streams = new KafkaStreams(builder, props);
    streams.start();
  }

}
