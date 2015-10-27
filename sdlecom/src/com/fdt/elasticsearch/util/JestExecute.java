package com.fdt.elasticsearch.util;

import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;

import java.io.IOException;

import com.fdt.elasticsearch.exception.JestFailureException;

public class JestExecute {

    public static <T extends JestResult> T execute(JestClient jestClient, Action<T> action) {
        T result = null;
        try {
            result = jestClient.execute(action);
            if (!result.isSucceeded()) {
                throw new JestFailureException(result.getErrorMessage());
            }
        } catch (IOException e) {
            throw new JestFailureException(e);
        }
        return result;
    }

    public static <T extends JestResult> T executeNoCheck(JestClient jestClient, Action<T> action) {
        T result = null;
        try {
            result = jestClient.execute(action);
        } catch (IOException e) {
            throw new JestFailureException(e);
        }
        return result;
    }
}
