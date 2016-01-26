/*
 * Copyright 2004.
 */

package net.javacoding.xsearch.config;

import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.commons.digester.BeanPropertySetterRule;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;
import org.xml.sax.Attributes;

import com.fdt.sdl.styledesigner.util.PageStyleUtil;

/**
 * The set of Digester rules required to parse a dataset configuration file.
 */
public class DatasetConfigurationRuleSet extends RuleSetBase {

    // --------------------------------------------------------- Public Methods

    /**
     * Add the set of Rule instances defined in this RuleSet to the specified
     * <code>Digester</code> instance, associating them with our namespace URI
     * (if any). This method should only be called by a Digester instance.
     * These rules assume that an instance of
     * <code>net.javacoding.xsearch.config.DatasetConfiguration</code> is
     * pushed onto the evaluation stack before parsing begins.
     *
     * @param digester Digester instance to which the new Rule instances should
     *                 be added.
     */
    public void addRuleInstances(Digester digester) {
        digester.addSetProperties("dataset", "name", "displayName");
        digester.addBeanPropertySetter("dataset/display-order", "displayOrder");
        digester.addBeanPropertySetter("dataset/description", "description");
        digester.addBeanPropertySetter("dataset/index-dir", "indexdir");
        digester.addBeanPropertySetter("dataset/is-prefix-root-index-directory", "prefixIndexRootDirectory");
        digester.addBeanPropertySetter("dataset/use-server-db-connection", "useServerDBConnection");
        digester.addBeanPropertySetter("dataset/number-of-hours-before-deletion", "numberOfHoursBeforeDeletion");
        digester.addBeanPropertySetter("dataset/work-directory", "workDirectory");
        digester.addBeanPropertySetter("dataset/data-source-type", "dataSourceType");
        digester.addCallMethod("dataset/index-type", "setIndexType", 1);
        digester.addCallParam("dataset/index-type", 0);

        digester.addObjectCreate("dataset/fetcher-configuration", "net.javacoding.xsearch.config.FetcherConfiguration");
        digester.addSetProperties("dataset/fetcher-configuration", "dir", "dir");
        digester.addCallMethod("dataset/fetcher-configuration/properties/entry", "addPair", 2);
        digester.addCallParam("dataset/fetcher-configuration/properties/entry", 0, "key");
        digester.addCallParam("dataset/fetcher-configuration/properties/entry", 1);
        digester.addSetNext("dataset/fetcher-configuration", "setFetcherConfiguration");

        digester.addObjectCreate("dataset/data-sources/data-source", "net.javacoding.xsearch.config.DataSource");
        digester.addBeanPropertySetter("dataset/data-sources/data-source/name", "name");
        digester.addBeanPropertySetter("dataset/data-sources/data-source/driver-directory-name", "driverDirectoryName");
        digester.addBeanPropertySetter("dataset/data-sources/data-source/jdbcdriver", "jdbcdriver");
        digester.addRule("dataset/data-sources/data-source/db-url", new IndexDataSourceRule());
        digester.addRule("dataset/data-sources/data-source/db-username", new IndexDataSourceRule());
        digester.addRule("dataset/data-sources/data-source/db-password", new IndexDataSourceRule());
        digester.addBeanPropertySetter("dataset/data-sources/data-source/dbcp-validation-query", "dbcpValidationQuery");
        digester.addSetNext("dataset/data-sources/data-source", "addDataSource");

        //database connection setting, preserved for upgrading purpose
        digester.addBeanPropertySetter("dataset/jdbcdriver", "jdbcdriver");
        digester.addBeanPropertySetter("dataset/db-url", "dbUrl");
        digester.addBeanPropertySetter("dataset/db-username", "dbUsername");
        digester.addBeanPropertySetter("dataset/db-password", "dbPassword");
        digester.addBeanPropertySetter("dataset/dbcp-validation-query", "dbcpValidationQuery");

        digester.addBeanPropertySetter("dataset/fetcher-threads", "fetcherThreadsCount");
        digester.addBeanPropertySetter("dataset/batch-fetcher-threads", "batchFetcherThreadsCount");
        digester.addBeanPropertySetter("dataset/writer-threads", "writerThreadsCount");
        digester.addBeanPropertySetter("dataset/index-max-size", "indexMaxSize");
        digester.addBeanPropertySetter("dataset/max-merge-docs", "maxMergeDocs");
        digester.addBeanPropertySetter("dataset/is-optimize-needed", "isOptimizeNeeded");
        digester.addBeanPropertySetter("dataset/list-fetch-size", "listFetchSize");
        digester.addBeanPropertySetter("dataset/temporary-index-minimal-merge-percentage", "mergePercentage");
        digester.addBeanPropertySetter("dataset/prune-index-target-percentage", "prunePercentage");
        digester.addBeanPropertySetter("dataset/number-replicas", "numberReplicas");
        digester.addBeanPropertySetter("dataset/number-shards", "numberShards");
        digester.addCallMethod("dataset/merge-hours", "setMergeHours", 3);
        digester.addCallParam("dataset/merge-hours", 0, "enabled");
        digester.addCallParam("dataset/merge-hours", 1, "begin");
        digester.addCallParam("dataset/merge-hours", 2,"end");
        digester.addBeanPropertySetter("dataset/max-field-length", "maxFieldLength");
        digester.addBeanPropertySetter("dataset/jvm-max-heap-size", "jvmMaxHeapSize");
        digester.addBeanPropertySetter("dataset/document-buffer-size-mb", "documentBufferSizeMB");
        digester.addBeanPropertySetter("dataset/language", "language");

        digester.addBeanPropertySetter("dataset/analyzer-name", "analyzerName");
        digester.addBeanPropertySetter("dataset/similarity-name", "similarityName");

        digester.addObjectCreate("dataset/schedules/schedule", "net.javacoding.xsearch.config.Schedule");
        digester.addSetProperties("dataset/schedules/schedule", "id", "id");
        digester.addBeanPropertySetter("dataset/schedules/schedule/is-enabled", "isEnabled");
        digester.addBeanPropertySetter("dataset/schedules/schedule/is-interval", "isInterval");
        digester.addBeanPropertySetter("dataset/schedules/schedule/interval", "interval");
        digester.addBeanPropertySetter("dataset/schedules/schedule/cron-setting", "cronSetting");
        digester.addBeanPropertySetter("dataset/schedules/schedule/indexing-mode", "indexingMode");
        digester.addSetNext("dataset/schedules/schedule", "addSchedule");
        
        //to be deleted start
        digester.addObjectCreate("dataset/schedule", "net.javacoding.xsearch.config.Schedule");
        digester.addBeanPropertySetter("dataset/schedule/is-enabled", "isEnabled");
        digester.addBeanPropertySetter("dataset/schedule/is-interval", "isInterval");
        digester.addBeanPropertySetter("dataset/schedule/interval", "interval");
        digester.addBeanPropertySetter("dataset/schedule/cron-setting", "cronSetting");
        digester.addSetNext("dataset/schedule", "addSchedule");
        //to be deleted end

        digester.addFactoryCreate("dataset/dataquery", new DataqueryFactory());
        digester.addSetProperties("dataset/dataquery", "name", "name");
        digester.addSetProperties("dataset/dataquery", "data-source-name", "dataSourceName");
        digester.addSetProperties("dataset/dataquery", "is-cache-needed", "isCacheNeeded");
        digester.addSetProperties("dataset/dataquery", "is-batch-needed", "isBatchNeeded");
        digester.addSetProperties("dataset/dataquery", "is-delete-only", "isDeleteOnly");
        digester.addSetProperties("dataset/dataquery", "batch-size", "batchSize");
        digester.addSetProperties("dataset/dataquery", "is-skipping-null-parameters", "isSkippingNullParameters");
        digester.addBeanPropertySetter("dataset/dataquery/sql", "sql");

        digester.addObjectCreate("dataset/dataquery/parameter", "net.javacoding.xsearch.config.Parameter");
        digester.addBeanPropertySetter("dataset/dataquery/parameter/name", "name");
        digester.addBeanPropertySetter("dataset/dataquery/parameter/index", "index");
        digester.addBeanPropertySetter("dataset/dataquery/parameter/is-variable-binding", "isVariableBinding");
        digester.addCallMethod("dataset/dataquery/parameter", "setValue", 2);
        digester.addCallParam("dataset/dataquery/parameter/type", 0);
        digester.addCallParam("dataset/dataquery/parameter/value", 1);
        digester.addSetNext("dataset/dataquery/parameter", "addParameter");

        digester.addObjectCreate("dataset/dataquery/column", "net.javacoding.xsearch.config.Column");
        digester.addBeanPropertySetter("dataset/dataquery/column/column-name", "columnName");
        digester.addBeanPropertySetter("dataset/dataquery/column/display-name", "displayName");
        digester.addBeanPropertySetter("dataset/dataquery/column/column-index", "columnIndex");
        digester.addBeanPropertySetter("dataset/dataquery/column/column-type", "columnType");
        digester.addSetProperties("dataset/dataquery/column/column-type", "precision", "columnPrecision");
        digester.addSetProperties("dataset/dataquery/column/column-type", "scale", "columnScale");
        digester.addBeanPropertySetter("dataset/dataquery/column/primary-key", "isPrimaryKey");
        digester.addBeanPropertySetter("dataset/dataquery/column/modified-date", "isModifiedDate");
        digester.addBeanPropertySetter("dataset/dataquery/column/is-date-weight", "isDateWeight");
        digester.addBeanPropertySetter("dataset/dataquery/column/aggregate", "isAggregate");
        digester.addSetProperties("dataset/dataquery/column/aggregate", "separator", "aggregateSeparator");
        digester.addBeanPropertySetter("dataset/dataquery/column/index-field-type", "indexFieldType");
        digester.addBeanPropertySetter("dataset/dataquery/column/is-sortable", "isSortable");
        digester.addSetProperties("dataset/dataquery/column/is-sortable", "sort-display-order", "sortDisplayOrder");
        digester.addSetProperties("dataset/dataquery/column/is-sortable", "descending", "isDescending");
        digester.addBeanPropertySetter("dataset/dataquery/column/is-searchable", "isSearchable");
        digester.addSetProperties("dataset/dataquery/column/is-searchable", "search-weight", "searchWeight");
        digester.addBeanPropertySetter("dataset/dataquery/column/is-filterable", "isFilterable");
        digester.addSetProperties("dataset/dataquery/column/is-filterable", "filter-display-order", "filterDisplayOrder");
        digester.addSetProperties("dataset/dataquery/column/is-filterable", "filter-parent-column-name", "filterParentColumnName");
        //this line is for backward compatible only
        digester.addSetProperties("dataset/dataquery/column/is-filterable", "sum-column-name", "sumColumnName");
        digester.addSetProperties("dataset/dataquery/column/is-filterable", "sum-column-names", "commaSeparatedSumColumnNames");
        digester.addSetProperties("dataset/dataquery/column/is-filterable", "has-multiple-keywords", "hasMultipleKeywords");
        digester.addSetProperties("dataset/dataquery/column/is-filterable", "sort-filter-counts-by", "sortFilterCountsBy");
        digester.addSetProperties("dataset/dataquery/column/is-filterable", "filter-facet-type-name", "filterFacetTypeName");
        digester.addBeanPropertySetter("dataset/dataquery/column/analyzer-name", "analyzerName");
        digester.addBeanPropertySetter("dataset/dataquery/column/need-synonyms-and-stopwords", "needSynonymsAndStopwords");
        digester.addBeanPropertySetter("dataset/dataquery/column/is-secure", "isSecure");
        digester.addBeanPropertySetter("dataset/dataquery/column/is-spell-checking", "isSpellChecking");
        digester.addBeanPropertySetter("dataset/dataquery/column/tag", "tag");

        digester.addSetNext("dataset/dataquery/column", "addColumn");
        digester.addSetNext("dataset/dataquery", "addDataquery");

        digester.addBeanPropertySetter("dataset/default-template", "defaultTemplateName");
        digester.addBeanPropertySetter("dataset/tablet-template", "tabletTemplateName");
        digester.addBeanPropertySetter("dataset/mobile-template", "mobileTemplateName");
        digester.addBeanPropertySetter("dataset/searcher-max-active", "searcherMaxactive");
        digester.addBeanPropertySetter("dataset/searcher-when-exhausted", "searcherWhenexhausted");
        digester.addBeanPropertySetter("dataset/searcher-max-wait", "searcherMaxwait");
        digester.addBeanPropertySetter("dataset/searcher-max-idle", "searcherMaxidle");
        digester.addBeanPropertySetter("dataset/max-open-files", "maxOpenFiles");
        digester.addBeanPropertySetter("dataset/is-wildcard-allowed", "isWildcardAllowed");
        digester.addBeanPropertySetter("dataset/is-wildcard-lowercase-needed", "isWildcardLowercaseNeeded");
        digester.addBeanPropertySetter("dataset/min-wildcard-prefix-length", "minWildcardPrefixLength");
        digester.addBeanPropertySetter("dataset/is-in-memory-search", "isInMemorySearch");
        digester.addBeanPropertySetter("dataset/subscription-url", "subscriptionUrl");
        digester.addBeanPropertySetter("dataset/is-query-default-and", "isQueryDefaultAnd");
        digester.addBeanPropertySetter("dataset/is-secure", "isSecure");
        digester.addBeanPropertySetter("dataset/allowed-ip-list", "allowedIpList");
        digester.addBeanPropertySetter("dataset/is-empty-query-match-all", "isEmptyQueryMatchAll");
        digester.addBeanPropertySetter("dataset/url-to-ping", "urlToPing");
        
        digester.addObjectCreate("dataset/date-weight-formula/time-weight", "net.javacoding.xsearch.config.TimeWeight");
        digester.addSetProperties("dataset/date-weight-formula/time-weight","time", "time");
        digester.addSetProperties("dataset/date-weight-formula/time-weight","weight", "weight");
        digester.addSetNext("dataset/date-weight-formula/time-weight", "addTimeWeight");

        digester.addSetProperties("dataset/spell-checking", "enabled", "isSpellChecking");

    }

}

/**
 * An object creation factory which creates <code>Dataquery</code> instances.
 */
final class DataqueryFactory extends AbstractObjectCreationFactory {

    public Object createObject(Attributes attributes) {
        String dataqueryName = attributes.getValue("name");
        if ("WorkingQueue".equals(dataqueryName)) {
            return new WorkingQueueDataquery();
        } else if ("Content".equals(dataqueryName)) {
            return new ContentDataquery();
        } else if ("Deletion".equals(dataqueryName)) {
            return new DeletionDataquery();
        } else if ("Incremental".equals(dataqueryName)) {
            return new IncrementalDataquery();
        } else {
            return new Dataquery();
        }
    }

}

class IndexDataSourceRule extends BeanPropertySetterRule {
	
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
