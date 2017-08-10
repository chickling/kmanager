package com.chickling.kmonitor.test;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import com.chickling.kmonitor.utils.elasticsearch.restapi.ElasticsearchRESTUtil;

/**
 * 
 * @author Hulva Luva.H
 *
 */
public class EsSearchTest {

  public static void main(String[] args) throws InterruptedException {
    // ElasticsearchJavaUtil es = new
    // ElasticsearchJavaUtil("10.16.238.82:9300,10.16.238.83:9300,10.16.238.84:9300");
    // List<OffsetPoints> result = es.offsetHistory("logx_healthcheck_test", "kafkaoffset", "testkafka",
    // "EC2_Test");
    //
    // System.out.println(result);
    ElasticsearchRESTUtil esUtil = new ElasticsearchRESTUtil("10.16.238.92:9200");
    JSONObject root = new JSONObject();
    JSONObject doc = null;
    SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    while (true) {
      for (int i = 0; i < 10; i++) {
        doc = new JSONObject();
        doc.put("hell", Math.random() * 1000);
        doc.put("world", Math.random() * 1000);
        doc.put("count", Math.random() * 1000);
        doc.put("date", sFormat.format(new Date()));
        root.put(i + "", doc);
      }
      esUtil.bulkIndex(root, "luva-test", "test");
      Thread.sleep(5000);
    }
  }

}
