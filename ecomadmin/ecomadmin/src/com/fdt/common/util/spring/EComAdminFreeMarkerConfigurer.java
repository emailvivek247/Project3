package com.fdt.common.util.spring;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import freemarker.template.TemplateException;

/**
 * This class is a custom Freemarker Configurer. This class is used to set SL4J logger to Freemarker
 *
 * @author smani
 *
 */
public class EComAdminFreeMarkerConfigurer extends FreeMarkerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(EComAdminFreeMarkerConfigurer.class);

    @Override
    public void afterPropertiesSet() throws IOException, TemplateException {
        fixFreemarkerLogging();
        super.afterPropertiesSet();
    }

    private void fixFreemarkerLogging() {
        try {
            freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_SLF4J);
            logger.info("Switched broken Freemarker logging to slf4j");
        } catch (ClassNotFoundException e) {
            logger.warn("Failed to switch broken Freemarker logging to slf4j");
        }
    }
}
