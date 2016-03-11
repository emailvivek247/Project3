package com.fdt.elasticsearch.config;

import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }

    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    public static Properties getElasticsearchProperties() {
        return (Properties) applicationContext.getBean("elasticsearchProperties");
    }

    public static int getBatchSize() {
        return Integer.parseInt(getElasticsearchProperties().getProperty("elasticsearch.indexing.batch_size"));
    }

    public static int getBatchesInQueue() {
        return Integer.parseInt(getElasticsearchProperties().getProperty("elasticsearch.indexing.batches_in_queue"));
    }

    public static int getAwaitTermTime() {
        return Integer.parseInt(getElasticsearchProperties().getProperty("elasticsearch.indexing.await_term_time"));
    }

    public static int getNumConsumerThreads() {
        return Integer.parseInt(getElasticsearchProperties().getProperty("elasticsearch.indexing.num_consumer_threads"));
    }

    public static int getNumIndexVersionsToKeep() {
        return Integer.parseInt(getElasticsearchProperties().getProperty("elasticsearch.indexing.num_index_versions_to_keep"));
    }

    public static int getMaxResultWindow() {
        return Integer.parseInt(getElasticsearchProperties().getProperty("elasticsearch.index.max_result_window"));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }

}
