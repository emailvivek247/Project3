package com.fdt.elasticsearch.type.result;

import io.searchbox.core.DocumentResult;

import java.util.Map;
import java.util.stream.Collectors;

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
        return sourceObj.entrySet().stream().collect(
                Collectors.toMap(e -> e.getKey(), e -> e.getValue().getAsString())
        );
    }

}
