package com.fdt.security.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.stereotype.Component;

import com.fdt.common.exception.rs.HttpStatusCodes;
import com.fdt.security.exception.DuplicateAlertException;

/**
 * @author valampally
 *
 * This Class is a REST Exception Mapper that handles DuplicateAlertExceptionMapper, so that the error can be displayed in a nice
 * format with code and Description. If this class is not implemented the response will return the custom 436 error code.
 */
@Component("duplicateAlertExceptionMapper")
public class DuplicateAlertExceptionMapper implements ExceptionMapper<DuplicateAlertException> {

    @Override
    public Response toResponse(DuplicateAlertException duplicateAlertException) {
        ResponseBuilder builder = Response.status(HttpStatusCodes.DUPLICATE_ALERT_EXCEPTION.value());
        builder.type(MediaType.APPLICATION_JSON);
        builder.entity("{\"errorCode\":\""    + duplicateAlertException.getErrorCode() +
                     "\",\"description\":\""  + duplicateAlertException.getMessage() + "\"}");
        return builder.build();
    }
}
