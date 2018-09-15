package com.chickling.kmanager.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.apache.kafka.common.TopicPartition;
import org.apache.zookeeper.data.Stat;

import com.chickling.kmanager.config.AppConfig;
import com.chickling.kmanager.core.service.KafkaConsumerGroupService;
import com.chickling.kmanager.initialize.SystemManager;
import com.chickling.kmanager.model.OffsetInfo;
import com.chickling.kmanager.model.ZkDataAndStat;
import com.chickling.kmanager.utils.ZKUtils;

import kafka.consumer.ConsumerThreadId;
import kafka.utils.ZkUtils;
import scala.Tuple2;
import scala.collection.JavaConversions;

/**
 * @author Hulva Luva.H from ECBD
 * @since 2017年6月15日
 *
 */
public class CombinedOffsetGetter extends AbstractOffsetGetter {

  private static Long excludeByLastSeen = 604_800_000L;

  public CombinedOffsetGetter(AppConfig config) {
    excludeByLastSeen = config.getExcludeByLastSeen() * 1000;
    ZKUtils.init(config.getZkHosts(), config.getZkSessionTimeout(), config.getZkConnectionTimeout());
    this.kafkaConsumerGroupService = new KafkaConsumerGroupService();
    Properties props = new Properties();
    props.setProperty("bootstrapServer", config.getBootstrapServers());
    this.kafkaConsumerGroupService.setProperties(props);
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
    List<String> groups = SystemManager.og.getGroups();
    groups.addAll(SystemManager.og.getGroupsCommittedToBroker());
    List<String> topics = null;
    for (String group : groups) {
      topics = getTopicList(group);
      topics.forEach(topic -> {
        List<String> nestedGroups = null;
        if (topicGroupsMap.containsKey(topic)) {
          nestedGroups = topicGroupsMap.get(topic);
          nestedGroups.add(group);
        } else {
          nestedGroups = new ArrayList<String>();
          nestedGroups.add(group);
        }
        topicGroupsMap.put(topic, nestedGroups);
      });
    }
    return topicGroupsMap;
  }

  @Override
  public Map<String, List<String>> getTopicMap(boolean belongZK) {
    Map<String, List<String>> topicGroupsMap = new HashMap<String, List<String>>();
    List<String> groups = new ArrayList<String>();
    if (belongZK) {
      groups = SystemManager.og.getGroups();
      List<String> topics = null;
      for (String group : groups) {
        topics = getTopicList(group);
        topics.forEach(topic -> {
          List<String> nestedGroups = null;
          if (topicGroupsMap.containsKey(topic)) {
            nestedGroups = topicGroupsMap.get(topic);
            nestedGroups.add(group);
          } else {
            nestedGroups = new ArrayList<String>();
            nestedGroups.add(group);
          }
          topicGroupsMap.put(topic, nestedGroups);
        });
      }
    } else {
      groups.addAll(SystemManager.og.getGroupsCommittedToBroker());
    }

    return topicGroupsMap;
  }

  public Set<String> getTopicsForGroupCommittedToKafka(String group) {
    // "Dead"
    // "Empty"
    // "PreparingRebalance"
    // "Stable"
    return this.kafkaConsumerGroupService
        .describeGroup(group).entrySet().stream().map(entry -> entry.getValue()).map(partitionAssignmentList -> partitionAssignmentList
            .stream().map(partitionAssignment -> partitionAssignment.getTopic().get()).collect(Collectors.toList()))
        .flatMap(List::stream).collect(Collectors.toSet());

  }

  @Override
  public Map<String, List<String>> getActiveTopicMap(boolean belongZK) {
    Map<String, List<String>> topicGroupsMap = new HashMap<String, List<String>>();

    // Consumers committed offsets to Zk
    if (belongZK) {
      List<String> consumers = ZKUtils.getChildren(ZkUtils.ConsumersPath());
      for (String consumer : consumers) {
        Map<String, scala.collection.immutable.List<ConsumerThreadId>> consumer_consumerThreadId = null;
        try {
          consumer_consumerThreadId = JavaConversions.mapAsJavaMap(ZKUtils.getZKUtilsFromKafka().getConsumersPerTopic(consumer, true));
        } catch (Exception e) {
          LOG.warn("getActiveTopicMap-> getConsumersPerTopic for group: " + consumer + "failed! " + e.getMessage());
          // TODO /consumers/{group}/ids/{id} 节点的内容不符合要求。这个group有问题
          continue;
        }
        Set<String> topics = consumer_consumerThreadId.keySet();
        topics.forEach(topic -> {
          List<String> nestedGroups = null;
          if (topicGroupsMap.containsKey(topic)) {
            nestedGroups = topicGroupsMap.get(topic);
            nestedGroups.add(consumer);
          } else {
            nestedGroups = new ArrayList<String>();
            nestedGroups.add(consumer);
          }
          topicGroupsMap.put(topic, nestedGroups);
        });
      }
    } else {
      // TODO Consumers committed offsets to Kafka that is Active
    }

    return topicGroupsMap;
  }

  @Override
  public Map<String, List<String>> getActiveTopicMap() {
    Map<String, List<String>> topicGroupsMap = new HashMap<String, List<String>>();
    // Consumers committed offsets to Zk
    List<String> consumers = ZKUtils.getChildren(ZkUtils.ConsumersPath());
    for (String consumer : consumers) {
      Map<String, scala.collection.immutable.List<ConsumerThreadId>> consumer_consumerThreadId = null;
      try {
        consumer_consumerThreadId = JavaConversions.mapAsJavaMap(ZKUtils.getZKUtilsFromKafka().getConsumersPerTopic(consumer, true));
      } catch (Exception e) {
        LOG.warn("getActiveTopicMap-> getConsumersPerTopic for group: " + consumer + "failed! " + e.getMessage());
        // TODO /consumers/{group}/ids/{id} 节点的内容不符合要求。这个group有问题
        continue;
      }
      Set<String> topics = consumer_consumerThreadId.keySet();
      topics.forEach(topic -> {
        List<String> nestedGroups = null;
        if (topicGroupsMap.containsKey(topic)) {
          nestedGroups = topicGroupsMap.get(topic);
          nestedGroups.add(consumer);
        } else {
          nestedGroups = new ArrayList<String>();
          nestedGroups.add(consumer);
        }
        topicGroupsMap.put(topic, nestedGroups);
      });
    }
    // TODO Consumers committed offsets to Kafka that is Active
    // this.kafkaConsumerGroupService.listGroups()
    return topicGroupsMap;
  }

  @Override
  public Map<String, List<String>> getTopicMapCommitedToKafka() {
    Map<String, List<String>> topicGroupsMap = new HashMap<String, List<String>>();
    List<String> consumers = this.getGroupsCommittedToBroker();
    for (String consumer : consumers) {
      Set<String> topics = this.getTopicsForGroupCommittedToKafka(consumer);
      topics.forEach(topic -> {
        List<String> nestedGroups = null;
        if (topicGroupsMap.containsKey(topic)) {
          nestedGroups = topicGroupsMap.get(topic);
          nestedGroups.add(consumer);
        } else {
          nestedGroups = new ArrayList<String>();
          nestedGroups.add(consumer);
        }
        topicGroupsMap.put(topic, nestedGroups);
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
    Tuple2<String, Stat> offset_stat = readZkData(ZkUtils.ConsumersPath() + "/" + group + "/" + "offsets/" + topic + "/" + partitionId);
    if (offset_stat == null) {
      return null;
    }
    if (System.currentTimeMillis() - offset_stat._2().getMtime() > excludeByLastSeen) {
      // TODO 对于最后一次消费时间为一周前的，直接抛弃。是否维护一个被排除的partition 列表？
      return null;
    }
    // offset
    ZkDataAndStat dataAndStat =
        ZKUtils.readDataMaybeNull(ZkUtils.ConsumersPath() + "/" + group + "/" + "owners/" + topic + "/" + partitionId);
    Optional<Long> logEndOffset =
        this.kafkaConsumerGroupService.getLogEndOffset(group, new TopicPartition(topic, Integer.parseInt(partitionId)));

    if (dataAndStat.getData() == null) { // Owner not available
      // TODO dataAndStat that partition may not have any owner
      offsetInfo = new OffsetInfo(group, topic, Integer.parseInt(partitionId), Long.parseLong(offset_stat._1()), logEndOffset.get(), "NA",
          offset_stat._2().getCtime(), offset_stat._2().getMtime());
    } else {
      offsetInfo = new OffsetInfo(group, topic, Integer.parseInt(partitionId), Long.parseLong(offset_stat._1()), logEndOffset.get(),
          dataAndStat.getData(), offset_stat._2().getCtime(), offset_stat._2().getMtime());
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
