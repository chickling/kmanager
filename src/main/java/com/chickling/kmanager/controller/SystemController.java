package com.chickling.kmanager.controller;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.kafka.common.TopicPartition;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.chickling.kmanager.initialize.SystemManager;

/**
 * @author Hulva Luva.H
 *
 */
@RequestMapping("/extra")
@RestController
@CrossOrigin(origins = "*")
public class SystemController {
  // private static Logger LOG = LoggerFactory.getLogger(SyatemController.class);

  @RequestMapping(value = "/nocurrentassignment", method = RequestMethod.GET)
  public String getTopicList() {
    Map<String, TopicPartition> nocurrentassignmentMap = SystemManager.og.kafkaConsumerGroupService.getNocurrentassignmentMap();
    JSONArray jsonArr = new JSONArray();
    JSONObject json = null;
    for (Entry<String, TopicPartition> entry : nocurrentassignmentMap.entrySet()) {
      json = new JSONObject();
      json.put("group", entry.getKey());
      json.put("topic", entry.getValue().topic());
      json.put("partition", entry.getValue().partition());
      jsonArr.put(json);
    }
    return jsonArr.toString();
  }

}
