package com.fdt.security.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.stereotype.Component;

import com.fdt.common.exception.rs.HttpStatusCodes;
import com.fdt.security.exception.MaximumNumberOfAlertsReachedException;

/**
 * @author valampally
 *
 * This Class is a REST Exception Mapper that handles MaximumNumberOfAlertsReachedException, so that the error can be displayed in a nice
 * format with code and Description. If this class is not implemented the response will return the custom 437 error code.
 */
@Component("maximumNumberOfAlertsReachedExceptionMapper")
public class MaximumNumberOfAlertsReachedExceptionMapper implements ExceptionMapper<MaximumNumberOfAlertsReachedException> {

    @Override
    public Response toResponse(MaximumNumberOfAlertsReachedException maximumNumberOfAlertsReachedException) {
        ResponseBuilder builder = Response.status(HttpStatusCodes.MAXIMUM_NUMBER_OF_ALERTS_REACHED_EXCEPTION.value());
        builder.type(MediaType.APPLICATION_JSON);
        builder.entity("{\"errorCode\":\""    + maximumNumberOfAlertsReachedException.getErrorCode() +
                     "\",\"description\":\""  + maximumNumberOfAlertsReachedException.getMessage() + "\"}");
        return builder.build();
    }
}
