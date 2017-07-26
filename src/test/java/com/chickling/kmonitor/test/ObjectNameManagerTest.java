package com.chickling.kmonitor.test;

import java.util.Map;

import org.json.JSONObject;

import com.chickling.kmonitor.jmx.ObjectNameHolder;
import com.chickling.kmonitor.jmx.ObjectNameManager;
import com.chickling.kmonitor.utils.ZKUtils;

/**
 * 
 * @author Hulva Luva.H
 * @since 2017-07-25
 *
 */
public class ObjectNameManagerTest {

	public static void main(String[] args) {
		ZKUtils.init("10.16.238.101:8181,10.16.238.102:8181,10.16.238.103:8181", 30000, 30000);
		ObjectNameManager onm = ObjectNameManager.getInstance();
		Map<String, ObjectNameHolder> objectNames = onm.getObjectNames();
		System.out.println(objectNames);
		System.out.println(new JSONObject(objectNames));
	}

}
