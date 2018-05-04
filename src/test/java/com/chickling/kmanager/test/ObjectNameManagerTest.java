package com.chickling.kmanager.test;

import java.util.Map;

import org.json.JSONObject;

import com.chickling.kmanager.jmx.ObjectNameHolder;
import com.chickling.kmanager.jmx.ObjectNameManager;
import com.chickling.kmanager.utils.ZKUtils;

/**
 * 
 * @author Hulva Luva.H
 * @since 2017-07-25
 *
 */
public class ObjectNameManagerTest {

	public static void main(String[] args) {
		ZKUtils.init("luva101:8181,luva102:8181,luva103:8181", 30000, 30000);
		ObjectNameManager onm = ObjectNameManager.getInstance();
		Map<String, ObjectNameHolder> objectNames = onm.getObjectNames();
		System.out.println(objectNames);
		System.out.println(new JSONObject(objectNames));
	}

}
