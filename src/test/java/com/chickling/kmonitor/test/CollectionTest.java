package com.chickling.kmonitor.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.chickling.kmonitor.jmx.ObjectNameHolder;

/**
 * @author Hulva Luva.H from ECBD
 * @date 2017年7月22日
 * @description
 *
 */
public class CollectionTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Set<ObjectNameHolder> objectNames = new HashSet<ObjectNameHolder>();
		ObjectNameHolder onh = new ObjectNameHolder("metric", "type", "name", new HashMap<String, Object>());
		objectNames.add(onh);
		System.out.println(
				objectNames.contains(new ObjectNameHolder("metric", "type", "name", new HashMap<String, Object>())));
		Map<String, Object> ops = new HashMap<String, Object>();
		ops.put("Hello", "World");
		ObjectNameHolder onh1 = new ObjectNameHolder("metric", "type", "name", ops);
		System.out.println(objectNames.contains(onh1));
		System.out.println(onh1.equals(onh));
		ObjectNameHolder onh2 = new ObjectNameHolder("metric", "type", "name", null);
		System.out.println(onh2.equals(onh));
		ObjectNameHolder onh3 = new ObjectNameHolder("metric", "type", null, null);
		System.out.println(onh2.equals(onh3));
	}

}
