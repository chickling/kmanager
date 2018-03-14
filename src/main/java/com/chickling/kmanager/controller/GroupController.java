package com.chickling.kmanager.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
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
        groups.addAll(SystemManager.og.getGroupsCommittedToBroker());
		return groups;
	}

	@RequestMapping(value = "/{group}", method = RequestMethod.GET)
	public String getGroupInfo(@PathVariable String group) {
		KafkaInfo zk = null;
		KafkaInfo broker = null;
		JSONObject result = new JSONObject();
		try {
			zk = SystemManager.og.getInfo(group, new ArrayList<String>(),true);
			broker = SystemManager.og.getInfo(group, new ArrayList<String>(),false);
			result.put("zk", new JSONObject(zk));
			result.put("broker", new JSONObject(broker));
		} catch (Exception e) {
			LOG.warn("Ops~", e);
		}
		
		
		return result.toString();
	}

	@RequestMapping(value = "/{group}/{topic}", method = RequestMethod.GET)
	public Map<String, List<OffsetStat>> getOffsetStats(@PathVariable String group, @PathVariable String topic,
			@RequestParam(value = "start", defaultValue = "") String start,
			@RequestParam(value = "end", defaultValue = "") String end) {

		return ElasticsearchRESTUtil.offset(group, topic, start, end);
	}

	@RequestMapping(value = "/2/{group}/", method = RequestMethod.GET)
	public Map<String, List<OffsetStat>> getOffsetStats2(@PathVariable String group,
			@RequestParam(value = "start", defaultValue = "") String start,
			@RequestParam(value = "end", defaultValue = "") String end) {

		return ElasticsearchRESTUtil.offset(group, null, start, end);
	}

}
