/**
 * 
 */
package com.chickling.kmanager.core.service;

import java.util.Map;
import java.util.Optional;

import kafka.common.TopicAndPartition;

/**
 * @author Hulva Luva.H
 * @since 2018年4月23日
 */
@FunctionalInterface
public interface MyFunctions {
	public Map<TopicAndPartition, Optional<Long>> getPartitionOffset(TopicAndPartition topicAndPartition);
}
