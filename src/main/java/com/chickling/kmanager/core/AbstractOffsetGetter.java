package com.chickling.kmanager.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import com.chickling.kmanager.core.service.KafkaConsumerGroupService;
import com.chickling.kmanager.core.service.PartitionAssignmentState;
import com.chickling.kmanager.model.ConsumerDetail;
import com.chickling.kmanager.model.KafkaInfo;
import com.chickling.kmanager.model.Node;
import com.chickling.kmanager.model.OffsetInfo;
import com.chickling.kmanager.model.TopicAndConsumersDetails;
import com.chickling.kmanager.model.TopicDetails;
import com.chickling.kmanager.utils.ZKUtils;

import kafka.cluster.Broker;
import kafka.cluster.EndPoint;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.utils.ZkUtils;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.runtime.AbstractFunction0;

/**
 * @author Hulva Luva.H
 * @since 2017年6月15日
 *
 */
public abstract class AbstractOffsetGetter {

  static Logger LOG = LoggerFactory.getLogger(AbstractOffsetGetter.class);

  protected static boolean earliest = false;

  protected static final String clientId = "Kmanager";

  public KafkaConsumerGroupService kafkaConsumerGroupService = null;

  final AbstractFunction0<Integer> elseIntOption = new AbstractFunction0<Integer>() {
    @Override
    public Integer apply() {
      return -1;
    }
  };

  final AbstractFunction0<String> elseStringOption = new AbstractFunction0<String>() {
    @Override
    public String apply() {
      return "-";
    }
  };

  final AbstractFunction0<Long> elseLongOption = new AbstractFunction0<Long>() {
    @Override
    public Long apply() {
      return 0L;
    }
  };

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
   * @param bId broker id
   * @return SimpleConsumer
   */
  @Deprecated
  public SimpleConsumer getConsumer(int bId) {
    String jsonString = null;
    JSONObject brokerInfoJson = null;
    try {
      Tuple2<Option<String>, Stat> brokerInfo_stat = ZKUtils.getZKUtilsFromKafka().readDataMaybeNull(ZkUtils.BrokerIdsPath() + "/" + bId);
      if (brokerInfo_stat._1.isEmpty()) {
        throw new BrokerNotAvailableException("Broker id " + bId + " does not exist");
      }
      jsonString = brokerInfo_stat._1.get();
      brokerInfoJson = new JSONObject(jsonString);
    } catch (Exception e) {
      throw new BrokerNotAvailableException("Broker id:" + bId + " " + jsonString);
    }
    return new SimpleConsumer(brokerInfoJson.getString("host"), brokerInfoJson.getInt("port"), 10000, 100000, "ConsumerOffsetChecker");
  }

  public List<String> getGroupsCommittedToBroker() {
    return this.kafkaConsumerGroupService.listGroups();
  }

  protected List<OffsetInfo> getOffsetInfoCommittedToBroker(String group, List<String> topics) {
    List<OffsetInfo> offsets = new ArrayList<OffsetInfo>();
    Map<String, List<PartitionAssignmentState>> groupAssignment = this.kafkaConsumerGroupService.describeGroup(group);
    List<PartitionAssignmentState> partitionAssignments = null;
    // groupAssignment 这个 map 中只有一对数据
    for (Entry<String, List<PartitionAssignmentState>> entry : groupAssignment.entrySet()) {
      // TODO key -> consumerGroupSummary.state "Dead" "Empty" "PreparingRebalance" "Stable"
      partitionAssignments = entry.getValue();
    }
    if (partitionAssignments == null) {
      return offsets;
    }
    // System.out.println(group + " - " + groupAssignment._1().get());

    // System.out.println(String.format("\n%-30s %-10s %-15s %-15s %-10s %-50s",
    // "TOPIC", "PARTITION",
    // "CURRENT-OFFSET", "LOG-END-OFFSET", "LAG", "CONSUMER-ID"));

    partitionAssignments.forEach((partitionAssignment) -> {
      if (!topics.isEmpty()) {
        if (topics.contains(partitionAssignment.getTopic().orElse("-"))) {
          offsets.add(new OffsetInfo(group, (String) partitionAssignment.getTopic().orElse("-"),
              (Integer) partitionAssignment.getPartition().orElse(-1), (Long) partitionAssignment.getOffset().orElse(-1L),
              (Long) partitionAssignment.getLogEndOffset().orElse(-1L),
              partitionAssignment.getConsumerId().orElse("-") + partitionAssignment.getHost().orElse("-"), -1L, -1L,
              (Long) partitionAssignment.getLag().orElse(-1L), false));
        }
      } else {
        offsets.add(new OffsetInfo(group, (String) partitionAssignment.getTopic().orElse("-"),
            (Integer) partitionAssignment.getPartition().orElse(-1), (Long) partitionAssignment.getOffset().orElse(-1L),
            (Long) partitionAssignment.getLogEndOffset().orElse(-1L),
            partitionAssignment.getConsumerId().orElse("-") + partitionAssignment.getHost().orElse("-"), -1L, -1L,
            (Long) partitionAssignment.getLag().orElse(-1L), false));
      }
      // System.out.println(String.format("%-30s %-10s %-15s %-15s %-10s %-50s",
      // partitionAssignment.topic().get(),
      // partitionAssignment.partition().get(),
      // partitionAssignment.offset().get(),
      // partitionAssignment.logEndOffset().get(),
      // partitionAssignment.lag().get(),
      // partitionAssignment.consumerId().get()));
    });
    return offsets;
  }

  public List<OffsetInfo> processTopic(String group, String topic) throws Exception {
    List<String> partitionIds = null;
    try {
      partitionIds = JavaConversions
          .seqAsJavaList(ZKUtils.getZKUtilsFromKafka().getChildren(ZkUtils.BrokerTopicsPath() + "/" + topic + "/partitions"));
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

  public List<OffsetInfo> offsetInfo(String group, List<String> topics) throws Exception {
    List<OffsetInfo> offsetInfos = new ArrayList<OffsetInfo>();
    if (topics.isEmpty()) {
      topics = getTopicList(group);
    }
    for (String topic : topics) {
      List<OffsetInfo> offsetInfoList = processTopic(group, topic);
      if (!(offsetInfoList == null)) {
        offsetInfos.addAll(offsetInfoList);
      }
    }
    return offsetInfos;
  }

  // get information about a consumer group and the topics it consumes
  public KafkaInfo getInfo(String group, List<String> topics) throws Exception {
    List<OffsetInfo> offsetInfos = offsetInfo(group, topics);
    // for Broker
    offsetInfos.addAll(this.getOffsetInfoCommittedToBroker(group, topics));
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
    // return new KafkaInfo(group, brokerInfo(), offsetInfos);
    return new KafkaInfo(group, null, offsetInfos);
  }

  /**
   * 获取指定ZK或者broker信息，将两个信息分离
   * 
   * @param group
   * @param topics
   * @param belongZK
   * @return
   * @throws Exception
   */
  public KafkaInfo getInfo(String group, List<String> topics, boolean belongZK) throws Exception {
    List<OffsetInfo> offsetInfos = new ArrayList<OffsetInfo>();
    // for Broker
    if (!belongZK) {
      offsetInfos.addAll(this.getOffsetInfoCommittedToBroker(group, topics));
    } else {
      offsetInfos = offsetInfo(group, topics);
    }
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
    // return new KafkaInfo(group, brokerInfo(), offsetInfos);
    return new KafkaInfo(group, null, offsetInfos);
  }

  public List<String> getTopics() {
    List<String> topics = null;
    try {
      topics = JavaConversions.seqAsJavaList(ZKUtils.getZKUtilsFromKafka().getChildren(ZkUtils.BrokerTopicsPath()));
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
      childNodes.add(new Node(broker.id() + ":" + endPoints.get(0).host() + ":" + endPoints.get(0).port(), null));
    });
    rootNode.setChildren(childNodes);
    return rootNode;
  }

  /**
   * Returns details for a given topic such as the consumers pulling off of it
   * 
   * @return TopicDetails
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
   * Returns details for a given topic such as the active consumers pulling off of it and for each of
   * the active consumers it will return the consumer data
   * 
   * @throws Exception exception
   */
  public TopicAndConsumersDetails getTopicAndConsumersDetail(String topic) throws Exception {
    Map<String, List<String>> topicMap = getTopicMap(true);
    // Map<String, List<String>> activeTopicMap = getActiveTopicMap(true);
    Map<String, List<String>> topicMapCommittedToBroker = getTopicMapCommitedToKafka();

    List<KafkaInfo> consumersCommittedToZK = new ArrayList<KafkaInfo>();
    if (topicMap.containsKey(topic)) {
      consumersCommittedToZK = mapConsumersToKafkaInfo(topicMap.get(topic), topic, true);
    }
    if (consumersCommittedToZK != null) {
      Collections.sort(consumersCommittedToZK);
    }

    List<KafkaInfo> consumersCommittedToBroker = new ArrayList<KafkaInfo>();
    if (topicMapCommittedToBroker.containsKey(topic)) {
      consumersCommittedToBroker = mapConsumersToKafkaInfo(topicMapCommittedToBroker.get(topic), topic, false);
    }
    if (consumersCommittedToBroker != null) {
      Collections.sort(consumersCommittedToBroker);
    }
    return new TopicAndConsumersDetails(consumersCommittedToZK, consumersCommittedToBroker);
  }

  public List<String> getActiveConsumer(String topic) throws Exception {
    Map<String, List<String>> activeTopicMap = getActiveTopicMap();

    List<String> activeConsumers = null;
    if (activeTopicMap.containsKey(topic)) {
      activeConsumers = activeTopicMap.get(topic);
    }
    return activeConsumers;
  }

  @SuppressWarnings("unused")
  private List<KafkaInfo> mapConsumersToKafkaInfo(List<String> consumers, String topic) throws Exception {
    List<KafkaInfo> kafkaInfos = new ArrayList<KafkaInfo>();
    List<String> topics = new ArrayList<String>();
    topics.add(topic);
    for (String consumer : consumers) {
      kafkaInfos.add(getInfo(consumer, topics));
    }
    return kafkaInfos;
  }

  private List<KafkaInfo> mapConsumersToKafkaInfo(List<String> consumers, String topic, boolean belongZK) throws Exception {
    List<KafkaInfo> kafkaInfos = new ArrayList<KafkaInfo>();
    List<String> topics = new ArrayList<String>();
    topics.add(topic);
    for (String consumer : consumers) {
      kafkaInfos.add(getInfo(consumer, topics, belongZK));
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
    if (this.kafkaConsumerGroupService != null) {
      this.kafkaConsumerGroupService.close();
    }
  }

  public abstract Map<String, List<String>> getTopicMap();

  public abstract Map<String, List<String>> getTopicMap(boolean belongZK);

  public abstract Map<String, List<String>> getActiveTopicMap();

  public abstract Map<String, List<String>> getActiveTopicMap(boolean belongZK);

  public abstract List<String> getTopicList(String group);

  public abstract OffsetInfo processPartition(String group, String topic, String partitionId);

  public abstract List<String> getGroups();

  public abstract Map<String, List<String>> getTopicMapCommitedToKafka();

}
