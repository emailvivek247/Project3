package com.fdt.elasticsearch.type.result;

import io.searchbox.client.JestResult;

public abstract class CustomJestResult<T extends JestResult> {

    protected T jestResult;

    public CustomJestResult(T jestResult) {
        this.jestResult = jestResult;
    }

}
