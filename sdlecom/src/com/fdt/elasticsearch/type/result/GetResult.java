package com.fdt.elasticsearch.type.result;

import io.searchbox.core.DocumentResult;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GetResult extends CustomJestResult<DocumentResult> {

    public GetResult(DocumentResult jestResult) {
        super(jestResult);
    }

    public boolean exists() {
        return jestResult.getJsonObject().get("found").getAsBoolean();
    }

    public Map<String, String> getAsMap() {
        JsonObject sourceObj = jestResult.getJsonObject().getAsJsonObject("_source");
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

}
