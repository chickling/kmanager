package com.chickling.kmonitor.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.management.MBeanServerConnection;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.chickling.kmonitor.initialize.SystemManager;
import com.chickling.kmonitor.jmx.FormatedMeterMetric;
import com.chickling.kmonitor.jmx.JMXExecutor;
import com.chickling.kmonitor.jmx.KafkaJMX;
import com.chickling.kmonitor.jmx.KafkaMetrics;
import com.chickling.kmonitor.jmx.MeterMetric;
import com.chickling.kmonitor.utils.ZKUtils;

/**
 * 
 * @author Hulva Luva.H
 * @since 2017-07-12
 *
 */

@RestController
@RequestMapping("/metrics")
public class JMXMetricController {
  private static Logger LOG = LoggerFactory.getLogger(JMXMetricController.class);

  @RequestMapping(value = "/brokerTopicMetrics/brokers", method = RequestMethod.GET)
  public String getBrokerTopicMetricsForBrokers() {
    Map<String, MeterMetric> result = new HashMap<String, MeterMetric>();
    try {
      List<String> jmxHosts = ZKUtils.getKafkaJMXHostsFromZookeeper();
      for (String jmxHost : jmxHosts) {
        String[] jmxArr = jmxHost.split(":");
        if ("-1".equals(jmxArr[2])) {
          continue;
        }
        KafkaJMX kafkaJMX = new KafkaJMX();
        kafkaJMX.doWithConnection(jmxArr[1], Integer.parseInt(jmxArr[2]), Optional.of(""), Optional.of(""), false, new JMXExecutor() {

          @Override
          public void doWithConnection(MBeanServerConnection mBeanServerConnection) {
            KafkaMetrics metrics = new KafkaMetrics();
            if (result.containsKey("MessagesInPerSec")) {
              result.put("MessagesInPerSec",
                  merge(result.get("MessagesInPerSec"), metrics.getMessagesInPerSec(mBeanServerConnection, Optional.empty())));
            } else {
              result.put("MessagesInPerSec", metrics.getMessagesInPerSec(mBeanServerConnection, Optional.empty()));
            }

            if (result.containsKey("BytesInPerSec")) {
              result.put("BytesInPerSec",
                  merge(result.get("BytesInPerSec"), metrics.getBytesInPerSec(mBeanServerConnection, Optional.empty())));
            } else {
              result.put("BytesInPerSec", metrics.getBytesInPerSec(mBeanServerConnection, Optional.empty()));
            }

            if (result.containsKey("BytesOutPerSec")) {
              result.put("BytesOutPerSec",
                  merge(result.get("BytesOutPerSec"), metrics.getBytesOutPerSec(mBeanServerConnection, Optional.empty())));
            } else {
              result.put("BytesOutPerSec", metrics.getBytesOutPerSec(mBeanServerConnection, Optional.empty()));
            }

            if (result.containsKey("BytesRejectedPerSec")) {
              result.put("BytesRejectedPerSec",
                  merge(result.get("BytesRejectedPerSec"), metrics.getBytesRejectedPerSec(mBeanServerConnection, Optional.empty())));
            } else {
              result.put("BytesRejectedPerSec", metrics.getBytesRejectedPerSec(mBeanServerConnection, Optional.empty()));
            }

            if (result.containsKey("FailedFetchRequestsPerSec")) {
              result.put("FailedFetchRequestsPerSec", merge(result.get("FailedFetchRequestsPerSec"),
                  metrics.getFailedFetchRequestsPerSec(mBeanServerConnection, Optional.empty())));
            } else {
              result.put("FailedFetchRequestsPerSec", metrics.getFailedFetchRequestsPerSec(mBeanServerConnection, Optional.empty()));
            }

            if (result.containsKey("FailedProduceRequestsPerSec")) {
              result.put("FailedProduceRequestsPerSec", merge(result.get("FailedProduceRequestsPerSec"),
                  metrics.getFailedProduceRequestsPerSec(mBeanServerConnection, Optional.empty())));
            } else {
              result.put("FailedProduceRequestsPerSec", metrics.getFailedProduceRequestsPerSec(mBeanServerConnection, Optional.empty()));
            }
          }
        });
      }
    } catch (Exception e) {
      LOG.error("Get jmxHosts error!" + e.getMessage());
    }
    JSONObject response = new JSONObject();
    Set<String> keys = result.keySet();
    for (String key : keys) {
      if ("MessagesInPerSec".equals(key)) {
        response.put(key, new JSONObject(new FormatedMeterMetric(result.get(key), 0)));
      } else {
        response.put(key, new JSONObject(new FormatedMeterMetric(result.get(key))));
      }
    }
    response.put("esUrl", SystemManager.getConfig().getEsHosts().split(":")[0] + ":9200");
    return response.toString();
  }

  protected MeterMetric merge(MeterMetric old, MeterMetric newOne) {
    return new MeterMetric(old.getCount() + newOne.getCount(), old.getMeanRate() + newOne.getMeanRate(),
        old.getOneMinuteRate() + newOne.getOneMinuteRate(), old.getFiveMinuteRate() + newOne.getFiveMinuteRate(),
        old.getFifteenMinuteRate() + newOne.getFifteenMinuteRate());
  }

  @RequestMapping(value = "/brokerTopicMetrics/broker/{bid}", method = RequestMethod.GET)
  public String getBrokerTopicMetricsForBroker(@PathVariable String bid) {
    JSONObject response = new JSONObject();
    try {
      List<String> jmxHosts = ZKUtils.getKafkaJMXHostsFromZookeeper();
      for (String jmxHost : jmxHosts) {
        String[] jmxArr = jmxHost.split(":");
        if (bid.equals(jmxArr[0])) {
          if ("-1".equals(jmxArr[2])) {
            return response.toString();
          }
          KafkaJMX kafkaJMX = new KafkaJMX();
          kafkaJMX.doWithConnection(jmxArr[1], Integer.parseInt(jmxArr[2]), Optional.of(""), Optional.of(""), false, new JMXExecutor() {

            @Override
            public void doWithConnection(MBeanServerConnection mBeanServerConnection) {
              KafkaMetrics metrics = new KafkaMetrics();
              response.put("MessagesInPerSec",
                  new JSONObject(new FormatedMeterMetric(metrics.getMessagesInPerSec(mBeanServerConnection, Optional.empty()), 0)));
              response.put("BytesInPerSec",
                  new JSONObject(new FormatedMeterMetric(metrics.getBytesInPerSec(mBeanServerConnection, Optional.empty()))));
              response.put("BytesOutPerSec",
                  new JSONObject(new FormatedMeterMetric(metrics.getBytesOutPerSec(mBeanServerConnection, Optional.empty()))));
              response.put("BytesRejectedPerSec",
                  new JSONObject(new FormatedMeterMetric(metrics.getBytesRejectedPerSec(mBeanServerConnection, Optional.empty()))));
              response.put("FailedFetchRequestsPerSec",
                  new JSONObject(new FormatedMeterMetric(metrics.getFailedFetchRequestsPerSec(mBeanServerConnection, Optional.empty()))));
              response.put("FailedProduceRequestsPerSec",
                  new JSONObject(new FormatedMeterMetric(metrics.getFailedProduceRequestsPerSec(mBeanServerConnection, Optional.empty()))));
            }
          });
        }
      }
    } catch (Exception e) {
      LOG.error("Get jmxHosts error!" + e.getMessage());
    }
    return response.toString();
  }

  @RequestMapping(value = "/brokerTopicMetrics/topic/{topic}", method = RequestMethod.GET)
  public String getBrokerTopicMetrics(@PathVariable String topic) {
    Map<String, MeterMetric> result = new HashMap<String, MeterMetric>();
    try {
      List<String> jmxHosts = ZKUtils.getKafkaJMXHostsFromZookeeper();
      for (String jmxHost : jmxHosts) {
        String[] jmxArr = jmxHost.split(":");
        if ("-1".equals(jmxArr[2])) {
          continue;
        }
        KafkaJMX kafkaJMX = new KafkaJMX();
        kafkaJMX.doWithConnection(jmxArr[1], Integer.parseInt(jmxArr[2]), Optional.of(""), Optional.of(""), false, new JMXExecutor() {

          @Override
          public void doWithConnection(MBeanServerConnection mBeanServerConnection) {
            KafkaMetrics metrics = new KafkaMetrics();
            if (result.containsKey("MessagesInPerSec")) {
              result.put("MessagesInPerSec",
                  merge(result.get("MessagesInPerSec"), metrics.getMessagesInPerSec(mBeanServerConnection, Optional.of(topic))));
            } else {
              result.put("MessagesInPerSec", metrics.getMessagesInPerSec(mBeanServerConnection, Optional.of(topic)));
            }
            if (result.containsKey("BytesInPerSec")) {
              result.put("BytesInPerSec",
                  merge(result.get("BytesInPerSec"), metrics.getBytesInPerSec(mBeanServerConnection, Optional.of(topic))));
            } else {
              result.put("BytesInPerSec", metrics.getBytesInPerSec(mBeanServerConnection, Optional.of(topic)));
            }

            if (result.containsKey("BytesOutPerSec")) {
              result.put("BytesOutPerSec",
                  merge(result.get("BytesOutPerSec"), metrics.getBytesOutPerSec(mBeanServerConnection, Optional.of(topic))));
            } else {
              result.put("BytesOutPerSec", metrics.getBytesOutPerSec(mBeanServerConnection, Optional.of(topic)));
            }

            if (result.containsKey("BytesRejectedPerSec")) {
              result.put("BytesRejectedPerSec",
                  merge(result.get("BytesRejectedPerSec"), metrics.getBytesRejectedPerSec(mBeanServerConnection, Optional.of(topic))));
            } else {
              result.put("BytesRejectedPerSec", metrics.getBytesRejectedPerSec(mBeanServerConnection, Optional.of(topic)));
            }

            if (result.containsKey("FailedFetchRequestsPerSec")) {
              result.put("FailedFetchRequestsPerSec", merge(result.get("FailedFetchRequestsPerSec"),
                  metrics.getFailedFetchRequestsPerSec(mBeanServerConnection, Optional.empty())));
            } else {
              result.put("FailedFetchRequestsPerSec", metrics.getFailedFetchRequestsPerSec(mBeanServerConnection, Optional.of(topic)));
            }
            if (result.containsKey("FailedProduceRequestsPerSec")) {
              result.put("FailedProduceRequestsPerSec", merge(result.get("FailedProduceRequestsPerSec"),
                  metrics.getFailedProduceRequestsPerSec(mBeanServerConnection, Optional.empty())));
            } else {
              result.put("FailedProduceRequestsPerSec", metrics.getFailedProduceRequestsPerSec(mBeanServerConnection, Optional.of(topic)));
            }
          }

        });
      }
    } catch (Exception e) {
      LOG.error("Get jmxHosts error!" + e.getMessage());

    }
    JSONObject response = new JSONObject();
    Set<String> keys = result.keySet();
    for (String key : keys) {
      if ("MessagesInPerSec".equals(key)) {
        response.put(key, new JSONObject(new FormatedMeterMetric(result.get(key), 0)));
      } else {
        response.put(key, new JSONObject(new FormatedMeterMetric(result.get(key))));
      }
    }
    return response.toString();
  }
}
