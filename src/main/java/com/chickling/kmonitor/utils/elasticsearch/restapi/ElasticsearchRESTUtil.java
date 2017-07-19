package com.chickling.kmonitor.utils.elasticsearch.restapi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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

import com.chickling.kmonitor.alert.WorkerThreadFactory;
import com.chickling.kmonitor.initialize.SystemManager;
import com.chickling.kmonitor.model.OffsetHistoryQueryParams;
import com.chickling.kmonitor.model.OffsetPoints;
import com.chickling.kmonitor.utils.elasticsearch.Ielasticsearch;

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
	 * @return
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
			bulkData.append(
					"{\"index\": {\"_index\":\"" + indexPrefix + indexSufix + "\",\"_type\":\"" + docType + "\"}}")
					.append("\n");
			bulkData.append(data.getJSONObject(keys.next()).toString()).append("\n");
		}
		if (!hasData) {
			return;
		}
		ResponseEntity<String> response = REST.exchange("http://" + RERST_HOST + "/_bulk", HttpMethod.POST,
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

			ResponseEntity<String> response = REST
					.exchange("http://" + RERST_HOST + "/" + indexNameSearch + "/" + docType + "/_search?scroll=1m",
							HttpMethod.POST,
							new HttpEntity<String>(
									ScrollSearchTemplate.getScrollSearchBody(params.getTopic(), params.getGroup(),
											rangeFrom, sFormat.format(new Date(Long.parseLong(params.getRangeto())))),
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
					future = pool.submit(new GenerateOffsetHistoryDataset(searchHits, concernedTimestamp));
					futureList.add(future);
				} catch (Exception e) {
					LOG.warn("Ops...GenerateOffsetHistoryDataset went wrong! " + e.getMessage());
				}

				response = REST.exchange("http://" + RERST_HOST + "/_search/scroll", HttpMethod.POST,
						new HttpEntity<String>(
								ScrollSearchTemplate.getScrollNextBody(searchResult.getString("_scroll_id")), headers),
						String.class);
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

					"http://" + RERST_HOST + "/" + indexNameSearch + "/" + docType + "/_search?scroll=1m",
					HttpMethod.POST,
					new HttpEntity<String>(
							ScrollSearchTemplate.getScrollSearchBody(topic, group, rangeFrom, sFormat.format(now)),
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

				response = REST.exchange("http://" + RERST_HOST + "/_search/scroll", HttpMethod.POST,
						new HttpEntity<String>(
								ScrollSearchTemplate.getScrollNextBody(searchResult.getString("_scroll_id")), headers),
						String.class);
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
}
