package com.chickling.kmonitor.utils.elasticsearch.restapi;

import java.util.Date;

/**
 * @author Hulva Luva.H
 * @since 2017-7-19
 *
 */
public class ScrollSearchTemplate {

  public static String getScrollSearchBody(String topic, String group, String from, String to) {
    return "{\"size\":1000,"

        + "\"sort\":[{\"timestamp\":{\"order\":\"asc\"}},{\"partition\":{\"order\":\"asc\"}}],"

        + "\"query\":{\"bool\":{"

        + "\"must\":[{\"match\":{\"topic\":\"" + topic + "\"}},{\"match\":{\"group\":\"" + group + "\"}}],"

        + "\"filter\":[{\"range\":{\"date\":{\"gte\":\"" + from + "\",\"lte\":\"" + to
        + "\",\"format\":\"yyyy-MM-dd'T'HH:mm:ss.SSSZ\"}}}]}}}";
  }

  public static String getScrollNextBody(String scrollId) {
    return "{\"scroll\":\"1m\",\"scroll_id\":\"" + scrollId + "\"}";
  }

  public static String getMetricVizSearchBody(String metric) {
    return "{\"size\": 2000,"

        + "\"sort\": [{\"timestamp\": {\"order\": \"desc\"}},\"broker\"],"

        + "\"query\": {\"bool\": {"

        + "\"must\": [{\"match\": { \"metric\": \"" + metric + "\" }}],"

        + "\"filter\": [{"

        + "\"range\": {\"timestamp\": {"

        + "\"gte\": " + (new Date().getTime() - 480 * 60000) + ","

        + "\"lte\": " + new Date().getTime() + "}}}]}}}";
  }
}
