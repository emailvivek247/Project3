package com.fdt.security.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.stereotype.Component;

import com.fdt.common.exception.rs.HttpStatusCodes;
import com.fdt.security.exception.MaxUsersExceededException;

/**
 * @author APatel
 *
 * This Class is a REST Exception Mapper that handles MaxUsersExceededExceptionMapper, so that the error can be displayed in a nice
 * format with code and Description. If this class is not implemented the response will return the custom HttpStatusCodes.MAX_USERS_EXCEEDED_EXCEPTION error code.
 */
@Component("maxUsersExceededExceptionMapper")
public class MaxUsersExceededExceptionMapper implements ExceptionMapper<MaxUsersExceededException> {

    @Override
    public Response toResponse(MaxUsersExceededException maxUsersExceededException) {
        ResponseBuilder builder = Response.status(HttpStatusCodes.MAX_USERS_EXCEEDED_EXCEPTION.value());
        builder.type(MediaType.APPLICATION_JSON);
        builder.entity("{\"errorCode\":\""    + maxUsersExceededException.getErrorCode() +
                     "\",\"description\":\""  + maxUsersExceededException.getMessage() + "\"}");
        return builder.build();
    }
}
