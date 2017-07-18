package com.chickling.kmonitor.core.db;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import com.chickling.kmonitor.config.AppConfig;
import com.chickling.kmonitor.model.OffsetHistory;
import com.chickling.kmonitor.model.OffsetHistoryQueryParams;
import com.chickling.kmonitor.model.OffsetInfo;
import com.chickling.kmonitor.model.OffsetPoints;
import com.chickling.kmonitor.utils.CommonUtils;
import com.chickling.kmonitor.utils.ElasticsearchUtil;

/**
 * @author Hulva Luva.H
 *
 */
public class ElasticsearchOffsetDB implements OffsetDB {

	private ElasticsearchUtil esUtil;
	private String indexPrefix;
	private String docType;

	private static final SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	public ElasticsearchOffsetDB(AppConfig config) {
		esUtil = new ElasticsearchUtil(config.getEsHosts());
		setIndexAndType(config.getEsIndex(), config.getDocTypeForOffset());
	}

	public void setIndexAndType(String index, String docType) {
		this.indexPrefix = index + "-";
		this.docType = docType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.chickling.kmonitor.core.db.OffsetDb#insert(long,
	 * com.chickling.kmonitor.model.OffsetInfo)
	 */
	@Override
	public void insert(long timestamp, OffsetInfo offsetInfo) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.chickling.kmonitor.core.db.OffsetDb#batchInsert(java. util.List)
	 */
	@Override
	public void batchInsert(List<OffsetInfo> offsetInfoList) {
		long now = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(now);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);

		JSONObject data = new JSONObject();
		for (int i = 0; i < offsetInfoList.size(); i++) {
			data.put(i + "", generateRecord(cal.getTimeInMillis(), offsetInfoList.get(i)));
		}
		esUtil.bulkIndex(data, docType, indexPrefix);
	}

	private JSONObject generateRecord(long timestamp, OffsetInfo offsetInfo) {
		JSONObject data = new JSONObject();
		data.put("group", offsetInfo.getGroup());
		data.put("topic", offsetInfo.getTopic());
		data.put("partition", offsetInfo.getPartition());
		data.put("offset", offsetInfo.getOffset());
		data.put("logSize", offsetInfo.getLogSize());
		data.put("owner", offsetInfo.getOwner()/* owner.getOrElse("NA") */);
		data.put("date", sFormat.format(new Date(timestamp)));
		data.put("timestamp", timestamp);
		data.put("creation", offsetInfo.getCreation());
		data.put("modified", offsetInfo.getModified());
		data.put("lag", offsetInfo.getLag());
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.chickling.kmonitor.core.db.OffsetDb#offsetHistory()
	 */
	@Override
	public OffsetHistory offsetHistory(String group, String topic) {
		List<OffsetPoints> offsetPointsList = esUtil.offsetHistory(indexPrefix, docType, group, topic);
		CommonUtils.sortByTimestampThenPartition(offsetPointsList);
		return new OffsetHistory(group, topic, offsetPointsList);
	}

	@Override
	public OffsetHistory offsetHistory(OffsetHistoryQueryParams params) {
		List<OffsetPoints> offsetPointsList = esUtil.scrollsSearcher(params, docType, indexPrefix);
		CommonUtils.sortByTimestampThenPartition(offsetPointsList);
		return new OffsetHistory(params.getGroup(), params.getTopic(), offsetPointsList);
	}

	@Override
	public boolean check() {
		return esUtil.check();
	}

	@Override
	public void close() {
		esUtil.close();
	}

}
