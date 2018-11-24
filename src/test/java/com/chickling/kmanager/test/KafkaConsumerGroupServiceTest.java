/**
 * 
 */
package com.chickling.kmanager.test;

import java.util.Properties;

import com.chickling.kmanager.core.service.KafkaConsumerGroupService;

/**
 * @author Hulva Luva.H
 * @since 2018年4月23日
 */
public class KafkaConsumerGroupServiceTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Properties props = new Properties();
		props.setProperty("bootstrapServer", "localhost:9092");
		KafkaConsumerGroupService kafkaConsumerGroupService = new KafkaConsumerGroupService();
		kafkaConsumerGroupService.setProperties(props);
		System.out.println(kafkaConsumerGroupService.listGroups());
	}

}
