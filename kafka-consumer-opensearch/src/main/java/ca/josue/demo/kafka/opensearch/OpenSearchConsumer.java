package ca.josue.demo.kafka.opensearch;

import com.google.gson.JsonParser;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.opensearch.action.admin.indices.create.CreateIndexRequest;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * @author Josue Lubaki
 * @version 1.0
 * @since 2022-05-23
 */
public class OpenSearchConsumer {
    public static void main(String[] args) throws IOException {

        Logger log = LoggerFactory.getLogger(OpenSearchConsumer.class.getSimpleName());
        final String INDEX_NAME = "wikimedia";
        final String TOPIC = "wikimedia.recentchange";

        // create an OpenSearch Client
        RestHighLevelClient openSearchClient = createOpenSearchClient();

        // create our kafka Client
        KafkaConsumer<String, String> consumer = createKafkaConsumer();

        // We need to create the index on OpenSearch if it doesn't exist already
        try (openSearchClient; consumer) {

            boolean indexExists = openSearchClient
                    .indices()
                    .exists(new GetIndexRequest(INDEX_NAME), RequestOptions.DEFAULT);

            if (!indexExists) {
                CreateIndexRequest indexRequest = new CreateIndexRequest(INDEX_NAME);
                openSearchClient.indices().create(indexRequest, RequestOptions.DEFAULT);
                log.info("The index " + INDEX_NAME + " was created successfully");
            } else {
                log.info("The index " + INDEX_NAME + " already exists");
            }

            // subscribe to our topic
            consumer.subscribe(Collections.singleton(TOPIC));

            while(true){
                // poll for messages
                ConsumerRecords<String, String> records =
                        consumer.poll(Duration.ofMillis(3000));

                int recordCount = records.count();
                log.info("Received " + recordCount + " records.");

                for(ConsumerRecord<String, String> record : records){

                    // strategies 1
                    // define an ID using Kafka Record coordinates
                    //String id = record.topic() + "_" + record.partition() + "_" + record.offset();

                    try {
                        // strategies 2
                        // we extract the ID from the JSON value
                        String id = extractId(record.value());

                        // send the message to OpenSearch
                        IndexRequest indexRequest =
                                new IndexRequest(INDEX_NAME)
                                        .source(record.value(), XContentType.JSON)
                                        .id(id);

                        IndexResponse response = openSearchClient.index(indexRequest, RequestOptions.DEFAULT);
                        log.info("ID response : " + response.getId());
                    } catch (Exception e){
                        log.error("Error while sending message to OpenSearch : {}", e.getMessage());
                    }
                }

                // commit offsets after the batch is consumed
                consumer.commitAsync();
                log.info("Offsets have been committed");
            }
        }

        // main code logic

        // close things
        //openSearchClient.close();
    }

    private static String extractId(String json) {
        // gson library
        return JsonParser.parseString(json)
                .getAsJsonObject()
                .get("meta")
                .getAsJsonObject()
                .get("id")
                .getAsString();
    }

    /**
     * Function to create a openSearch client
     * @return RestHighLevelClient
     **/
    private static RestHighLevelClient createOpenSearchClient() {
        // OpenSearch server
        // String connectionUrl = "http://localhost:9200";

        // Bonsai server
        String connectionUrl = "https://x06splfe72:ie9ki4x5zy@kafka-course-4347232978.us-east-1.bonsaisearch.net:443";

        // we build a URI from the connection string
        RestHighLevelClient restHighLevelClient;
        URI connectionUri = URI.create(connectionUrl);

        // extract login information if it exists
        String userInfo = connectionUri.getUserInfo();

        if (userInfo == null) {
            restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(
                                    connectionUri.getHost(),
                                    connectionUri.getPort(),
                                    connectionUri.getScheme()
                            )
                    )
            );
        }
        else {
            String[] auth = userInfo.split(":");

            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(auth[0], auth[1])
            );

            restHighLevelClient = new RestHighLevelClient(
                    RestClient
                            .builder(
                                    new HttpHost(
                                            connectionUri.getHost(),
                                            connectionUri.getPort(),
                                            connectionUri.getScheme()
                                    )
                            )
                            .setHttpClientConfigCallback(httpClientBuilder ->
                                    httpClientBuilder
                                            .setDefaultCredentialsProvider(credentialsProvider)
                                            .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
                            )
            );
        }

        return restHighLevelClient;
    }

    /**
     * Function to create a kafka consumer
     * @return KafkaConsumer<String, String>
     **/
    private static KafkaConsumer<String, String> createKafkaConsumer() {
        final String BOOTSTRAP_SERVERS = "127.0.0.1:9092";
        final String GROUP_ID = "consumer-opensearch-demo";

        // create properties
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        // create consumer
        return new KafkaConsumer<>(properties);
    }
}
