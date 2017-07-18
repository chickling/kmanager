package com.chickling.kmonitor.test;

import java.util.List;

import com.chickling.kmonitor.model.OffsetPoints;
import com.chickling.kmonitor.utils.ElasticsearchUtil;

/**
 * 
 * @author Hulva Luva.H
 *
 */
public class EsSearchTest {

	public static void main(String[] args) {
		ElasticsearchUtil es = new ElasticsearchUtil("10.16.238.82:9300,10.16.238.83:9300,10.16.238.84:9300");
		List<OffsetPoints> result = es.offsetHistory("logx_healthcheck_test", "kafkaoffset", "testkafka", "EC2_Test");

		System.out.println(result);
	}

}
