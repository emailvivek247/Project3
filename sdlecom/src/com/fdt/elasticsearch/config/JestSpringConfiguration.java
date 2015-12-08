package com.fdt.elasticsearch.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:elasticsearch.properties")
public class JestSpringConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(JestSpringConfiguration.class);

    @Autowired
    private Environment environment;

    @Bean
    public JestClient jestClient() {

        String endpoint = environment.getProperty("elasticsearch.client.endpoint");

        logger.debug("ES endpoint = " + endpoint);

        HttpClientConfig clientConfig = new HttpClientConfig
                .Builder(endpoint)
                .multiThreaded(true)
                .maxConnectionIdleTime(1, TimeUnit.MINUTES)
                .build();

        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(clientConfig);

        return factory.getObject();
    }

    @Bean
    public Map<String, String> indexSettings() {

        String numberOfShards = environment.getProperty("elasticsearch.index.number_of_shards");
        String numberOfReplicas = environment.getProperty("elasticsearch.index.number_of_replicas");

        Map<String, String> indexSettings = new HashMap<String, String>();
        indexSettings.put("number_of_shards", numberOfShards);
        indexSettings.put("number_of_replicas", numberOfReplicas);

        return indexSettings;
    }
}
