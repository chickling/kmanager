package com.chickling.kmonitor.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.chickling.kmonitor.initialize.SystemManager;
import com.chickling.kmonitor.model.Node;
import com.chickling.kmonitor.model.TopicAndConsumersDetails;
import com.chickling.kmonitor.model.TopicDetails;

/**
 * @author Hulva Luva.H
 *
 */
@RestController
public class TopicController {
	private static Logger LOG = LoggerFactory.getLogger(TopicController.class);

	@RequestMapping(value = "/topiclist", method = RequestMethod.GET)
	public List<String> getTopicList() {
		List<String> topicList = SystemManager.og.getTopics();
		Collections.sort(topicList);
		return topicList;
	}

	@RequestMapping(value = "/topicdetails/{topic}", method = RequestMethod.GET)
	public TopicDetails getTopicDetails(@PathVariable String topic) {
		return SystemManager.og.getTopicDetail(topic);
	}

	@RequestMapping(value = "/topic/{topic}/consumers", method = RequestMethod.GET)
	public String getTopicAndConsumersDetail(@PathVariable String topic) {
		TopicAndConsumersDetails consumers = null;
		try {
			consumers = SystemManager.og.getTopicAndConsumersDetail(topic);
		} catch (Exception e) {
			LOG.error("getTopicAndConsumersDetail for topic " + topic + "failed!" + e.getMessage());
			consumers = new TopicAndConsumersDetails();
		}
		JSONObject result = new JSONObject();
		result.put("consumers", new JSONObject(consumers));
		return result.toString();
	}

	@RequestMapping(value = "/activetopics", method = RequestMethod.GET)
	public Node getActiveTopics() {
		try {
			return SystemManager.og.getActiveTopics();
		} catch (Exception e) {
			LOG.error("Get active topics failed!" + e.getMessage());
		}
		return new Node();
	}

	@RequestMapping(value = "/activeconsumers/{topic}", method = RequestMethod.GET)
	public List<String> getActiveConsumers(@PathVariable String topic) {
		try {
			return SystemManager.og.getActiveConsumer(topic);
		} catch (Exception e) {
			LOG.error("Get active groups failed!" + e.getMessage());
		}
		return new ArrayList<String>();
	}

}
