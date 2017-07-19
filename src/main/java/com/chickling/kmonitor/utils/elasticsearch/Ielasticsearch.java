package com.chickling.kmonitor.utils.elasticsearch;

import java.util.List;

import org.json.JSONObject;

import com.chickling.kmonitor.model.OffsetHistoryQueryParams;
import com.chickling.kmonitor.model.OffsetPoints;

/**
 * @author Hulva Luva.H from ECBD
 * @date 2017年7月19日
 * @description 
 *
 */
public interface Ielasticsearch {

	void bulkIndex(JSONObject data, String docType, String indexPrefix);

	List<OffsetPoints> offsetHistory(String indexPrefix, String docType, String group, String topic);

	List<OffsetPoints> scrollsSearcher(OffsetHistoryQueryParams params, String docType, String indexPrefix);

	boolean check();

	void close();

}
