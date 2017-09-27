package com.chickling.kmanager.utils.elasticsearch.restapi;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.chickling.kmanager.model.ElasticsearchAssistEntity;

/**
 * @author Hulva Luva.H
 * @since 2017-7-19
 *
 */
public class ScrollSearchTemplate {

  public static String getScrollSearchBody(String topic, String group, String from, String to) {
    return "{\"size\":1000,"

        + "\"sort\":[{\"timestamp\":{\"order\":\"asc\"}},{\"partition\":{\"order\":\"asc\"}}],"

        + "\"query\":{\"bool\":{"

        + "\"must\":[{\"match\":{\"topic\":\"" + topic + "\"}},{\"match\":{\"group\":\"" + group + "\"}}],"

        + "\"filter\":[{\"range\":{\"date\":{\"gte\":\"" + from + "\",\"lte\":\"" + to
        + "\",\"format\":\"yyyy-MM-dd'T'HH:mm:ss.SSSZ\"}}}]}}}";
  }

  public static String getScrollNextBody(String scrollId) {
    return "{\"scroll\":\"1m\",\"scroll_id\":\"" + scrollId + "\"}";
  }

  public static String getInterval(int diff) {
    if (diff / 5 < 100) {
      return "5m";
    }
    if (diff / 10 < 100) {
      return "10m";
    }
    if (diff / 30 < 100) {
      return "30m";
    }
    if (diff / 60 < 100) {
      return "1h";
    }
    if (diff / (4 * 60) < 100) {
      return "4h";
    }
    if (diff / (8 * 60) < 100) {
      return "8h";
    }
    if (diff / (16 * 60) < 100) {
      return "16h";
    }
    return "1d";
  }

  public static ElasticsearchAssistEntity getInterval(String start, String end) {
    String format = "MM/dd/yyyy HH:mm";

    SimpleDateFormat sdf = new SimpleDateFormat(format);

    Date startDate;
    try {

      startDate = sdf.parse(start + " 00:00:00");
    } catch (Exception ex) {
      // yesterday
      startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
    }
    Date endDate;
    try {
      endDate = sdf.parse(end + " 23:59:59");
      Date now = new Date();
      if (endDate.getTime() > now.getTime()) {
        endDate = now;
      }
    } catch (Exception ex) {
      endDate = new Date();
    }
    long diff = endDate.getTime() - startDate.getTime();
    int diffmin = (int) (diff / (60 * 1000));

    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
    LocalDate startLocalDate = LocalDate.parse(sdf2.format(startDate));
    LocalDate endLocalDate = LocalDate.parse(sdf2.format(endDate));

    List<String> days = new ArrayList<String>();
    while (!startLocalDate.isAfter(endLocalDate)) {
      days.add(startLocalDate.toString());
      startLocalDate = startLocalDate.plusDays(1);
    }

    return new ElasticsearchAssistEntity(getInterval(diffmin), days, startDate, endDate);
  }

  public static String getOffset(String group, String topic, ElasticsearchAssistEntity assistEntity, boolean bIsOldAPI) {
    String query = "";
    
    if(!bIsOldAPI) {
      query = "{" + 
          "    \"size\": 0," + 
          "    \"aggs\": {" + 
          "        \"aggs\": {" + 
          "            \"date_histogram\": {" + 
          "                \"field\": \"timestamp\"," + 
          "                \"interval\": \"" + assistEntity.getInterval() + "\"" + 
          "            }," + 
          "            \"aggs\": {" + 
          "                \"group\": {" + 
          "                    \"terms\": {" + 
          "                        \"field\": \"group\"," + 
          "                        \"order\": {" + 
          "                            \"_term\": \"desc\"" + 
          "                        }" + 
          "                    }," + 
          "                    \"aggs\": {" + 
          "                        \"topic\": {" + 
          "                            \"terms\": {" + 
          "                                \"field\": \"topic\"," + 
          "                                \"order\": {" + 
          "                                    \"_term\": \"desc\"" + 
          "                                }" + 
          "                            }," + 
          "                            \"aggs\": {" + 
          "                                \"data\": {" + 
          "                                    \"terms\": {" + 
          "                                        \"field\": \"partition\"," + 
          "                                        \"order\": {" + 
          "                                            \"_term\": \"desc\"" + 
          "                                        }" + 
          "                                    }," + 
          "                                    \"aggs\": {" + 
          "                                        \"offset\": {" + 
          "                                            \"max\": {" + 
          "                                                \"field\": \"offset\"" + 
          "                                            }" + 
          "                                        }," + 
          "                                        \"lag\": {" + 
          "                                            \"max\": {" + 
          "                                                \"field\": \"lag\"" + 
          "                                            }" + 
          "                                        }" + 
          "                                    }" + 
          "                                }," + 
          "                                \"offset\": {" + 
          "                                    \"sum_bucket\": {" + 
          "                                        \"buckets_path\": \"data>offset\"" + 
          "                                    }" + 
          "                                }," + 
          "                                \"lag\": {" + 
          "                                    \"sum_bucket\": {" + 
          "                                        \"buckets_path\": \"data>lag\"" + 
          "                                    }" + 
          "                                }" + 
          "                            }" + 
          "                        }" + 
          "                    }" + 
          "                }" + 
          "            }" + 
          "        }" + 
          "    }," + 
          "    \"query\": {" + 
          "        \"bool\": {" + 
          "            \"must\": [";
      
      
      if (group != null && group.length() > 0) {
        query += "                {" + 
            "                    \"match\": {" + 
            "                        \"group\": {" + 
            "                            \"query\": \"" + group + "\"," + 
            "                            \"type\": \"phrase\"" + 
            "                        }" + 
            "                    }" + 
            "                },";
      }
      if (topic != null && topic.length() > 0) {
        query += "                {" + 
            "                    \"match\": {" + 
            "                        \"topic\": {" + 
            "                            \"query\": \"" + topic + "\"," + 
            "                            \"type\": \"phrase\"" + 
            "                        }" + 
            "                    }" + 
            "                }";
      }
      
      query +=
          
          "            ]," + 
          "            \"filter\": [" + 
          "                {" + 
          "                    \"range\": {" + 
          "                        \"timestamp\": {" + 
          "                            \"gte\": " + assistEntity.getStart().getTime() + "," + 
          "                            \"lte\": " + assistEntity.getEnd().getTime() + "," + 
          "                            \"format\": \"epoch_millis\"" + 
          "                        }" + 
          "                    }" + 
          "                }," + 
          "                {" + 
          "                    \"query_string\": {" + 
          "                        \"analyze_wildcard\": true," + 
          "                        \"query\": \"*\"" + 
          "                    }" + 
          "                }" + 
          "            ]" + 
          "        }" + 
          "    }" + 
          "}";
    }else {

      query = "{\"size\": 0,\"aggs\": {\"aggs\": {\"date_histogram\": {\"field\": \"timestamp\"," + "\"interval\": \""
          + assistEntity.getInterval() + "\""
          + ",\"time_zone\": \"America/Los_Angeles\"},\"aggs\": {\"group\": {\"terms\": {\"field\": \"group\",\"order\": {\"_term\": \"desc\"}}"
          + ",\"aggs\": {\"topic\": {\"terms\": {\"field\": \"topic\",\"order\": {\"_term\": \"desc\"}}"
          + ",\"aggs\": {\"data\": {\"terms\": {\"field\": \"partition\",\"order\": {\"_term\": \"desc\"}}"
          + ",\"aggs\": {\"offset\": {\"max\": {\"field\": \"offset\"}}, \"lag\": {\"max\": {\"field\": \"lag\"}}}}"
          + ",\"offset\":{\"sum_bucket\":{\"buckets_path\": \"data>offset\"}}"
          + ",\"lag\":{\"sum_bucket\":{\"buckets_path\": \"data>lag\"}}}}}}}}}"
          + ",\"query\": {\"filtered\": {\"query\": {\"query_string\": {\"analyze_wildcard\": true,\"query\": \"*\"}}"
          + ",\"filter\": {\"bool\": {\"must\": [";
      if (group != null && group.length() > 0) {
        query += "{\"query\": {\"match\": {\"group\": {\"query\": \"" + group + "\",\"type\": \"phrase\"}}}},";
      }
      if (topic != null && topic.length() > 0) {
        query += "{\"query\": {\"match\": {\"topic\": {\"query\": \"" + topic + "\",\"type\": \"phrase\"}}}},";
      }
      query += "{\"range\": {\"timestamp\": {" + "\"gte\":" + assistEntity.getStart().getTime() + ",\"lte\": "
          + assistEntity.getEnd().getTime() + ",\"format\": \"epoch_millis\"}}}" + "]}}}}}";
    }
    return query;
  }

  public static String JmxTrend(ElasticsearchAssistEntity assistEntity, boolean bIsOldAPI) {

    return bIsOldAPI
        ? "{\"size\": 0," + "  \"aggs\": {" + "    \"aggs\": {" + "      \"date_histogram\": {" + "        \"field\": \"timestamp\","
            + "        \"interval\": \"" + assistEntity.getInterval() + "\"," + "        \"time_zone\": \"America/Los_Angeles\""
            + "      }," + "      \"aggs\": {" + "        \"metrics\": {" + "          \"terms\": {" + "            \"field\": \"metric\","
            + "            \"size\": 0," + "            \"order\": {" + "              \"_term\": \"desc\"" + "            }"
            + "          }," + "          \"aggs\": {" + "            \"brokers\": {" + "              \"terms\": {"
            + "                \"field\": \"broker\"," + "                \"size\": 0," + "                \"order\": {"
            + "                  \"_term\": \"desc\"" + "                }" + "              }," + "              \"aggs\": {"
            + "                \"offset\": {" + "                  \"max\": {" + "                    \"field\": \"count\""
            + "                  }" + "                }" + "              }" + "            }" + "          }" + "        }" + "      }"
            + "    }" + "  }," + "  \"query\": {" + "    \"filtered\": {" + "      \"filter\": {" + "        \"bool\": {"
            + "          \"must\": [{" + "            \"range\": {" + "              \"timestamp\": {" + "                \"gte\":"
            + assistEntity.getStart().getTime() + "," + "                \"lte\": " + assistEntity.getEnd().getTime() + ","
            + "                \"format\": \"epoch_millis\"" + "              }" + "            }" + "          }],"
            + "          \"must_not\": []" + "        }" + "      }" + "    }" + "  }" + "}"
        : "{" + 
            "    \"size\": 0," + 
            "    \"aggs\": {" + 
            "        \"aggs\": {" + 
            "            \"date_histogram\": {" + 
            "                \"field\": \"timestamp\"," + 
            "                \"interval\": \"" + assistEntity.getInterval() + "\"" + 
            "            }," + 
            "            \"aggs\": {" + 
            "                \"metrics\": {" + 
            "                    \"terms\": {" + 
            "                        \"field\": \"metric\"," + 
            "                        \"order\": {" + 
            "                            \"_term\": \"desc\"" + 
            "                        }" + 
            "                    }," + 
            "                    \"aggs\": {" + 
            "                        \"brokers\": {" + 
            "                            \"terms\": {" + 
            "                                \"field\": \"broker\"," + 
            "                                \"order\": {" + 
            "                                    \"_term\": \"desc\"" + 
            "                                }" + 
            "                            }," + 
            "                            \"aggs\": {" + 
            "                                \"offset\": {" + 
            "                                    \"max\": {" + 
            "                                        \"field\": \"count\"" + 
            "                                    }" + 
            "                                }" + 
            "                            }" + 
            "                        }" + 
            "                    }" + 
            "                }" + 
            "            }" + 
            "        }" + 
            "    }," + 
            "    \"query\": {" + 
            "        \"bool\": {" + 
            "            \"must\": [" + 
            "                {" + 
            "                    \"range\": {" + 
            "                        \"timestamp\": {" + 
            "                            \"gte\": "+ assistEntity.getStart().getTime() +"," + 
            "                            \"lte\": " + assistEntity.getEnd().getTime() + "," + 
            "                            \"format\": \"epoch_millis\"" + 
            "                        }" + 
            "                    }" + 
            "                }" + 
            "            ]," + 
            "            \"must_not\": []" + 
            "        }" + 
            "    }" + 
            "}";

  }
}
