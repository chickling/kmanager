package com.chickling.kmanager.test;

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
import javax.management.ObjectInstance;
import javax.management.remote.JMXConnector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chickling.kmanager.jmx.JMXExecutor;
import com.chickling.kmanager.jmx.KafkaJMX;
import com.chickling.kmanager.jmx.ObjectNameHolder;
import com.chickling.kmanager.model.BrokerInfo;
import com.chickling.kmanager.utils.ZKUtils;

/**
 * @author Hulva Luva.H
 * @since 2017-07-11
 *
 */
public class JMXTest {
  private static Logger LOG = LoggerFactory.getLogger(JMXTest.class);

  private static boolean excludeInternalTopic = true; // like __consumer_offsets
  private static Map<String, ObjectNameHolder> objectNames = new HashMap<String, ObjectNameHolder>();

  private static Set<String> exceptObjectNames = new HashSet<String>();


  static {
    exceptObjectNames.add("java.lang:type=GarbageCollector,name=G1 Young Generation");
  }

  public static void main(String[] args) throws Exception {

    KafkaJMX kafkaJMX = new KafkaJMX();
    ZKUtils.init("luva101:8181,luva102:8181,luva103:8181", 30000, 30000);
    // objectName_Metrics(kafkaJMX);
    objectNames(kafkaJMX);

    // initObjectNames(kafkaJMX);
  }

  public static void initObjectNames(KafkaJMX kafkaJMX) {
    try {
      if (kafkaJMX == null) {
        kafkaJMX = new KafkaJMX();
      }
      List<BrokerInfo> brokers = ZKUtils.getBrokers();
      for (BrokerInfo broker : brokers) {
        kafkaJMX.doWithConnection(broker.getHost(), broker.getJmxPort(), Optional.of(""), Optional.of(""), false, new JMXExecutor() {

          @Override
          public void doWithConnection(JMXConnector jmxConnector) {
            try {
              Set<ObjectInstance> beans = jmxConnector.getMBeanServerConnection().queryMBeans(null, null);
              ObjectNameHolder objectNameHolder = null;
              ObjectNameHolder objectNameHolderOld = null;
              for (ObjectInstance bean : beans) {
                objectNameHolder = new ObjectNameHolder();
                String objectName = bean.getObjectName().toString();
                if (excludeInternalTopic && objectName.contains("__consumer_offsets")) {
                  continue;
                }
                if (exceptObjectNames.contains(bean.getObjectName().toString())) {
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
                    currentSort += i == type_name_other.length - 1 ? tempArr[0] : tempArr[0] + ",";
                    temp.put(tempArr[0], tempArr[1]);
                  }
                }
                String key = objectNameHolder.getName() == null ? metric_other[0] + objectNameHolder.getType() + objectNameHolder.getName()
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
              LOG.error("Ops~", e);
            }
          }
        });
        objectNames.forEach((k, v) -> {
          LOG.info(k + " -> " + v);
        });
      }
    } catch (Exception e) {

    }
  }

  public static void objectNames(KafkaJMX kafkaJMX) throws Exception {
    List<BrokerInfo> brokers = ZKUtils.getBrokers();
    for (BrokerInfo broker : brokers) {
      kafkaJMX.doWithConnection(broker.getHost(), broker.getJmxPort(), Optional.of(""), Optional.of(""), false, new JMXExecutor() {

        @Override
        public void doWithConnection(JMXConnector jmxConnector) {
          // KafkaMetrics kafkaMetrics = new KafkaMetrics();
          try (FileWriter fw = new FileWriter("objectNames.json", true);
              BufferedWriter bw = new BufferedWriter(fw);
              PrintWriter out = new PrintWriter(bw)) {
            Set<ObjectInstance> beans = jmxConnector.getMBeanServerConnection().queryMBeans(null, null);
            JSONArray objectName = new JSONArray();
            for (ObjectInstance bean : beans) {
              if (excludeInternalTopic && bean.getObjectName().toString().contains("__consumer_offsets")) {
                continue;
              }
              if (exceptObjectNames.contains(bean.getObjectName().toString())) {
                continue;
              }
              System.out.println("ObjectName: " + bean.getObjectName());
              objectName.put(bean.getObjectName().toString());
              MBeanInfo mbeanInfo = jmxConnector.getMBeanServerConnection().getMBeanInfo(bean.getObjectName());
              System.out.println("\tMBeanInfo: " + mbeanInfo);
              MBeanAttributeInfo[] attributes = mbeanInfo.getAttributes();
              String[] attributeArr = new String[attributes.length];
              for (int i = 0; i < attributes.length; i++) {
                attributeArr[i] = attributes[i].getName();
              }
              AttributeList attributeList = jmxConnector.getMBeanServerConnection().getAttributes(bean.getObjectName(), attributeArr);
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

  }

  public static void objectName_Metrics(KafkaJMX kafkaJMX) throws Exception {
    List<BrokerInfo> brokers = ZKUtils.getBrokers();
    for (BrokerInfo broker : brokers) {
      kafkaJMX.doWithConnection(broker.getHost(), broker.getJmxPort(), Optional.of(""), Optional.of(""), false, new JMXExecutor() {

        @Override
        public void doWithConnection(JMXConnector jmxConnector) {
          // KafkaMetrics kafkaMetrics = new KafkaMetrics();
          try (FileWriter fw = new FileWriter("metrics.json", true);
              BufferedWriter bw = new BufferedWriter(fw);
              PrintWriter out = new PrintWriter(bw)) {
            Set<ObjectInstance> beans = jmxConnector.getMBeanServerConnection().queryMBeans(null, null);

            JSONArray objectNameMetrics = new JSONArray();
            JSONObject objectName = null;
            for (ObjectInstance bean : beans) {
              objectName = new JSONObject();
              String objectNameStr = bean.getObjectName().toString();
              if (excludeInternalTopic && objectNameStr.contains("__consumer_offsets")) {
                continue;
              }
              if (exceptObjectNames.contains(bean.getObjectName().toString())) {
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
              MBeanInfo mbeanInfo = jmxConnector.getMBeanServerConnection().getMBeanInfo(bean.getObjectName());
              System.out.println("\tMBeanInfo: " + mbeanInfo);
              MBeanAttributeInfo[] attributes = mbeanInfo.getAttributes();
              String[] attributeArr = new String[attributes.length];
              for (int i = 0; i < attributes.length; i++) {
                attributeArr[i] = attributes[i].getName();
              }
              AttributeList attributeList = jmxConnector.getMBeanServerConnection().getAttributes(bean.getObjectName(), attributeArr);
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

}
