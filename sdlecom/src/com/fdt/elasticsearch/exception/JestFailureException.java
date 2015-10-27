package com.fdt.elasticsearch.exception;

public class JestFailureException extends RuntimeException {

    private static final long serialVersionUID = -8521944519292988319L;

    public JestFailureException(String errorMessage) {
        super(errorMessage);
    }

    public JestFailureException(Throwable t) {
        super(t);
    }

}
