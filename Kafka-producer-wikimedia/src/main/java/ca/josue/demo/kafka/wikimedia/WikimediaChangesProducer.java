package ca.josue.demo.kafka.wikimedia;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.net.URI;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author Josue Lubaki
 * @version 1.0
 * @since 2022-05-22
 */
public class WikimediaChangesProducer {
    public static void main(String[] args) throws InterruptedException {

        final String BOOTSTRAP_SERVERS = "127.0.0.1:9092";
        final String TOPIC = "wikimedia.recentchange";
        final String URL = "https://stream.wikimedia.org/v2/stream/recentchange";

        // create Producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // create a producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // create EventHandler & EventSource
        EventHandler eventHandler = new WikimediaChangeHandler(producer, TOPIC);
        EventSource eventSource = new EventSource
                .Builder(eventHandler, URI.create(URL))
                .build();

        // start the producer in another thread
        eventSource.start();

        // we produce for 10 minutes and block the program until then
        TimeUnit.MINUTES.sleep(10);
    }
}
