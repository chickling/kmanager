package com.chickling.kmanager.alert;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chickling.kmanager.config.AppConfig;
import com.chickling.kmanager.model.OffsetInfo;
import com.google.gson.Gson;

/**
 * @author Hulva Luva.H
 * @since 2017年2月11日
 *
 */
public class TaskManager {
	protected static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

	public static String taskFolder;

	public static ConcurrentHashMap<String, Long> cachedLastSendTime = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, Set<OffsetInfo>> cachedTriggeredOffsetInfo = new ConcurrentHashMap<>();

	// k:group v:(k:topic v:taskContent)
	private static Map<String, Map<String, TaskContent>> tasks;

	public static void init(AppConfig config) {
		taskFolder = config.getTaskFolder();
		tasks = Collections.synchronizedMap(new HashMap<String, Map<String, TaskContent>>());
	}

	public static TaskContent getTask(String group, String topic) {
		if (tasks.containsKey(group)) {
			return tasks.get(group).get(topic);
		}
		return null;
	}

	public static boolean exits(TaskContent taskContent) {
		if (tasks.containsKey(taskContent.getGroup())) {
			return tasks.get(taskContent.getGroup()).containsKey(taskContent.getTopic());
		}
		return false;
	}

	public static void addTask(TaskContent taskContent) throws JSONException {
		Map<String, TaskContent> task = null;
		if (tasks.containsKey(taskContent.getGroup())) {
			task = tasks.get(taskContent.getGroup());
		} else {
			task = new HashMap<String, TaskContent>();
		}
		task.put(taskContent.getTopic(), taskContent);
		tasks.put(taskContent.getGroup(), task);
	}

	public static void deleteTask(String taskNameToRemove) {
		String[] group_topic = taskNameToRemove.split("-");
		if (exits(new TaskContent(group_topic[0], group_topic[1], null, null, null,null))) {
			TaskContent taskToDelete = tasks.get(group_topic[0]).remove(group_topic[1]);
			deleteTaskFile(taskToDelete);
		}
	}

	public static void deleteTaskFile(TaskContent task) {
		new File(taskFolder + "/" + task.getGroup() + "-" + task.getTopic() + ".task").delete();
	}

	public static Set<TaskContent> getTasks() {
		Set<TaskContent> _tasks = new HashSet<TaskContent>();
		tasks.forEach((group, topic_task) -> {
			Set<Entry<String, TaskContent>> entrySet = topic_task.entrySet();
			entrySet.forEach(entry -> {
				_tasks.add(entry.getValue());
			});
		});
		return _tasks;
	}

	public static void saveTaskToFileAndAddToTasks(TaskContent taskContent) {
		try {
			PrintWriter writer = new PrintWriter(
					taskFolder + "/" + taskContent.getGroup() + "-" + taskContent.getTopic() + ".task", "UTF-8");
			writer.println(new Gson().toJson(taskContent));
			writer.close();
			addTask(taskContent);
		} catch (IOException e) {
			logger.error("create task file failed!", e);
		}
	}
}
