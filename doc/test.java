import java.util.Date;
import java.util.Random;

public class test {
    public static void main(String[] args) {
//System.out.println(getOffset("botdetection_tomysql3", "EC_bot_detection"));

System.out.println(Long.parseLong("1502830800000"));
String clusterName = "my cluster - e11";
String output = clusterName.replaceAll("\\W", "");
System.out.println(output);
System.out.println(new Random().nextInt(10));
System.out.println(new Random().nextInt(10));
System.out.println(new Random().nextInt(10));
System.out.println(new Random().nextInt(10));
System.out.println(new Random().nextInt(10));
System.out.println(new Random().nextInt(10));
System.out.println(new Random().nextInt(10));
System.out.println(new Random().nextInt(10));
System.out.println(new Random().nextInt(10));
    }

      public static String getOffset(String group, String topic) {
    String query = "{\"size\": 0,\"aggs\": {\"aggs\": {\"date_histogram\": {\"field\": \"timestamp\","
        + "\"interval\": \"30m\""
        + ",\"time_zone\": \"America/Los_Angeles\"},\"aggs\": {\"group\": {\"terms\": {\"field\": \"group\",\"order\": {\"_term\": \"desc\"}}"
        + ",\"aggs\": {\"topic\": {\"terms\": {\"field\": \"topic\",\"order\": {\"_term\": \"desc\"}}"
        + ",\"aggs\": {\"data\": {\"terms\": {\"field\": \"partition\",\"order\": {\"_term\": \"desc\"}}"
        + ",\"aggs\": {\"offset\": {\"max\": {\"field\": \"offset\"}}, \"lag\": {\"max\": {\"field\": \"lag\"}}}}"
        + ",\"offset\":{\"sum_bucket\":{\"buckets_path\": \"data>offset\"}}"
        + ",\"lag\":{\"sum_bucket\":{\"buckets_path\": \"data>lag\"}}}}}}}}}"
        + ",\"query\": {\"filtered\": {\"query\": {\"query_string\": {\"analyze_wildcard\": true,\"query\": \"*\"}}"
        + ",\"filter\": {\"bool\": {\"must\": ["
        + "{\"query\": {\"match\": {\"_type\": {\"query\": \"kafkaoffset-e3\",\"type\": \"phrase\"}}}}, ";
    if (group != null && group.length() > 0) {
      query += "{\"query\": {\"match\": {\"group\": {\"query\": \"" + group + "\",\"type\": \"phrase\"}}}},";
    }
    if (topic != null && topic.length() > 0) {
      query += "{\"query\": {\"match\": {\"topic\": {\"query\": \"" + topic + "\",\"type\": \"phrase\"}}}},";
    }
    query += "{\"range\": {\"timestamp\": {" + "\"gte\":" + (System.currentTimeMillis() - 24 * 60 * 60000) + ",\"lte\": "
        + System.currentTimeMillis() + ",\"format\": \"epoch_millis\"}}}" + "]}}}}}";
    return query;
  }
}
