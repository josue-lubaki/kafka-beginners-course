package ca.josue.demo.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

/**
 * @author Josue Lubaki
 * @version 1.0
 * @since 2022-05-20
 */
public class ConsumerDemo {

    private static final Logger log = LoggerFactory.getLogger(ConsumerDemo.class);
    private static final String BOOTSTRAP_SERVERS = "127.0.0.1:9092";
    private static final String GROUP_ID = "my-first-application";
    private static final String TOPIC = "demo_java";

    public static void main(String[] args) {
        log.info("I am kafka Consumer");

        // create properties
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        // create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // Subscribe consumer to our topic
        consumer.subscribe(List.of(TOPIC));

        // poll for messages
        while (true) {
            log.info("POLLING...");
            ConsumerRecords<String, String> records =
                    consumer.poll(Duration.ofMillis(1000));

            records.forEach((record) -> {
                log.info("Key : " + record.key() + " \nvalue : " + record.value());
                log.info("Partition : " + record.partition() + " \nOffset : " + record.offset());
            });
        }

    }
}
