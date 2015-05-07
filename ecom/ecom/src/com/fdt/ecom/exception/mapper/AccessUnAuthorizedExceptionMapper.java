package com.fdt.ecom.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.stereotype.Component;

import com.fdt.ecom.exception.AccessUnAuthorizedException;

/**
 * @author valampally
 *
 * This Class is a REST Exception Mapper that handles AccessUnAuthorizedExceptionMapper, so that the error can be displayed in a nice
 * format with code and Description. If this class is not implemented the response will return the custom 441 error code.
 */
@Component("accessUnAuthorizedExceptionMapper")
public class AccessUnAuthorizedExceptionMapper implements ExceptionMapper<AccessUnAuthorizedException> {

    @Override
    public Response toResponse(AccessUnAuthorizedException accessUnAuthorizedException) {
        ResponseBuilder builder = Response.status(441);
        builder.type(MediaType.APPLICATION_JSON);
        builder.entity("{\"errorCode\":\""    + accessUnAuthorizedException.getErrorCode() +
                     "\",\"description\":\""  + accessUnAuthorizedException.getMessage() + "\"}");
        return builder.build();
    }
}
