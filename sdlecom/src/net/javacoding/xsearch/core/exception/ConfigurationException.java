/*
 * Copyright 2004.
 */

package net.javacoding.xsearch.core.exception;

/**
 * Signals that an error occurs during configuration.
 *
 */
public class ConfigurationException extends Exception {
    
    /**
     * Constructs an exception with no descriptive information.
     */
    public ConfigurationException() {
        super();
    }

    /**
     * Constructs an exception with the given descriptive message.
     *
     * @param message a description of or information about the exception.
     *                Should not be <code>null</code>.
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the specified cause and a detail message of
     * <code>(cause==null ? null : cause.toString())</code>
     *
     * @param cause the cause
     */
    public ConfigurationException(Throwable cause) {
        super(cause);
    }

}
