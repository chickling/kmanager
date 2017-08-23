package com.chickling.kmanager.controller;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.chickling.kmanager.alert.TaskContent;
import com.chickling.kmanager.alert.TaskManager;
import com.chickling.kmanager.initialize.SystemManager;

/**
 * @author Hulva Luva.H
 *
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/alerting")
public class AlertController {
	protected static final Logger logger = LoggerFactory.getLogger(AlertController.class);

	@RequestMapping(value = "/tasks", method = RequestMethod.GET)
	public Set<TaskContent> get() {
		if (SystemManager.getConfig().getIsAlertEnabled()) {
			return TaskManager.getTasks();
		}
		return new HashSet<TaskContent>();
	}

	@RequestMapping(value = "/isAlertEnabled", method = RequestMethod.GET)
	public String isAlertEnabled() {
		JSONObject response = new JSONObject();
		response.put("isAlertEnabled", SystemManager.getConfig().getIsAlertEnabled());
		return response.toString();
	}

	@RequestMapping(value = { "/task" }, method = RequestMethod.POST)
	public Set<TaskContent> put(@RequestBody TaskContent taskContent) {
		TaskManager.saveTaskToFileAndAddToTasks(taskContent);
		return TaskManager.getTasks();
	}

	@RequestMapping(value = "/delete/{taskName}", method = RequestMethod.DELETE)
	public Set<TaskContent> delete(@PathVariable String taskName) {
		TaskManager.deleteTask(taskName);
		return TaskManager.getTasks();
	}
}
