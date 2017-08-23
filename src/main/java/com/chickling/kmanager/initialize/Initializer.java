package com.chickling.kmanager.initialize;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.chickling.kmanager.config.AppConfig;
import com.chickling.kmanager.utils.CommonUtils;
import com.google.gson.Gson;

/**
 * @author Hulva Luva.H
 *
 */
@Component
public class Initializer implements CommandLineRunner {

	private static Logger LOG = LoggerFactory.getLogger(Initializer.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])
	 */
	@Override
	public void run(String... args) throws Exception {
		try {
			File systemConfigFile = new File("system.json");
			if (!systemConfigFile.exists()) {
				SystemManager.IS_SYSTEM_READY.set(false);
			} else {
				String systemConfigFileContent = CommonUtils.loadFileContent(systemConfigFile.getAbsolutePath());
				AppConfig config = new Gson().fromJson(systemConfigFileContent, AppConfig.class);
				SystemManager.setConfig(config);
			}
		} catch (Exception e) {
			// If there comes out error, it is mostly the system.json file's fault,
			SystemManager.IS_SYSTEM_READY.set(false);
			LOG.error("init system failed!", e);
		}
	}
}
