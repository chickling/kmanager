package com.chickling.kmonitor.core.jmx.metric;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chickling.kmonitor.core.jmx.JMXExecutor;
import com.chickling.kmonitor.core.jmx.KafkaJMX;
import com.chickling.kmonitor.utils.ZKUtils;

/**
 * @author Hulva Luva.H from ECBD
 * @date 2017年7月22日
 * @description
 *
 */
public class ObjectNameManager {
	private static Logger LOG = LoggerFactory.getLogger(ObjectNameManager.class);

	private static boolean excludeInternalTopic = true; // like __consumer_offsets

	private static Map<String, ObjectNameHolder> objectNames = new HashMap<String, ObjectNameHolder>();
	private static KafkaJMX kafkaJMX = null;

	private ObjectNameManager() {
		kafkaJMX = new KafkaJMX();
		initObjectNames();
	}

	private void initObjectNames() {
		try {
			if (kafkaJMX == null) {
				kafkaJMX = new KafkaJMX();
			}
			List<String> jmxHosts = ZKUtils.getKafkaJMXHostsFromZookeeper();
			for (String jmxHost : jmxHosts) {
				String[] jmxArr = jmxHost.split(":");
				if ("-1".equals(jmxArr[2])) {
					continue;
				}
				kafkaJMX.doWithConnection(jmxArr[1], Integer.parseInt(jmxArr[2]), Optional.of(""), Optional.of(""),
						false, new JMXExecutor() {

							@Override
							public void doWithConnection(MBeanServerConnection mbsc) {
								try {
									Set<ObjectInstance> beans = mbsc.queryMBeans(null, null);
									ObjectNameHolder objectNameHolder = null;
									ObjectNameHolder objectNameHolderOld = null;
									for (ObjectInstance bean : beans) {
										objectNameHolder = new ObjectNameHolder();
										String objectName = bean.getObjectName().toString();
										if (excludeInternalTopic && objectName.contains("__consumer_offsets")) {
											continue;
										}
										String[] metric_other = objectName.split(":");
										objectNameHolder.setMetric(metric_other[0]);
										String[] type_name_other = metric_other[1].split(",");
										Map<String, String> temp = new HashMap<String, String>();
										String currentSort = "";
										for (int i = 0; i < type_name_other.length; i++) {
											String[] tempArr = type_name_other[i].split("=");
											if ("type".equalsIgnoreCase(tempArr[0])) {
												objectNameHolder.setType(tempArr[1]);
											} else if ("name".equalsIgnoreCase(tempArr[0])) {
												objectNameHolder.setName(tempArr[1]);
											} else {
												currentSort += i == type_name_other.length - 1 ? tempArr[0]
														: tempArr[0] + ",";
												temp.put(tempArr[0], tempArr[1]);
											}
										}
										String key = objectNameHolder.getName() == null
												? metric_other[0] + objectNameHolder.getType()
														+ objectNameHolder.getName()
												: metric_other[0] + objectNameHolder.getType();

										if (objectNames.containsKey(key)) {
											objectNameHolderOld = objectNames.get(key);
											final Map<String, Object> extras = objectNameHolderOld.getExtra();

											if (extras.isEmpty()) {
												continue;
											}
											String sort = (String) extras.get("sort");
											if (sort.equals(currentSort)) {
												temp.forEach((k, v) -> {
													@SuppressWarnings("unchecked")
													Set<String> somthing = (Set<String>) extras.get(k);
													somthing.add(v);
												});
											} else {
												extras.put("sort", currentSort);
												temp.forEach((k, v) -> {
													Set<String> somthing = new HashSet<String>();
													somthing.add(v);
													extras.put(k, somthing);
												});
											}
										} else {
											Map<String, Object> extras = new HashMap<String, Object>();
											extras.put("sort", currentSort);
											temp.forEach((k, v) -> {
												Set<String> somthing = new HashSet<String>();
												somthing.add(v);
												extras.put(k, somthing);
											});
											objectNameHolder.setExtra(extras);
											objectNames.put(key, objectNameHolder);
										}
									}
								} catch (Exception e) {

								}
							}
						});
			}
		} catch (Exception e) {

		}
	}

}
