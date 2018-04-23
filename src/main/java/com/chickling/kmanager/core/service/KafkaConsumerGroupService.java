/**
 * 
 */
package com.chickling.kmanager.core.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import kafka.admin.AdminClient;
import kafka.admin.AdminClient.ConsumerGroupSummary;
import kafka.admin.AdminClient.ConsumerSummary;
import kafka.common.TopicAndPartition;
import scala.collection.JavaConversions;

/**
 * @author Hulva Luva.H
 * @since 2018年4月23日
 */
public class KafkaConsumerGroupService extends ConsumerGroupService {

	private AdminClient adminClient;

	Map<String, KafkaConsumer<String, String>> consumerMap = new HashMap<String, KafkaConsumer<String, String>>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.chickling.kmanager.core.service.ConsumerGroupService#listGroups()
	 */
	@Override
	public List<String> listGroups() {
		return JavaConversions.seqAsJavaList(this.getAdminClient().listAllConsumerGroupsFlattened()).stream()
				.map(groupOverview -> groupOverview.groupId()).collect(Collectors.toList());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.chickling.kmanager.core.service.ConsumerGroupService#close()
	 */
	@Override
	public void close() {
		this.adminClient.close();
		if (this.consumerMap != null && !this.consumerMap.isEmpty()) {
			this.consumerMap.values().forEach(consumer -> consumer.close());
			this.consumerMap.clear();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.chickling.kmanager.core.service.ConsumerGroupService#
	 * collectGroupAssignment(java.lang.String)
	 */
	@Override
	protected Map<String, List<PartitionAssignmentState>> collectGroupAssignment(String group) {
		Map<String, List<PartitionAssignmentState>> ret = new HashMap<String, List<PartitionAssignmentState>>();

		ConsumerGroupSummary consumerGroupSummary = this.getAdminClient().describeConsumerGroup(group);
		consumerGroupSummary.state(); // "Dead"
		List<ConsumerSummary> consumerSummarys = JavaConversions.seqAsJavaList(consumerGroupSummary.consumers().get());
		List<TopicPartition> assignedTopicPartitions = new ArrayList<>();
		// Map<TopicPartition, Long>
		Map<TopicPartition, Object> offsets = JavaConversions
				.mapAsJavaMap(this.getAdminClient().listGroupOffsets(group));
		List<PartitionAssignmentState> rowsWithConsumer = new ArrayList<PartitionAssignmentState>();
		if (!offsets.isEmpty()) {
			if (consumerSummarys.size() > 1) {
				Collections.sort(consumerSummarys, new Comparator<ConsumerSummary>() {

					@Override
					public int compare(ConsumerSummary cs1, ConsumerSummary cs2) {
						return cs1.assignment().size() > cs2.assignment().size() ? 1
								: (cs1.assignment().size() < cs2.assignment().size() ? -1 : 0);
					}
				});
			}
			consumerSummarys.forEach(consumerSummary -> {
				List<TopicAndPartition> topicPartitions = JavaConversions.seqAsJavaList(consumerSummary.assignment())
						.stream().map(topicPartition -> new TopicAndPartition(topicPartition))
						.collect(Collectors.toList());
				assignedTopicPartitions.addAll(JavaConversions.seqAsJavaList(consumerSummary.assignment()));

				rowsWithConsumer.addAll(this.collectConsumerAssignment(group,
						Optional.ofNullable(consumerGroupSummary.coordinator()), topicPartitions, new MyFunctions() {

							@Override
							public Map<TopicAndPartition, Optional<Long>> getPartitionOffset(
									TopicAndPartition topicAndPartition) {
								Map<TopicAndPartition, Optional<Long>> partitionOffsets = new HashMap<>();
								JavaConversions.seqAsJavaList(consumerSummary.assignment()).forEach(topicPartition -> {
									partitionOffsets.put(new TopicAndPartition(topicPartition),
											Optional.ofNullable((Long) offsets.get(topicPartition)));
								});
								return partitionOffsets;
							}

						}, Optional.ofNullable(consumerSummary.consumerId()),
						Optional.ofNullable(consumerSummary.host()), Optional.ofNullable(consumerSummary.clientId())));
			});
		}

		List<PartitionAssignmentState> rowsWithoutConsumer = new ArrayList<PartitionAssignmentState>();
		TopicAndPartition topicAndPartition = null;
		for (Entry<TopicPartition, Object> entry : offsets.entrySet()) {
			if (assignedTopicPartitions.contains(entry.getKey())) {
				topicAndPartition = new TopicAndPartition(entry.getKey());
				this.collectConsumerAssignment(group, Optional.ofNullable(consumerGroupSummary.coordinator()),
						Arrays.asList(topicAndPartition), new MyFunctions() {

							@Override
							public Map<TopicAndPartition, Optional<Long>> getPartitionOffset(
									TopicAndPartition topicAndPartition) {
								Map<TopicAndPartition, Optional<Long>> temp = new HashMap<TopicAndPartition, Optional<Long>>();
								temp.put(topicAndPartition, Optional.ofNullable((Long) entry.getValue()));
								return temp;
							}

						}, Optional.of(MISSING_COLUMN_VALUE), Optional.of(MISSING_COLUMN_VALUE),
						Optional.of(MISSING_COLUMN_VALUE));
			}
		}
		rowsWithConsumer.addAll(rowsWithoutConsumer);
		ret.put(consumerGroupSummary.state(), rowsWithConsumer);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.chickling.kmanager.core.service.ConsumerGroupService#getLogEndOffset(org.
	 * apache.kafka.common.TopicPartition)
	 */
	@Override
	protected Optional<Long> getLogEndOffset(String group, TopicPartition topicPartition) {
		KafkaConsumer<String, String> consumer = null;
		if (this.consumerMap.containsKey(group)) {
			consumer = this.consumerMap.get(group);
		} else {
			consumer = this.getConsumer(group);
			this.consumerMap.put(group, consumer);
		}
		consumer.assign(Arrays.asList(topicPartition));
		consumer.seekToEnd(Arrays.asList(topicPartition));
		long logEndOffset = consumer.position(topicPartition);
		return Optional.ofNullable(logEndOffset);
	}

	private KafkaConsumer<String, String> getConsumer(String group) {
		return this.createNewConsumer(group);
	}

	private AdminClient getAdminClient() {
		if (this.adminClient == null)
			this.adminClient = this.createAdminClient();
		return this.adminClient;
	}

	public AdminClient createAdminClient() {
		Properties props = new Properties();
		props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, this.props.get("bootstrapServer"));
		return AdminClient.create(props);
	}

	private KafkaConsumer<String, String> createNewConsumer(String group) {
		Properties properties = new Properties();
		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.props.getProperty("bootstrapServer"));
		properties.put(ConsumerConfig.GROUP_ID_CONFIG, group);
		properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		return new KafkaConsumer<String, String>(properties);
	}
}
