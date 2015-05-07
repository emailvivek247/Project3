package com.fdt.common.util.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service("springUtil")
public class SpringUtil implements ApplicationContextAware, BeanFactoryPostProcessor {

	private final Logger logger = LoggerFactory.getLogger(getClass());

    private ApplicationContext appContext = null;

    private ConfigurableListableBeanFactory factory = null;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return appContext;
    }

    public Object getBean(String beanName) {
        Object bean = null;
        try {
            bean = appContext.getBean(beanName);
        } catch (BeansException beanException) {
        	logger.error("Cannot Create Bean with Bean Name :" + beanName);
        }
        return bean;
    }

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {
		this.factory = factory;
	}

	public ConfigurableListableBeanFactory getFactory() {
	    return factory;
	}
}