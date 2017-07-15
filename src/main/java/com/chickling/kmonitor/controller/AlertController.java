package com.chickling.kmonitor.controller;

import java.util.Set;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.chickling.kmonitor.alert.TaskContent;
import com.chickling.kmonitor.alert.TaskManager;
import com.chickling.kmonitor.initialize.SystemManager;

/**
 * @author Hulva Luva.H
 *
 */
@RestController
@RequestMapping("/alerting")
public class AlertController {
	protected static final Logger logger = LoggerFactory.getLogger(AlertController.class);

	@RequestMapping(value = "/tasks", method = RequestMethod.GET)
	public Set<TaskContent> get() {
		return TaskManager.getTasks();
	}

	@RequestMapping(value = "/isAlertEnabled", method = RequestMethod.GET)
	public String isAlertEnabled() {
		JSONObject response = new JSONObject();
		response.put("isAlertEnabled", SystemManager.getConfig().getIsAlertEnabled());
		return response.toString();
	}

	@RequestMapping(value = "/new", method = RequestMethod.POST)
	public Set<TaskContent> post(@RequestBody TaskContent taskContent) {
		TaskManager.saveTaskToFileAndAddToTasks(taskContent);
		return TaskManager.getTasks();
	}

	@RequestMapping(value = { "/update" }, method = RequestMethod.POST)
	public Set<TaskContent> put(@RequestBody TaskContent taskContent) {
		TaskManager.refreshTask(taskContent);
		return TaskManager.getTasks();
	}

	@RequestMapping(value = "/delete/{taskName}", method = RequestMethod.DELETE)
	public Set<TaskContent> delete(@PathVariable String taskName) {
		TaskManager.deleteTask(taskName);
		return TaskManager.getTasks();
	}
}
