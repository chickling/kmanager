package com.chickling.kmanager.controller;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.chickling.kmanager.config.AppConfig;
import com.chickling.kmanager.initialize.SystemManager;

/**
 * @author Hulva Luva.H
 * @since 2017-07-13
 *
 */
@RestController
@CrossOrigin(origins = "*")
public class SettingController {
	@RequestMapping(value = "/isSystemReady", method = RequestMethod.GET)
	public String isSystemReady() {
		JSONObject response = new JSONObject();
		response.put("isSystemReady", SystemManager.IS_SYSTEM_READY.get());
		return response.toString();
	}

	@RequestMapping(value = "/setting", method = RequestMethod.GET)
	public String gettSetting() {
		if (!SystemManager.IS_SYSTEM_READY.get()) {
			JSONObject response = new JSONObject();
			response.put("isSystemReady", SystemManager.IS_SYSTEM_READY.get());
			return response.toString();
		}
		return new JSONObject(SystemManager.getConfig()).toString();
	}

	@RequestMapping(value = "/setting", method = RequestMethod.POST)
	public String postSetting(@RequestBody AppConfig config) {
		JSONObject response = new JSONObject();
		try {
			SystemManager.setConfig(config);
		} catch (Exception e) {
			response.put("message", e.getMessage());
		}
		response.put("isSystemReady", SystemManager.IS_SYSTEM_READY.get());
		return response.toString();
	}
}
