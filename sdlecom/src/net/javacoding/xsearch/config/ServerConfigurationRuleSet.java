/*
 * Copyright 2004.
 */

package net.javacoding.xsearch.config;

import org.apache.commons.digester.BeanPropertySetterRule;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

import com.fdt.sdl.styledesigner.util.PageStyleUtil;

/**
 * The set of Digester rules required to parse the server configuration file.
 *
 */
public class ServerConfigurationRuleSet extends RuleSetBase {

    // --------------------------------------------------------- Public Methods

    /**
     * Add the set of Rule instances defined in this RuleSet to the specified
     * <code>Digester</code> instance, associating them with our namespace URI
     * (if any). This method should only be called by a Digester instance.
     * These rules assume that an instance of
     * <code>net.javacoding.xsearch.config.ServerConfiguration</code> is pushed
     * onto the evaluation stack before parsing begins.
     *
     * @param digester Digester instance to which the new Rule instances should
     *                 be added.
     */
    public void addRuleInstances(Digester digester) {
        digester.addSetProperties("xsearch", "basedir", "basedir");
        digester.addSetProperties("xsearch", "password", "password");
        digester.addSetProperties("xsearch", "adminuser", "adminUser");

        digester.addBeanPropertySetter("xsearch/index-root-directory", "indexRootDirectory");
        digester.addBeanPropertySetter("xsearch/is-merging-old-dataset-values", "isMergingOldDatasetValues");
        digester.addBeanPropertySetter("xsearch/search-log-size-in-MB", "searchLogSizeInMB");
        digester.addBeanPropertySetter("xsearch/indexing-log-size-in-MB", "indexingLogSizeInMB");
        digester.addBeanPropertySetter("xsearch/is-short-indexing-log-enabled", "isShortIndexingLogEnabled");

        digester.addBeanPropertySetter("xsearch/registration-information/version", "licenseVersion");
        digester.addBeanPropertySetter("xsearch/registration-information/user", "user");
        digester.addBeanPropertySetter("xsearch/registration-information/license-level", "licenseLevel");
        digester.addBeanPropertySetter("xsearch/registration-information/max-index-size", "maxIndexSize");
        digester.addBeanPropertySetter("xsearch/registration-information/start-date", "startDate");
        digester.addBeanPropertySetter("xsearch/registration-information/upgrade-end-date", "upgradeEndDate");
        digester.addBeanPropertySetter("xsearch/registration-information/end-date", "endDate");
        digester.addBeanPropertySetter("xsearch/registration-information/registration-number", "registrationNumber");
        digester.addBeanPropertySetter("xsearch/registration-information/host-ip-list", "allowedIpsString");
        digester.addBeanPropertySetter("xsearch/registration-information/registration-code", "registrationCode");

        digester.addObjectCreate("xsearch/logging-configuration", "net.javacoding.xsearch.config.LoggingConfiguration");
        digester.addBeanPropertySetter("xsearch/logging-configuration/enabled", "isEnabled");
        digester.addBeanPropertySetter("xsearch/logging-configuration/smtp-host", "smtpHost");
        digester.addBeanPropertySetter("xsearch/logging-configuration/smtp-port", "smtpPort");
        digester.addBeanPropertySetter("xsearch/logging-configuration/to-address", "toAddress");
        digester.addBeanPropertySetter("xsearch/logging-configuration/from-address", "fromAddress");
        digester.addBeanPropertySetter("xsearch/logging-configuration/threshold", "threshold");
        digester.addSetNext("xsearch/logging-configuration", "setLoggingConfiguration");
                
        digester.addObjectCreate("xsearch/data-source", "net.javacoding.xsearch.config.DataSource");
        digester.addBeanPropertySetter("xsearch/data-source/name", "name");
        digester.addBeanPropertySetter("xsearch/data-source/driver-directory-name", "driverDirectoryName");
        digester.addBeanPropertySetter("xsearch/data-source/jdbcdriver", "jdbcdriver");
        digester.addRule("xsearch/data-source/db-url", new ServerDataSourceRule());
        digester.addRule("xsearch/data-source/db-username", new ServerDataSourceRule());
        digester.addRule("xsearch/data-source/db-password", new ServerDataSourceRule());
        digester.addBeanPropertySetter("xsearch/data-source/dbcp-validation-query", "dbcpValidationQuery");
        digester.addSetNext("xsearch/data-source", "setServerDataSource");
                  
        digester.addObjectCreate("xsearch/instance-job-schedules/instance-job-schedule", "net.javacoding.xsearch.config.InstanceJobSchedule");
        digester.addSetProperties("xsearch/instance-job-schedules/instance-job-schedule", "id", "id");
        digester.addBeanPropertySetter("xsearch/instance-job-schedules/instance-job-schedule/schedule-name", "scheduleName");
        digester.addBeanPropertySetter("xsearch/instance-job-schedules/instance-job-schedule/job-data-string", "jobDataString");
        digester.addObjectCreate("xsearch/instance-job-schedules/instance-job-schedule/schedule", "net.javacoding.xsearch.config.Schedule");
        digester.addSetProperties("xsearch/instance-job-schedules/instance-job-schedule/schedule", "id", "id");
        digester.addBeanPropertySetter("xsearch/instance-job-schedules/instance-job-schedule/schedule/is-enabled", "isEnabled");
        digester.addBeanPropertySetter("xsearch/instance-job-schedules/instance-job-schedule/schedule/is-interval", "isInterval");
        digester.addBeanPropertySetter("xsearch/instance-job-schedules/instance-job-schedule/schedule/interval", "interval");
        digester.addBeanPropertySetter("xsearch/instance-job-schedules/instance-job-schedule/schedule/cron-setting", "cronSetting");
        digester.addSetNext("xsearch/instance-job-schedules/instance-job-schedule/schedule", "setSchedule");
        digester.addSetNext("xsearch/instance-job-schedules/instance-job-schedule", "setInstanceJobSchedule");
        
        
        //database connection setting, preserved for upgrading purpose
        digester.addBeanPropertySetter("xsearch/jdbcdriver", "jdbcdriver");
        digester.addBeanPropertySetter("xsearch/db-url", "dbUrl");
        digester.addBeanPropertySetter("xsearch/db-username", "dbUsername");
        digester.addBeanPropertySetter("xsearch/db-password", "dbPassword");
        digester.addBeanPropertySetter("xsearch/dbcp-validation-query", "dbcpValidationQuery");

    }
}

class ServerDataSourceRule extends BeanPropertySetterRule {
	
	public void body(String namespace, String name, String text) {
		if (name.equalsIgnoreCase("db-url")) {
			this.propertyName = "dbUrl";
		} else if (name.equalsIgnoreCase("db-username")) {
			this.propertyName = "dbUsername";
		} else if (name.equalsIgnoreCase("db-password")) {
			this.propertyName = "dbPassword";
		}
		this.bodyText = PageStyleUtil.decrypt(text);
	}
}

