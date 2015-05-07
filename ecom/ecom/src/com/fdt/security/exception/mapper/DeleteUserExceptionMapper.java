package com.fdt.security.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.stereotype.Component;

import com.fdt.common.exception.rs.HttpStatusCodes;
import com.fdt.security.exception.DeleteUserException;

/**
 * @author APatel
 *
 * This Class is a REST Exception Mapper that handles DeleteUserException, so that the error can be displayed in a nice
 * format with code and Description. If this class is not implemented the response will return the custom HttpStatusCodes.DELETE_USER_EXCEPTION error code.
 */
@Component("deleteUserExceptionMapper")
public class DeleteUserExceptionMapper implements ExceptionMapper<DeleteUserException> {

    @Override
    public Response toResponse(DeleteUserException deleteUserException) {
        ResponseBuilder builder = Response.status(HttpStatusCodes.DELETE_USER_EXCEPTION.value());
        builder.type(MediaType.APPLICATION_JSON);
        builder.entity("{\"errorCode\":\""    + deleteUserException.getErrorCode() +
                     "\",\"description\":\""  + deleteUserException.getMessage() + "\"}");
        return builder.build();
    }
}
