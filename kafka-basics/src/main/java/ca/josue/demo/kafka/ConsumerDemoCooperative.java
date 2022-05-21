package ca.josue.demo.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
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
public class ConsumerDemoCooperative {

    private static final Logger log = LoggerFactory.getLogger(ConsumerDemoCooperative.class);
    private static final String BOOTSTRAP_SERVERS = "127.0.0.1:9092";
    private static final String GROUP_ID = "my-second-application";
    private static final String TOPIC = "demo_java";

    public static void main(String[] args) {
        log.info("I am kafka Consumer with shutdown and using CooperativeStickyAssignor strategy");

        // create properties
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, CooperativeStickyAssignor.class.getName());

        // create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        //  get a reference to the current thread
        final Thread mainThread = Thread.currentThread();

        // adding the shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Detected a shutdown, let's exit by calling consumer.wakeup()...");
            consumer.wakeup();

            // join the main thread to allow the execution of the code in the main thread
            try{
                mainThread.join();
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        }));

        try {
            // Subscribe consumer to our topic
            consumer.subscribe(List.of(TOPIC));

            // poll for messages
            while (true) {
                ConsumerRecords<String, String> records =
                        consumer.poll(Duration.ofMillis(1000));

                records.forEach((record) -> {
                    log.info("Key : " + record.key() + " \nvalue : " + record.value());
                    log.info("Partition : " + record.partition() + " \nOffset : " + record.offset());
                });
            }
        } catch(WakeupException e){
            log.info("Wake Up Exception ! ");
            // we ignore this as this is an excepted exception when closing a consumer
        } catch(Exception e){
            log.error("Unexpected exception ! " + e.getMessage());
        } finally {
            consumer.close();
            log.info("The consumer is now gracefully closed !");
        }



    }
}
