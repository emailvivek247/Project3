package com.fdt.elasticsearch.type.result;

import io.searchbox.core.SearchResult;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CustomSearchResult extends CustomJestResult<SearchResult>  {

    public CustomSearchResult(SearchResult jestResult) {
        super(jestResult);
    }

    public Stream<JsonObject> stream() {
        JsonArray hitsArray = jestResult.getJsonObject().getAsJsonObject("hits").getAsJsonArray("hits");
        return StreamSupport.stream(hitsArray.spliterator(), false).map(e -> e.getAsJsonObject());
    }

    public Map<String, String> getResultAsMap(int i) {
        JsonArray hitsArray = jestResult.getJsonObject().getAsJsonObject("hits").getAsJsonArray("hits");
        JsonObject sourceObj = hitsArray.get(i).getAsJsonObject().getAsJsonObject("_source");
        Function<Entry<String, JsonElement>, String> valueMapper = (e) -> {
            JsonElement jsonElement = e.getValue();
            if (jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                return StreamSupport.stream(jsonArray.spliterator(), false)
                        .map(j -> j.getAsString())
                        .collect(Collectors.joining("; "));
            } else {
                return jsonElement.getAsString();
            }
        };
        return sourceObj.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), valueMapper));
    }

    public JsonObject getAggregations() {
        return jestResult.getJsonObject().getAsJsonObject("aggregations");
    }

    public int getTotal() {
        return jestResult.getTotal();
    }
}
