package ca.josue.demo.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author Josue Lubaki
 * @version 1.0
 * @since 2022-05-20
 */
public class ProducerDemoKeys {

    private static final Logger log = LoggerFactory.getLogger(ProducerDemoKeys.class);

    public static void main(String[] args) {
        log.info("I am kafka ProducerDemoCallback");
        String topic = "demo_java";

        // create Producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // create producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for (int i = 0; i < 10; i++) {

            String value = "hello world " + i;
            String key = "id_" + i;

            // create a producer record
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);

            // send message - asynchronously
            producer.send(record, (metadata, error) -> {
                // execute every time a record is successfully sent or an exception is thrown
                if (error == null) {
                    log.info("Received new metadata/ \n" +
                            "Topic : " + metadata.topic() + "\n" +
                            "Key    : " + record.key() + "\n" +
                            "Partitions : " + metadata.partition() + "\n" +
                            "Offset : " + metadata.offset() + "\n" +
                            "Timestamp : " + metadata.timestamp()
                    );
                } else {
                    // something went wrong
                    log.error("Error while producing", error);
                }
            });
        }

        // flush data - synchronously and close the producer
        producer.flush();
        producer.close();
    }

}
