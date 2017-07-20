package com.chickling.kmonitor.core.jmx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chickling.kmonitor.core.jmx.metric.OSMetric;

/**
 * @author Hulva Luva.H
 * @since 2017-07-11
 *
 */
public class KafkaMetrics {
	private static Logger LOG = LoggerFactory.getLogger(KafkaMetrics.class);

	// kafka.network:type=RequestMetrics,name=RequestsPerSec,request={Produce|FetchConsumer|FetchFollower}

	public static final Map<String, ObjectName> objectNames = new HashMap<String, ObjectName>();
	public static final String replicaFetcherManagerMinFetchRate = "replicaFetcherManagerMinFetchRate";
	public static final String replicaFetcherManagerMaxLag = "replicaFetcherManagerMaxLag ";
	public static final String kafkaControllerActiveControllerCount = "kafkaControllerActiveControllerCount ";
	public static final String kafkaControllerOfflinePartitionsCount = "kafkaControllerOfflinePartitionsCount ";
	public static final String logFlushStats = "logFlushStats ";
	public static final String operatingSystemObjectName = "operatingSystemObjectName ";
	public static final String logSegmentObjectName = "logSegmentObjectName ";
	public static final String directoryObjectName = "directoryObjectName ";

	static {
		try {
			objectNames.put(replicaFetcherManagerMinFetchRate,
					new ObjectName("kafka.server:type=ReplicaFetcherManager,name=MinFetchRate,clientId=Replica"));
			objectNames.put(replicaFetcherManagerMaxLag,
					new ObjectName("kafka.server:type=ReplicaFetcherManager,name=MaxLag,clientId=Replica"));
			objectNames.put(kafkaControllerActiveControllerCount,
					new ObjectName("kafka.controller:type=KafkaController,name=ActiveControllerCount"));
			objectNames.put(kafkaControllerOfflinePartitionsCount,
					new ObjectName("kafka.controller:type=KafkaController,name=OfflinePartitionsCount"));
			objectNames.put(logFlushStats, new ObjectName("kafka.log:type=LogFlushStats,name=LogFlushRateAndTimeMs"));
			objectNames.put(operatingSystemObjectName, new ObjectName("java.lang:type=OperatingSystem"));
			objectNames.put(logSegmentObjectName, new ObjectName("kafka.log:type=Log,name=*-LogSegments"));
			objectNames.put(directoryObjectName, new ObjectName("kafka.log:type=Log,name=*-Directory"));
		} catch (MalformedObjectNameException e) {
			LOG.error(e.getMessage());
		}
	}

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

	private MeterMetric getBrokerTopicMetrics(MBeanServerConnection mbsc, String metricName,
			Optional<String> topicName) {
		return getMeterMetric(mbsc, getObjectName(metricName, topicName));
	}

	private MeterMetric getMeterMetric(MBeanServerConnection mbsc, ObjectName objectName) {
		String[] attributes = { "Count", "MeanRate", "OneMinuteRate", "FiveMinuteRate", "FifteenMinuteRate" };
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
				objectName = new ObjectName(
						"kafka.server:type=BrokerTopicMetrics,name=" + metricName + ",topic=" + topicName.get());
			} else {
				objectName = new ObjectName("kafka.server:type=BrokerTopicMetrics,name=" + metricName);
			}
		} catch (MalformedObjectNameException e) {
			LOG.error("Get ObjectName error! " + e.getMessage());
		}
		return objectName;
	}

	public OSMetric getOSMetric(MBeanServerConnection mbsc) {
		String[] attributes = { "ProcessCpuLoad", "SystemCpuLoad" };
		AttributeList attributeList = null;
		try {
			attributeList = mbsc.getAttributes(objectNames.get(operatingSystemObjectName), attributes);
		} catch (Exception e) {
			LOG.warn("getOSMetric failed! " + e.getMessage());
			return new OSMetric(0D, 0D);
		}
		return new OSMetric(getDoubleValue(attributeList, attributes[0]), getDoubleValue(attributeList, attributes[0]));
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
