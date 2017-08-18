package com.chickling.kmanager.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chickling.kmanager.initialize.SystemManager;
import com.chickling.kmanager.model.KafkaInfo;
import com.chickling.kmanager.model.OffsetHistory;
import com.chickling.kmanager.model.OffsetStat;
import com.chickling.kmanager.utils.elasticsearch.restapi.ElasticsearchRESTUtil;

/**
 * 
 * @author Hulva Luva.H
 *
 */
@RestController
@CrossOrigin(origins = "*")
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

	@RequestMapping(value="/2/{group}/{topic}", method = RequestMethod.GET)
	public Map<String, List<OffsetStat>> getOffsetStats(@PathVariable String group, @PathVariable String topic
		, @RequestParam(value="start", defaultValue="") String start
		, @RequestParam(value="end", defaultValue="") String end){
		
		return ElasticsearchRESTUtil.offset(group, topic, start, end);
	}
	@RequestMapping(value="/2/{group}/", method = RequestMethod.GET)
	public Map<String, List<OffsetStat>> getOffsetStats2(@PathVariable String group
		, @RequestParam(value="start", defaultValue="") String start
		, @RequestParam(value="end", defaultValue="") String end){
		
		return ElasticsearchRESTUtil.offset(group, null, start, end);
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
