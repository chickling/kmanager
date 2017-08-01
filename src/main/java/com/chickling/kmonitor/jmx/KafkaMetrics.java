package com.chickling.kmonitor.jmx;

import java.util.List;
import java.util.Optional;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hulva Luva.H
 * @since 2017-07-11
 *
 */
public class KafkaMetrics {
  private static Logger LOG = LoggerFactory.getLogger(KafkaMetrics.class);

  // kafka.network:type=RequestMetrics,name=RequestsPerSec,request={Produce|FetchConsumer|FetchFollower}

  // java.lang
  public static final String OPRATING_SYSTEM = "java.lang:type=OperatingSystem";
  public static final String RUNTIME = "java.lang:type=Runtime";

  // kafka.cluster
  public static final String IN_SYNC_REPLICAS_COUNT = "kafka.cluster:type=Partition,name=InSyncReplicasCount,topic={},partition={}";
  public static final String REPLICAS_COUNT = "kafka.cluster:type=Partition,name=ReplicasCount,topic={},partition={}";
  public static final String UNDER_REPLICATED = "kafka.cluster:type=Partition,name=UnderReplicated,topic={},partition={}";

  // kafka.controller
  public static final String LEADER_ELECTION_RATE_AND_TIME_MS = "kafka.controller:type=ControllerStats,name=LeaderElectionRateAndTimeMs";
  public static final String UNCLEAN_LEADER_ELECTIONS_PER_SEC = "kafka.controller:type=ControllerStats,name=UncleanLeaderElectionsPerSec";
  public static final String ACTIVE_CONTROLLER_COUNT = "kafka.controller:type=KafkaController,name=ActiveControllerCount";
  public static final String OFFLINE_PARTITIONS_COUNT = "kafka.controller:type=KafkaController,name=OfflinePartitionsCount";
  public static final String PREFERED_REPLICA_IMBALANCE = "kafka.controller:type=KafkaController,name=PreferredReplicaImbalanceCount";

  // kafka.log
  public static final String LOG_END_OFFSET = "kafka.log:type=Log,name=LogEndOffset,topic={},partition={}";
  public static final String LOG_START_OFFSET = "kafka.log:type=Log,name=LogStartOffset,topic={},partition={}";
  public static final String LOG_SEGMENTS_NUM = "kafka.log:type=Log,name=NumLogSegments,topic={},partition={}";
  public static final String LOG_SIZE = "kafka.log:type=Log,name=Size,topic={},partition={}";
  public static final String CLEANER_RECOPY_PERCENT = "kafka.log:type=LogCleaner,name=cleaner-recopy-percent";
  public static final String MAX_BUFFER_UTILIZATION_PERCENT = "kafka.log:type=LogCleaner,name=max-buffer-utilization-percent";
  public static final String MAX_CLEAN_TIME_SECS = "kafka.log:type=LogCleaner,name=max-clean-time-secs";
  public static final String MAX_DIRTY_PERCENT = "kafka.log:type=LogCleanerManager,name=max-dirty-percent";
  public static final String TIME_SINCE_LAST_RUN_CLEANER_MS = "kafka.log:type=LogCleanerManager,name=time-since-last-run-ms";

  // kafka.network
  public static final String PROCESSOR_IDLEPERCENT = "kafka.network:type=Processor,name=IdlePercent,networkProcessor={}";
  public static final String REQUEST_QUEUE_SIZE = "kafka.network:type=RequestChannel,name=RequestQueueSize";
  public static final String RESPONSE_QUEUE_SIZE = "kafka.network:type=RequestChannel,name=ResponseQueueSize";
  public static final String RESPONSE_QUEUE_SIZE_OF_PROCESSOR = "kafka.network:type=RequestChannel,name=ResponseQueueSize,processor={}";

  public static final String REQUEST_METRICS = "kafka.network:type=RequestMetrics,name={},request= {}";
  public static final String REQUEST_METRICS_NAMES[] = {"LocalTimeMs", "RemoteTimeMs", "RequestQueueTimeMs", "RequestQueueTimeMs",
      "RequestsPerSec", "ResponseQueueTimeMs", "ResponseSendTimeMs", "ThrottleTimeMs", "TotalTimeMs"};
  public static final String REQUEST_METRICS_REQUESTS[] =
      {"ApiVersions", "ControlledShutdown", "CreateTopics", "DeleteTopics", "DescribeGroups", "Fetch", "FetchConsumer", "FetchFollower",
          "GroupCoordinator", "Heartbeat", "JoinGroup", "LeaderAndIsr", "LeaveGroup", "ListGroups", "Metadata", "OffsetCommit",
          "OffsetFetch", "Offsets", "Produce", "SaslHandshake", "StopReplica", "SyncGroup", "UpdateMetadata"};

  public static final String NETWORK_PROCESSOR_AVG_IDLE_PERCENT = "kafka.network:type=SocketServer,name=NetworkProcessorAvgIdlePercent";

  // kafka.server
  public static final String BROKER_BYTES_IN_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec";
  public static final String TOPIC_BYTES_IN_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec,topic={}";
  public static final String BROKER_BYTES_OUT_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec";
  public static final String TOPIC_BYTES_OUT_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec,topic={}";
  public static final String BROKER_BYTES_REJECTERD_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=BytesRejectedPerSec";
  public static final String TOPIC_BYTES_REJECTERD_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=BytesRejectedPerSec,topic={}";
  public static final String BROKER_FAILED_FETCH_REQUESTS_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=FailedFetchRequestsPerSec";
  public static final String TOPIC_FAILED_FETCH_REQUESTS_PER_SEC =
      "kafka.server:type=BrokerTopicMetrics,name=FailedFetchRequestsPerSec,topic={}";
  public static final String BROKER_FAILED_PRODUCE_REQUESTS_PER_SEC =
      "kafka.server:type=BrokerTopicMetrics,name=FailedProduceRequestsPerSec";
  public static final String TOPIC_FAILED_PRODUCE_REQUESTS_PER_SEC =
      "kafka.server:type=BrokerTopicMetrics,name=FailedProduceRequestsPerSec,topic={}";
  public static final String BROKER_MESSAGES_IN_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec";
  public static final String TOPIC_MESSAGES_IN_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec,topic={}";
  public static final String TOTAL_FETCH_REQUESTS_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=TotalFetchRequestsPerSec";
  public static final String TOTAL_PRODUCE_REQUESTS_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=TotalProduceRequestsPerSec";

  public static final String CONSUMER_EXPIRES_PER_SEC = "kafka.server:type=DelayedFetchMetrics,name=ExpiresPerSec,fetcherType=consumer";
  public static final String FOLLOWER_EXPIRES_PER_SEC = "kafka.server:type=DelayedFetchMetrics,name=ExpiresPerSec,fetcherType=follower";

  public static final String DELAYED_OPERATIONS_FETCH_NUM =
      "kafka.server:type=DelayedOperationPurgatory,name=NumDelayedOperations,delayedOperation=Fetch";
  public static final String DELAYED_OPERATIONS_HEARTBEAT_NUM =
      "kafka.server:type=DelayedOperationPurgatory,name=NumDelayedOperations,delayedOperation=Heartbeat";
  public static final String DELAYED_OPERATIONS_PRODUCE_NUM =
      "kafka.server:type=DelayedOperationPurgatory,name=NumDelayedOperations,delayedOperation=Produce";
  public static final String DELAYED_OPERATIONS_REBALANCE_NUM =
      "kafka.server:type=DelayedOperationPurgatory,name=NumDelayedOperations,delayedOperation=Rebalance";
  public static final String DELAYED_OPERATIONS_TOPIC_NUM =
      "kafka.server:type=DelayedOperationPurgatory,name=NumDelayedOperations,delayedOperation=topic";

  public static final String DELAYED_OPERATIONS_FETCH_PURGATORY_SIZE =
      "kafka.server:type=DelayedOperationPurgatory,name=PurgatorySize,delayedOperation=Fetch";
  public static final String DELAYED_OPERATIONS_HEARTBEAT_PURGATORY_SIZE =
      "kafka.server:type=DelayedOperationPurgatory,name=PurgatorySize,delayedOperation=Heartbeat";
  public static final String DELAYED_OPERATIONS_PRODUCE_PURGATORY_SIZE =
      "kafka.server:type=DelayedOperationPurgatory,name=PurgatorySize,delayedOperation=Produce";
  public static final String DELAYED_OPERATIONS_REBALANCE_PURGATORY_SIZE =
      "kafka.server:type=DelayedOperationPurgatory,name=PurgatorySize,delayedOperation=Rebalance";
  public static final String DELAYED_OPERATIONS_TOPIC_PURGATORY_SIZE =
      "kafka.server:type=DelayedOperationPurgatory,name=PurgatorySize,delayedOperation=topic";

  public static final String FETCH_DELAY_QUEUE_SIZE = "kafka.server:type=Fetch";
  public static final String PRODUCE_DELAY_QUEUE_SIZE = "kafka.server:type=Produce";

  public static final String CONSUMER_LAG = "kafka.server:type=FetcherLagMetrics,name=ConsumerLag,clientId={},topic={},partition={}";

  public static final String FETCHER_STATS_BYTES_PER_SEC =
      "kafka.server:type=FetcherStats,name=BytesPerSec,clientId={},brokerHost={},brokerPort={}";
  public static final String FETCHER_STATS_REQUESTS_PER_SEC =
      "kafka.server:type=FetcherStats,name=RequestsPerSec,clientId={},brokerHost={},brokerPort={}";

  public static final String REQUEST_HANDLER_AVG_IDLE_PERCENT =
      "kafka.server:type=KafkaRequestHandlerPool,name=RequestHandlerAvgIdlePercent";

  public static final String BROKER_STATE = "kafka.server:type=KafkaServer,name=BrokerState";
  public static final String CLUSTER_ID = "kafka.server:type=KafkaServer,name=ClusterId";

  public static final String LEADER_REPLICATION = "kafka.server:type=LeaderReplication";

  public static final String REPLICA_MAX_LAG = "kafka.server:type=ReplicaFetcherManager,name=MaxLag,clientId=Replica";
  public static final String REPLICA_MIN_FETCH_RATE = "kafka.server:type=ReplicaFetcherManager,name=MinFetchRate,clientId=Replica";
  public static final String REPLICA_ISR_EXPANDS_PER_SEC = "kafka.server:type=ReplicaManager,name=IsrExpandsPerSec";
  public static final String REPLICA_ISR_SHRINKS_PER_SEC = "kafka.server:type=ReplicaManager,name=IsrShrinksPerSec";
  public static final String REPLICA_LEADER_COUNT = "kafka.server:type=ReplicaManager,name=LeaderCount";
  public static final String REPLICA_PARTITION_COUNT = "kafka.server:type=ReplicaManager,name=PartitionCount";
  public static final String REPLICA_UNDERREPLICATED_PARTITIONS = "kafka.server:type=ReplicaManager,name=UnderReplicatedPartitions";

  // kafka.server:type=SessionExpireListener
  public static final String ZK_AUTHFAILURES_PER_SEC = "kafka.server:type=SessionExpireListener,name=ZooKeeperAuthFailuresPerSec";
  public static final String ZK_DISCONNECTS_PER_SEC = "kafka.server:type=SessionExpireListener,name=ZooKeeperDisconnectsPerSec";
  public static final String ZK_EXPIRES_PER_SEC = "kafka.server:type=SessionExpireListener,name=ZooKeeperExpiresPerSec";
  public static final String ZK_RO_CONNECTS_PER_SEC = "kafka.server:type=SessionExpireListener,name=ZooKeeperReadOnlyConnectsPerSec";
  public static final String ZK_SASL_AUTHS_PER_SEC = "kafka.server:type=SessionExpireListener,name=ZooKeeperSaslAuthenticationsPerSec";
  public static final String ZK_SYNC_CONNECTIONS_PER_SEC = "kafka.server:type=SessionExpireListener,name=ZooKeeperSyncConnectsPerSec";

  // app-info
  public static final String APP_INFO = "kafka.server:type=app-info,id=0";

  public static final String REPLICA_FETCHER_METRICS = "kafka.server:type=replica-fetcher-metrics,broker-id={},fetcher-id={}";
  public static final String SOCKET_SERVER_METRICS = "kafka.server:type=socket-server-metrics,networkProcessor={}";


  public MeterMetric getBytesInPerSec(MBeanServerConnection mbsc, Optional<String> topicName) {
    return getBrokerTopicMetrics(mbsc, "BytesInPerSec", topicName);
  }

  public MeterMetric getBytesOutPerSec(MBeanServerConnection mbsc, Optional<String> topicName) {
    return getBrokerTopicMetrics(mbsc, "BytesOutPerSec", topicName);
  }

  public MeterMetric getBytesRejectedPerSec(MBeanServerConnection mbsc, Optional<String> topicName) {
    return getBrokerTopicMetrics(mbsc, "BytesRejectedPerSec", topicName);
  }

  public MeterMetric getFailedFetchRequestsPerSec(MBeanServerConnection mbsc, Optional<String> topicName) {
    return getBrokerTopicMetrics(mbsc, "FailedFetchRequestsPerSec", topicName);
  }

  public MeterMetric getFailedProduceRequestsPerSec(MBeanServerConnection mbsc, Optional<String> topicName) {
    return getBrokerTopicMetrics(mbsc, "FailedProduceRequestsPerSec", topicName);
  }

  public MeterMetric getMessagesInPerSec(MBeanServerConnection mbsc, Optional<String> topicName) {
    return getBrokerTopicMetrics(mbsc, "MessagesInPerSec", topicName);
  }

  private MeterMetric getBrokerTopicMetrics(MBeanServerConnection mbsc, String metricName, Optional<String> topicName) {
    return getMeterMetric(mbsc, getObjectName(metricName, topicName));
  }

  private MeterMetric getMeterMetric(MBeanServerConnection mbsc, ObjectName objectName) {
    String[] attributes = {"Count", "MeanRate", "OneMinuteRate", "FiveMinuteRate", "FifteenMinuteRate"};
    AttributeList attributeList = null;
    try {
      attributeList = mbsc.getAttributes(objectName, attributes);
    } catch (Exception e) {
      LOG.warn("getMeterMetric failed! " + e.getMessage());
      return new MeterMetric(0L, 0D, 0D, 0D, 0D);
    }
    return new MeterMetric(getLongValue(attributeList, attributes[0]), getDoubleValue(attributeList, attributes[1]),
        getDoubleValue(attributeList, attributes[2]), getDoubleValue(attributeList, attributes[3]),
        getDoubleValue(attributeList, attributes[4]));
  }

  private ObjectName getObjectName(String metricName, Optional<String> topicName) {
    ObjectName objectName = null;
    try {
      if (topicName.isPresent()) {
        objectName = new ObjectName("kafka.server:type=BrokerTopicMetrics,name=" + metricName + ",topic=" + topicName.get());
      } else {
        objectName = new ObjectName("kafka.server:type=BrokerTopicMetrics,name=" + metricName);
      }
    } catch (MalformedObjectNameException e) {
      LOG.error("Get ObjectName error! " + e.getMessage());
    }
    return objectName;
  }

  private Double getDoubleValue(AttributeList attributes, String name) {
    List<Attribute> _attributes = attributes.asList();
    for (Attribute attr : _attributes) {
      if (attr.getName().equalsIgnoreCase(name)) {
        return (Double) attr.getValue();
      }
    }
    return 0D;
  }

  private Long getLongValue(AttributeList attributes, String name) {
    List<Attribute> _attributes = attributes.asList();
    for (Attribute attr : _attributes) {
      if (attr.getName().equalsIgnoreCase(name)) {
        return (Long) attr.getValue();
      }
    }
    return 0L;
  }
}
