package com.chickling.kmanager.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chickling.kmanager.jmx.JMXExecutor;
import com.chickling.kmanager.jmx.KafkaJMX;
import com.chickling.kmanager.model.BrokerInfo;
import com.chickling.kmanager.utils.ZKUtils;
import com.chickling.kmanager.utils.elasticsearch.javaapi.ElasticsearchJavaUtil;

import scala.App;

/**
 * @author Hulva Luva.H from ECBD
 * @date 2017年7月25日
 * @description
 *
 */
public class SaveJMXMetricsToES {
  private static Logger LOG = LoggerFactory.getLogger(App.class);
  private static boolean excludeInternalTopic = true; // like __consumer_offsets
  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

  private static final String indexPrefix = "logx_monitor_kafka-";
  private static final String docType = "kafkajmx";
  private static final SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");

  public static void main(String[] args) {
    ZKUtils.init("10.16.238.101:8181,10.16.238.102:8181,10.16.238.103:8181", 30000, 30000);
    // ElasticsearchJavaUtil es = new
    // ElasticsearchJavaUtil("10.16.238.82:9300,10.16.238.83:9300,10.16.238.84:9300");
    ElasticsearchJavaUtil es = new ElasticsearchJavaUtil("10.16.232.120:9300");
    KafkaJMX kafkaJMX = new KafkaJMX();

    scheduler.scheduleAtFixedRate(new Runnable() {

      @Override
      public void run() {
        try {

          List<BrokerInfo> brokers = ZKUtils.getBrokers();
          for (BrokerInfo broker : brokers) {
            kafkaJMX.doWithConnection(broker.getHost(), broker.getPort(), Optional.of(""), Optional.of(""), false, new JMXExecutor() {

              @Override
              public void doWithConnection(MBeanServerConnection mbsc) {
                JSONObject objectName = null;
                try {
                  Date now = new Date();
                  SimpleDateFormat sFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                  String indexSufix = sFormat.format(now);
                  Set<ObjectInstance> beans = mbsc.queryMBeans(null, null);

                  for (ObjectInstance bean : beans) {
                    objectName = new JSONObject();
                    objectName.put("timestamp", now.getTime());
                    objectName.put("formatedDate", sFormat1.format(now));
                    String objectNameStr = bean.getObjectName().toString();
                    if (excludeInternalTopic && objectNameStr.contains("__consumer_offsets")) {
                      continue;
                    }
                    if (objectNameStr.contains("java.lang:type=Runtime")) {
                      continue;
                    }
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
                    MBeanAttributeInfo[] attributes = mbeanInfo.getAttributes();
                    String[] attributeArr = new String[attributes.length];
                    for (int i = 0; i < attributes.length; i++) {
                      attributeArr[i] = attributes[i].getName();
                    }
                    AttributeList attributeList = mbsc.getAttributes(bean.getObjectName(), attributeArr);
                    List<Attribute> attributeList1 = attributeList.asList();

                    for (Attribute attr : attributeList1) {
                      if (attr.getValue() == null) {
                        objectName.put(attr.getName(), "NA");
                      } else if (attr.getValue().equals(Double.NEGATIVE_INFINITY)) {
                        objectName.put(attr.getName(), attr.getValue() + "");
                      } else if ("kafka.server:type=KafkaServer,name=ClusterId".equalsIgnoreCase(bean.getObjectName().toString())) {
                        objectName.put("ClusterId" + attr.getName(), attr.getValue() + "");
                      } else {
                        objectName.put(attr.getName(), attr.getValue());
                      }
                    }
                     es.bulkIndex(objectName, indexPrefix + indexSufix, docType);
                  }
                } catch (Exception e) {
                  LOG.error("Ops~" + objectName, e);
                }
              }
            });
          }
//           es.bulkIndex(null, null, null);
        } catch (Exception e) {
          LOG.warn("Ops..." + e.getMessage());
        }
      }
    }, 0, 1 * 60 * 1000, TimeUnit.MILLISECONDS);
  }
}
