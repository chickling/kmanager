package com.chickling.kmanager.test;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Hulva Luva.H
 * @since 2017-07-24
 *
 */
public class JSONTest {

	public static void main(String[] args) {
		JSONObject json = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		jsonArr.put(1);
		jsonArr.put(3);
		json.put("columns", jsonArr);
		JSONArray jsonArr1 = new JSONArray();

		jsonArr1.put(jsonArr);
		jsonArr1.put(json);

		System.out.println(jsonArr1);
	}

	public static void test1() {
    Map<String, Object> root = new HashMap<String, Object>();
    root.put("firstLevel", JSONObject.NULL);
    if (root.get("firstLevel").equals(JSONObject.NULL)) {
      Map<String, Object> firstLevel = new HashMap<String, Object>();
      firstLevel.put("secondLevel", JSONObject.NULL);
      root.put("firstLevel", firstLevel);
    }
    JSONObject json = new JSONObject(root);
    System.out.println(json);
  }

}
