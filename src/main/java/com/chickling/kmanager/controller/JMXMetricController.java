package com.chickling.kmanager.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.management.MBeanServerConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chickling.kmanager.jmx.JMXExecutor;
import com.chickling.kmanager.jmx.KafkaJMX;
import com.chickling.kmanager.jmx.KafkaMetrics;
import com.chickling.kmanager.jmx.MeterMetric;
import com.chickling.kmanager.model.BrokerInfo;
import com.chickling.kmanager.utils.ZKUtils;
import com.chickling.kmanager.utils.elasticsearch.restapi.ElasticsearchRESTUtil;

/**
 * 
 * @author Hulva Luva.H
 * @since 2017-07-12
 *
 */

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/stats")
public class JMXMetricController {
	private static Logger LOG = LoggerFactory.getLogger(JMXMetricController.class);

	@RequestMapping(value = "/trend", method = RequestMethod.GET)
	public HashMap<String, ArrayList<Long[]>> trend(@RequestParam(value="start", defaultValue="") String start
		, @RequestParam(value="end", defaultValue="") String end) {
		return ElasticsearchRESTUtil.JmxTrend(start, end);
	}

	@RequestMapping(value = "/topic/{topic}", method = RequestMethod.GET)
	public Map<String, Map<String, MeterMetric>> getMetricsByTopic(@PathVariable String topic) {
		return getMetrics(-1, Optional.of(topic));
	}

	@RequestMapping(value = "/brokers", method = RequestMethod.GET)
	public Map<String, Map<String, MeterMetric>> getMetrics() {
		return getMetrics(-1, Optional.empty());
	}

	@RequestMapping(value = "/broker/{bid}", method = RequestMethod.GET)
	public Map<String, MeterMetric> getMetricsByBroker(@PathVariable Integer bid) {
		Map<String, Map<String, MeterMetric>> metrics =  getMetrics(bid, Optional.empty());
		if(metrics.containsKey("broker" + bid)){
			return metrics.get("broker" + bid);
		}
		return new HashMap<String, MeterMetric>();
	}
	private Map<String, Map<String, MeterMetric>> getMetrics(Integer bid, Optional<String> topic) {
		Map<String, Map<String, MeterMetric>> result = new HashMap<String, Map<String, MeterMetric>>();

		try {
			List<BrokerInfo> brokers = ZKUtils.getBrokers();
			for (BrokerInfo broker : brokers) {
				if (broker.getJmxPort() <= 0) {
					continue;
				}
				if (bid >= 0 && broker.getBid() != bid) {
					continue;
				}
				KafkaJMX kafkaJMX = new KafkaJMX();
				kafkaJMX.doWithConnection(broker.getHost(), broker.getJmxPort(), Optional.of(""), Optional.of(""),
						false, new JMXExecutor() {
							@Override
							public void doWithConnection(MBeanServerConnection mBeanServerConnection) {
								Map<String, MeterMetric> item = getMetricsFromMBean(mBeanServerConnection, topic);
								result.put("broker" + broker.getBid(), item);
							}
						});
			}
		} catch (Exception e) {
			LOG.error("Get jmxHosts error!" + e.getMessage());
		}
		if (bid < 0) {
			Map<String, MeterMetric> summary = new HashMap<String, MeterMetric>();
			result.forEach((key, itemObj) -> {
				Map<String, MeterMetric> item = (Map<String, MeterMetric>) itemObj;
				for (String metric : metricName) {
					summary.put(metric, merge(summary.get(metric), item.get(metric)));
				}
			});
			result.put("summary", summary);
		}
		return result;
	}

	String[] metricName = new String[] { "MessagesInPerSec", "BytesInPerSec", "BytesOutPerSec", "BytesRejectedPerSec",
			"FailedFetchRequestsPerSec", "FailedProduceRequestsPerSec" };

	private Map<String, MeterMetric> getMetricsFromMBean(MBeanServerConnection mBeanServerConnection,
			Optional<String> topic) {
		Map<String, MeterMetric> result = new HashMap<String, MeterMetric>();
		KafkaMetrics metrics = new KafkaMetrics();
		for (String metric : metricName) {
			if (result.containsKey(metric)) {
				result.put(metric, merge(result.get(metric), metrics.getItem(mBeanServerConnection, topic, metric)));
			} else {
				result.put(metric, metrics.getItem(mBeanServerConnection, topic, metric));
			}
		}
		return result;
	}

	protected MeterMetric merge(MeterMetric old, MeterMetric newOne) {
		if (old == null) {
			return newOne;
		}
		if (newOne == null) {
			return new MeterMetric();
		}
		return new MeterMetric(old.getCount() + newOne.getCount(), old.getMeanRate() + newOne.getMeanRate(),
				old.getOneMinuteRate() + newOne.getOneMinuteRate(),
				old.getFiveMinuteRate() + newOne.getFiveMinuteRate(),
				old.getFifteenMinuteRate() + newOne.getFifteenMinuteRate());
	}
}
