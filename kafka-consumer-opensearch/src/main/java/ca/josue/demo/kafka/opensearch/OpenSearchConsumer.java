package ca.josue.demo.kafka.opensearch;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.opensearch.action.admin.indices.create.CreateIndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.GetIndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

/**
 * @author Josue Lubaki
 * @version 1.0
 * @since 2022-05-23
 */
public class OpenSearchConsumer {

    public static RestHighLevelClient createOpenSearchClient() {
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

    public static void main(String[] args) throws IOException {

        Logger log = LoggerFactory.getLogger(OpenSearchConsumer.class.getSimpleName());
        final String INDEX_NAME = "wikimedia";

        // create an OpenSearch Client
        RestHighLevelClient openSearchClient = createOpenSearchClient();

        // We need to create the index on OpenSearch if it doesn't exist already
        try (openSearchClient) {

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
        }

        // create our kafka Client

        // main code logic

        // close things
        openSearchClient.close();
    }
}
