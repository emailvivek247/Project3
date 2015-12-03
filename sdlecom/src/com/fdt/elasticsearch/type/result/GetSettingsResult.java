package com.fdt.elasticsearch.type.result;

import io.searchbox.client.JestResult;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GetSettingsResult extends CustomJestResult<JestResult> {

    public GetSettingsResult(JestResult jestResult) {
        super(jestResult);
    }

    public List<String> getIndexNameList() {
        Set<Entry<String, JsonElement>> entrySet = jestResult.getJsonObject().entrySet();
        return entrySet.stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public List<String> getIndexNameList(String regex) {
        return getIndexNameList().stream().filter(p -> p.matches(regex)).collect(Collectors.toList());
    }

    public List<JsonObject> getSettingsList() {
        Set<Entry<String, JsonElement>> entrySet = jestResult.getJsonObject().entrySet();
        return entrySet.stream().map(e -> e.getValue().getAsJsonObject()).collect(Collectors.toList());
    }

    public long getCreationDate(String indexName) {
        return jestResult
                .getJsonObject()
                .getAsJsonObject(indexName)
                .getAsJsonObject("settings")
                .getAsJsonObject("index")
                .get("creation_date")
                .getAsLong();
    }
}
