package com.chickling.kmonitor.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.chickling.kmonitor.initialize.SystemManager;
import com.chickling.kmonitor.model.KafkaInfo;
import com.chickling.kmonitor.model.OffsetHistory;

/**
 * 
 * @author Hulva Luva.H
 *
 */
@RestController
@RequestMapping("/group")
public class GroupController {
	private static Logger LOG = LoggerFactory.getLogger(GroupController.class);

	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
	public List<String> getGroups() {
		List<String> groups = SystemManager.og.getGroups();
		Collections.sort(groups);
		return groups;
	}

	@RequestMapping(value = "/{group}", method = RequestMethod.GET)
	public KafkaInfo getGroupInfo(@PathVariable String group) {
		KafkaInfo kafkaInfo = null;
		try {
			kafkaInfo = SystemManager.og.getInfo(group, new ArrayList<String>());
		} catch (Exception e) {
			LOG.warn("Ops~", e);
		}
		return kafkaInfo;
	}

	@RequestMapping(value = "/{group}/{topic}", method = RequestMethod.GET)
	public OffsetHistory getGroupTopicOffsetHistory(@PathVariable String group, @PathVariable String topic) {
		OffsetHistory offsetHistory = null;
		try {
			offsetHistory = SystemManager.db.offsetHistory(group, topic);
		} catch (Exception e) {
			LOG.warn("offsetHistory Ops~" + e.getMessage());
		}
		return offsetHistory;
	}

}
