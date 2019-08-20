package pl.postgresql_indexer.indexer.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ElasticsearchConfiguration {

    @Value("${elasticsearch.host}")
    private String esHost;

    @Value("${elasticsearch.port}")
    private int esPort;

    @Value("${elasticsearch.clustername}")
    private String esClusterName;

    @Bean(destroyMethod = "close")
    public RestHighLevelClient client() {

     return new RestHighLevelClient(RestClient
                .builder(new HttpHost(esHost, esPort, "http")));

    }

    public void close() throws IOException {
        client().close();
    }
}
