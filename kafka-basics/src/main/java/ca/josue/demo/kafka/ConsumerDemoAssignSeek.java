package ca.josue.demo.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

/**
 * @author Josue Lubaki
 * @version 1.0
 * @since 2022-05-21
 * In case you are looking to read specific messages from specific partitions,
 * the .seek() and .assign() API may help you.
 */
public class ConsumerDemoAssignSeek {
    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(ConsumerDemoAssignSeek.class.getName());

        String bootstrapServers = "127.0.0.1:9092";
        String topic = "demo_java";

        // create consumer configs
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // create consumer
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties)) {
            // assign
            TopicPartition partitionToReadFrom = new TopicPartition(topic, 0);
            consumer.assign(List.of(partitionToReadFrom));

            // seek
            long offsetToReadFrom = 7L;
            consumer.seek(partitionToReadFrom, offsetToReadFrom);

            int numberOfMessagesToRead = 20;
            boolean keepOnReading = true;
            int numberOfMessagesReadSoFar = 0;

            while(keepOnReading){
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                for(ConsumerRecord<String, String> record : records){
                    numberOfMessagesReadSoFar += 1;
                    log.info("Key: " + record.key() + ", Value: " + record.value());
                    log.info("Partition: " + record.partition() + ", Offset:" + record.offset());

                    if (numberOfMessagesReadSoFar >= numberOfMessagesToRead){
                        keepOnReading = false; // to exit the while loop
                        break; // to exit the for loop
                    }
                }
            }
        }

        // assign and seek are mostly used to replay data or fetch a specific message

        log.info("Exiting the application");
    }
}
