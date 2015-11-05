package com.fdt.elasticsearch.type.result;

import io.searchbox.client.JestResult;

public class GetResult extends CustomJestResult {

    public GetResult(JestResult jestResult) {
        super(jestResult);
    }

    public boolean exists() {
        return jestResult.getJsonObject().get("found").getAsBoolean();
    }

}
