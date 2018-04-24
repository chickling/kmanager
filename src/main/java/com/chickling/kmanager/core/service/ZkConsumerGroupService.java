/**
 * 
 */
package com.chickling.kmanager.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.kafka.common.TopicPartition;

import com.chickling.kmanager.utils.ZKUtils;

import kafka.common.TopicAndPartition;
import kafka.utils.ZkUtils;
import scala.collection.JavaConversions;

/**
 * @author Hulva Luva.H
 * @since 2018年4月23日
 * @desc appending....
 */
@Deprecated
public class ZkConsumerGroupService extends ConsumerGroupService {

  public ZkConsumerGroupService(String zkUrl) {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.chickling.kmanager.core.service.ConsumerGroupService#listGroups()
   */
  @Override
  public List<String> listGroups() {
    return JavaConversions.seqAsJavaList(ZKUtils.getZKUtilsFromKafka().getConsumerGroups().toList());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.chickling.kmanager.core.service.ConsumerGroupService#close()
   */
  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.chickling.kmanager.core.service.ConsumerGroupService#
   * collectGroupAssignment(java.lang.String)
   */
  @Override
  protected Map<String, List<PartitionAssignmentState>> collectGroupAssignment(String group) {
    int channelSocketTimeoutMs = Integer.parseInt(this.props.getProperty("channelSocketTimeoutMs", "600"));
    int channelRetryBackoffMs = Integer.parseInt(this.props.getProperty("channelRetryBackoffMsOpt", "300"));
    if (!this.listGroups().contains(group))
      return new HashMap<String, List<PartitionAssignmentState>>();

    List<String> topics = JavaConversions.seqAsJavaList(ZKUtils.getZKUtilsFromKafka().getTopicsByConsumerGroup(group));
    List<TopicAndPartition> topicPartitions = getAllTopicPartitions(topics);
    List<String> groupConsumerIds = JavaConversions.seqAsJavaList(ZKUtils.getZKUtilsFromKafka().getConsumersInGroup(group));

    // mapping of topic partition -> consumer id
    Map<TopicAndPartition, String> consumerIdByTopicPartition = new HashMap<>();
    topicPartitions.forEach(topicPartition -> {
      String owner = ZKUtils.readDataMaybeNull(
          ZkUtils.ConsumersPath() + "/" + topicPartition.topic() + "/owners/" + topicPartition.topic() + "/" + topicPartition.partition())
          .getData();
      consumerIdByTopicPartition.put(topicPartition, owner);
    });

    return null;
  }

  private List<TopicAndPartition> getAllTopicPartitions(List<String> topics) {
    Map<String, List<Integer>> topicPartitionMap = ZKUtils.getPartitionsForTopics(topics);
    return topics.stream().flatMap(topic -> {
      List<Integer> partitions = topicPartitionMap.getOrDefault(topic, new ArrayList<Integer>());
      return partitions.stream().map(partition -> new TopicAndPartition(topic, partition));
    }).collect(Collectors.toList());
  }

  @Override
  protected Optional<Long> getLogEndOffset(String group, TopicPartition topicPartition) {
    // TODO Auto-generated method stub
    return null;
  }

}
