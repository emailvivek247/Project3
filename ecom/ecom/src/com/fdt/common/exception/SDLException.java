package com.fdt.common.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SDLException extends Exception {

    private static final long serialVersionUID = 6030735553953912529L;

    /**Stores The Paypal Error Code **/
    private String errorCode = null;

    /**Stores The Error Description. We cannot use Message because jackson cannot Serialize/DeSerialize message **/
    private String description = null;

    public SDLException() {
        super();
    }

    public SDLException(String message) {
        super(message);
        this.description = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}