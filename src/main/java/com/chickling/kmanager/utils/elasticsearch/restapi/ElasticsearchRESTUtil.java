package com.chickling.kmanager.utils.elasticsearch.restapi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.chickling.kmanager.alert.WorkerThreadFactory;
import com.chickling.kmanager.initialize.SystemManager;
import com.chickling.kmanager.model.ElasticsearchAssistEntity;
import com.chickling.kmanager.model.OffsetHistoryQueryParams;
import com.chickling.kmanager.model.OffsetPoints;
import com.chickling.kmanager.model.OffsetStat;
import com.chickling.kmanager.utils.elasticsearch.Ielasticsearch;

/**
 * @author Hulva Luva.H
 *
 */
public class ElasticsearchRESTUtil implements Ielasticsearch {
  private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchRESTUtil.class);
  private static String RERST_HOST;
  private static RestTemplate REST;
  private static HttpComponentsClientHttpRequestFactory httpRequestFactory;
  private static HttpHeaders headers;

  static {
    httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
    httpRequestFactory.setConnectTimeout(300000); // 5min
    httpRequestFactory.setConnectionRequestTimeout(300000);
    httpRequestFactory.setReadTimeout(300000);
    REST = new RestTemplate();
    REST.setRequestFactory(httpRequestFactory);
    headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add("Accept", "*/*");
  }

  public ElasticsearchRESTUtil(String restHost) {
    RERST_HOST = restHost;
  }

  public String getHost() {
    String[] hosts = RERST_HOST.split("[,;]");
    return hosts[new Random().nextInt(hosts.length)];
  }

  /**
   * GET _cluster/health
   * 
   * <code>
   * 	{
  		"cluster_name" : "testcluster",
  		"status" : "yellow",
  		"timed_out" : false,
  		"number_of_nodes" : 1,
  		"number_of_data_nodes" : 1,
  		"active_primary_shards" : 5,
  		"active_shards" : 5,
  		"relocating_shards" : 0,
  		"initializing_shards" : 0,
  		"unassigned_shards" : 5,
  		"delayed_unassigned_shards": 0,
  		"number_of_pending_tasks" : 0,
  		"number_of_in_flight_fetch": 0,
  		"task_max_waiting_in_queue_millis": 0,
  		"active_shards_percent_as_number": 50.0
  	}		
   * </code>
   * 
   * @return just true
   */
  public boolean check() {

    return true;
  }

  public void bulkIndex(JSONObject data, String docType, String indexPrefix) {
    StringBuilder bulkData = new StringBuilder();
    SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
    Date now = new Date();
    String indexSufix = sFormat.format(now);

    boolean hasData = false;
    Iterator<String> keys = data.keys();
    while (keys.hasNext()) {
      hasData = true;
      bulkData.append("{\"index\": {\"_index\":\"" + indexPrefix + indexSufix + "\",\"_type\":\"" + docType + "\"}}")
          .append("\n");
      bulkData.append(data.getJSONObject(keys.next()).toString()).append("\n");
    }
    if (!hasData) {
      return;
    }

    ResponseEntity<String> response = REST.exchange("http://" + getHost() + "/_bulk", HttpMethod.POST,
        new HttpEntity<String>(bulkData.toString(), headers), String.class);
    // TODO Do something with response?
    response.getBody();
  }

  public List<OffsetPoints> scrollsSearcher(OffsetHistoryQueryParams params, String docType, String indexPrefix) {
    ExecutorService pool = Executors.newFixedThreadPool(SystemManager.DEFAULT_THREAD_POOL_SIZE,
        new WorkerThreadFactory("OffsetHistoryQuery-RESTAPI"));

    List<OffsetPoints> result = new ArrayList<OffsetPoints>();

    SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    try {
      String indexNameSearch = indexPrefix + "*";

      String rangeFrom = getRangeFrom(params);

      List<String> concernedTimestamp = dateHistogram(sFormat.parse(rangeFrom).getTime(),
          Long.parseLong(params.getRangeto()), params.getInterval());

      List<Future<List<OffsetPoints>>> futureList = new ArrayList<Future<List<OffsetPoints>>>();

      ResponseEntity<String> response = REST.exchange(
          "http://" + getHost() + "/" + indexNameSearch + "/" + docType + "/_search?scroll=1m", HttpMethod.POST,
          new HttpEntity<String>(ScrollSearchTemplate.getScrollSearchBody(params.getTopic(), params.getGroup(),
              rangeFrom, sFormat.format(new Date(Long.parseLong(params.getRangeto())))), headers),
          String.class);

      JSONObject searchResult = null;
      while (true) {
        searchResult = new JSONObject(response.getBody());
        final JSONArray searchHits = searchResult.getJSONObject("hits").getJSONArray("hits");
        if (searchHits.length() == 0) {
          break;
        }
        try {
          Future<List<OffsetPoints>> future = null;
          future = pool.submit(new GenerateOffsetHistoryDataset(searchHits, concernedTimestamp));
          futureList.add(future);
        } catch (Exception e) {
          LOG.warn("Ops...GenerateOffsetHistoryDataset went wrong! " + e.getMessage());
        }

        response = REST.exchange("http://" + getHost() + "/_search/scroll", HttpMethod.POST, new HttpEntity<String>(
            ScrollSearchTemplate.getScrollNextBody(searchResult.getString("_scroll_id")), headers), String.class);
      }
      for (Future<List<OffsetPoints>> future : futureList) {
        try {
          result.addAll(future.get());
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          LOG.error("Interrupted when get GenerateOffsetHistoryDataset in future...", e);
        } catch (ExecutionException e) {
          // TODO Auto-generated catch block
          LOG.error("QAQ when get GenerateOffsetHistoryDataset in future...", e);
        }
      }
      pool.shutdown();
    } catch (

    Exception e) {
      // TODO
      LOG.error("Damn...", e);
    }
    return result;
  }

  private List<String> dateHistogram(long from, long to, String interval) {
    switch (interval) {
    case "1m":
      return dateHistogram(from, to, 1 * 60 * 1000);
    case "10m":
      return dateHistogram(from, to, 10 * 60 * 1000);
    case "30m":
      return dateHistogram(from, to, 30 * 60 * 1000);
    case "1h":
      return dateHistogram(from, to, 1 * 60 * 60 * 1000);
    case "1d":
      return dateHistogram(from, to, 24 * 60 * 60 * 1000);
    default:
      return dateHistogram(from, to, 1 * 60 * 1000);
    }
  }

  private List<String> dateHistogram(long from, long to, long interval) {
    SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    List<String> histogramedTimestamp = new ArrayList<String>();
    for (long i = from; i < to + interval; i = i + interval) {
      histogramedTimestamp.add(sFormat.format(new Date(i)));
    }
    return histogramedTimestamp;
  }

  private String getRangeFrom(OffsetHistoryQueryParams params) throws ParseException {
    SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date(Long.parseLong(params.getRangeto())));
    switch (params.getRange()) {
    case "1h":
      cal.add(Calendar.HOUR, -1);
      break;
    case "8h":
      cal.add(Calendar.HOUR, -8);
      break;
    case "16h":
      cal.add(Calendar.HOUR, -16);
      break;
    case "1d":
      cal.add(Calendar.DATE, -1);
      break;
    case "2d":
      cal.add(Calendar.DATE, -2);
      break;
    case "1w":
      cal.add(Calendar.WEEK_OF_MONTH, -1);
      break;
    default:
      cal.add(Calendar.HOUR, -1);
      break;
    }
    return sFormat.format(cal.getTime());
  }

  // private int calculatePageSize(LogsizeRequestParams params, String
  // rangeFrom) throws ParseException {
  // SimpleDateFormat sFormat = new
  // SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
  // switch (params.getInterval()) {
  // case "5m":
  // return (int) ((Long.parseLong(params.getRangeTo()) -
  // sFormat.parse(rangeFrom).getTime()) / (5 * 60 * 1000));
  // case "30m":
  // return (int) ((Long.parseLong(params.getRangeTo()) -
  // sFormat.parse(rangeFrom).getTime())
  // / (30 * 60 * 1000));
  // case "1h":
  // return (int) ((Long.parseLong(params.getRangeTo()) -
  // sFormat.parse(rangeFrom).getTime())
  // / (60 * 60 * 1000));
  // case "1d":
  // return (int) ((Long.parseLong(params.getRangeTo()) -
  // sFormat.parse(rangeFrom).getTime())
  // / (24 * 60 * 60 * 1000));
  // default:
  // return (int) ((Long.parseLong(params.getRangeTo()) -
  // sFormat.parse(rangeFrom).getTime())
  // / (60 * 60 * 1000));
  // }
  // }

  public List<OffsetPoints> offsetHistory(String indexPrefix, String docType, String group, String topic) {
    ExecutorService pool = Executors.newFixedThreadPool(SystemManager.DEFAULT_THREAD_POOL_SIZE,
        new WorkerThreadFactory("OffsetHistoryQuery-RESTAPI"));

    List<OffsetPoints> result = new ArrayList<OffsetPoints>();
    SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    try {
      String indexNameSearch = indexPrefix + "*";

      Date now = new Date();
      Calendar cal = Calendar.getInstance();
      cal.setTime(now);
      cal.set(Calendar.MILLISECOND, 0);
      cal.set(Calendar.SECOND, 0);
      cal.add(Calendar.HOUR, -1);
      String rangeFrom = sFormat.format(cal.getTime());
      List<Future<List<OffsetPoints>>> futureList = new ArrayList<Future<List<OffsetPoints>>>();

      ResponseEntity<String> response = REST.exchange(

          "http://" + getHost() + "/" + indexNameSearch + "/" + docType + "/_search?scroll=1m", HttpMethod.POST,
          new HttpEntity<String>(ScrollSearchTemplate.getScrollSearchBody(topic, group, rangeFrom, sFormat.format(now)),
              headers),
          String.class);

      JSONObject searchResult = null;
      while (true) {
        searchResult = new JSONObject(response.getBody());
        final JSONArray searchHits = searchResult.getJSONObject("hits").getJSONArray("hits");
        if (searchHits.length() == 0) {
          break;
        }
        try {
          Future<List<OffsetPoints>> future = null;
          future = pool.submit(new GenerateOffsetHistoryDataset(searchHits));
          futureList.add(future);
        } catch (Exception e) {
          LOG.warn("Ops...GenerateOffsetHistoryDataset went wrong! " + e.getMessage());
        }

        response = REST.exchange("http://" + getHost() + "/_search/scroll", HttpMethod.POST, new HttpEntity<String>(
            ScrollSearchTemplate.getScrollNextBody(searchResult.getString("_scroll_id")), headers), String.class);
      }
      for (Future<List<OffsetPoints>> future : futureList) {
        try {
          result.addAll(future.get());
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          LOG.error("Interrupted when get GenerateOffsetHistoryDataset in future...", e);
        } catch (ExecutionException e) {
          // TODO Auto-generated catch block
          LOG.error("QAQ when get GenerateOffsetHistoryDataset in future...", e);
        }
      }
      pool.shutdown();
    } catch (Exception e) {
      pool.shutdown();
      // TODO
      LOG.error("Damn...", e);
    }

    return result;
  }

  class GenerateOffsetHistoryDataset implements Callable<List<OffsetPoints>> {

    private JSONArray searchHits;
    List<String> concernedTimestamp = null;

    public GenerateOffsetHistoryDataset(JSONArray searchHits) {
      this.searchHits = searchHits;
    }

    public GenerateOffsetHistoryDataset(JSONArray searchHitPart, List<String> concernedTimestamp) {
      this.searchHits = searchHitPart;
      this.concernedTimestamp = concernedTimestamp;
    }

    @Override
    public List<OffsetPoints> call() {
      JSONObject source = null;
      List<OffsetPoints> datasets = new ArrayList<OffsetPoints>();
      try {
        for (int i = 0; i < searchHits.length(); i++) {
          source = searchHits.getJSONObject(i).getJSONObject("_source");
          if (concernedTimestamp != null) {
            if (!concernedTimestamp.contains(source.getString("date"))) {
              continue;
            }
          }
          Long offset = source.getLong("offset");
          Long logsize = source.getLong("logSize");

          datasets.add(new OffsetPoints(source.getLong("timestamp"), source.getInt("partition"),
              source.getString("owner"), offset, logsize));
        }
      } catch (Exception e) {
        LOG.error("GenerateOffsetHistoryDataset error! " + e.getMessage());
      }
      return datasets;
    }

  }

  @Override
  public void close() {

  }

  public static Map<String, List<OffsetStat>> offset(String group, String topic, String start, String end) {
    Map<String, List<OffsetStat>> statsMap = new HashMap<String, List<OffsetStat>>();
    String indexPrefix = SystemManager.getConfig().getEsIndex();
    try {
      ElasticsearchAssistEntity assistEntity = ScrollSearchTemplate.getInterval(start, end);

      List<String> indexes = new ArrayList<String>();
      assistEntity.getIndexs().forEach(a -> {
        indexes.add(indexPrefix + "-" + a);
      });

      String[] esHost = SystemManager.getConfig().getEsHosts().split("[,;]")[0].split(":");

      String url = "http://" + esHost[0] + ":" + esHost[1] + "/" + String.join(",", indexes) + "/"
          + SystemManager.getElasticSearchOffsetType() + "/_search?ignore_unavailable=true&allow_no_indices=true";
      String template = ScrollSearchTemplate.getOffset(group, topic, assistEntity, false);

      ResponseEntity<String> response = REST.exchange(url, HttpMethod.POST,
          new HttpEntity<String>(template, headers), String.class);
      String searchResult = response.getBody();
      JSONObject temp = new JSONObject(searchResult);
      JSONArray temp2 = temp.getJSONObject("aggregations").getJSONObject("aggs").getJSONArray("buckets");
      List<OffsetStat> stats = new ArrayList<OffsetStat>();
      temp2.forEach(obj -> {
        JSONObject item = (JSONObject) obj;
        JSONArray xx = item.getJSONObject("group").getJSONArray("buckets");
        for (int i = 0; i < xx.length(); i++) {
          JSONObject item2 = xx.getJSONObject(i);
          JSONArray xxx = item2.getJSONObject("topic").getJSONArray("buckets");
          for (int j = 0; j < xxx.length(); j++) {
            JSONObject item3 = xxx.getJSONObject(j);
            stats.add(new OffsetStat(item.getLong("key"), item2.get("key").toString(), item3.get("key").toString(),
                item3.getJSONObject("offset").getLong("value"), item3.getJSONObject("lag").getLong("value")));
          }
        }
      });

      stats.forEach(a -> {
        String topicName = a.getTopic();
        if (topicName == null || topicName.length() == 0) {
          topicName = "empty";
        }
        if (statsMap.containsKey(topicName)) {
          statsMap.get(topicName).add(a);
        } else {
          List<OffsetStat> arr = new ArrayList<OffsetStat>();
          arr.add(a);
          statsMap.put(topicName, arr);
        }
      });

      statsMap.forEach((key, val) -> {
        for (int i = val.size() - 1; i > 0; i--) {
          val.get(i).setOffset(val.get(i).getOffset() - val.get(i - 1).getOffset());
        }
        val.remove(0);
      });

    } catch (Exception e) {
      // TODO
      LOG.error("Damn...", e);
    }
    return statsMap;
  }

  public static HashMap<String, ArrayList<Long[]>> JmxTrend(String start, String end) {
    HashMap<String, ArrayList<Long[]>> mappedResult = new HashMap<>();
    String indexPrefix = SystemManager.getConfig().getEsIndex();
    try {
      ElasticsearchAssistEntity assistEntity = ScrollSearchTemplate.getInterval(start, end);

      List<String> indexes = new ArrayList<String>();
      assistEntity.getIndexs().forEach(a -> {
        indexes.add(indexPrefix + "-" + a);
      });

      String[] esHost = SystemManager.getConfig().getEsHosts().split("[,;]")[0].split(":");

      String url = "http://" + esHost[0] + ":" + esHost[1] + "/" + String.join(",", indexes) + "/"
          + SystemManager.getElasticSearchJmxType() + "/_search?ignore_unavailable=true&allow_no_indices=true";
      String template = ScrollSearchTemplate.JmxTrend(assistEntity, false);

      ResponseEntity<String> response = REST.exchange(url, HttpMethod.POST,
          new HttpEntity<String>(template, headers), String.class);
      String searchResult = response.getBody();
      JSONObject temp = new JSONObject(searchResult);
      JSONArray temp2 = temp.getJSONObject("aggregations").getJSONObject("aggs").getJSONArray("buckets");

      temp2.forEach(obj -> {
        JSONObject item = (JSONObject) obj;
        JSONArray xx = item.getJSONObject("metrics").getJSONArray("buckets");
        for (int i = 0; i < xx.length(); i++) {
          JSONObject item2 = xx.getJSONObject(i);
          JSONArray xxx = item2.getJSONObject("brokers").getJSONArray("buckets");
          for (int j = 0; j < xxx.length(); j++) {
            JSONObject item3 = xxx.getJSONObject(j);
            String key = item2.get("key").toString().replaceAll("persec", "") + "|" + item3.get("key");
            ArrayList<Long[]> array = mappedResult.get(key);
            if (array == null) {
              array = new ArrayList<Long[]>();
            }
            array.add(new Long[] { Long.parseLong(item.get("key").toString()),
                (long) Double.parseDouble(item3.getJSONObject("offset").get("value").toString()) });
            mappedResult.put(key, array);
          }
        }
      });
      List<String> list = new ArrayList<String>(mappedResult.keySet());
      java.util.Collections.sort(list);
      HashMap<String, ArrayList<Long[]>> mappedResult2 = new HashMap<>();
      for (String key : list) {
        ArrayList<Long[]> item = mappedResult.get(key);
        ArrayList<Long[]> item2 = new ArrayList<>();
        long val = 0;
        for (Integer i = 0; i < item.size(); i++) {
          if (i > 0) {
            item2.add(new Long[] { item.get(i)[0], item.get(i)[1] - val });
          }
          val = item.get(i)[1];
        }
        mappedResult2.put(key, item2);
      }
      return mappedResult2;

    } catch (Exception e) {
      // TODO
      LOG.error("Damn...", e);
    }
    return mappedResult;
  }

}
