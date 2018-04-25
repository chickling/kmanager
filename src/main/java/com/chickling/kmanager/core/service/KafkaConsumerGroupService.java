/**
 * 
 */
package com.chickling.kmanager.core.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
//  private static Logger LOG = LoggerFactory.getLogger(ConsumerGroupService.class);

  private AdminClient adminClient;

  private Map<String, KafkaConsumer<String, String>> kmanagerLogEndOffsetGetter = new HashMap<>();;
  private Map<String, TopicPartition> nocurrentassignmentMap = new HashMap<String, TopicPartition>();

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
    if (this.kmanagerLogEndOffsetGetter != null) {
      Iterator<Entry<String, KafkaConsumer<String, String>>> ite = this.kmanagerLogEndOffsetGetter.entrySet().iterator();
      while (ite.hasNext()) {
        ite.next().getValue().close();
      }
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
    Map<TopicPartition, Object> offsets = JavaConversions.mapAsJavaMap(this.getAdminClient().listGroupOffsets(group));
    List<PartitionAssignmentState> rowsWithConsumer = new ArrayList<PartitionAssignmentState>();
    if (!offsets.isEmpty()) {
      consumerSummarys.forEach(consumerSummary -> {
        List<TopicAndPartition> topicPartitions = JavaConversions.seqAsJavaList(consumerSummary.assignment()).stream()
            .map(topicPartition -> new TopicAndPartition(topicPartition)).collect(Collectors.toList());
        assignedTopicPartitions.addAll(JavaConversions.seqAsJavaList(consumerSummary.assignment()));

        rowsWithConsumer.addAll(this.collectConsumerAssignment(group, Optional.ofNullable(consumerGroupSummary.coordinator()),
            topicPartitions, new MyFunctions() {

              @Override
              public Map<TopicAndPartition, Optional<Long>> getPartitionOffset(TopicAndPartition topicAndPartition) {
                Map<TopicAndPartition, Optional<Long>> partitionOffsets = new HashMap<>();
                JavaConversions.seqAsJavaList(consumerSummary.assignment()).forEach(topicPartition -> {
                  partitionOffsets.put(new TopicAndPartition(topicPartition), Optional.ofNullable((Long) offsets.get(topicPartition)));
                });
                return partitionOffsets;
              }

            }, Optional.ofNullable(consumerSummary.consumerId()), Optional.ofNullable(consumerSummary.host()),
            Optional.ofNullable(consumerSummary.clientId())));
      });
    }

    List<PartitionAssignmentState> rowsWithoutConsumer = new ArrayList<PartitionAssignmentState>();
    TopicAndPartition topicAndPartition = null;
    for (Entry<TopicPartition, Object> entry : offsets.entrySet()) {
      if (assignedTopicPartitions.contains(entry.getKey())) {
        topicAndPartition = new TopicAndPartition(entry.getKey());
        this.collectConsumerAssignment(group, Optional.ofNullable(consumerGroupSummary.coordinator()), Arrays.asList(topicAndPartition),
            new MyFunctions() {

              @Override
              public Map<TopicAndPartition, Optional<Long>> getPartitionOffset(TopicAndPartition topicAndPartition) {
                Map<TopicAndPartition, Optional<Long>> temp = new HashMap<TopicAndPartition, Optional<Long>>();
                temp.put(topicAndPartition, Optional.ofNullable((Long) entry.getValue()));
                return temp;
              }

            }, Optional.of(MISSING_COLUMN_VALUE), Optional.of(MISSING_COLUMN_VALUE), Optional.of(MISSING_COLUMN_VALUE));
      }
    }
    rowsWithConsumer.addAll(rowsWithoutConsumer);
    ret.put(consumerGroupSummary.state(), rowsWithConsumer);
    return ret;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.chickling.kmanager.core.service.ConsumerGroupService#getLogEndOffset(org.
   * apache.kafka.common.TopicPartition)
   */
  @Override
  public Optional<Long> getLogEndOffset(String group, TopicPartition topicPartition) {
    long logEndOffset = -1;
    KafkaConsumer<String, String> consumer = this.getConsumer(group);
    try {
      consumer.assign(Arrays.asList(topicPartition));
      consumer.seekToEnd(Arrays.asList(topicPartition));
      logEndOffset = consumer.position(topicPartition);
    } catch (Exception e) {
      // No current assignment for partition
      if (this.nocurrentassignmentMap.size() == 100) {
        this.nocurrentassignmentMap.remove(this.nocurrentassignmentMap.keySet().iterator().next());
      }
      this.nocurrentassignmentMap.put(group, topicPartition);
    }
    return Optional.ofNullable(logEndOffset);
  }

  private KafkaConsumer<String, String> getConsumer(String group) {
    KafkaConsumer<String, String> consumer = null;
    if (this.kmanagerLogEndOffsetGetter.containsKey(group)) {
      consumer = this.kmanagerLogEndOffsetGetter.get(group);
    } else {
      consumer = this.createNewConsumer(group);
      this.kmanagerLogEndOffsetGetter.put(group, consumer);
    }
    return consumer;
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
    properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "60000");
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    return new KafkaConsumer<String, String>(properties);
  }

  public Map<String, TopicPartition> getNocurrentassignmentMap() {
    Map<String, TopicPartition> temp = new HashMap<String, TopicPartition>();
    temp.putAll(this.nocurrentassignmentMap);
    return temp;
  }
}
