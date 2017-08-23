package com.chickling.kmanager.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.apache.kafka.common.errors.BrokerNotAvailableException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.data.Stat;

import com.chickling.kmanager.config.AppConfig;
import com.chickling.kmanager.model.OffsetInfo;
import com.chickling.kmanager.model.ZkDataAndStat;
import com.chickling.kmanager.utils.ZKUtils;

import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.TopicAndPartition;
import kafka.consumer.ConsumerThreadId;
import kafka.javaapi.OffsetRequest;
import kafka.javaapi.OffsetResponse;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.utils.ZkUtils;
import scala.Tuple2;
import scala.collection.JavaConversions;

/**
 * @author Hulva Luva.H from ECBD
 * @date 2017年6月15日
 * @description
 *
 */
public class ZKOffsetGetter extends OffsetGetter {

	private static Long excludeByLastSeen = 604_800_000L;

	public ZKOffsetGetter(AppConfig config) {
		excludeByLastSeen = config.getExcludeByLastSeen() * 1000;
		ZKUtils.init(config.getZkHosts(), config.getZkSessionTimeout(), config.getZkConnectionTimeout());
	}

	@Override
	public List<String> getGroups() {
		return ZKUtils.getChildren(ZkUtils.ConsumersPath());
	}

	public List<String> checkIfTopicExistsAndRemoveThatNot(List<String> topicsProvide, String group) {
		List<String> checkedTopic = new ArrayList<String>();
		List<String> allTopicsBelongTheGroup = getTopicList(group);
		topicsProvide.forEach(topic -> {
			if (allTopicsBelongTheGroup.contains(topic)) {
				checkedTopic.add(topic);
			}
		});
		return checkedTopic;
	}

	@Override
	public Map<String, List<String>> getTopicMap() {
		Map<String, List<String>> topicGroupsMap = new HashMap<String, List<String>>();
		List<String> groups = getGroups();
		List<String> topics = null;
		for (String group : groups) {
			topics = getTopicList(group);
			topics.forEach(topic -> {
				List<String> _groups = null;
				if (topicGroupsMap.containsKey(topic)) {
					_groups = topicGroupsMap.get(topic);
					_groups.add(group);
				} else {
					_groups = new ArrayList<String>();
					_groups.add(group);
				}
				topicGroupsMap.put(topic, _groups);
			});
		}
		return topicGroupsMap;
	}

	@Override
	public Map<String, List<String>> getActiveTopicMap() {
		Map<String, List<String>> topicGroupsMap = new HashMap<String, List<String>>();
		List<String> consumers = ZKUtils.getChildren(ZkUtils.ConsumersPath());
		for (String consumer : consumers) {
			Map<String, scala.collection.immutable.List<ConsumerThreadId>> consumer_consumerThreadId = null;
			try {
				consumer_consumerThreadId = JavaConversions
						.mapAsJavaMap(ZKUtils.getZKUtilsFromKafka().getConsumersPerTopic(consumer, true));
			} catch (Exception e) {
				LOG.warn("getActiveTopicMap-> getConsumersPerTopic for group: " + consumer + "failed! "
						+ e.getMessage());
				// TODO /consumers/{group}/ids/{id} 节点的内容不符合要求。这个group有问题
				continue;
			}
			Set<String> topics = consumer_consumerThreadId.keySet();
			topics.forEach(topic -> {
				List<String> _groups = null;
				if (topicGroupsMap.containsKey(topic)) {
					_groups = topicGroupsMap.get(topic);
					_groups.add(consumer);
				} else {
					_groups = new ArrayList<String>();
					_groups.add(consumer);
				}
				topicGroupsMap.put(topic, _groups);
			});
		}
		return topicGroupsMap;
	}

	@Override
	public List<String> getTopicList(String group) {
		return ZKUtils.getChildren(ZkUtils.ConsumersPath() + '/' + group + "/offsets");
	}

	@Override
	public OffsetInfo processPartition(String group, String topic, String partitionId) {
		OffsetInfo offsetInfo = null;
		Tuple2<String, Stat> offset_stat = readZkData(
				ZkUtils.ConsumersPath() + "/" + group + "/" + "offsets/" + topic + "/" + partitionId);
		if (offset_stat == null) {
			return null;
		}
		if (System.currentTimeMillis() - offset_stat._2().getMtime() > excludeByLastSeen) {
			// TODO 对于最后一次消费时间为一周前的，直接抛弃。是否维护一个被排除的partition 列表？
			return null;
		}
		ZkDataAndStat dataAndStat = ZKUtils
				.readDataMaybeNull(ZkUtils.ConsumersPath() + "/" + group + "/" + "owners/" + topic + "/" + partitionId);
		try {
			Integer leader = (Integer) ZKUtils.getZKUtilsFromKafka()
					.getLeaderForPartition(topic, Integer.parseInt(partitionId)).get();

			SimpleConsumer consumer = null;
			if (consumerMap.containsKey(leader)) {
				consumer = consumerMap.get(leader);
			} else {
				consumer = getConsumer(leader);
				consumerMap.put(leader, consumer);
			}

			TopicAndPartition topicAndPartition = new TopicAndPartition(topic, Integer.parseInt(partitionId));

			Map<TopicAndPartition, PartitionOffsetRequestInfo> tpMap = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
			tpMap.put(topicAndPartition, new PartitionOffsetRequestInfo(
					earliest ? kafka.api.OffsetRequest.EarliestTime() : kafka.api.OffsetRequest.LatestTime(), 1));
			OffsetRequest request = new OffsetRequest(tpMap, kafka.api.OffsetRequest.CurrentVersion(),
					String.format("%s_%s_%s", topic, partitionId, clientId));
			OffsetResponse response = null;
			response = consumer.getOffsetsBefore(request);
			if (response.hasError()) {
				LOG.error("error fetching data Offset from the Broker {}. reason: {}", leader,
						response.errorCode(topic, Integer.parseInt(partitionId)));
				throw new RuntimeException("fetching offset error!");
			}
			long[] offsets = response.offsets(topic, Integer.parseInt(partitionId));

			if (dataAndStat.getData() == null) { // Owner not available
				// TODO dataAndStat that partition may not have any owner
				offsetInfo = new OffsetInfo(group, topic, Integer.parseInt(partitionId),
						Long.parseLong(offset_stat._1()), offsets[offsets.length - 1], "NA",
						offset_stat._2().getCtime(), offset_stat._2().getMtime());
			} else {
				offsetInfo = new OffsetInfo(group, topic, Integer.parseInt(partitionId),
						Long.parseLong(offset_stat._1()), offsets[offsets.length - 1], dataAndStat.getData(),
						offset_stat._2().getCtime(), offset_stat._2().getMtime());
			}
		} catch (Exception e) {

			if (e instanceof ZkNoNodeException) {

			} else if (e instanceof NoNodeException) {
				// TODO dataAndStat that partition may not have any owner

			} else if (e instanceof BrokerNotAvailableException) {
				// TODO broker id -1 ? Alerting???
				LOG.warn(String.format("Get leader partition for [group: %s, topic: %s, partition: %s] faild!", group,
						topic, partitionId), e.getMessage());
				if (dataAndStat.getData() == null) { // Owner not available
					// TODO dataAndStat that partition may not have any owner
					offsetInfo = new OffsetInfo(group, topic, Integer.parseInt(partitionId),
							Long.parseLong(offset_stat._1()), -1l, "NA", offset_stat._2().getCtime(),
							offset_stat._2().getMtime());
				} else {
					offsetInfo = new OffsetInfo(group, topic, Integer.parseInt(partitionId),
							Long.parseLong(offset_stat._1()), -1l, dataAndStat.getData(), offset_stat._2().getCtime(),
							offset_stat._2().getMtime());
				}
			} else {
				throw new RuntimeException("Something went wrong!" + e);
			}
		}
		return offsetInfo;
	}

	private Tuple2<String, Stat> readZkData(String path) {
		Tuple2<String, Stat> offset_stat = null;
		try {
			offset_stat = ZKUtils.getZKUtilsFromKafka().readData(path);
		} catch (ZkNoNodeException znne) {
			// TODO no offset record in zk?
		}
		return offset_stat;
	}

	@Override
	public void close() {
		ZKUtils.close();
		super.close();
	}

}
