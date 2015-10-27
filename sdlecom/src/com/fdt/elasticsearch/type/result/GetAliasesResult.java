package com.fdt.elasticsearch.type.result;

import io.searchbox.client.JestResult;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;

public class GetAliasesResult extends CustomJestResult {

    public GetAliasesResult(JestResult jestResult) {
        super(jestResult);
    }

    public List<String> getIndexNameList() {
        Set<Entry<String, JsonElement>> entrySet = jestResult.getJsonObject().entrySet();
        return entrySet.stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public List<String> getIndexNameList(String regex) {
        return getIndexNameList().stream().filter(p -> p.matches(regex)).collect(Collectors.toList());
    }

    public List<String> getAliases(String indexName) {
        Set<Entry<String, JsonElement>> entrySet = jestResult
                .getJsonObject()
                .getAsJsonObject(indexName)
                .getAsJsonObject("aliases")
                .entrySet();
        return entrySet.stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

}
