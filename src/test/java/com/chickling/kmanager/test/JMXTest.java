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
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;

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

  public static void main(String[] args) {

    KafkaJMX kafkaJMX = new KafkaJMX();
    // objectName_Metrics(kafkaJMX);
    // objectNames(kafkaJMX);

    ZKUtils.init("10.16.238.101:8181,10.16.238.102:8181,10.16.238.103:8181", 30000, 30000);
    initObjectNames(kafkaJMX);
  }

  private static void initObjectNames(KafkaJMX kafkaJMX) {
    try {
      if (kafkaJMX == null) {
        kafkaJMX = new KafkaJMX();
      }
      List<BrokerInfo> brokers = ZKUtils.getBrokers();
      for (BrokerInfo broker : brokers) {
        kafkaJMX.doWithConnection(broker.getHost(), broker.getPort(), Optional.of(""), Optional.of(""), false, new JMXExecutor() {

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

  public static void objectNames(KafkaJMX kafkaJMX) {
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

  public static void objectName_Metrics(KafkaJMX kafkaJMX) {
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
