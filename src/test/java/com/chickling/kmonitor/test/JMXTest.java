package com.chickling.kmonitor.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;

import org.json.JSONArray;
import org.json.JSONObject;

import com.chickling.kmonitor.core.jmx.JMXExecutor;
import com.chickling.kmonitor.core.jmx.KafkaJMX;

/**
 * @author Hulva Luva.H
 * @since 2017-07-11
 *
 */
public class JMXTest {

	private static boolean excludeInternalTopic = true; // like __consumer_offsets

	public static void main(String[] args) {
		KafkaJMX kafkaJMX = new KafkaJMX();
		objectName_Metrics(kafkaJMX);
		objectNames(kafkaJMX);
	}

	private static void objectNames(KafkaJMX kafkaJMX) {
		kafkaJMX.doWithConnection("10.16.238.94", 8888, Optional.of(""), Optional.of(""), false, new JMXExecutor() {

			@Override
			public void doWithConnection(MBeanServerConnection mbsc) {
				// KafkaMetrics kafkaMetrics = new KafkaMetrics();
				try (FileWriter fw = new FileWriter("objectNames.json", true);
						BufferedWriter bw = new BufferedWriter(fw);
						PrintWriter out = new PrintWriter(bw)) {
					Set<ObjectInstance> beans = mbsc.queryMBeans(null, null);
					JSONArray objectName = new JSONArray();
					for (ObjectInstance bean : beans) {
						if (excludeInternalTopic && bean.getObjectName().toString().contains("__consumer_offsets")) {
							continue;
						}
						System.out.println("ObjectName: " + bean.getObjectName());
						objectName.put(bean.getObjectName().toString());
						MBeanInfo mbeanInfo = mbsc.getMBeanInfo(bean.getObjectName());
						System.out.println("\tMBeanInfo: " + mbeanInfo);
						MBeanAttributeInfo[] attributes = mbeanInfo.getAttributes();
						String[] attributeArr = new String[attributes.length];
						for (int i = 0; i < attributes.length; i++) {
							attributeArr[i] = attributes[i].getName();
						}
						AttributeList attributeList = mbsc.getAttributes(bean.getObjectName(), attributeArr);
						List<Attribute> attributeList1 = attributeList.asList();

						for (Attribute attr : attributeList1) {
							System.out.println("\t\tName: " + attr.getName() + " Value: " + attr.getValue());
						}
					}
					out.println(objectName.toString());
				} catch (Exception e) {

				}
			}
		});

	}

	private static void objectName_Metrics(KafkaJMX kafkaJMX) {
		kafkaJMX.doWithConnection("10.16.238.94", 8888, Optional.of(""), Optional.of(""), false, new JMXExecutor() {

			@Override
			public void doWithConnection(MBeanServerConnection mbsc) {
				// KafkaMetrics kafkaMetrics = new KafkaMetrics();
				try (FileWriter fw = new FileWriter("metrics.json", true);
						BufferedWriter bw = new BufferedWriter(fw);
						PrintWriter out = new PrintWriter(bw)) {
					Set<ObjectInstance> beans = mbsc.queryMBeans(null, null);

					JSONArray objectNameMetrics = new JSONArray();
					JSONObject objectName = null;
					for (ObjectInstance bean : beans) {
						objectName = new JSONObject();
						String objectNameStr = bean.getObjectName().toString();
						if (excludeInternalTopic && objectNameStr.contains("__consumer_offsets")) {
							continue;
						}
						System.out.println("ObjectName: " + objectNameStr);
						String[] metric_other = objectNameStr.split(":");
						objectName.put("metric", metric_other[0]);
						String[] type_name_other = metric_other[1].split(",");
						String[] temp;
						for (int i = 0; i < type_name_other.length; i++) {
							temp = type_name_other[i].split("=");
							objectName.put(temp[0], temp[1]);
						}
						objectName.put("objectName", bean.getObjectName().toString());
						MBeanInfo mbeanInfo = mbsc.getMBeanInfo(bean.getObjectName());
						System.out.println("\tMBeanInfo: " + mbeanInfo);
						MBeanAttributeInfo[] attributes = mbeanInfo.getAttributes();
						String[] attributeArr = new String[attributes.length];
						for (int i = 0; i < attributes.length; i++) {
							attributeArr[i] = attributes[i].getName();
						}
						AttributeList attributeList = mbsc.getAttributes(bean.getObjectName(), attributeArr);
						List<Attribute> attributeList1 = attributeList.asList();

						for (Attribute attr : attributeList1) {
							objectName.put(attr.getName(), attr.getValue());
							System.out.println("\t\tName: " + attr.getName() + " Value: " + attr.getValue());
						}
						objectNameMetrics.put(objectName);
					}
					out.println(objectNameMetrics.toString());
				} catch (Exception e) {

				}
			}
		});
	}

}
