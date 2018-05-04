package com.chickling.kmanager.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.chickling.kmanager.initialize.SystemManager;
import com.chickling.kmanager.model.Node;
import com.chickling.kmanager.model.TopicAndConsumersDetails;
import com.chickling.kmanager.model.TopicDetails;

/**
 * @author Hulva Luva.H
 *
 */
@RestController
@CrossOrigin(origins = "*")
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

  /**
   * 获取所有的consumers包含激活和未激活
   * 
   * @param topic
   * @return
   */
  @RequestMapping(value = "/consumers/{topic}", method = RequestMethod.GET)
  public List<String> getAllConsumers(@PathVariable String topic) {
    try {
      List<String> zkGroups = SystemManager.og.getTopicMap(true).get(topic);
      List<String> brokerGroups = SystemManager.og.getTopicMap(false).get(topic);
      Set<String> groups = new HashSet<String>();
      if (brokerGroups != null) {
        groups.addAll(brokerGroups);
      }
      if (zkGroups != null) {
        groups.addAll(zkGroups);
      }
      return SystemManager.og.getGroups();
    } catch (Exception e) {
      LOG.error("Get active groups failed!" + e.getMessage());
    }
    return new ArrayList<String>();
  }

}
