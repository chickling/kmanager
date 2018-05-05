/**
 * 
 */
package com.chickling.kmanager.test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.json.JSONObject;

import kafka.common.OffsetAndMetadata;
import kafka.coordinator.BaseKey;
import kafka.coordinator.GroupMetadataManager;
import kafka.coordinator.OffsetKey;

/**
 * @author Hulva Luva.H
 * @since 2018年4月21日
 *
 */
public class OffsetFetchTest {
	private final static String OFFSET_TOPIC = "__consumer_offsets";
	private final static String BOOTSTRAP_SERVERS = "localhost:9092";
	private final static String OUTPUT_TOPIC = "__consumer_offsets_output";
	private final static Set<String> EXCEPT_TOPIC = new HashSet<String>();

	private final static boolean ENABLE_INCLUDE = true;
	private final static Set<String> INCLUDE_GROUP = new HashSet<String>();

	static {
		EXCEPT_TOPIC.add(OFFSET_TOPIC);
		EXCEPT_TOPIC.add(OUTPUT_TOPIC);

		INCLUDE_GROUP.addAll(Arrays.asList("BI_EC_Pf_Newegg_com_adobe_products", "BI_EC_Pf_Newegg_com_adobe_others",
				"BI_EC_Pf_Newegg_com_adobe_allpage1", "I_EC_Pf_Newegg_com_adobe_middle_layer",
				"BI_EC_Pf_Newegg_com_adobe_hbaserawdata", "BI_EC_Pf_Newegg_com_adobe_COUNT"));
	}

	public static void main(String[] args) {
		// DefaultOffsetManager
		// ZookeeperOffsetManager
		Properties props = new Properties();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "consumer-offsets-consumer-app");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass());
		props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass());
		props.put("exclude.internal.topics", "false"); // necessary to consume __consumer_offsets

		KStreamBuilder builder = new KStreamBuilder();
		builder.stream(Serdes.ByteArray(), Serdes.ByteArray(), OFFSET_TOPIC)
				// @formatter:off
				.map((k, v) -> new KeyValue<BaseKey, byte[]>(GroupMetadataManager.readMessageKey(ByteBuffer.wrap(k)),
						v))
				.filter((k, v) -> {
					if (k instanceof OffsetKey) {
						OffsetKey offsetKey = (OffsetKey) k;
						// System.out.println(offsetKey.key().topicPartition().topic());
						return !EXCEPT_TOPIC.contains(offsetKey.key().topicPartition().topic())
								&& (ENABLE_INCLUDE ? INCLUDE_GROUP.contains(offsetKey.key().group()) : true);
					}
					// if (k instanceof GroupMetadataKey) {
					// GroupMetadataKey key = (GroupMetadataKey) k;
					// System.out.println(key.toString());
					// }
					return false;
				})
				.map((k, v) -> new KeyValue<OffsetKey, OffsetAndMetadata>((OffsetKey) k,
						/* v 有可能是 null */ v == null ? null
								: GroupMetadataManager.readOffsetMessageValue(ByteBuffer.wrap(v))))
				// .to(OUTPUT_TOPIC)
				.foreach((key, offsetAndMetadata) -> {
					JSONObject json = new JSONObject();
					json.put("group", key.key().group());
					json.put("topic", key.key().topicPartition().topic());
					json.put("partition", key.key().topicPartition().partition());
					json.put("version", key.version());
					json.put("offset", offsetAndMetadata.offset());
					json.put("commitTimestamp", offsetAndMetadata.commitTimestamp());
					json.put("expireTimestamp", offsetAndMetadata.expireTimestamp());
					json.put("metadata", offsetAndMetadata.metadata());
					System.out.println(json.toString());
				});
		// @formatter:on

		KafkaStreams streams = new KafkaStreams(builder, props);
		streams.start();
		// streams.close();
	}

}
