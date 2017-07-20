package com.chickling.kmonitor.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chickling.kmonitor.model.ZkDataAndStat;

import kafka.cluster.Broker;
import kafka.common.TopicAndPartition;
import kafka.utils.ZKStringSerializer;
import kafka.utils.ZkUtils;
import scala.Option;
import scala.collection.JavaConversions;

/**
 * @author Hulva Luva.H
 *
 */
public class ZKUtils {
	private static Logger LOG = LoggerFactory.getLogger(ZKUtils.class);

	private static ZkClient zkClient = null;
	private static ZkConnection zkConnection = null;
	private static ZkUtils zkUtilsFromKafka = null;

	public static void init(String zkHosts, int zkSessionTimeout, int zkConnectionTimeout) {
		try {
			LOG.debug("init ZKUtil with " + zkHosts + " - " + zkSessionTimeout + " - " + zkConnectionTimeout);
			if (zkConnection == null) {
				zkConnection = new ZkConnection(zkHosts);
			}
			if (zkClient == null) {
				zkClient = new ZkClient(zkConnection, zkConnectionTimeout, new ZkSerializer() {

					@Override
					public byte[] serialize(Object paramObject) throws ZkMarshallingError {
						return ZKStringSerializer.serialize(paramObject);
					}

					@Override
					public Object deserialize(byte[] paramArrayOfByte) throws ZkMarshallingError {
						return ZKStringSerializer.deserialize(paramArrayOfByte);
					}

				});
			}
			if (zkUtilsFromKafka == null) {
				zkUtilsFromKafka = new ZkUtils(zkClient, zkConnection, false);
			}
		} catch (Exception e) {
			throw new RuntimeException("Init ZKUtils failed! " + e.getMessage());
		}
	}

	public static ZkClient getZKClient() {
		return zkClient;
	}

	public static ZkUtils getZKUtilsFromKafka() {
		return zkUtilsFromKafka;
	}

	public static Set<TopicAndPartition> getAllPartitions() {
		return JavaConversions.setAsJavaSet(zkUtilsFromKafka.getAllPartitions());
	}

	public static Map<String, List<Integer>> getPartitionsForTopics(List<String> topics) {
		Map<String, List<Integer>> topicPartitions = new HashMap<String, List<Integer>>();
		Map<String, Map<Integer, List<Integer>>> ps4Topics = getPartitionAssignmentForTopics(topics);
		ps4Topics.forEach((topic, PartitionReplica) -> {
			List<Integer> partitions = new ArrayList<Integer>();
			PartitionReplica.forEach((partition, replicas) -> {
				partitions.add(partition);
			});
			Collections.sort(partitions, new Comparator<Integer>() {
				@Override
				public int compare(Integer pid1, Integer pid2) {
					return pid1 < pid2 ? 1 : -1;
				}
			});
			topicPartitions.put(topic, partitions);
		});
		return topicPartitions;

	}

	public static Map<String, Map<Integer, List<Integer>>> getPartitionAssignmentForTopics(List<String> topics) {
		Map<String, Map<Integer, List<Integer>>> ret = new HashMap<String, Map<Integer, List<Integer>>>();
		topics.forEach(topic -> {
			String data = readDataMaybeNull(ZkUtils.BrokerTopicsPath() + "/" + topic).getData();
			if (data == null) {
				ret.put(topic, null);
			} else {
				JSONObject json = new JSONObject(data).getJSONObject("partitions");
				Map<Integer, List<Integer>> replicaMap = new HashMap<Integer, List<Integer>>();
				for (String pid : json.keySet()) {
					JSONArray bidsJson = json.getJSONArray(pid);
					List<Integer> replicas = new ArrayList<Integer>();
					bidsJson.forEach(bid -> {
						replicas.add((Integer) bid);
					});
					replicaMap.put(Integer.parseInt(pid), replicas);
				}
				ret.put(topic, replicaMap);
			}
		});
		return ret;
	}

	public static List<String> getAllTopics() {
		return JavaConversions.seqAsJavaList(zkUtilsFromKafka.getAllTopics());
	}

	public static Option<Broker> getBrokerInfo(int brokerId) {
		// Option[Broker] = zkUtilsFromKafka.getBrokerInfo(zkClient, brokerId)
		return zkUtilsFromKafka.getBrokerInfo(brokerId);
	}

	public static List<String> getConsumersInGroup(String group) {
		return JavaConversions.seqAsJavaList(zkUtilsFromKafka.getConsumersInGroup(group));

	}

	public static List<String> parseTopicsData(String jsonData) {
		return JavaConversions.seqAsJavaList(ZkUtils.parseTopicsData(jsonData));
	}

	public static boolean pathExists(String path) {
		return zkUtilsFromKafka.pathExists(path);
	}

	public static List<String> getChildren(String path) {
		List<String> children = null;
		try {
			children = zkClient.getChildren(path);
		} catch (Exception e) { // that should be
								// {org.apache.zookeeper.KeeperException$NoNodeException}
			children = new ArrayList<String>();
		}
		return children;
	}

	public static ZkDataAndStat readDataMaybeNull(String path) {
		Stat stat = new Stat();
		String data = null;
		try {
			data = zkClient.readData(path, stat);
		} catch (Exception e) {
			LOG.warn("Path: " + path + " do not exits in ZK!" + e.getMessage());
		}
		return new ZkDataAndStat(data, stat);
	}

	public static List<String> getKafkaJMXHostsFromZookeeper() throws Exception {
		List<String> kafkaHosts = new ArrayList<String>();
		List<String> ids = getChildren("/brokers/ids");
		for (String id : ids) {
			try {
				String brokerInfo = new String(readDataMaybeNull("/brokers/ids/" + id).getData());
				JSONObject jsonObj = new JSONObject(brokerInfo);
				if (jsonObj.has("host")) {
					if (jsonObj.has("jmx_port")) {
						kafkaHosts.add(
								id + ":" + jsonObj.get("host").toString() + ":" + jsonObj.get("jmx_port").toString());
					}
				}
			} catch (Exception e) {
				LOG.error("Zookeeper borker getting exception {}", e);
			}
		}
		return kafkaHosts;
	}

	// TODO for those invalid consumer id content, is that a concerned part?
	// public static Map<String, List<ConsumerThreadId>> getConsumersPerTopic(String
	// group,
	// boolean excludeInternalTopics) {
	// List<String> consumers = getChildren(ZkUtils.ConsumersPath() + "/" + group +
	// "/ids");
	// Map<String, List<ConsumerThreadId>> consumersPerTopicMap = new
	// HashMap<String, List<ConsumerThreadId>>();
	// for (String consumer : consumers) {
	// TopicCount topicCount = constructTopicCount(group, consumer,
	// zkUtilsFromKafka, excludeInternalTopics);
	// }
	//
	// return null;
	//
	// }
	//
	// private static TopicCount constructTopicCount(String group, String
	// consumerId, ZkUtils zkUtilsFromKafka,
	// boolean excludeInternalTopics) {
	// ZkDataAndStat consumerData = readDataMaybeNull(
	// ZkUtils.ConsumersPath() + "/" + group + "/ids" + "/" + consumerId);
	// String topicCountString = consumerData.getData();
	// String subscriptionPattern = null;
	// Map<String, Integer> topMap = null;
	// //
	// {"version":1,"subscription":{"EC2_SHOPPINGCART":1},"pattern":"static","timestamp":"1498610861408"}
	// if (topicCountString != null) {
	// JSONObject topicCountJson = new JSONObject(topicCountString);
	//
	// }
	// return null;
	// }
	public static void close() {
		try {
			if (zkUtilsFromKafka != null) {
				zkUtilsFromKafka.close();
				zkUtilsFromKafka = null;
			}
			if (zkClient != null) {
				zkClient.close();
				zkClient = null;
			}
			if (zkConnection != null) {
				zkConnection.close();
				zkConnection = null;
			}
		} catch (InterruptedException e) {
			LOG.error("ZKUtils close() error! ", e);
		}
	}
}
