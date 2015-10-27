package com.fdt.elasticsearch.type.result;

import io.searchbox.client.JestResult;

public abstract class CustomJestResult {

    protected JestResult jestResult;

    public CustomJestResult(JestResult jestResult) {
        this.jestResult = jestResult;
    }

}
