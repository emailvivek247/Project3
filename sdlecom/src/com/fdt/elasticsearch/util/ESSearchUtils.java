package com.fdt.elasticsearch.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.javacoding.xsearch.api.Document;
import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.search.result.filter.Count;
import net.javacoding.xsearch.search.result.filter.FilterColumn;
import net.javacoding.xsearch.search.result.filter.FilterResult;

import com.fdt.elasticsearch.type.result.CustomSearchResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ESSearchUtils {

    /**
     * Populates FilterResult for Elasticsearch index types
     * 
     * @param filterResult the object to populate
     * @param result the Elasticsearch search result (JSON in Google's GSON format)
     * @param dc the dataset configuration for the active search
     */
    public static void populateFilterResult(FilterResult filterResult, CustomSearchResult result, DatasetConfiguration dc) {
        filterResult.setFilterColumns(dc.getFilterableColumns());
        JsonObject aggregations = result.getAggregations();
        for (Column column : dc.getFilterableColumns()) {
            String parentFilter = column.getFilterParentColumnName();
            if (parentFilter == null || filterResult.hasFilteredColumn(parentFilter)) {
                String columnName = column.getColumnName();
                if (column.getIsDate()) {
                    String suffix = "-yyyy";
                    if (filterResult.getFilteredColumn(column.getColumnName()) != null) {
                        suffix = "-yyyy/MM";
                    }
                    for (Map.Entry<String, JsonElement> aggregation : aggregations.entrySet()) {
                        if (aggregation.getKey().equals(columnName + suffix)) {
                            FilterColumn filterColumn = filterResult.getFilterColumn(columnName);
                            if (filterColumn != null) {
                                JsonObject aggValueObject = aggregation.getValue().getAsJsonObject();
                                JsonArray bucketsArray = aggValueObject.getAsJsonArray("buckets");
                                Map<Object, Count> counts = new HashMap<Object, Count>();
                                for (JsonElement bucketElement : bucketsArray) {
                                    JsonObject bucket = bucketElement.getAsJsonObject();
                                    int docCount = bucket.get("doc_count").getAsInt();
                                    String key = bucket.get("key_as_string").getAsString();
                                    if (docCount > 0) {
                                        Count count = new Count(columnName, key, docCount);
                                        counts.put(key, count);
                                    }
                                }
                                filterColumn.setCounts(counts);
                            }
                        }
                    }
                } else {
                    for (Map.Entry<String, JsonElement> aggregation : aggregations.entrySet()) {
                        if (aggregation.getKey().equals(columnName)) {
                            FilterColumn filterColumn = filterResult.getFilterColumn(columnName);
                            if (filterColumn != null && !filterResult.hasFilteredColumn(columnName)) {
                                JsonObject aggValueObject = aggregation.getValue().getAsJsonObject();
                                JsonArray bucketsArray = aggValueObject.getAsJsonArray("buckets");
                                Map<Object, Count> counts = new HashMap<Object, Count>();
                                for (JsonElement bucketElement : bucketsArray) {
                                    JsonObject bucket = bucketElement.getAsJsonObject();
                                    int docCount = bucket.get("doc_count").getAsInt();
                                    String key = bucket.get("key").getAsString();
                                    Count count = new Count(columnName, key, docCount);
                                    counts.put(key, count);
                                }
                                filterColumn.setCounts(counts);
                            }
                        }
                    }
                }
            }
        }
        filterResult.finish();
    }

    public static List<Document> extractResultDocs(CustomSearchResult result) {
        return result.stream().map(hit -> new Document(hit)).collect(Collectors.toList());
    }
}
