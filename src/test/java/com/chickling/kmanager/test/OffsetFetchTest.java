/**
 * 
 */
package com.chickling.kmanager.test;

import java.io.IOException;

import org.apache.kafka.common.requests.MetadataRequest;

import kafka.cluster.Broker;
import kafka.common.ErrorMapping;
import kafka.common.TopicAndPartition;
import kafka.network.BlockingChannel;

/**
 * @author Hulva Luva.H
 * @since 2018年4月21日
 *
 */
public class OffsetFetchTest {

  public static void main(String[] args) {
//    ClientUtils.
    try {
      BlockingChannel channel = new BlockingChannel("localhost", 9092, BlockingChannel.UseDefaultBufferSize(),
          BlockingChannel.UseDefaultBufferSize(), 5000 /* read timeout in millis */);
      channel.connect();
      final String MY_GROUP = "demoGroup";
      final String MY_CLIENTID = "demoClientId";
      int correlationId = 0;
      final TopicAndPartition testPartition0 = new TopicAndPartition("demoTopic", 0);
      final TopicAndPartition testPartition1 = new TopicAndPartition("demoTopic", 1);
      channel.send(MetadataRequest.allTopics(MetadataRequest.Builder.allTopics().));
      ConsumerMetadataResponse metadataResponse = ConsumerMetadataResponse.readFrom(channel.receive().buffer());

      if (metadataResponse.errorCode() == ErrorMapping.NoError()) {
        Broker offsetManager = metadataResponse.coordinator();
        // if the coordinator is different, from the above channel's host then reconnect
        channel.disconnect();
        channel = new BlockingChannel(offsetManager.host(), offsetManager.port(), BlockingChannel.UseDefaultBufferSize(),
            BlockingChannel.UseDefaultBufferSize(), 5000 /* read timeout in millis */);
        channel.connect();
      } else {
        // retry (after backoff)
      }
    } catch (IOException e) {
      // retry the query (after backoff)
    }
  }

}
