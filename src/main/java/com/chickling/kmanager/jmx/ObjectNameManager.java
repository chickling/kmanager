package com.chickling.kmanager.jmx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.management.ObjectInstance;
import javax.management.remote.JMXConnector;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chickling.kmanager.model.BrokerInfo;
import com.chickling.kmanager.utils.ZKUtils;

/**
 * 
 * @author Hulva Luva.H
 * @since 2017-07-23
 *
 */
public class ObjectNameManager {
  private static Logger LOG = LoggerFactory.getLogger(ObjectNameManager.class);

  private static boolean excludeInternalTopic = true; // like __consumer_offsets

  private static Map<String, ObjectNameHolder> objectNames = new HashMap<String, ObjectNameHolder>();
  // private static

  private static KafkaJMX kafkaJMX = null;

  private static ObjectNameManager objectNameManager = null;

  public static ObjectNameManager getInstance() {
    return objectNameManager == null ? new ObjectNameManager() : objectNameManager;
  }

  private ObjectNameManager() {
    initObjectNames();
  }

  public Map<String, ObjectNameHolder> getObjectNames() {
    return objectNames;
  }

  public void refreshObjectNames() {
    initObjectNames();
  }

  private void initObjectNames() {
    try {
      if (kafkaJMX == null) {
        kafkaJMX = new KafkaJMX();
      }
      List<BrokerInfo> brokers = ZKUtils.getBrokers();
      for (BrokerInfo broker : brokers) {
        if (broker.getJmxPort() <= 0) {
          continue;
        }
        kafkaJMX.doWithConnection(broker.getHost(), broker.getJmxPort(), Optional.of(""), Optional.of(""), false, new JMXExecutor() {

          @SuppressWarnings("unchecked")
          @Override
          public void doWithConnection(JMXConnector jmxConnector) {
            try {
              Set<ObjectInstance> beans = jmxConnector.getMBeanServerConnection().queryMBeans(null, null);

              beans.forEach(bean -> {
                ObjectNameHolder objectNameHolder = new ObjectNameHolder();
                String objectName = bean.getObjectName().toString();
                if (excludeInternalTopic && !objectName.contains("__consumer_offsets")) {
                  String[] metric_other = objectName.split(":");
                  objectNameHolder.setMetric(metric_other[0]);
                  String[] type_name_other = metric_other[1].split(",");
                  String firstLevelK = null;
                  for (int i = 0; i < type_name_other.length; i++) {
                    String[] tempArr = type_name_other[i].split("=");
                    if ("type".equalsIgnoreCase(tempArr[0])) {
                      objectNameHolder.setType(tempArr[1]);
                    } else if ("name".equalsIgnoreCase(tempArr[0])) {
                      objectNameHolder.setName(tempArr[1]);
                    } else {
                      String key = objectNameHolder.getName() == null ? metric_other[0] + objectNameHolder.getType()
                          : metric_other[0] + objectNameHolder.getType() + objectNameHolder.getName();

                      if (objectNames.containsKey(key)) {
                        ObjectNameHolder objectNameHolderOld = objectNames.get(key);
                        Map<String, Object> extras = objectNameHolderOld.getExtra();
                        if (extras == null) {
                          extras = new HashMap<String, Object>();
                        }

                        if (extras.containsKey(firstLevelK)) {
                          String secondLevelK = tempArr[0] + "=" + tempArr[1];
                          Map<String, Object> firstLevelInExtras = null;
                          if (extras.get(firstLevelK).equals(JSONObject.NULL)) {
                            firstLevelInExtras = new HashMap<String, Object>();
                            firstLevelInExtras.put(secondLevelK, JSONObject.NULL);
                            extras.put(firstLevelK, firstLevelInExtras);
                          }
                          firstLevelInExtras = (Map<String, Object>) extras.get(firstLevelK);

                          // has next level?
                          if (i == type_name_other.length - 1) {
                            continue;
                          }
                          i++;
                          tempArr = type_name_other[i].split("=");
                          String thirdLevelK = tempArr[0] + "=" + tempArr[1];
                          Map<String, Object> secondLevelInExtras = null;
                          if (firstLevelInExtras.get(secondLevelK).equals(JSONObject.NULL)) {
                            secondLevelInExtras = new HashMap<String, Object>();
                            secondLevelInExtras.put(thirdLevelK, JSONObject.NULL);
                            firstLevelInExtras.put(secondLevelK, secondLevelInExtras);
                          }
                          secondLevelInExtras = (Map<String, Object>) firstLevelInExtras.get(secondLevelK);

                          // has next level?
                          if (i == type_name_other.length - 1) {
                            continue;
                          }
                          i++;
                          tempArr = type_name_other[i].split("=");
                          String fourthLevelK = tempArr[0] + "=" + tempArr[1];
                          Map<String, Object> thirdLevelInExtras = null;
                          if (firstLevelInExtras.get(thirdLevelK).equals(JSONObject.NULL)) {
                            thirdLevelInExtras = new HashMap<String, Object>();
                            thirdLevelInExtras.put(fourthLevelK, JSONObject.NULL);
                            secondLevelInExtras.put(thirdLevelK, thirdLevelInExtras);
                          }
                          // thirdLevelInExtras = (Map<String, Object>) secondLevelInExtras.get(thirdLevelK);
                          // TODO so far as i know, there's no more than two level
                          if (i < type_name_other.length - 1) {
                            LOG.warn("Ops~ There's do have a objectName over thrid level! objectName -> " + objectName);
                          }
                        } else {
                          // TODO that Metric has same metric, type and name with an exits one
                          firstLevelK = tempArr[0] + "=" + tempArr[1];
                          extras.put(firstLevelK, JSONObject.NULL);
                        }
                      } else {
                        firstLevelK = tempArr[0] + "=" + tempArr[1];
                        Map<String, Object> extras = new HashMap<String, Object>();
                        extras.put(firstLevelK, JSONObject.NULL);
                        objectNameHolder.setExtra(extras);
                        objectNames.put(key, objectNameHolder);
                      }
                    }
                  }
                }
              });
            } catch (Exception e) {
              LOG.warn("Ops~ Get objectNames - " + e.getMessage());
            }
          }
        });
      }
    } catch (Exception e) {
      LOG.error("initObjectNames went wrong! " + e.getMessage());
    }
  }

}
