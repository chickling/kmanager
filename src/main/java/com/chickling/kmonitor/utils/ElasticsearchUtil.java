package com.chickling.kmonitor.utils;

import java.net.InetSocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chickling.kmonitor.model.OffsetHistoryQueryParams;
import com.chickling.kmonitor.model.OffsetPoints;

/**
 * @author Hulva Luva.H
 *
 */
public class ElasticsearchUtil {
	private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchUtil.class);
	static TransportClient client = null;

	public ElasticsearchUtil(String stringHosts) {
		initClient(stringHosts);
	}

	public boolean check() {
		return client.connectedNodes().isEmpty();
	}

	private void initClient(String stringHosts) {
		Settings settings = Settings.settingsBuilder().put("client.transport.ignore_cluster_name", true).build();
		client = TransportClient.builder().settings(settings).build();
		String[] hosts = stringHosts.split(",");
		for (String host : hosts) {
			String[] ha = host.split(":");
			if (ha.length < 2) {
				throw new RuntimeException("Elasticsearch host should be like-> 127.0.0.1:9300");
			}
			client.addTransportAddress(
					new InetSocketTransportAddress(new InetSocketAddress(ha[0], Integer.parseInt(ha[1]))));
		}
	}

	public void bulkIndex(JSONObject data, String docType, String indexPrefix) {
		BulkProcessor bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {
			@Override
			public void beforeBulk(long executionId, BulkRequest request) {
			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
			}
		}).setBulkActions(10000).setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
				.setFlushInterval(TimeValue.timeValueSeconds(5)).setConcurrentRequests(1)
				.setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3)).build();

		SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date now = new Date();

		Iterator<String> keys = data.keys();
		while (keys.hasNext()) {
			bulkProcessor.add(new IndexRequest(indexPrefix + sFormat.format(now), docType)
					.source(data.getJSONObject(keys.next()).toString()));
		}
		try {
			bulkProcessor.awaitClose(10, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public List<OffsetPoints> scrollsSearcher(OffsetHistoryQueryParams params, String docType, String indexPrefix) {
		int parallism = Runtime.getRuntime().availableProcessors();
		ExecutorService pool = Executors.newFixedThreadPool(parallism);

		List<OffsetPoints> result = new ArrayList<OffsetPoints>();

		SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		try {
			String indexNameSearch = indexPrefix + "*";
			String rangeFrom = getRangeFrom(params);

			// int pageSize = calculatePageSize(params, rangeFrom);
			String queryString = "topic:" + params.getTopic() + " AND group:" + params.getGroup() + " AND date:["
					+ rangeFrom + " TO " + sFormat.format(new Date(Long.parseLong(params.getRangeto()))) + "]";

			SearchResponse response = client.prepareSearch(indexNameSearch).setTypes(docType)
					.addSort("date", SortOrder.ASC).setScroll(new TimeValue(60000))
					.setQuery(QueryBuilders.queryStringQuery(queryString)).setSize(1000).execute().actionGet();

			List<String> concernedTimestamp = dateHistogram(sFormat.parse(rangeFrom).getTime(),
					Long.parseLong(params.getRangeto()), params.getInterval());

			List<Future<List<OffsetPoints>>> futureList = new ArrayList<Future<List<OffsetPoints>>>();
			while (true) {
				final SearchHit[] searchHits = response.getHits().getHits();
				if (searchHits.length == 0) {
					break;
				}
				try {
					int step = 1;
					if (searchHits.length < parallism) {
						step = 1;
					} else {
						step = searchHits.length / parallism;
					}
					Future<List<OffsetPoints>> future = null;
					for (int i = 0; i < searchHits.length; i = i + step) {
						int to = i + step < searchHits.length ? i + step : searchHits.length;
						SearchHit[] searchHitPart = Arrays.copyOfRange(searchHits, i, to);
						future = pool.submit(new GenerateOffsetHistoryDataset(searchHitPart, concernedTimestamp));
						futureList.add(future);
					}

				} catch (Exception e) {
					LOG.warn("Ops...GenerateOffsetHistoryDataset went wrong! " + e.getMessage());
				}
				response = client.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(60000)).execute()
						.actionGet();
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
		int parallism = Runtime.getRuntime().availableProcessors();
		ExecutorService pool = Executors.newFixedThreadPool(parallism);

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

			// int pageSize = calculatePageSize(params, rangeFrom);
			String queryString = "topic:" + topic + " AND group:" + group + " AND date:[" + rangeFrom + " TO "
					+ sFormat.format(now) + "]";

			SearchResponse response = client.prepareSearch(indexNameSearch).setTypes(docType)
					.addSort("date", SortOrder.ASC).setScroll(new TimeValue(60000))
					.setQuery(QueryBuilders.queryStringQuery(queryString)).setSize(1000).execute().actionGet();

			List<Future<List<OffsetPoints>>> futureList = new ArrayList<Future<List<OffsetPoints>>>();
			while (true) {
				SearchHit[] searchHits = response.getHits().getHits();
				if (searchHits.length == 0) {
					break;
				}
				try {
					int step = 1;
					if (searchHits.length < parallism) {
						step = 1;
					} else {
						step = searchHits.length / parallism;
					}
					Future<List<OffsetPoints>> future = null;
					for (int i = 0; i < searchHits.length; i = i + step) {
						int to = i + step < searchHits.length ? i + step : searchHits.length;
						SearchHit[] searchHitPart = Arrays.copyOfRange(searchHits, i, to);
						future = pool.submit(new GenerateOffsetHistoryDataset(searchHitPart));
						futureList.add(future);
					}
				} catch (Exception e) {
					LOG.warn("Ops...GenerateOffsetHistoryDataset went wrong! " + e.getMessage());
				}

				response = client.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(60000)).execute()
						.actionGet();
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

	public void close() {
		client.close();
	}

	class GenerateOffsetHistoryDataset implements Callable<List<OffsetPoints>> {

		private SearchHit[] searchHits;
		List<String> concernedTimestamp = null;

		public GenerateOffsetHistoryDataset(SearchHit[] searchHits) {
			this.searchHits = searchHits;
		}

		public GenerateOffsetHistoryDataset(SearchHit[] searchHitPart, List<String> concernedTimestamp) {
			this.searchHits = searchHitPart;
			this.concernedTimestamp = concernedTimestamp;
		}

		@Override
		public List<OffsetPoints> call() {
			Map<String, Object> source = null;
			List<OffsetPoints> datasets = new ArrayList<OffsetPoints>();
			try {
				for (SearchHit hit : searchHits) {
					source = hit.getSource();
					if (concernedTimestamp != null) {
						if (!concernedTimestamp.contains(source.get("date"))) {
							continue;
						}
					}
					Long offset = null;
					if (source.get("offset") instanceof Integer) {
						Integer tempOffset = (Integer) source.get("offset");
						offset = new Long(tempOffset);
					} else {
						offset = (Long) source.get("offset");
					}
					Long logsize = null;
					if (source.get("offset") instanceof Integer) {
						Integer tempLogsize = (Integer) source.get("logSize");
						logsize = new Long(tempLogsize);
					} else {
						logsize = (Long) source.get("logSize");
					}

					datasets.add(new OffsetPoints((Long) source.get("timestamp"), (Integer) source.get("partition"),
							(String) source.get("owner"), offset, logsize));
				}
			} catch (Exception e) {
				LOG.error("GenerateOffsetHistoryDataset error! " + e.getMessage());
			}
			return datasets;
		}

	}
}
