package com.chickling.kmonitor.utils.elasticsearch.restapi;

/**
 * @author Hulva Luva.H from ECBD
 * @date 2017年7月19日
 * @description
 *
 */
public class ScrollSearchTemplate {

	public static String getScrollSearchBody(String topic, String group, String from, String to) {
		return "{\"size\":1000,\"sort\":[{\"timestamp\":{\"order\":\"asc\"}},{\"partition\":{\"order\":\"asc\"}}],\"query\":{\"bool\":{\"must\":[{\"match\":{\"topic\":\""
				+ topic + "\"}},{\"match\":{\"group\":\"" + group + "\"}}],\"filter\":[{\"range\":{\"date\":{\"gte\":\""
				+ from + "\",\"lte\":\"" + to + "\",\"format\":\"yyyy-MM-dd'T'HH:mm:ss.SSSZ\"}}}]}}}";
	}

	public static String getScrollNextBody(String scrollId) {
		return "{\"scroll\":\"1m\",\"scroll_id\":\"" + scrollId + "\"}";
	}
}
