/**
 * 
 */
package com.chickling.kmanager.core.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;

import kafka.common.TopicAndPartition;

/**
 * @author Hulva Luva.H
 * @since 2018年4月23日
 */
public abstract class ConsumerGroupService {

	protected final static String MISSING_COLUMN_VALUE = "-";

	protected Properties props;

	public abstract List<String> listGroups();

	public abstract void close();

	public void setProperties(Properties props) {
		this.props = props;
	}

	public Map<String, List<PartitionAssignmentState>> describeGroup(String group) {
		return collectGroupAssignment(group);
	}

	protected abstract Map<String, List<PartitionAssignmentState>> collectGroupAssignment(String group);

	protected abstract Optional<Long> getLogEndOffset(String group, TopicPartition topicPartition);

	protected List<PartitionAssignmentState> collectConsumerAssignment(String group, Optional<Node> coordinator,
			List<TopicAndPartition> topicPartitions, MyFunctions myFunc, Optional<String> consumerIdOpt,
			Optional<String> hostOpt, Optional<String> clientIdOpt) {
		List<PartitionAssignmentState> ret = new ArrayList<PartitionAssignmentState>();
		if (topicPartitions.isEmpty())
			ret.add(new PartitionAssignmentState(group, coordinator, Optional.of(null), Optional.of(null),
					Optional.of(null), getLag(Optional.of(null), Optional.of(null)), consumerIdOpt, hostOpt,
					clientIdOpt, Optional.of(null)));
		else {
			topicPartitions.sort(new Comparator<TopicAndPartition>() {

				@Override
				public int compare(TopicAndPartition tap1, TopicAndPartition tap2) {
					return tap1.partition() > tap2.partition() ? 1 : (tap1.partition() == tap2.partition() ? 0 : -1);
				}

			});
			topicPartitions
					.forEach(topicPartition -> ret.add(describePartition(group, coordinator, topicPartition.topic(),
							topicPartition.partition(), myFunc.getPartitionOffset(topicPartition).get(topicPartition),
							consumerIdOpt, hostOpt, clientIdOpt)));
		}
		return ret;
	}

	protected Optional<Long> getLag(Optional<Long> offset, Optional<Long> logEndOffset) {
		return offset.filter(o -> o != -1l)
				.flatMap(offset1 -> logEndOffset.map(logEndOffset1 -> logEndOffset1.longValue() - offset1.longValue()));
	}

	private PartitionAssignmentState describePartition(String group, Optional<Node> coordinator, String topic,
			Integer partition, Optional<Long> offsetOpt, Optional<String> consumerIdOpt, Optional<String> hostOpt,
			Optional<String> clientIdOpt) {
		Optional<Long> logEndOffsetResult = getLogEndOffset(group, new TopicPartition(topic, partition));
		return new PartitionAssignmentState(group, coordinator, Optional.ofNullable(topic),
				Optional.ofNullable(partition), offsetOpt, getLag(offsetOpt, logEndOffsetResult), consumerIdOpt,
				hostOpt, clientIdOpt, logEndOffsetResult);
	}
}
