package com.chickling.kmonitor.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.kafka.common.errors.BrokerNotAvailableException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.data.Stat;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chickling.kmonitor.model.BrokerInfo;
import com.chickling.kmonitor.model.ConsumerDetail;
import com.chickling.kmonitor.model.KafkaInfo;
import com.chickling.kmonitor.model.Node;
import com.chickling.kmonitor.model.OffsetInfo;
import com.chickling.kmonitor.model.TopicAndConsumersDetails;
import com.chickling.kmonitor.model.TopicDetails;
import com.chickling.kmonitor.utils.ZKUtils;

import kafka.cluster.Broker;
import kafka.cluster.EndPoint;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.utils.ZkUtils;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConversions;

/**
 * @author Hulva Luva.H from ECBD
 * @date 2017年6月15日
 * @description
 *
 */
public abstract class OffsetGetter {

	static Logger LOG = LoggerFactory.getLogger(OffsetGetter.class);

	Map<Integer, SimpleConsumer> consumerMap = new HashMap<Integer, SimpleConsumer>();

	protected static boolean earliest = false;

	protected static final String clientId = "kafkaMonitor";

	/**
	 * <code>
	 * 			{
				    "listener_security_protocol_map": {
				        "PLAINTEXT": "PLAINTEXT"
				    },
				    "endpoints": [
				        "PLAINTEXT://10.16.238.94:8092"
				    ],
				    "jmx_port": 8888,
				    "host": "10.16.238.94",
				    "timestamp": "1497505727915",
				    "port": 8092,
				    "version": 4
				}
	 * </code>
	 * 
	 * @param bId
	 * @return
	 */
	public SimpleConsumer getConsumer(int bId) {
		String jsonString = null;
		JSONObject brokerInfoJson = null;
		try {
			Tuple2<Option<String>, Stat> brokerInfo_stat = ZKUtils.getZKUtilsFromKafka()
					.readDataMaybeNull(ZkUtils.BrokerIdsPath() + "/" + bId);
			if (brokerInfo_stat._1.isEmpty()) {
				throw new BrokerNotAvailableException("Broker id " + bId + " does not exist");
			}
			jsonString = brokerInfo_stat._1.get();
			brokerInfoJson = new JSONObject(jsonString);
		} catch (Exception e) {
			throw new BrokerNotAvailableException("Broker id:" + bId + " " + jsonString);
		}
		return new SimpleConsumer(brokerInfoJson.getString("host"), brokerInfoJson.getInt("port"), 10000, 100000,
				"ConsumerOffsetChecker");
	}

	public List<OffsetInfo> processTopic(String group, String topic) throws Exception {
		List<String> partitionIds = null;
		try {
			partitionIds = JavaConversions.seqAsJavaList(ZKUtils.getZKUtilsFromKafka()
					.getChildren(ZkUtils.BrokerTopicsPath() + "/" + topic + "/partitions"));
		} catch (Exception e) {
			if (e instanceof NoNodeException) {
				LOG.warn("Is topic >" + topic + "< exists!", e);
				return null;
			}
		}
		List<OffsetInfo> offsetInfos = new ArrayList<OffsetInfo>();
		OffsetInfo offsetInfo = null;
		if (partitionIds == null) {
			// TODO that topic exists in consumer node but not in topics node?!
			return null;
		}

		for (String partitionId : partitionIds) {
			offsetInfo = processPartition(group, topic, partitionId);
			if (offsetInfo != null) {
				offsetInfos.add(offsetInfo);
			}
		}
		return offsetInfos;
	}

	public List<BrokerInfo> brokerInfo() {
		List<BrokerInfo> binfos = new ArrayList<BrokerInfo>();
		Set<Integer> bids = consumerMap.keySet();
		BrokerInfo binfo = null;
		SimpleConsumer consumer = null;
		for (int bid : bids) {
			consumer = consumerMap.get(bid);
			binfo = new BrokerInfo(bid, consumer.host(), consumer.port());
			binfos.add(binfo);
		}
		return binfos;

	}

	public List<OffsetInfo> offsetInfo(String group, List<String> topics) throws Exception {
		List<OffsetInfo> offsetInfos = new ArrayList<OffsetInfo>();
		if (topics.isEmpty()) {
			topics = getTopicList(group);
		}
		for (String topic : topics) {
			List<OffsetInfo> offsetInfoList = processTopic(group, topic);
			if (!(offsetInfoList == null))
				offsetInfos.addAll(offsetInfoList);
		}
		return offsetInfos;
	}

	// get information about a consumer group and the topics it consumes
	public KafkaInfo getInfo(String group, List<String> topics) throws Exception {
		List<OffsetInfo> offsetInfos = offsetInfo(group, topics);
		Collections.sort(offsetInfos, new Comparator<OffsetInfo>() {

			@Override
			public int compare(OffsetInfo o1, OffsetInfo o2) {
				int flag = o1.getTopic().compareTo(o2.getTopic());
				if (flag == 0) {
					return o1.getPartition().compareTo(o2.getPartition());
				} else {
					return flag;
				}
			}

		});
		return new KafkaInfo(group, brokerInfo(), offsetInfos);
	}

	public List<String> getTopics() {
		List<String> topics = null;
		try {
			topics = JavaConversions
					.seqAsJavaList(ZKUtils.getZKUtilsFromKafka().getChildren(ZkUtils.BrokerTopicsPath()));
		} catch (Exception e) {
			LOG.error("could not get topics because of " + e.getMessage(), e);
		}
		return topics;
	}

	public Node getClusterViz() {
		Node rootNode = new Node("KafkaCluster");
		List<Node> childNodes = new ArrayList<Node>();
		List<Broker> brokers = JavaConversions.seqAsJavaList(ZKUtils.getZKUtilsFromKafka().getAllBrokersInCluster());
		brokers.forEach(broker -> {
			List<EndPoint> endPoints = JavaConversions.seqAsJavaList(broker.endPoints().seq());
			childNodes.add(new Node(endPoints.get(0).host() + ":" + endPoints.get(0).port(), null));
		});
		rootNode.setChildren(childNodes);
		return rootNode;
	}

	/**
	 * Returns details for a given topic such as the consumers pulling off of it
	 * 
	 * @param topic
	 * @return
	 */
	public TopicDetails getTopicDetail(String topic) {
		Map<String, List<String>> topicMap = getActiveTopicMap();
		List<String> consumers = null;
		List<ConsumerDetail> consumerDetails = new ArrayList<ConsumerDetail>();
		if (topicMap.containsKey(topic)) {
			consumers = topicMap.get(topic);
			for (String consumer : consumers) {
				consumerDetails.add(new ConsumerDetail(consumer));
			}
		}
		return new TopicDetails(consumerDetails);
	}

	public List<ConsumerDetail> mapConsumerDetails(List<String> consumers) {
		List<ConsumerDetail> consumerDetails = new ArrayList<ConsumerDetail>();
		for (String consumer : consumers) {
			consumerDetails.add(new ConsumerDetail(consumer));
		}
		return consumerDetails;
	}

	/**
	 * Returns details for a given topic such as the active consumers pulling off of
	 * it and for each of the active consumers it will return the consumer data
	 * 
	 * @throws Exception
	 */
	public TopicAndConsumersDetails getTopicAndConsumersDetail(String topic) throws Exception {
		Map<String, List<String>> topicMap = getTopicMap();
		Map<String, List<String>> activeTopicMap = getActiveTopicMap();

		List<KafkaInfo> activeConsumers = new ArrayList<KafkaInfo>();
		if (activeTopicMap.containsKey(topic)) {
			activeConsumers = mapConsumersToKafkaInfo(activeTopicMap.get(topic), topic);
		}

		List<KafkaInfo> inActiveConsumers = new ArrayList<KafkaInfo>();
		if (!activeTopicMap.containsKey(topic) && topicMap.containsKey(topic)) {
			inActiveConsumers = mapConsumersToKafkaInfo(topicMap.get(topic), topic);
		}
		return new TopicAndConsumersDetails(activeConsumers, inActiveConsumers);
	}

	public List<String> getActiveConsumer(String topic) throws Exception {
		Map<String, List<String>> activeTopicMap = getActiveTopicMap();

		List<String> activeConsumers = null;
		if (activeTopicMap.containsKey(topic)) {
			activeConsumers = activeTopicMap.get(topic);
		}
		return activeConsumers;
	}

	private List<KafkaInfo> mapConsumersToKafkaInfo(List<String> consumers, String topic) throws Exception {
		List<KafkaInfo> kafkaInfos = new ArrayList<KafkaInfo>();
		List<String> topics = new ArrayList<String>();
		topics.add(topic);
		for (String consumer : consumers) {
			kafkaInfos.add(getInfo(consumer, topics));
		}
		return kafkaInfos;
	}

	public Node getActiveTopics() {
		Map<String, List<String>> activeTopicMap = getActiveTopicMap();
		List<Node> activeTopics = new ArrayList<Node>();
		List<Node> temp = null;
		Set<String> keys = activeTopicMap.keySet();
		for (String topic : keys) {
			List<String> consumers = activeTopicMap.get(topic);
			temp = new ArrayList<Node>();
			for (String consumer : consumers) {
				temp.add(new Node(consumer));
			}
			activeTopics.add(new Node(topic, temp));
		}
		return new Node("ActiveTopics", activeTopics);
	}

	public void close() {
		try {
			Iterator<Entry<Integer, SimpleConsumer>> it = consumerMap.entrySet().iterator();
			while (it.hasNext()) {
				LOG.debug("Closing consumer: " + it.next().getValue().clientId());
				it.next().getValue().close();
			}
		} catch (Exception e) {
			LOG.warn("Close SimpleConsumer: " + e.getMessage());
		}
	}

	public abstract Map<String, List<String>> getTopicMap();

	public abstract Map<String, List<String>> getActiveTopicMap();

	public abstract List<String> getTopicList(String group);

	public abstract OffsetInfo processPartition(String group, String topic, String partitionId);

	public abstract List<String> getGroups();

}
