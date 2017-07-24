package com.chickling.kmonitor.test;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

/**
 * @author Hulva Luva.H
 * @since 2017-07-24
 *
 */
public class JSONTest {

  public static void main(String[] args) {
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
