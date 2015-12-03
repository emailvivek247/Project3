package com.fdt.elasticsearch.type.result;

import io.searchbox.client.JestResult;

public class StatsResult extends CustomJestResult<JestResult> {

    public StatsResult(JestResult jestResult) {
        super(jestResult);
    }

    public long getSizeForIndex(String indexName) {
        return jestResult.getJsonObject()
                .getAsJsonObject("indices")
                .getAsJsonObject(indexName)
                .getAsJsonObject("total")
                .getAsJsonObject("store")
                .get("size_in_bytes")
                .getAsLong();
    }

    public long getSizeForFirstIndex() {
        return jestResult.getJsonObject()
                .getAsJsonObject("indices")
                .entrySet()
                .stream()
                .findFirst()
                .get()
                .getValue()
                .getAsJsonObject()
                .getAsJsonObject("total")
                .getAsJsonObject("store")
                .get("size_in_bytes")
                .getAsLong();
    }
}
