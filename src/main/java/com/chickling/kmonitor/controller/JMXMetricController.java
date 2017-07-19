package com.chickling.kmonitor.controller;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.chickling.kmonitor.initialize.SystemManager;

/**
 * 
 * @author Hulva Luva.H
 * @since 2017-07-12
 *
 */

@RestController
@RequestMapping("/metrics")
public class JMXMetricController {
	private static Logger LOG = LoggerFactory.getLogger(JMXMetricController.class);

	@RequestMapping(value = "/brokerTopicMetrics/broker", method = RequestMethod.GET)
	public List<String> getBrokerTopicMetrics(@PathVariable String broker, @PathVariable String topic) {
		List<String> groups = SystemManager.og.getGroups();
		Collections.sort(groups);
		return groups;
	}
}
