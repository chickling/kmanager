package com.chickling.kmanager.initialize;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.remote.JMXConnector;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chickling.kmanager.alert.TaskContent;
import com.chickling.kmanager.alert.TaskHandler;
import com.chickling.kmanager.alert.TaskManager;
import com.chickling.kmanager.alert.WorkerThreadFactory;
import com.chickling.kmanager.config.AppConfig;
import com.chickling.kmanager.core.AbstractOffsetGetter;
import com.chickling.kmanager.core.CombinedOffsetGetter;
import com.chickling.kmanager.core.db.ElasticsearchOffsetDB;
import com.chickling.kmanager.core.db.OffsetDB;
import com.chickling.kmanager.email.EmailSender;
import com.chickling.kmanager.jmx.FormatedMeterMetric;
import com.chickling.kmanager.jmx.JMXExecutor;
import com.chickling.kmanager.jmx.KafkaJMX;
import com.chickling.kmanager.jmx.KafkaMetrics;
import com.chickling.kmanager.model.BrokerInfo;
import com.chickling.kmanager.model.KafkaInfo;
import com.chickling.kmanager.utils.CommonUtils;
import com.chickling.kmanager.utils.ZKUtils;
import com.chickling.kmanager.utils.elasticsearch.Ielasticsearch;
import com.google.gson.Gson;

/**
 * @author Hulva Luva.H
 * @since 2017-07-13
 *
 */
public class SystemManager {
  private static Logger LOG = LoggerFactory.getLogger(SystemManager.class);

  // TODO schedule pool size? any other schedule?
  private static ScheduledExecutorService scheduler = null;

  private static ExecutorService worker;

  public static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

  private static final ExecutorService kafkaInfoCollectAndSavePool =
      Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE, new WorkerThreadFactory("KafkaInfo Collector"));

  public static final String JMX_METRIC_ES_DOC_TYPE = "jmxMetrics";

  public static BlockingQueue<KafkaInfo> offsetInfoCacheQueue;

  public static OffsetDB<Ielasticsearch> db = null;

  public static AbstractOffsetGetter og = null;

  public static AtomicBoolean IS_SYSTEM_READY = new AtomicBoolean(false);

  public static List<String> excludePath = new ArrayList<String>();

  public static Map<String, JMXConnector> jmxConnectors = null;

  private final static SimpleDateFormat SFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  private static AppConfig config;

  static {
    excludePath.add("/");
    excludePath.add("/views/setting.html");
    excludePath.add("/setting");
    excludePath.add("/favicon.svg");
    excludePath.add("/style.css");
    excludePath.add("/index.html");
  }

  public static AppConfig getConfig() {
    return config == null ? new AppConfig() : config;
  }

  public static String getElasticSearchOffsetType() {
    return "Offset-" + config.getClusterName().replaceAll("\\W", "");
  }

  public static String getElasticSearchJmxType() {
    return "Jmx-" + config.getClusterName().replaceAll("\\W", "");
  }

  public static synchronized void setConfig(AppConfig appConfig) throws Exception {
    config = appConfig;
    initSystem();
    // TODO
    IS_SYSTEM_READY.set(true);;
    saveToFile();
  }

  private static void saveToFile() {
    try {
      PrintWriter writer = new PrintWriter("config/system.json", "UTF-8");
      writer.println(new Gson().toJson(config));
      writer.close();
    } catch (IOException e) {
      LOG.error("Save system config to file failed!", e);
    }
  }

  private static void initSystem() {
    LOG.info("init system with config: {}", config);
    try {
      if (db != null) {
        LOG.info("ElasticsearchOffsetDB isn't null(This may be a setting change event), will shut it down and restart it...");
        db.close();
      }
      db = new ElasticsearchOffsetDB(config);
      if (!db.check()) {
        throw new RuntimeException("No elasticsearch node avialable!");
      }
      if (og != null) {
        LOG.info("CombinedOffsetGetter isn't null(This may be a setting change event), will shut it down and restart it...");
        og.close();
      }
      og = new CombinedOffsetGetter(config);
      // TODO how cheack og is avialable?

      if (scheduler != null) {
        LOG.info("ScheduledThreadPool isn't null(This may be a setting change event), will shut it down and renew it...");
        scheduler.shutdownNow();
      }
      scheduler = Executors.newScheduledThreadPool(2, new WorkerThreadFactory("FixedRateSchedule"));

      if (config.getIsAlertEnabled()) {
        LOG.info("Alert is enabled...");
        initAlert(config);
      }

      // Offset info data
      scheduler.scheduleAtFixedRate(new Runnable() {

        @Override
        public void run() {
          try {
            List<String> groups = og.getGroups();
            groups.addAll(SystemManager.og.getGroupsCommittedToBroker());
            groups.forEach(group -> {
              kafkaInfoCollectAndSavePool.submit(new GenerateKafkaInfoTask(group));
            });
          } catch (Exception e) {
            LOG.warn("Ops..." + e.getMessage());
          }
        }
      }, 1000, config.getDataCollectFrequency() * 60 * 1000, TimeUnit.MILLISECONDS);

      // JMX metrics data
      jmxMetrics();
    } catch (Exception e) {
      System.out.print(stackTraceToString(e));
      // TODO
      IS_SYSTEM_READY.set(false);
      throw new RuntimeException("Init system failed! " + e.getMessage());

    }
  }

  private static void jmxMetrics() {
    try {
      List<BrokerInfo> brokers = ZKUtils.getBrokers();
      jmxConnectors = new HashMap<String, JMXConnector>(brokers.size());
      for (BrokerInfo broker : brokers) {
        if (broker.getJmxPort() <= 0) {
          LOG.warn("JMX disabled in " + broker.getHost());
          continue;
        }
        KafkaJMX kafkaJMX = new KafkaJMX();
        kafkaJMX.doWithConnection(broker.getHost(), broker.getJmxPort(), Optional.of(""), Optional.of(""), false, new JMXExecutor() {

          @Override
          public void doWithConnection(JMXConnector jmxConnector) {
            jmxConnectors.put(broker.getHost(), jmxConnector);
          }
        });
      }

      scheduler.scheduleAtFixedRate(new Runnable() {

        @Override
        public void run() {
          try {
            Date now = new Date();
            JSONObject data = new JSONObject();

            Iterator<Entry<String, JMXConnector>> ite = jmxConnectors.entrySet().iterator();
            KafkaMetrics metrics = new KafkaMetrics();
            Entry<String, JMXConnector> brokerJmxConnector = null;
            while (ite.hasNext()) {
              brokerJmxConnector = ite.next();
              JSONObject metric = null;
              metric = new JSONObject(new FormatedMeterMetric(
                  metrics.getMessagesInPerSec(brokerJmxConnector.getValue().getMBeanServerConnection(), Optional.empty()), 0));
              metric.put("broker", brokerJmxConnector.getKey());
              metric.put("date", SFORMAT.format(now));
              metric.put("timestamp", now.getTime());
              metric.put("metric", "MessagesInPerSec");
              data.put("MessagesInPerSec" + brokerJmxConnector.getKey(), metric);

              metric = new JSONObject(new FormatedMeterMetric(
                  metrics.getBytesInPerSec(brokerJmxConnector.getValue().getMBeanServerConnection(), Optional.empty())));
              metric.put("broker", brokerJmxConnector.getKey());
              metric.put("date", SFORMAT.format(now));
              metric.put("timestamp", now.getTime());
              metric.put("metric", "BytesInPerSec");
              data.put("BytesInPerSec" + brokerJmxConnector.getKey(), metric);

              metric = new JSONObject(new FormatedMeterMetric(
                  metrics.getBytesOutPerSec(brokerJmxConnector.getValue().getMBeanServerConnection(), Optional.empty())));
              metric.put("broker", brokerJmxConnector.getKey());
              metric.put("date", SFORMAT.format(now));
              metric.put("timestamp", now.getTime());
              metric.put("metric", "BytesOutPerSec");
              data.put("BytesOutPerSec" + brokerJmxConnector.getKey(), metric);
            }

            db.getDB().bulkIndex(data, SystemManager.getElasticSearchJmxType(), config.getEsIndex() + "-");
          } catch (Exception e) {
            LOG.warn("Gather JMX info went wrong...", e);
          }
        }
      }, 0, config.getDataCollectFrequency() * 60 * 1000, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      LOG.warn("Ops..." + e.getMessage());
    } finally {
      try {
        if (jmxConnectors != null) {
        }
      } catch (Exception e) {
        LOG.error("Close JMXConnector error! " + e.getMessage());
      }
    }
  }

  public static String stackTraceToString(Throwable e) {
    StringBuilder sb = new StringBuilder();
    for (StackTraceElement element : e.getStackTrace()) {
      sb.append(element.toString());
      sb.append("\n");
    }
    return sb.toString();
  }

  private static void initAlert(AppConfig config) {
    EmailSender.setConfig(config);
    TaskManager.init(config);
    if (offsetInfoCacheQueue != null) {
      LOG.info("offsetInfoCacheQueue isn't null, do clear...");
      offsetInfoCacheQueue.clear();
    } else {
      LOG.info("new offsetInfoCacheQueue...");
      offsetInfoCacheQueue = new LinkedBlockingQueue<KafkaInfo>(config.getOffsetInfoCacheQueue());
    }
    if (worker != null) {
      LOG.info("AlertTaskChecker isn't null, will shut it down and renew it...");
      worker.shutdownNow();
    }
    int corePoolSize = config.getOffsetInfoHandler() != null ? config.getOffsetInfoHandler() : DEFAULT_THREAD_POOL_SIZE;
    if (worker != null) {
      LOG.info("AlertTaskChecker isn't null, will shut it down and renew it...");
      worker.shutdownNow();
    }
    worker = Executors.newFixedThreadPool(corePoolSize, new WorkerThreadFactory("AlertTaskChecker"));

    for (int i = 0; i < corePoolSize; i++) {
      worker.submit(new TaskHandler());
    }

    File dir = new File(config.getTaskFolder());
    if (!dir.exists()) {
      dir.mkdir();
    }
    // Scan task folder load tasks to memory
    File[] listOfFiles = dir.listFiles();
    for (File file : listOfFiles) {
      if (file.isFile() && (file.getName().substring(file.getName().lastIndexOf('.') + 1).equals("task"))) {
        String strTaskContent = CommonUtils.loadFileContent(file.getAbsolutePath());
        TaskManager.addTask(new Gson().fromJson(strTaskContent, TaskContent.class));
      }
    }
  }
}
