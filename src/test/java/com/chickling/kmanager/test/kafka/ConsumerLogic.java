/**
 * 
 */
package com.chickling.kmanager.test.kafka;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;

/**
 * @author lvswe
 *
 */
public class ConsumerLogic implements Runnable {
    private KafkaStream<byte[], byte[]> stream;
    private int threadNumber;

    public ConsumerLogic(KafkaStream<byte[], byte[]> stream, int threadNumber) {
        this.threadNumber = threadNumber;
        this.stream = stream;
    }

    public void run() {
        ConsumerIterator<byte[], byte[]> it = stream.iterator();

        while (true) {
            MessageAndMetadata<byte[], byte[]> record = it.next();

            String topic = record.topic();
            int partition = record.partition();
            long offset = record.offset();
            Object key = record.key();
            System.out.println("Thread " + threadNumber + " received: " + "Topic " + topic
                    + " Partition " + partition + " Offset " + offset + " Key " + key + " Message "
                    + new String(record.message()));

            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}